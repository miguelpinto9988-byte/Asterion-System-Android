#!/usr/bin/env python3
"""Validate splash resources and launcher wiring without artwork hashes or time locks."""
from pathlib import Path
import sys
import struct
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "app/src/main"
RES = BASE / "res"
ANDROID = "{http://schemas.android.com/apk/res/android}"

def main():
    backgrounds = list((RES / "drawable-nodpi").glob("asterion_loading_*.png"))
    gifs = list((RES / "raw").glob("asterion_splash_*.gif"))
    if not backgrounds and not gifs:
        raise AssertionError("Faltan recursos visuales de carga")
    for file in backgrounds:
        data = file.read_bytes()
        if len(data) < 24 or not data.startswith(bytes.fromhex("89504e470d0a1a0a")):
            raise AssertionError(f"PNG invalido: {file}")
        width, height = struct.unpack(">II", data[16:24])
        if min(width, height) < 256 or height <= width:
            raise AssertionError(f"Recurso de carga no vertical o muy pequeno: {file}")
    for file in gifs:
        data = file.read_bytes()
        if len(data) < 13 or data[:6] not in (b"GIF87a", b"GIF89a"):
            raise AssertionError(f"GIF invalido: {file}")
        width, height = struct.unpack("<HH", data[6:10])
        if min(width, height) < 256 or height <= width:
            raise AssertionError(f"Animacion no vertical o muy pequena: {file}")
    manifest = ET.parse(BASE / "AndroidManifest.xml").getroot()
    app = manifest.find("application")
    activities = {a.get(ANDROID + "name"): a for a in app.findall("activity")}
    launcher = activities.get(".SplashActivity")
    if launcher is None or launcher.get(ANDROID + "exported") != "true":
        raise AssertionError("SplashActivity debe ser el lanzador")
    actions = [n.get(ANDROID + "name") for n in launcher.findall("./intent-filter/action")]
    categories = [n.get(ANDROID + "name") for n in launcher.findall("./intent-filter/category")]
    if "android.intent.action.MAIN" not in actions or "android.intent.category.LAUNCHER" not in categories:
        raise AssertionError("Lanzador no declarado")
    source = (BASE / "java/pe/asterionsystem/app/SplashActivity.java").read_text()
    if "R.layout.activity_splash" not in source:
        raise AssertionError("Falta layout de inicio")
    ET.parse(RES / "layout/activity_splash.xml")
    print("OK: pantallas editables y recursos validos")

if __name__ == "__main__":
    try:
        main()
    except (AssertionError, OSError, ET.ParseError) as exc:
        print("ERROR:", exc)
        sys.exit(1)
