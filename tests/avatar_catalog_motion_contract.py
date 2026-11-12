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
assert len(supported)==63,sorted(supported)
for cmd in supported:
    assert f"case {cmd}:" in timeline,cmd
assert len(re.findall(r'\(".*?"\)',timeline.split("public enum Command")[0]))>=24

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
                               "FRONT_BALANCE","FRONT_SIDE_STEP","FRONT_NECK_TILT","HIP_EXTENSION_STAND",
                               "GLUTE_BRIDGE_SHORT","GLUTE_BRIDGE_BASIC",
                               "SEATED_KNEE_EXTENSION","SEATED_TOE_RAISE",
                               "SEATED_FORWARD_REACH",
                               "GLUTE_BRIDGE_PAUSE","SEATED_KNEE_EXTENSION_PAUSE",
                               "SEATED_KNEE_EXTENSION_ALTERNATING",
                               "GLUTE_BRIDGE_BAND","SEATED_TOE_RAISE_SINGLE",
                               "SEATED_HEEL_RAISE","SEATED_HEEL_RAISE_GENTLE",
                               "SEATED_HEEL_TOE_ALTERNATION",
                               "STANDING_HEEL_RAISE","STANDING_TOE_RAISE"),ident
    assert row["status"] in ("ILLUSTRATIVE","SPEC_ONLY"),ident
    assert (row["status"]=="SPEC_ONLY")==(row["clip"]=="NONE"),ident
    if row["status"]=="ILLUSTRATIVE":
        if row["clip"]!="AUTHORED_VARIANT":
            assert int(row["level"])<=3 and row["supervision"]=="0"
        assert row["clip"] in ("AUTHORED_VARIANT","SQUAT","LUNGE","HIP_HINGE",
                               "ARM_RAISE","ARM_CURL","KNEE_CURL","MARCH","FRONT",
                               "FRONT_LATERAL","FRONT_BALANCE","FRONT_SIDE_STEP",
                               "FRONT_NECK_TILT","HIP_EXTENSION_STAND",
                               "GLUTE_BRIDGE_SHORT","GLUTE_BRIDGE_BASIC",
                               "SEATED_KNEE_EXTENSION","SEATED_TOE_RAISE",
                               "SEATED_FORWARD_REACH","GLUTE_BRIDGE_PAUSE",
                               "SEATED_KNEE_EXTENSION_PAUSE",
                               "SEATED_KNEE_EXTENSION_ALTERNATING",
                               "GLUTE_BRIDGE_BAND","SEATED_TOE_RAISE_SINGLE",
                               "SEATED_HEEL_RAISE","SEATED_HEEL_RAISE_GENTLE",
                               "SEATED_HEEL_TOE_ALTERNATION",
                               "STANDING_HEEL_RAISE","STANDING_TOE_RAISE")
        illustrated.append(row)
    counts[(row["category"],row["status"])]+=1
    families_covered.add(family)
    if ident in ("curl_biceps_v01","flexion_rodilla_bipeda_v03",
                 "puente_gluteo_v02","rotacion_cervical_suave_v02",
                 "equilibrio_unipodal_v04","alcance_unipodal_v01",
                 "elevacion_lateral_v01","caminata_banda_v01",
                 "inclinacion_cervical_suave_v01","extension_cadera_bipeda_v01",
                 "paso_lateral_y_retorno_v01",
                 "puente_gluteo_v01","extension_rodilla_sin_carga_v01",
                 "sentadilla_bilateral_v04","marcha_v02",
                 "dorsiflexion_tibial_v01","alcances_tronco_sentado_v02",
                 "puente_gluteo_v03","extension_rodilla_sin_carga_v02",
                 "extension_rodilla_sin_carga_v03",
                 "puente_gluteo_v04","dorsiflexion_tobillo_v01",
                 "elevacion_gemelos_v01","balanceo_talon_punta_sentado_v01",
                 "balanceo_talon_punta_sentado_v02",
                 "elevacion_gemelos_v03","dorsiflexion_tibial_v03"):
        new_exercises.append(row)

assert len(families_covered)==155
assert sum(counts.values())==1257
assert len(illustrated)==138
assert len({row["command"] for row in rows})>=49
for string in ("postInvalidateOnAnimation()","AvatarMotionEngine.pose("):
    assert string in (base/"NativeAvatarView.java").read_text(encoding="utf-8")
for string in ('putExtra("variant_id",current.id())',
               "Ver comandos y avatar del ejercicio"):
    assert string in guided
for string in ("selectedEntry.canIllustrate()","avatar.setVisibility(View.GONE)",
               "commandDiagram.setEntry(entry)","commandDiagram.setVisibility(View.VISIBLE)",
               "showJointStages()","commandWorker.execute"):
    assert string in viewer,string
for symbol in ("ARM_CURL","KNEE_CURL","HIP_EXTENSION_STAND",
               "SQUAT","LUNGE","BURPEE"):
    assert symbol in motion
diagram=(base/"AvatarJointCommandView.java").read_text(encoding="utf-8")
for symbol in ("postInvalidateOnAnimation()","timeline.offset(joint,phase)",
               "No demuestra la ejecución del ejercicio","setFrameListener",
               "AvatarCommandFigurePose.from(timeline,phase)"):
    assert symbol in diagram,symbol
figure=(base/"AvatarCommandFigurePose.java").read_text(encoding="utf-8")
for symbol in ("requireFixedLengths","AvatarRigConstraints.requireFixedLength",
               "leftShoulder","rightShoulder","leftKnee","rightKnee",
               "AvatarJointTimeline.Joint.NECK_SIDE"):
    assert symbol in figure,symbol
assert "ILLUSTRATIVE" in catalog and "SPEC_ONLY" in catalog
assert "status==Status.SPEC_ONLY" in catalog
# An exercise-specific illustration is not interchangeable with a similar
# movement if contacts, equipment or locomotion differ.
by_id={r["variant_id"]:r for r in rows}
for wrong in ("curl_biceps_v05","marcha_v02"):
    assert by_id[wrong]["status"]=="SPEC_ONLY",wrong
    assert by_id[wrong]["clip"]=="NONE",wrong
assert by_id["dorsiflexion_tibial_v01"]["command"]=="SEATED_TOE_RAISE"
assert by_id["alcances_tronco_sentado_v02"]["command"]=="SEATED_FORWARD_REACH"
assert by_id["puente_gluteo_v03"]["command"]=="GLUTE_BRIDGE_PAUSE"
assert by_id["extension_rodilla_sin_carga_v01"]["command"]=="SEATED_KNEE_EXTENSION_SINGLE"
assert by_id["extension_rodilla_sin_carga_v02"]["command"]=="SEATED_KNEE_EXTENSION_ALTERNATING"
assert by_id["extension_rodilla_sin_carga_v03"]["command"]=="SEATED_KNEE_EXTENSION_PAUSE"
assert by_id["dorsiflexion_tobillo_v01"]["command"]=="SEATED_TOE_RAISE_SINGLE"
assert by_id["elevacion_gemelos_v01"]["command"]=="SEATED_HEEL_RAISE"
assert by_id["balanceo_talon_punta_sentado_v01"]["command"]=="SEATED_HEEL_RAISE_GENTLE"
assert by_id["balanceo_talon_punta_sentado_v02"]["command"]=="SEATED_HEEL_TOE_ALTERNATION"
assert by_id["elevacion_gemelos_v03"]["command"]=="STANDING_HEEL_RAISE"
assert by_id["dorsiflexion_tibial_v03"]["command"]=="STANDING_TOE_RAISE"

for ident,clip,equipment in (
        ("puente_gluteo_v01","GLUTE_BRIDGE_SHORT","colchoneta"),
        ("puente_gluteo_v02","GLUTE_BRIDGE_BASIC","colchoneta"),
        ("extension_rodilla_sin_carga_v01","SEATED_KNEE_EXTENSION","silla"),
        ("dorsiflexion_tibial_v01","SEATED_TOE_RAISE","silla"),
        ("alcances_tronco_sentado_v02","SEATED_FORWARD_REACH","silla"),
        ("puente_gluteo_v03","GLUTE_BRIDGE_PAUSE","colchoneta"),
        ("extension_rodilla_sin_carga_v02",
                "SEATED_KNEE_EXTENSION_ALTERNATING","silla"),
        ("extension_rodilla_sin_carga_v03",
                "SEATED_KNEE_EXTENSION_PAUSE","silla"),
        ("puente_gluteo_v04","GLUTE_BRIDGE_BAND","banda+colchoneta"),
        ("dorsiflexion_tobillo_v01","SEATED_TOE_RAISE_SINGLE","silla"),
        ("elevacion_gemelos_v01","SEATED_HEEL_RAISE","silla"),
        ("balanceo_talon_punta_sentado_v01",
                "SEATED_HEEL_RAISE_GENTLE","silla"),
        ("balanceo_talon_punta_sentado_v02",
                "SEATED_HEEL_TOE_ALTERNATION","silla"),
        ("elevacion_gemelos_v03","STANDING_HEEL_RAISE","sin_equipo"),
        ("dorsiflexion_tibial_v03","STANDING_TOE_RAISE","sin_equipo"),
        ("sentadilla_bilateral_v04","SQUAT","sin_equipo")):
    assert by_id[ident]["status"]=="ILLUSTRATIVE",ident
    assert by_id[ident]["clip"]==clip,ident
    assert by_id[ident]["equipment"]==equipment,ident
dedicated=(base/"AvatarDedicatedScene.java").read_text(encoding="utf-8")
dedicated_view=(base/"AvatarDedicatedSceneRenderer.java").read_text(encoding="utf-8")
native=(base/"NativeAvatarView.java").read_text(encoding="utf-8")
for marker in ("GLUTE_BRIDGE_SHORT","GLUTE_BRIDGE_BASIC",
               "SEATED_KNEE_EXTENSION","AvatarMotionEngine.assertSkeleton"):
    assert marker in dedicated,marker
for marker in ("chair(c,dark)","mat(c,dark)","AvatarSkeletonFrame.fromSide"):
    assert marker in dedicated_view,marker
assert "dedicatedRenderer.draw" in native
assert "avatar.setDedicatedScene(dedicated)" in viewer
manifest=(base/"AvatarDemonstrationManifest.java").read_text(encoding="utf-8")
assert "entry.command!=record.command" in manifest
assert "AvatarDemonstrationManifest.requireConsistent(e)" in catalog
assert "requireConsistent(AvatarExerciseCommandCatalog.Entry entry)" in manifest
assert len(re.findall(r'register\(rows,',manifest))==27


assert len(new_exercises)==27

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
from avatar_completion_progress import summary
progress=summary()
assert progress["body_demonstrations_registered"]==len(illustrated)
assert progress["body_demonstrations_pending"]==1257-len(illustrated)
print("OK: 1257 variantes, 155 familias, 63 comandos definidos, 78 variantes ilustrativas registradas")
print("Avance del catálogo corporal:",progress["percent_registered"],"% / pendientes:",progress["body_demonstrations_pending"])
