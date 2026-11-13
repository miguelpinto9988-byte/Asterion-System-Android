#!/usr/bin/env python3
"""Unifica Asterion original con datos MIT en español: original tiene prioridad.
Conservador: los candidatos a duplicado se retienen para revisión, no se
añaden a los ejercicios automáticos ni se redistribuyen medios de terceros.
"""
import csv
import json
import re
import unicodedata
from collections import Counter, defaultdict
from pathlib import Path

ROOT=Path(__file__).resolve().parent.parent
LEGACY=ROOT/"progress-engine/src/main/resources/asterion_exercises/v1"
SOURCE=ROOT/"app/src/main/assets/external_exercises/v1/ejercicios_es.json"
DEST=ROOT/"app/src/main/assets/unified_exercises/v1"
REPORT=ROOT/"docs/datos/comparacion_catalogos_es.csv"

# Equivalencias confirmadas por nombre de movimiento, material y variante.
# Estas son vinculaciones específicas, no coincidencias aproximadas automáticas.
CONFIRMED={
    "ext_0025":"press_pecho_v06",       # Press de pecho con barra
    "ext_0032":"peso_muerto_v06",        # Peso muerto convencional con barra
    "ext_0978":"elevacion_frontal_v02",  # Elevación frontal con banda
    "ext_0334":"elevacion_lateral_v03",  # Elevación lateral con mancuernas
    "ext_0970":"dominadas_v03",          # Dominada asistida con banda
}
EQUIPMENT={
    "body weight":"sin_equipo","dumbbell":"mancuerna",
    "barbell":"barra","olympic barbell":"barra","ez barbell":"barra",
    "cable":"polea","band":"banda","resistance band":"banda",
    "leverage machine":"maquina","smith machine":"maquina",
    "kettlebell":"kettlebell","stability ball":"pelota",
    "medicine ball":"balon","assisted":"sin_equipo",
    "rope":"cuerda_salto","weighted":"sin_equipo",
}
# Incluye solo movimientos identificables; ninguno se equipara por región
# anatómica o por la palabra genérica "ejercicio".
MOVEMENTS=[
 ("sentadilla",r"\bsquat|\bsentadilla|\bcossack"),
 ("zancada",r"\blunge|\bzancada|\bdesplante"),
 ("peso_muerto",r"\bdeadlift|\bpeso muerto|\bbisagra de cadera"),
 ("puente",r"\bglute bridge|\bhip thrust|\bpuente de glute|\belevacion de cadera"),
 ("talones",r"\bcalf raise|\bcalf press|\belevacion de talones|\bplantiflexion"),
 ("curl_femoral",r"\bleg curl|\bcurl femoral|\bflexion de rodilla"),
 ("extension_rodilla",r"\bleg extension|\bextension de rodilla|\bextension de cuadriceps"),
 ("flexion_brazos",r"\bpush.up|\bflexion de brazos|\bflexion inclinada|\bflexion en pared|\bflexion de agarre"),
 ("press_pecho",r"\bbench press|\bchest press|\bpress de pecho"),
 ("press_hombros",r"\bshoulder press|\boverhead press|\bmilitary press|\bpress de hombros"),
 ("dominada",r"\bpull.up|\bchin.up|\bdominada"),
 ("jalon",r"\bpull.down|\bjalon"),
 ("remo",r"\brow(?:ing)?\b|\bremo"),
 ("curl_biceps",r"\bbiceps curl|\bhammer curl|\bpreacher curl|\bcurl de biceps|\bcurl martillo"),
 ("extension_triceps",r"\btriceps extension|\bpush.down|\bextension de triceps|\bjalon de triceps"),
 ("fondo",r"\bdip\b|\bfondo"),
 ("apertura",r"\bchest fly|\bflye|\bpec deck|\bapertura"),
 ("elevacion_lateral",r"\blateral raise|\belevacion lateral"),
 ("elevacion_frontal",r"\bfront raise|\belevacion frontal"),
 ("encogimiento",r"\bshrug|\bencogimiento"),
 ("plancha",r"\bplank|\bplancha"),
 ("crunch",r"\bcrunch|\bencogimiento abdominal"),
 ("abdominal",r"\bsit.up|\babdominal(?:es)?\b"),
 ("elevacion_pierna",r"\bleg raise|\bhip raise|\belevacion de piernas"),
 ("giro",r"\btorso twist|\brussian twist|\btwist|\bgiro de tronco"),
 ("abduccion",r"\bhip abduction|\babduccion"),
 ("aduccion",r"\bhip adduction|\baduccion"),
 ("estiramiento",r"\bstretch|\bestiramiento"),
 ("movilidad",r"\bmobility|\bmovilidad"),
 ("caminata",r"\bwalk|\bcaminata|\bmarcha"),
 ("carrera",r"\brun(?:ning)?|\btrote|\bcarrera"),
 ("salto",r"\bjump|\bsalto"),
 ("burpee",r"\bburpee"),
 ("rotacion",r"\brotation|\brotacion"),
 ("patada",r"\bkick|\bpatada"),
 ("bicicleta",r"\bbicycle|\bbicicleta"),
]
QUALIFIERS=[
 ("inclinada",r"\binclin|\bincline"),
 ("declinada",r"\bdeclin|\bdecline"),
 ("frontal",r"\bfrontal|\bfront\b"),
 ("trasera",r"\breverse|\bposterior|\batras|\bbehind"),
 ("lateral",r"\blateral|\bside"),
 ("sentado",r"\bseated|\bsentad"),
 ("de_pie",r"\bstanding|\bde pie"),
 ("tumbado",r"\blying|\btumbad|\bsupin"),
 ("rodillas",r"\bkneeling|\barrodillad"),
 ("unilateral",r"\bone.arm|\bone.leg|\bsingle.arm|\bsingle.leg|\bunilateral|\buna mano|\bun brazo"),
 ("agarre_ancho",r"\bwide.grip|\bgrip ancho"),
 ("agarre_estrecho",r"\bnarrow.grip|\bclose.grip|\bgrip estrecho|\bgrip cerrado"),
 ("pausa",r"\bpause|\bpausa"),
 ("asistido",r"\bassisted|\basistid"),
 ("alterno",r"\balternat|\baltern"),
 ("isometrico",r"\bisometric|\bisometric"),
]
CATEGORIES={"tren_superior","tren_central","tren_inferior","movilidad","cardio"}

def ascii_name(value):
    return "".join(c for c in unicodedata.normalize("NFKD",value.lower())
                   if not unicodedata.combining(c))

def movement(name):
    norm=ascii_name(name)
    return next((key for key,pattern in MOVEMENTS
                 if re.search(pattern,norm)),None)

def qualifiers(name):
    norm=ascii_name(name)
    return {key for key,pattern in QUALIFIERS
            if re.search(pattern,norm)}

def read_tsv(name):
    with (LEGACY/name).open(encoding="utf-8",newline="") as stream:
        return list(csv.DictReader(stream,delimiter="\t"))

def assess(external,old_by_movement):
    id=external["id"]
    if id in CONFIRMED:
        return ("duplicado_confirmado",CONFIRMED[id],"equivalencia_revisada")
    family=movement(external["nombre_original"])
    gear=EQUIPMENT.get(external["material_original"])
    if family is None or gear is None:
        return ("nuevo_provisional","","sin_correspondencia_verificable")
    cat=external["categoria"]
    qnew=qualifiers(external["nombre_original"])
    for original in old_by_movement.get((cat,family),[]):
        old_gear=set(original["equipment"].split("+"))
        if any(x in old_gear for x in ("sin_equipo","colchoneta","pared","silla")):
            old_gear.add("sin_equipo")
        if gear not in old_gear:continue
        qold=qualifiers(original["name"])
        if len(qnew.symmetric_difference(qold))<=1:
            # No afirmar duplicado por un patrón similar; necesita revisión.
            return ("posible_duplicado",original["variant_id"],
                    "misma_categoria_movimiento_material")
    return ("nuevo_provisional","","sin_duplicado_verificado")

def generate():
    external=json.loads(SOURCE.read_text(encoding="utf-8"))
    newer=external["ejercicios"]
    families={r["family_id"]:r for r in read_tsv("families.tsv")}
    types={r["type_id"]:r for r in read_tsv("types.tsv")}
    originals=read_tsv("variants.tsv")
    if len(originals)!=1257 or len(newer)!=1324:
        raise ValueError("Catálogos incompletos; revisión manual requerida")
    by_id={r["variant_id"]:r for r in originals}
    by_movement=defaultdict(list)
    original_entries=[]
    for row in originals:
        typ=types[row["type_id"]]
        category=families[typ["family_id"]]["category"]
        if category not in CATEGORIES:raise ValueError("Categoría original no válida")
        mov=movement(row["name"]) or movement(typ["name"])
        if mov:by_movement[(category,mov)].append(row)
        original_entries.append({
            "id":row["variant_id"],
            "origen":"asterion",
            "nombre_es":row["name"],
            "nombre_original":row["name"],
            "categoria":category,
            "region_es":families[typ["family_id"]]["name"],
            "material_es":row["equipment"].replace("_"," ").replace("+",", "),
            "instrucciones_es":[],
            "nivel_tecnico":row["level"],
            "apto_para_rutinas_automaticas":False,
            "gif_incluido":False,
            "imagen_incluida":False,
        })
    # Datos externos se conservan dentro del repositorio y se incorporan al
    # buscador unificado SOLO si no hay duplicado plausible.
    labels=Counter()
    reports=[]
    unique=[]
    for x in newer:
        decision,match,reason=assess(x,by_movement)
        if decision=="duplicado_confirmado":
            expected=by_id.get(match)
            if not expected:raise ValueError("ID antiguo ya no existe: "+match)
            # Las 5 equivalencias confirmadas conservan siempre Asterion.
        elif decision=="nuevo_provisional":
            entry=dict(x)
            entry["origen"]="dataset_mit"
            entry["nivel_tecnico"]="pendiente_revision"
            entry["apto_para_rutinas_automaticas"]=False
            entry["gif_incluido"]=False
            entry["imagen_incluida"]=False
            unique.append(entry)
        labels[decision]+=1
        reports.append({
            "id_externo":x["id"],"nombre_origen":x["nombre_original"],
            "categoria":x["categoria"],"decision":decision,
            "id_original_relacionado":match,"motivo":reason
        })
    total=original_entries+unique
    ids=[r["id"] for r in total]
    if len(set(ids))!=len(ids):raise ValueError("ID duplicado en el catálogo unificado")
    if sum(labels.values())!=1324:raise ValueError("No se cubrió todo el catálogo")
    result={
        "schema":"asterion.unificado.es.v1",
        "idioma":"es",
        "politica":"Asterion tiene prioridad; duplicados potenciales en revisión",
        "total":len(total),
        "originales":1257,
        "nuevos_provisionales":len(unique),
        "duplicados_confirmados":labels["duplicado_confirmado"],
        "posibles_duplicados_en_revision":labels["posible_duplicado"],
        "multimedia_incluida":False,
        "fuente_nueva":"https://github.com/hasaneyldrm/exercises-dataset",
        "licencia_datos":"MIT; terceros con derechos reservados en medios",
        "ejercicios":total
    }
    DEST.mkdir(parents=True,exist_ok=True)
    (DEST/"catalogo_es.json").write_text(
        json.dumps(result,ensure_ascii=False,separators=(",",":")),encoding="utf-8")
    REPORT.parent.mkdir(parents=True,exist_ok=True)
    with REPORT.open("w",encoding="utf-8",newline="") as output:
        writer=csv.DictWriter(output,fieldnames=list(reports[0].keys()))
        writer.writeheader()
        writer.writerows(reports)
    manifest={k:v for k,v in result.items() if k!="ejercicios"}
    (DEST/"manifest.json").write_text(
        json.dumps(manifest,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
    print(json.dumps(manifest,ensure_ascii=False,indent=2))
    return manifest

if __name__=="__main__":
    generate()
