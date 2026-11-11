#!/usr/bin/env python3
"""V4: complete authored preset coverage and non-stretch joint architecture."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
base=r/"app/src/main/java/pe/asterionsystem/app"
read=lambda f:(base/f).read_text(encoding="utf8")
view=read("NativeAvatarView.java")
engine=read("AvatarMotionEngine.java")
rig=read("AvatarRigConstraints.java")
kin=read("AvatarKinematics.java")
act=read("NativeAvatarActivity.java")
tests=(r/"app/src/test/java/pe/asterionsystem/app/AvatarRigConstraintsTest.java").read_text()
for token in ("class AvatarRigConstraints","twoBone(","fabrik(","targetLimited",
              "maxIterations","rootForPinnedEnd(","requireFixedLength("):
    assert token in rig,token
for token in ("Motion.SQUAT","Motion.ARM_RAISE","Motion.MARCH","Motion.LUNGE",
              "Motion.HIP_HINGE","assertSkeleton(","targetLimited"):
    assert token in engine,token
assert "AvatarRigConstraints.twoBone(" in kin
for token in ("setMotion(","AvatarMotionEngine.pose(","getMotion()"):
    assert token in view,token
for token in ("Spinner","setOnItemSelectedListener","state.putString(\"motion\""):
    assert token in act,token
for token in ("noStretchForUnreachableTarget","fabrikMultiBoneChainsAlwaysKeepLengths",
              "plantedContactIsExactWithRootProjection"):
    assert token in tests or token in (
       r/"app/src/test/java/pe/asterionsystem/app/AvatarMotionEngineTest.java"
    ).read_text(),token
for token in ("com.unity3d","org.godotengine","HttpURLConnection","android.permission.INTERNET"):
    assert token not in view+engine+rig,token
print("OK: IK de dos huesos, FABRIK y 5 movimientos con objetivos y huesos fijos.")
