#!/usr/bin/env python3
"""Arrange dark/light mobile renders into review sheets."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).parent
SOURCE = ROOT / "prediseno-asterion"
FONT = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 23)
SMALL = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 16)
SETS = [
    ("01_inicio_misiones_rutinas", [("02_inicio", "Inicio"), ("03_misiones", "Misiones"), ("04_rutinas", "Rutinas")]),
    ("02_sesion_calendario_biblioteca", [("05_sesion", "Sesión guiada"), ("06_calendario", "Calendario"), ("08_ejercicio", "Ficha de ejercicio")]),
    ("03_alta_progreso_perfil", [("01_alta", "Alta y preferencias"), ("07_progreso", "Progreso"), ("09_perfil", "Perfil y ajustes")]),
]

for filename, items in SETS:
    board = Image.new("RGB", (1708, 696), (10, 15, 29))
    draw = ImageDraw.Draw(board)
    draw.text((26, 15), filename.replace("_", " ").title(), font=FONT, fill=(245, 248, 255))
    for i, (key, label) in enumerate(items):
        x = 26 + i * 557
        draw.text((x, 56), label, font=FONT, fill=(217, 232, 255))
        for j, theme in enumerate(("dark", "light")):
            source = Image.open(SOURCE / f"{key}_{theme}.png").convert("RGB")
            image = source.resize((260, 563), Image.Resampling.LANCZOS)
            px = x + j * 272
            board.paste(image, (px, 91))
            draw.text((px + 3, 665), "OSCURO" if theme == "dark" else "CLARO",
                      font=SMALL, fill=(139, 207, 240) if theme == "dark" else (214, 202, 248))
    board.save(ROOT / f"{filename}.jpg", quality=91, optimize=True)

pair = Image.new("RGB", (810, 890), (10, 15, 29))
for x, theme in ((10, "dark"), (410, "light")):
    image = Image.open(SOURCE / f"05_sesion_{theme}.png").convert("RGB")
    pair.paste(image.resize((390, 844), Image.Resampling.LANCZOS), (x, 35))
ImageDraw.Draw(pair).text((11, 9), "SESIÓN GUIADA · OSCURO / CLARO", font=FONT, fill=(245, 248, 255))
pair.save(ROOT / "04_sesion_detalle.jpg", quality=92, optimize=True)
