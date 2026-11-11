#!/usr/bin/env python3
"""V8: closed movement cycles and camera-specific authoring remain wired in CI."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
java=root/"app/src/main/java/pe/asterionsystem/app"
src=lambda name:(java/name).read_text(encoding="utf8")
rig=src("AvatarMultiViewKinematics.java")
multi=src("AvatarMultiViewRenderer.java")
burpee=src("AvatarBurpeeKinematics.java")
motion=src("AvatarMotionEngine.java")
view=src("NativeAvatarView.java")
for token in ("FRONT_JACKS(", "FRONT_ELBOW(", "frontJumpingJack(",
              "frontElbowFlex(", "frontWideSquat(", "49f*d",
              "at(174f,244f+shift)", "at(127f,428f)"):
    assert token in rig,token
for token in ("drawFront(c,p,squat||jacks)", "FRONTAL · SALTOS DE TIJERA",
              "FRONTAL · FLEXIÓN DE CODOS", "drawTopArm("):
    assert token in multi,token
for token in ("class AvatarBurpeeKinematics","AvatarRigConstraints.twoBone(",
              "handsPlanted(", "feetTravelling(", "305f,364f"):
    assert token in burpee,token
for token in ("BURPEE(", "AvatarBurpeeKinematics.pose("):
    assert token in motion,token
for token in ("ViewMode.FRONT_JACKS", "ViewMode.FRONT_ELBOW", "LOOP_MS*1.6f"):
    assert token in view,token
tests=(root/"app/src/test/java/pe/asterionsystem/app/AvatarMultiViewKinematicsTest.java").read_text()
for token in ("wideSquatBeginsAndEndsFullyExtendedWithFixedShoes",
              "jumpingJacksOpenAndCloseWithIKAndVisibleFlight",
              "visibleElbowFlexionChangesReachWithoutStretch"):
    assert token in tests,token
assert "UnityPlayer" not in multi+burpee and "HttpURLConnection" not in multi+burpee
print("OK: sentadilla completa, tijeras, codos articulados y burpee sin motor externo.")
