#!/usr/bin/env python3
"""Import exact approved Asterion WebP assets without overwriting existing files.

Usage: python3 scripts/import_asterion_webp.py <approved-assets-zip> <android-project-root>
"""
import hashlib
import json
from pathlib import Path
import sys
import zipfile

MAPPING = {
    "retratos/01_cazador_azul.webp": "asterion_hunter_blue.webp",
    "retratos/02_cazadora_blanco_dorado.webp": "asterion_hunter_gold.webp",
    "retratos/03_guerrero_oscuro.webp": "asterion_hunter_dark.webp",
    "retratos/04_estratega_blanco.webp": "asterion_hunter_white.webp",
    "fondos_tarjeta/01_temporal_oriental.webp": "asterion_card_temple.webp",
    "fondos_tarjeta/02_playa_tropical.webp": "asterion_card_beach.webp",
    "fondos_tarjeta/03_ciudad_occidental.webp": "asterion_card_city.webp",
    "fondos_tarjeta/04_desierto.webp": "asterion_card_desert.webp",
    "fondos_tarjeta/05_bosque_oriental.webp": "asterion_card_forest.webp",
}

def digest(raw):
    return hashlib.sha256(raw).hexdigest()

def main():
    if len(sys.argv) != 3:
        raise SystemExit("Uso: python3 scripts/import_asterion_webp.py ARCHIVO.zip RUTA_PROYECTO_ANDROID")
    archive, project = Path(sys.argv[1]), Path(sys.argv[2])
    if not archive.is_file():
        raise SystemExit("No existe el paquete ZIP de recursos aprobados.")
    target = project / "app/src/main/res/drawable-nodpi"
    if not (project / "app/src/main/AndroidManifest.xml").exists():
        raise SystemExit("No parece una raíz de proyecto Android; no se ha modificado nada.")
    with zipfile.ZipFile(archive) as z:
        manifest = json.loads(z.read("MANIFIESTO.json"))
        expected = {a["path"]: a for a in manifest["immutable_approved_assets"]}
        # Check all source hashes and destination conflicts before any write.
        entries = []
        for src, dest_name in MAPPING.items():
            meta = expected[src]
            raw = z.read(src)
            if len(raw) != meta["bytes"] or digest(raw) != meta["sha256"]:
                raise SystemExit("La integridad del archivo no coincide: " + src)
            if raw[:4] != b"RIFF" or raw[8:12] != b"WEBP":
                raise SystemExit("El archivo no es WebP: " + src)
            dest = target / dest_name
            if dest.exists() and digest(dest.read_bytes()) != digest(raw):
                raise SystemExit("Conflicto: existe otro archivo diferente en " + str(dest))
            entries.append((dest, raw))
        target.mkdir(parents=True, exist_ok=True)
        for dest, raw in entries:
            if dest.exists():
                print("Ya coincide:", dest.name)
            else:
                dest.write_bytes(raw)
                print("Añadido:", dest.name, len(raw), "bytes")
    print("Completado: nueve WebP aprobados sin sobrescribir ni eliminar archivos.")

if __name__ == "__main__":
    main()
