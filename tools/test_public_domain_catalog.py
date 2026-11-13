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
        self.assertGreater(self.manifest["candidatos_de_duplicado"],0)

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
            self.assertFalse(x.get("instrucciones_es"))
            self.assertNotIn("gymvisual",json.dumps(x).lower())
        self.assertTrue(self.data["no_incluye_contenido_mit_previo"])

    def test_all_five_categories_and Spanish_copy(self):
        cats={x["categoria"] for x in self.data["ejercicios"]
              if x["origen"]=="dominio_publico"}
        self.assertEqual(cats,{"tren_superior","tren_central","tren_inferior","movilidad","cardio"})
        self.assertTrue(all(x["nombre_es"] and x["material_es"] for x in
                            self.data["ejercicios"]))

    def test_unknown_source_schema_is_rejected(self):
        with self.assertRaises(ValueError):catalog.build([])
        with self.assertRaises(ValueError):
            catalog.category({"category":"strength","primaryMuscles":["unrecognized"]})

if __name__=="__main__":
    unittest.main()
