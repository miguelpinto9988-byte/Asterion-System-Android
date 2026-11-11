#!/usr/bin/env python3
"""End-to-end invariants of 100-family / 917-variant exercise taxonomy."""
import csv
from collections import Counter
from pathlib import Path
import re
import unicodedata

ROOT=Path(__file__).resolve().parents[1]
DATA=ROOT/"progress-engine/src/main/resources/asterion_exercises/v1"
CORE=ROOT/"progress-engine/src/main/java/pe/asterionsystem/core/engine/exercises"
APP=ROOT/"app/src/main/java/pe/asterionsystem/app"
MISSION=ROOT/"progress-engine/src/main/java/pe/asterionsystem/core/engine/missions"

def read(file):
    with (DATA/file).open(encoding="utf-8",newline="") as stream:
        return list(csv.DictReader(stream,delimiter="\t"))

families=read("families.tsv")
types=read("types.tsv")
variants=read("variants.tsv")
intensity=read("intensity.tsv")
assert tuple(map(len,(families,types,variants,intensity)))==(155,474,1257,752)
def unique(rows,key):
    keys=[r[key] for r in rows]
    assert len(keys)==len(set(keys)),key
    return set(keys)
family_ids=unique(families,"family_id")
type_ids=unique(types,"type_id")
variant_ids=unique(variants,"variant_id")
unique(intensity,"variant_id")
assert set(t["family_id"] for t in types)==family_ids
assert set(v["type_id"] for v in variants)==type_ids
assert set(i["variant_id"] for i in intensity)<=variant_ids
assert set(int(x["level"]) for x in variants)<={1,2,3,4,5}
assert all(v["supervision"]=="1" for v in variants if int(v["level"])>=4)
category_mapper={"coordinacion":"movilidad","integrado":"tren_central"}
group_count=Counter(category_mapper.get(f["category"],f["category"]) for f in families)
assert group_count=={"tren_superior":33,"tren_central":29,"tren_inferior":33,
                     "movilidad":30,"cardio":30}
assert all(f["category"] in {"tren_superior","tren_central","tren_inferior","movilidad","cardio"} for f in families)
normalized=lambda value:"".join(c for c in unicodedata.normalize("NFD",value.casefold())
                                 if not unicodedata.combining(c))
assert len({normalized(v["name"]) for v in variants})==1257
assert len({v["name"] for v in variants})==1257

# The historical 75/204/817 rows are a literal prefix; expansion is append-only.
old_families,families_new=families[:75],families[75:100]
old_types,types_new=types[:204],types[204:254]
old_variants,variants_new=variants[:817],variants[817:917]
old_intensity,intensity_new=intensity[:312],intensity[312:412]
assert len(families_new)==25 and len(types_new)==50
assert len(variants_new)==100 and len(intensity_new)==100
assert {"sentadillas","transportes","equilibrio","recorridos_al_aire_libre"} <= {
    f["family_id"] for f in old_families}
assert {"sentadilla_bilateral_v01","flexiones_v04","marcha_v01"} <= {
    v["variant_id"] for v in old_variants}
assert all(f["category"] in {"tren_superior","tren_central","tren_inferior","movilidad","cardio"}
           for f in families_new)
assert Counter(f["category"] for f in families_new)==dict.fromkeys(group_count,5)
family_to_cat={f["family_id"]:category_mapper.get(f["category"],f["category"])
               for f in families}
type_to_cat={t["type_id"]:family_to_cat[t["family_id"]] for t in types}
new_ids={v["variant_id"] for v in variants_new}
assert all(v["type_id"] in {t["type_id"] for t in types_new} for v in variants_new)
assert all(v["supervision"]=="0" and v["level"] in ("1","2") for v in variants_new)
assert all(v["equipment"] in {"sin_equipo","silla","pared","colchoneta"}
           for v in variants_new)
assert all(v["execution"]=="controlado" for v in variants_new)
assert set(i["variant_id"] for i in intensity_new)==new_ids
assert all(i["intensity"] in ("baja","moderada") for i in intensity_new)
assert Counter(i["intensity"] for i in intensity)=={
    "baja":400,"moderada":264,"variable":71,"alta":17}
for category in group_count:
    assert sum(type_to_cat[t["type_id"]]==category for t in types_new)==10
    assert sum(type_to_cat[v["type_id"]]==category for v in variants_new)==20

focus=(CORE/"ExerciseMuscleFocus.java").read_text(encoding="utf-8")
ids=re.findall(r'Map\.entry\("([a-z0-9_]+)",new Focus\("',focus)
assert len(ids)==155 and set(ids)==family_ids
index=(CORE/"ExerciseCategoryIndex.java").read_text(encoding="utf-8")
catalog=(CORE/"ExerciseCatalog.java").read_text(encoding="utf-8")
activity=(APP/"MissionsActivity.java").read_text(encoding="utf-8")
planner=(MISSION/"MissionPlanner.java").read_text(encoding="utf-8")
for marker in ("families(String category)","types(String category)",
               "variants(String category)","categoryForVariant(String id)",
               "familyForVariant(String id)","freeze(fs)","requireCategory"):
    assert marker in index,marker
assert "new ExerciseCategoryIndex(this.families,this.types,this.variants)" in catalog
assert "catalog.categoryIndex().families(code)" in activity
assert "catalog.categoryIndex().types(code)" in activity
assert "catalog.categoryIndex().variants(code)" in activity
assert "catalog.categoryIndex().categoryForType(type.id())" in planner
print("OK: 155 families, 474 types, 1257 distinct variants, five categories, original IDs, "
      "155 explicit focus labels and safe two-level new variants.")
