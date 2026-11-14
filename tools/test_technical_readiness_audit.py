import csv
import json
import unittest
import sys
from pathlib import Path

sys.path.insert(0,str(Path(__file__).resolve().parent))
import audit_technical_readiness as audit

class TechnicalReadinessAuditTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.result=audit.generate()
        with audit.OUTPUT.open(encoding="utf-8",newline="") as f:
            cls.rows=list(csv.DictReader(f))
        with audit.ANIMATIONS.open(encoding="utf-8",newline="") as f:
            cls.animations=list(csv.DictReader(f))

    def test_all_2104_exercises_have_an_individual_audit_record(self):
        self.assertEqual(2104,len(self.rows))
        self.assertEqual(2104,len({x["id"] for x in self.rows}))
        self.assertEqual(2104,len(self.animations))
        self.assertEqual(
            {x["id"] for x in self.rows},
            {x["id"] for x in self.animations})

    def test_previous_exercises_are_preserved_without_claiming_extra_steps(self):
        old=[x for x in self.rows if x["procedencia"]=="asterion"]
        self.assertEqual(1257,len(old))
        self.assertTrue(all(x["pasos_es"]=="0" for x in old))
        self.assertTrue(all(x["habilitado_para_rutinas"]=="reglas_historicas"
                            for x in old))

    def test_new_entries_are_not_falsely_marked_as_professionally_validated(self):
        new=[x for x in self.rows if x["procedencia"]=="dominio_publico"]
        self.assertEqual(847,len(new))
        self.assertEqual(842,sum(int(x["pasos_original_en"])>0 for x in new))
        self.assertEqual(5,sum(x["pasos_original_en"]=="0" for x in new))
        self.assertTrue(all(x["fuente_individual"].startswith(
            "https://github.com/yuhonas/free-exercise-db/blob/")
            for x in new))
        self.assertTrue(all(x["habilitado_para_rutinas"]=="no" for x in new))
        self.assertTrue(all(x["estado_validacion_profesional"]==
                            "sin_aprobacion_individual_registrada" for x in new))

    def test_five_missing_individual_sources_are_visible(self):
        expected={
            "Iron_Cross","One-Arm_Kettlebell_Swings","Push_Press",
            "Side_Bridge","Side_Jackknife"}
        observed={
            x["fuente_individual"].split("/")[-1].removesuffix(".json")
            for x in self.rows if x["procedencia"]=="dominio_publico"
            and x["pasos_original_en"]=="0"}
        self.assertEqual(expected,observed)

    def test_all_gif_publication_is_blocked_until_documented_review(self):
        self.assertTrue(all(x["autorizado_para_publicar_gif"]=="no"
                            for x in self.animations))
        self.assertEqual(0,self.result["animaciones_publicables_con_la_evidencia_actual"])
        self.assertEqual(0,self.result["validaciones_profesionales_documentadas"])
        self.assertEqual(2104,self.result["registros_con_requisito_de_aprobacion"])
        self.assertFalse(self.result["apto_para_publicacion_de_demostraciones"])

    def test_review_register_has_strict_columns(self):
        with audit.REVIEW_REGISTER.open(encoding="utf-8",newline="") as f:
            r=csv.DictReader(f)
            self.assertEqual(tuple(r.fieldnames),audit.REVIEW_FIELDS)

if __name__=="__main__":
    unittest.main()
