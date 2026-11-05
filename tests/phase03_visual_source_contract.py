#!/usr/bin/env python3
"""Static visual-contract checks; standard library only, never builds Android."""
from pathlib import Path
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app/src/main/res"
NS = "{http://schemas.android.com/apk/res/android}"

class Phase03VisualTest(unittest.TestCase):
    def test_native_xml_well_formed(self):
        for folder in ("layout", "drawable", "values"):
            for path in (RES / folder).glob("*.xml"):
                with self.subTest(path=path.name):
                    ET.parse(path)

    def test_dark_theme_and_color_tokens(self):
        colors = ET.parse(RES / "values/colors.xml").getroot()
        palette = {x.attrib["name"]: (x.text or "").strip() for x in colors.findall("color")}
        self.assertEqual("#090E1D", palette["asterion_background"])
        self.assertEqual("#F4F7FF", palette["asterion_text"])
        self.assertEqual("#76C7FF", palette["asterion_accent"])
        styles = (RES / "values/styles.xml").read_text()
        self.assertIn("Theme.Material.NoActionBar", styles)
        self.assertNotIn("Theme.Material.Light.NoActionBar", styles)
        self.assertIn("Asterion.ActionButton", styles)

    def test_all_navigation_buttons_preserved(self):
        home = ET.parse(RES / "layout/screen_home.xml").getroot()
        ids = {node.get(NS + "id") for node in home.iter()}
        for name in ("open_secondary", "open_plan", "open_progress",
                     "open_settings", "open_local_status", "open_catalog"):
            self.assertIn("@+id/" + name, ids)

    def test_no_html_in_android(self):
        app = ROOT / "app/src/main"
        self.assertFalse(list(app.rglob("*.html")))
        self.assertFalse(list(app.rglob("*.js")))
        self.assertFalse(list(app.rglob("*.css")))

    def test_ci_manual_and_sqlite_unchanged(self):
        ci = (ROOT / ".github/workflows/android-ci.yml").read_text()
        self.assertIn("  workflow_dispatch:", ci)
        for trigger in ("  push:", "  pull_request:", "  schedule:"):
            self.assertNotIn(trigger, ci)
        helper = (ROOT / "app/src/main/java/pe/asterionsystem/app/data/AsterionDatabaseHelper.java").read_text()
        self.assertIn("DATABASE_VERSION = 5", helper)

if __name__ == "__main__":
    unittest.main(verbosity=2)
