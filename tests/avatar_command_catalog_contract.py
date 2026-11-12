#!/usr/bin/env python3
"""Ensures that all 1,257 variants have explicit commands and safe visual modes."""
import csv
from pathlib import Path
from collections import Counter

root=Path(__file__).resolve().parents[1]
assets=root/"progress-engine/src/main/resources/asterion_exercises/v1"
java=root/"app/src/main/java/pe/asterionsystem/app"

with (assets/"variants.tsv").open(encoding="utf-8", newline="") as f:
    variants={r["variant_id"]:r for r in csv.DictReader(f, delimiter="\t")}
with (assets/"avatar_command_profiles.tsv").open(encoding="utf-8",newline="") as f:
    commands=list(csv.DictReader(f,delimiter="\t"))

assert len(variants)==1257,len(variants)
assert len(commands)==1257,len(commands)
assert len({row["variant_id"] for row in commands})==1257
assert {row["variant_id"] for row in commands}==set(variants)
assert all(row["type_id"]==variants[row["variant_id"]]["type_id"] for row in commands)
states=Counter()
for row in commands:
    status=row["visual_status"]
    camera=row["camera"]
    movement=row["command"]
    assert camera in ("SIDE","FRONT","FRONT_SQUAT","FRONT_JACKS","FRONT_ELBOW","TOP"),row
    assert status in ("SCHEMATIC","TEXT_ONLY"),row
    assert 2000<=int(row["tempo_ms"])<=12000,row
    assert 0<float(row["amplitude"])<=1,row
    for joint in ("neck","shoulder","elbow","wrist","trunk","hip","knee","ankle"):
        values=[int(x) for x in row[f"{joint}_deg"].split(">")]
        assert len(values)==3 and values[0]==values[-1],row
        assert all(abs(v)<=180 for v in values),row
    assert all(row[k].strip() for k in
               ("start_pose","active_pose","return_pose","notice")),row
    if movement.endswith("_REFERENCE") or movement in ("HIP_ABDUCTION","SIDE_BEND","PLANK_HOLD"):
        assert status=="TEXT_ONLY",row
    states[status]+=1

assert states["SCHEMATIC"]>600
assert states["TEXT_ONLY"]>150
index={r["variant_id"]:r for r in commands}
for id in ("superman_v01","ciclo_estatico_v01","equilibrio_unipodal_v01"):
    assert index[id]["visual_status"]=="TEXT_ONLY",id
assert index["sentadilla_bilateral_v03"]["command"]=="SQUAT"
assert index["sentadilla_bilateral_v03"]["visual_status"]=="SCHEMATIC"

activity=(java/"NativeAvatarActivity.java").read_text(encoding="utf-8")
step=(java/"RoutineStepActivity.java").read_text(encoding="utf-8")
view=(java/"NativeAvatarView.java").read_text(encoding="utf-8")
kinematics=(java/"AvatarCommandKinematics.java").read_text(encoding="utf-8")
assert 'putExtra("variant_id",current.id())' in step
for needle in ("loadCommandCatalog(", "showCatalogPicker(", "showCommandDetails(",
               "selectCatalogEntry(", "technicalDescription()"):
    assert needle in activity,needle
for needle in ("setCatalogEntry(", "getCatalogEntry(", "catalogEntry.canAnimate()",
               "AvatarCommandKinematics.pose("):
    assert needle in view,needle
for needle in ("projectedJointTrace(", "AvatarMotionEngine.pose(old,phase,body)",
               "nearArm.targetLimited", "farLeg.targetLimited"):
    assert needle in kinematics,needle
print("OK: catálogo avatar 1.257 variantes, modos seguros y rutas UI",dict(states))
