#!/usr/bin/env python3
"""Validate the complete, ID-matched GIF bundle staged for Android."""
import argparse
import hashlib
import json
import re
from pathlib import Path

ROOT=Path(__file__).resolve().parent.parent
DIR=ROOT/"app/src/main/assets/exercise_gifs/v10"
MANIFEST=DIR/"specs_es.json"
EXPECTED=2123
MAX_BYTES=800000

def check(require_all=False):
    manifest=json.loads(MANIFEST.read_text(encoding="utf-8"))
    permitted={x["id"]:x for x in manifest["ejercicios"]}
    if manifest.get("schema")!="asterion.exercise-motion.es.v2":
        raise ValueError("Esquema de fichas incorrecto")
    if manifest.get("total")!=EXPECTED or len(permitted)!=EXPECTED:
        raise ValueError("Inventario de variantes incorrecto")
    files=list(DIR.glob("*.gif"))
    if any(not re.fullmatch(r"[a-z0-9_-]+\.gif",f.name) for f in files):
        raise ValueError("Nombres de GIF inválidos")
    if any(f.stem not in permitted for f in files):
        raise ValueError("GIF sin ejercicio correspondiente")
    present={f.stem for f in files}
    missing=sorted(set(permitted)-present)
    hashes={}
    for file in files:
        if file.stat().st_size>MAX_BYTES:
            raise ValueError("GIF excede 800 KB: "+file.name)
        with file.open("rb") as handle:
            header=handle.read(10)
        if len(header)<10 or header[:6] not in (b"GIF89a",b"GIF87a"):
            raise ValueError("Archivo no GIF: "+file.name)
        w=int.from_bytes(header[6:8],"little")
        h=int.from_bytes(header[8:10],"little")
        if w<320 or h<400 or w>1024 or h>1365:
            raise ValueError("Dimensiones inadecuadas para móvil: "+file.name)
        if not .5<=w/h<=2.0:
            raise ValueError("Relación de aspecto inesperada: "+file.name)
        digest=hashlib.sha256(file.read_bytes()).hexdigest()
        if digest in hashes:
            raise ValueError("Dos variantes usan GIF idéntico: "+hashes[digest]+" y "+file.name)
        hashes[digest]=file.name
    if require_all and (missing or len(files)!=EXPECTED):
        sample=", ".join(missing[:8])
        raise ValueError(f"Faltan GIFs: {len(missing)}; {sample}")
    counts={"schema":"asterion.motion-gifs.validation.v2",
            "ejercicios":EXPECTED,"gifs_fisicos":len(files),
            "gifs_faltantes":len(missing),
            "paquete_completo":not missing and len(files)==EXPECTED and require_all,
            "gifs_unicos":True,"tecnica_aprobada_por_profesional":False}
    print(json.dumps(counts,ensure_ascii=False,indent=2))
    return counts

if __name__=="__main__":
    parser=argparse.ArgumentParser()
    parser.add_argument("--require-all",action="store_true")
    args=parser.parse_args()
    check(args.require_all)
