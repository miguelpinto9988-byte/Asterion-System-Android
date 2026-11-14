import argparse
import hashlib
import json
import math
import re
from pathlib import Path

from PIL import Image, ImageDraw

W = H = 500
FRAMES = 7
DURATION = 370
MAX_COLORS = 24

SKIN = (235, 181, 136, 255)
SKIN_HI = (255, 215, 176, 255)
OUTLINE = (48, 53, 61, 255)
TORSO = (215, 216, 214, 255)
SHORTS = (82, 85, 91, 255)
MUSCLE = (222, 83, 73, 255)
MUSCLE_DARK = (183, 54, 55, 255)
EQUIPMENT = (55, 72, 91, 255)
EQUIPMENT_LIGHT = (100, 115, 130, 255)
FLOOR = (190, 195, 200, 255)


def hash_unit(value):
    digest = hashlib.sha256(value.encode("utf-8")).digest()
    return int.from_bytes(digest[:4], "big") / 2**32


def has(text, *terms):
    return any(re.search(term, text, flags=re.I) for term in terms)


def text_for(row):
    return " ".join(str(row.get(k, "")) for k in ("id", "nombre", "categoria", "material")).lower()


def movement_kind(row):
    text = text_for(row)
    category = row.get("categoria", "")
    if has(text, r"flexi[oó]n|push.?up|fondos|dips?", r"press de banca"):
        return "push"
    if has(text, r"dominada|pull.?up|jal[oó]n"):
        return "pullup"
    if has(text, r"remo|row"):
        return "row"
    if has(text, r"sentadilla|squat"):
        return "squat"
    if has(text, r"zancada|desplante|lunge|split squat"):
        return "lunge"
    if has(text, r"peso muerto|deadlift|bisagra|buenos d[ií]as|hiperext"):
        return "hinge"
    if has(text, r"puente|hip thrust|elevaci[oó]n de cadera|glute bridge"):
        return "bridge"
    if has(text, r"plancha|plank|escalador|mountain climber|bear crawl"):
        return "plank"
    if has(text, r"crunch|abdominal|sit.?up|elevaci[oó]n de piernas"):
        return "core"
    if has(text, r"salto|jump|burpee|pliometr|jack"):
        return "jump"
    if has(text, r"curl|b[ií]ceps|biceps"):
        return "curl"
    if has(text, r"tr[ií]ceps|extensi[oó]n de codo"):
        return "triceps"
    if has(text, r"elevaci[oó]n lateral|elevaci[oó]n frontal|p[aá]jaro|delto"):
        return "raise"
    if has(text, r"rotaci[oó]n|twist|woodchop|russian"):
        return "rotation"
    if has(text, r"equilibrio|balance|una pierna|unilateral"):
        return "balance"
    if has(text, r"carrera|sprint|marcha|caminata|paseo|skipping"):
        return "cardio"
    if has(text, r"movilidad|estiramiento|stretch|yoga|90.?90") or category == "movilidad":
        return "mobility"
    if category == "tren_inferior":
        return "squat"
    if category == "tren_central":
        return "core"
    if category == "cardio":
        return "cardio"
    return "raise"


def flags(row):
    text = text_for(row)
    material = str(row.get("material", "")).lower()
    return {
        "unilateral": has(text, r"unilateral|una pierna|single.?leg|single arm|un brazo"),
        "alternating": has(text, r"altern|caminando|marcha|contralateral"),
        "pause": has(text, r"pausa|isom[eé]tric|hold|sost[eé]n|tempo"),
        "partial": has(text, r"parcial|rango corto|media repetici[oó]n|half"),
        "assisted": has(text, r"asistid|apoyo|pared|silla|banco|inclina") or material in {"pared", "silla", "banco"},
        "elevated": has(text, r"elevad|declinad|pies elevados|apoyo alto|escal[oó]n"),
        "explosive": has(text, r"salto|jump|explos|pliometr|r[aá]pido"),
        "band": has(text, r"banda|band"),
        "bar": has(text, r"barra|barbell"),
        "dumbbell": has(text, r"mancuerna|dumbbell"),
        "kettlebell": has(text, r"pesa rusa|kettlebell"),
        "ball": has(text, r"bal[oó]n|pelota|fitball"),
        "cable": has(text, r"polea|cable"),
        "bench": has(text, r"banco|silla|caja|escal[oó]n"),
        "mat": has(text, r"colchoneta|suelo|mat") or row.get("categoria") == "tren_central",
    }


def lerp(a, b, t):
    return (a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t)


def add(a, dx=0, dy=0):
    return (a[0] + dx, a[1] + dy)


def mirror_pose(pose):
    return {k: (W - x, y) for k, (x, y) in pose.items()}


def standing(phase=0, seed=.5):
    bend = phase
    sway = math.sin(seed * math.tau) * 5
    return {
        "head": (250 + sway, 66), "neck": (250 + sway, 98),
        "shoulder_l": (220 + sway, 112), "shoulder_r": (280 + sway, 112),
        "elbow_l": (203 + sway, 145), "elbow_r": (297 + sway, 145),
        "wrist_l": (190 + sway, 177), "wrist_r": (310 + sway, 177),
        "hip_l": (232 + sway, 218 + 28 * bend), "hip_r": (268 + sway, 218 + 28 * bend),
        "knee_l": (218 - 12 * bend + sway, 324 + 30 * bend), "knee_r": (282 + 12 * bend + sway, 324 + 30 * bend),
        "ankle_l": (208 - 6 * bend + sway, 435), "ankle_r": (292 + 6 * bend + sway, 435),
        "toe_l": (190 - 8 * bend + sway, 446), "toe_r": (310 + 8 * bend + sway, 446),
    }


def pose_for(kind, phase, row_flags, seed):
    p = standing(0, seed)
    side = -1 if seed < .5 else 1
    depth = .55 if row_flags["partial"] else 1.0
    if row_flags["assisted"]:
        depth *= .72
    if kind == "squat":
        q = phase * depth
        p = standing(0, seed)
        for key in ("head", "neck", "shoulder_l", "shoulder_r"):
            p[key] = add(p[key], 0, 42 * q)
        p["hip_l"] = add(p["hip_l"], 0, 68 * q)
        p["hip_r"] = add(p["hip_r"], 0, 68 * q)
        p["knee_l"] = add(p["knee_l"], -5 * q, 34 * q)
        p["knee_r"] = add(p["knee_r"], 5 * q, 34 * q)
    elif kind == "lunge":
        q = phase * depth
        p["hip_l"], p["hip_r"] = (246, 218 + 35 * q), (274, 218 + 35 * q)
        p["knee_l"], p["knee_r"] = (155, 340 + 18 * q), (335, 330 + 25 * q)
        p["ankle_l"], p["ankle_r"] = (115, 440), (390, 440)
        p["toe_l"], p["toe_r"] = (95, 446), (415, 446)
        p["head"] = add(p["head"], 0, 14 * q)
        if row_flags["unilateral"] and side < 0:
            p = mirror_pose(p)
    elif kind == "hinge":
        q = phase * depth
        p["head"] = (185 + 55 * q, 76 + 72 * q)
        p["neck"] = (210 + 55 * q, 105 + 70 * q)
        p["shoulder_l"] = (190 + 55 * q, 118 + 80 * q)
        p["shoulder_r"] = (238 + 55 * q, 118 + 80 * q)
        p["hip_l"], p["hip_r"] = (245, 220), (275, 220)
        p["knee_l"], p["knee_r"] = (220, 335 - 12 * q), (280, 335 - 12 * q)
        p["wrist_l"], p["wrist_r"] = (355 + 35 * q, 250 + 70 * q), (385 + 35 * q, 250 + 70 * q)
        p["elbow_l"], p["elbow_r"] = (305 + 30 * q, 205 + 65 * q), (335 + 30 * q, 205 + 65 * q)
    elif kind in {"push", "plank"}:
        q = phase * depth
        p = {
            "head": (78, 278 + 12 * q), "neck": (112, 287 + 12 * q),
            "shoulder_l": (143, 295 + 18 * q), "shoulder_r": (158, 303 + 18 * q),
            "elbow_l": (142, 342 + (25 * q if kind == "push" else 0)), "elbow_r": (158, 345 + (25 * q if kind == "push" else 0)),
            "wrist_l": (132, 382), "wrist_r": (164, 384),
            "hip_l": (300, 312 + 9 * q), "hip_r": (315, 319 + 9 * q),
            "knee_l": (390, 326 + 4 * q), "knee_r": (401, 334 + 4 * q),
            "ankle_l": (464, 337), "ankle_r": (472, 346),
            "toe_l": (485, 337), "toe_r": (492, 346),
        }
        if row_flags["elevated"]:
            for k in ("ankle_l", "ankle_r", "toe_l", "toe_r"):
                p[k] = add(p[k], 0, -32)
    elif kind == "pullup":
        q = phase * depth
        p["head"] = (250, 83 - 25 * q)
        p["neck"] = (250, 112 - 28 * q)
        p["shoulder_l"], p["shoulder_r"] = (222, 130 - 35 * q), (278, 130 - 35 * q)
        p["elbow_l"], p["elbow_r"] = (205, 95 - 10 * q), (295, 95 - 10 * q)
        p["wrist_l"], p["wrist_r"] = (205, 62), (295, 62)
        p["hip_l"], p["hip_r"] = (232, 250 - 30 * q), (268, 250 - 30 * q)
        p["knee_l"], p["knee_r"] = (220, 330 - 12 * q), (280, 330 - 12 * q)
        p["ankle_l"], p["ankle_r"] = (195, 405), (305, 405)
        p["toe_l"], p["toe_r"] = (180, 410), (320, 410)
    elif kind == "row":
        q = phase * depth
        p["head"] = (175 + 40 * q, 96 + 45 * q)
        p["neck"] = (202 + 42 * q, 120 + 50 * q)
        p["shoulder_l"], p["shoulder_r"] = (185 + 45 * q, 132 + 55 * q), (225 + 45 * q, 142 + 55 * q)
        p["hip_l"], p["hip_r"] = (260, 225), (292, 235)
        p["knee_l"], p["knee_r"] = (225, 340), (285, 345)
        p["ankle_l"], p["ankle_r"] = (210, 440), (300, 440)
        start_l, end_l = (355, 225), (270, 225)
        start_r, end_r = (380, 245), (285, 235)
        p["wrist_l"], p["wrist_r"] = lerp(start_l, end_l, q), lerp(start_r, end_r, q)
        p["elbow_l"], p["elbow_r"] = lerp((300, 190), (255, 215), q), lerp((325, 210), (275, 220), q)
    elif kind == "bridge":
        q = phase * depth
        p = {
            "head": (86, 391), "neck": (118, 390), "shoulder_l": (135, 382), "shoulder_r": (150, 391),
            "elbow_l": (112, 414), "elbow_r": (155, 420), "wrist_l": (90, 430), "wrist_r": (175, 435),
            "hip_l": (280, 380 - 68 * q), "hip_r": (298, 386 - 68 * q),
            "knee_l": (365, 315 - 20 * q), "knee_r": (385, 324 - 20 * q),
            "ankle_l": (435, 400), "ankle_r": (455, 405), "toe_l": (463, 405), "toe_r": (482, 410),
        }
    elif kind == "core":
        q = phase * depth
        p = {
            "head": (94 + 88 * q, 392 - 108 * q), "neck": (125 + 70 * q, 390 - 100 * q),
            "shoulder_l": (142 + 55 * q, 382 - 86 * q), "shoulder_r": (157 + 55 * q, 391 - 86 * q),
            "elbow_l": (106 + 58 * q, 410 - 72 * q), "elbow_r": (175 + 58 * q, 420 - 72 * q),
            "wrist_l": (88 + 65 * q, 430 - 55 * q), "wrist_r": (195 + 65 * q, 438 - 55 * q),
            "hip_l": (280, 382), "hip_r": (298, 388), "knee_l": (365, 320), "knee_r": (385, 328),
            "ankle_l": (435, 402), "ankle_r": (455, 408), "toe_l": (463, 407), "toe_r": (482, 413),
        }
    elif kind == "jump":
        q = phase * (1.25 if row_flags["explosive"] else 1.0)
        p = standing(0, seed)
        lift = -56 * q
        for k in p:
            p[k] = add(p[k], 0, lift)
        p["knee_l"] = add(p["knee_l"], -20 * q, -18 * q)
        p["knee_r"] = add(p["knee_r"], 20 * q, -18 * q)
        p["ankle_l"] = add(p["ankle_l"], -24 * q, -30 * q)
        p["ankle_r"] = add(p["ankle_r"], 24 * q, -30 * q)
    elif kind == "curl":
        p = standing(0, seed)
        q = phase * depth
        p["wrist_l"] = (190, 177 - 70 * q); p["wrist_r"] = (310, 177 - 70 * q)
        p["elbow_l"] = (205, 150 - 10 * q); p["elbow_r"] = (295, 150 - 10 * q)
    elif kind == "triceps":
        p = standing(0, seed)
        q = phase * depth
        p["elbow_l"] = (205, 142 - 42 * q); p["elbow_r"] = (295, 142 - 42 * q)
        p["wrist_l"] = (210, 78 - 18 * q); p["wrist_r"] = (290, 78 - 18 * q)
    elif kind == "raise":
        p = standing(0, seed)
        q = phase * depth
        p["elbow_l"] = lerp((203, 145), (178, 108), q); p["elbow_r"] = lerp((297, 145), (322, 108), q)
        p["wrist_l"] = lerp((190, 177), (165, 96), q); p["wrist_r"] = lerp((310, 177), (335, 96), q)
    elif kind == "rotation":
        p = standing(0, seed)
        q = phase * depth
        p["shoulder_l"] = add(p["shoulder_l"], 26 * q * side, -4 * q)
        p["shoulder_r"] = add(p["shoulder_r"], -26 * q * side, 4 * q)
        p["wrist_l"] = add(p["wrist_l"], 45 * q * side, -12 * q)
        p["wrist_r"] = add(p["wrist_r"], -45 * q * side, 12 * q)
    elif kind == "balance":
        p = standing(0, seed)
        q = phase * depth
        lifted = "knee_l" if side < 0 else "knee_r"
        lifted_ankle = "ankle_l" if side < 0 else "ankle_r"
        lifted_toe = "toe_l" if side < 0 else "toe_r"
        p[lifted] = add(p[lifted], 30 * side, -120 * q)
        p[lifted_ankle] = add(p[lifted_ankle], 60 * side, -105 * q)
        p[lifted_toe] = add(p[lifted_toe], 65 * side, -100 * q)
    elif kind == "cardio":
        p = standing(0, seed)
        q = phase
        p["knee_l"] = add(p["knee_l"], -35 * q, -85 * q)
        p["knee_r"] = add(p["knee_r"], 35 * q, -20 * q)
        p["ankle_l"] = add(p["ankle_l"], -30 * q, -105 * q)
        p["ankle_r"] = add(p["ankle_r"], 25 * q, -8 * q)
        p["wrist_l"] = add(p["wrist_l"], -15 * q, -50 * q)
        p["wrist_r"] = add(p["wrist_r"], 15 * q, -22 * q)
    elif kind == "mobility":
        p = standing(0, seed)
        q = phase * depth
        p["shoulder_l"] = add(p["shoulder_l"], -14 * q, 12 * q)
        p["shoulder_r"] = add(p["shoulder_r"], 14 * q, -12 * q)
        p["wrist_l"] = add(p["wrist_l"], -35 * q, 52 * q)
        p["wrist_r"] = add(p["wrist_r"], 35 * q, -52 * q)
    if row_flags["alternating"] and phase > .5:
        p = mirror_pose(p)
    return p


def phase_at(index, pause=False):
    if index == 0 or index == FRAMES - 1:
        return 0.0
    if pause and index in (FRAMES // 2, FRAMES // 2 + 1):
        return 1.0
    return math.sin(math.pi * index / (FRAMES - 1))


def line(draw, a, b, fill, outline_width=25, inner_width=17):
    draw.line((a, b), fill=OUTLINE, width=outline_width, joint="curve")
    draw.line((a, b), fill=fill, width=inner_width, joint="curve")


def draw_prop(draw, row, row_flags, kind, phase):
    material = str(row.get("material", "")).lower()
    floor_y = 430
    if kind in {"push", "plank", "bridge", "core"}:
        draw.line((58, floor_y - 50 if kind in {"push", "plank"} else floor_y, 442, floor_y - 50 if kind in {"push", "plank"} else floor_y), fill=FLOOR, width=5)
    if row_flags["bar"] or "barra" in material:
        y = 56 if kind == "pullup" else 90
        draw.line((80, y, 420, y), fill=EQUIPMENT, width=8)
        draw.ellipse((73, y - 6, 87, y + 8), fill=EQUIPMENT)
        draw.ellipse((413, y - 6, 427, y + 8), fill=EQUIPMENT)
    if row_flags["band"] or "banda" in material:
        draw.arc((130, 120, 370, 390), 15, 165, fill=EQUIPMENT_LIGHT, width=5)
    if row_flags["dumbbell"] or row_flags["kettlebell"]:
        y = 200 - int(30 * phase)
        for x in (188, 312):
            draw.line((x - 17, y, x + 17, y), fill=EQUIPMENT, width=6)
            draw.rectangle((x - 23, y - 12, x - 14, y + 12), fill=EQUIPMENT_LIGHT)
            draw.rectangle((x + 14, y - 12, x + 23, y + 12), fill=EQUIPMENT_LIGHT)
    if row_flags["bench"] or "silla" in material or "banco" in material:
        x, y = 370, 350
        draw.rectangle((x - 48, y, x + 48, y + 12), fill=EQUIPMENT_LIGHT)
        draw.line((x - 36, y + 12, x - 45, 430), fill=EQUIPMENT, width=7)
        draw.line((x + 36, y + 12, x + 45, 430), fill=EQUIPMENT, width=7)
    if "pared" in material:
        draw.line((420, 80, 420, 430), fill=FLOOR, width=9)


def render_pose(row, pose, phase):
    flags_row = flags(row)
    kind = movement_kind(row)
    image = Image.new("RGBA", (W, H), "white")
    draw = ImageDraw.Draw(image)
    draw_prop(draw, row, flags_row, kind, phase)

    # Rear limbs first.
    for a, b in (("shoulder_r", "elbow_r"), ("elbow_r", "wrist_r"), ("hip_r", "knee_r"), ("knee_r", "ankle_r")):
        line(draw, pose[a], pose[b], SKIN, 27, 18)
    # Pelvis and torso retain the anatomical reference's neutral palette.
    torso = [pose["shoulder_l"], pose["shoulder_r"], pose["hip_r"], pose["hip_l"]]
    draw.polygon(torso, fill=TORSO, outline=OUTLINE)
    draw.line((pose["shoulder_l"], pose["hip_l"]), fill=OUTLINE, width=3)
    draw.line((pose["shoulder_r"], pose["hip_r"]), fill=OUTLINE, width=3)
    shorts = [pose["hip_l"], pose["hip_r"], add(pose["hip_r"], 4, 42), add(pose["hip_l"], -4, 42)]
    draw.polygon(shorts, fill=SHORTS, outline=OUTLINE)
    # Front limbs.
    for a, b in (("shoulder_l", "elbow_l"), ("elbow_l", "wrist_l"), ("hip_l", "knee_l"), ("knee_l", "ankle_l")):
        line(draw, pose[a], pose[b], SKIN, 29, 19)
    # Muscle accents vary by movement family, not just by frame.
    for a, b in (("hip_l", "knee_l"), ("hip_r", "knee_r")):
        draw.line((pose[a], lerp(pose[a], pose[b], .45)), fill=MUSCLE, width=10)
    if kind in {"push", "plank", "row", "pullup", "raise", "curl", "triceps"}:
        for a, b in (("shoulder_l", "elbow_l"), ("shoulder_r", "elbow_r")):
            draw.line((pose[a], lerp(pose[a], pose[b], .45)), fill=MUSCLE_DARK, width=8)
    if kind in {"push", "plank"}:
        draw.line((pose["shoulder_l"], pose["hip_l"]), fill=MUSCLE, width=7)

    # Feet, hands, head and hair complete the figure.
    draw.line((pose["ankle_l"], pose["toe_l"]), fill=OUTLINE, width=12)
    draw.line((pose["ankle_r"], pose["toe_r"]), fill=OUTLINE, width=12)
    for wrist in (pose["wrist_l"], pose["wrist_r"]):
        draw.ellipse((wrist[0] - 10, wrist[1] - 10, wrist[0] + 10, wrist[1] + 10), fill=SKIN_HI, outline=OUTLINE, width=2)
    head = pose["head"]
    line(draw, add(head, 0, 22), pose["neck"], SKIN, 20, 13)
    draw.ellipse((head[0] - 23, head[1] - 30, head[0] + 23, head[1] + 30), fill=SKIN_HI, outline=OUTLINE, width=3)
    draw.arc((head[0] - 24, head[1] - 31, head[0] + 23, head[1] + 25), 190, 350, fill=OUTLINE, width=7)
    draw.ellipse((head[0] + 10, head[1] - 4, head[0] + 14, head[1]), fill=OUTLINE)
    return image.convert("RGB")


def make_gif(row, output):
    row_flags = flags(row)
    kind = movement_kind(row)
    seed = hash_unit(row["id"])
    frames = [render_pose(row, pose_for(kind, phase_at(i, row_flags["pause"]), row_flags, seed), phase_at(i, row_flags["pause"])) for i in range(FRAMES)]
    quantized = [frame.quantize(colors=MAX_COLORS, method=Image.Quantize.FASTOCTREE).convert("P", palette=Image.Palette.ADAPTIVE) for frame in frames]
    output.parent.mkdir(parents=True, exist_ok=True)
    quantized[0].save(output, format="GIF", save_all=True, append_images=quantized[1:], duration=DURATION, loop=0, disposal=2, optimize=True)
    return output.stat().st_size, {"kind": kind, "flags": row_flags}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--references", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--only", nargs="*", default=None)
    args = parser.parse_args()
    rows = json.loads(args.references.read_text(encoding="utf-8"))["referencias"]
    if args.only:
        wanted = set(args.only)
        rows = [row for row in rows if row["id"] in wanted]
    args.output.mkdir(parents=True, exist_ok=True)
    items = []
    for row in rows:
        size, motion = make_gif(row, args.output / f"{row['id']}.gif")
        items.append({"id": row["id"], "bytes": size, "motion": motion})
    sizes = [x["bytes"] for x in items]
    manifest = {"schema": "asterion.exercise-gifs.motion-v4", "total": len(items), "generated": len(list(args.output.glob("*.gif"))), "min_bytes": min(sizes) if sizes else 0, "max_bytes": max(sizes) if sizes else 0, "average_bytes": round(sum(sizes) / len(sizes)) if sizes else 0, "target_format": {"width": W, "height": H, "frames": FRAMES, "duration_ms": DURATION, "colors": MAX_COLORS, "background": "white"}, "items": items}
    (args.output / "_motion_manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({k: manifest[k] for k in ("total", "generated", "min_bytes", "max_bytes", "average_bytes", "target_format")}, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
