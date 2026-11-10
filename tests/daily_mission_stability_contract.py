#!/usr/bin/env python3
"""Same-day catalog missions use only compact IDs, date, profile signature."""
from pathlib import Path

root=Path(__file__).resolve().parents[1]
app=root/"app/src/main/java/pe/asterionsystem/app"
engine=root/"progress-engine/src/main/java/pe/asterionsystem/core/engine/missions"
planner=(engine/"MissionPlanner.java").read_text()
activity=(app/"MissionsActivity.java").read_text()
pin=(app/"DailyMissionStore.java").read_text()
tracking=(app/"MissionTrackingStore.java").read_text()
profile=(app/"ProfileRegistrationSteps.java").read_text()
adapter=(app/"MissionProfileAdapter.java").read_text()
test=(root/"progress-engine/src/test/java/pe/asterionsystem/core/engine/missions/DailyMissionStabilityTest.java").read_text()

for marker in ("missionSignature()", "daySeed(profile,today)",
               "Collections.shuffle(shuffled,new Random(seed))",
               "public Proposal restoreDaily(", "profile.intensityPreference",
               "catalog.eligibleForAutomaticSuggestion(variant)"):
    assert marker in planner,marker
for marker in ("DailyMissionStore.read(this,today,profile,catalog)",
               "DailyMissionStore.write(this,today,profile,catalog,",
               "planner.restoreDaily(profile,today,pinned)",
               "MissionTrackingStore.latestCompletedForCategory(",
               "MissionTrackingStore.clearLegacyProposalSnapshots(this)",
               "profile=MissionProfileAdapter.fromRegistration("):
    assert marker in activity,marker
assert "MissionTrackingStore.rememberCategoryProposal(" not in activity
for marker in ('private static final String DATE="local_date"',
               'private static final String SIGNATURE="profile_signature"',
               'private static final String CATALOG="catalog_signature"',
               'private static final String IDS="variant_ids"',
               "profile.missionSignature()", "today.toString()",
               "catalogSignature(catalog)", "choices.size()>profile.dailyLimit"):
    assert marker in pin,marker
for forbidden in ("repetitions","workout_json","exercise_description","set_count",
                  "full_session", "series_target", "daily_activities_json"):
    assert forbidden not in pin,forbidden
assert "latestCompletedForCategory(" in tracking
assert "boolean completed=false;" in tracking
assert "exerciseCategory.getOrDefault(id,-1)==category" in tracking
assert '"intensity",new String[]{"baja","moderada"}' in profile
assert 'profile.optString("intensity","moderada")' in adapter
for testName in (
    "sameDaySameProfileNeverReshufflesOnAnotherScreen",
    "futureDaysProduceVarietyAcrossWholeEligibleCatalog",
    "changedIntensityInvalidatesSignatureAndLimitsToLow",
    "equipmentCalendarGoalAndRoutineModeChangeFingerprint",
    "unorderedEquipmentInputDoesNotReshuffle",
    "storedIdentifiersAreRehydratedOnlyWhenSafe",
    "restDayHasNoExercisesEvenAfterProfileUpdate",
    "vigorousCatalogItemsAreNotAssignedAutomatically",
):
    assert testName in test,testName
print("OK: misiones diarias estables por fecha/perfil con solo identificadores temporales.")
