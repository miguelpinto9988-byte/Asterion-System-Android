#!/usr/bin/env python3
"""Native 2D avatar: no video engine, no remote rendering, no home shortcuts."""
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
APP=ROOT/"app/src/main"
JAVA=APP/"java/pe/asterionsystem/app"
def read(name):return (JAVA/name).read_text(encoding="utf8")
anim=read("NativeAvatarView.java")
pose=read("AvatarKinematics.java")
activity=read("NativeAvatarActivity.java")
routine=read("RoutinesActivity.java")
step=read("RoutineStepActivity.java")
manifest=(APP/"AndroidManifest.xml").read_text(encoding="utf8")
home=(APP/"res/layout/screen_home.xml").read_text(encoding="utf8")
for token in ("extends View","onDraw(Canvas canvas)","postInvalidateOnAnimation()",
              "Path()","path.cubicTo","drawLimb(","drawJoint(",
              "AvatarKinematics.squat(","setFemale(","setPlaying(",
              "setPhase(","setSpeed("):
    assert token in anim,token
for token in ("kneeForward(","UPPER_LEG","LOWER_LEG","TORSO",
              "depthAtPhase(","new Point(182f,364f)",
              "float raw=(float)Math.hypot(dx,dy)"):
    assert token in pose,token
for token in ("Chico azul","Chica morada","Pausar","Reiniciar",
              "Velocidad 1×","SeekBar","setOnSeekBarChangeListener"):
    assert token in activity,token
assert 'new Intent(this,NativeAvatarActivity.class)' in routine
assert 'new Intent(this,NativeAvatarActivity.class)' in step
assert "contains(\"sentadilla\")" in step
assert '<activity android:name=".NativeAvatarActivity" android:exported="false" />' in manifest
assert 'NativeAvatarActivity' not in home
# No third-party animation SDK, image/video API, permission or network.
for token in ("Godot","Unity","VideoView","MediaPlayer","HttpURLConnection",
              "WebView","FFmpeg","mp4"):
    assert token not in pose+anim+activity,token
assert "android.permission.INTERNET" not in manifest
test=(ROOT/"app/src/test/java/pe/asterionsystem/app/AvatarKinematicsTest.java").read_text()
assert "shinAndThighLengthsStayConstantForFullMovement" in test
assert "pelvisDescendsContinuouslyWhenSquatting" in test
print("OK: indigenous Java/Canvas 2D avatar, dynamic IK, both palettes, controls and accessible in-APK navigation")
