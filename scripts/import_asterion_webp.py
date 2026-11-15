#!/usr/bin/env python3
"""Import the approved Asterion visual pack to Android without overwrites.

Usage:
    python3 scripts/import_asterion_webp.py Asterion_Paquete_Visual_Definitivo_v2.zip .
The archive is intentionally distributed separately; GitHub does not yet
contain the approved binary artwork. Verify hashes before writing ANY file.
"""
import hashlib
import json
import re
from pathlib import Path
import sys
import zipfile

EXACT = {
    "retratos/01_cazador_azul.webp": "asterion_hunter_blue.webp",
    "retratos/02_cazadora_blanco_dorado.webp": "asterion_hunter_gold.webp",
    "retratos/03_guerrero_oscuro.webp": "asterion_hunter_dark.webp",
    "retratos/04_estratega_blanco.webp": "asterion_hunter_white.webp",
    "fondos_tarjeta/01_temporal_oriental.webp": "asterion_card_temple.webp",
    "fondos_tarjeta/02_playa_tropical.webp": "asterion_card_beach.webp",
    "fondos_tarjeta/03_ciudad_occidental.webp": "asterion_card_city.webp",
    "fondos_tarjeta/04_desierto.webp": "asterion_card_desert.webp",
    "fondos_tarjeta/05_bosque_oriental.webp": "asterion_card_forest.webp",
    "marcos/01_tecnologico.webp": "asterion_frame_tech.webp",
    "marcos/02_celestial.webp": "asterion_frame_celestial.webp",
    "marcos/03_epico.webp": "asterion_frame_epic.webp",
    "marcos/04_natural.webp": "asterion_frame_nature.webp",
    "marcos/05_arcano.webp": "asterion_frame_arcane.webp",
    "marcos/06_glacial.webp": "asterion_frame_glacial.webp",
    "marcos/07_oriental.webp": "asterion_frame_oriental.webp",
    "marcos/08_legendario.webp": "asterion_frame_legendary.webp",
    "cabeceras/paisaje_cabecera_claro.webp": "asterion_header_light.webp",
    "cabeceras/paisaje_cabecera_oscuro.webp": "asterion_header_dark.webp",
}

def name_for(source):
    if source in EXACT:
        return EXACT[source]
    if source.startswith("iconos/") and source.endswith(".webp"):
        parts = source.split("/")
        if len(parts) == 3 and re.fullmatch(r"ic_[a-z0-9_]+[.]webp", parts[2]):
            prefix = {
                "tema_claro_negro": "asterion_light_",
                "tema_oscuro_blanco": "asterion_dark_",
                "categorias_color": "asterion_cat_",
            }.get(parts[1])
            if prefix:
                return prefix + parts[2]
    raise ValueError("Archivo no reconocido en el paquete: " + source)

def sha256(raw):
    return hashlib.sha256(raw).hexdigest()

def main():
    if len(sys.argv) != 3:
        raise SystemExit("Uso: python3 scripts/import_asterion_webp.py PAQUETE.zip PROYECTO_ANDROID")
    archive, project = Path(sys.argv[1]), Path(sys.argv[2])
    if not (project / "app/src/main/AndroidManifest.xml").is_file():
        raise SystemExit("Raíz Android no válida; no se ha escrito ningún archivo.")
    target = project / "app/src/main/res/drawable-nodpi"
    with zipfile.ZipFile(archive, "r") as z:
        manifest = json.loads(z.read("MANIFIESTO.json"))
        entries = manifest.get("files", manifest.get("immutable_approved_assets"))
        if not isinstance(entries, list) or not entries:
            raise SystemExit("Manifiesto ausente o inválido.")
        plan = []
        seen = set()
        for entry in entries:
            source = entry["path"]
            dest_name = name_for(source)
            if dest_name in seen:
                raise SystemExit("Nombre Android repetido: " + dest_name)
            seen.add(dest_name)
            raw = z.read(source)
            if len(raw) != entry["bytes"] or sha256(raw) != entry["sha256"]:
                raise SystemExit("Error de integridad SHA-256: " + source)
            if not (raw[:4] == b"RIFF" and raw[8:12] == b"WEBP"):
                raise SystemExit("No es un WebP válido: " + source)
            target_file = target / dest_name
            if target_file.exists() and sha256(target_file.read_bytes()) != sha256(raw):
                raise SystemExit("Conflicto, no se sobrescribe: " + str(target_file))
            plan.append((target_file, raw))
        if manifest.get("version") == "Asterion-visual-v2" and len(plan) != 99:
            raise SystemExit("El paquete v2 debe contener exactamente 99 WebP.")
        target.mkdir(parents=True, exist_ok=True)
        added = 0
        for dest, raw in plan:
            if not dest.exists():
                # 'xb' ensures a newly created file never overwrites another.
                with dest.open("xb") as f:
                    f.write(raw)
                added += 1
        print("Recursos verificados:", len(plan), "Nuevos:", added,
              "Ya idénticos:", len(plan) - added)
        print("No se ha modificado ningún recurso previamente existente.")

if __name__ == "__main__":
    main()
