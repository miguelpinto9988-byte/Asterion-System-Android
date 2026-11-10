#!/usr/bin/env python3
"""Regression guard for the approved, offline Asterion baseline.

This is a source/data contract, not a substitute for Android UI instrumentation.
"""
import csv
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "app/src/main"
JAVA = APP / "java/pe/asterionsystem/app"
DATA = ROOT / "progress-engine/src/main/resources/asterion_exercises/v1"
ANDROID = "{http://schemas.android.com/apk/res/android}"


def rows(filename):
    with (DATA / filename).open(encoding="utf-8", newline="") as stream:
        return list(csv.DictReader(stream, delimiter="\t"))


def unique(rows_, field):
    values = [item[field] for item in rows_]
    assert len(values) == len(set(values)), f"Duplicate {field}"
    return set(values)


families = rows("families.tsv")
types = rows("types.tsv")
variants = rows("variants.tsv")
intensities = rows("intensity.tsv")
assert (len(families), len(types), len(variants), len(intensities)) == (
    75, 204, 817, 312
), "The approved local exercise taxonomy has changed"
family_ids = unique(families, "family_id")
type_ids = unique(types, "type_id")
variant_ids = unique(variants, "variant_id")
unique(intensities, "variant_id")
assert all(item["family_id"] in family_ids for item in types)
assert all(item["type_id"] in type_ids for item in variants)
assert all(item["variant_id"] in variant_ids for item in intensities)

manifest = ET.parse(APP / "AndroidManifest.xml").getroot()
assert not manifest.findall("uses-permission"), "Unexpected Android permission"
application = manifest.find("application")
assert application is not None
assert application.get(ANDROID + "allowBackup") == "false"
assert application.get(ANDROID + "usesCleartextTraffic") == "false"
assert application.get(ANDROID + "icon") == "@drawable/asterion_icon_oficial"
for asset in ("asterion_icon_oficial.webp", "asterion_loading_manana.png",
              "asterion_loading_tarde_noche.png"):
    assert (APP / "res/drawable-nodpi" / asset).is_file(), asset

wizard = (JAVA / "ProfileCreateActivity.java").read_text(encoding="utf-8")
for fragment in (
    'if (!draft.has("theme")) put("theme","light");',
    "dynamic.addView(forms.step2())",
    "new SimpleProfileStepThreeView(",
    "dynamic.addView(forms.step4())",
    "ProfileStore.saveRegistration(this,draft)",
    "if(stage==5 && ProfileStore.hasProfile(this))showActivated();",
    'saved.putInt("stage",step);',
):
    assert fragment in wizard, fragment
ET.parse(APP / "res/layout/screen_profile_create.xml")
assert 'THEMES = {"light","dark"}' in (
    JAVA / "SimpleProfileStepThreeView.java"
).read_text(encoding="utf-8")

daily = (JAVA / "DailyMissionStore.java").read_text(encoding="utf-8")
for fragment in ('"local_date"', '"profile_signature"',
                 '"catalog_signature"', '"variant_ids"',
                 "profile.missionSignature()"):
    assert fragment in daily, fragment
monarch = (JAVA / "MonarchSessionStore.java").read_text(encoding="utf-8")
assert "reviewed.canAccept()" in monarch
assert "putString(STATUS,ACTIVE)" in monarch
assert "putString(ACCEPTED,today.toString())" in monarch

print("OK: baseline nativa, 817 variantes, perfil 4 pasos, "
      "restauracion de activacion y misiones locales.")
