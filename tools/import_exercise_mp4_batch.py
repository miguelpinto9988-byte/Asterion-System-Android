#!/usr/bin/env python3
"""Importa lotes MP4 verificables, solo en debug si falta aprobación técnica.

Uso:
  python tools/import_exercise_mp4_batch.py mi_lote.zip --mode debug
  python tools/import_exercise_mp4_batch.py lote_aprobado.zip --mode release

Por defecto NO agrega contenido no revisado a la APK de producción.
La animación debe corresponder al movimiento REAL de cada identificador;
el validador físico no reemplaza la revisión humana de técnica.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
import re
import subprocess
import tempfile
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SPECS = ROOT / "app/src/main/assets/exercise_gifs/v1/specs_es.json"
MAX_MP4_SIZE = 800_000
VALID_ID = re.compile(r"[a-z0-9_-]+")
VALID_MODES = {"debug", "release"}


def inspect_mp4(path: Path) -> dict:
    result = subprocess.run(
        ["ffprobe", "-v", "error", "-show_streams", "-show_format",
         "-of", "json", str(path)],
        check=True, capture_output=True, text=True,
    )
    data = json.loads(result.stdout)
    streams = data["streams"]
    video = [s for s in streams if s.get("codec_type") == "video"]
    if len(video) != 1 or video[0]["codec_name"] != "h264":
        raise ValueError("El archivo no contiene un único vídeo H.264")
    if (video[0]["width"], video[0]["height"]) != (640, 360):
        raise ValueError("La resolución debe ser 640 × 360 (16:9)")
    if any(s["codec_type"] == "audio" for s in streams):
        raise ValueError("Las demostraciones MP4 deben ser silenciosas")
    duration = float(data["format"].get("duration", 0))
    if not (0.5 <= duration <= 8.0):
        raise ValueError("Duración fuera del rango de demostración")
    return {"seconds": round(duration, 3), "frames": int(video[0].get("nb_frames") or 0)}


def import_batch(zip_path: Path, mode: str = "debug",
                 destination: Path | None = None, replace: bool = False) -> dict:
    if mode not in VALID_MODES:
        raise ValueError("El modo debe ser debug o release")
    specs = json.loads(SPECS.read_text(encoding="utf-8"))
    allowed = {entry["id"] for entry in specs["ejercicios"]}
    if len(allowed) != 2104:
        raise ValueError("Inventario Asterion v2 incompleto")
    destination = destination or (
        ROOT / "app/src" / mode / "assets/exercise_mp4/v1")
    with zipfile.ZipFile(zip_path) as archive:
        files = archive.namelist()
        if "manifest.json" not in files or len(files) != len(set(files)):
            raise ValueError("ZIP sin manifest o con entradas repetidas")
        if any(name.startswith("/") or ".." in Path(name).parts
               or "/" in name or "\\" in name for name in files):
            raise ValueError("El ZIP debe tener únicamente nombres simples")
        manifest = json.loads(archive.read("manifest.json"))
        records = manifest.get("videos", [])
        if (manifest.get("total_catalogo") != 2104
                or manifest.get("videos_reales_en_lote") != len(records)
                or not records):
            raise ValueError("Manifest inconsistente con el catálogo")
        ids = [record["id"] for record in records]
        if len(set(ids)) != len(ids):
            raise ValueError("ID repetido dentro del lote")
        if set(files) != {"manifest.json", *(id + ".mp4" for id in ids)}:
            raise ValueError("Archivos no declarados o faltantes en el ZIP")
        validated = []
        hashes = set()
        with tempfile.TemporaryDirectory(prefix="asterion_video_audit_") as tmp:
            tmpdir = Path(tmp)
            for record in records:
                id = record["id"]
                if id not in allowed or not VALID_ID.fullmatch(id):
                    raise ValueError("ID de ejercicio no existe: " + id)
                if record["archivo"] != id + ".mp4":
                    raise ValueError("Nombre MP4 no corresponde a su ID")
                if mode == "release":
                    if (record.get("estado") != "aprobado_por_revisor"
                            or not record.get("revision_url")
                            or not record.get("revisor")
                            or not record.get("derechos_visual_confirmados")):
                        raise ValueError(
                            "No se puede publicar un vídeo sin evidencia "
                            "de revisión técnica y derechos: " + id)
                content = archive.read(record["archivo"])
                if len(content) > MAX_MP4_SIZE or not content:
                    raise ValueError("Vídeo vacío o demasiado pesado: " + id)
                digest = hashlib.sha256(content).hexdigest()
                if digest != record["sha256"]:
                    raise ValueError("Hash SHA256 diferente para: " + id)
                if digest in hashes:
                    raise ValueError("Vídeo duplicado dentro del lote: " + id)
                hashes.add(digest)
                trial = tmpdir / (id + ".mp4")
                trial.write_bytes(content)
                details = inspect_mp4(trial)
                if trial.stat().st_size != record["tamano_bytes"]:
                    raise ValueError("Tamaño del archivo alterado: " + id)
                validated.append((id, trial, digest, details))
            destination.mkdir(parents=True, exist_ok=True)
            if not replace:
                collisions = [id for id, _, _, _ in validated
                              if (destination / (id + ".mp4")).exists()]
                if collisions:
                    raise ValueError("ID previamente instalado: " + ", ".join(collisions))
            for id, trial, digest, details in validated:
                final = destination / (id + ".mp4")
                staging = destination / (id + ".new")
                staging.write_bytes(trial.read_bytes())
                os.replace(staging, final)
        receipt = {
            "modo": mode,
            "videos_verificados": len(validated),
            "total_catalogo": 2104,
            "videos_incluidos": [
                {"id": id, "sha256": digest, **details}
                for id, _, digest, details in validated
            ],
            "aprobacion_tecnica_automatica": False,
        }
        (destination / "import_receipt.json").write_text(
            json.dumps(receipt, ensure_ascii=False, indent=2) + "\n",
            encoding="utf-8")
        return receipt


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("lote_zip", type=Path)
    parser.add_argument("--mode", choices=("debug", "release"), default="debug")
    parser.add_argument("--destination", type=Path)
    parser.add_argument("--replace", action="store_true")
    args = parser.parse_args()
    print(json.dumps(import_batch(args.lote_zip, args.mode,
                                  args.destination, args.replace),
                     ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
