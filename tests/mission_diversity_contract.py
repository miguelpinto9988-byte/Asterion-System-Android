#!/usr/bin/env python3
"""Guard the 100-family focus mapping and recent completed-only variety logic."""
from pathlib import Path
import csv
import re

ROOT=Path(__file__).resolve().parents[1]
LIB=ROOT/"progress-engine/src/main"
FOCUS=(LIB/"java/pe/asterionsystem/core/engine/exercises/ExerciseMuscleFocus.java").read_text()
PLANNER=(LIB/"java/pe/asterionsystem/core/engine/missions/MissionPlanner.java").read_text()
TRACKING=(ROOT/"app/src/main/java/pe/asterionsystem/app/MissionTrackingStore.java").read_text()
ACTIVITY=(ROOT/"app/src/main/java/pe/asterionsystem/app/MissionsActivity.java").read_text()
TEST=(ROOT/"app/src/androidTest/java/pe/asterionsystem/app/DailyMissionPersistenceTest.java").read_text()
with (LIB/"resources/asterion_exercises/v1/families.tsv").open(encoding="utf-8") as f:
    families={row["family_id"] for row in csv.DictReader(f,delimiter="\t")}

focus_ids=re.findall(r'Map\.entry\("([a-z0-9_]+)",new Focus\("',FOCUS)
assert len(focus_ids)==100 and len(set(focus_ids))==100
assert set(focus_ids)==families, "All family IDs must be classified"
for marker in ("ExerciseMuscleFocus.forFamily(", "recentCompleted",
               "daily(Profile profile, LocalDate today,", "new Random(seed)",
               "if(selected.size()>=count)break"):
    assert marker in PLANNER,marker
for marker in ("recentCompletedForCategory(", "recentDiversityForCategory(",
               "day<=14", "days<14", "completedToday("):
    assert marker in TRACKING,marker
assert "rememberCategoryProposal(this" not in ACTIVITY
for marker in ("MissionTrackingStore.recentCompletedForCategory(",
               "MissionTrackingStore.recentDiversityForCategory(",
               "ExerciseMuscleFocus.forVariant("):
    assert marker in ACTIVITY,marker
for marker in ("seedPin()", "verifyAfterRestartAndDayChange()",
               "DailyMissionStore.write(", "DailyMissionStore.read("):
    assert marker in TEST,marker
print("OK: 100 focos, variedad por completados reales, aislamiento de categoria y prueba Android.")
