#!/usr/bin/env python3
"""Native 2D avatar: no video engine, no remote rendering, no home shortcuts."""
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
APP=ROOT/"app/src/main"
JAVA=APP/"java/pe/asterionsystem/app"
def read(name):return (JAVA/name).read_text(encoding="utf8")
anim=read("NativeAvatarView.java")
render=read("AvatarRenderer.java")
body=read("AvatarBodySpec.java")
palette=read("AvatarPalette.java")
pose_model=read("AvatarPose.java")
pose=read("AvatarKinematics.java")
activity=read("NativeAvatarActivity.java")
controller=read("AvatarPlaybackController.java")
routine=read("RoutinesActivity.java")
step=read("RoutineStepActivity.java")
manifest=(APP/"AndroidManifest.xml").read_text(encoding="utf8")
home=(APP/"res/layout/screen_home.xml").read_text(encoding="utf8")
for token in ("extends View","onDraw(Canvas canvas)","postInvalidateOnAnimation()",
              "renderer.draw(","AvatarMotionEngine.pose(","setFemale(","setPlaying(",
              "setPhase(","setSpeed("):
    assert token in anim,token
for token in ("new Paint(Paint.ANTI_ALIAS_FLAG)","new Path()",
              "path.cubicTo(","drawLimb(","drawJoint(","drawTorso("):
    assert token in render,token
for token in ("waistHalfWidth","chestHalfWidth","upperArm","lowerLeg"):
    assert token in body,token
assert "extends AvatarPose" in pose
assert "class AvatarPose" in pose_model
assert "AvatarPalette.BLUE" in anim and "AvatarPalette.PURPLE" in anim
for token in ("kneeForward(","UPPER_LEG","LOWER_LEG","TORSO",
              "depthAtPhase(","new Point(182f,364f)",
              "float raw=(float)Math.hypot(dx,dy)"):
    assert token in pose,token
for token in ("Chico azul","Chica morada","Pausar","Reiniciar",
              "Velocidad 1×","SeekBar","setOnSeekBarChangeListener"):
    assert token in activity,token
for token in ("playback.onResume()","playback.onPause()","playback.beginScrub()",
              "playback.endScrub()","playback.isPlaybackRequested()"):
    assert token in activity,token
assert "shouldPlay()" in controller
assert "updateAccessibilityDescription()" in anim
assert 'new Intent(this,NativeAvatarActivity.class)' in routine
assert 'new Intent(this,NativeAvatarActivity.class)' in step
assert '.putExtra("variant_id",current.id())' in step
assert "Ver comandos y avatar del ejercicio" in step
assert '<activity android:name=".NativeAvatarActivity" android:exported="false" />' in manifest
assert 'NativeAvatarActivity' not in home
# No third-party animation SDK, image/video API, permission or network.
for token in ("import org.godotengine", "import com.unity3d",
              "VideoView","MediaPlayer","HttpURLConnection",
              "WebView","FFmpeg","player.mp4"):
    assert token not in pose+anim+activity,token
assert "android.permission.INTERNET" not in manifest
test=(ROOT/"app/src/test/java/pe/asterionsystem/app/AvatarKinematicsTest.java").read_text()
assert "shinAndThighLengthsStayConstantForFullMovement" in test
assert "pelvisDescendsContinuouslyWhenSquatting" in test
print("OK: indigenous Java/Canvas 2D avatar, dynamic IK, both palettes, controls and accessible in-APK navigation")
