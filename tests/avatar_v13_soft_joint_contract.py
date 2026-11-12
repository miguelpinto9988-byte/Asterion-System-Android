#!/usr/bin/env python3
"""V13: no disconnected joint bubbles, silent bad coordinates or floating contacts."""
from pathlib import Path
base=Path(__file__).resolve().parents[1]
src=base/"app/src/main/java/pe/asterionsystem/app"
read=lambda n:(src/n).read_text(encoding="utf8")
soft=read("AvatarSoftContour.java")
side=read("AvatarRenderer.java")
multi=read("AvatarMultiViewRenderer.java")
art=read("AvatarArtDetails.java")
view=read("NativeAvatarView.java")
support=read("AvatarFrameSafety.java")
for token in ("Path", "cubicTo(", "endWidth*.16f",
              "length*.09f", "Collapsed rendered bone",
              "Float.isFinite", "4096f", "hinge(", ".32f"):
    assert token in soft,token
assert "contours.segment(c,a,b,wa,wb,fill)" in side
assert "contours.segment(c,a,b,wa,wb,fill)" in multi
assert "contours.hinge(c,p,proximalWidth,distalWidth,fill)" in side
assert "contours.hinge(c,k," in multi
assert "19f,17f,palette.skin" not in side
assert "drawJoint(canvas,p.shoulder,12f,top)" not in side
assert "if(!palette.female)" not in side
assert "rr(c,-9f,-12f,9f,12f,5f" not in multi
for token in ("Single organic denim knee panel", "path.cubicTo(",
              "narrow hem follows the bone"):
    assert token in art,token
for token in ("SIDE_FOOT_Y", "FRONT_FOOT_Y", "TOP_FOOT_Y", "TOP_HAND_Y",
              "requireSide(", "requireCamera(", "Prone left palm floats",
              "Front left sole floats", "Side near foot floats"):
    assert token in support,token
for token in ("AvatarFrameSafety.requireSide(pose,contact)",
              "AvatarFrameSafety.requireCamera(pose,viewMode,contact)",
              "finally", "canvas.restore()",
              "!Float.isFinite(value)?0f"):
    assert token in view,token
unit=(base/"app/src/test/java/pe/asterionsystem/app/AvatarFrameSafetyTest.java").read_text()
assert unit.count("@Test")>=4
android=(base/"app/src/androidTest/java/pe/asterionsystem/app/NativeAvatarRenderTest.java").read_text()
for v in ("softHingesHaveNoProtrudingJointBalls",
          "continuousMotionRendersEveryViewWithoutCanvasLeaks",
          "getSaveCount()", "exportRealAndroidMultiviewFramesForReview"):
    assert v in android,v
assert android.count("@Test")>=15
for unwanted in ("android.permission.INTERNET", "HttpURLConnection",
                 "System.loadLibrary", "UnityPlayer"):
    assert unwanted not in soft+support+side+multi,unwanted
print("OK: contornos finitos compartidos, articulaciones suaves y apoyos verificados.")
