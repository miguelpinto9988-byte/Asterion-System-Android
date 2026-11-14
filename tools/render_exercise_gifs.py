#!/usr/bin/env python3
"""Empaqueta fotogramas de una variante creada en un GIF móvil.
No inventa posturas ni reutiliza GIFs de otras variantes.
Requiere Pillow==11.3.0. Ejecutar con --frames-dir y --id.
"""
import argparse
import json
from pathlib import Path
from PIL import Image, ImageOps

ROOT=Path(__file__).resolve().parent.parent
SPECS=ROOT/"app/src/main/assets/exercise_gifs/v1/specs_es.json"
DEFAULT_OUT=ROOT/"app/src/main/assets/exercise_gifs/v1"

def assemble(id,frame_dir,out,max_bytes=800000):
    if not frame_dir.is_dir():raise ValueError("No hay fotogramas para "+id)
    files=sorted(frame_dir.glob("*.png"))
    if len(files)<8 or len(files)>24:
        raise ValueError("Se requieren 8-24 fotogramas POR VARIANTE: "+id)
    frames=[]
    for source in files:
        with Image.open(source) as image:
            converted=ImageOps.contain(image.convert("RGB"),(480,640),Image.Resampling.LANCZOS)
            canvas=Image.new("RGB",(480,640),(30,33,44))
            canvas.paste(converted,((480-converted.width)//2,(640-converted.height)//2))
            frames.append(canvas)
    target=out/(id+".gif")
    target.parent.mkdir(parents=True,exist_ok=True)
    for colors in (96,64,48,32):
        palettes=[f.quantize(colors=colors,method=Image.Quantize.FASTOCTREE).convert("RGB") for f in frames]
        temp=target.with_suffix(".tmp.gif")
        palettes[0].save(temp,format="GIF",save_all=True,
                         append_images=palettes[1:],duration=120,
                         loop=0,disposal=2,optimize=True)
        if temp.stat().st_size<=max_bytes:
            temp.replace(target)
            return {"id":id,"file":str(target),"bytes":target.stat().st_size,
                    "frames":len(files),"estado":"borrador_sin_revision"}
        temp.unlink()
    raise ValueError("GIF excesivo: simplificar fondo o efectos")
def main():
    parser=argparse.ArgumentParser()
    parser.add_argument("--frames-dir",type=Path,required=True)
    parser.add_argument("--id",required=True)
    parser.add_argument("--output",type=Path,default=DEFAULT_OUT)
    args=parser.parse_args()
    specs=json.loads(SPECS.read_text(encoding="utf-8"))
    ids={x["id"] for x in specs["ejercicios"]}
    if args.id not in ids:raise ValueError("ID no registrado")
    print(json.dumps(assemble(args.id,args.frames_dir/args.id,args.output),
                     ensure_ascii=False,indent=2))
if __name__=="__main__":
    main()
