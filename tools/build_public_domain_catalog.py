#!/usr/bin/env python3
"""Generador de catálogo Asterion v2 basado exclusivamente en datos de dominio
público del proyecto free-exercise-db (Unlicense), NO en exercises-dataset.
No descarga ni copia imágenes, GIFs, vídeos ni instrucciones de Gym visual.
"""
from __future__ import annotations
import csv
import hashlib
import json
import re
import unicodedata
import urllib.request
from collections import Counter, defaultdict
from pathlib import Path

ROOT=Path(__file__).resolve().parent.parent
LEGACY=ROOT/"progress-engine/src/main/resources/asterion_exercises/v1"
DEST=ROOT/"progress-engine/src/main/resources/asterion_exercises/v2"
VIEW=ROOT/"app/src/main/assets/unified_exercises/v2"
AUDIT=ROOT/"docs/datos/auditoria_dominio_publico.csv"
UPSTREAM_COMMIT="f00c92c7dcf1216a928a52c3706c7ce8e2f71ed5"
UPSTREAM_BLOB="37beaac6031b34f7a7322b3150d201182c49a71b"
UPSTREAM_URL="https://github.com/yuhonas/free-exercise-db"
DOWNLOAD=("https://raw.githubusercontent.com/yuhonas/free-exercise-db/"
          +UPSTREAM_COMMIT+"/dist/exercises.json")
EXPECTED_COUNT=876

MUSCLES={
 "abdominals":("tren_central","abdomen"),"hamstrings":("tren_inferior","isquiotibiales"),
 "adductors":("tren_inferior","aductores"),"quadriceps":("tren_inferior","cuádriceps"),
 "biceps":("tren_superior","bíceps"),"shoulders":("tren_superior","hombros"),
 "chest":("tren_superior","pecho"),"middle back":("tren_superior","espalda"),
 "calves":("tren_inferior","pantorrillas"),"glutes":("tren_inferior","glúteos"),
 "lower back":("tren_central","zona lumbar"),"lats":("tren_superior","dorsales"),
 "triceps":("tren_superior","tríceps"),"traps":("tren_superior","trapecios"),
 "forearms":("tren_superior","antebrazos"),"neck":("movilidad","cuello"),
 "abductors":("tren_inferior","abductores")
}
EQUIPMENT={
 "body only":("sin_equipo","peso corporal"),"none":("sin_equipo","sin material"),
 "machine":("maquina","máquina"),"other":("material_especifico","material específico"),
 "foam roll":("rodillo","rodillo de espuma"),"kettlebells":("kettlebell","pesa rusa"),
 "dumbbell":("mancuerna","mancuernas"),"cable":("polea","polea"),
 "barbell":("barra","barra"),"bands":("banda","banda elástica"),
 "medicine ball":("balon","balón medicinal"),
 "exercise ball":("pelota","pelota de ejercicio"),
 "e-z curl bar":("barra","barra Z")
}
MOVEMENTS=[
 ("sentadilla",r"\bsquat\b|\bsquats\b|\bsentadilla"),
 ("zancada",r"\blung(e|es)\b|\bzancada|\bdesplante"),
 ("peso_muerto",r"\bdeadlift|\bpeso muerto"),
 ("puente",r"\bbridge|\bhip thrust|\bpuente|\belevaci[oó]n de cadera"),
 ("subida_escalon",r"\bstep.up|\bsubida al escal[oó]n"),
 ("flexion_brazos",r"\bpush.up|\bflexi[oó]n de brazos"),
 ("dominada",r"\bpull.up|\bchin.up|\bdominada"),
 ("jalon",r"\bpull.down|\bjal[oó]n"),
 ("press_pecho",r"\bbench press|\bchest press|\bpress de pecho"),
 ("press_hombros",r"\bshoulder press|\boverhead press|\bmilitary press|\bpress de hombros"),
 ("remo",r"\brow\b|\browing|\bremo"),
 ("curl_biceps",r"\b(?:biceps|hammer|preacher|concentration|spider) curl|\bcurl de b[ií]ceps|\bcurl martillo"),
 ("curl",r"\bcurl\b|\bcurl de "),
 ("extension_triceps",r"\btriceps extension|\bpush.down|\bextensi[oó]n de tr[ií]ceps"),
 ("extension_rodilla",r"\bleg extension|\bextensi[oó]n de rodilla"),
 ("elevacion_lateral",r"\blateral raise|\belevaci[oó]n lateral"),
 ("elevacion_frontal",r"\bfront raise|\belevaci[oó]n frontal"),
 ("elevacion_pierna",r"\bleg raise|\belevaci[oó]n de pierna"),
 ("apertura",r"\b(?:chest|pec) fly|\bflye|\bapertura"),
 ("pantorrillas",r"\bcalf raise|\bcalf press|\belevaci[oó]n de talones"),
 ("plancha",r"\bplank|\bplancha"),
 ("abdominal",r"\bsit.up|\bcrunch|\babdominal"),
 ("estiramiento",r"\bstretch|\bestiramiento"),
 ("burpee",r"\bburpee"),
 ("carrera",r"\brunning|\brun\b|\bcarrera"),
 ("caminata",r"\bwalk|\bcaminata"),
 ("salto",r"\bjump|\bsalto"),
 ("rotacion",r"\brotation|\brotaci[oó]n"),
 ("fondos",r"\bdips?\b|\bfondos?"),
 ("giro",r"\btwist|\bgiro"),
 ("patada",r"\bkick|\bpatada"),
 ("press",r"\bpress\b"),
 ("extension",r"\bextension|\bextensi[oó]n"),
 ("elevacion",r"\braise|\belevaci[oó]n"),
 ("remada",r"\bpull\b|\btir[oó]n"),
]
QUALIFIERS=[
 ("inclinado",r"\binclin|\bincline"),
 ("declinado",r"\bdeclin|\bdecline"),
 ("frontal",r"\bfront\b|\bfrontal"),
 ("posterior",r"\brear\b|\bbackward|\breverse\b|\btrasera"),
 ("lateral",r"\bside\b|\blateral"),
 ("sentado",r"\bseated|\bsentado"),
 ("de_pie",r"\bstanding|\bde pie"),
 ("acostado",r"\blying|\btumbad"),
 ("asistido",r"\bassisted|\basistid"),
 ("una_pierna",r"\bsingle.leg|\bone.leg|\bunilateral|\buna pierna"),
 ("un_brazo",r"\bsingle.arm|\bone.arm|\buna mano|\bun brazo"),
 ("agarre_ancho",r"\bwide.grip|\bgrip ancho"),
 ("agarre_cerrado",r"\bclose.grip|\bnarrow.grip|\bgrip cerrado"),
 ("alternado",r"\balternat|\balternad"),
 ("isometrico",r"\bisometric|\bisom[eé]tric"),
]
ES_NAMES={
 "sentadilla":"Sentadilla","zancada":"Zancada","peso_muerto":"Peso muerto",
 "puente":"Puente de cadera","subida_escalon":"Subida al escalón",
 "flexion_brazos":"Flexión de brazos","dominada":"Dominada","jalon":"Jalón",
 "press_pecho":"Press de pecho","press_hombros":"Press de hombros",
 "remo":"Remo","curl_biceps":"Curl de bíceps","curl":"Curl",
 "extension_triceps":"Extensión de tríceps",
 "extension_rodilla":"Extensión de rodilla",
 "elevacion_lateral":"Elevación lateral","elevacion_frontal":"Elevación frontal",
 "elevacion_pierna":"Elevación de piernas","apertura":"Apertura",
 "pantorrillas":"Elevación de talones","plancha":"Plancha",
 "abdominal":"Trabajo abdominal","estiramiento":"Estiramiento",
 "burpee":"Burpee","carrera":"Carrera","caminata":"Caminata","salto":"Salto",
 "rotacion":"Rotación","fondos":"Fondo","giro":"Giro","patada":"Patada",
 "press":"Press","extension":"Extensión","elevacion":"Elevación",
 "remada":"Tracción"
}
ES_QUALIFIERS={
 "inclinado":"inclinada","declinado":"declinada","frontal":"frontal",
 "posterior":"posterior","lateral":"lateral","sentado":"sentada",
 "de_pie":"de pie","acostado":"acostada","asistido":"asistida",
 "una_pierna":"a una pierna","un_brazo":"a un brazo",
 "agarre_ancho":"con agarre ancho","agarre_cerrado":"con agarre cerrado",
 "alternado":"alternada","isometrico":"isométrica"
}
# No inventar correspondencias: las coincidencias dudosas se excluyen
# del motor y se conservan en el informe para verificación individual.
NAME_EXACT_OVERRIDES={}  # Revisiones positivas reales pueden declararse aquí.

def normalize(text):
    norm=unicodedata.normalize("NFKD",text.lower())
    return "".join(c for c in norm if not unicodedata.combining(c))

def movement(text):
    value=normalize(text)
    for key,pattern in MOVEMENTS:
        if re.search(pattern,value):return key
    return None

def modifiers(text):
    value=normalize(text)
    return {key for key,pattern in QUALIFIERS if re.search(pattern,value)}

def category(item):
    if item["category"]=="stretching":return "movilidad"
    if item["category"] in ("cardio","plyometrics"):return "cardio"
    muscle=(item.get("primaryMuscles") or [""])[0]
    if muscle not in MUSCLES:raise ValueError("Músculo primario desconocido: "+muscle)
    return MUSCLES[muscle][0]

def format_spanish(item,number):
    mus=(item.get("primaryMuscles") or [""])[0]
    region=MUSCLES[mus][1]
    label=ES_NAMES.get(movement(item["name"]),"Movimiento de "+region)
    details=[ES_QUALIFIERS[q] for q in sorted(modifiers(item["name"]))]
    gear=EQUIPMENT[item["equipment"]][1]
    if details:label+=" "+", ".join(details)
    label+=" · "+gear
    # El identificador diferencia variaciones cuyo nombre requiere validación.
    return label+" · PD"+f"{number:04d}"

def tsv(name):
    with (LEGACY/name).open(encoding="utf-8",newline="") as f:
        return list(csv.DictReader(f,delimiter="\t"))

def write_tsv(name,columns,rows):
    DEST.mkdir(parents=True,exist_ok=True)
    p=DEST/name
    with p.open("w",encoding="utf-8",newline="") as f:
        w=csv.DictWriter(f,fieldnames=columns,delimiter="\t",lineterminator="\n")
        w.writeheader();w.writerows(rows)

def data_source():
    req=urllib.request.Request(DOWNLOAD,headers={"User-Agent":"Asterion-Independent-Public-Domain-Importer/1.0"})
    with urllib.request.urlopen(req,timeout=100) as stream:
        data=stream.read()
    check=hashlib.sha1(f"blob {len(data)}\0".encode()+data).hexdigest()
    if check!=UPSTREAM_BLOB:raise ValueError("La revisión de origen cambió: "+check)
    records=json.loads(data.decode("utf-8"))
    if len(records)!=EXPECTED_COUNT:raise ValueError("Inventario público incompleto")
    if len({x["id"] for x in records})!=len(records):
        raise ValueError("Duplicados del inventario público")
    return sorted(records,key=lambda x:x["id"])

def equivalence(external,originals,old_by_kind):
    if external["id"] in NAME_EXACT_OVERRIDES:
        oldid=NAME_EXACT_OVERRIDES[external["id"]]
        if oldid not in originals:raise ValueError("Equivalencia obsoleta "+oldid)
        return ("duplicado_verificado",oldid,"equivalencia_individual")
    kind=movement(external["name"])
    if kind is None:return ("nuevo_por_evaluar","","movimiento_sin_candidato")
    cat=category(external)
    equip=EQUIPMENT[external["equipment"]][0]
    newmods=modifiers(external["name"])
    candidates=[]
    for old in old_by_kind.get((cat,kind),[]):
        oldgear=set(old["equipment"].split("+"))
        if oldgear.issubset({"sin_equipo","colchoneta"}):oldgear.add("sin_equipo")
        if equip not in oldgear:continue
        oldmods=modifiers(old["name"])
        if len(newmods.symmetric_difference(oldmods))>1:continue
        candidates.append(old)
    if candidates:
        return ("posible_duplicado",candidates[0]["variant_id"],
                "familia_categoria_material_y_modificadores")
    return ("nuevo_por_evaluar","","sin_solapamiento_detectado")

def build(source_rows):
    original_families=tsv("families.tsv")
    original_types=tsv("types.tsv")
    original_variants=tsv("variants.tsv")
    original_volumes=tsv("volume_profiles.tsv")
    old_intensity=tsv("intensity.tsv")
    old_sports=tsv("sport_tags.tsv")
    old_sport_registry=tsv("sport_registry.tsv")
    if (len(original_families),len(original_types),len(original_variants),
        len(original_volumes))!=(155,474,1257,1257):
        raise ValueError("La línea base original cambió")
    fam={f["family_id"]:f for f in original_families}
    types={t["type_id"]:t for t in original_types}
    old_by_id={v["variant_id"]:v for v in original_variants}
    old_by_kind=defaultdict(list)
    for old in original_variants:
        typ=types[old["type_id"]]
        family=fam[typ["family_id"]]
        kind=movement(old["name"]) or movement(typ["name"])
        if kind:old_by_kind[(family["category"],kind)].append(old)
    counts=Counter()
    rows=[]
    extra=[]
    for index,item in enumerate(source_rows,1):
        required=("id","name","level","equipment","category","primaryMuscles")
        if any(not item.get(x) for x in required):raise ValueError("Registro incompleto")
        if item["equipment"] not in EQUIPMENT:raise ValueError("Material desconocido")
        if item["level"] not in ("beginner","intermediate","expert"):
            raise ValueError("Nivel fuente desconocido")
        result,match,reason=equivalence(item,old_by_id,old_by_kind)
        counts[result]+=1
        rows.append({
            "id_fuente_publico":item["id"],
            "nombre_fuente_ingles":item["name"],
            "categoria_asterion":category(item),
            "estado":result,
            "id_asterion_relacionado":match,
            "regla":reason
        })
        if result!="nuevo_por_evaluar":continue
        code="pd_e_"+f"{index:04d}"
        muscle=(item.get("primaryMuscles") or [""])[0]
        new={
            "id":code,"source_public_domain_id":item["id"],
            "nombre_es":format_spanish(item,index),
            "categoria":category(item),
            "region_es":MUSCLES[muscle][1],
            "material_es":EQUIPMENT[item["equipment"]][1],
            "material_cod":EQUIPMENT[item["equipment"]][0],
            "nivel_tecnico":"pendiente_revision",
            "nivel_fuente_orientativo":item["level"],
            "apto_para_rutinas_automaticas":False,
            "instrucciones_es":[],
            "descripcion_es":("Movimiento enfocado en "+MUSCLES[muscle][1]+
                               ". Material: "+EQUIPMENT[item["equipment"]][1]+
                               ". La técnica y la dificultad requieren revisión individual."),
            "animacion_propia":None,
            "origen":"dominio_publico"
        }
        extra.append(new)
    if sum(counts.values())!=EXPECTED_COUNT:
        raise ValueError("Inconsistencia en clasificación")
    if len(original_types)+len(extra)>1500:
        raise ValueError("Límite de tipos del motor excedido")
    families=list(original_families)
    for k,title in [
        ("tren_superior","Variantes adicionales de tren superior"),
        ("tren_central","Variantes adicionales de tren central"),
        ("tren_inferior","Variantes adicionales de tren inferior"),
        ("movilidad","Variantes adicionales de movilidad"),
        ("cardio","Variantes adicionales de cardio")
    ]:
        if any(v["categoria"]==k for v in extra):
            families.append({"family_id":"pd_"+k,"name":title,
                             "category":k,"pattern":"externo_pendiente"})
    newtypes=list(original_types)
    variants=list(original_variants)
    volumes=list(original_volumes)
    intensities=list(old_intensity)
    for e in extra:
        id=e["id"]
        newtypes.append({"type_id":"pd_t_"+id[5:],"family_id":"pd_"+e["categoria"],
                         "name":e["nombre_es"][:110]})
        variants.append({"variant_id":id,"type_id":"pd_t_"+id[5:],
            "name":e["nombre_es"][:115],"level":"0",
            "equipment":e["material_cod"],"execution":"consulta","supervision":"1"})
        intensities.append({"variant_id":id,"intensity":"alta"})
        unit="SECONDS" if e["categoria"]=="movilidad" else "REPETITIONS"
        volumes.append({"variant_id":id,"unit":unit,"initial_sets":"1",
          "initial_value":"10" if unit=="SECONDS" else "5","minimum_value":"1",
          "maximum_value":"180" if unit=="SECONDS" else "20","increment":"1"})
    write_tsv("families.tsv",["family_id","name","category","pattern"],families)
    write_tsv("types.tsv",["type_id","family_id","name"],newtypes)
    write_tsv("variants.tsv",
       ["variant_id","type_id","name","level","equipment","execution","supervision"],variants)
    write_tsv("volume_profiles.tsv",
       ["variant_id","unit","initial_sets","initial_value","minimum_value","maximum_value","increment"],volumes)
    write_tsv("intensity.tsv",["variant_id","intensity"],intensities)
    write_tsv("sport_registry.tsv",["code","name"],old_sport_registry)
    write_tsv("sport_tags.tsv",
       ["type_id","sports","goal","source_url","source_org","manual_only"],old_sports)
    AUDIT.parent.mkdir(parents=True,exist_ok=True)
    with AUDIT.open("w",encoding="utf-8",newline="") as f:
        writer=csv.DictWriter(f,fieldnames=list(rows[0]))
        writer.writeheader();writer.writerows(rows)
    # No se copia texto de instrucciones ni metadatos del dataset MIT anterior.
    # La vista utiliza sólo títulos originales en español elaborados para Asterion.
    index=[{
        "id":x["variant_id"],"nombre_es":x["name"],"origen":"asterion",
        "categoria":fam[types[x["type_id"]]["family_id"]]["category"],
        "nivel_tecnico":x["level"],"apto_para_rutinas_automaticas":False,
        "region_es":fam[types[x["type_id"]]["family_id"]]["name"],
        "material_es":x["equipment"].replace("_"," ").replace("+",", "),
        "instrucciones_es":[],"descripcion_es":"Ejercicio del catálogo histórico de Asterion.",
        "gif_incluido":False,"imagen_incluida":False
    } for x in original_variants]
    index.extend({**v,"gif_incluido":False,"imagen_incluida":False} for v in extra)
    if len({x["id"] for x in index})!=len(index):
        raise ValueError("Colisión de IDs al generar vista")
    VIEW.mkdir(parents=True,exist_ok=True)
    payload={"schema":"asterion.public-domain-unified.es.v2",
      "idioma":"es","originales":1257,"nuevos":len(extra),"total":len(index),
      "no_incluye_contenido_mit_previo":True,
      "multimedia_incluida":False,
      "ejercicios":index}
    (VIEW/"catalogo_es.json").write_text(
        json.dumps(payload,ensure_ascii=False,separators=(",",":")),encoding="utf-8")
    manifest={"schema":payload["schema"],"originales":1257,"nuevos":len(extra),
      "total":len(index),"candidatos_de_duplicado":counts["posible_duplicado"],
      "duplicados_verificados":counts["duplicado_verificado"],
      "registros_fuente_revisados":EXPECTED_COUNT,
      "origen_dominio_publico":UPSTREAM_URL,"revision_origen":UPSTREAM_COMMIT,
      "sin_datos_ni_gifs_del_repositorio_mit_anterior":True,
      "nuevos_bloqueados_hasta_revision_tecnica":len(extra)}
    (VIEW/"manifest.json").write_text(
        json.dumps(manifest,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
    print(json.dumps(manifest,ensure_ascii=False,indent=2))
    return manifest

def main():
    build(data_source())

if __name__=="__main__":main()
