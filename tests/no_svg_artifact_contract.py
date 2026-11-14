#!/usr/bin/env python3
"""Reject SVG files from Asterion source and distributable Android APKs."""
from pathlib import Path
from zipfile import ZipFile
import argparse
import json
import sys

ROOT=Path(__file__).resolve().parents[1]

def check_tree():
    candidates=[p for p in ROOT.rglob("*")
                if p.is_file() and p.suffix.lower()==".svg"
                and ".git" not in p.parts and "build" not in p.parts]
    if candidates:
        raise AssertionError("Archivos SVG detectados en GitHub: "
                             + ", ".join(str(p.relative_to(ROOT)) for p in candidates))

def check_apk(path,require_exercise_gifs=False):
    path=Path(path)
    if not path.is_file():
        raise AssertionError("No existe APK: "+str(path))
    with ZipFile(path) as archive:
        files=archive.namelist()
        svg=[entry for entry in files if entry.lower().endswith(".svg")]
        if svg:
            raise AssertionError("SVG detectados en APK: "+", ".join(svg))
        if require_exercise_gifs:
            manifest_path=ROOT/"app/src/main/assets/exercise_gifs/v10/specs_es.json"
            manifest=json.loads(manifest_path.read_text(encoding="utf-8"))
            expected={"assets/"+row["gif_ruta"] for row in manifest["ejercicios"]}
            actual=[entry for entry in files
                    if entry.startswith("assets/exercise_gifs/v10/")
                    and entry.endswith(".gif")]
            actual_set=set(actual)
            if manifest.get("total")!=2123 or len(expected)!=2123:
                raise AssertionError("El manifiesto de la APK no enumera 2123 IDs")
            if len(actual)!=2123 or actual_set!=expected:
                missing=sorted(expected-actual_set)
                extra=sorted(actual_set-expected)
                raise AssertionError(
                    f"GIFs en APK: {len(actual)}; esperados 2123; "
                    f"faltantes={missing[:5]}, extras={extra[:5]}")
        corrupt=archive.testzip()
        if corrupt:
            raise AssertionError("Entrada corrupta en APK: "+corrupt)

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument("--apk", action="append",default=[],
                        help="Ruta de APK a validar (repetible)")
    parser.add_argument("--require-exercise-gifs",action="store_true",
                        help="Exige los 2123 GIFs exactos enumerados en el manifiesto")
    args=parser.parse_args()
    if args.require_exercise_gifs and not args.apk:
        parser.error("--require-exercise-gifs requiere al menos un --apk")
    check_tree()
    for apk in args.apk:
        check_apk(apk,args.require_exercise_gifs)
    print("OK: Sin SVG en repositorio"
          + (" ni en "+str(len(args.apk))+" APK" if args.apk else "")
          + ("; 2123 GIFs por ID en APK." if args.require_exercise_gifs else ".")
          )

if __name__=="__main__":
    try: main()
    except (AssertionError,OSError,Exception) as exc:
        print("ERROR: "+str(exc),file=sys.stderr)
        sys.exit(1)
