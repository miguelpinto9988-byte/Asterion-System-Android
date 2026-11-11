#!/usr/bin/env python3
"""Protect the organic Canvas avatar from reintroducing rigid joints."""
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
MAIN=ROOT/"app/src/main/java/pe/asterionsystem/app"
TEST=ROOT/"app/src/test/java/pe/asterionsystem/app/AvatarKinematicsTest.java"
view=(MAIN/"NativeAvatarView.java").read_text(encoding="utf8")
renderer=(MAIN/"AvatarRenderer.java").read_text(encoding="utf8")
rig=(MAIN/"AvatarKinematics.java").read_text(encoding="utf8")
tests=TEST.read_text(encoding="utf8")

for required in ("drawTorso(canvas,p,top,hi);", "private void drawTorso(",
                 "torsoPoint(", "private void drawLimb(", "path.cubicTo(",
                 "paint.setStrokeCap(Paint.Cap.ROUND)", "0x3026334C"):
    assert required in renderer,required
for required in ("UPPER_ARM=58f", "FOREARM=63f", "elbowBelow(",
                 "Point elbow=elbowBelow(", "Point farElbow=elbowBelow("):
    assert required in rig,required
for required in ("armBonesRemainConstantThroughFullIllustratedSquat",
                 "elbowsMoveContinuouslyWithoutTeleporting"):
    assert required in tests,required
# No dark joint rings and no new engine, images, or network usage.
joint=renderer[renderer.index("private void drawJoint("):renderer.index("private void drawShoe(")]
assert "Paint.Style.STROKE" not in joint
for forbidden in ("import org.godotengine", "UnityPlayer", "MediaPlayer",
                  "android.permission.INTERNET"):
    assert forbidden not in view+renderer+rig
print("OK: contornos orgánicos, tronco contorneado y codos con dos huesos constantes.")
