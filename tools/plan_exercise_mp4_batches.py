#!/usr/bin/env python3
"""Cola reproducible de MP4 por categoría. No crea animaciones por sí sola."""
import csv
import json
from collections import defaultdict
from pathlib import Path
ROOT=Path(__file__).resolve().parent.parent
SPECS=ROOT/"app/src/main/assets/exercise_gifs/v1/specs_es.json"
OUT=ROOT/"docs/datos/produccion_mp4_por_variante.csv"
FIELDS=("id","nombre","categoria","variante","material","mp4_ruta",
        "fondo","personaje","tecnica_revisada","estado_video","accion_siguiente")

def build():
    d=json.loads(SPECS.read_text(encoding="utf-8"))
    if len(d["ejercicios"])!=2104:
        raise ValueError("No coincide el inventario de variantes")
    records=[]
    for e in d["ejercicios"]:
        records.append({
            "id":e["id"],
            "nombre":e["nombre"],
            "categoria":e["categoria"],
            "variante":";".join(e["variante"]),
            "material":e["material"],
            "mp4_ruta":e["mp4_ruta"],
            "fondo":"gimnasio_moderno_fijo",
            "personaje":"cazador_anime_original_aura_azul",
            "tecnica_revisada":"no",
            "estado_video":e["mp4_estado"],
            "accion_siguiente":"validar_variante_y_generar_fotogramas_propios"
        })
    if len({r["mp4_ruta"] for r in records})!=2104:
        raise ValueError("Rutas repetidas entre variantes")
    OUT.parent.mkdir(parents=True,exist_ok=True)
    with OUT.open("w",encoding="utf-8",newline="") as f:
        writer=csv.DictWriter(f,fieldnames=FIELDS)
        writer.writeheader()
        writer.writerows(records)
    print(json.dumps({"items":len(records),"completados":0,
           "estado":"cola_de_produccion_no_videos_terminados"},
           ensure_ascii=False))
    return records

if __name__=="__main__":build()
