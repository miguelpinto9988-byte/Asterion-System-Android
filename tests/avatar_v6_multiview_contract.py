#!/usr/bin/env python3
"""Multi-view exercise cameras must retain independently authored rigs."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"app/src/main/java/pe/asterionsystem/app"
rd=lambda name:(src/name).read_text(encoding="utf8")
kin=rd("AvatarMultiViewKinematics.java")
render=rd("AvatarMultiViewRenderer.java")
view=rd("NativeAvatarView.java")
act=rd("NativeAvatarActivity.java")
for v in ("FRONT(", "TOP(", "SIDE(", "frontArms(", "diamondTop(",
          "leftShoulder", "rightShoulder", "leftWrist", "rightWrist",
          "AvatarRigConstraints.twoBone(", "assertArms("):
    assert v in kin,v
for v in ("drawFront(", "drawDiamondTop(", "drawHandsInset(",
          "drawPushupSideInset(", "drawFaceFront(", "drawTopArm("):
    assert v in render,v
assert "multiRenderer.draw(" in view
assert "viewMode==AvatarMultiViewKinematics.ViewMode.SIDE" in view
assert "getViewMode()" in view and "setViewMode(" in view
for v in ("scenePicker", "camera", "motionPicker.setEnabled", "views[position]"):
    assert v in act,v
assert "UnityPlayer" not in render and "HttpURLConnection" not in render
print("OK: cámaras lateral/frontal/superior, manos ampliadas y rigs aislados.")
