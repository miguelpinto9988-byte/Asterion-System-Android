import csv
import json
import unittest
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
import build_public_domain_catalog as catalog

class PublicDomainImportTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.rows=catalog.data_source()
        cls.manifest=catalog.build(cls.rows)
        cls.data=json.loads((catalog.VIEW/"catalogo_es.json").read_text(encoding="utf-8"))

    def test_source_pinned_to_public_domain_independent_inventory(self):
        self.assertEqual(len(self.rows),876)
        self.assertEqual(catalog.UPSTREAM_BLOB,"37beaac6031b34f7a7322b3150d201182c49a71b")
        self.assertEqual(1257,self.manifest["originales"])
        self.assertEqual(876,self.manifest["registros_fuente_revisados"])

    def test_two_catalogs_compared_without_id_collision(self):
        data=self.data
        self.assertEqual(data["total"],len(data["ejercicios"]))
        self.assertEqual(len({x["id"] for x in data["ejercicios"]}),data["total"])
        self.assertEqual(data["total"],1257+self.manifest["nuevos"])
        self.assertEqual(876,
            self.manifest["nuevos"]+
            self.manifest["candidatos_de_duplicado"]+
            self.manifest["duplicados_verificados"])
        self.assertGreater(self.manifest["nuevos"],0)
        self.assertEqual(0,self.manifest["candidatos_de_duplicado"])
        self.assertEqual(22,self.manifest["candidatos_del_lote_cerrados"])

    def test_original_ids_and_volume_reference_are_unchanged(self):
        for filename in ("families.tsv","types.tsv","variants.tsv","volume_profiles.tsv"):
            before=catalog.tsv(filename)
            with (catalog.DEST/filename).open(encoding="utf-8") as file:
                after=list(csv.DictReader(file,delimiter="\t"))
            self.assertEqual(before,after[:len(before)],filename)
        self.assertEqual(1257,sum(x["origen"]=="asterion" for x in self.data["ejercicios"]))

    def test_added_variants_never_become_unreviewed_recommendations(self):
        with (catalog.DEST/"variants.tsv").open(encoding="utf-8") as file:
            rows=list(csv.DictReader(file,delimiter="\t"))
        new=rows[1257:]
        self.assertEqual(len(new),self.manifest["nuevos"])
        self.assertTrue(all(v["level"]=="0" and v["supervision"]=="1" for v in new))
        self.assertTrue(all(x["nivel_tecnico"]=="pendiente_revision" and
            not x["apto_para_rutinas_automaticas"] for x in
            self.data["ejercicios"] if x["origen"]=="dominio_publico"))

    def test_no_previous_mit_instructions_or_media(self):
        for x in self.data["ejercicios"]:
            self.assertFalse(x.get("gif_incluido"))
            self.assertFalse(x.get("imagen_incluida"))
            self.assertNotIn("gymvisual",json.dumps(x).lower())
            if x.get("origen")=="dominio_publico":
                self.assertGreaterEqual(len(x["instrucciones_es"]),3)
                self.assertEqual(x["fuente_tecnica_licencia"],"Unlicense")
                self.assertFalse(x["revision_tecnica_individual"])
                self.assertEqual(x["pasos_fuente_cantidad"],
                                 len(x["pasos_referencia_original_en"]))
                self.assertEqual(x["instrucciones_es_estado"],
                                 "orientacion_editorial_no_validada")
        self.assertTrue(self.data["no_incluye_contenido_mit_previo"])

    def test_all_five_categories_and_spanish_copy(self):
        cats={x["categoria"] for x in self.data["ejercicios"]
              if x["origen"]=="dominio_publico"}
        self.assertEqual(cats,{"tren_superior","tren_central","tren_inferior","movilidad","cardio"})
        self.assertTrue(all(x["nombre_es"] and x["material_es"] for x in
                            self.data["ejercicios"]))

    def test_verified_duplicates_keep_historic_identity(self):
        with catalog.AUDIT.open(encoding="utf-8",newline="") as handle:
            decisions={x["id_fuente_publico"]:x for x in csv.DictReader(handle)}
        self.assertEqual(
            decisions["Bodyweight_Squat"]["id_asterion_relacionado"],
            "sentadilla_bilateral_v03")
        self.assertEqual(
            decisions["Front_Barbell_Squat"]["id_asterion_relacionado"],
            "sentadilla_bilateral_v07")
        self.assertEqual(decisions["Bodyweight_Squat"]["estado"],
                         "duplicado_verificado")
        self.assertEqual(decisions["External_Rotation_with_Band"]["estado"],
                         "duplicado_verificado")

    def test_different_biomechanics_are_not_confirmed_as_same(self):
        with catalog.AUDIT.open(encoding="utf-8",newline="") as handle:
            decisions={x["id_fuente_publico"]:x for x in csv.DictReader(handle)}
        self.assertNotEqual(decisions["Barbell_Squat"]["estado"],
                            "duplicado_verificado")
        self.assertNotEqual(decisions["Barbell_Full_Squat"]["estado"],
                            "duplicado_verificado")
        self.assertNotEqual(decisions["Internal_Rotation_with_Band"]["estado"],
                            "duplicado_verificado")
        self.assertNotEqual(decisions["Incline_Push-Up"]["estado"],
                            "duplicado_verificado")

    def test_22_pending_cases_are_resolved_individually(self):
        self.assertEqual(22,len(catalog.REVIEWED_22))
        with catalog.REVIEW_REPORT.open(encoding="utf-8",newline="") as handle:
            review_rows=list(csv.DictReader(handle))
        self.assertEqual(22,len(review_rows))
        self.assertEqual(8,sum(x["estado"]=="duplicado_verificado" for x in review_rows))
        self.assertEqual(14,sum(x["estado"]=="nuevo_por_evaluar" for x in review_rows))
        with catalog.AUDIT.open(encoding="utf-8",newline="") as handle:
            decisions={x["id_fuente_publico"]:x for x in csv.DictReader(handle)}
        for id,(decision,old_id,reason) in catalog.REVIEWED_22.items():
            self.assertIn(id,decisions)
            self.assertEqual(decision,decisions[id]["estado"],id)
            self.assertEqual(old_id,decisions[id]["id_asterion_relacionado"],id)
            self.assertTrue(decisions[id]["regla"].startswith("auditoria_individual:"),id)
            self.assertTrue(reason)
        self.assertEqual(0,sum(x["estado"]=="posible_duplicado"
                              for x in decisions.values()))
        self.assertEqual("duplicado_verificado",
                         decisions["Seated_Side_Lateral_Raise"]["estado"])
        self.assertEqual("elevacion_lateral_v04",
                         decisions["Seated_Side_Lateral_Raise"]["id_asterion_relacionado"])
        self.assertEqual("press_hombros_v02",
                         decisions["Seated_Dumbbell_Press"]["id_asterion_relacionado"])

    def test_five_without_source_steps_are_flagged_for_technical_holds(self):
        missing={
            "Iron_Cross":"identidad_ambigua_y_sin_instrucciones",
            "One-Arm_Kettlebell_Swings":"balistico_unilateral_sin_instrucciones",
            "Push_Press":"levantamiento_tecnico_sin_instrucciones",
            "Side_Bridge":"estatico_sin_instrucciones",
            "Side_Jackknife":"variante_ambigua_sin_instrucciones",
        }
        new=[x for x in self.data["ejercicios"]
             if x["origen"]=="dominio_publico"]
        rows={x["source_public_domain_id"]:x for x in new}
        for id,reason in missing.items():
            self.assertTrue(rows[id]["fuente_sin_pasos"])
            self.assertEqual(reason,rows[id]["razon_bloqueo_es"])
            self.assertFalse(rows[id]["apto_para_rutinas_automaticas"])
            self.assertIn("La fuente no aporta instrucciones",
                          rows[id]["instrucciones_es"][-1])

    def test_static_side_bridge_has_time_unit_not_repetitions(self):
        new=[x for x in self.data["ejercicios"]
             if x["origen"]=="dominio_publico"]
        matched=[x for x in new if x["source_public_domain_id"]=="Side_Bridge"]
        self.assertEqual(1,len(matched))
        with (catalog.DEST/"volume_profiles.tsv").open(encoding="utf-8") as f:
            volumes={x["variant_id"]:x
                     for x in csv.DictReader(f,delimiter="\t")}
        self.assertEqual("SECONDS",volumes[matched[0]["id"]]["unit"])

    def test_unknown_source_schema_is_rejected(self):
        with self.assertRaises(ValueError):catalog.build([])
        with self.assertRaises(ValueError):
            catalog.category({"category":"strength","primaryMuscles":["unrecognized"]})

if __name__=="__main__":
    unittest.main()
