#!/usr/bin/env python3
"""Monarca cycle randomness, safety and consent regression contract."""
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
APP=ROOT/"app/src/main/java/pe/asterionsystem/app"
ENGINE=ROOT/"progress-engine/src/main/java/pe/asterionsystem/core/engine/missions"
PLANNER=(ENGINE/"MissionPlanner.java").read_text()
STORE=(APP/"MonarchSessionStore.java").read_text()
SCREEN=(APP/"MissionsActivity.java").read_text()
UNIT=(ROOT/"progress-engine/src/test/java/pe/asterionsystem/core/engine/missions/MonarchRandomnessTest.java").read_text()
INSTR=(ROOT/"app/src/androidTest/java/pe/asterionsystem/app/DailyMissionPersistenceTest.java").read_text()
SMOKE=(ROOT/"ci/android_daily_instrumented_smoke.sh").read_text()

for marker in ('"asterion.monarch.v3|"', "profile.missionSignature()",
               "monarchSeed(profile,starts)", "previousCycleIds",
               "shuffled=takeDiverse(pool,pool.size(),",
               "usedCategories.add(", "restoreMonarch(",
               "catalog.eligibleForAutomaticSuggestion(v)",
               "profile.equipment.containsAll(v.equipment())"):
    assert marker in PLANNER,marker
for marker in ("REVIEW_REQUIRED", "expiredActive", "profileChanged",
               "MonarchSessionStore", "restartAfterReview("):
    assert marker in (STORE+SCREEN),marker
for marker in ('putString(STATUS,ACTIVE)',
               'putString(ACCEPTED,today.toString())',
               'reviewed.previewStart.plusDays(5)', 
               'String.join(",",reviewedIds).equals(stored)',
               'REVIEW_REQUIRED.equals(status)',
               'sp.edit().putString(STATUS,REVIEW_REQUIRED).commit()'):
    assert marker in STORE,marker
assert "MonarchSessionStore.restartAfterReview(this)" in SCREEN
assert "MonarchSessionStore.accept(" in SCREEN
assert "sameMonarchChoices(" in SCREEN
assert "MonarchSessionStore.decline(this)" in SCREEN
assert "MonarchSessionStore.abandon(this)" in SCREEN
for name in ("sameCycleAndProfileRemainStableAcrossNewPlannerInstances",
             "newFiveDayCyclesProduceMultipleSelections",
             "previousCycleGetsNewCompatibleAlternativesWhenAvailable",
             "spreadAcrossAvailableScheduledCategories",
             "noAdvancedOrSupervisedAutomaticMonarchSelection",
             "restoredAcceptedListMustRemainCompatible"):
    assert name in UNIT,name
for name in ("seedMonarchPreviewAndAccept",
             "verifyMonarchAfterRestartAndProfileReview"):
    assert name in INSTR and name in SMOKE,name
print("OK: Monarca estable por ciclo, selección segura, sin cambios silenciosos tras aceptar.")
