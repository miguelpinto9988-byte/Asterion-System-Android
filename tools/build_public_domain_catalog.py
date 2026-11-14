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
from catalog_technical_es import technique
from pathlib import Path

ROOT=Path(__file__).resolve().parent.parent
LEGACY=ROOT/"progress-engine/src/main/resources/asterion_exercises/v1"
DEST=ROOT/"progress-engine/src/main/resources/asterion_exercises/v2"
VIEW=ROOT/"app/src/main/assets/unified_exercises/v2"
AUDIT=ROOT/"docs/datos/auditoria_dominio_publico.csv"
REVIEW_REPORT=ROOT/"docs/datos/revision_22_casos_cerrados.csv"
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
 "body only":("sin_equipo","peso corporal"),"none":("material_por_revisar","material por verificar"),
 "machine":("maquina","máquina"),"other":("material_especifico","material específico"),
 "foam roll":("rodillo","rodillo de espuma"),"kettlebells":("kettlebell","pesa rusa"),
 "dumbbell":("mancuerna","mancuernas"),"cable":("polea","polea"),
 "barbell":("barra","barra"),"bands":("banda","banda elástica"),
 "medicine ball":("balon","balón medicinal"),
 "exercise ball":("pelota","pelota de ejercicio"),
 "e-z curl bar":("barra","barra Z")
}
MOVEMENT_SPECIFIC=[
 ("circulos_tobillo",r"\bankle circles?\b"),
 ("circulos_brazo",r"\barm circles?\b"),
 ("circulos_codo",r"\belbow circles?\b"),
 ("elevacion_cadera",r"\bhip (?:lift|raise|thrust)\b"),
 ("balanceo_pesa",r"\bwindmill\b"),
 ("rodillo_abdominal",r"\bab roller\b|\bab rollout\b|\bbarbell rollout\b|\brollout\b"),
 ("encogimiento_hombros",r"\bshrugs?\b"),
 ("aduccion_cadera",r"\bhip adductions?\b|\badductor\b"),
 ("abduccion_cadera",r"\bhip abductions?\b|\babductor\b"),
 ("inclinacion_lateral",r"\bside bend\b"),
 ("pulover",r"\bpullovers?\b"),
 ("carrera_bicicleta",r"\bbicycling\b|\bstationary bike\b"),
 ("eliptica",r"\belliptical\b"),
 ("movilidad_espinal",r"\bcat stretch\b|\bchild'?s pose\b"),
 ("levantamiento_olimpico",r"\bclean\b|\bsnatch\b|\bjerk\b"),
 ("bicho_muerto",r"\bdead bug\b"),
 ("press_suelo",r"\bfloor press\b"),
 ("trabajo_tibial",r"\btibialis\b"),
 ("caminata_oso",r"\bbear crawl\b"),
 ("paso_carioca",r"\bcarioca\b"),
 ("salto_rana",r"\bfrog hops?\b"),
 ("puente_isquiotibiales",r"\bglute.ham raise\b"),
 ("flexion_muneca",r"\bfinger curls?\b|\bwrist curls?\b"),
 ("estiramiento",r"\bstretch\b|\bstretching\b"),
 ("empuje_poleas",r"\bcable crossover\b|\bchest push\b"),
 ("abdominal_corto",r"\bcrunch(?:es)?\b"),
 ("abdominal_situp",r"\bsit.up\b"),
 ("gemelos_rodillo",r"\bcalves.smr\b"),
 ("rodillo_muscular",r"\b.smr\b"),
 ("flexion_cadera",r"\bhip flexion\b"),
 ("giro_cadera",r"\bhip circles?\b"),
 ("circulos_rodilla",r"\bknee circles?\b"),
 ("caminata_rodillas",r"\bhug knees to chest\b"),
 ("plancha_abdominal",r"\bhanging pike\b"),
 ("pasos_rapidos",r"\bquick step\b|\bfast skipping\b"),
 ("giro_hombro",r"\bshoulder rotation\b|\binternal rotation\b|\bexternal rotation\b"),
 ("trabajo_mancuerna",r"\bscaption\b"),
 ("lanzamiento_balon",r"\b(?:ball throw|overhead throw|medicine ball throw)\b"),
 ("entrenamiento_equilibrio",r"\bbalance board\b"),
]
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
 ("rodillas_apoyadas",r"\b(?:on|from) knees\b|\b(?:rodillas apoyadas|con rodillas)\b"),
 ("banco",r"\bbench\b|\bbanco\b"),
 ("suelo",r"\bfloor\b|\bsuelo\b"),
 ("agarre_neutro",r"\bneutral grip\b|\bgrip neutro\b"),
 ("agarre_inverso",r"\breverse grip\b|\bgrip inverso\b"),
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
ES_NAMES.update({
 "circulos_tobillo":"Círculos de tobillo",
 "circulos_brazo":"Círculos de brazos",
 "circulos_codo":"Círculos de codos",
 "elevacion_cadera":"Elevación de cadera",
 "balanceo_pesa":"Molino con pesa rusa",
 "rodillo_abdominal":"Rodamiento abdominal",
 "encogimiento_hombros":"Encogimiento de hombros",
 "aduccion_cadera":"Aducción de cadera",
 "abduccion_cadera":"Abducción de cadera",
 "inclinacion_lateral":"Inclinación lateral",
 "pulover":"Pullover",
 "carrera_bicicleta":"Pedaleo",
 "eliptica":"Entrenamiento en elíptica",
 "movilidad_espinal":"Movilidad de columna",
 "levantamiento_olimpico":"Levantamiento técnico",
 "bicho_muerto":"Control abdominal tipo bicho muerto",
 "press_suelo":"Press en el suelo",
 "trabajo_tibial":"Trabajo tibial",
 "caminata_oso":"Desplazamiento de oso",
 "paso_carioca":"Paso cruzado carioca",
 "salto_rana":"Salto de rana",
 "puente_isquiotibiales":"Extensión de isquiotibiales",
 "flexion_muneca":"Flexión de muñeca",
 "empuje_poleas":"Cruce o empuje de poleas",
 "abdominal_corto":"Encogimiento abdominal",
 "abdominal_situp":"Incorporación abdominal",
 "gemelos_rodillo":"Automasaje de pantorrillas",
 "rodillo_muscular":"Automasaje con rodillo",
 "flexion_cadera":"Flexión de cadera",
 "giro_cadera":"Círculos de cadera",
 "circulos_rodilla":"Círculos de rodilla",
 "caminata_rodillas":"Flexión de rodillas hacia pecho",
 "plancha_abdominal":"Elevación abdominal en suspensión",
 "pasos_rapidos":"Pasos rápidos",
 "giro_hombro":"Rotación de hombro",
 "trabajo_mancuerna":"Elevación escapular",
 "lanzamiento_balon":"Lanzamiento con balón",
 "entrenamiento_equilibrio":"Ejercicio de equilibrio"
})
ES_QUALIFIERS={
 "inclinado":"inclinada","declinado":"declinada","frontal":"frontal",
 "posterior":"posterior","lateral":"lateral","sentado":"sentada",
 "de_pie":"de pie","acostado":"acostada","asistido":"asistida",
 "una_pierna":"a una pierna","un_brazo":"a un brazo",
 "agarre_ancho":"con agarre ancho","agarre_cerrado":"con agarre cerrado",
 "alternado":"alternada","isometrico":"isométrica",
 "rodillas_apoyadas":"con rodillas apoyadas","banco":"sobre banco",
 "suelo":"en el suelo","agarre_neutro":"con agarre neutro",
 "agarre_inverso":"con agarre inverso"
}
# No inventar correspondencias: las coincidencias dudosas se excluyen
# del motor y se conservan en el informe para verificación individual.
# Equivalencias evaluadas individualmente (nombres, gestos y equipamiento).
# Asterion conserva siempre su ID original. No se mezclan variantes genéricas
# con versiones frontales, asistidas o con apoyo distinto.
NAME_EXACT_OVERRIDES={
 "Bodyweight_Squat":"sentadilla_bilateral_v03",
 "Front_Barbell_Squat":"sentadilla_bilateral_v07",
 "Barbell_Hip_Thrust":"hip_thrust_v06",
 "External_Rotation_with_Band":"rotacion_externa_v02",
 "Lateral_Raise_-_With_Bands":"elevacion_lateral_v02",
 "Dumbbell_Step_Ups":"step_up_v04",
 # Mismo movimiento, material y posición en el inventario original.
 "Arnold_Dumbbell_Press":"press_hombros_v04",
 "Bent_Over_Barbell_Row":"dep_013_remo_con_flexion_de_tronco_barbell_row_v01",
 "Butt_Lift_Bridge":"puente_gluteo_v01",
 "Crunches":"crunch_v02",
 "Dumbbell_Bench_Press":"press_pecho_v04",
 "Exercise_Ball_Crunch":"crunch_v05",
 "Incline_Dumbbell_Press":"press_pecho_v05",
 "Leverage_Chest_Press":"press_pecho_v02",
 "Monster_Walk":"caminata_banda_v03",
 "Pallof_Press":"press_pallof_v04",
 "Plank":"plancha_frontal_v04",
 "Romanian_Deadlift":"peso_muerto_rumano_v06",
 "Seated_Leg_Curl":"curl_femoral_v04",
 "Side_Lateral_Raise":"elevacion_lateral_v03",
 "Single_Leg_Glute_Bridge":"puente_gluteo_v06",
}


# Auditoría individual de los 22 candidatos del comparador anterior.
# Cada decisión coteja la ficha técnica Unlicense, la variante de Asterion
# (nombre, equipo, postura, trayectoria) y el patrón mecánico. Cuando no hay
# equivalencia inequívoca se conserva la variante específica como ficha nueva
# no elegible para recomendaciones, sin afirmar que el gesto sea idéntico.
REVIEWED_22={
 "Barbell_Lunge":("nuevo_por_evaluar","zancada_estatica_v06",
   "Zancada con paso adelante y retorno frente a posición dividida estática"),
 "Bent-Arm_Dumbbell_Pullover":("nuevo_por_evaluar","dep_107_pullover_con_mancuerna_v01",
   "Pullover específico de codos flexionados; el original no fija ese ángulo"),
 "Cable_Crunch":("duplicado_verificado","crunch_v06",
   "Flexión abdominal arrodillada con polea alta y cuerda"),
 "Calf_Press":("nuevo_por_evaluar","dep_029_elevacion_de_talones_en_maquina_calf_r_v01",
   "Prensa de gemelos en máquina desde asiento, distinta de elevación de talones en máquina"),
 "Calf_Press_On_The_Leg_Press_Machine":("nuevo_por_evaluar","dep_029_elevacion_de_talones_en_maquina_calf_r_v01",
   "Empuje de plataforma en prensa de piernas, no máquina de gemelos convencional"),
 "Dead_Bug":("duplicado_verificado","dead_bug_v03",
   "Extensión alterna de una pierna con tronco y brazos estabilizados"),
 "Dumbbell_Lunges":("nuevo_por_evaluar","zancada_estatica_v05",
   "Zancada con paso y retorno, diferente de zancada estática con mancuernas"),
 "Dumbbell_Shoulder_Press":("duplicado_verificado","press_hombros_v02",
   "Press bilateral sentado en banco con respaldo y mancuernas"),
 "Kneeling_Cable_Crunch_With_Alternating_Oblique_Twists":("nuevo_por_evaluar","crunch_v06",
   "Añade rotaciones oblicuas alternas al crunch arrodillado"),
 "Leverage_High_Row":("nuevo_por_evaluar","remo_v03",
   "Remo alto desde asas superiores; trayectoria diferente de remo con apoyo de pecho"),
 "Leverage_Iso_Row":("nuevo_por_evaluar","remo_v03",
   "Remo independiente a altura de pecho, diferente de máquina de apoyo genérica"),
 "Low_Cable_Triceps_Extension":("nuevo_por_evaluar","extension_triceps_v03",
   "Extensión de tríceps supina con polea baja versus polea con cuerda en posición erguida"),
 "Rope_Crunch":("duplicado_verificado","crunch_v06",
   "Mismo crunch arrodillado con cuerda en polea alta"),
 "Rowing_Stationary":("duplicado_verificado","remo_ergometro_v02",
   "Remo cíclico en ergómetro; Asterion ya tiene la misma modalidad y patrón"),
 "Seated_Bent-Over_Two-Arm_Dumbbell_Triceps_Extension":("nuevo_por_evaluar","extension_triceps_v04",
   "Tronco inclinado en banco y brazos hacia atrás frente a extensión sentada genérica"),
 "Seated_Dumbbell_Inner_Biceps_Curl":("nuevo_por_evaluar","curl_biceps_v02",
   "Curl hacia fuera con rotación específica frente a curl sentado genérico"),
 "Seated_Dumbbell_Press":("duplicado_verificado","press_hombros_v02",
   "Press sentado con mancuernas sin rotación; no es press Arnold v04"),
 "Seated_Side_Lateral_Raise":("duplicado_verificado","elevacion_lateral_v04",
   "Elevación lateral sentada con mancuernas, variante exacta ya presente"),
 "Standing_Calf_Raises":("duplicado_verificado","dep_029_elevacion_de_talones_en_maquina_calf_r_v01",
   "Elevación de talones de pie en máquina de gemelos"),
 "Standing_Dumbbell_Triceps_Extension":("nuevo_por_evaluar","extension_triceps_v06",
   "Posición de pie y una mancuerna a dos manos; el original no confirma esa combinación"),
 "Stiff-Legged_Dumbbell_Deadlift":("nuevo_por_evaluar","peso_muerto_v03",
   "Bisagra con rodillas casi fijas frente a peso muerto con mancuerna genérico"),
 "Straight-Arm_Dumbbell_Pullover":("nuevo_por_evaluar","dep_107_pullover_con_mancuerna_v01",
   "Pullover específico de brazos extendidos, no equivalente al de codos flexionados"),
}

# Revisión semántica de fichas de dominio público: las siguientes
# NO son la variante original sugerida por el algoritmo (postura,
# dirección, equipamiento, patrón o técnica diferentes). La ficha sigue
# siendo provisional y NO apta para planes automáticos.
DISTINCT_BY_MECHANICS={
 "All_Fours_Quad_Stretch",
 "Alternate_Hammer_Curl",
 "Anti-Gravity_Press",
 "Back_Flyes_-_With_Bands",
 "Barbell_Deadlift",
 "Barbell_Glute_Bridge",
 "Barbell_Walking_Lunge",
 "Bent_Over_Two-Arm_Long_Bar_Row",
 "Bent_Over_Two-Dumbbell_Row",
 "Bent_Over_Two-Dumbbell_Row_With_Palms_In",
 "Board_Press",
 "Body_Tricep_Press",
 "Bosu_Ball_Cable_Crunch_With_Side_Bends",
 "Cable_Lying_Triceps_Extension",
 "Cable_Rope_Overhead_Triceps_Extension",
 "Cable_Seated_Crunch",
 "Clock_Push-Up",
 "Concentration_Curls",
 "Cross-Body_Crunch",
 "Cross_Body_Hammer_Curl",
 "Crunch_-_Hands_Overhead",
 "Crunch_-_Legs_On_Exercise_Ball",
 "Cuban_Press",
 "Deadlift_with_Bands",
 "Deadlift_with_Chains",
 "Deficit_Deadlift",
 "Dumbbell_Flyes",
 "Dumbbell_Squat",
 "Dumbbell_Tricep_Extension_-Pronated_Grip",
 "Elevated_Back_Lunge",
 "Flutter_Kicks",
 "Freehand_Jump_Squat",
 "Frog_Sit-Ups",
 "Glute_Kickback",
 "Gorilla_Chin_Crunch",
 "Hammer_Curls",
 "Handstand_Push-Ups",
 "Hip_Extension_with_Bands",
 "JM_Press",
 "Knee_Tuck_Jump",
 "Kneeling_Cable_Triceps_Extension",
 "Kneeling_High_Pulley_Row",
 "Low_Pulley_Row_To_Neck",
 "Lying_Cambered_Barbell_Row",
 "Lying_Dumbbell_Tricep_Extension",
 "Lying_T-Bar_Row",
 "Lying_Triceps_Press",
 "Neck_Press",
 "Oblique_Crunches",
 "Pallof_Press_With_Rotation",
 "Plie_Dumbbell_Squat",
 "Preacher_Curl",
 "Push-Up_Wide",
 "Push-Ups_-_Close_Triceps_Position",
 "Push-Ups_With_Feet_Elevated",
 "Push_Press",
 "Push_Press_-_Behind_the_Neck",
 "Push_Up_to_Side_Plank",
 "Rocket_Jump",
 "Romanian_Deadlift_from_Deficit",
 "Russian_Twist",
 "Scissors_Jump",
 "Seated_Calf_Raise",
 "Seated_Triceps_Press",
 "See-Saw_Press_Alternating_Side_Press",
 "Shotgun_Row",
 "Side_Bridge",
 "Side_Laterals_to_Front_Raise",
 "Smith_Machine_Bent_Over_Row",
 "Smith_Machine_Calf_Raise",
 "Smith_Machine_Upright_Row",
 "Spider_Curl",
 "Split_Jump",
 "Split_Squat_with_Dumbbells",
 "Standing_Alternating_Dumbbell_Press",
 "Standing_Barbell_Press_Behind_Neck",
 "Standing_Bent-Over_Two-Arm_Dumbbell_Triceps_Extension",
 "Standing_Bradford_Press",
 "Standing_Concentration_Curl",
 "Standing_Dumbbell_Press",
 "Standing_Dumbbell_Upright_Row",
 "Standing_Inner-Biceps_Curl",
 "Standing_Long_Jump",
 "Standing_Palms-In_Dumbbell_Press",
 "Standing_Rope_Crunch",
 "Star_Jump",
 "Stiff-Legged_Barbell_Deadlift",
 "Sumo_Deadlift",
 "Sumo_Deadlift_with_Bands",
 "Sumo_Deadlift_with_Chains",
 "T-Bar_Row_with_Handle",
 "Tate_Press",
 "Tuck_Crunch",
 "Two-Arm_Dumbbell_Preacher_Curl",
 "Upright_Barbell_Row",
 "Upright_Cable_Row",
 "Upright_Row_-_With_Bands",
 "Zottman_Preacher_Curl",
}

def normalize(text):
    norm=unicodedata.normalize("NFKD",text.lower())
    return "".join(c for c in norm if not unicodedata.combining(c))

def movement(text):
    value=normalize(text)
    for key,pattern in MOVEMENT_SPECIFIC+MOVEMENTS:
        if re.search(pattern,value):return key
    return None

def modifiers(text):
    value=normalize(text)
    return {key for key,pattern in QUALIFIERS if re.search(pattern,value)}

def category(item):
    if item["category"]=="stretching":return "movilidad"
    if item["category"] in ("cardio","plyometrics"):return "cardio"
    # Priorizar la clasificación del gesto en compuestos claros.
    kind=movement(item.get("name",""))
    if kind in ("sentadilla","zancada","peso_muerto",
                "puente","elevacion_cadera","subida_escalon"):
        return "tren_inferior"
    if kind in ("carrera_bicicleta","eliptica","pasos_rapidos"):
        return "cardio"
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
    if external["id"] in REVIEWED_22:
        decision,oldid,reason=REVIEWED_22[external["id"]]
        if oldid not in originals:
            raise ValueError("Variante antigua no encontrada: "+oldid)
        return (decision,oldid,"auditoria_individual: "+reason)
    if external["id"] in DISTINCT_BY_MECHANICS:
        return ("nuevo_por_evaluar","","diferente_por_descriptores_mecanicos")
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
        # Los calificadores estructurales no son intercambiables.
        critical={"inclinado","declinado","frontal","posterior","asistido",
                  "una_pierna","un_brazo","agarre_ancho","agarre_cerrado",
                  "agarre_neutro","agarre_inverso","rodillas_apoyadas",
                  "banco","suelo","isometrico"}
        if (newmods.symmetric_difference(oldmods)) & critical:continue
        if len(newmods.symmetric_difference(oldmods))>1:continue
        candidates.append(old)
    if candidates:
        return ("posible_duplicado",candidates[0]["variant_id"],
                "familia_categoria_material_y_modificadores")
    return ("nuevo_por_evaluar","","sin_solapamiento_detectado")

def build(source_rows):
    if len(source_rows)!=EXPECTED_COUNT:
        raise ValueError("El catálogo de origen no está completo")
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
        kind=movement(old["name"])
        if kind:old_by_kind[(family["category"],kind)].append(old)
    counts=Counter()
    rows=[]
    extra=[]
    for index,original in enumerate(source_rows,1):
        item={**original,"equipment":original.get("equipment") or "none"}
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
            "nombre_asterion_comparado":old_by_id[match]["name"] if match else "",
            "equipo_asterion_comparado":old_by_id[match]["equipment"] if match else "",
            "regla":reason,
            "fuente_unlicense":"https://github.com/yuhonas/free-exercise-db/blob/"
                +UPSTREAM_COMMIT+"/exercises/"+item["id"]+".json"
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
            "musculos_secundarios_es":[MUSCLES[m][1]
                for m in item.get("secondaryMuscles",[]) if m in MUSCLES],
            "patron_movimiento_es":{
                "push":"empuje","pull":"tracción","static":"estático"
            }.get(item.get("force"),"por determinar"),
            "mecanica_es":{
                "compound":"compuesto","isolation":"aislado"
            }.get(item.get("mechanic"),"por determinar"),
            "nombre_fuente_dominio_publico":item["name"],
            "animacion_propia_esperada":"animaciones/"+code+".gif",
            "apto_para_rutinas_automaticas":False,
            "instrucciones_es":[],
            "descripcion_es":("Movimiento enfocado en "+MUSCLES[muscle][1]+
                               ". Material: "+EQUIPMENT[item["equipment"]][1]+
                               ". La técnica y la dificultad requieren revisión individual."),
            "animacion_propia":None,
            "origen":"dominio_publico"
        }
        new.update(technique(item,movement(item["name"]),modifiers(item["name"]),
                             MUSCLES[muscle][1],EQUIPMENT[item["equipment"]][1]))
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
        source=source_rows[int(id[-4:])-1]
        unit=("SECONDS" if source["category"] in
              ("stretching","cardio","plyometrics") else "REPETITIONS")
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
    reviewed_rows=[x for x in rows if x["id_fuente_publico"] in REVIEWED_22]
    if len(reviewed_rows)!=22 or counts["posible_duplicado"]!=0:
        raise ValueError("Casos candidatos aún sin cerrar")
    with REVIEW_REPORT.open("w",encoding="utf-8",newline="") as f:
        writer=csv.DictWriter(f,fieldnames=list(rows[0]))
        writer.writeheader();writer.writerows(reviewed_rows)
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
      "nuevos_bloqueados_hasta_revision_tecnica":len(extra),
      "candidatos_del_lote_cerrados":len(REVIEWED_22),
      "duplicados_confirmados_en_lote":sum(
          x[0]=="duplicado_verificado" for x in REVIEWED_22.values()),
      "nuevas_variantes_diferenciadas_en_lote":sum(
          x[0]=="nuevo_por_evaluar" for x in REVIEWED_22.values()),
      "fichas_nuevas_con_guia_editorial_es":sum(
          bool(x["instrucciones_es"]) for x in extra),
      "fichas_nuevas_con_pasos_fuente_en":sum(
          bool(x["pasos_referencia_original_en"]) for x in extra),
      "guia_editorial_pendiente_revision_humana":True}
    (VIEW/"manifest.json").write_text(
        json.dumps(manifest,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
    print(json.dumps(manifest,ensure_ascii=False,indent=2))
    return manifest

def main():
    build(data_source())

if __name__=="__main__":main()
