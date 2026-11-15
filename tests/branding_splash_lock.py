#!/usr/bin/env python3
"""Guard the approved 2160 x 4800 full-bleed WebP splash art and time schedule."""
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
    "asterion_loading_manana_4k.webp": "2124c87d5bfc9d793fae5a9e558b91eb34884fbbb3c9d5df36667617f0f4b434",
    "asterion_loading_tarde_noche_4k.webp": "dc1c38e2d6699c0a7c18506e1c2dc1b382be1796166f4f858f91e246e19ac494",
}


def webp_dimensions(data: bytes):
    """Read the dimensions of a simple lossy WebP/VP8 image."""
    if data[:4] != b"RIFF" or data[8:12] != b"WEBP" or data[12:16] != b"VP8 ":
        raise AssertionError("Recurso no es WebP/VP8")
    if data[23:26] != b"\x9d\x01\x2a":
        raise AssertionError("Cabecera VP8 de imagen no reconocida")
    return (struct.unpack("<H", data[26:28])[0] & 0x3FFF,
            struct.unpack("<H", data[28:30])[0] & 0x3FFF)


def main() -> int:
    for name, expected_sha in ASSETS.items():
        path = BASE / "res/drawable-nodpi" / name
        data = path.read_bytes()
        actual = hashlib.sha256(data).hexdigest()
        if actual != expected_sha:
            raise AssertionError(f"Pantalla de carga alterada: {name}, SHA256={actual}")
        if webp_dimensions(data) != (2160, 4800):
            raise AssertionError(f"Resolución incorrecta de {name}: {webp_dimensions(data)}")

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
        raise AssertionError("El lanzador debe apuntar a SplashActivity")

    selector = (BASE / "java/pe/asterionsystem/app/SplashTimeWindow.java").read_text()
    if not re.search(r"return\s+hourOfDay\s*<\s*12\s*;", selector):
        raise AssertionError("Cambió el límite de mediodía")
    activity = (BASE / "java/pe/asterionsystem/app/SplashActivity.java").read_text()
    for required in (
        "Calendar.getInstance().get(Calendar.HOUR_OF_DAY)",
        "SplashTimeWindow.isMorning(",
        "R.drawable.asterion_loading_manana_4k",
        "R.drawable.asterion_loading_tarde_noche_4k",
        "R.layout.activity_splash",
        "WindowInsets.Type.systemBars()",
    ):
        if required not in activity:
            raise AssertionError("Contrato de arranque alterado: " + required)
    layout = (BASE / "res/layout/activity_splash.xml").read_text()
    if 'android:scaleType="centerCrop"' not in layout:
        raise AssertionError("La ilustración debe cubrir la pantalla sin márgenes")
    if 'android:layout_width="match_parent"' not in layout or 'android:layout_height="match_parent"' not in layout:
        raise AssertionError("La pantalla no ocupa toda la superficie")
    print("OK: pantallas 4K sin márgenes y horario local 00-12 / 12-24 protegidos")
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (AssertionError, OSError, ET.ParseError) as exc:
        print("ERROR: contrato de pantalla de carga:", exc)
        sys.exit(1)
