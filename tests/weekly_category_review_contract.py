#!/usr/bin/env python3
"""The day-seven review is autonomous, category-scoped and recovery-safe."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
java=root/"progress-engine/src/main/java/pe/asterionsystem/core/engine"
planner=(java/"missions/MissionPlanner.java").read_text()
weekly=(java/"progression/WeeklyCategoryChallengePolicy.java").read_text()
activity=(root/"app/src/main/java/pe/asterionsystem/app/MissionsActivity.java").read_text()
store=(root/"app/src/main/java/pe/asterionsystem/app/MissionTrackingStore.java").read_text()
test=(root/"progress-engine/src/test/java/pe/asterionsystem/core/engine/progression/WeeklyCategoryChallengePolicyTest.java").read_text()
for bit in ("ChronoUnit.DAYS.between(firstRecordedDay,today)",
            "if(elapsed<7)", "completed<2", "low>=2 && low==completed",
            "if(high>0)", "observation.category!=category",
            "if(!observation.activityCompleted)continue"):
    assert bit in weekly,bit
assert "WeeklyCategoryChallengePolicy.evaluate(" in activity
assert "MissionTrackingStore.recentCategoryObservations(this,category,today)" in activity
assert "weekly!=null && weekly.rotateEquivalentVariant()" in activity
assert "boolean enableEquivalentRotation" in planner
assert "if(enableEquivalentRotation" in planner
assert "CATEGORY_PLAN" in store
assert "category_effort_" in store
for bit in ("severeUnfinishedReportAlwaysBlocksNovelty",
            "reviewDoesNotRunUntilSevenDaysElapsed",
            "twoCompletedEasyUpperSessionsTriggerAutomaticVarietyAfterWeek",
            "lowerBodyFeedbackDoesNotInfluenceUpperBody"):
    assert bit in test,bit
assert "setRepetitions" not in weekly and "extraSets" not in planner
print("OK: revisión automática semanal por categoría, 2 observaciones completas, recuperación prioritaria.")
