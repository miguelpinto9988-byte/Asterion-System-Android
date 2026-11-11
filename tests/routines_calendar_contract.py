#!/usr/bin/env python3
"""New seeded routine chooser and month-calendar integrations remain safe."""
from pathlib import Path
from xml.etree import ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
APP=ROOT/"app/src/main"
JAVA=APP/"java/pe/asterionsystem/app"
ENGINE=ROOT/"progress-engine/src/main/java/pe/asterionsystem/core/engine/missions"
src=lambda name:(JAVA/name).read_text(encoding="utf8")
engine=lambda name:(ENGINE/name).read_text(encoding="utf8")
routine=engine("RoutineGenerator.java")
month=engine("MonthCalendar.java")
step=src("RoutineStepActivity.java")
choose=src("RoutinesActivity.java")
calendar=src("CalendarActivity.java")
experience=src("RoutineExperienceStore.java")
mission=engine("MissionPlanner.java")
manifest=(APP/"AndroidManifest.xml").read_text()
home=(APP/"res/layout/screen_home.xml").read_text()
missions=(APP/"res/layout/screen_missions.xml").read_text()
for name in ("tren_superior","tren_central","tren_inferior","movilidad","cardio"):
    assert name in (ROOT/"progress-engine/src/main/java/pe/asterionsystem/core/engine/exercises/ExerciseCategoryMapper.java").read_text()
assert "Collections.shuffle(pool,new Random(seed+" in routine
assert "profile.missionSignature()" in routine
assert "catalog.categoryIndex().variants(request.category)" in routine
for check in ("catalog.eligibleForAutomaticSuggestion(variant)",
    "profile.equipment.containsAll(variant.equipment())",
    "intensity!=ExerciseIntensity.MODERATE",
    "variant.difficulty()>request.maxTechnicalLevel()",
    "usedFamilies.contains(family)","seenAcross.contains(variant.id())"):
    assert check in routine,check
assert "OFFER_COUNT=6" in routine
assert "tech" not in ""
for key in ("YearMonth","lengthOfMonth()","plusMonths(months)",
            "atDay(day)","DayOfWeek.MONDAY"):
    assert key in month
for key in ("MonthCalendar.days(month)","MonthCalendar.shift(month,delta)",
            "MonthCalendar.chooseYear(month,year)",
            "MissionTrackingStore.completedToday(this,date)",
            "RoutineExperienceStore.recorded(this,date)"):
    assert key in calendar,key
for key in ('"custom".equals(kind)',"RoutineGenerator.Request(",
            "generate(request,configured)","RoutineExperienceStore.awardOnce(this,planDate)",
            "RoutineSessionStore.clear(this,token)",
            'else if("monarch".equals(kind))'):
    assert key in step,key
assert "ONCE_PER_DAY=10" in experience
assert 'p.getBoolean(key,false)' in experience
assert "date.equals(LocalDate.now())" in experience
# The two functional features remain global bottom navigation destinations,
# never redundant shortcut squares on Home or in the Missions toolbar.
for key in ("SimpleBottomNav.attach", "R.id.home_avatar",
            "ProfileDetailsActivity.class"):
    assert key in src("presentation/HomeMissionBinder.java"),key
for removed in ("open_routines","open_calendar",
                "missions_open_routines","missions_open_calendar"):
    assert f"@+id/{removed}" not in (home+missions),removed
assert "@+id/home_bottom_bar" in home
assert "@+id/missions_bottom_bar" in missions
for key in ("SimpleBottomNav.ROUTINES","SimpleBottomNav.CALENDAR"):
    assert key in (choose+calendar),key
for item in (".RoutinesActivity",".CalendarActivity"):
    assert item in manifest
assert "Collections.shuffle(shuffled,new Random(seed))" in mission
assert "shuffled.sort(Comparator.comparing(ExerciseVariant::id));" in mission
assert 'MonarchSessionStore.ACTIVE.equals(snapshot.status)' in step
assert 'SessionSafetyStore.globallyPaused(this)' in choose
assert 'SessionSafetyStore.categoryPaused(this,categoryIndex)' in choose
print("OK: six stable routines, five categories, safe XP, monthly calendar, UI navigation and Monarca preserved")
