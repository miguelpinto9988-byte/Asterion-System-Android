import csv
import json
import sys
import unittest
from pathlib import Path

sys.path.insert(0,str(Path(__file__).resolve().parent))
import build_exercise_motion_specs as generator

class MotionSpecTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.result=generator.build()
        cls.entries={x["id"]:x for x in cls.result["ejercicios"]}

    def test_all_2104_get_unique_gif_identities_not_fake_assets(self):
        self.assertEqual(2104,len(self.entries))
        self.assertEqual(0,self.result["gifs_producidos_por_este_importador"])
        self.assertEqual(2104,self.result["gifs_pendientes"])
        self.assertEqual(2104,len(set(x["gif_ruta"] for x in self.entries.values())))
        for id,record in self.entries.items():
            self.assertEqual("exercise_gifs/v1/"+id+".gif",record["gif_ruta"])
            self.assertFalse(record["gif_validado"])
            self.assertEqual("pendiente_produccion",record["gif_estado"])

    def test_pushup_variants_have_distinct_motion_constraints(self):
        standard=self.entries["flexiones_v04"]
        close=self.entries["pd_e_0565"]
        wide=self.entries["pd_e_0564"]
        inclined=self.entries["pd_e_0347"]
        self.assertIn("convencional",standard["variante"])
        self.assertIn("manos_proximas",close["variante"])
        self.assertNotIn("diamante",close["variante"])
        self.assertIn("manos_separadas",wide["variante"])
        self.assertIn("apoyo_elevado",inclined["variante"])
        self.assertEqual(4,len({x["gif_ruta"] for x in
                       (standard,close,wide,inclined)}))

    def test_diamond_not_falsely_assigned_to_close_grip(self):
        for x in self.entries.values():
            if "diamante" in x["variante"]:
                name=(x["nombre"]+" "+x["id_fuente"]).lower()
                self.assertTrue("diamond" in name or "diamante" in name)

    def test_originals_and_new_variants_are_conserved(self):
        original=sum(1 for x in self.entries.values() if x["origen"]=="asterion")
        self.assertEqual(1257,original)
        self.assertEqual(847,len(self.entries)-original)
        self.assertEqual(
            {"tren_superior","tren_inferior","tren_central","movilidad","cardio"},
            set(self.result["categorias"]))

if __name__=="__main__":
    unittest.main()
