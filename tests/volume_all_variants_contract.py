#!/usr/bin/env python3
"""Every real catalog variant has a validated, explicit numerical unit and baseline."""
from pathlib import Path
import csv

root=Path(__file__).resolve().parents[1]
assets=root/"progress-engine/src/main/resources/asterion_exercises/v1"
with (assets/"variants.tsv").open(encoding="utf-8",newline="") as file:
    variants={r["variant_id"]:r for r in csv.DictReader(file,delimiter="\t")}
with (assets/"volume_profiles.tsv").open(encoding="utf-8",newline="") as file:
    data=list(csv.DictReader(file,delimiter="\t"))
assert len(variants)==1257,len(variants)
assert len(data)==1257,len(data)
assert {r["variant_id"] for r in data}==set(variants)
units={}
for row in data:
    unit=row["unit"]
    assert unit in ("REPETITIONS","SECONDS"),row
    initial_sets=int(row["initial_sets"])
    initial=int(row["initial_value"])
    low=int(row["minimum_value"])
    high=int(row["maximum_value"])
    step=int(row["increment"])
    assert 1<=initial_sets<=3
    assert 1<=low<=initial<=high<=(180 if unit=="SECONDS" else 20)
    assert 1<=step<=5
    units[unit]=units.get(unit,0)+1
assert units["REPETITIONS"]>600 and units["SECONDS"]>200,units
profiles={r["variant_id"]:r for r in data}
for id,unit in {
    "sentadilla_bilateral_v03":"REPETITIONS",
    "flexiones_v02":"REPETITIONS",
    "superman_v01":"REPETITIONS",
    "plancha_frontal_v01":"SECONDS",
    "equilibrio_unipodal_v01":"SECONDS",
    "marcha_v02":"SECONDS",
    "ciclo_estatico_v01":"SECONDS",
}.items():
    assert profiles[id]["unit"]==unit,(id,profiles[id])

# Golden references after the second increase, all still within profile caps.
expected_start={
    "sentadilla_bilateral_v01":12,
    "sentadilla_bilateral_v03":10,
    "sentadilla_bilateral_v07":7,
    "flexiones_v02":12,
    "plancha_frontal_v01":20,
    "equilibrio_unipodal_v01":25,
    "marcha_v02":85,
    "ciclo_estatico_v01":85,
}
for variant_id, initial in expected_start.items():
    assert int(profiles[variant_id]["initial_value"])==initial,variant_id
assert all(int(row["initial_sets"])==1 for row in data)
assert all(int(row["initial_value"])>=
           (5 if row["unit"]=="REPETITIONS" else 11) for row in data)

engine=(root/"progress-engine/src/main/java/pe/asterionsystem/core/engine/progression/VolumeProgressionEngine.java").read_text()
store=(root/"app/src/main/java/pe/asterionsystem/app/VolumeProgressStore.java").read_text()
activity=(root/"app/src/main/java/pe/asterionsystem/app/RoutineStepActivity.java").read_text()
missions=(root/"app/src/main/java/pe/asterionsystem/app/MissionsActivity.java").read_text()
coordinator=(root/"progress-engine/src/main/java/pe/asterionsystem/core/engine/progression/CoordinatedTrainingEngine.java").read_text()
catalog=(root/"progress-engine/src/main/java/pe/asterionsystem/core/engine/progression/ExerciseVolumeProfileCatalog.java").read_text()
for symbol in ("public enum Unit", "REPETITIONS", "SECONDS",
               "initialSets", "initialValue", "maximumValue",
               "catalog.variants().size()", "eligibleForAutomaticSuggestion"):
    assert symbol in catalog,symbol
for symbol in ("public final ExerciseVolumeProfileCatalog.Unit unit;",
               "recent.quantity", "profile.increment", "profile.maximumValue",
               "profile.initialSets", "obs.unit==profile.unit"):
    assert symbol in engine,symbol
for symbol in ("entry.unit.name()", "p.length != 10 && p.length != 11",
               "Unit.REPETITIONS", "old.unit!=entry.unit"):
    assert symbol in store,symbol
for symbol in ("startOrCancelExerciseTimer(", "exerciseTimer.start()",
               "volumeProfiles.require(current)", "reference.unit",
               "AssetExerciseVolumeProfileRepository"):
    assert symbol in activity,symbol
for symbol in ("volumeProfiles", "AssetExerciseVolumeProfileRepository",
               "numericForSelection", "volume.unit.label", "volume.offeredQuantity"):
    assert symbol in missions,symbol
assert "ExerciseVolumeProfileCatalog profiles" in coordinator
print("OK: 1257 variantes, referencia individual y unidad segura",units)
