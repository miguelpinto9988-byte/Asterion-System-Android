#!/usr/bin/env python3
"""Regression for mission tracking, voluntary effort review and real catalog links."""
from pathlib import Path
import xml.etree.ElementTree as ET

root=Path(__file__).resolve().parents[1]
app=root/"app/src/main"
java=app/"java/pe/asterionsystem/app"
def source(name): return (java/name).read_text(encoding="utf-8")

tracking=source("MissionTrackingStore.java")
activity=source("MissionsActivity.java")
binder=source("presentation/HomeMissionBinder.java")
renderer=source("presentation/NavigationRenderer.java")
domain=(root/"progress-engine/src/main/java/pe/asterionsystem/core/engine/progression/EffortReviewEngine.java").read_text(encoding="utf-8")
tests=(root/"progress-engine/src/test/java/pe/asterionsystem/core/engine/progression/EffortReviewEngineTest.java").read_text(encoding="utf-8")
home=ET.parse(app/"res/layout/screen_home.xml").getroot()
ns="{http://schemas.android.com/apk/res/android}"
ids={v.get(ns+"id") for v in home.iter()}
for control in ("open_daily","open_monarch","open_library","open_progress",
                "open_catalog","open_legacy_progress"):
    assert "@+id/"+control in ids,control
for text in ("MissionTrackingStore.setDailyCompleted(", "MissionTrackingStore.rate(",
             "MissionTrackingStore.markMonarch(", "MissionTrackingStore.completedToday(",
             "MissionTrackingStore.monarchCompleted(", "EffortReviewEngine.review(",
             "new AlertDialog.Builder(this)", "setSingleChoiceItems",
             "show(SECTION_PROGRESS)", "requestEffort("):
    assert text in activity,text
for text in ('open(activity,"library")', 'open(activity,"progress")',
             'openHistoricalProgress.run()', 'open(activity,"daily")'):
    assert text in binder,text
assert "HomeMissionBinder.bind(activity,screen,openProgress)" in renderer
for text in ('"day_effort_"', '"monarch_done_"', '"day_done_"',
             'effort<1||effort>10', "SharedPreferences"):
    assert text in tracking,text
for text in ("day >=21 ?21", "day>=14 ?14", "day>=7 ?7",
             "if (average>=7.0)", "no se aumentará", "sin aumentar automáticamente"):
    assert text in domain,text
assert "highEffortCanSuggestReducingAtDayTwentyOne" in tests
assert "lowEffortDoesNotTriggerAutomaticIncrease" in tests
assert "EffortAdaptationPolicy.dailySuggestionCount(" in activity
assert "today.minusDays(1)" in activity
policy=(root/"progress-engine/src/main/java/pe/asterionsystem/core/engine/progression/EffortAdaptationPolicy.java").read_text(encoding="utf-8")
for condition in ("review.averageEffort >= 8.0", "review.averageEffort >= 7.0", "return original"):
    assert condition in policy, condition
assert not any(s in activity for s in ("setRepetitions(", "repetitionTarget=150",
                                      "setTargetReps(", "mustIncreaseIntensity"))
print("OK: biblioteca real en Inicio, feedback 1-10, progreso 7/14/21 y Monarca voluntaria.")
