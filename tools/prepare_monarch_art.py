#!/usr/bin/env python3
"""Obtiene arte de personajes reales en build, lo empaqueta en la APK y deja la app offline."""
import argparse
from pathlib import Path
from urllib.request import Request, urlopen

SOURCES = {
    # Fuentes visuales existentes. El propietario confirma tener autorización de uso.
    "asterion_monarch_igris.jpg":
        "https://images.cgames.de/images/gsgp/290/solo-leveling-igris-bild-chugong_6330960.jpg",
    "asterion_monarch_beru.jpg":
        "https://i0.wp.com/anitrendz.net/news/wp-content/uploads/2025/03/solo-leveling-beru-shadow-visual-e1743269718671.jpg",
    "asterion_monarch_ashborn.webp":
        "https://cdn.shopify.com/s/files/1/0902/6388/0060/files/ashborn.webp?v=1782126453",
    "asterion_monarch_baran.jpg":
        "https://image-5.uhdpaper.com/wallpaper/baran-solo-leveling-arise-overdrive-4k-wallpaper-uhdpaper.com-616%405%40k.jpg",
}

def valid_image(name, blob):
    if len(blob) < 4096 or len(blob) > 16000000:
        return False
    if name.endswith(".webp"):
        return blob[:4] == b"RIFF" and blob[8:12] == b"WEBP"
    return blob.startswith(bytes((0xFF, 0xD8, 0xFF)))

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--dest", type=Path, required=True)
    args = parser.parse_args()
    args.dest.mkdir(parents=True, exist_ok=True)
    for name, url in SOURCES.items():
        target = args.dest / name
        if target.exists() and valid_image(name, target.read_bytes()):
            print("Arte Monarca en caché:", name)
            continue
        try:
            request = Request(url, headers={"User-Agent": "Mozilla/5.0 AsterionPersonal/1.0"})
            with urlopen(request, timeout=22) as response:
                data = response.read(16000001)
            if not valid_image(name, data):
                raise ValueError("Formato o tamaño de imagen no válido")
            target.write_bytes(data)
            print("Arte Monarca integrado en APK:", name, len(data), "bytes")
        except Exception as exc:
            # El arte es opcional. Nunca bloquear un APK offline por fallos de CDN.
            target.unlink(missing_ok=True)
            print("AVISO: ilustración no disponible; se omite", name, type(exc).__name__)

if __name__ == "__main__":
    main()
