#!/usr/bin/env python3
"""Auditoría de preparación editorial y técnica para cada ID Asterion v2.

IMPORTANTE: cobertura de datos != validación técnica por un profesional.
No otorga aptitud para entrenamientos, cargas, progresión ni GIF publicados.
"""
from __future__ import annotations
import csv
import json
from collections import Counter
from pathlib import Path

ROOT=Path(__file__).resolve().parent.parent
CATALOG=ROOT/"app/src/main/assets/unified_exercises/v2/catalogo_es.json"
MANIFEST=ROOT/"app/src/main/assets/unified_exercises/v2/manifest.json"
OUTPUT=ROOT/"docs/datos/estado_validacion_tecnica_v2.csv"
SUMMARY=ROOT/"docs/datos/resumen_validacion_tecnica_v2.json"
ANIMATIONS=ROOT/"docs/datos/preparacion_animaciones_originales_v2.csv"
REVIEW_REGISTER=ROOT/"docs/datos/revisiones_tecnicas_individuales.csv"
FIELDS=("id","nombre_es","categoria","procedencia","nivel_declarado",
        "pasos_es","pasos_original_en","fuente_individual",
        "estado_documentacion","estado_validacion_profesional",
        "riesgo_revision","animacion_propia","habilitado_para_rutinas",
        "siguiente_accion")
REVIEW_FIELDS=("id","revisor","credencial_verificada_por","fecha_iso",
               "fuente_contrastada","pasos_es_revisados_url",
               "postura_verificada","material_verificado",
               "variantes_verificadas","riesgos_verificados",
               "unidad_volumen_verificada","dictamen")
CATEGORIES={"tren_superior","tren_central","tren_inferior","movilidad","cardio"}
SOURCE_BASE=("https://github.com/yuhonas/free-exercise-db/blob/"
    "f00c92c7dcf1216a928a52c3706c7ce8e2f71ed5/exercises/")

def write_csv(path,fields,rows):
    path.parent.mkdir(parents=True,exist_ok=True)
    with path.open("w",encoding="utf-8",newline="") as file:
        writer=csv.DictWriter(file,fieldnames=fields)
        writer.writeheader()
        writer.writerows(rows)

def load_approved_register(allowed_ids):
    """Only accepts a signed-off documentary record, never invents one."""
    if not REVIEW_REGISTER.exists():
        write_csv(REVIEW_REGISTER,REVIEW_FIELDS,[])
        return {}
    with REVIEW_REGISTER.open(encoding="utf-8",newline="") as file:
        reader=csv.DictReader(file)
        if tuple(reader.fieldnames or ())!=REVIEW_FIELDS:
            raise ValueError("Registro de revisiones con columnas incorrectas")
        approved={}
        for entry in reader:
            id=entry["id"]
            if id not in allowed_ids or id in approved:
                raise ValueError("Revisión referida a ID inválido/duplicado: "+id)
            if entry["dictamen"]!="aprobado":
                continue
            for field in REVIEW_FIELDS:
                if not entry.get(field,"").strip():
                    raise ValueError("Revisión sin evidencia: "+id+" / "+field)
            if entry["fecha_iso"][:4]<"2026":
                raise ValueError("Fecha de revisión incorrecta: "+id)
            # El control de credenciales debe realizarse FUERA del generador.
            # Estos metadatos prueban trazabilidad formal, no competencia clínica.
            approved[id]=entry
    return approved

def generate():
    data=json.loads(CATALOG.read_text(encoding="utf-8"))
    manifest=json.loads(MANIFEST.read_text(encoding="utf-8"))
    entries=data["ejercicios"]
    if data["schema"]!="asterion.public-domain-unified.es.v2":
        raise ValueError("Esquema distinto de catálogo v2")
    if len(entries)!=2104 or manifest["total"]!=len(entries):
        raise ValueError("Se esperaban 2104 registros v2")
    ids=[x["id"] for x in entries]
    if len(set(ids))!=len(ids):
        raise ValueError("IDs repetidos")
    verified=load_approved_register(set(ids))
    counts=Counter()
    rows=[]
    animations=[]
    source_missing=[]
    for x in entries:
        id=x["id"]
        origin=x["origen"]
        if origin not in ("asterion","dominio_publico"):
            raise ValueError("Origen no reconocido: "+id)
        if x["categoria"] not in CATEGORIES or not x.get("nombre_es"):
            raise ValueError("Datos mínimos ausentes: "+id)
        newer=origin=="dominio_publico"
        es=x.get("instrucciones_es") or []
        source=x.get("pasos_referencia_original_en") or []
        if not isinstance(es,list) or not isinstance(source,list):
            raise ValueError("Pasos inválidos: "+id)
        if newer:
            if x.get("revision_tecnica_individual") is not False:
                raise ValueError("Datos de nueva ficha con revisión ficticia: "+id)
            if x.get("nivel_tecnico")!="pendiente_revision":
                raise ValueError("Nivel nuevo no validado: "+id)
            if x.get("apto_para_rutinas_automaticas"):
                raise ValueError("Variante nueva elegible sin revisión: "+id)
            if x.get("instrucciones_es_estado")!="orientacion_editorial_no_validada":
                raise ValueError("Instrucciones nuevas sin cautela: "+id)
            if len(es)<3:
                raise ValueError("Ficha española sin orientación mínima: "+id)
            if len(source)!=x.get("pasos_fuente_cantidad"):
                raise ValueError("Conteo de pasos de origen incorrecto: "+id)
            if not x.get("source_public_domain_id"):
                raise ValueError("Sin ID de procedencia: "+id)
            source_url=SOURCE_BASE+x["source_public_domain_id"]+".json"
            if source:
                status="fuente_ingles_pendiente_traduccion_y_revision"
            else:
                status="falta_fuente_con_pasos_individuales"
                source_missing.append(id)
            specialized=x.get("nivel_fuente_orientativo")=="expert"
            danger="revision_especializada" if specialized else "revision_por_realizar"
            counts["nuevos"]+=1
            if source:counts["fuente_en_con_pasos"]+=1
            if es:counts["orientacion_es"]+=1
        else:
            source_url=""
            status="sin_ficha_paso_a_paso_en_visor"
            danger="revision_por_realizar"
            counts["historicos"]+=1
        reviewed=id in verified
        state="documentado_con_revisor" if reviewed else "sin_aprobacion_individual_registrada"
        if reviewed:counts["aprobaciones_documentadas"]+=1
        next_action=("vincular_animacion_creada_y_revisar_storyboard" if reviewed
            else ("crear_ficha_es_y_validarla_individualmente" if not newer
            else ("investigar_fuente_con_ficha_individual" if not source
                else "traducir_comparar_y_validar_pasos_tecnicos")))
        rows.append({
            "id":id,"nombre_es":x["nombre_es"],"categoria":x["categoria"],
            "procedencia":origin,"nivel_declarado":x.get("nivel_tecnico",""),
            "pasos_es":len(es),"pasos_original_en":len(source),
            "fuente_individual":source_url,
            "estado_documentacion":status,
            "estado_validacion_profesional":state,
            "riesgo_revision":danger,
            "animacion_propia":"pendiente",
            "habilitado_para_rutinas":"no" if newer else "reglas_historicas",
            "siguiente_accion":next_action
        })
        animations.append({
            "id":id,"nombre_es":x["nombre_es"],
            "categoria":x["categoria"],
            "estado_storyboard":"listo_para_borrador" if source or reviewed
                               else "requiere_fuente_de_movimiento",
            "autorizado_para_publicar_gif":"no",
            "revision_tecnica_documentada":"si" if reviewed else "no",
            "archivo_reservado":"asterion_"+id+".gif"
        })
    if counts["historicos"]!=1257 or counts["nuevos"]!=847:
        raise ValueError("Cambio inesperado en reparto del catálogo")
    if counts["fuente_en_con_pasos"]!=842 or len(source_missing)!=5:
        raise ValueError("Inconsistencia de datos técnicos de origen")
    write_csv(OUTPUT,FIELDS,rows)
    write_csv(ANIMATIONS,tuple(animations[0]),animations)
    report={
        "schema":"asterion.technical-readiness.v1",
        "total":len(entries),
        "historicos_sin_pasos_individuales_en_visor":counts["historicos"],
        "nuevos_con_orientacion_es":counts["orientacion_es"],
        "nuevos_con_pasos_fuente_ingles":counts["fuente_en_con_pasos"],
        "nuevos_sin_pasos_fuente":len(source_missing),
        "ids_sin_pasos_fuente":source_missing,
        "validaciones_profesionales_documentadas":counts["aprobaciones_documentadas"],
        "validaciones_sin_evidencia_que_se_puedan_afirmar":0,
        "registros_con_requisito_de_aprobacion":len(entries)-counts["aprobaciones_documentadas"],
        "animaciones_publicables_con_la_evidencia_actual":0,
        "pruebas_documentales_automaticas":"completas",
        "documentacion_tecnica_individual":"pendiente_revision",
        "apto_para_comenzar_storyboards_internos":True,
        "apto_para_publicacion_de_demostraciones":False
    }
    SUMMARY.write_text(
        json.dumps(report,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
    print(json.dumps(report,ensure_ascii=False,indent=2))
    return report

if __name__=="__main__":
    generate()
