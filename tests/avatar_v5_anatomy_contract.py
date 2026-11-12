#!/usr/bin/env python3
"""V5: correct false raised shoulder, partial neutral poses and frontal face."""
from pathlib import Path
BASE=Path(__file__).resolve().parents[1]
SRC=BASE/"app/src/main/java/pe/asterionsystem/app"
read=lambda name:(SRC/name).read_text(encoding="utf8")
kin=read("AvatarKinematics.java")
engine=read("AvatarMotionEngine.java")
view=read("AvatarRenderer.java")
details=read("AvatarArtDetails.java")
for expected in (
    "174.4f+82f*s", "new Point(hip.x-10f,hip.y-1f)",
    "shoulder.x-6f,shoulder.y+10f",
    "wristOnArc(shoulder,fullArmReach",
    "wristOnArc(farShoulder,fullArmReach"):
    assert expected in kin,expected
for expected in ("hy=174.4f","hy=181f+42f*s",
                 "AvatarKinematics.wristOnArc(","Math.PI*s"):
    assert expected in engine,expected
for expected in ("cameraScale,cameraScale,200f,364f",
                 "?.78f:.94f",
                 "interp(p.shoulder,p.elbow,.095f)",
                 "Side-profile torso", "shoulderFront"):
    assert expected in view,expected
assert "two eyes" not in details
assert "Only the eye facing the camera" in details
assert "c.drawOval(10f,-3f,13.4f,1.3f,paint)" in details
assert "path.quadTo(24f,3f,23f,6f)" in details
for filename in ("AvatarKinematicsTest.java","AvatarMotionEngineTest.java"):
    body=(BASE/"app/src/test/java/pe/asterionsystem/app"/filename).read_text()
    assert "extended" in body.lower() or "overhead" in body.lower()
print("OK: hombro posterior, perfil facial, extensión y recorrido completo.")
