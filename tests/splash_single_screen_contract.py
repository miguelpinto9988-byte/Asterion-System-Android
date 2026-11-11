#!/usr/bin/env python3
"""Regression: never display the old PNG and celestial video sequentially on a normal launch."""
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app/src/main/res"
JAVA = ROOT / "app/src/main/java/pe/asterionsystem/app/SplashActivity.java"
NS = "{http://schemas.android.com/apk/res/android}"

def check():
    source = JAVA.read_text(encoding="utf-8")
    on_create = source.split("protected void onCreate(", 1)[1].split(
        "private int optionalRawResource(", 1)[0]
    original = source.split("private void showOriginalFallback(", 1)[1].split(
        "@Override", 1
    )[0]
    first_frame = source.split("public void onRenderedFirstFrame()", 1)[1].split(
        "public void onPlayerError(", 1
    )[0]
    html = source.split("private void initializeHtmlOverlay()", 1)[1].split(
        "private void releaseVideoPlayer()", 1
    )[0]
    assert "staticArtwork.setImageResource" not in on_create, (
        "No mostrar el PNG historico mientras se inicia el MP4"
    )
    assert "staticArtwork.setVisibility(View.INVISIBLE);" in on_create
    assert "staticArtwork.setImageResource" in original, (
        "Mantener el PNG solo como recurso alternativo ante errores"
    )
    assert "showVideoAndLocalHtml();" in first_frame, (
        "Solo revelar el video al renderizar su primer fotograma"
    )
    assert "onPlaybackStateChanged(" not in source, (
        "STATE_READY no garantiza el primer fotograma visible"
    )
    assert "videoView.setAlpha(0f);" in source
    assert "videoView.setAlpha(1f);" in source
    assert "htmlOverlay.setVisibility(View.GONE);" in html
    assert "public void onPageFinished(" in html
    assert re.search(r"onPageFinished\([^)]*\).*?view.setVisibility\(View.VISIBLE\)", html, re.S)

    layout = ET.parse(RES / "layout/activity_splash.xml").getroot()
    views = {node.get(NS + "id"): node for node in layout.iter()}
    assert views["@+id/splash_video"].get(NS + "visibility") == "gone"
    assert views["@+id/splash_web_overlay"].get(NS + "visibility") == "gone"

    # Android 12+ has an unavoidable platform start window. Our system window
    # contains no second Asterion illustration and uses a blank vector icon.
    theme = (RES / "values-v31/styles.xml").read_text(encoding="utf-8")
    assert "@drawable/asterion_splash_blank_icon" in theme
    assert "@color/asterion_background" in theme
    print("OK: una sola ilustracion de carga; PNG solo ante fallo; web transparente.")

if __name__ == "__main__":
    try:
        check()
    except (AssertionError, OSError, ValueError, ET.ParseError) as error:
        print("ERROR: pantalla de carga duplicada:", error)
        sys.exit(1)
