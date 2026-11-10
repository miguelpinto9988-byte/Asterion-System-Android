#!/usr/bin/env python3
"""Automatic equivalent-variant selection never confuses categories or increases load."""
from pathlib import Path

root=Path(__file__).resolve().parents[1]
engine=(root/"progress-engine/src/main/java/pe/asterionsystem/core/engine/missions/MissionPlanner.java").read_text()
policy=(root/"progress-engine/src/main/java/pe/asterionsystem/core/engine/progression/CategoryEffortPolicy.java").read_text()
store=(root/"app/src/main/java/pe/asterionsystem/app/MissionTrackingStore.java").read_text()
screen=(root/"app/src/main/java/pe/asterionsystem/app/MissionsActivity.java").read_text()
tests=(root/"progress-engine/src/test/java/pe/asterionsystem/core/engine/missions/AutonomousCategoryMissionTest.java").read_text()
for fragment in (
    "CategoryEffortPolicy.Decision decision",
    "decision.category",
    "option.typeId().equals(prior.typeId())",
    "option.difficulty()==prior.difficulty()",
    "profile.equipment.containsAll(variant.equipment())",
    "chosen, false",
):
    assert fragment in engine,fragment
assert "MissionTrackingStore.latestCompletedForCategory(" in screen
assert "DailyMissionStore.write(this,today,profile,catalog," in screen
assert "planner.daily(" in screen
assert "Explorar variantes de esta categoría" not in screen
assert "requestEffort(today,category)" in screen
assert "El motor buscará automáticamente" in policy
assert "category_plan_current_" in store and "category_plan_previous_" in store
assert "oldDay.isBefore(today)" in store
assert "date.isBefore(today.minusDays(21))" in store
for fragment in (
    "lowEffortChangesVariantAutomaticallyWithinSameTypeAndLevel",
    "moderateEffortKeepsSameVariantWithoutUserSelection",
    "cannotApplyLowerBodyEffortToUpperBodyDay",
    "highEffortAutomaticallyReducesActivitiesAndTechnicalLevel",
    "recommendationsAreDeterministicWithSameHistory",
):
    assert fragment in tests,fragment
print("OK: selección automática por categoría, sin cambios de carga automáticos.")
