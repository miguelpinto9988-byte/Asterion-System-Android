#!/usr/bin/env python3
"""Checkpoint selection must restart numeric volume without erasing activity records."""
from pathlib import Path

root = Path(__file__).resolve().parents[1]
engine = root/"progress-engine/src/main/java/pe/asterionsystem/core/engine/progression"
app = root/"app/src/main/java/pe/asterionsystem/app"
policy = (engine/"VolumeResetPolicy.java").read_text(encoding="utf-8")
volume = (app/"VolumeProgressStore.java").read_text(encoding="utf-8")
missions = (app/"MissionsActivity.java").read_text(encoding="utf-8")
routine = (app/"RoutineStepActivity.java").read_text(encoding="utf-8")
junit = (root/"progress-engine/src/test/java/pe/asterionsystem/core/engine/progression/VolumeResetPolicyTest.java").read_text(encoding="utf-8")
instrument = (root/"app/src/androidTest/java/pe/asterionsystem/app/VolumeProgressResetPersistenceTest.java").read_text(encoding="utf-8")
smoke = (root/"ci/android_daily_instrumented_smoke.sh").read_text(encoding="utf-8")

for word in ("currentCheckpoint > previousCheckpoint", "previousSelection",
             "restartVariantIds", "selected.id()", "variant.difficulty()",
             "checkpoint == 7", "checkpoint == 14", "checkpoint == 21"):
    assert word in policy, word
for word in ("observeCheckpointSelection(", "LAST_SELECTION", "LAST_CHECKPOINT",
             "VOLUME_RESTART", "EXPERIENCE_ENTRY", "progressionHistoryForCategory(",
             "previousForCategory(", "item.day.isBefore(restart)"):
    assert word in volume, word
assert "sp.edit().clear()" not in volume
assert "VolumeProgressStore.observeCheckpointSelection(" in missions
assert "VolumeProgressStore.progressionHistoryForCategory(" in missions
assert "VolumeProgressStore.progressionHistoryForCategory(" in routine
assert "VolumeProgressionEngine.review(" in missions
for test in ("daySevenChangedExerciseResetsOnlyNewVariant",
             "dayFourteenChangedTechniqueStartsFreshVolumeEpoch",
             "dayTwentyOneAlsoResetsAPreviouslyUsedVariantWhenReintroduced",
             "unchangedTechniqueDoesNotLoseItsVolumeHistory",
             "screenReopenCannotRestartVolumeTwice"):
    assert test in junit, test
for test in ("checkpointReintroducingOldVariantResetsItsNumericHistory",
             "unchangedExerciseAtMilestonesKeepsOwnBaseline"):
    assert test in instrument, test
assert "VolumeProgressResetPersistenceTest" in smoke
print("OK: reinicio 7/14/21 por variante, historial intacto y prueba Android conectada.")
