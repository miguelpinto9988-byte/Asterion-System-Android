#!/usr/bin/env python3
"""Pure source tests: XML, security, pagination and retry invariants; NO Gradle/APK."""
from pathlib import Path
import re
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "app/src/main"
JAVA = APP / "java/pe/asterionsystem/app"
CORE = ROOT / "core/src/main/java/pe/asterionsystem/core"
ANDROID = "{http://schemas.android.com/apk/res/android}"


def read(rel):
    return (ROOT / rel).read_text(encoding="utf-8")


class Phase15QualityTest(unittest.TestCase):
    def test_layouts_parse_and_resource_references_exist(self):
        defs = ET.parse(APP / "res/values/strings.xml").getroot()
        strings = {e.attrib["name"] for e in defs.findall("string")}
        ids = set()
        for path in sorted((APP / "res/layout").glob("*.xml")):
            root = ET.parse(path).getroot()
            local = []
            for node in root.iter():
                value = node.attrib.get(ANDROID + "id", "")
                if value.startswith("@+id/"):
                    local.append(value.split("/", 1)[1])
                for attr in node.attrib.values():
                    if attr.startswith("@string/"):
                        self.assertIn(attr.split("/", 1)[1], strings, path.name)
            self.assertEqual(len(local), len(set(local)), path.name)
            ids.update(local)
        for path in JAVA.rglob("*.java"):
            for match in re.finditer(r"R\.id\.([A-Za-z0-9_]+)", path.read_text(encoding="utf-8")):
                self.assertIn(match.group(1), ids, path.name)

    def test_accessibility_labels_and_live_regions(self):
        names = ("catalog", "catalog_detail", "plan", "plan_form", "progress",
                 "progress_summary", "local_status")
        for name in names:
            xml = read("app/src/main/res/layout/screen_" + name + ".xml")
            self.assertIn('android:accessibilityLiveRegion="polite"', xml, name)
        form = read("app/src/main/res/layout/screen_plan_form.xml")
        for field in ("plan_date", "plan_time", "plan_duration"):
            self.assertIn('android:labelFor="@id/' + field + '"', form)
            self.assertIn('android:id="@+id/' + field + '"', form)
        rows = read("app/src/main/java/pe/asterionsystem/app/presentation/PreferencesRowAdapter.java")
        self.assertIn("row.setMinHeight(dp(compact ? 48 : 56));", rows)

    def test_bulk_titles_use_only_bound_sql_and_chunking(self):
        repo = read("app/src/main/java/pe/asterionsystem/app/data/SQLiteCatalogRepository.java")
        contract = read("core/src/main/java/pe/asterionsystem/core/catalog/CatalogRepository.java")
        planner = read("core/src/main/java/pe/asterionsystem/core/planning/SchedulePlanner.java")
        self.assertIn("final int batchSize = 200;", repo)
        self.assertIn("sql.append('?')", repo)
        self.assertIn("args[j - from] = distinct.get(j).value()", repo)
        self.assertIn("default Map<EntityId, String> titlesById", contract)
        self.assertIn("catalog.titlesById(ids)", planner)
        self.assertNotIn("rows.add(new ScheduleListing(entry, titleFor(", planner)

    def test_debounced_search_cancels_old_tasks_and_watchers(self):
        presenter = read("app/src/main/java/pe/asterionsystem/app/presentation/CatalogPresenter.java")
        self.assertIn("SEARCH_DEBOUNCE_MS = 280L", presenter)
        self.assertIn("main.postDelayed(task, SEARCH_DEBOUNCE_MS)", presenter)
        self.assertIn("main.removeCallbacks(pendingSearch)", presenter)
        self.assertIn("removeTextChangedListener(searchWatcher)", presenter)
        self.assertIn("++generation;", presenter)

    def test_storage_check_requires_foreign_key_enforcement(self):
        diagnostics = read("app/src/main/java/pe/asterionsystem/app/data/SQLiteLocalStorageCheck.java")
        self.assertIn('PRAGMA foreign_keys', diagnostics)
        self.assertIn('PRAGMA foreign_key_check', diagnostics)
        self.assertIn("new long[]{cursor.getLong(0), cursor.getLong(1), cursor.getLong(2)}", diagnostics)
        self.assertIn("SELECT (SELECT COUNT(*) FROM catalog_items)", diagnostics)
        self.assertNotIn("DELETE FROM", diagnostics)

    def test_private_local_security_and_manual_ci(self):
        manifest = read("app/src/main/AndroidManifest.xml")
        self.assertIn('android:allowBackup="false"', manifest)
        self.assertIn('android:usesCleartextTraffic="false"', manifest)
        self.assertNotIn("android.permission.INTERNET", manifest)
        self.assertNotIn("android.permission.ACCESS_NETWORK_STATE", manifest)
        self.assertIn("Context.MODE_PRIVATE", read("app/src/main/java/pe/asterionsystem/app/platform/AndroidPreferencesRepository.java"))
        self.assertIn("NonDestructiveDatabaseErrorHandler", read("app/src/main/java/pe/asterionsystem/app/data/AsterionDatabaseHelper.java"))
        db = read("app/src/main/java/pe/asterionsystem/app/data/AsterionDatabaseHelper.java")
        self.assertIn("DATABASE_VERSION = 5", db)
        self.assertNotIn("DROP TABLE", db.upper())
        workflow = read(".github/workflows/android-ci.yml")
        self.assertIn("  workflow_dispatch:", workflow)
        self.assertNotRegex(workflow, r"(?m)^  (push|pull_request|schedule):")

    def test_html_does_not_enter_android_sources(self):
        self.assertFalse(list(APP.rglob("*.html")))
        self.assertFalse(list(APP.rglob("*.js")))
        self.assertFalse(list(APP.rglob("*.css")))


if __name__ == "__main__":
    unittest.main(verbosity=2)
