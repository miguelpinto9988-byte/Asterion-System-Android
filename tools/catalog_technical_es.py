"""Ficha técnica editorial en español para datos Unlicense de free-exercise-db.

El material original (inglés) tiene pasos por ejercicio; se incorpora en
campo de referencia para revisión, no como traducción humana certificada.
Las pautas españolas describen patrones generales, nunca prescriben
cargas/repeticiones y no habilitan rutinas automáticamente.
"""
from __future__ import annotations
import re

# Pautas del gesto anatómico por familia; NO aplicables automáticamente a
# variaciones complejas de halterofilia, strongman o equipamiento especializado.
GESTURES={
    "sentadilla":"Coordina la flexión de caderas y rodillas manteniendo apoyo estable en los pies; vuelve de manera controlada.",
    "zancada":"Desplaza una pierna según la variante y flexiona cadera y rodilla sin perder el equilibrio; recupera la posición de partida.",
    "peso_muerto":"El movimiento se organiza alrededor de la bisagra de cadera; conserva el tronco controlado y evita tirones del material.",
    "puente":"Con apoyo estable de pies y tronco, eleva la cadera de forma controlada y desciende sin impulso.",
    "elevacion_cadera":"Controla el movimiento de la cadera manteniendo estables los puntos de apoyo previstos.",
    "subida_escalon":"Apoya el pie sobre una superficie estable, traslada el cuerpo de forma controlada y desciende manteniendo el equilibrio.",
    "flexion_brazos":"Con manos y tronco bien apoyados, flexiona los codos y vuelve a extenderlos de forma controlada.",
    "dominada":"El gesto combina una tracción de brazos con control de los omóplatos; evita balanceos bruscos.",
    "jalon":"Acerca el agarre hacia el torso con una tracción controlada, sin tirar del cuello ni balancearte.",
    "press_pecho":"El gesto es un empuje de brazos desde el pecho; controla la trayectoria y el retorno del implemento.",
    "press_hombros":"Eleva los brazos mediante un empuje por encima de los hombros en un recorrido cómodo; controla el descenso.",
    "remo":"Acerca el agarre al tronco coordinando codos y omóplatos; devuelve el agarre sin sacudidas.",
    "curl_biceps":"Flexiona los codos con los brazos superiores estables; controla también el movimiento de retorno.",
    "curl":"Identifica la articulación que se flexiona y realiza el movimiento de forma controlada, sin balancear el tronco.",
    "extension_triceps":"Extiende los codos manteniendo controlados los brazos superiores y la posición del tronco.",
    "extension_rodilla":"Extiende y flexiona la rodilla de manera controlada utilizando el apoyo indicado por el equipo.",
    "elevacion_lateral":"Eleva lateralmente los brazos en un rango cómodo sin compensar con un balanceo del tronco.",
    "elevacion_frontal":"Eleva los brazos hacia el frente de forma controlada y desciéndelos sin impulso.",
    "elevacion_pierna":"Controla la elevación de piernas con el abdomen estable, evitando compensar arqueando la espalda.",
    "apertura":"Abre y aproxima los brazos siguiendo una trayectoria controlada y sin forzar el hombro.",
    "pantorrillas":"Controla el ascenso y descenso del talón mediante el tobillo, sin rebotes.",
    "plancha":"Mantén una alineación corporal estable con respiración natural; evita hundir la zona lumbar.",
    "abdominal":"Controla la flexión del tronco con los músculos del abdomen, sin tirar del cuello.",
    "abdominal_corto":"Realiza una flexión corta del tronco desde el abdomen sin tirar de la cabeza.",
    "abdominal_situp":"Coordina la incorporación del tronco evitando impulsos repentinos y movimientos cervicales forzados.",
    "estiramiento":"Adopta una posición estable para explorar la movilidad del grupo indicado sin dolor ni rebotes.",
    "caminata":"Coordina los pasos con postura cómoda y estable, ajustando el movimiento al entorno.",
    "carrera":"La técnica varía con el terreno y la experiencia; evita cambios bruscos de ritmo.",
    "salto":"La acción combina impulso y aterrizaje; la técnica de absorción del apoyo requiere evaluación.",
    "rotacion":"Controla el giro alrededor de la articulación implicada sin alcanzar extremos dolorosos.",
    "fondos":"El movimiento implica flexión y extensión de codos con apoyo estable; protege el recorrido del hombro.",
    "giro":"Gira el tronco o la articulación indicada sin forzar el rango ni realizar sacudidas.",
    "patada":"Controla el desplazamiento de la pierna sin perder la alineación ni golpear superficies.",
    "press":"Realiza el empuje con el equipo apropiado controlando su trayectoria y el retorno.",
    "extension":"Controla la extensión de la articulación principal evitando bloqueos o impulsos.",
    "elevacion":"Eleva el segmento corporal o implemento según su recorrido técnico y vuelve con control.",
    "remada":"Controla la tracción con el tronco estable y una posición cómoda de los hombros.",
    "circulos_tobillo":"Traza círculos suaves con el tobillo evitando forzar la articulación.",
    "circulos_brazo":"Mueve los brazos en círculos de amplitud cómoda manteniendo el tronco estable.",
    "circulos_codo":"Explora la movilidad del codo de forma suave y sin forzar la articulación.",
    "balanceo_pesa":"El movimiento combina cadera, tronco y control del implemento; requiere verificación técnica antes de practicarlo.",
    "rodillo_abdominal":"El rodamiento requiere control del tronco y del rango de hombro; debe revisarse antes de recomendarlo.",
    "encogimiento_hombros":"Eleva y desciende los hombros con un movimiento corto, sin rotaciones bruscas.",
    "aduccion_cadera":"Acerca la pierna hacia la línea media de forma controlada y con apoyo estable.",
    "abduccion_cadera":"Separa la pierna de la línea media de manera controlada manteniendo estables los apoyos.",
    "inclinacion_lateral":"Inclina suavemente el tronco hacia un lado manteniendo controlado el movimiento.",
    "pulover":"Controla el recorrido de brazos y hombros con el implemento estabilizado; no fuerces el rango.",
    "carrera_bicicleta":"Coordina el pedaleo con una postura cómoda y el equipo correctamente ajustado.",
    "eliptica":"Mantén un movimiento alterno fluido, con apoyos estables y control del equipo.",
    "movilidad_espinal":"Explora la movilidad de la columna dentro de un recorrido cómodo y sin rebotes.",
    "bicho_muerto":"Mantén el abdomen estable mientras alternas el movimiento de extremidades sin arquear la zona lumbar.",
    "press_suelo":"Empuja desde el apoyo en el suelo y controla el descenso, cuidando la posición del hombro.",
    "trabajo_tibial":"Controla los movimientos del tobillo sin compensar con balanceos del tronco.",
    "caminata_oso":"Coordina el apoyo de manos y pies manteniendo control del tronco durante el desplazamiento.",
    "paso_carioca":"Coordina los cruces laterales de pies y el equilibrio, sin cambios bruscos de dirección.",
    "salto_rana":"Este patrón combina flexión y salto; la técnica de impulso y aterrizaje requiere revisión.",
    "puente_isquiotibiales":"Controla la elevación de cadera y la flexión de rodilla según la variante, sin movimientos bruscos.",
    "flexion_muneca":"Flexiona y devuelve la muñeca de forma controlada, sin forzar el rango.",
    "empuje_poleas":"Controla la trayectoria de los brazos y los cables con el torso estable.",
    "gemelos_rodillo":"Desplaza suavemente el rodillo por el grupo muscular previsto sin presionar zonas dolorosas.",
    "rodillo_muscular":"Usa el rodillo con movimientos lentos y controlados, evitando articulaciones y zonas dolorosas.",
    "flexion_cadera":"Eleva la pierna mediante flexión de cadera con postura estable.",
    "giro_cadera":"Realiza círculos de cadera dentro de un rango cómodo con equilibrio.",
    "circulos_rodilla":"Moviliza suavemente la rodilla sin forzar articulaciones ni rangos extremos.",
    "caminata_rodillas":"Controla la flexión y elevación alterna de rodillas durante el desplazamiento.",
    "plancha_abdominal":"El movimiento combina apoyo o suspensión con control abdominal; requiere comprobación técnica.",
    "pasos_rapidos":"Coordina los cambios de apoyo en un espacio despejado, manteniendo equilibrio.",
    "giro_hombro":"Controla la rotación de hombro sin forzar el rango disponible.",
    "trabajo_mancuerna":"Mantén el brazo y el tronco controlados al desplazar el implemento.",
    "lanzamiento_balon":"La trayectoria del balón y el entorno requieren supervisión y un espacio despejado.",
    "entrenamiento_equilibrio":"Mantén un apoyo estable o cercano y prioriza el control postural.",
}
SPECIALIZED_CATEGORIES={"olympic weightlifting","strongman","powerlifting"}
TAG_TEXT={
    "sentado":"Realiza la variante con apoyo sentado y el banco o asiento estable.",
    "de_pie":"Mantén un apoyo estable sobre ambos pies mientras corresponda.",
    "acostado":"Verifica un apoyo cómodo y estable de espalda, cabeza y piernas.",
    "banco":"Comprueba que el banco no se desplace y que los apoyos estén estables.",
    "suelo":"Verifica que la superficie de apoyo sea firme y no resbaladiza.",
    "una_pierna":"La variante implica un apoyo o movimiento unilateral; revisa el equilibrio.",
    "un_brazo":"La variante implica un brazo; evita compensar con el tronco.",
    "asistido":"Utiliza el soporte previsto para conservar estabilidad y control.",
    "isometrico":"Se prioriza mantener una posición con respiración natural y sin dolor.",
    "rodillas_apoyadas":"Mantén las rodillas apoyadas sobre una superficie adecuada.",
    "alternado":"Alterna los lados sin modificar bruscamente la postura.",
    "agarre_ancho":"Comprueba una posición cómoda de los hombros antes de utilizar el agarre.",
    "agarre_cerrado":"Comprueba que el agarre no fuerce las muñecas.",
}
# Estos cinco IDs carecen de pasos en la fuente Unlicense. La diferencia
# entre descripción factual y demostración técnica queda explícita.
SOURCE_WITHOUT_STEPS={
    "Iron_Cross":(
        "Identidad ambigua: la fuente registra mancuernas, pero el nombre "
        "también se usa para un elemento gimnástico distinto.",
        "identidad_ambigua_y_sin_instrucciones"),
    "One-Arm_Kettlebell_Swings":(
        "Patrón unilateral y dinámico con pesa rusa: requiere revisar "
        "trayectoria, estabilidad y seguridad del implemento.",
        "balistico_unilateral_sin_instrucciones"),
    "Push_Press":(
        "Movimiento técnico de levantamiento por encima de la cabeza "
        "con barra: requiere supervisión especializada.",
        "levantamiento_tecnico_sin_instrucciones"),
    "Side_Bridge":(
        "Ejercicio estático lateral: debe confirmarse la postura y el "
        "tipo de apoyo antes de fijar la demostración.",
        "estatico_sin_instrucciones"),
    "Side_Jackknife":(
        "La variante exacta de flexión lateral necesita identificarse "
        "antes de crear pasos o animaciones.",
        "variante_ambigua_sin_instrucciones"),
}

def technique(item, movement_key, qualifiers, muscle_es, material_es):
    """Produces editorial orientation and preserves unmodified Unlicense steps.

    It does NOT certify the movement. Expert/weightlifting/strongman entries
    get descriptors only, no prescriptive technique sequence.
    """
    english=item.get("instructions") or []
    english=[re.sub(r"<[^>]*>","",s).strip() for s in english
             if isinstance(s,str) and s.strip()]
    category=item.get("category","")
    advanced=(category in SPECIALIZED_CATEGORIES or item.get("level")=="expert")
    intro=f"Identifica el movimiento y verifica el material indicado: {material_es}. El grupo principal descrito es {muscle_es}."
    if item["id"] in SOURCE_WITHOUT_STEPS and not english:
        description,reason=SOURCE_WITHOUT_STEPS[item["id"]]
        steps=[
            intro,
            description,
            "La fuente no aporta instrucciones de ejecución para esta "
            "variante. No generar demostración instructiva ni activarla "
            "en rutinas hasta una verificación técnica individual."
        ]
    elif advanced:
        steps=[
            intro,
            "Es un patrón técnico especializado. Sus posiciones, agarres y trayectorias necesitan una demostración y revisión profesional individual.",
            "No se proporciona una secuencia práctica autónoma hasta disponer de una ficha técnica validada."
        ]
    else:
        hint=GESTURES.get(movement_key)
        steps=[intro]
        if hint:
            steps.append(hint)
        else:
            steps.append("La trayectoria y postura específicas están pendientes de revisión: consulta la referencia técnica de dominio público asociada a esta ficha.")
        for key in ("sentado","de_pie","acostado","banco","suelo",
                    "una_pierna","un_brazo","asistido","isometrico",
                    "rodillas_apoyadas","alternado"):
            if key in qualifiers:
                steps.append(TAG_TEXT[key])
                break
        steps.append("Controla el movimiento sin rebotes ni dolor; detén la actividad si no puedes mantener una postura estable.")
    return {
       "instrucciones_es":steps,
       "instrucciones_es_estado":"orientacion_editorial_no_validada",
       "pasos_referencia_original_en":english,
       "fuente_tecnica":"https://github.com/yuhonas/free-exercise-db",
       "fuente_tecnica_licencia":"Unlicense",
       "fuente_tecnica_id":item["id"],
       "pasos_fuente_cantidad":len(english),
       "revision_tecnica_individual":False,
       "fuente_sin_pasos":not bool(english),
       "razon_bloqueo_es":(
           SOURCE_WITHOUT_STEPS[item["id"]][1] if
           item["id"] in SOURCE_WITHOUT_STEPS and not english else
           "pendiente_revision_tecnica_individual"),
    }
