#!/usr/bin/env python3
"""Generate code-native SVG mockups of the proposed Asterion Android UI."""
from __future__ import annotations

import html
import subprocess
from dataclasses import dataclass
from pathlib import Path
from PIL import ImageFont

ROOT = Path(__file__).parent
OUT = ROOT / "prediseno-asterion"
OUT.mkdir(exist_ok=True)
FONT = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
BOLD = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"


@dataclass(frozen=True)
class Palette:
    bg: str
    surface: str
    surface2: str
    ink: str
    muted: str
    line: str
    accent: str
    violet: str
    soft: str
    pill: str
    green: str


DARK = Palette("#090F1E", "#141F34", "#1C2B44", "#F7F9FF", "#A9B8CF",
               "#33445E", "#80DFFF", "#B295FF", "#1D374C", "#263950", "#58D6AE")
LIGHT = Palette("#F3F6FC", "#FFFFFF", "#EAF1F9", "#17263F", "#60728A",
                "#D7E1EC", "#076F9E", "#6C4BB4", "#E0F3FA", "#E8EFF8", "#087A61")


class Canvas:
    def __init__(self, theme: str):
        self.theme = theme
        self.p = DARK if theme == "dark" else LIGHT
        self.parts: list[str] = []
        self.parts.append(
            '<svg xmlns="http://www.w3.org/2000/svg" width="390" height="844" '
            'viewBox="0 0 390 844">'
            '<defs>'
            '<linearGradient id="cta" x2="1" y2="1">'
            '<stop stop-color="#0879A8"/><stop offset="1" stop-color="#5B45A9"/>'
            '</linearGradient>'
            '<linearGradient id="hero" x2="1" y2="1">'
            '<stop stop-color="#173454"/><stop offset=".65" stop-color="#1C264B"/>'
            '<stop offset="1" stop-color="#302058"/></linearGradient>'
            '<linearGradient id="demo" x2="1" y2="1">'
            '<stop stop-color="#143551"/><stop offset=".65" stop-color="#162640"/>'
            '<stop offset="1" stop-color="#312354"/></linearGradient>'
            '<filter id="shadow" x="-20%" y="-20%" width="140%" height="150%">'
            '<feDropShadow dx="0" dy="4" stdDeviation="7" flood-color="#101C36" '
            'flood-opacity=".10"/></filter></defs>'
        )
        self.rect(0, 0, 390, 844, self.p.bg)
        self.circle(326, 103, 145, "#6741C0" if theme == "dark" else "#C9B6F8", opacity=".055")
        self.circle(18, 21, 110, "#61CAE6", opacity=".055")

    def raw(self, s: str):
        self.parts.append(s)

    def rect(self, x, y, w, h, fill, r=0, stroke=None, sw=1, opacity=1, shadow=False):
        attrs = (f'x="{x}" y="{y}" width="{w}" height="{h}" rx="{r}" '
                 f'fill="{fill}" opacity="{opacity}"')
        if stroke:
            attrs += f' stroke="{stroke}" stroke-width="{sw}"'
        # Inkscape's SVG renderer may drop gradient fills inside a drop-shadow
        # filter. Keep the full surface and button contrast in exported PNGs.
        self.raw(f"<rect {attrs}/>")

    def circle(self, x, y, r, fill="none", stroke=None, sw=1, opacity=1):
        attrs = f'cx="{x}" cy="{y}" r="{r}" fill="{fill}" opacity="{opacity}"'
        if stroke:
            attrs += f' stroke="{stroke}" stroke-width="{sw}"'
        self.raw(f"<circle {attrs}/>")

    def line(self, x1, y1, x2, y2, stroke=None, sw=1, opacity=1):
        self.raw(f'<line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" '
                 f'stroke="{stroke or self.p.line}" stroke-width="{sw}" opacity="{opacity}"/>')

    def path(self, d, stroke, sw=2, fill="none", opacity=1):
        self.raw(f'<path d="{d}" fill="{fill}" stroke="{stroke}" '
                 f'stroke-width="{sw}" stroke-linecap="round" '
                 f'stroke-linejoin="round" opacity="{opacity}"/>')

    def text(self, x, y, s, size=12, color=None, bold=False, anchor="start",
             spacing=None, opacity=1):
        attrs = (f'x="{x}" y="{y}" font-family="DejaVu Sans" font-size="{size}" '
                 f'fill="{color or self.p.ink}" font-weight="{700 if bold else 400}" '
                 f'text-anchor="{anchor}" opacity="{opacity}"')
        if spacing is not None:
            attrs += f' letter-spacing="{spacing}"'
        self.raw(f"<text {attrs}>{html.escape(str(s))}</text>")

    def wrap(self, x, y, s, width, size=11, color=None, bold=False, leading=None,
             max_lines=None):
        leading = leading or size * 1.45
        font = ImageFont.truetype(BOLD if bold else FONT, round(size * 2))
        words = s.split()
        rows: list[str] = []
        current = ""
        for word in words:
            candidate = (current + " " + word).strip()
            if font.getlength(candidate) / 2 <= width or not current:
                current = candidate
            else:
                rows.append(current)
                current = word
        if current:
            rows.append(current)
        if max_lines:
            rows = rows[:max_lines]
        for i, row in enumerate(rows):
            self.text(x, y + i * leading, row, size, color, bold)
        return y + len(rows) * leading

    def card(self, x, y, w, h, border=None, fill=None):
        self.rect(x, y, w, h, fill or self.p.surface, 16,
                  border or self.p.line, shadow=True)

    def chip(self, x, y, label, color=None, w=None):
        color = color or self.p.accent
        font = ImageFont.truetype(BOLD, 18)
        w = w or min(190, font.getlength(label) / 2 + 22)
        self.rect(x, y, w, 24, self.p.soft if color == self.p.accent else self.p.pill, 12)
        self.text(x + w / 2, y + 16, label, 9, color, True, "middle")
        return w

    def button(self, x, y, w, label, primary=True, h=42):
        self.rect(x, y, w, h, "url(#cta)" if primary else self.p.surface, 11,
                  None if primary else self.p.line, shadow=primary)
        self.text(x + w / 2, y + h / 2 + 4, label, 11,
                  "#FFFFFF" if primary else self.p.ink, True, "middle")

    def section(self, x, y, title, action=""):
        self.text(x, y, title, 14, bold=True)
        if action:
            self.text(372, y, action, 10, self.p.accent, True, "end")

    def progress(self, x, y, w, fraction):
        self.rect(x, y, w, 7, self.p.pill, 4)
        self.rect(x, y, max(4, w * fraction), 7, "url(#cta)", 4)

    def header(self):
        self.text(20, 22, "9:41", 11, bold=True)
        self.text(370, 22, "▴  ▰", 12, anchor="end")
        self.raw('<path d="M31 44 43 56 31 68 19 56Z" fill="none" '
                 f'stroke="{self.p.accent}" stroke-width="1.5"/>')
        self.text(31, 61, "✦", 15, self.p.accent, True, "middle")
        self.text(54, 53, "ASTERION", 12, bold=True, spacing=1.2)
        self.text(55, 64, "SYSTEM", 7, self.p.muted, True, spacing=2.1)
        self.rect(312, 41, 32, 32, self.p.surface, 10, self.p.line)
        self.text(328, 62, "☼" if self.theme == "dark" else "☾", 18, self.p.accent, anchor="middle")
        self.circle(364, 57, 16, "url(#cta)")
        self.text(364, 62, "A", 14, "#FFFFFF", True, "middle")

    def nav(self, active="home"):
        self.rect(0, 768, 390, 76, self.p.bg)
        self.line(0, 768, 390, 768)
        tabs = [("home", "⌂", "Inicio"), ("missions", "◇", "Misiones"),
                ("routines", "✦", "Rutinas"), ("progress", "▥", "Progreso")]
        for i, (name, glyph, label) in enumerate(tabs):
            x = 8 + i * 96
            if name == active:
                self.rect(x, 779, 86, 48, self.p.soft, 10)
            col = self.p.accent if name == active else self.p.muted
            self.text(x + 43, 799, glyph, 21, col, True, "middle")
            self.text(x + 43, 817, label, 9, col, True, "middle")
        self.rect(136, 838, 118, 4, self.p.ink, 2, opacity=.7)

    def base(self, active="home", nav=True):
        self.header()
        if nav:
            self.nav(active)

    def save(self, filename):
        self.parts.append("</svg>")
        svg = OUT / f"{filename}_{self.theme}.svg"
        png = OUT / f"{filename}_{self.theme}.png"
        svg.write_text("".join(self.parts), encoding="utf-8")
        result = subprocess.run(
            ["inkscape", str(svg), "--export-filename", str(png), "--export-width", "780"],
            capture_output=True, text=True
        )
        if result.returncode:
            raise RuntimeError(result.stderr)
        return png


def headline(c, eyebrow, title, subtitle, y=105):
    c.text(18, y, eyebrow.upper(), 10, c.p.accent, True, spacing=1.2)
    c.text(18, y + 32, title, 25, bold=True)
    c.wrap(18, y + 52, subtitle, 352, 11, c.p.muted, leading=15)


def demo(c, x, y, w=354, h=188):
    c.rect(x, y, w, h, "url(#demo)", 15, "#46658A")
    for radius, op in ((52, .10), (75, .06), (97, .04)):
        c.circle(x + 180, y + 91, radius, "none", "#A9D8FF", 1, op)
    c.rect(x + 10, y + 9, 142, 24, "#243C59", 12)
    c.text(x + 81, y + 25, "▶  GIF · DEMOSTRACIÓN", 9, "#C5F5FF", True, "middle")
    floor = y + 151
    c.line(x + 17, floor, x + w - 18, floor, "#5385AD", 1.5)
    # Simplified two-pose technique illustration, intended to reserve space for the actual GIF.
    c.circle(x + 94, y + 57, 10, "#83E7F5")
    for path in (f"M{x+94} {y+68}v48", f"M{x+92} {y+85}l-22 16",
                 f"M{x+96} {y+85}l20 16", f"M{x+94} {y+115}l-14 35",
                 f"M{x+94} {y+115}l16 35"):
        c.path(path, "#83E7F5", 7)
    c.path(f"M{x+149} {y+95}q20 10 5 31m-2-8 2 8 8-4", "#C0A9FF", 2)
    c.circle(x + 244, y + 76, 10, "#BEA8FF")
    for path in (f"M{x+240} {y+87}l-8 38", f"M{x+238} {y+100}l-20 19",
                 f"M{x+238} {y+100}l20 13", f"M{x+232} {y+124}l29 5",
                 f"M{x+232} {y+124}l-18 27", f"M{x+261} {y+129}l12 22"):
        c.path(path, "#BEA8FF", 7)
    c.path(f"M{x+272} {y+128}h24v24m-66 0h72", "#A8B4D0", 3)
    c.text(x + 93, y + 172, "INICIO", 9, "#A8DCE7", True, "middle")
    c.text(x + 249, y + 172, "DESCENSO", 9, "#DCCEFF", True, "middle")


def welcome(c):
    c.base()
    headline(c, "PERFIL · PASO 2 DE 4", "Diseña tu camino.",
             "Propuestas según tu tiempo, experiencia y equipo. Puedes cambiarlo después.")
    for i in range(4):
        c.rect(18 + i * 90, 192, 84, 5, c.p.accent if i < 2 else c.p.pill, 3)
    c.card(18, 216, 354, 179)
    c.text(32, 238, "TU PUNTO DE PARTIDA", 10, c.p.muted, True, spacing=.5)
    for i, label in enumerate(("Estoy empezando", "Ya entreno algunas veces",
                               "Tengo experiencia")):
        y = 248 + i * 44
        c.rect(31, y, 328, 39, c.p.soft if i == 0 else c.p.surface, 9,
               c.p.accent if i == 0 else c.p.line)
        c.circle(49, y + 19, 7, "none", c.p.accent, 1.5)
        if i == 0:
            c.circle(49, y + 19, 3, c.p.accent)
        c.text(64, y + 24, label, 11, bold=i == 0)
    c.text(18, 420, "Tiempo disponible por sesión", 11, c.p.muted, True)
    c.card(18, 430, 354, 48)
    c.text(32, 459, "20–30 minutos", 12, bold=True)
    c.text(349, 459, "⌄", 14, c.p.accent, anchor="end")
    c.text(18, 503, "Equipo disponible", 11, c.p.muted, True)
    c.chip(18, 513, "✓ Peso corporal", w=118)
    c.chip(142, 513, "Silla", w=56)
    c.chip(204, 513, "Mancuernas", w=101)
    c.text(18, 560, "Disponibilidad semanal", 11, c.p.muted, True)
    for i, day in enumerate(("L", "M", "X", "J", "V", "S", "D")):
        x = 18 + 51 * i
        active = i in (0, 2, 4)
        c.rect(x, 572, 45, 53, c.p.soft if active else c.p.surface, 10,
               c.p.accent if active else c.p.line)
        c.text(x + 22.5, 590, day, 9, c.p.accent if active else c.p.muted, True, "middle")
        c.text(x + 22.5, 612, "✓" if active else "·", 14,
               c.p.accent if active else c.p.muted, True, "middle")
    c.card(18, 642, 354, 52)
    c.text(32, 665, "Tema de la app", 11, bold=True)
    c.text(32, 681, "Claro u oscuro, cuando quieras.", 9, c.p.muted)
    c.chip(285, 656, "Oscuro" if c.theme == "dark" else "Claro", w=73)
    c.button(18, 710, 354, "Continuar  →", h=44)


def home(c):
    c.base("home")
    headline(c, "SÁBADO 14 NOV · NIVEL 12", "Cada movimiento cuenta.",
             "Tu aventura se adapta a ti. Hoy puedes entrenar o descansar.")
    c.rect(18, 200, 354, 184, "url(#hero)", 18, "#47638B")
    for r, op in ((54, .25), (73, .17), (93, .10)):
        c.circle(327, 263, r, "none", "#96DFFF", 1, op)
    c.text(326, 283, "✧", 64, "#C5C5FF", anchor="middle")
    c.text(34, 227, "TU PRÓXIMA SESIÓN", 10, "#B2E4FC", True, spacing=.6)
    c.text(34, 261, "Fuerza", 24, "#FFFFFF", True)
    c.text(34, 289, "esencial", 24, "#FFFFFF", True)
    c.text(34, 309, "4 ejercicios · 20–30 min", 10, "#D3E2F4")
    c.text(34, 323, "Con peso corporal y silla", 10, "#D3E2F4")
    c.rect(34, 340, 172, 31, "#E5FAFF", 9)
    c.text(120, 360, "Comenzar rutina  →", 10, "#184261", True, "middle")
    for i, (value, label) in enumerate((("2/4", "Misiones hoy"),
                                        ("3", "Días activos"),
                                        ("74", "Min esta semana"))):
        x = 18 + i * 121
        c.card(x, 395, 112, 70)
        c.text(x + 12, 426, value, 18, bold=True)
        c.text(x + 12, 449, label, 9, c.p.muted)
    c.section(18, 491, "Misiones de hoy", "Ver todas  →")
    c.card(18, 505, 354, 176)
    for i, (title, detail, done) in enumerate((
        ("Preparar el cuerpo", "Movilidad · realizada", True),
        ("Fuerza esencial", "Tu rutina · 4 movimientos", False),
        ("Caminar a tu ritmo", "Actividad libre · opcional", False))):
        y = 516 + i * 54
        c.rect(32, y, 22, 22, "#28AD8A" if done else c.p.surface, 6,
               None if done else c.p.accent)
        if done:
            c.text(43, y + 16, "✓", 15, "#FFFFFF", True, "middle")
        c.text(65, y + 11, title, 11, bold=True)
        c.text(65, y + 27, detail, 9, c.p.muted)
        if i < 2:
            c.line(32, y + 43, 358, y + 43)
    c.section(18, 709, "Esta semana", "Progreso  →")
    c.card(18, 721, 354, 59)
    c.text(32, 741, "Movimiento acumulado", 10, c.p.muted)
    c.text(358, 741, "74 min", 10, bold=True, anchor="end")
    c.progress(32, 753, 326, .61)


def missions(c):
    c.base("missions")
    headline(c, "TABLERO DE MISIONES", "Misiones de hoy",
             "Marca lo que hiciste. Puedes desmarcarlo o descansar sin perder progreso.")
    c.card(18, 193, 354, 89)
    c.text(32, 219, "Progreso de hoy", 12, bold=True)
    c.chip(239, 202, "2 de 4 hechas", w=119)
    c.progress(32, 235, 326, .5)
    c.text(32, 262, "Tú decides cuándo una misión está finalizada.", 9, c.p.muted)
    c.section(18, 307, "Sábado 14 · misiones", "Editar plan")
    items = [
        ("Preparar el cuerpo", "Movilidad suave · 5 min", "Breve", True),
        ("Fuerza esencial", "Rutina guiada · 4 ejercicios", "Rutina", False),
        ("Caminar a tu ritmo", "Movimiento libre · flexible", "Opcional", False),
        ("Registrar cómo te sientes", "Chequeo personal · 1 min", "Bienestar", True),
    ]
    for i, (name, detail, label, done) in enumerate(items):
        y = 320 + i * 70
        c.card(18, y, 354, 62)
        c.rect(30, y + 17, 26, 26, "#2BAE8D" if done else c.p.surface, 7,
               None if done else c.p.accent)
        if done:
            c.text(43, y + 36, "✓", 16, "#FFFFFF", True, "middle")
        c.text(66, y + 27, name, 11, bold=True, opacity=.7 if done else 1)
        c.text(66, y + 43, detail, 9, c.p.muted)
        c.chip(294, y + 8, label, c.p.muted, 64)
    c.rect(18, 614, 354, 115, "url(#hero)", 16, "#6256A8")
    c.text(33, 638, "MISIÓN ESPECIAL · OPCIONAL", 10, "#D1BFFF", True, spacing=.4)
    c.text(33, 661, "✦  Desafío Monarca", 15, "#FFFFFF", True)
    c.wrap(33, 679, "Tres días de opciones para revisar y adaptar antes de aceptar.", 315,
           10, "#D4D1EC", leading=14)
    c.rect(33, 701, 123, 22, "#D9CAFF", 7)
    c.text(94, 716, "Explorar reto  →", 9, "#2A2350", True, "middle")
    c.text(19, 747, "Pausar o sustituir una misión no implica penalización.", 9, c.p.muted)


def routines(c):
    c.base("routines")
    headline(c, "ENTRENAMIENTOS", "Rutinas para ti",
             "Elige según tu energía, tu tiempo y el equipo disponible.")
    c.rect(18, 185, 354, 36, c.p.pill, 10)
    c.rect(22, 189, 113, 28, c.p.surface, 8)
    for x, label, active in ((78, "Para ti", True), (194, "Explorar", False),
                             (308, "Guardadas", False)):
        c.text(x, 207, label, 10, c.p.ink if active else c.p.muted, True, "middle")
    c.card(18, 235, 354, 181, c.p.violet)
    c.rect(32, 249, 75, 75, "url(#hero)", 14)
    c.text(69, 302, "✧", 43, "#D5F0FF", anchor="middle")
    c.text(121, 265, "RECOMENDADA HOY", 9, c.p.accent, True)
    c.text(121, 291, "Fuerza esencial", 17, bold=True)
    c.text(121, 310, "4 movimientos · 20–30 min", 9, c.p.muted)
    c.chip(32, 337, "Principiante", w=90)
    c.chip(128, 337, "Sin gimnasio", c.p.muted, 97)
    c.chip(231, 337, "Pausas libres", c.p.muted, 98)
    c.button(32, 371, 326, "Ver sesión paso a paso  →", h=33)
    c.section(18, 440, "Ajustar búsqueda", "Mis preferencias")
    for x, label, w in ((18, "20–30 min", 84), (108, "Fuerza", 66),
                        (180, "Movilidad", 80), (266, "Sin equipo", 93)):
        c.chip(x, 452, label, w=w)
    c.section(18, 504, "Otras opciones", "Ver biblioteca  →")
    for i, (name, meta, glyph) in enumerate((
        ("Movilidad y recuperación", "6 movimientos · 15 min · suave", "◇"),
        ("Piernas y estabilidad", "5 movimientos · 25 min · silla", "✦"))):
        y = 517 + i * 82
        c.card(18, y, 354, 74)
        c.rect(30, y + 10, 52, 52, "url(#demo)", 12)
        c.text(56, y + 45, glyph, 28, "#CFEAFB", anchor="middle")
        c.text(93, y + 29, name, 11, bold=True)
        c.text(93, y + 47, meta, 9, c.p.muted)
        c.text(354, y + 44, "›", 20, c.p.muted, anchor="end")
    c.section(18, 706, "Plan semanal", "Calendario  →")
    for i, day in enumerate(("L", "M", "X", "J", "V", "S", "D")):
        x = 18 + i * 51
        c.rect(x, 715, 45, 45, c.p.soft if i == 4 else c.p.surface, 9,
               c.p.accent if i == 4 else c.p.line)
        c.text(x + 22.5, 732, day, 9, c.p.muted, True, "middle")
        c.text(x + 22.5, 750, "✓" if i in (0, 2) else "•" if i == 4 else "·",
               12, c.p.accent if i in (0, 2, 4) else c.p.muted, True, "middle")


def workout(c):
    c.base(nav=False)
    c.text(18, 106, "SESIÓN GUIADA · 02 / 04", 10, c.p.accent, True, spacing=1)
    c.chip(293, 89, "⏸ Pausar", c.p.violet, 79)
    c.text(18, 139, "Sentadilla a silla", 23, bold=True)
    c.text(18, 158, "Fuerza esencial · Piernas y estabilidad", 10, c.p.muted)
    c.progress(18, 170, 354, .43)
    demo(c, 18, 190)
    c.chip(18, 391, "Ejercicio 2 de 4", w=107)
    c.chip(131, 391, "Serie 1 de 2", c.p.muted, 96)
    c.chip(233, 391, "Silla estable", c.p.muted, 92)
    c.card(18, 428, 354, 118)
    c.text(32, 453, "REPETICIONES REGISTRADAS", 9, c.p.muted, True, spacing=.5)
    c.text(32, 492, "8", 30, bold=True)
    c.text(72, 492, "/ 10", 15, c.p.muted)
    for x, label in ((286, "−"), (328, "+")):
        c.rect(x, 452, 32, 32, c.p.surface2, 9, c.p.line)
        c.text(x + 16, 475, label, 21, c.p.ink, anchor="middle")
    c.wrap(32, 519, "Meta orientativa y ajustable. Cuenta solo repeticiones cómodas.", 323,
           9, c.p.muted, leading=13)
    c.card(18, 558, 354, 69)
    c.circle(54, 592, 22, "none", c.p.violet, 4)
    c.text(54, 598, "◷", 20, c.p.violet, anchor="middle")
    c.text(89, 584, "Descanso · 01:20", 13, bold=True)
    c.text(89, 602, "Pausa, reanuda o salta a tu ritmo.", 9, c.p.muted)
    c.rect(319, 573, 38, 38, c.p.surface2, 9, c.p.line)
    c.text(338, 599, "▶", 16, c.p.accent, anchor="middle")
    c.text(18, 649, "Puedes omitir un ejercicio o detenerte por molestias.", 9, c.p.muted)
    c.button(18, 670, 110, "Salir", False, 45)
    c.button(136, 670, 236, "Registrar y continuar  →", True, 45)
    c.rect(0, 764, 390, 80, c.p.bg)
    c.line(0, 764, 390, 764)
    c.text(18, 789, "SIGUIENTE", 9, c.p.accent, True, spacing=.8)
    c.text(18, 810, "Puente de glúteos", 13, bold=True)
    c.text(371, 810, "03 / 04", 10, c.p.muted, True, "end")
    c.rect(136, 838, 118, 4, c.p.ink, 2, opacity=.7)


def calendar(c):
    c.base("routines")
    headline(c, "TU SEMANA Y TU DESCANSO", "Calendario",
             "La planificación es flexible. Un día libre no borra tus avances.")
    c.card(18, 192, 354, 374)
    c.rect(32, 207, 28, 27, c.p.surface2, 7, c.p.line)
    c.rect(330, 207, 28, 27, c.p.surface2, 7, c.p.line)
    c.text(46, 226, "‹", 18, c.p.ink, anchor="middle")
    c.text(344, 226, "›", 18, c.p.ink, anchor="middle")
    c.text(195, 225, "Noviembre 2026", 14, bold=True, anchor="middle")
    weekdays = ("L", "M", "X", "J", "V", "S", "D")
    for i, day in enumerate(weekdays):
        c.text(55 + i * 46, 261, day, 10, c.p.muted, True, "middle")
    for number in range(1, 31):
        pos = number + 5  # November 2026 starts on Sunday.
        row, col = divmod(pos, 7)
        x, y = 35 + col * 46, 272 + row * 43
        selected = number == 14
        complete = number in (2, 4, 6, 9, 11, 13)
        planned = number in (16, 18, 20, 23, 25)
        if selected or planned:
            c.rect(x, y, 40, 37, c.p.violet if selected else c.p.soft, 9,
                   None if selected else c.p.line)
        c.text(x + 20, y + 23, str(number), 11,
               "#FFFFFF" if selected else c.p.accent if complete else c.p.ink,
               selected or complete, "middle")
        if selected or complete or planned:
            c.circle(x + 20, y + 31, 2, "#FFFFFF" if selected else c.p.accent)
    for x, label, col in ((32, "● Hecho", c.p.green),
                          (137, "● Planificado", c.p.accent),
                          (265, "● Descanso", c.p.muted)):
        c.text(x, 549, label, 9, col, True)
    c.section(18, 591, "Sábado 14", "Ver misiones  →")
    c.card(18, 605, 354, 90)
    c.text(32, 627, "Fuerza esencial", 13, bold=True)
    c.chip(262, 614, "Planificada", w=95)
    c.text(32, 645, "4 movimientos · 20–30 min · a tu ritmo", 9, c.p.muted)
    c.button(32, 656, 326, "Abrir rutina  →", h=29)
    c.card(18, 705, 354, 54)
    c.text(33, 735, "☾", 24, c.p.violet)
    c.text(65, 728, "Mañana: descanso flexible", 11, bold=True)
    c.text(65, 745, "Sin penalización por descansar.", 9, c.p.muted)


def progress(c):
    c.base("progress")
    headline(c, "EVOLUCIÓN PERSONAL", "A tu ritmo.",
             "Observa tu movimiento y cómo te sientes. Tu salud es más que una cifra.")
    for i, (value, label) in enumerate((("3", "Días activos"), ("74", "Min de movimiento"),
                                        ("7", "Misiones hechas"))):
        x = 18 + i * 121
        c.card(x, 193, 112, 75)
        c.text(x + 12, 225, value, 20, bold=True)
        c.text(x + 12, 250, label, 8.5, c.p.muted)
    c.section(18, 296, "Actividad de esta semana", "Calendario  →")
    c.card(18, 309, 354, 195)
    c.text(32, 335, "LUN 9 — DOM 15", 10, c.p.muted, True)
    c.chip(265, 318, "+1 día activo", c.p.green, 93)
    levels = (.55, .12, .78, .12, .91, .10, .10)
    for i, (day, value) in enumerate(zip(("L", "M", "X", "J", "V", "S", "D"), levels)):
        x = 41 + i * 45
        height = value * 93
        c.rect(x, 461 - height, 22, height, "url(#cta)" if value > .2 else c.p.pill, 6)
        c.text(x + 11, 482, day, 9, c.p.muted, True, "middle")
    c.text(32, 496, "Incluye movimiento libre y rutinas registradas.", 9, c.p.muted)
    c.section(18, 533, "Cómo te has sentido", "Registrar hoy  →")
    c.card(18, 546, 354, 96)
    c.text(32, 574, "Energía después de moverte", 12, bold=True)
    c.chip(303, 555, "Bien", w=55)
    c.progress(32, 589, 326, .70)
    c.text(32, 615, "Este registro es personal y opcional.", 9, c.p.muted)
    c.section(18, 671, "Constancia sin presión")
    c.card(18, 685, 354, 74)
    c.text(32, 722, "✧", 27, c.p.violet)
    c.text(64, 711, "Tu progreso es tuyo", 11, bold=True)
    c.wrap(64, 727, "Compara tu actividad contigo y adapta tu plan cuando lo necesites.",
           280, 9, c.p.muted, leading=13)


def exercise(c):
    c.base("routines")
    headline(c, "BIBLIOTECA · TREN INFERIOR", "Sentadilla a silla",
             "Ejercicio de fuerza adaptable · equipo: silla estable.")
    demo(c, 18, 194)
    c.chip(18, 392, "GIF por variante", w=110)
    c.chip(134, 392, "Nivel inicial", c.p.muted, 95)
    c.chip(235, 392, "Sin cargas", c.p.muted, 86)
    c.section(18, 447, "Cómo hacerlo", "Ampliar GIF  ↗")
    c.card(18, 460, 354, 181)
    steps = [
        ("1 · Prepara el espacio", "Coloca una silla firme detrás de ti y encuentra una postura cómoda."),
        ("2 · Desciende con control", "Lleva la cadera hacia la silla sin dejarte caer."),
        ("3 · Vuelve a subir", "Levántate con control. Reduce el rango si hay molestias."),
    ]
    for i, (title, detail) in enumerate(steps):
        y = 483 + i * 54
        c.line(35, y - 11, 35, y + 35, c.p.accent, 2)
        c.text(49, y, title, 10, bold=True)
        c.wrap(49, y + 16, detail, 295, 9, c.p.muted, leading=12, max_lines=2)
    c.section(18, 667, "Opciones y seguridad")
    c.card(18, 680, 354, 77)
    c.text(32, 702, "Variante con apoyo de brazos", 11, bold=True)
    c.wrap(32, 719, "Elige un rango cómodo. Detente si hay dolor o malestar.", 323,
           9, c.p.muted, leading=13)


def profile(c):
    c.base()
    headline(c, "TU ESPACIO", "Perfil y ajustes",
             "Tus preferencias cambian el plan, el aspecto y el seguimiento.")
    c.card(18, 192, 354, 91)
    c.circle(59, 238, 26, "url(#cta)")
    c.text(59, 246, "A", 24, "#FFFFFF", True, "middle")
    c.text(98, 224, "Cazador demo", 14, bold=True)
    c.text(98, 243, "Nivel 12 · bienestar general", 9, c.p.muted)
    c.chip(98, 253, "Tu camino, tus reglas", c.p.violet, 138)
    c.section(18, 310, "Entrenamiento", "Editar  →")
    c.card(18, 322, 354, 160)
    rows = (("Tiempo por sesión", "20–30 min"), ("Experiencia", "Inicial"),
            ("Equipo disponible", "Peso corporal y silla"))
    for i, (name, value) in enumerate(rows):
        y = 347 + i * 50
        c.text(32, y, name, 11, bold=True)
        c.text(358, y, value, 10, c.p.accent, True, "end")
        if i < 2:
            c.line(32, y + 17, 358, y + 17)
    c.section(18, 510, "Aspecto y acceso")
    c.card(18, 522, 354, 130)
    c.text(32, 548, "Tema de la aplicación", 11, bold=True)
    c.chip(277, 532, "Oscuro  ↔" if c.theme == "dark" else "Claro  ↔", w=81)
    c.line(32, 564, 358, 564)
    c.text(32, 589, "Texto y contraste", 11, bold=True)
    c.text(358, 589, "Ajustar  ›", 10, c.p.accent, True, "end")
    c.line(32, 604, 358, 604)
    c.text(32, 629, "Datos locales", 11, bold=True)
    c.text(358, 629, "Abrir  ›", 10, c.p.accent, True, "end")
    c.section(18, 681, "Bienestar")
    c.card(18, 694, 354, 65)
    c.text(32, 720, "Pausar o adaptar el plan", 11, bold=True)
    c.text(32, 739, "Descanso y molestias sin penalización.", 9, c.p.muted)
    c.text(358, 728, "›", 19, c.p.accent, anchor="end")


SCREENS = {
    "01_alta": welcome,
    "02_inicio": home,
    "03_misiones": missions,
    "04_rutinas": routines,
    "05_sesion": workout,
    "06_calendario": calendar,
    "07_progreso": progress,
    "08_ejercicio": exercise,
    "09_perfil": profile,
}

if __name__ == "__main__":
    for theme in ("dark", "light"):
        for filename, draw in SCREENS.items():
            canvas = Canvas(theme)
            draw(canvas)
            print(canvas.save(filename))
