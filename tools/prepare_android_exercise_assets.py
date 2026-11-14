#!/usr/bin/env python3
"""Stage the pinned GIF release, generate its ID manifest, and validate Android assets."""
import argparse
import hashlib
import json
import re
import shutil
import sys
import tempfile
import urllib.request
import zipfile
from pathlib import Path, PurePosixPath

ROOT=Path(__file__).resolve().parent.parent
BASE_CATALOG=ROOT/"app/src/main/assets/unified_exercises/v2/catalogo_es.json"
EXTRA_CATALOG=ROOT/"app/src/main/assets/unified_exercises/v2/calistenia_casa_es.json"
ASSET_DIR=ROOT/"app/src/main/assets/exercise_gifs/v10"
ARCHIVE_URL=("https://github.com/miguelpinto9988-byte/Asterion-System-Android/releases/"
             "download/v10.0.0-calisthenics-preview.1/"
             "asterion-exercise-viewer-v10-calisthenics.zip")
ARCHIVE_SHA256="c4966b756150c8dcb684e0c950de118a4901d4ee169758c9a2e4a4c131a51d14"
GIF_PREFIX="build/gifs/exercise_gifs/v10/"
EXPECTED_COUNT=2123
MAX_BYTES=800000

def catalog_ids():
    base=json.loads(BASE_CATALOG.read_text(encoding="utf-8"))
    extra=json.loads(EXTRA_CATALOG.read_text(encoding="utf-8"))
    if base.get("schema")!="asterion.public-domain-unified.es.v2" or base.get("total")!=2104:
        raise ValueError("El catálogo base no coincide con el paquete v10")
    if extra.get("schema")!="asterion.public-domain-unified.es.v2" or extra.get("total")!=19:
        raise ValueError("La colección de calistenia debe contener 19 ejercicios")
    rows=base["ejercicios"]+extra["ejercicios"]
    ids=[row["id"] for row in rows]
    if len(ids)!=EXPECTED_COUNT or len(set(ids))!=EXPECTED_COUNT:
        raise ValueError("El catálogo debe tener 2123 IDs únicos")
    if any(not re.fullmatch(r"[a-z0-9_-]+",item) for item in ids):
        raise ValueError("El catálogo contiene un ID no seguro")
    return set(ids)

def sha256_file(path):
    digest=hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(1024*1024),b""):
            digest.update(chunk)
    return digest.hexdigest()

def validate_header(header,name,size):
    if size<10 or size>MAX_BYTES:
        raise ValueError(f"Tamaño de GIF inválido: {name} ({size} bytes)")
    if len(header)<10 or header[:6] not in (b"GIF89a",b"GIF87a"):
        raise ValueError("La entrada no contiene una cabecera GIF: "+name)
    width=int.from_bytes(header[6:8],"little")
    height=int.from_bytes(header[8:10],"little")
    if width<320 or height<400 or width>1024 or height>1365:
        raise ValueError("Dimensiones inadecuadas para móvil: "+name)
    if not .5<=width/height<=2.0:
        raise ValueError("Relación de aspecto inesperada: "+name)

def existing_bundle_is_complete(ids):
    if not ASSET_DIR.is_dir():
        return False
    files=list(ASSET_DIR.glob("*.gif"))
    if len(files)!=EXPECTED_COUNT or {item.stem for item in files}!=ids:
        return False
    for item in files:
        size=item.stat().st_size
        with item.open("rb") as source:
            header=source.read(10)
        try:
            validate_header(header,item.name,size)
        except ValueError:
            return False
    return True

def download_archive(target):
    request=urllib.request.Request(
        ARCHIVE_URL,headers={"User-Agent":"Asterion-System-Android-build"})
    with urllib.request.urlopen(request,timeout=180) as response,target.open("wb") as output:
        shutil.copyfileobj(response,output,length=1024*1024)

def stage_archive(archive_path,ids):
    if sha256_file(archive_path)!=ARCHIVE_SHA256:
        raise ValueError("SHA-256 del paquete de GIFs no coincide con la release fijada")
    prefix_parts=PurePosixPath(GIF_PREFIX.rstrip("/")).parts
    ASSET_DIR.mkdir(parents=True,exist_ok=True)
    with zipfile.ZipFile(archive_path) as archive:
        entries=[]
        seen=set()
        for info in archive.infolist():
            if info.is_dir() or not info.filename.startswith(GIF_PREFIX) or not info.filename.endswith(".gif"):
                continue
            path=PurePosixPath(info.filename)
            name=path.name
            exercise_id=name[:-4]
            if path.parts[:-1]!=prefix_parts or not re.fullmatch(r"[a-z0-9_-]+\.gif",name):
                raise ValueError("Ruta de GIF inválida en el paquete: "+info.filename)
            if exercise_id in seen:
                raise ValueError("GIF duplicado en el paquete: "+exercise_id)
            if exercise_id not in ids:
                raise ValueError("GIF sin ejercicio en el catálogo: "+exercise_id)
            if info.file_size>MAX_BYTES:
                raise ValueError("GIF excede 800 KB: "+name)
            seen.add(exercise_id)
            entries.append((exercise_id,info))
        if seen!=ids or len(entries)!=EXPECTED_COUNT:
            missing=sorted(ids-seen)
            extra=sorted(seen-ids)
            raise ValueError(f"El paquete no coincide con el catálogo: faltan {missing[:5]}, extras {extra[:5]}")
        for old in ASSET_DIR.glob("*.gif"):
            old.unlink()
        total_bytes=0
        for exercise_id,info in entries:
            target=ASSET_DIR/(exercise_id+".gif")
            with archive.open(info) as source,target.open("wb") as output:
                header=source.read(10)
                output.write(header)
                total=10
                while True:
                    chunk=source.read(1024*1024)
                    if not chunk:
                        break
                    output.write(chunk)
                    total+=len(chunk)
            if total!=info.file_size:
                raise ValueError("Tamaño extraído distinto al ZIP: "+target.name)
            validate_header(header,target.name,total)
            total_bytes+=total
    return total_bytes

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument("--archive",type=Path,help="ZIP ya descargado, útil para una compilación offline")
    parser.add_argument("--if-missing",action="store_true",
                        help="Reutiliza una colección completa ya preparada")
    args=parser.parse_args()
    ids=catalog_ids()
    downloaded=False
    if args.if_missing and not args.archive and existing_bundle_is_complete(ids):
        total_bytes=sum(item.stat().st_size for item in ASSET_DIR.glob("*.gif"))
    else:
        if args.archive:
            archive_path=args.archive.resolve()
            if not archive_path.is_file():
                raise FileNotFoundError(archive_path)
            total_bytes=stage_archive(archive_path,ids)
        else:
            with tempfile.TemporaryDirectory(prefix="asterion-gif-package-") as folder:
                archive_path=Path(folder)/"asterion-exercise-viewer-v10.zip"
                print("Descargando el paquete de GIFs con SHA-256 fijado...",flush=True)
                download_archive(archive_path)
                total_bytes=stage_archive(archive_path,ids)
            downloaded=True
    sys.path.insert(0,str(ROOT/"tools"))
    import build_exercise_motion_specs
    import validate_exercise_gifs
    build_exercise_motion_specs.build()
    validation=validate_exercise_gifs.check(require_all=True)
    result={
        "gifs":validation["gifs_fisicos"],
        "bytes_gif":total_bytes,
        "zip_sha256":ARCHIVE_SHA256,
        "descargado_ahora":downloaded,
        "aprobacion_tecnica":False,
        "recursos_listos_para_empaquetar":True
    }
    print(json.dumps(result,ensure_ascii=False,indent=2))

if __name__=="__main__":
    main()
