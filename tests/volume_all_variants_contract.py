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

# Golden references after the category-specific calibration; within profile caps.
expected_start={
    "sentadilla_bilateral_v01":17,
    "sentadilla_bilateral_v03":15,
    "sentadilla_bilateral_v07":12,
    "flexiones_v02":15,
    "plancha_frontal_v01":25,
    "equilibrio_unipodal_v01":30,
    "marcha_v02":95,
    "ciclo_estatico_v01":95,
}
for variant_id, initial in expected_start.items():
    assert int(profiles[variant_id]["initial_value"])==initial,variant_id
# All five exercise categories now have individually calibrated references.
assert int(profiles["superman_v01"]["initial_value"])==14
assert int(profiles["alcance_unipodal_v01"]["initial_value"])==12
assert int(profiles["talones_alternos_v02"]["initial_value"])==13

with (assets/"types.tsv").open(encoding="utf-8",newline="") as file:
    types={r["type_id"]:r["family_id"] for r in csv.DictReader(file,delimiter="\t")}
with (assets/"families.tsv").open(encoding="utf-8",newline="") as file:
    families={r["family_id"]:r["category"] for r in csv.DictReader(file,delimiter="\t")}
coverage={}
for row in data:
    variant=variants[row["variant_id"]]
    category=families[types[variant["type_id"]]]
    key=(category,row["unit"])
    coverage[key]=coverage.get(key,0)+1
expected_coverage={
    ("tren_inferior","REPETITIONS"):268,
    ("tren_superior","REPETITIONS"):276,
    ("tren_central","REPETITIONS"):165,
    ("movilidad","REPETITIONS"):197,
    ("cardio","REPETITIONS"):18,
    ("tren_inferior","SECONDS"):26,
    ("tren_superior","SECONDS"):5,
    ("tren_central","SECONDS"):65,
    ("movilidad","SECONDS"):46,
    ("cardio","SECONDS"):191,
}
assert coverage==expected_coverage,coverage
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
print("OK: 1257 variantes, cinco categorías, dos unidades y límites seguros",units)
