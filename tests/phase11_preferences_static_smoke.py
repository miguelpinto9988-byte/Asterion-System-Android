#!/usr/bin/env python3
"""Source-level contract checks for phase 11. No SDK, Gradle or APK is involved."""
from pathlib import Path
import re
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "app/src/main"
CORE = ROOT / "core/src/main/java/pe/asterionsystem/core"
JAVA = APP / "java/pe/asterionsystem/app"
ANDROID_NS = "{http://schemas.android.com/apk/res/android}"


def java(path: str) -> str:
    return (JAVA / path).read_text(encoding="utf-8")


class SettingsSourceTest(unittest.TestCase):
    def test_settings_xml_and_labels_are_valid(self):
        layout = ET.parse(APP / "res/layout/screen_settings.xml").getroot()
        strings = ET.parse(APP / "res/values/strings.xml").getroot()
        defined = {element.attrib["name"] for element in strings.findall("string")}
        ids = {view.attrib[ANDROID_NS + "id"].split("/")[-1]
               for view in layout.iter() if ANDROID_NS + "id" in view.attrib}
        needed = {"settings_text_group", "settings_text_normal", "settings_text_large",
                  "settings_density_group", "settings_density_normal", "settings_density_compact",
                  "settings_start_group", "settings_start_home", "settings_start_catalog",
                  "settings_start_plan", "settings_start_progress", "settings_reset", "settings_back"}
        self.assertTrue(needed.issubset(ids))
        for view in layout.iter():
            label = view.attrib.get(ANDROID_NS + "text", "")
            if label.startswith("@string/"):
                self.assertIn(label.split("/", 1)[1], defined)

    def test_persistence_is_private_sharedpreferences_only(self):
        source = java("platform/AndroidPreferencesRepository.java")
        self.assertIn("getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)", source)
        self.assertIn("values.edit().clear().apply()", source)
        self.assertNotIn("import android.database", source)
        self.assertIn("catch (ClassCastException wrongType)", source)
        db = java("data/AsterionDatabaseHelper.java")
        self.assertIn("DATABASE_VERSION = 5", db)
        self.assertNotIn("CREATE_SETTINGS_TABLE", db)

    def test_core_has_no_android_sdk_dependency(self):
        for name in ("AppPreferences", "PreferencesRepository", "PreferencesService"):
            source = (CORE / "settings" / (name + ".java")).read_text(encoding="utf-8")
            self.assertNotRegex(source, r"import\s+android\.")
        self.assertIn("static AppPreferences defaults()",
                      (CORE / "settings/AppPreferences.java").read_text(encoding="utf-8"))

    def test_navigation_prioritizes_saved_bundle(self):
        main = java("MainActivity.java")
        self.assertIn("if (saved != null) return NavigationBundleAdapter.read(saved);", main)
        self.assertIn("initialNavigation(savedInstanceState)", main)
        self.assertIn("NavigationRoute.SETTINGS", main)
        renderer = java("presentation/NavigationRenderer.java")
        self.assertIn("R.layout.screen_settings", renderer)
        self.assertIn("NativePreferencesApplier.apply", renderer)

    def test_all_list_adapters_opt_in(self):
        for name in ("CatalogPresenter", "SchedulePresenter", "ProgressPresenter"):
            self.assertIn("new PreferencesRowAdapter(activity)",
                          java("presentation/" + name + ".java"))

    def test_all_android_screens_use_xml_and_no_webview(self):
        layouts = list((APP / "res/layout").glob("*.xml"))
        self.assertGreaterEqual(len(layouts), 10)
        for path in layouts:
            ET.parse(path)
        self.assertFalse(list(APP.rglob("*.html")))
        self.assertFalse(list(APP.rglob("*.js")))
        self.assertNotIn("WebView", java("presentation/NavigationRenderer.java"))

    def test_workflow_push_main_and_manual_only(self):
        source = (ROOT / ".github/workflows/android-ci.yml").read_text(encoding="utf-8")
        self.assertIn("  workflow_dispatch:", source)
        self.assertIn("  push:\n    branches:\n      - main\n", source)
        self.assertNotRegex(source, r"(?m)^\s{2}(pull_request|schedule):")

    def test_java_junit_source_exists(self):
        target = ROOT / "core/src/test/java/pe/asterionsystem/core/settings/PreferencesServiceTest.java"
        self.assertTrue(target.is_file())
        self.assertIn("@Test", target.read_text(encoding="utf-8"))


if __name__ == "__main__":
    unittest.main(verbosity=2)
