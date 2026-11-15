#!/usr/bin/env python3
"""Import five approved chibi WebP companions safely, with SHA256 checks.

Usage: python3 scripts/import_asterion_companions.py Asterion_5_Acompanantes_WebP_Definitivos.zip .
Preserves all existing resources and does not update profile data.
"""
from pathlib import Path
import hashlib
import json
import sys
import zipfile

NAMES=("igris","beru","tank","tusk","iron")

def sha256(raw):
    return hashlib.sha256(raw).hexdigest()

def main():
    if len(sys.argv)!=3:
        raise SystemExit("Uso: python3 scripts/import_asterion_companions.py ARCHIVO.zip PROYECTO_ANDROID")
    zip_path=Path(sys.argv[1])
    project=Path(sys.argv[2])
    if not (project/"app/src/main/AndroidManifest.xml").is_file():
        raise SystemExit("La carpeta indicada no es la raíz del proyecto Android.")
    target=project/"app/src/main/res/drawable-nodpi"
    plan=[]
    with zipfile.ZipFile(zip_path) as bundle:
        entries=json.loads(bundle.read("MANIFIESTO.json"))["assets"]
        expected={e["id"]:e for e in entries}
        if set(expected)!=set(NAMES) or len(entries)!=5:
            raise SystemExit("El manifiesto debe contener exactamente los cinco acompañantes aprobados.")
        for name in NAMES:
            record=expected[name]
            filename="asterion_companion_"+name+".webp"
            if record["path"]!=filename or record.get("dimensions")!=[1024,1024] or not record.get("rgba"):
                raise SystemExit("Archivo o dimensiones incorrectas: "+name)
            raw=bundle.read(filename)
            if len(raw)!=record["bytes"] or sha256(raw)!=record["sha256"]:
                raise SystemExit("Error SHA-256: "+filename)
            if raw[:4]!=b"RIFF" or raw[8:12]!=b"WEBP":
                raise SystemExit("Formato WebP no válido: "+filename)
            dest=target/filename
            if dest.exists() and sha256(dest.read_bytes())!=sha256(raw):
                raise SystemExit("Conflicto: archivo ya existente diferente; no se sobrescribe: "+str(dest))
            plan.append((dest,raw))
    # All checks complete before writing anything
    target.mkdir(parents=True,exist_ok=True)
    imported=0
    for dest,raw in plan:
        if not dest.exists():
            with dest.open("xb") as f:
                f.write(raw)
            imported+=1
    print("Cinco compañeros verificados. Nuevos:",imported,"Ya presentes:",5-imported)

if __name__=="__main__":
    main()
