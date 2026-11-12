#!/usr/bin/env python3
"""Validate 1257 explicit avatar commands and preserve honest visual fallback."""
from collections import Counter
from pathlib import Path
import csv
import json
import re
import sys

root=Path(__file__).resolve().parents[1]
assets=root/"app/src/main/assets/asterion_exercises/v1"
taxonomy=root/"progress-engine/src/main/resources/asterion_exercises/v1"
def load(path):
    with path.open(encoding="utf-8",newline="") as stream:
        return list(csv.DictReader(stream,delimiter="\t"))
rows=load(assets/"avatar_motion_commands.tsv")
variants={x["variant_id"]:x for x in load(taxonomy/"variants.tsv")}
types={x["type_id"]:x["family_id"] for x in load(taxonomy/"types.tsv")}
families={x["family_id"]:x for x in load(taxonomy/"families.tsv")}
assert len(rows)==len(variants)==1257
assert len({x["variant_id"] for x in rows})==1257
assert {x["variant_id"] for x in rows}==set(variants)
assert len(types)==474 and len(families)==155

base=root/"app/src/main/java/pe/asterionsystem/app"
timeline=(base/"AvatarJointTimeline.java").read_text(encoding="utf-8")
catalog=(base/"AvatarExerciseCommandCatalog.java").read_text(encoding="utf-8")
viewer=(base/"NativeAvatarActivity.java").read_text(encoding="utf-8")
guided=(base/"RoutineStepActivity.java").read_text(encoding="utf-8")
motion=(base/"AvatarMotionEngine.java").read_text(encoding="utf-8")

match=re.search(r"public enum Command\s*\{(.*?)\}",timeline,re.S)
assert match
supported={x.strip() for x in match.group(1).replace("\n","").split(",") if x.strip()}
assert len(supported)==49,sorted(supported)
for cmd in supported:
    assert f"case {cmd}:" in timeline,cmd
assert len(re.findall(r'\(".*?"\)',timeline.split("public enum Command")[0]))>=23

counts=Counter();illustrated=[];families_covered=set();new_exercises=[]
for row in rows:
    ident=row["variant_id"];v=variants[ident]
    family=types[v["type_id"]];f=families[family]
    assert row["category"]==f["category"],ident
    assert row["level"]==v["level"],ident
    assert row["execution"]==v["execution"],ident
    assert row["equipment"]==v["equipment"],ident
    assert row["supervision"]==v["supervision"],ident
    assert row["command"] in supported,ident
    assert row["camera"] in ("SIDE","FRONT","TOP","FRONT_LATERAL",
                               "FRONT_BALANCE","FRONT_SIDE_STEP"),ident
    assert row["status"] in ("ILLUSTRATIVE","SPEC_ONLY"),ident
    assert (row["status"]=="SPEC_ONLY")==(row["clip"]=="NONE"),ident
    if row["status"]=="ILLUSTRATIVE":
        assert int(row["level"])<=3 and row["supervision"]=="0"
        assert row["clip"] in ("SQUAT","LUNGE","HIP_HINGE",
                               "ARM_RAISE","ARM_CURL","KNEE_CURL","MARCH","FRONT",
                               "FRONT_LATERAL","FRONT_BALANCE","FRONT_SIDE_STEP")
        illustrated.append(row)
    counts[(row["category"],row["status"])]+=1
    families_covered.add(family)
    if ident in ("curl_biceps_v01","flexion_rodilla_bipeda_v03",
                 "puente_gluteo_v02","rotacion_cervical_suave_v02",
                 "equilibrio_unipodal_v04","alcance_unipodal_v01"):
        new_exercises.append(row)

assert len(families_covered)==155
assert sum(counts.values())==1257
assert len(illustrated)==10
assert len({row["command"] for row in rows})>=48
for string in ("postInvalidateOnAnimation()","AvatarMotionEngine.pose("):
    assert string in (base/"NativeAvatarView.java").read_text(encoding="utf-8")
for string in ('putExtra("variant_id",current.id())',
               "Ver comandos y avatar del ejercicio"):
    assert string in guided
for string in ("selectedEntry.canIllustrate()","avatar.setVisibility(View.GONE)",
               "showJointStages()","commandWorker.execute"):
    assert string in viewer,string
for symbol in ("ARM_CURL","KNEE_CURL","SQUAT","LUNGE","BURPEE"):
    assert symbol in motion
assert "ILLUSTRATIVE" in catalog and "SPEC_ONLY" in catalog
assert "status==Status.SPEC_ONLY" in catalog
assert len(new_exercises)==6

if "--samples" in sys.argv:
    out=root/"build/avatar-command-samples/muestras_catalogo.json"
    out.parent.mkdir(parents=True,exist_ok=True)
    out.write_text(json.dumps({
        "source":"Catálogo real Android + registro de comandos v1",
        "status":"Muestreo de comandos; solo ILLUSTRATIVE tiene render específico",
        "items":[{"id":x["variant_id"],"name":variants[x["variant_id"]]["name"],
                  "command":x["command"],"camera":x["camera"],
                  "clip":x["clip"],"status":x["status"],
                  "level":x["level"],"equipment":x["equipment"]}
                 for x in new_exercises]
    },ensure_ascii=False,indent=2),encoding="utf-8")
    print("Saved",out)
print("OK: 1257 variantes, 155 familias, 49 comandos definidos, 10 escenas ilustrativas validadas")
