#!/usr/bin/env python3
"""Specs por ID para GIF propios. Un GIF pendiente JAMÁS se presenta como creado."""
import json
import re
import unicodedata
from collections import Counter
from pathlib import Path

ROOT=Path(__file__).resolve().parent.parent
SOURCE=ROOT/"app/src/main/assets/unified_exercises/v2/catalogo_es.json"
OUT=ROOT/"app/src/main/assets/exercise_gifs/v1/specs_es.json"
CURRENT_SCHEMA="asterion.exercise-motion.es.v1"

def simple(value):
    return "".join(c for c in unicodedata.normalize("NFKD",str(value).lower())
                   if not unicodedata.combining(c))

def specs(x):
    id=x["id"]
    if not re.fullmatch(r"[a-z0-9_-]+",id):
        raise ValueError("ID no seguro: "+id)
    name=simple(x["nombre_es"])
    origin=simple(x.get("nombre_fuente_dominio_publico",""))
    source=simple(x.get("source_public_domain_id","")).replace("_"," ").replace("-"," ")
    words=" ".join((name,origin,source))
    tags=[]
    base="Movimiento específico pendiente de revisión individual."
    hands="Verificar postura, apoyos y agarre de esta variante."
    position="Postura inicial por comprobar para la variante."
    difference="No reutilizar el GIF de otra variante sin cotejar la técnica."
    if re.search(r"flexion de brazos|flexiones|push.up|push up|pushup",words):
        base="Flexión y extensión de codos con el tronco controlado."
        position="Apoyo de manos y pies o apoyo elevado, según variante."
        if re.search(r"diamond|diamante",words):
            tags.append("diamante")
            hands="Manos próximas en forma de rombo; comprobar comodidad de muñecas."
            difference="Diamante no es sinónimo de cualquier agarre cerrado."
        elif re.search(r"wide|ancho|separadas",words):
            tags.append("manos_separadas")
            hands="Manos más separadas que en la variante convencional."
        elif re.search(r"close|cerrado|estrecho|juntas",words):
            tags.append("manos_proximas")
            hands="Manos próximas; NO se supone una colocación en diamante."
        else:
            tags.append("agarre_por_revisar")
        if re.search(r"incline|inclinad",words):
            tags.append("apoyo_elevado")
            position="Manos sobre superficie elevada y estable."
        if re.search(r"decline|feet elevated|pies elevados",words):
            tags.append("pies_elevados")
        if re.search(r"\bpared\b",words):
            tags.append("pared")
            position="Manos apoyadas en pared estable."
        if re.search(r"plyo|depth jump|pliom",words):
            tags.append("movimiento_dinamico_avanzado")
        if re.search(r"handstand|invertid",words):
            tags.append("invertida_avanzada")
        if id=="flexiones_v04":
            tags=["convencional"]
            hands="Manos aproximadamente a la anchura de los hombros."
            position="Apoyo firme de manos y pies con cuerpo alineado."
    elif re.search(r"sentadilla|squat",words):
        tags=["sentadilla"]
        position="Pies en apoyo estable; postura exacta depende de material."
        base="Flexión controlada de caderas y rodillas y regreso a la postura inicial."
    elif re.search(r"zancada|lunge",words):
        tags=["zancada"]
        position="Apoyos de piernas según se trate de paso o variante estática."
        base="Cambio de apoyo y flexión controlada de rodilla y cadera."
    elif re.search(r"remo|row",words):
        tags=["remo"]
        position="Apoyo y orientación del tronco según máquina o implemento."
        base="Tracción del implemento según la trayectoria de la variante."
    else:
        tags=["pendiente_clasificacion_de_variante"]
    return {
        "id":id,"nombre":x["nombre_es"],"categoria":x["categoria"],
        "region":x.get("region_es",""),"material":x.get("material_es",""),
        "nivel":x.get("nivel_tecnico","pendiente_revision"),
        "origen":x["origen"],"id_fuente":x.get("source_public_domain_id",""),
        "variante":tags,"posicion":position,"agarre_o_apoyo":hands,
        "recorrido_general":base,"criterio_diferenciador":difference,
        "instrucciones_es":x.get("instrucciones_es",[]),
        "estado_tecnico":"orientacion_no_validada_para_gif",
        "gif_ruta":"exercise_gifs/v1/"+id+".gif",
        "mp4_ruta":"exercise_mp4/v1/"+id+".mp4",
        "mp4_estado":"pendiente_produccion",
        "video_tecnica_validada":False,
        "gif_estado":"pendiente_produccion",
        "gif_validado":False,
        "nota":"No es una ficha técnica individual aprobada por profesional."
    }

def build():
    d=json.loads(SOURCE.read_text(encoding="utf-8"))
    if d.get("schema")!="asterion.public-domain-unified.es.v2" or d.get("total")!=2104:
        raise ValueError("Catálogo v2 ajeno o incompleto")
    rows=[specs(x) for x in d["ejercicios"]]
    ids=[x["id"] for x in rows]
    if len(rows)!=2104 or len(set(ids))!=len(ids):
        raise ValueError("Fichas de origen repetidas o incompletas")
    if len({x["gif_ruta"] for x in rows})!=2104:
        raise ValueError("Cada variante requiere un GIF propio")
    video_root=ROOT/"app/src/main/assets/exercise_mp4/v1"
    video_present=sum(1 for x in rows if (video_root/(x["id"]+".mp4")).is_file())
    for row in rows:
        if (video_root/(row["id"]+".mp4")).is_file():
            row["mp4_estado"]="archivo_presente_pendiente_revision"
    physically_present=sum(1 for x in rows
        if (OUT.parent/(x["id"]+".gif")).is_file())
    for row in rows:
        if (OUT.parent/(row["id"]+".gif")).is_file():
            row["gif_estado"]="archivo_presente_pendiente_revision"
    result={"schema":CURRENT_SCHEMA,"idioma":"es","total":2104,
        "gifs_producidos_por_este_importador":0,
        "gifs_fisicos":physically_present,
        "videos_mp4_fisicos":video_present,
        "videos_mp4_pendientes":2104-video_present,
        "formato_preferido":"mp4_h264",
        "estilo_video":"personaje_anime_original_aura_azul_gimnasio_moderno",
        "gifs_pendientes":2104-physically_present,
        "personaje":"cazador_anime_original_aura_azul",
        "formato_recomendado":{"ancho_px":480,"alto_px":640,
            "frames":12,"ms_por_frame":120,"max_bytes":800000,
            "presentacion":"adaptar_sin_recortar_en_movil"},
        "formato_mp4_recomendado":{"codec":"h264","ancho_px":640,
          "alto_px":360,"fps":25,"sin_audio":True,
          "fondo":"gimnasio_moderno_estatico",
          "bucle":True,"bytes_objetivo_max":400000,
          "bytes_maximos":800000,"sin_interpolacion_borrosa":True},
        "politica_multimedia":"solo_mp4_no_generar_gif",
        "personaje_visual":"hombre_anime_original_polo_negro_aura_azul_profundo",
        "categorias":dict(sorted(Counter(x["categoria"] for x in rows).items())),
        "ejercicios":rows}
    OUT.parent.mkdir(parents=True,exist_ok=True)
    OUT.write_text(json.dumps(result,ensure_ascii=False,
                   separators=(",",":"))+"\n",encoding="utf-8")
    print(json.dumps({k:v for k,v in result.items() if k!="ejercicios"},
                     ensure_ascii=False,indent=2))
    return result

if __name__=="__main__":
    build()
