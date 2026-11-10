#!/usr/bin/env python3
"""Validate append-only 817-variant catalog and five profile training groups."""
from collections import Counter
import csv
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
DATA=ROOT/"progress-engine/src/main/resources/asterion_exercises/v1"
JAVA=ROOT/"progress-engine/src/main/java/pe/asterionsystem/core/engine"
APP=ROOT/"app/src/main/java/pe/asterionsystem/app"

def records(name,header):
    with (DATA/name).open(encoding="utf-8",newline="") as f:
        reader=csv.DictReader((line for line in f if not line.startswith("#")),
                              delimiter="\t")
        assert reader.fieldnames==header,(name,reader.fieldnames)
        return list(reader)

families=records("families.tsv",["family_id","name","category","pattern"])
types=records("types.tsv",["type_id","family_id","name"])
variants=records("variants.tsv",["variant_id","type_id","name",
                                 "level","equipment","execution","supervision"])
assert (len(families),len(types),len(variants))==(75,204,817)
assert len({f["family_id"] for f in families})==75
assert len({t["type_id"] for t in types})==204
assert len({v["variant_id"] for v in variants})==817
assert len({v["name"].lower() for v in variants})==817,"Duplicate exercise labels"
category={
    "tren_superior":"tren_superior",
    "tronco":"tronco",
    "piernas":"piernas",
    "movilidad":"movilidad",
    "cardio":"cardio",
    "coordinacion":"movilidad",
    "integrado":"tronco",
}
expected={"tren_superior":17,"tronco":13,"piernas":16,"movilidad":15,"cardio":14}
assert Counter(category[f["category"]] for f in families)==expected
family_id={f["family_id"] for f in families}
type_family={t["type_id"]:t["family_id"] for t in types}
assert set(type_family.values())==family_id
assert all(v["type_id"] in type_family for v in variants)
old_ids={"sentadilla_bilateral_v01","flexiones_v04","marcha_v01",
         "gato_vaca_v01","escalera_agilidad_v01"}
assert old_ids <= {v["variant_id"] for v in variants},"Legacy IDs changed"
new=[v for v in variants if (v["variant_id"].startswith("prension_suave_")
          or v["variant_id"].startswith("pasos_danza_basicos_"))]
assert len(new)==6
for variant in variants:
    level=int(variant["level"])
    assert 1<=level<=5
    assert variant["supervision"] in ("0","1")
    if level>=4:
        assert variant["supervision"]=="1"
assert not any(f["category"]=="descanso" for f in families)
mapper=(JAVA/"exercises/ExerciseCategoryMapper.java").read_text(encoding="utf-8")
planner=(JAVA/"missions/MissionPlanner.java").read_text(encoding="utf-8")
activity=(APP/"MissionsActivity.java").read_text(encoding="utf-8")
guided=(APP/"RoutineStepActivity.java").read_text(encoding="utf-8")
assert '"coordinacion"' in mapper and '"integrado"' in mapper
assert "ExerciseCategoryMapper.primaryCategory(" in planner
assert "ExerciseCategoryMapper.weeklyCode(i)" in activity
assert "ExerciseCategoryMapper.displayName(i)" in activity
assert "for(int i=0;i<5;i++)" in activity
assert "ExerciseCategoryMapper.weeklyIndex(taxonomyCode)" in guided
assert "AssetExerciseCatalogRepository(this).load()" in activity
print("OK: 75 familias, 204 tipos, 817 variantes y exactamente 5 categorías operativas.")
