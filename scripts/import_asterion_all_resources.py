#!/usr/bin/env python3
"""Safely add 94 approved WebP files from the Android-ready Asterion ZIP.

Usage: python3 scripts/import_asterion_all_resources.py /path/Asterion_Android_94_WebP_Sin_Marcos_Cuadrados.zip .
Never delete or overwrite existing files; verify all source hashes and
conflicts before writing any file.
"""
from pathlib import Path
from hashlib import sha256
import json
import sys
import zipfile

def checksum(data):
    return sha256(data).hexdigest()

def main():
    if len(sys.argv) != 3:
        raise SystemExit("Uso: python3 scripts/import_asterion_all_resources.py PAQUETE.zip RUTA_PROYECTO")
    archive, project = Path(sys.argv[1]), Path(sys.argv[2])
    if not (project / "app/src/main/AndroidManifest.xml").is_file():
        raise SystemExit("Raíz Android incorrecta; no se ha escrito nada.")
    with zipfile.ZipFile(archive) as z:
        records = json.loads(z.read("MANIFIESTO_ANDROID.json"))["assets"]
        if len(records) != 94:
            raise SystemExit("Se requieren exactamente 94 recursos WebP aprobados.")
        plan=[]
        seen=set()
        for r in records:
            path=r["path"]
            if path in seen or not path.startswith("app/src/main/res/drawable-nodpi/") or not path.endswith(".webp"):
                raise SystemExit("Ruta inválida o repetida: "+path)
            if "asterion_frame_" in path or "ic_marco." in path:
                raise SystemExit("Este paquete ya no admite marcos ni iconos de marcos: "+path)
            seen.add(path)
            raw=z.read(path)
            if len(raw)!=r["bytes"] or checksum(raw)!=r["sha256"]:
                raise SystemExit("Hash SHA256 inválido: "+path)
            if raw[:4]!=b"RIFF" or raw[8:12]!=b"WEBP":
                raise SystemExit("Cabecera WebP incorrecta: "+path)
            dest=project/path
            if dest.exists() and checksum(dest.read_bytes())!=checksum(raw):
                raise SystemExit("Existe un archivo diferente; no se sobrescribe: "+path)
            plan.append((dest,raw))
        # All validation has passed. Do not modify existing files.
        written=0
        for dest,raw in plan:
            if not dest.exists():
                dest.parent.mkdir(parents=True,exist_ok=True)
                with dest.open("xb") as output:
                    output.write(raw)
                written+=1
    print("Integridad OK: 94 WebP, nuevos:",written,"ya idénticos:",94-written)

if __name__ == "__main__":
    main()
