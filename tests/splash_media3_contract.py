#!/usr/bin/env python3
"""Validate Media3 local MP4 splash and the tightly scoped HTML/CSS overlay."""
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "app/src/main"
JAVA = APP / "java/pe/asterionsystem/app/SplashActivity.java"
HTML = APP / "assets/asterion/splash_overlay.html"
WEBP = APP / "assets/asterion/asterion_splash_celestial.webp"


def main():
    gradle = (ROOT / "app/build.gradle").read_text(encoding="utf-8")
    for dependency in (
        "androidx.media3:media3-exoplayer:1.11.1",
        "androidx.media3:media3-ui:1.11.1",
    ):
        assert dependency in gradle, dependency

    source = JAVA.read_text(encoding="utf-8")
    for required in (
        "new ExoPlayer.Builder(this).build()",
        "MediaItem.fromUri(uri)",
        "Player.REPEAT_MODE_ONE",
        "setVolume(0f)",
        "setBlockNetworkLoads(true)",
        "setJavaScriptEnabled(false)",
        "file:///android_asset/asterion/splash_overlay.html",
        "R.drawable.asterion_loading_manana",
        "R.drawable.asterion_loading_tarde_noche",
        "handler.postDelayed(openApp, DISPLAY_MILLIS)",
        "private static final long DISPLAY_MILLIS = 1600L",
        "releaseVideoPlayer()",
        "player.release()",
        "onRenderedFirstFrame()",
        "videoPlayer.pause()",
    ):
        assert required in source, required

    xml = ET.parse(APP / "res/layout/activity_splash.xml").getroot()
    android = "{http://schemas.android.com/apk/res/android}"
    ids = {node.get(android + "id", ""): node for node in xml.iter()}
    assert "@+id/splash_artwork" in ids
    assert "@+id/splash_video" in ids
    assert "@+id/splash_web_overlay" in ids
    assert ids["@+id/splash_video"].tag == "androidx.media3.ui.PlayerView"
    assert ids["@+id/splash_web_overlay"].tag == "WebView"

    markup = HTML.read_text(encoding="utf-8").lower()
    assert "<html" in markup and "<style>" in markup
    for prohibited in ("<script", "http://", "https://", "onload=", "fetch(", "javascript:"):
        assert prohibited not in markup, prohibited
    assert WEBP.is_file(), "Falta el WebP Full HD versionado"
    data = WEBP.read_bytes()
    assert len(data) > 500000 and data[:4] == b"RIFF" and data[8:12] == b"WEBP"
    assert (ROOT / "scripts/prepare_splash_video.py").is_file()
    assert "android.permission.INTERNET" not in (APP / "AndroidManifest.xml").read_text()
    print("OK: Media3/MP4 + HTML local sin red, webp HD y PNG alternativos.")


if __name__ == "__main__":
    try:
        main()
    except (AssertionError, OSError, ET.ParseError) as e:
        print("ERROR: contrato de video y HTML:", e)
        sys.exit(1)
