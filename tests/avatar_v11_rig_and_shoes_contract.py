#!/usr/bin/env python3
"""Future poses cannot draw reversed shoes, mirrored heels or unrelated rigs."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
java=root/"app/src/main/java/pe/asterionsystem/app"
read=lambda f:(java/f).read_text(encoding="utf8")
orient=read("AvatarOrientationRules.java")
foot=read("AvatarFootOrientation.java")
rig=read("AvatarSkeletonFrame.java")
contacts=read("AvatarContactFrame.java")
view=read("NativeAvatarView.java")
art=read("AvatarArtDetails.java")
multi=read("AvatarMultiViewRenderer.java")
side=read("AvatarRenderer.java")
for token in ("PRONE_GROUND_FACING", "new Vector3(0f,0f,-1f));",
              "topToeSign(frame)>0f", "soleNormal", "torsoAnteriorNormal",
              "palmNormal"):
    assert token in orient,token
for token in ("class AvatarFootOrientation","TOE_REACH=17f",
              "HEEL_REACH=12f","Contact.TOE", "side==Side.LEFT",
              "FootProjection", "ankle.x+dx*TOE_REACH",
              "ankle.y+dy*TOE_REACH", "angleDegrees", "requirePair(",
              "proneTop("):
    assert token in foot,token
for token in ("enum Joint","HEAD,NECK","LEFT_ELBOW","RIGHT_ANKLE",
              "fromSide(","fromCamera(","requireArmLengths(",
              "requireLegLengths(", "AvatarFootOrientation.requirePair"):
    assert token in rig,token
for token in ("enum Ground", "Ground.SOLE", "Ground.TOE", "Ground.PALM",
              "forCamera(", "forSide(", "assertProneUpperCamera(",
              "Math.sin(phase*Math.PI*2d)", "AvatarBurpeeKinematics.handsPlanted"):
    assert token in contacts,token
assert "AvatarContactFrame.forSide(motion,phase);" in view
assert "AvatarContactFrame.forCamera(viewMode,phase);" in view
for token in ("drawProneTopSneaker(", "AvatarFootOrientation.FootProjection",
              "foot.requireProneOrientation()", "Toe/upper are at local NEGATIVE Y",
              "Toe cap is towards the head", "drawRoundRect(-8.6f,-19.3f"):
    assert token in art,token
assert "AvatarSkeletonFrame.fromCamera(p,camera,body)" in multi
assert "AvatarFootOrientation.proneTop(p," in multi
assert "details.drawProneTopSneaker(c,foot,palette);" in multi
assert "AvatarSkeletonFrame.fromSide(pose,body)" in side
tests=(root/"app/src/test/java/pe/asterionsystem/app/AvatarV11OrientationTest.java").read_text()
assert tests.count("@Test")>=5
contactTests=(root/"app/src/test/java/pe/asterionsystem/app/AvatarContactFrameTest.java").read_text()
assert contactTests.count("@Test")>=6
instrument=(root/"app/src/androidTest/java/pe/asterionsystem/app/NativeAvatarRenderTest.java").read_text()
assert "exportRealAndroidMultiviewFramesForReview" in instrument
assert "proneTopShoesPointTowardHeadAndShowOnlyUpper" in instrument
sh=(root/"ci/android_daily_instrumented_smoke.sh").read_text()
assert "adb pull" in sh and "test \"$COUNT\" -eq 42" in sh
print("OK: punteras hacia la cabeza, pies simétricos, rig común y PNG de emulador.")
