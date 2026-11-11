#!/usr/bin/env python3
"""Prevent black letterbox bars on phone screens taller than the video artwork."""
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app/src/main/res"
JAVA = ROOT / "app/src/main/java/pe/asterionsystem/app/SplashActivity.java"
ANDROID = "{http://schemas.android.com/apk/res/android}"
APP = "{http://schemas.android.com/apk/res-auto}"

def check():
    root = ET.parse(RES / "layout/activity_splash.xml").getroot()
    nodes = {node.get(ANDROID + "id"): node for node in root.iter()}
    video = nodes["@+id/splash_video"]
    fallback = nodes["@+id/splash_artwork"]
    html = nodes["@+id/splash_web_overlay"]
    assert video.get(APP + "resize_mode") == "zoom", "MP4 debe llenar el area sin franjas (zoom)"
    assert video.get(APP + "surface_type") == "texture_view"
    assert fallback.get(ANDROID + "scaleType") == "centerCrop", "PNG debe llenar la pantalla"
    for view in (video, fallback, html):
        assert view.get(ANDROID + "layout_width") == "match_parent"
        assert view.get(ANDROID + "layout_height") == "match_parent"

    for qualifier in ("values-v28", "values-v31"):
        styles = ET.parse(RES / qualifier / "styles.xml").getroot()
        splash = next(s for s in styles.findall("style") if s.get("name") == "Theme.AsterionSplash")
        items = {i.get("name"): (i.text or "").strip() for i in splash.findall("item")}
        assert items["android:windowLayoutInDisplayCutoutMode"] == "shortEdges", qualifier

    code = JAVA.read_text(encoding="utf-8")
    for required in (
        "configureFullBleedWindow();",
        "getWindow().setDecorFitsSystemWindows(false)",
        "WindowInsets.Type.systemBars()",
        "View.SYSTEM_UI_FLAG_HIDE_NAVIGATION",
        "View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY",
    ):
        assert required in code, required

    print("OK: fullscreen sin franjas negras (zoom, centerCrop, cutouts y barras ocultas).")

if __name__ == "__main__":
    try:
        check()
    except (AssertionError, OSError, ValueError, ET.ParseError, StopIteration) as error:
        print("ERROR: pantalla de carga con posibles franjas negras:", error)
        sys.exit(1)
