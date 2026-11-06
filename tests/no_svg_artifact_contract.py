#!/usr/bin/env python3
"""Reject SVG files from Asterion source and distributable Android APKs."""
from pathlib import Path
from zipfile import ZipFile
import argparse
import sys

ROOT=Path(__file__).resolve().parents[1]

def check_tree():
    candidates=[p for p in ROOT.rglob("*")
                if p.is_file() and p.suffix.lower()==".svg"
                and ".git" not in p.parts and "build" not in p.parts]
    if candidates:
        raise AssertionError("Archivos SVG detectados en GitHub: "
                             + ", ".join(str(p.relative_to(ROOT)) for p in candidates))

def check_apk(path):
    path=Path(path)
    if not path.is_file():
        raise AssertionError("No existe APK: "+str(path))
    with ZipFile(path) as archive:
        files=archive.namelist()
        svg=[entry for entry in files if entry.lower().endswith(".svg")]
        if svg:
            raise AssertionError("SVG detectados en APK: "+", ".join(svg))
        corrupt=archive.testzip()
        if corrupt:
            raise AssertionError("Entrada corrupta en APK: "+corrupt)

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument("--apk", action="append",default=[],
                        help="Ruta de APK a validar (repetible)")
    args=parser.parse_args()
    check_tree()
    for apk in args.apk:
        check_apk(apk)
    print("OK: Sin SVG en repositorio"
          + (" ni en "+str(len(args.apk))+" APK" if args.apk else "")+".")

if __name__=="__main__":
    try: main()
    except (AssertionError,OSError,Exception) as exc:
        print("ERROR: "+str(exc),file=sys.stderr)
        sys.exit(1)
