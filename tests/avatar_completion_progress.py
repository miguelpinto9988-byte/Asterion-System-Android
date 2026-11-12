#!/usr/bin/env python3
"""Authoritative count of catalog entries with authored body illustrations.

An animated joint-command map is NOT an exercise demonstration. A manifest
entry establishes an implementation mapping, not independent professional
biomechanical certification. Workflow CI must pass before reporting as tested.
"""
import argparse
import csv
import json
import re
from collections import Counter
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
TSV=ROOT/"app/src/main/assets/asterion_exercises/v1/avatar_motion_commands.tsv"
MANIFEST=ROOT/"app/src/main/java/pe/asterionsystem/app/AvatarDemonstrationManifest.java"

def summary():
    with TSV.open(encoding="utf-8",newline="") as source:
        rows=list(csv.DictReader(source,delimiter="\t"))
    if len(rows)!=1257:
        raise ValueError("Catálogo incompleto: se esperaban 1.257 IDs")
    ids=[r["variant_id"] for r in rows]
    if len(set(ids))!=len(ids):
        raise ValueError("IDs duplicados")
    illustrated={r["variant_id"] for r in rows if r["status"]=="ILLUSTRATIVE"}
    pending={r["variant_id"] for r in rows if r["status"]=="SPEC_ONLY"}
    if illustrated|pending!=set(ids) or illustrated&pending:
        raise ValueError("Estado visual desconocido")
    source=MANIFEST.read_text(encoding="utf-8")
    recipes=(MANIFEST.parent/"AvatarVariantScene.java").read_text(encoding="utf-8")
    authored=re.findall(r'add\(rows,"([^"]+)"',recipes)
    registered=re.findall(r'register\(rows,"([^"]+)"',source)+authored
    if len(set(registered))!=len(registered) or set(registered)!=illustrated:
        raise ValueError("Manifiesto visual y TSV tienen coberturas diferentes")
    if "entry.command!=record.command" not in source:
        raise ValueError("No se validan los comandos concretos de las escenas")
    per_category=Counter(r["category"] for r in rows if r["status"]=="SPEC_ONLY")
    total=len(rows)
    return {
        "total_variants":total,
        "body_demonstrations_registered":len(illustrated),
        "body_demonstrations_pending":len(pending),
        "percent_registered":round(100*len(illustrated)/total,2),
        "all_variants_have_a_command_map":True,
        "individual_biomechanical_review":False,
        "remaining_by_category":dict(sorted(per_category.items())),
        "pr":"https://github.com/miguelpinto9988-byte/Asterion-System-Android/pull/36",
        "issue":"https://github.com/miguelpinto9988-byte/Asterion-System-Android/issues/37",
    }

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument("--format",choices=("json","text"),default="text")
    parser.add_argument("--pending-csv",type=Path,help="Export every remaining ID with catalog metadata")
    args=parser.parse_args()
    result=summary()
    if args.pending_csv:
        taxonomy=ROOT/"progress-engine/src/main/resources/asterion_exercises/v1"
        with (taxonomy/"variants.tsv").open(encoding="utf-8") as f:
            variants={r["variant_id"]:r for r in csv.DictReader(f,delimiter="\t")}
        with TSV.open(encoding="utf-8") as f:
            pending=[r for r in csv.DictReader(f,delimiter="\t") if r["status"]=="SPEC_ONLY"]
        args.pending_csv.parent.mkdir(parents=True,exist_ok=True)
        with args.pending_csv.open("w",encoding="utf-8",newline="") as f:
            columns=["variant_id","type_id","name","equipment","execution","level","supervision","command","status"]
            writer=csv.DictWriter(f,fieldnames=columns);writer.writeheader()
            for row in pending:
                merged={**variants[row["variant_id"]],**row}
                writer.writerow({c:merged[c] for c in columns})
    if args.format=="json":
        print(json.dumps(result,ensure_ascii=False,indent=2))
    else:
        print(
            f"Avance real registrado: {result['percent_registered']:.2f}% "
            f"({result['body_demonstrations_registered']}/{result['total_variants']}). "
            f"Pendientes: {result['body_demonstrations_pending']}. "
            "Los mapas articulares no cuentan como demostraciones."
        )

if __name__=="__main__":
    main()
