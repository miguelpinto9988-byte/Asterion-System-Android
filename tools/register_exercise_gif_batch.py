#!/usr/bin/env python3
"""Registra GIF transparentes del lote Asterion sin confundirlos con assets aprobados.

Uso:
  python3 tools/register_exercise_gif_batch.py --input-dir ./mis_gifs
  python3 tools/register_exercise_gif_batch.py --input-dir ./mis_gifs --stage-debug

Requiere Pillow: python3 -m pip install Pillow
Solo acepta archivos con basename igual al ID existente en el manifiesto.
El modo debug copia GIFs a assets de desarrollo; release exige revisión separada.
"""
import argparse
import hashlib
import json
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DEFAULT_MANIFEST = ROOT / "docs/datos/lote_0001_gifs_transparentes.json"
DEBUG_ASSETS = ROOT / "app/src/debug/assets/exercise_gifs/v2"


def validate_gif(file_path, expected_size=(551, 466)):
    try:
        from PIL import Image
    except ImportError as exc:
        raise RuntimeError("Falta Pillow; instala con: pip install Pillow") from exc

    with Image.open(file_path) as image:
        if image.format != "GIF":
            raise ValueError(f"No es GIF: {file_path.name}")
        if image.size != expected_size:
            raise ValueError(f"Dimensiones incorrectas {image.size}: {file_path.name}")
        if getattr(image, "n_frames", 1) < 2:
            raise ValueError(f"No es animación: {file_path.name}")
        if "transparency" not in image.info:
            raise ValueError(f"GIF sin transparencia: {file_path.name}")
        count = image.n_frames
        total_ms = 0
        for index in range(count):
            image.seek(index)
            total_ms += int(image.info.get("duration", 100))
        if total_ms <= 0:
            raise ValueError(f"Duración inválida: {file_path.name}")

    data = file_path.read_bytes()
    if not data.startswith((b"GIF89a", b"GIF87a")):
        raise ValueError(f"Cabecera GIF inválida: {file_path.name}")
    return {"sha256": hashlib.sha256(data).hexdigest(),
            "bytes": len(data), "fotogramas": count, "duracion_ms": total_ms}


def ingest(folder, manifest_path=DEFAULT_MANIFEST, stage_debug=False):
    folder = Path(folder)
    manifest_path = Path(manifest_path)
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    items = manifest["items"]
    by_id = {item["id"]: item for item in items}
    if len(by_id) != len(items):
        raise ValueError("IDs duplicados en el manifiesto")
    known_hashes = {item["gif_sha256"] for item in items if item.get("gif_sha256")}
    outcome = {"nuevos": [], "sin_cambios": [], "rechazados": []}
    for path in sorted(folder.glob("*.gif")):
        identifier = path.stem
        if identifier not in by_id:
            outcome["rechazados"].append({"archivo": path.name, "motivo": "ID fuera del lote"})
            continue
        try:
            details = validate_gif(path)
        except (ValueError, RuntimeError, OSError) as exc:
            outcome["rechazados"].append({"archivo": path.name, "motivo": str(exc)})
            continue
        item = by_id[identifier]
        if item.get("gif_sha256") == details["sha256"]:
            if stage_debug:
                dest = DEBUG_ASSETS / (identifier + ".gif")
                dest.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(path, dest)
                item["integrado_en_apk_debug"] = True
            outcome["sin_cambios"].append(identifier)
            continue
        if details["sha256"] in known_hashes:
            outcome["rechazados"].append({"archivo": path.name,
                                         "motivo": "Mismo archivo asignado a otro ID"})
            continue
        if item.get("gif_sha256"):
            outcome["rechazados"].append({"archivo": path.name,
                                         "motivo": "Ya existe otra versión: requiere revisión explícita"})
            continue
        item["gif_sha256"] = details["sha256"]
        item["gif_bytes"] = details["bytes"]
        item["fotogramas"] = details["fotogramas"]
        item["duracion_ms"] = details["duracion_ms"]
        item["gif_estado"] = "generado_pendiente_revision"
        item["origen_archivo"] = str(path)
        item["integrado_en_apk"] = False
        if stage_debug:
            dest = DEBUG_ASSETS / (identifier + ".gif")
            dest.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(path, dest)
            item["integrado_en_apk_debug"] = True
        known_hashes.add(details["sha256"])
        outcome["nuevos"].append(identifier)

    manifest["gifs_nuevos_generados"] = sum(
        bool(x.get("gif_sha256")) for x in items)
    manifest["gifs_aprobados"] = sum(
        bool(x.get("tecnica_revisada")) and bool(x.get("gif_sha256")) for x in items)
    manifest["gifs_integrados_apk"] = sum(bool(x.get("integrado_en_apk")) for x in items)
    if outcome["nuevos"] or (stage_debug and outcome["sin_cambios"]):
        manifest_path.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n",
                                 encoding="utf-8")
    return outcome


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--input-dir", type=Path, required=True)
    parser.add_argument("--manifest", type=Path, default=DEFAULT_MANIFEST)
    parser.add_argument("--stage-debug", action="store_true")
    args = parser.parse_args()
    result = ingest(args.input_dir, args.manifest, args.stage_debug)
    print(json.dumps(result, ensure_ascii=False, indent=2))
    if result["rechazados"]:
        raise SystemExit(2)


if __name__ == "__main__":
    main()
