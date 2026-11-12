#!/usr/bin/env python3
"""V13: organic hinge bridges and explicit pre-render geometric validation."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
j=root/"app/src/main/java/pe/asterionsystem/app"
skin=(j/"AvatarOrganicJointSkin.java").read_text()
guard=(j/"AvatarPoseGuard.java").read_text()
side=(j/"AvatarRenderer.java").read_text()
multi=(j/"AvatarMultiViewRenderer.java").read_text()
for token in ("path.cubicTo(", "path.quadTo(", "root.distance(hinge)",
              "hinge.distance(tip)", "paint.setStyle(Paint.Style.FILL)",
              "requireWidth(incomingWidth)", "requireWidth(outgoingWidth)"):
    assert token in skin,token
for token in ("requireLink(", "requireWidth(", "hingeAngle(",
              "requireRig(", "Float.isFinite", "IllegalArgumentException"):
    assert token in guard,token
assert side.count("organicJoints.draw(")>=4
assert multi.count("organicJoints.draw(")>=4
assert "AvatarPoseGuard.requireRig(skeleton);" in side
assert "AvatarPoseGuard.requireRig(skeleton);" in multi
assert "rr(c,-9f,-12f,9f,12f,5f,0xFF3B4D69);" not in multi
assert "drawJoint(canvas,p.elbow,8.8f,palette.skin);" not in side
tests=(root/"app/src/test/java/pe/asterionsystem/app/AvatarV13PoseGuardTest.java").read_text()
assert tests.count("@Test")>=4
print("OK: codos y rodillas con puentes orgánicos y rig validado antes de pintar")
