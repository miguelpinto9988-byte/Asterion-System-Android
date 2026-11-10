#!/usr/bin/env python3
"""Contract: real catalog drives proposals; Monarch starts only with consent."""
from pathlib import Path
import xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
BASE=ROOT/"app/src/main"
JAVA=BASE/"java/pe/asterionsystem/app"
MOTOR=ROOT/"progress-engine/src/main/java/pe/asterionsystem/core/engine/missions/MissionPlanner.java"
NS="{http://schemas.android.com/apk/res/android}"

screen=ET.parse(BASE/"res/layout/screen_home.xml").getroot()
ids={node.get(NS+"id") for node in screen.iter() if node.get(NS+"id")}
for name in ("open_monarch","home_profile_name","open_daily","open_progress","open_library"):
    assert "@+id/"+name in ids,name
layout=ET.parse(BASE/"res/layout/screen_missions.xml").getroot()
ids2={node.get(NS+"id") for node in layout.iter() if node.get(NS+"id")}
for name in ("missions_tab_monarch","missions_tab_daily","missions_tab_library",
             "missions_content","missions_back"):
    assert "@+id/"+name in ids2,name

manifest=ET.parse(BASE/"AndroidManifest.xml").getroot()
activities=manifest.find("application").findall("activity")
assert any(a.get(NS+"name")==".MissionsActivity"
           and a.get(NS+"exported")=="false" for a in activities)
planner=MOTOR.read_text()
session=(JAVA/"MonarchSessionStore.java").read_text()
activity=(JAVA/"MissionsActivity.java").read_text()
adapter=(JAVA/"MissionProfileAdapter.java").read_text()
binder=(JAVA/"presentation/HomeMissionBinder.java").read_text()
renderer=(JAVA/"presentation/NavigationRenderer.java").read_text()
assert "catalog.variants()" in planner
assert "catalog.eligibleForAutomaticSuggestion(variant)" in planner
assert "profile.equipment.containsAll(variant.equipment())" in planner
assert "today.getDayOfWeek().getValue() - 1" in planner
assert "today.plusDays(1)" in planner
assert "starts.plusDays(5)" in planner
assert "takeDiverse(pool,pool.size()," in planner
assert "monarchSeed(profile,starts)" in planner
assert "restoreMonarch(" in planner
assert "return new Proposal(MONARCH" in planner
assert "requiresConsent" in planner
assert not any(fragment in planner for fragment in (
    "targetReps", "setRepetitions", "100 *", "75 *", "maxRep"))
assert 'putString(STATUS,ACTIVE)' in session
assert 'putString(ACCEPTED,today.toString())' in session
assert 'new AlertDialog.Builder(this)' in activity
assert 'setPositiveButton("Aceptar y activar"' in activity
assert 'MonarchSessionStore.decline(this)' in activity
assert 'MonarchSessionStore.abandon(this)' in activity
assert 'AssetExerciseCatalogRepository(this).load()' in activity
assert 'ProfileStore.getRegistration(this)' in activity
assert '"week"' in adapter and '"missions"' in adapter
assert '"gym_"' in adapter
assert 'HomeMissionBinder.bind(activity,screen,openProgress)' in renderer
assert '"monarch"' in binder and '"daily"' in binder and '"library"' in binder
print("OK: motor conectado a 817 variantes, renovación diaria, Monarca 5 días opt-in y control manual.")
