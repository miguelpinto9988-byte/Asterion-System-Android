#!/usr/bin/env python3
"""Phase 03 native dark-fantasy resource checks, no Gradle, Android SDK or APK."""
from pathlib import Path
import re
import unittest
import xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/"app/src/main/res"
APP=ROOT/"app/src/main"
NS="{http://schemas.android.com/apk/res/android}"


def xml(path):
    return ET.parse(path).getroot()


class NativeVisualTest(unittest.TestCase):
    def test_xml_valid_and_drawables_resolve(self):
        for folder in ("layout","drawable","values"):
            for path in (RES/folder).glob("*.xml"):
                with self.subTest(path=path.name):
                    root=xml(path)
                    for node in root.iter():
                        for value in node.attrib.values():
                            if value.startswith("@drawable/"):
                                name=value.split("/",1)[1]
                                candidates = [
                                    RES/"drawable"/(name+".xml"),
                                    *[RES/folder/(name+ext)
                                      for folder in ("drawable", "drawable-nodpi")
                                      for ext in (".png", ".webp", ".jpg", ".jpeg")],
                                ]
                                self.assertTrue(any(item.is_file() for item in candidates),
                                                (path, name))

    def test_original_characters_and_icons(self):
        names=("asterion_guardian","asterion_shadow_sentinel","asterion_crest",
               "asterion_ic_catalog","asterion_ic_plan","asterion_ic_progress",
               "asterion_ic_settings","asterion_ic_status")
        for name in names:
            with self.subTest(name=name):
                node=xml(RES/"drawable"/(name+".xml"))
                self.assertEqual("vector",node.tag)
                self.assertGreaterEqual(len(node.findall("path")),1)

    def test_preserved_routes_and_character_accessibility(self):
        home=xml(RES/"layout/screen_home.xml")
        ids={n.get(NS+"id","") for n in home.iter()}
        for rid in ("open_catalog","open_plan","open_progress","open_settings",
                    "open_local_status","open_secondary"):
            self.assertIn("@+id/"+rid,ids)
        views=[n for n in home.iter() if n.tag=="ImageView"]
        self.assertTrue(any(v.get(NS+"src")=="@drawable/asterion_guardian"
                         and v.get(NS+"contentDescription")=="@string/design_guardian_description"
                         for v in views))
        self.assertTrue(any(v.get(NS+"src")=="@drawable/asterion_shadow_sentinel"
                         and v.get(NS+"importantForAccessibility")=="no"
                         for v in views))

    def test_selected_day_styles_and_weekly_metrics(self):
        plan=(RES/"layout/screen_plan.xml").read_text()
        for i in range(7):
            self.assertRegex(plan, rf'plan_day_{i}"[^>]*@drawable/asterion_day_selector')
        summary=xml(RES/"layout/screen_progress_summary.xml")
        for name in ("progress_summary_total","progress_summary_done",
                     "progress_summary_skipped","progress_summary_pending"):
            targets=[n for n in summary.iter() if n.get(NS+"id")=="@+id/"+name]
            self.assertEqual(1,len(targets))
            self.assertEqual("@drawable/asterion_list_card",targets[0].get(NS+"background"))

    def test_preferences_and_rows_keep_touch_target(self):
        rows=(APP/"java/pe/asterionsystem/app/presentation/PreferencesRowAdapter.java").read_text()
        self.assertIn("dp(compact ? 48 : 56)",rows)
        self.assertIn("asterion_list_row_selector",rows)
        settings=xml(RES/"layout/screen_settings.xml")
        groups=[v for v in settings.iter() if v.tag=="RadioGroup"]
        self.assertEqual(3,len(groups))
        self.assertTrue(all(v.get(NS+"background")=="@drawable/asterion_list_card" for v in groups))

    def test_themes_and_tokens(self):
        styles=(RES/"values/styles.xml").read_text()
        colors={c.get("name"):c.text for c in xml(RES/"values/colors.xml") if c.tag=="color"}
        self.assertIn("Theme.Material.NoActionBar",styles)
        self.assertEqual("#090E1D",colors["asterion_background"])
        self.assertEqual("#F4F7FF",colors["asterion_text"])
        self.assertEqual("#76C7FF",colors["asterion_accent"])

    def test_offline_constraints_preserved(self):
        self.assertEqual(
            {APP / "assets/asterion/splash_overlay.html"},
            set(APP.rglob("*.html")),
            "La unica pagina HTML admitida es la capa local de la pantalla de carga"
        )
        self.assertFalse(list(APP.rglob("*.js")))
        helper=(APP/"java/pe/asterionsystem/app/data/AsterionDatabaseHelper.java").read_text()
        self.assertIn("DATABASE_VERSION = 5",helper)
        workflow=(ROOT/".github/workflows/android-ci.yml").read_text()
        self.assertRegex(workflow,r"(?m)^  workflow_dispatch:")
        self.assertIn("  push:\n    branches:\n      - main\n", workflow)
        self.assertNotRegex(workflow,r"(?m)^  (pull_request|schedule):")


if __name__=="__main__":
    unittest.main(verbosity=2)
