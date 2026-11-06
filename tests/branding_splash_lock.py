#!/usr/bin/env python3
"""Lock Asterion's two permanent 1080x1920 splash images and local-time schedule."""
import hashlib
from pathlib import Path
import re
import struct
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "app/src/main"
ANDROID = "{http://schemas.android.com/apk/res/android}"
ASSETS = {
    "asterion_loading_manana.png": "67c495b89b0acd631e1a64c113c6803df31e488cef559237b936b1ed6a88ff27",
    "asterion_loading_tarde_noche.png": "d1af6660b3c1afb9db0e6b5bdca2d7c4e457e302e1f363f3d03226c3461b3cba",
}


def main() -> int:
    for name, expected_sha in ASSETS.items():
        path = BASE / "res/drawable-nodpi" / name
        if not path.is_file():
            raise AssertionError(f"Falta la pantalla permanente: {path}")
        data = path.read_bytes()
        actual = hashlib.sha256(data).hexdigest()
        if actual != expected_sha:
            raise AssertionError(f"Pantalla oficial modificada: {name}; SHA256 {actual}")
        if not data.startswith(b"\x89PNG\r\n\x1a\n"):
            raise AssertionError(f"Archivo no es PNG: {name}")
        width, height = struct.unpack(">II", data[16:24])
        if (width, height) != (1080, 1920):
            raise AssertionError(f"Resolución alterada: {name} {width}x{height}")
    manifest = ET.parse(BASE / "AndroidManifest.xml").getroot()
    app = manifest.find("application")
    activities = {a.get(ANDROID + "name"): a for a in app.findall("activity")}
    launcher = activities.get(".SplashActivity")
    main_activity = activities.get(".MainActivity")
    if launcher is None or launcher.get(ANDROID + "exported") != "true":
        raise AssertionError("SplashActivity debe ser el lanzador exportado")
    if main_activity is None or main_activity.get(ANDROID + "exported") != "false":
        raise AssertionError("MainActivity debe mantenerse interna")
    actions = [n.get(ANDROID + "name") for n in launcher.findall("./intent-filter/action")]
    categories = [n.get(ANDROID + "name") for n in launcher.findall("./intent-filter/category")]
    if "android.intent.action.MAIN" not in actions or "android.intent.category.LAUNCHER" not in categories:
        raise AssertionError("Lanzador no apunta a SplashActivity")
    selector = (BASE / "java/pe/asterionsystem/app/SplashTimeWindow.java").read_text()
    if not re.search(r"return\s+hourOfDay\s*<\s*12\s*;", selector):
        raise AssertionError("Cambio no autorizado del límite de mediodía")
    activity = (BASE / "java/pe/asterionsystem/app/SplashActivity.java").read_text()
    for expected in (
        "Calendar.getInstance().get(Calendar.HOUR_OF_DAY)",
        "SplashTimeWindow.isMorning(",
        "R.drawable.asterion_loading_manana",
        "R.drawable.asterion_loading_tarde_noche",
        "R.layout.activity_splash",
    ):
        if expected not in activity:
            raise AssertionError("Contrato de arranque modificado: " + expected)
    layout = (BASE / "res/layout/activity_splash.xml").read_text()
    if 'android:scaleType="fitCenter"' not in layout:
        raise AssertionError("Se perdió la presentación sin recortes")
    print("OK: ambas pantallas Full HD y horario 00-12/12-24 protegidos.")
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (AssertionError, OSError, ET.ParseError) as exc:
        print("ERROR: bloqueo de identidad visual:", exc)
        sys.exit(1)
