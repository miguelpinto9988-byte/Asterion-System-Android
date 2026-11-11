#!/usr/bin/env python3
"""Exercise taxonomy extension is append-only, five-way, and safe to browse."""
import csv
from pathlib import Path
from collections import Counter
import unicodedata
import re

ROOT=Path(__file__).resolve().parents[1]
BASE=ROOT/"progress-engine/src/main/resources/asterion_exercises/v1"
ENGINE=ROOT/"progress-engine/src/main/java/pe/asterionsystem/core/engine"
APP=ROOT/"app/src/main/java/pe/asterionsystem/app"

def load(file,expected):
    with (BASE/file).open("r",encoding="utf-8",newline="") as fh:
        rows=list(csv.DictReader(fh,delimiter="\t"))
    assert rows or file=="intensity.tsv"
    assert list(rows[0])==expected if rows else True,file
    return rows

families=load("families.tsv",["family_id","name","category","pattern"])
types=load("types.tsv",["type_id","family_id","name"])
variants=load("variants.tsv",["variant_id","type_id","name","level",
                              "equipment","execution","supervision"])
tags=load("intensity.tsv",["variant_id","intensity"])

assert (len(families),len(types),len(variants),len(tags))==(155,474,1257,752)
ids=[v["variant_id"] for v in variants]
assert len(set(ids))==len(ids)
assert len(set(f["family_id"] for f in families))==len(families)
assert len(set(t["type_id"] for t in types))==len(types)
assert len(set(t["variant_id"] for t in tags))==len(tags)
assert all(t["variant_id"] in ids for t in tags)
assert all(t["intensity"] in ("baja","moderada","alta","variable") for t in tags)
assert Counter(t["intensity"] for t in tags)=={
    "baja":400,"moderada":264,"variable":71,"alta":17
}
norm=lambda s:"".join(c for c in unicodedata.normalize("NFD",s.casefold())
                    if not unicodedata.combining(c))
assert len(set(map(norm,(v["name"] for v in variants))))==1257

family_lookup={f["family_id"]:f for f in families}
type_lookup={t["type_id"]:t for t in types}
assert all(t["family_id"] in family_lookup for t in types)
assert all(v["type_id"] in type_lookup for v in variants)
assert all(re.fullmatch("[a-z][a-z0-9_]{1,63}",v["variant_id"]) for v in variants)
assert all(1<=int(v["level"])<=5 for v in variants)
assert all(v["supervision"]=="1" if int(v["level"])>=4 else True
           for v in variants)
tagged_ids={t["variant_id"]:t["intensity"] for t in tags}
lookup={v["variant_id"]:v for v in variants}
for vid,intensity in tagged_ids.items():
    if intensity=="alta":
        assert lookup[vid]["supervision"]=="1",vid

map_cat={"coordinacion":"movilidad","integrado":"tren_central"}
categories=Counter(map_cat.get(f["category"],f["category"]) for f in families)
assert categories=={
    "tren_superior":33,"tren_central":29,"tren_inferior":33,"movilidad":30,"cardio":30
}
assert "descanso" not in categories

catalog=(ENGINE/"exercises/ExerciseCatalog.java").read_text(encoding="utf-8")
loader=(ENGINE/"exercises/ExerciseCatalogLoader.java").read_text(encoding="utf-8")
planner=(ENGINE/"missions/MissionPlanner.java").read_text(encoding="utf-8")
android=(APP/"MissionsActivity.java").read_text(encoding="utf-8")
repo=(APP/"data/AssetExerciseCatalogRepository.java").read_text(encoding="utf-8")
assert "tag!=ExerciseIntensity.HIGH && tag!=ExerciseIntensity.VARIABLE" in catalog
assert "catalog.eligibleForAutomaticSuggestion(variant)" in planner
assert "ExerciseCatalogLoader.load(families, types, variants," in repo
assert 'context.getAssets().open(DIRECTORY + "intensity.tsv")' in repo
assert "ExerciseIntensity.parse(row[1])" in loader
assert "catalog.intensity(v.id())" in android
assert "showTypeFiltered" in android and '"Filtrar por intensidad"' in android
assert ids[:505] and tagged_ids.get("sentadilla_bilateral_v01") is None
assert "marcha_v01" in ids and "sentadilla_bilateral_v01" in ids
print("OK: 155 familias, 474 tipos, 1257 variantes (752 con intensidad) y sin selección automática de alta intensidad.")
