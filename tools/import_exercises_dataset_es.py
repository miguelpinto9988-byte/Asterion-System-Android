#!/usr/bin/env python3
"""Importación exclusiva de metadatos MIT e instrucciones en español. Nunca GIF/JPG."""
import hashlib
import json
import re
import urllib.request
from pathlib import Path
from collections import Counter

UPSTREAM="7455efae41b330c265e7cd4b78dfa848e7ce5ebd"
BLOB_SHA="3cbb77b678c40d7342897d38e4c61e9502f829a7"
BASE="https://raw.githubusercontent.com/hasaneyldrm/exercises-dataset/"+UPSTREAM
OUTPUT=Path("app/src/main/assets/external_exercises/v1")
NOTICES=Path("third_party/exercises_dataset")
GROUP={
 "upper arms":"tren_superior","lower arms":"tren_superior","shoulders":"tren_superior",
 "chest":"tren_superior","back":"tren_superior","upper legs":"tren_inferior",
 "lower legs":"tren_inferior","waist":"tren_central","neck":"movilidad","cardio":"cardio"
}
REGION={
 "upper arms":"brazos","lower arms":"antebrazos","shoulders":"hombros",
 "chest":"pecho","back":"espalda","upper legs":"piernas",
 "lower legs":"pantorrillas","waist":"abdomen","neck":"cuello","cardio":"cardio"
}
GEAR={
 "body weight":"peso corporal","dumbbell":"mancuerna","cable":"polea",
 "barbell":"barra","leverage machine":"máquina guiada","band":"banda elástica",
 "smith machine":"máquina Smith","kettlebell":"pesa rusa","weighted":"lastre",
 "stability ball":"pelota de estabilidad","ez barbell":"barra Z",
 "medicine ball":"balón medicinal"
}
PATTERNS=[
 (r"pull[\s-]?up|chin[\s-]?up","Dominada"),
 (r"push[\s-]?up","Flexión"),
 (r"squat","Sentadilla"),
 (r"lunge","Zancada"),
 (r"deadlift","Peso muerto"),
 (r"plank","Plancha"),
 (r"crunch","Abdominal corto"),
 (r"sit[\s-]?up","Abdominal"),
 (r"row","Remo"),
 (r"pulldown","Jalón"),
 (r"curl","Curl"),
 (r"press","Press"),
 (r"raise","Elevación"),
 (r"extension","Extensión"),
 (r"fly|flies","Apertura"),
 (r"stretch","Estiramiento"),
 (r"bridge","Puente"),
 (r"shrug","Encogimiento"),
 (r"rotation","Rotación"),
 (r"twist","Giro"),
 (r"step","Paso"),
 (r"jump","Salto"),
 (r"kick","Patada"),
 (r"dip","Fondo"),
 (r"walk","Caminata"),
 (r"run","Carrera"),
 (r"hold","Sostenimiento")
]

def code_for(body,name):
    if body not in GROUP: raise ValueError("Región desconocida: "+body)
    if re.search(r"\bstretch(ing)?\b",name.lower()):return "movilidad"
    return GROUP[body]

def title_es(name,body,equipment):
    label=next((label for pattern,label in PATTERNS
        if re.search(pattern,name.lower())), "Ejercicio")
    area=REGION[body]
    material=GEAR.get(equipment,"equipamiento específico")
    return f"{label} de {area} con {material}"

def transform(rows):
    if len(rows)!=1324:raise ValueError("Se esperaban 1324 ejercicios")
    seen=set()
    result=[]
    for row in rows:
        id=str(row["id"])
        if not re.fullmatch(r"[0-9]{4}",id) or id in seen:raise ValueError("ID incorrecto")
        seen.add(id)
        body=row["body_part"]
        name=row["name"]
        equipment=row["equipment"]
        steps=row.get("instruction_steps",{}).get("es")
        if not isinstance(steps,list) or not steps or not all(
                isinstance(s,str) and s.strip() for s in steps):
            raise ValueError("Faltan instrucciones en español: "+id)
        if not row.get("instructions",{}).get("es","").strip():
            raise ValueError("Falta texto en español: "+id)
        if not str(row.get("gif_url","")).startswith("videos/"):
            raise ValueError("GIF no referenciado: "+id)
        if not str(row.get("image","")).startswith("images/"):
            raise ValueError("Imagen no referenciada: "+id)
        result.append({
            "id":"ext_"+id,
            "origen_id":id,
            "nombre_es":title_es(name,body,equipment)+" · "+id,
            "nombre_original":name,
            "categoria":code_for(body,name),
            "region_es":REGION[body],
            "material_es":GEAR.get(equipment,"equipamiento específico"),
            "material_original":equipment,
            "instrucciones_es":[s.strip() for s in steps],
            "nivel_tecnico":"pendiente_revision",
            "apto_para_rutinas_automaticas":False,
            "gif_archivo_original":row["gif_url"],
            "imagen_archivo_original":row["image"],
            "attribution":row.get("attribution","© Gym visual — https://gymvisual.com/"),
            "gif_incluido":False,
            "imagen_incluida":False
        })
    return {"schema":"asterion.external-exercises.es.v1",
            "fuente":"https://github.com/hasaneyldrm/exercises-dataset",
            "commit_fuente":UPSTREAM,
            "idioma":"es",
            "total":len(result),
            "multimedia_incluida":False,
            "aviso_multimedia":"© Gym visual — https://gymvisual.com/; licencia propia requerida.",
            "ejercicios":result}

def download(filename):
    request=urllib.request.Request(BASE+"/"+filename,
        headers={"User-Agent":"Asterion-Importer/1.0"})
    with urllib.request.urlopen(request,timeout=120) as response:
        return response.read()

def main():
    raw=download("data/exercises.json")
    actual=hashlib.sha1(f"blob {len(raw)}\0".encode()+raw).hexdigest()
    if actual!=BLOB_SHA:raise ValueError("El JSON de origen cambió: "+actual)
    data=transform(json.loads(raw.decode("utf-8")))
    OUTPUT.mkdir(parents=True,exist_ok=True)
    filename=OUTPUT/"ejercicios_es.json"
    filename.write_text(json.dumps(data,ensure_ascii=False,separators=(",",":")),
                        encoding="utf-8")
    NOTICES.mkdir(parents=True,exist_ok=True)
    for file in ("LICENSE","NOTICE.md"):
        (NOTICES/file).write_bytes(download(file))
    summary={"total":len(data["ejercicios"]),
        "categorias":dict(Counter(x["categoria"] for x in data["ejercicios"])),
        "espanol":sum(bool(x["instrucciones_es"]) for x in data["ejercicios"]),
        "niveles_sin_validar":len(data["ejercicios"]),
        "gifs_incluidos":0,"imagenes_incluidas":0,
        "fuente_sha":UPSTREAM,
        "sha256":hashlib.sha256(filename.read_bytes()).hexdigest()}
    (OUTPUT/"manifest.json").write_text(
        json.dumps(summary,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
    print(json.dumps(summary,ensure_ascii=False,indent=2))

if __name__=="__main__":main()
