#!/usr/bin/env python3
"""Verify additive catalog consolidation, canonical taxonomy and manual-review gate."""
import csv
from collections import Counter
from pathlib import Path
import unicodedata

ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "progress-engine/src/main/resources/asterion_exercises/v1"

def read(name):
    with (DATA / name).open(encoding="utf-8", newline="") as stream:
        return list(csv.DictReader(stream, delimiter="\t"))

families, types, variants, intensity = (
    read(name) for name in ("families.tsv", "types.tsv", "variants.tsv", "intensity.tsv")
)
assert (len(families), len(types), len(variants), len(intensity)) == (155, 474, 1257, 752)
ids = (("family_id", families), ("type_id", types), ("variant_id", variants),
       ("variant_id", intensity))
assert all(len({row[key] for row in rows}) == len(rows) for key, rows in ids)
assert {t["family_id"] for t in types} == {f["family_id"] for f in families}
assert {v["type_id"] for v in variants} == {t["type_id"] for t in types}
assert {i["variant_id"] for i in intensity} <= {v["variant_id"] for v in variants}

# The prior 125/354/1017/512 rows are an unchanged prefix; PR #12 adds only new IDs.
new_families, new_types = families[125:], types[354:]
new_variants, new_tags = variants[1017:], intensity[512:]
assert (len(new_families), len(new_types), len(new_variants), len(new_tags)) == (30, 120, 240, 240)
assert set(f["family_id"] for f in new_families).isdisjoint(
    f["family_id"] for f in families[:125])
assert set(t["type_id"] for t in new_types).isdisjoint(t["type_id"] for t in types[:354])
assert set(v["variant_id"] for v in new_variants).isdisjoint(
    v["variant_id"] for v in variants[:1017])
assert {t["family_id"] for t in new_types} == {f["family_id"] for f in new_families}
assert {v["type_id"] for v in new_variants} == {t["type_id"] for t in new_types}
assert {i["variant_id"] for i in new_tags} == {v["variant_id"] for v in new_variants}
assert all(v["supervision"] == "1" for v in new_variants), "Review gate bypassed"

categories = {"tren_superior", "tren_central", "tren_inferior", "movilidad", "cardio"}
assert set(f["category"] for f in families) == categories
assert Counter(f["category"] for f in new_families) == dict.fromkeys(categories, 6)
assert Counter(f["category"] for f in families) == {
    "tren_superior": 33, "tren_central": 29, "tren_inferior": 33,
    "movilidad": 30, "cardio": 30,
}
assert len({v["name"].casefold() for v in variants}) == 1257
def normalize(value):
    return "".join(c for c in unicodedata.normalize("NFD", value.casefold())
                   if not unicodedata.combining(c))
assert len({normalize(v["name"]) for v in variants}) == 1257
print("OK: 155 families / 474 types / 1257 variants, append-only, manual review required.")
