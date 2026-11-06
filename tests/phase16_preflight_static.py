#!/usr/bin/env python3
"""Read-only phase-16 preflight. Parses sources; NEVER invokes Gradle, Java, SDK or a build."""
from pathlib import Path
import re
import sys
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "app/src/main"
ANDROID = "{http://schemas.android.com/apk/res/android}"
MODULES = ROOT / "app/build.gradle"
APP_JAVA = APP / "java"

def read(path):
    return Path(path).read_text(encoding="utf-8")

def values():
    result = {kind: set() for kind in ("string", "color", "array", "style")}
    for path in (APP / "res/values").glob("*.xml"):
        doc = ET.parse(path).getroot()
        for node in doc:
            name = node.get("name")
            if not name:
                continue
            tag = node.tag
            kind = "array" if tag.endswith("-array") else tag
            if kind in result:
                result[kind].add(name)
    return result

def resources():
    available = values()
    ids = set()
    available["layout"] = set()
    for path in (APP / "res/layout").glob("*.xml"):
        root = ET.parse(path).getroot()
        available["layout"].add(path.stem)
        local = []
        for node in root.iter():
            key = node.attrib.get(ANDROID + "id", "")
            if key.startswith("@+id/"):
                local.append(key.split("/", 1)[1])
        if len(local) != len(set(local)):
            raise AssertionError(f"IDs duplicados: {path}")
        ids.update(local)
    available["id"] = ids
    return available

class PreflightNoBuild(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.res = resources()

    def test_project_modules_and_pinned_versions(self):
        root = read(ROOT / "settings.gradle")
        plugin = read(ROOT / "build.gradle")
        core = read(ROOT / "core/build.gradle")
        app = read(MODULES)
        self.assertIn("include ':app', ':core'", root)
        self.assertRegex(plugin, r"version\s+'8\.13\.2'")
        self.assertIn("id 'java-library'", core)
        self.assertIn("JavaLanguageVersion.of(17)", core)
        for value in ("compileSdk 36", "targetSdk 36", "minSdk 26",
                      "JavaVersion.VERSION_17", "applicationId 'pe.asterionsystem.app'"):
            self.assertIn(value, app)
        self.assertIn("versionCode 1", app)
        self.assertIn("versionName '0.1.0'", app)

    def test_manifest_launcher_and_privacy(self):
        root = ET.parse(APP / "AndroidManifest.xml").getroot()
        application = root.find("application")
        self.assertIsNotNone(application)
        self.assertEqual("false", application.get(ANDROID + "allowBackup"))
        self.assertEqual("false", application.get(ANDROID + "usesCleartextTraffic"))
        self.assertEqual("@string/app_name", application.get(ANDROID + "label"))
        activity = application.find("activity")
        self.assertEqual(".SplashActivity", activity.get(ANDROID + "name"))
        self.assertEqual("true", activity.get(ANDROID + "exported"))
        actions = [node.get(ANDROID + "name") for node in activity.findall("./intent-filter/action")]
        self.assertIn("android.intent.action.MAIN", actions)
        categories = [node.get(ANDROID + "name")
                      for node in activity.findall("./intent-filter/category")]
        self.assertIn("android.intent.category.LAUNCHER", categories)
        main_activity = next((node for node in application.findall("activity")
                              if node.get(ANDROID + "name") == ".MainActivity"), None)
        self.assertIsNotNone(main_activity)
        self.assertEqual("false", main_activity.get(ANDROID + "exported"))
        permissions = [node.get(ANDROID + "name") for node in root.findall("uses-permission")]
        self.assertNotIn("android.permission.INTERNET", permissions)

    def test_all_native_layout_xml_resources_resolve(self):
        for path in (APP / "res/layout").glob("*.xml"):
            root = ET.parse(path).getroot()
            for element in root.iter():
                for value in element.attrib.values():
                    match = re.fullmatch(r"@(string|color|array|layout|style|id)/([A-Za-z_0-9.]+)", value)
                    if match:
                        self.assertIn(match.group(2), self.res[match.group(1)], str(path))
        self.assertTrue(self.res["layout"])
        self.assertIn("bootstrap_root", self.res["id"])

    def test_java_local_r_references_resolve(self):
        for path in APP_JAVA.rglob("*.java"):
            source = read(path)
            for match in re.finditer(
                    r"(?<!android\.)\bR\.(id|string|color|array|layout|style)\.([A-Za-z_0-9]+)",
                    source):
                self.assertIn(match.group(2), self.res[match.group(1)],
                              f"{path.relative_to(ROOT)}: {match.group(0)}")

    def test_java_generated_r_imports_are_declared(self):
        """A nested-package class must import the application's generated R."""
        for path in APP_JAVA.rglob("*.java"):
            source = read(path)
            package = re.search(r"(?m)^\s*package\s+([\w.]+)\s*;", source)
            self.assertIsNotNone(package, str(path))
            if package.group(1) == "pe.asterionsystem.app":
                continue  # R is in the same package.
            uses_app_r = re.search(
                r"(?<![\w.])R\.(?:id|string|color|array|layout|drawable|style)\.[A-Za-z_0-9]+",
                source)
            if uses_app_r:
                self.assertRegex(source, r"(?m)^import pe\.asterionsystem\.app\.R;",
                                 f"Missing R import: {path.relative_to(ROOT)}")

    def test_androidx_back_dispatcher_contract(self):
        """Back navigation is handled through AndroidX rather than overriding onBackPressed."""
        main = read(APP_JAVA / "pe/asterionsystem/app/MainActivity.java")
        app = read(MODULES)
        properties = read(ROOT / "gradle.properties")
        self.assertIn("extends ComponentActivity", main)
        self.assertIn("import androidx.activity.OnBackPressedCallback;", main)
        self.assertIn("getOnBackPressedDispatcher().addCallback(this, navigationBackCallback);", main)
        self.assertIn("navigationBackCallback.setEnabled(navigation.state().canGoBack());", main)
        self.assertIn("navigation.goBack();", main)
        self.assertNotIn("void onBackPressed(", main)
        self.assertNotIn("OnBackInvokedCallback", main)
        self.assertIn("android.useAndroidX=true", properties)
        self.assertIn("implementation 'androidx.activity:activity:1.11.0'", app)

    def test_modular_progress_engine_is_pure_java(self):
        module = ROOT / "progress-engine"
        settings = read(ROOT / "settings.gradle")
        app = read(MODULES)
        library = read(module / "build.gradle")
        workflow = read(ROOT / ".github/workflows/android-ci.yml")
        self.assertIn("':progress-engine'", settings)
        self.assertIn("implementation project(':progress-engine')", app)
        self.assertIn("id 'java-library'", library)
        self.assertIn("api project(':core')", library)
        self.assertIn("JavaLanguageVersion.of(17)", library)
        self.assertIn(":progress-engine:test", workflow)
        sources = list((module / "src/main/java").rglob("*.java"))
        self.assertGreaterEqual(len(sources), 8)
        for source_file in sources:
            content = read(source_file)
            self.assertNotRegex(content, r"(?m)^\s*import (android|androidx)\.")
            self.assertNotIn("SQLiteDatabase", content)
            self.assertNotIn("SharedPreferences", content)
        self.assertFalse(list(module.rglob("*.xml")))

    def test_versioned_exercise_catalog_taxonomy_and_asset_adapter(self):
        """Exercise rows must be valid and accessible to Android without a SQL migration."""
        root = ROOT / "progress-engine/src/main/resources/asterion_exercises/v1"
        app_gradle = read(MODULES)
        adapter = read(APP_JAVA / "pe/asterionsystem/app/data/AssetExerciseCatalogRepository.java")
        self.assertIn("assets.srcDir '../progress-engine/src/main/resources'", app_gradle)
        self.assertIn("ExerciseCatalogLoader.load(families, types, variants)", adapter)
        self.assertIn("getAssets().open", adapter)
        self.assertIn("Looper.getMainLooper()", adapter)

        def read_rows(filename, fields):
            text = read(root / filename).splitlines()
            self.assertEqual(fields, text[0].split("\t"))
            rows = [line.split("\t") for line in text[1:] if line and not line.startswith("#")]
            self.assertTrue(all(len(row) == len(fields) for row in rows))
            return rows

        families = read_rows("families.tsv",
                             ["family_id", "name", "category", "pattern"])
        types = read_rows("types.tsv", ["type_id", "family_id", "name"])
        variants = read_rows("variants.tsv",
                             ["variant_id", "type_id", "name", "level",
                              "equipment", "execution", "supervision"])
        self.assertEqual((39, 80, 445), (len(families), len(types), len(variants)))
        family_ids = {row[0] for row in families}
        type_ids = {row[0] for row in types}
        self.assertEqual(len(family_ids), len(families))
        self.assertEqual(len(type_ids), len(types))
        self.assertEqual(len({row[0] for row in variants}), len(variants))
        self.assertTrue(all(row[1] in family_ids for row in types))
        self.assertTrue(all(row[1] in type_ids for row in variants))
        self.assertTrue(all(any(row[1] == family for row in types) for family in family_ids))
        self.assertTrue(all(any(row[1] == kind for row in variants) for kind in type_ids))
        previous = {}
        for row in variants:
            level = int(row[3])
            self.assertIn(level, (1, 2, 3, 4, 5))
            self.assertGreaterEqual(level, previous.get(row[1], 0))
            previous[row[1]] = level
            self.assertTrue(row[4] and row[5])
            self.assertIn(row[6], ("0", "1"))
            if level >= 4:
                self.assertEqual("1", row[6], row[0])

    def test_sdk36_edge_to_edge_safe_insets(self):
        main = read(APP_JAVA / "pe/asterionsystem/app/MainActivity.java")
        xml = read(APP / "res/layout/activity_main.xml")
        self.assertIn("configureSafeSystemInsets();", main)
        self.assertIn("Build.VERSION.SDK_INT < 35", main)
        self.assertIn("WindowInsets.Type.systemBars()", main)
        self.assertIn("WindowInsets.Type.displayCutout()", main)
        self.assertIn("root.setOnApplyWindowInsetsListener(", main)
        self.assertNotIn("fitsSystemWindows", xml)

    def test_sqlite_v5_and_private_preferences_untouched(self):
        db = read(APP_JAVA / "pe/asterionsystem/app/data/AsterionDatabaseHelper.java")
        pref = read(APP_JAVA / "pe/asterionsystem/app/platform/AndroidPreferencesRepository.java")
        self.assertIn("DATABASE_VERSION = 5", db)
        self.assertIn("NonDestructiveDatabaseErrorHandler", db)
        self.assertNotIn("DROP TABLE", db.upper())
        self.assertIn("Context.MODE_PRIVATE", pref)

    def test_approved_debug_ci_triggers_and_secrets_excluded(self):
        workflow = read(ROOT / ".github/workflows/android-ci.yml")
        self.assertRegex(workflow, r"(?m)^  workflow_dispatch:")
        self.assertIn("  push:\n    branches:\n      - main\n", workflow)
        self.assertNotRegex(workflow, r"(?m)^  (pull_request|schedule):")
        self.assertIn("gradle-version: '8.13'", workflow)
        ignores = read(ROOT / ".gitignore")
        for pattern in ("*.apk", "*.aab", "*.jks", "*.keystore", "local.properties"):
            self.assertIn(pattern, ignores)
        for ext in (".apk", ".aab", ".jks", ".keystore"):
            self.assertFalse(list(ROOT.rglob("*" + ext)), f"Archivo no permitido: {ext}")
        self.assertFalse(list(APP.rglob("*.html")))
        self.assertFalse(list(APP.rglob("*.js")))

if __name__ == "__main__":
    print("Fase 16: inspección estática; no compila ni genera APK.")
    if not (ROOT / "gradle/wrapper/gradle-wrapper.properties").is_file():
        print("AVISO: no hay Gradle Wrapper; el workflow manual instala Gradle 8.13.")
    unittest.main(verbosity=2)
