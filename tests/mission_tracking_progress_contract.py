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
                "home_avatar","home_bottom_bar"):
    assert "@+id/"+control in ids,control
for text in ("MissionTrackingStore.setDailyCompleted(", "MissionTrackingStore.rateCategory(",
             "MissionTrackingStore.markMonarch(", "MissionTrackingStore.completedToday(",
             "MissionTrackingStore.monarchCompleted(", "EffortReviewEngine.review(",
             "new AlertDialog.Builder(this)", "setSingleChoiceItems",
             "show(SECTION_PROGRESS)", "requestEffort("):
    assert text in activity,text
for text in ('open(activity,"library")', 'open(activity,"progress")',
             'open(activity,"daily")'):
    assert text in binder,text
assert "HomeMissionBinder.bind(activity,screen)" in renderer
# Historical catalog/progress remain accessible inside the avatar tools.
profile_tools=source("ProfileDetailsActivity.java")
assert '"catalog"' in profile_tools and '"progress"' in profile_tools
for text in ('"day_effort_"', '"monarch_done_"', '"day_done_"',
             'effort<1||effort>10', "SharedPreferences"):
    assert text in tracking,text
for text in ("day >=21 ?21", "day>=14 ?14", "day>=7 ?7",
             "if (average>=7.0)", "no se aumentará", "sin aumentar automáticamente"):
    assert text in domain,text
assert "highEffortCanSuggestReducingAtDayTwentyOne" in tests
assert "lowEffortDoesNotTriggerAutomaticIncrease" in tests
assert "CategoryEffortPolicy.evaluate(" in activity
assert "MissionTrackingStore.latestCategoryRating(this,category,today)" in activity
assert "MissionTrackingStore.rateCategory(this,day,category,selected[0])" in activity
assert "MissionTrackingStore.ratingForCategory(this,day,category)" in activity
assert "int category=profile.weekly.get(today.getDayOfWeek().getValue()-1)" in activity
assert "EffortAdaptationPolicy.dailySuggestionCount(" not in activity
policy=(root/"progress-engine/src/main/java/pe/asterionsystem/core/engine/progression/CategoryEffortPolicy.java").read_text(encoding="utf-8")
for condition in ("lastEffort<=3", "lastEffort<=7", "lastEffort>=9",
                  "REVIEW_OPTIONAL", "MAINTAIN", "EASIER",
                  "Los ajustes de repeticiones se calculan por separado"):
    assert condition in policy, condition
assert '"category_effort_"' in tracking
assert "latestCategoryRating(Context context,int category,LocalDate before)" in tracking
assert not any(s in activity for s in ("setRepetitions(", "repetitionTarget=150",
                                      "setTargetReps(", "mustIncreaseIntensity"))
print("OK: biblioteca real en Inicio, feedback 1-10, progreso 7/14/21 y Monarca voluntaria.")
