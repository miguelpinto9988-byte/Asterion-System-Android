#!/usr/bin/env python3
"""Option-D avatar architecture: keep drawing, rig and palette isolated."""
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
APP=ROOT/"app/src/main/java/pe/asterionsystem/app"
read=lambda name:(APP/name).read_text(encoding="utf8")
view=read("NativeAvatarView.java")
renderer=read("AvatarRenderer.java")
kin=read("AvatarKinematics.java")
spec=read("AvatarBodySpec.java")
pal=read("AvatarPalette.java")
pose=read("AvatarPose.java")
for token in ("renderer.draw(canvas,","setBodySpec(","AvatarPalette.BLUE",
              "AvatarPalette.PURPLE","postInvalidateOnAnimation()"):
    assert token in view,token
for token in ("drawTorso(", "drawLimb(", "drawJoint(", "path.cubicTo(",
              "Paint.ANTI_ALIAS_FLAG","Paint.Join.ROUND","torsoPoint(",
              "body.thighWidth","body.waistHalfWidth", "palette.garmentShade"):
    assert token in renderer,token
for token in ("public static Pose squat(float depth,AvatarBodySpec spec)",
              "kneeForward(", "elbowBelow(", "spec.upperArm","spec.lowerLeg"):
    assert token in kin,token
for token in ("float headScale","float chestHalfWidth","final class AvatarBodySpec",
              "Float.isFinite"):
    assert token in spec,token
assert "AvatarPalette BLUE=" in pal and "AvatarPalette PURPLE=" in pal
assert "class AvatarPose" in pose
assert "new Path()" not in view
assert "new Paint(" not in view
for prohibited in ("UnityPlayer","MediaPlayer","HttpURLConnection","import org.godotengine"):
    assert prohibited not in view+renderer+kin
print("OK: opción D con rig, proporciones, paleta y renderer desacoplados.")
