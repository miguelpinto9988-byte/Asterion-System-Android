#!/usr/bin/env python3
"""Comprueba que el lote GIF usa 100 IDs reales del catálogo y fondos válidos."""
import csv
import json
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CSV = ROOT / "docs/datos/produccion_mp4_por_variante.csv"
BATCH = ROOT / "docs/datos/lote_0001_gifs_transparentes.json"
BGS = ROOT / "docs/datos/biblioteca_fondos_gif_v1.json"


class GifBatchRegistryTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        with CSV.open(encoding="utf-8", newline="") as handle:
            cls.catalogue = list(csv.DictReader(handle))
        cls.batch = json.loads(BATCH.read_text(encoding="utf-8"))
        cls.backgrounds = json.loads(BGS.read_text(encoding="utf-8"))["fondos"]

    def test_exact_first_100_catalogue_ids(self):
        rows = self.batch["items"]
        self.assertEqual(len(rows), 100)
        ids = [row["id"] for row in rows]
        self.assertEqual(len(set(ids)), 100)
        self.assertEqual(ids, [row["id"] for row in self.catalogue[:100]])

    def test_metadata_matches_catalogue(self):
        for item, source in zip(self.batch["items"], self.catalogue[:100]):
            for k in ("id", "nombre", "categoria", "variante", "material"):
                self.assertEqual(item[k], source[k], (item["id"], k))

    def test_paths_backgrounds_and_honest_status(self):
        valid_bgs = {bg["id"] for bg in self.backgrounds}
        self.assertEqual(len(valid_bgs), len(self.backgrounds))
        self.assertEqual(self.batch["gifs_integrados_apk"], 0)
        for item in self.batch["items"]:
            self.assertIn(item["background_id"], valid_bgs)
            self.assertEqual(item["gif_path"], f'exercise_gifs/v2/{item["id"]}.gif')
            self.assertFalse(item["integrado_en_apk"])
            if item["gif_estado"] == "pendiente_generacion":
                self.assertIsNone(item["gif_sha256"])
                self.assertIsNone(item["gif_bytes"])
            elif item["gif_estado"] == "generado_pendiente_revision":
                self.assertRegex(item["gif_sha256"], r"^[0-9a-f]{64}$")
                self.assertGreater(item["gif_bytes"], 0)
            else:
                self.fail("Estado desconocido: " + item["gif_estado"])

    def test_first_three_not_mislabeled(self):
        by_id = {r["id"]: r for r in self.batch["items"]}
        self.assertEqual(by_id["sentadilla_bilateral_v01"]["nombre"], "Sentadilla a silla")
        self.assertEqual(by_id["sentadilla_bilateral_v01"]["background_id"], "BG_005")
        self.assertEqual(by_id["sentadilla_bilateral_v02"]["background_id"], "BG_009")
        self.assertEqual(by_id["sentadilla_bilateral_v03"]["nombre"], "Sentadilla libre")


if __name__ == "__main__":
    unittest.main()
