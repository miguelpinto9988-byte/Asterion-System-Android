#!/usr/bin/env python3
"""Source-only offline checks; no Gradle, Android SDK, APK or network required."""
from pathlib import Path
import re
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "app/src/main"
JAVA = APP / "java/pe/asterionsystem/app"
CORE = ROOT / "core/src/main/java/pe/asterionsystem/core"
ANDROID = "{http://schemas.android.com/apk/res/android}"


def read(path):
    return path.read_text(encoding="utf-8")


class Phase14OfflineContractTest(unittest.TestCase):
    def test_manifest_has_no_network_permission_and_disables_backup(self):
        manifest = read(APP / "AndroidManifest.xml")
        self.assertNotIn("android.permission.INTERNET", manifest)
        self.assertNotIn("android.permission.ACCESS_NETWORK_STATE", manifest)
        self.assertIn('android:allowBackup="false"', manifest)
        self.assertIn('android:usesCleartextTraffic="false"', manifest)

    def test_schema_remains_version_five_without_destructive_rollback(self):
        database = read(JAVA / "data/AsterionDatabaseHelper.java")
        self.assertIn("DATABASE_VERSION = 5", database)
        self.assertNotIn("DROP TABLE", database.upper())
        self.assertIn("setForeignKeyConstraintsEnabled(true)", database)
        self.assertIn("setWriteAheadLoggingEnabled(true)", database)
        self.assertIn("new NonDestructiveDatabaseErrorHandler()", database)
        error_handler = read(JAVA / "data/NonDestructiveDatabaseErrorHandler.java")
        self.assertIn("implements DatabaseErrorHandler", error_handler)
        self.assertNotIn("deleteDatabase(", error_handler)
        self.assertNotIn("deleteDatabase(", database)

    def test_diagnostics_are_read_only(self):
        source = read(JAVA / "data/SQLiteLocalStorageCheck.java")
        self.assertIn("PRAGMA quick_check(1)", source)
        self.assertIn("PRAGMA foreign_key_check", source)
        self.assertIn("getReadableDatabase()", source)
        self.assertNotIn("execSQL(", source)
        self.assertNotRegex(source, r"\\b(delete|update|insertOrThrow|replaceOrThrow)\\s*\\(")
        self.assertIn("offMain();", source)

    def test_retry_actions_are_read_only_and_user_initiated(self):
        for layout_name in ("catalog", "catalog_detail", "plan", "plan_form",
                            "progress", "progress_summary"):
            root = ET.parse(APP / f"res/layout/screen_{layout_name}.xml").getroot()
            ids = {v.attrib[ANDROID + "id"].split("/")[-1]
                   for v in root.iter() if ANDROID + "id" in v.attrib}
            candidates = {
                "catalog": "catalog_retry",
                "catalog_detail": "catalog_detail_retry",
                "plan": "plan_retry",
                "plan_form": "plan_form_retry",
                "progress": "progress_retry",
                "progress_summary": "progress_summary_retry",
            }
            self.assertIn(candidates[layout_name], ids)

    def test_every_xml_id_and_string_reference_is_valid(self):
        layouts = sorted((APP / "res/layout").glob("*.xml"))
        ids = set()
        refs = set()
        for layout in layouts:
            root = ET.parse(layout).getroot()
            local = []
            for element in root.iter():
                key = element.attrib.get(ANDROID + "id")
                if key:
                    local.append(key.split("/")[-1])
                for value in element.attrib.values():
                    if value.startswith("@string/"):
                        refs.add(value.split("/", 1)[1])
            self.assertEqual(len(local), len(set(local)), layout.name)
            ids.update(local)
        strings = ET.parse(APP / "res/values/strings.xml").getroot()
        known_strings = {e.attrib["name"] for e in strings.findall("string")}
        self.assertFalse(refs - known_strings)
        for source in JAVA.rglob("*.java"):
            content = read(source)
            for match in re.finditer(r"R\\.id\\.([A-Za-z0-9_]+)", content):
                self.assertIn(match.group(1), ids, source.name)
        self.assertIn("local_status_check", ids)
        self.assertIn("open_local_status", ids)

    def test_core_is_platform_independent(self):
        for source in (CORE / "offline").glob("*.java"):
            self.assertNotIn("import android.", read(source))

    def test_no_remote_sync_or_webview_added(self):
        self.assertFalse(list(APP.rglob("*.html")))
        self.assertFalse(list(APP.rglob("*.js")))
        manifest = read(APP / "AndroidManifest.xml")
        self.assertNotIn("<uses-permission", manifest)
        renderer = read(JAVA / "presentation/NavigationRenderer.java")
        self.assertNotIn("WebView", renderer)

    def test_preferences_remain_separate(self):
        source = read(JAVA / "platform/AndroidPreferencesRepository.java")
        self.assertIn("Context.MODE_PRIVATE", source)
        self.assertIn("values.edit().clear().apply()", source)

    def test_workflow_is_manual_only(self):
        workflow = read(ROOT / ".github/workflows/android-ci.yml")
        self.assertRegex(workflow, r"(?m)^  workflow_dispatch:")
        self.assertNotRegex(workflow, r"(?m)^  (push|pull_request|schedule):")


if __name__ == "__main__":
    unittest.main(verbosity=2)
