#!/usr/bin/env python3
"""Check source provenance, canonical five-way taxonomy and safe sport tags."""
import csv
from collections import Counter
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
DATA=ROOT/"progress-engine/src/main/resources/asterion_exercises/v1"
CORE=ROOT/"progress-engine/src/main/java/pe/asterionsystem/core/engine/exercises"
APP=ROOT/"app/src/main/java/pe/asterionsystem/app"
DOC=ROOT/"docs/investigacion/multideporte-curado-20261111.tsv"

def read(file):
    with file.open(encoding="utf-8",newline="") as f:
        return list(csv.DictReader(f,delimiter="\t"))

families=read(DATA/"families.tsv")
types=read(DATA/"types.tsv")
variants=read(DATA/"variants.tsv")
intensity=read(DATA/"intensity.tsv")
registry=read(DATA/"sport_registry.tsv")
sports=read(DATA/"sport_tags.tsv")
research=read(DOC)
assert (len(families),len(types),len(variants),len(intensity))==(155,474,1257,752)
assert (len(registry),len(sports),len(research))==(56,125,100)
assert all({x["category"] for x in families}=={
    "tren_superior","tren_inferior","tren_central","movilidad","cardio"}
           for _ in range(1)), "Sixth category or legacy top-level code detected"
by_family={r["family_id"]:r for r in families}
by_type={r["type_id"]:r for r in types}
by_variant={r["variant_id"]:r for r in variants}
sports_by_type={r["type_id"]:r for r in sports}
codes={r["code"] for r in registry}
assert len(by_family)==len(families)
assert len(by_type)==len(types)
assert len(by_variant)==len(variants)
assert len(sports_by_type)==len(sports)
assert len(codes)==56
assert len({r["name"] for r in registry})==56
assert all(t["family_id"] in by_family for t in types)
assert all(v["type_id"] in by_type for v in variants)
assert all(k in by_type for k in sports_by_type)
assert len({r["variant_id"] for r in intensity})==len(intensity)
assert {r["variant_id"] for r in intensity}<={r["variant_id"] for r in variants}
assert Counter(f["category"] for f in families)=={
    "tren_superior":27,"tren_inferior":27,"tren_central":23,
    "movilidad":24,"cardio":24}
assert families[0]["family_id"]=="sentadillas"
assert types[0]["type_id"]=="sentadilla_bilateral"
assert variants[0]["variant_id"]=="sentadilla_bilateral_v01"
assert variants[916]["variant_id"]=="seguir_pasos_ritmicos_v02"
assert len({r["name"].casefold() for r in variants})==1257
assert all(v["supervision"]=="1" for v in variants
           if int(v["level"])>=4)

tag_use=Counter()
manual=0
source_urls=set()
for row in research:
    type_id=row["type_id"]
    variant=by_variant[type_id+"_v01"]
    tag=sports_by_type[type_id]
    assert row["category"]==by_family[by_type[type_id]["family_id"]]["category"]
    assert row["family_id"]==by_type[type_id]["family_id"]
    assert row["source_url"].startswith("https://")
    assert tag["source_url"]==row["source_url"]
    assert variant["equipment"]==row["equipment"]
    assert variant["supervision"]==row["supervision"]
    assert variant["level"]==row["level"]
    assert tag["manual_only"]==row["supervision"]
    source_urls.add(row["source_url"])
    tags=tag["sports"].split("+")
    assert tags and len(set(tags))==len(tags)
    assert set(tags)<=codes
    for code in tags:tag_use[code]+=1
    if tag["manual_only"]=="1":
        manual+=1
        assert variant["supervision"]=="1"
        assert int(variant["level"])>=4
        assert row["intensity"] in ("variable","alta")
    assert row["type_name"] and row["goal"] and row["source_org"]
assert len(tag_use)==56 and min(tag_use.values())>0
assert {"sentadilla_bilateral","flexiones","dominadas","plancha_frontal","marcha"} <= set(sports_by_type)
assert manual>=25
assert len(source_urls)>=25
assert by_variant["dep_burpee_tecnico_v01"]["supervision"]=="1"
assert by_variant["dep_carrera_ida_vuelta_v01"]["supervision"]=="1"
assert len([x for x in variants if x["variant_id"].startswith("dep_")])==100
focus=(CORE/"ExerciseMuscleFocus.java").read_text(encoding="utf-8")
assert focus.count("Map.entry(")==155
loader=(CORE/"ExerciseCatalogLoader.java").read_text(encoding="utf-8")
index=(CORE/"ExerciseSportIndex.java").read_text(encoding="utf-8")
catalog=(CORE/"ExerciseCatalog.java").read_text(encoding="utf-8")
adapter=(APP/"data/AssetExerciseCatalogRepository.java").read_text(encoding="utf-8")
activity=(APP/"MissionsActivity.java").read_text(encoding="utf-8")
assert "sportRegistry,sportTags,parsedTypes" in loader
assert "ExerciseSportIndex.load(" in loader
assert "sport_registry.tsv" in adapter and "sport_tags.tsv" in adapter
assert "sportIndex.manualOnly(variant.typeId())" in catalog
assert "sportIndex.includes(type.id(), sportCode)" in catalog
assert "chooseSport" in activity and "chooseEquipment" in activity
assert "showSportLibrary" in activity and "showEquipmentLibrary" in activity
assert "57," not in index  # sport tags are not main training categories
print(f"OK: 5 primary categories, 155 families, 474 types, 1257 variants; "
      f"{len(tag_use)} sports, {len(source_urls)} citations, {manual} manual-only records.")
