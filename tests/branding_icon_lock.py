#!/usr/bin/env python3
"""Falla si el icono oficial inmutable de Asterion System cambia."""
import hashlib
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
ICON = ROOT / "app/src/main/res/drawable-nodpi/asterion_icon_oficial.webp"
MANIFEST = ROOT / "app/src/main/AndroidManifest.xml"
EXPECTED = "5852d7185e4676b9301761ec4a886fdebc88615331c01c837e34dc6c125dd3f3"
ICON_RESOURCE = "@drawable/asterion_icon_oficial"


def main() -> int:
    if not ICON.is_file():
        print("ERROR: falta el icono oficial inmutable:", ICON)
        return 1
    actual = hashlib.sha256(ICON.read_bytes()).hexdigest()
    if actual != EXPECTED:
        print("ERROR: se ha alterado el icono oficial de Asterion System.")
        print("SHA-256 esperado:", EXPECTED)
        print("SHA-256 actual:  ", actual)
        return 1
    manifest = MANIFEST.read_text(encoding="utf-8")
    for attr in ("android:icon", "android:roundIcon"):
        expected_attr = f'{attr}="{ICON_RESOURCE}"'
        if expected_attr not in manifest:
            print("ERROR: el manifiesto no utiliza el icono oficial:", expected_attr)
            return 1
    print("OK: icono oficial inmutable verificado; SHA-256:", actual)
    return 0


if __name__ == "__main__":
    sys.exit(main())
