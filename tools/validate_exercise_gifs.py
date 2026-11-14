#!/usr/bin/env python3
"""Validate only GIFs physically present, never claim the 2104 missing exist."""
import argparse
import hashlib
import json
import re
from pathlib import Path

ROOT=Path(__file__).resolve().parent.parent
DIR=ROOT/"app/src/main/assets/exercise_gifs/v1"
MANIFEST=DIR/"specs_es.json"
MAX_BYTES=800000

def check(require_all=False):
    manifest=json.loads(MANIFEST.read_text(encoding="utf-8"))
    permitted={x["id"]:x for x in manifest["ejercicios"]}
    if len(permitted)!=2104:raise ValueError("Inventario incorrecto")
    files=list(DIR.glob("*.gif"))
    if any(not re.fullmatch(r"[a-z0-9_-]+\.gif",f.name) for f in files):
        raise ValueError("Nombres de GIF inválidos")
    if any(f.stem not in permitted for f in files):
        raise ValueError("GIF sin ejercicio correspondiente")
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
        if abs(w/h-.75)>.12:
            raise ValueError("Relación de aspecto inesperada: "+file.name)
        digest=hashlib.sha256(file.read_bytes()).hexdigest()
        if digest in hashes:
            raise ValueError("Dos variantes usan GIF idéntico: "+hashes[digest]+" y "+file.name)
        hashes[digest]=file.name
    if require_all and len(files)!=2104:
        raise ValueError("No se pueden declarar listos los GIFs ausentes")
    counts={"schema":"asterion.motion-gifs.validation.v1",
            "ejercicios":2104,"gifs_fisicos":len(files),
            "gifs_faltantes":2104-len(files),
            "publicacion_completa":len(files)==2104 and require_all,
            "gifs_unicos":True,"tecnica_aprobada_por_profesional":False}
    print(json.dumps(counts,ensure_ascii=False,indent=2))
    return counts

if __name__=="__main__":
    parser=argparse.ArgumentParser()
    parser.add_argument("--require-all",action="store_true")
    args=parser.parse_args()
    check(args.require_all)
