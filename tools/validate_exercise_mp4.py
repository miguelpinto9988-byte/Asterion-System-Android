#!/usr/bin/env python3
"""Audita MP4 H264 locales por ejercicio sin fingir cobertura o aprobación."""
from __future__ import annotations
import argparse
import hashlib
import json
import subprocess
from pathlib import Path

ROOT=Path(__file__).resolve().parent.parent
SPECS=ROOT/"app/src/main/assets/exercise_gifs/v1/specs_es.json"
DEFAULT_DIR=ROOT/"app/src/main/assets/exercise_mp4/v1"
MAX_BYTES=400000

def validate(directory=DEFAULT_DIR,require_all=False):
    data=json.loads(SPECS.read_text(encoding="utf-8"))
    entries=data["ejercicios"]
    if len(entries)!=2104:raise ValueError("Catálogo incompleto")
    target={x["id"]:x for x in entries}
    if len(target)!=2104:raise ValueError("IDs duplicados")
    for id,spec in target.items():
        if spec.get("mp4_ruta")!="exercise_mp4/v1/"+id+".mp4":
            raise ValueError("Ruta MP4 no es única "+id)
    videos=sorted(directory.glob("*.mp4")) if directory.exists() else []
    hashes={}
    for file in videos:
        if file.stem not in target:
            raise ValueError("MP4 sin ID registrado: "+file.name)
        if file.stat().st_size>MAX_BYTES:
            raise ValueError("MP4 excede presupuesto: "+file.name)
        if file.stat().st_size<100:
            raise ValueError("MP4 vacío o truncado: "+file.name)
        digest=hashlib.sha256(file.read_bytes()).hexdigest()
        if digest in hashes:
            raise ValueError("MP4 reutilizado por dos variantes: "+
                             hashes[digest]+" y "+file.name)
        hashes[digest]=file.name
        info=json.loads(subprocess.run(
            ["ffprobe","-v","error","-show_streams","-show_format",
             "-of","json",str(file)],
            capture_output=True,text=True,check=True).stdout)
        streams=info["streams"]
        if any(s["codec_type"]=="audio" for s in streams):
            raise ValueError("El MP4 contiene audio: "+file.name)
        video=[s for s in streams if s["codec_type"]=="video"]
        if len(video)!=1 or video[0]["codec_name"]!="h264":
            raise ValueError("El MP4 no es H264: "+file.name)
        if (video[0]["width"],video[0]["height"])!=(480,640):
            raise ValueError("Dimensiones incorrectas: "+file.name)
        dur=float(info["format"].get("duration","0"))
        if not 0.5<=dur<=8:
            raise ValueError("Duración de ciclo fuera de rango: "+file.name)
    if require_all and len(videos)!=2104:
        raise ValueError(f"Faltan {2104-len(videos)} de 2104 MP4")
    result={
        "total_variantes":2104,
        "mp4_fisicos":len(videos),
        "mp4_faltantes":2104-len(videos),
        "mp4_revisados_tecnicamente":0,
        "existen_rutas_individuales":True,
        "mp4_no_reutilizados":True,
        "resultado":"auditoria_de_archivos_no_validacion_biomecanica"}
    print(json.dumps(result,ensure_ascii=False,indent=2))
    return result

if __name__=="__main__":
    p=argparse.ArgumentParser()
    p.add_argument("--directory",type=Path,default=DEFAULT_DIR)
    p.add_argument("--require-all",action="store_true")
    args=p.parse_args()
    validate(args.directory,args.require_all)
