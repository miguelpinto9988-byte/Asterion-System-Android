#!/usr/bin/env python3
"""Prevent regression from rear-heel-only shoes to laced front shoes in TOP camera."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
java=root/"app/src/main/java/pe/asterionsystem/app"
art=(java/"AvatarArtDetails.java").read_text(encoding="utf8")
foot=(java/"AvatarFootOrientation.java").read_text(encoding="utf8")
multi=(java/"AvatarMultiViewRenderer.java").read_text(encoding="utf8")
start=art.index("public void drawProneTopSneaker(")
end=art.index("public void drawProjectedHand(",start)
rear=art[start:end]
for token in ("REAR HEEL camera asset", "FootProjection foot",
              "foot.requireProneOrientation()", "heel cup",
              "rear quarters", "Padded heel collar",
              "vertical backstay", "rear pull-tab", "c.rotate(foot.angleDegrees)"):
    assert token in rear,token
for outdated in ("for(int i=0;i<3;i++)", "Small lace/ankle highlight",
                 "Tongue and upward-facing", "Toe cap is towards the head",
                 "drawRoundRect(-3.8f,-12f,3.8f,6f"):
    assert outdated not in rear,outdated
assert "proneTop(p," in multi
assert "drawProneTopSneaker(c,foot,palette)" in multi
assert "ankle.y+dy*TOE_REACH" in foot
tests=(root/"app/src/androidTest/java/pe/asterionsystem/app/NativeAvatarRenderTest.java").read_text(encoding="utf8")
assert "proneTopShoesUseRearHeelCounterWithoutLaceBands" in tests
assert "exportRealAndroidMultiviewFramesForReview" in tests
print("OK: heel counter/backstay without laces, with anatomical toe axis retained.")
