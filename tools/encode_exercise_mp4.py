#!/usr/bin/env python3
"""Convierte un GIF creado para un ID a un MP4 H264 móvil, sin audio.

Requisitos: ffmpeg y ffprobe. El GIF debe representar EXACTAMENTE el ejercicio.
No genera movimientos, no reasigna vídeos y no certifica técnica deportiva.
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
MAX_BYTES=400000

def inspect(path):
    result=subprocess.run(
        ["ffprobe","-v","error","-show_streams","-show_format",
         "-of","json",str(path)],
        check=True,capture_output=True,text=True)
    return json.loads(result.stdout)

def encode(source,id,output_dir=OUT,max_bytes=MAX_BYTES):
    if not re.fullmatch(r"[a-z0-9_-]+",id):raise ValueError("ID inválido")
    manifest=json.loads(SPECS.read_text(encoding="utf-8"))
    record=next((x for x in manifest["ejercicios"] if x["id"]==id),None)
    if record is None or record.get("mp4_ruta")!="exercise_mp4/v1/"+id+".mp4":
        raise ValueError("ID MP4 no coincide con ficha original")
    if not source.is_file():raise FileNotFoundError(source)
    if source.suffix.lower() not in (".gif",".apng",".webp",".mov",".mp4"):
        raise ValueError("Origen no válido; aportar animación original")
    output_dir.mkdir(parents=True,exist_ok=True)
    output=output_dir/(id+".mp4")
    filt=("fps=25/3,scale=480:640:force_original_aspect_ratio=decrease:"
          "flags=lanczos,pad=480:640:(ow-iw)/2:(oh-ih)/2:"
          "color=0x181b24,format=yuv420p")
    for crf in (24,26,28,30,32):
        trial=output_dir/(id+".preview.mp4")
        args=["ffmpeg","-hide_banner","-loglevel","error","-y",
              "-i",str(source),"-vf",filt,"-an",
              "-c:v","libx264","-preset","slow","-crf",str(crf),
              "-pix_fmt","yuv420p","-movflags","+faststart",
              str(trial)]
        subprocess.run(args,check=True)
        if 0<trial.stat().st_size<=max_bytes:
            trial.replace(output)
            metadata=inspect(output)
            video=[s for s in metadata["streams"] if s["codec_type"]=="video"]
            if len(video)!=1 or video[0]["codec_name"]!="h264" or \
                (video[0]["width"],video[0]["height"])!=(480,640):
                output.unlink(missing_ok=True)
                raise ValueError("MP4 inválido tras codificación")
            return {"id":id,"path":str(output),
                    "bytes":output.stat().st_size,
                    "crf":crf,"frames":video[0].get("nb_frames"),
                    "estado":"prototipo_visual_pendiente_de_revision"}
        trial.unlink(missing_ok=True)
    raise ValueError("MP4 sobrepasa presupuesto de bytes; modificar escena")

def main():
    p=argparse.ArgumentParser()
    p.add_argument("--id",required=True)
    p.add_argument("--source",type=Path,required=True)
    p.add_argument("--output-dir",type=Path,default=OUT)
    args=p.parse_args()
    print(json.dumps(encode(args.source,args.id,args.output_dir),
                     ensure_ascii=False,indent=2))

if __name__=="__main__":main()
