#!/usr/bin/env python3
"""The approved visual system and Monarca boss selection stay integrated globally."""
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1]
APP=ROOT/"app/src/main"
JAVA=APP/"java/pe/asterionsystem/app"
RES=APP/"res"
styles=(RES/"values/styles.xml").read_text()
theme=(JAVA/"AppTheme.java").read_text()
base=(JAVA/"AsterionActivity.java").read_text()
missions=(JAVA/"MissionsActivity.java").read_text()
store=(JAVA/"MonarchSessionStore.java").read_text()
planner=(ROOT/"progress-engine/src/main/java/pe/asterionsystem/core/engine/missions/MissionPlanner.java").read_text()

for color in ("#F6F6F9","#FFFFFF","#181821","#6544AA",
              "#09090F","#17171F","#F5F4FA","#C4ABFF"):
    assert color in styles or color in theme,color
assert "Typeface.BOLD" in theme
assert "AppTheme.apply(content,visualLight)" in base
assert "AppTheme.systemBars" in base
for activity in ("CalendarActivity.java","MissionsActivity.java","RoutinesActivity.java",
                 "RoutineStepActivity.java","ProfileDetailsActivity.java",
                 "ImportedExerciseCatalogActivity.java","ExerciseMotionDetailActivity.java"):
    assert "extends AsterionActivity" in (JAVA/activity).read_text(),activity

home=ET.parse(RES/"layout/screen_home.xml").getroot()
ns="{http://schemas.android.com/apk/res/android}"
avatar=next(v for v in home.iter() if v.get(ns+"id")=="@+id/home_avatar")
assert avatar.get(ns+"layout_width")=="52dp"
assert avatar.get(ns+"tag")=="avatar"
assert "HomeOverview.bind" in (JAVA/"presentation/HomeMissionBinder.java").read_text()

for boss in ("igris","beru","baran","antares"):
    assert f'"{boss}"' in (JAVA/"MonarchBoss.java").read_text()
    assert (RES/"drawable-nodpi"/f"monarch_{boss}.webp").stat().st_size>10_000
    assert f'case "{boss}"' in planner or boss=="igris"
assert "MonarchBossPicker.create" in missions
assert "selectBoss" in store and ".putString(BOSS,boss)" in store
assert "bossPriority" in planner and "missionSignature()+\"|\"+boss" in planner
assert "DailyMissionStore.read" in (JAVA/"HomeOverview.java").read_text()
print("OK: rediseño global, perfil superior, datos reales y cuatro encuentros Monarca integrados")
