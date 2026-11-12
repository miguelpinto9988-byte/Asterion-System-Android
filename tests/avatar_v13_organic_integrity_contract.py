#!/usr/bin/env python3
"""V13: every view uses continuous two-bone contours and checks contacts."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
java=root/"app/src/main/java/pe/asterionsystem/app"
read=lambda n:(java/n).read_text(encoding="utf8")
skin=read("AvatarOrganicVolume.java")
guard=read("AvatarFrameSafety.java")
side=read("AvatarRenderer.java")
multi=read("AvatarMultiViewRenderer.java")
art=read("AvatarArtDetails.java")
view=read("NativeAvatarView.java")
for token in ("class AvatarOrganicVolume","JointMetrics","public void drawChain(",
              "measure(","bridgeRadius","contour.cubicTo(","paint.setAlpha(255)",
              "A bone cannot collapse","Unsupported limb envelope width"):
    assert token in skin,token
for token in ("class AvatarFrameSafety","public static void side(",
              "public static void camera(","AvatarSkeletonFrame.fromSide(",
              "AvatarSkeletonFrame.fromCamera(","requireGround(",
              "requireY(", "Ground.PALM","contact.airborne"):
    assert token in guard,token
assert "organic.drawChain(canvas,p.hip,p.knee,p.ankle" in side
assert "organic.drawChain(canvas,p.farHip,p.farKnee,p.farAnkle" in side
assert "organic.drawChain(canvas," in side
assert "drawJoint(canvas,p.knee" not in side
assert "drawJoint(canvas,p.elbow" not in side
assert "drawJoint(canvas,p.farKnee" not in side
assert "drawJoint(canvas,p.farElbow" not in side
assert "organic.drawChain(c,hip,knee,ankle" in multi
assert "organic.drawChain(c,h,k,a" in multi
assert "organic.drawChain(c,sleeve,elbow,wrist" in multi
assert "rr(c,-9f,-12f,9f,12f" not in multi
assert "c.drawRoundRect(-9f,-11f,12f,11f" not in art
assert "AvatarFrameSafety.side(sidePose,contact,bodySpec)" in view
assert "AvatarFrameSafety.camera(pose,viewMode,contact,bodySpec)" in view
unit=root/"app/src/test/java/pe/asterionsystem/app"
assert (unit/"AvatarOrganicVolumeTest.java").read_text().count("@Test")>=3
assert (unit/"AvatarFrameSafetyTest.java").read_text().count("@Test")>=4
instrument=(root/"app/src/androidTest/java/pe/asterionsystem/app/NativeAvatarRenderTest.java").read_text()
assert "elbowsAndKneesHaveContinuousOrganicPixelCoverage" in instrument
for forbidden in ("UnityPlayer", "HttpURLConnection", "MediaPlayer",
                  "System.loadLibrary", "android.permission.INTERNET"):
    assert forbidden not in skin+guard+multi+side,forbidden
print("OK: volúmenes orgánicos sin tapones, vínculos fijos y contactos validados.")
