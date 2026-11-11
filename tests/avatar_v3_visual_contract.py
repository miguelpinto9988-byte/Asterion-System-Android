#!/usr/bin/env python3
"""V3: each detailed piece is a Canvas drawable bound to current rig bones."""
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/pe/asterionsystem/app"
art=(JAVA/"AvatarArtDetails.java").read_text(encoding="utf8")
renderer=(JAVA/"AvatarRenderer.java").read_text(encoding="utf8")
view=(JAVA/"NativeAvatarView.java").read_text(encoding="utf8")
for token in ("drawDenimLeg(","drawPelvis(","drawJersey(",
              "drawSleeve(","drawForearm(","drawSneaker(",
              "drawHead(","drawHand(","Path()",
              "Paint.ANTI_ALIAS_FLAG","LinearGradient(",
              "c.drawRoundRect(","paint.setStrokeCap(Paint.Cap.ROUND)"):
    assert token in art,token
for token in ("details.drawDenimLeg(canvas,p.hip,p.knee,p.ankle,false)",
              "details.drawDenimLeg(canvas,p.farHip,p.farKnee,p.farAnkle,true)",
              "details.drawPelvis(","details.drawJersey(",
              "details.drawSneaker(","details.drawHead(",
              "details.drawSleeve(","details.drawForearm(",
              "details.drawHand("):
    assert token in renderer,token
assert "renderer.draw(canvas," in view
assert "private void drawShoe(" not in renderer
assert "private void drawHead(" not in renderer
assert "new Paint(" not in view and "new Path()" not in view
for token in ("MediaPlayer","UnityPlayer","HttpURLConnection","android.permission.INTERNET"):
    assert token not in art+renderer,token
print("OK: 8 tipos de piezas V3 con detalles anclados al rig Canvas.")
