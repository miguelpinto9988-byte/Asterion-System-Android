#!/usr/bin/env python3
"""Contracts for gradual safe mission tiers, profile equipment and local phases."""
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
PLANNER=(ROOT/"progress-engine/src/main/java/pe/asterionsystem/core/engine/missions/MissionPlanner.java").read_text()
POLICY=(ROOT/"progress-engine/src/main/java/pe/asterionsystem/core/engine/missions/MissionDifficultyMixPolicy.java").read_text()
STATE=(ROOT/"app/src/main/java/pe/asterionsystem/app/MissionLevelProgressStore.java").read_text()
ACTIVITY=(ROOT/"app/src/main/java/pe/asterionsystem/app/MissionsActivity.java").read_text()
PROFILE=(ROOT/"app/src/main/java/pe/asterionsystem/app/ProfileRegistrationSteps.java").read_text()
ADAPTER=(ROOT/"app/src/main/java/pe/asterionsystem/app/MissionProfileAdapter.java").read_text()
TEST=(ROOT/"progress-engine/src/test/java/pe/asterionsystem/core/engine/missions/DailyMissionStabilityTest.java").read_text()
INSTRUMENT=(ROOT/"app/src/androidTest/java/pe/asterionsystem/app/DailyMissionPersistenceTest.java").read_text()

for marker in ('MAX_AUTOMATIC_LEVEL=3','new int[]{25,40,60}',
               'new int[]{33,55,75}','"intermedio"','"avanzado"',
               '"principiante"'):
    assert marker in POLICY,marker
for marker in ("takeLevelMix(", "MissionDifficultyMixPolicy.higherTarget(",
               "catalog.eligibleForAutomaticSuggestion(variant)",
               "variant.difficulty() > maxTechnicalLevel",
               "selected.size()+upperTarget", "decision.preferSimplerTechnique"):
    assert marker in PLANNER,marker
for marker in ("TechnicalCheckpointPolicy.evaluate(", "last.plusDays(7)",
               "review.mayAdvance()", "if(category==5",
               "Context.MODE_PRIVATE", "selected_experience"):
    assert marker in STATE,marker
CHECKPOINT=(ROOT/"progress-engine/src/main/java/pe/asterionsystem/core/engine/progression/TechnicalCheckpointPolicy.java").read_text()
for marker in ("weekly.completedRatings < 2", "weekly.lowRatings < 2",
               "weekly.category != category", "weekly.highRatings > 0",
               "daysSinceExperienceStart >= 7",
               "daysSinceExperienceStart >= 14",
               "daysSinceExperienceStart >= 21"):
    assert marker in CHECKPOINT,marker
for marker in ("MissionLevelProgressStore.observe(", "levelState.phase",
               "recentlyInterrupted", "DailyMissionStore.read(this,today,profile,catalog)"):
    assert marker in ACTIVITY,marker
for key,code in (("mat","colchoneta"),("wall","pared"),("chair","silla")):
    assert f'check(out,"{key}"' in PROFILE
    assert f'equipment.add("{code}")' in ADAPTER
for name in ("beginnerMissionsContainOnlyTechnicalLevelsOneAndTwo",
             "intermediateEntryIsMostlyBasicThenBecomesMostlyIntermediate",
             "advancedEntryMixesMediumAndHarderSafeOptions",
             "veryHighEffortNeverEscalatesTechnicalLevel",
             "previouslyPinnedMissionDoesNotChangeWithNewMixPhase"):
    assert name in TEST,name
assert "MissionLevelProgressStore.observe(" in INSTRUMENT
assert "DailyMissionStore.read(context,DAY,profile,library)" in INSTRUMENT
print("OK: niveles graduales, filtro seguro, evidencia semanal aislada y persistencia por categoria.")
