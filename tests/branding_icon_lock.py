#!/usr/bin/env python3
"""Check icon resources without fixing their artwork to specific hashes."""
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app/src/main/res"
ANDROID = "{http://schemas.android.com/apk/res/android}"

def check_drawable(reference):
    if not reference or not reference.startswith("@drawable/"):
        raise AssertionError("El icono debe usar un recurso @drawable")
    name = reference.split("/", 1)[1]
    for folder in ("drawable", "drawable-nodpi", "mipmap"):
        for extension in (".png", ".webp", ".jpg", ".jpeg", ".xml"):
            file = RES / folder / (name + extension)
            if not file.is_file():
                continue
            data = file.read_bytes()
            if not data:
                raise AssertionError(f"Icono vacio: {file}")
            if extension == ".png" and not data.startswith(bytes.fromhex("89504e470d0a1a0a")):
                raise AssertionError(f"PNG invalido: {file}")
            if extension == ".webp" and not (data[:4] == b"RIFF" and data[8:12] == b"WEBP"):
                raise AssertionError(f"WebP invalido: {file}")
            if extension in (".jpg", ".jpeg") and not data.startswith(bytes.fromhex("ffd8")):
                raise AssertionError(f"JPEG invalido: {file}")
            if extension == ".xml":
                ET.parse(file)
            return
    raise AssertionError(f"Icono no encontrado: {reference}")

def main():
    manifest = ET.parse(ROOT / "app/src/main/AndroidManifest.xml").getroot()
    app = manifest.find("application")
    if app is None:
        raise AssertionError("Falta application en el manifiesto")
    check_drawable(app.get(ANDROID + "icon"))
    check_drawable(app.get(ANDROID + "roundIcon"))
    print("OK: icono editable y recursos validos")

if __name__ == "__main__":
    try:
        main()
    except (AssertionError, OSError, ET.ParseError) as exc:
        print("ERROR:", exc)
        sys.exit(1)
