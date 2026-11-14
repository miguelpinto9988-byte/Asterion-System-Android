#!/usr/bin/env python3
"""Codifica MP4 H.264 16:9 desde vídeo o fotogramas PNG originales.

No genera GIF ni reutiliza el movimiento entre ejercicios.
La fuente debe representar el movimiento específico del ID registrado.
La resolución 16:9 debe existir en la fuente: NO se añaden barras laterales.
"""
from __future__ import annotations

import argparse
import json
import re
import subprocess
from pathlib import Path

ROOT=Path(__file__).resolve().parent.parent
SPECS=ROOT/"app/src/main/assets/exercise_gifs/v1/specs_es.json"
OUT=ROOT/"app/src/main/assets/exercise_mp4/v1"
MAX_BYTES=800000
W,H=640,360

def inspect(path):
    return json.loads(subprocess.run(
        ["ffprobe","-v","error","-show_streams","-show_format",
         "-of","json",str(path)],check=True,capture_output=True,text=True).stdout)

def video_stream(meta):
    v=[s for s in meta["streams"] if s["codec_type"]=="video"]
    if len(v)!=1:raise ValueError("La fuente debe contener exactamente un vídeo")
    return v[0]

def encode(source,id,output_dir=OUT,max_bytes=MAX_BYTES,source_fps=10.0):
    if not re.fullmatch(r"[a-z0-9_-]+",id):
        raise ValueError("ID inválido")
    manifest=json.loads(SPECS.read_text(encoding="utf-8"))
    record=next((x for x in manifest["ejercicios"] if x["id"]==id),None)
    if record is None or record.get("mp4_ruta")!="exercise_mp4/v1/"+id+".mp4":
        raise ValueError("ID de MP4 no coincide con su ficha")
    if source.is_dir():
        frames=sorted(source.glob("*.png"))
        if len(frames)<8 or len(frames)!=len(list(source.iterdir())):
            raise ValueError("Se esperan 8 o más PNG y ningún otro archivo")
        if [f.name for f in frames]!=[f"{i:03d}.png" for i in range(len(frames))]:
            raise ValueError("Los PNG deben tener nombres 000.png, 001.png, etc.")
        src=frames[0]
        meta=inspect(src)
        incoming=["-framerate",str(source_fps),"-i",str(source/"%03d.png")]
    elif source.is_file() and source.suffix.lower() in (".mp4",".mov",".webm",".mkv"):
        src=source
        meta=inspect(src)
        incoming=["-i",str(source)]
    else:
        raise ValueError("Origen no válido: usar MP4/MOV/WEBM/MKV o PNG originales")
    stream=video_stream(meta)
    width,height=stream["width"],stream["height"]
    if abs(width/height-16/9)>0.01:
        raise ValueError("La fuente debe ser panorámica 16:9 antes de codificar")
    output_dir.mkdir(parents=True,exist_ok=True)
    output=output_dir/(id+".mp4")
    trial=output_dir/(id+".preview.mp4")
    try:
        for crf in (21,23,25,27,29,31):
            subprocess.run(
                ["ffmpeg","-hide_banner","-loglevel","error","-y",*incoming,
                 "-vf","fps=25,scale=640:360:flags=lanczos,format=yuv420p",
                 "-an","-c:v","libx264","-preset","slow","-crf",str(crf),
                 "-movflags","+faststart",str(trial)],check=True)
            if 0<trial.stat().st_size<=max_bytes:
                finished=inspect(trial)
                v=video_stream(finished)
                if v["codec_name"]!="h264" or (v["width"],v["height"])!=(W,H):
                    raise ValueError("Resultado MP4 inválido")
                if any(s["codec_type"]=="audio" for s in finished["streams"]):
                    raise ValueError("No se permite audio en la animación")
                trial.replace(output)
                return {"id":id,"path":str(output),"bytes":output.stat().st_size,
                        "fps":25,"ancho":W,"alto":H,"crf":crf,
                        "estado":"prototipo_pendiente_revision"}
        raise ValueError("El MP4 excede 800 KB incluso con compresión: revisar la escena")
    finally:
        trial.unlink(missing_ok=True)

def main():
    p=argparse.ArgumentParser()
    p.add_argument("--id",required=True)
    p.add_argument("--source",required=True,type=Path,
                   help="Vídeo original 16:9 o carpeta de PNG 000.png, 001.png...")
    p.add_argument("--source-fps",type=float,default=10)
    p.add_argument("--output-dir",type=Path,default=OUT)
    args=p.parse_args()
    print(json.dumps(encode(args.source,args.id,args.output_dir,
                            source_fps=args.source_fps),ensure_ascii=False,indent=2))

if __name__=="__main__":
    main()
