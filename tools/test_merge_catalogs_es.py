import json
import unittest
from pathlib import Path
import sys
sys.path.insert(0,str(Path(__file__).resolve().parent))
import merge_catalogs_es as merge

class MergeCatalogsTest(unittest.TestCase):
    def test_old_catalogue_is_preserved_and_unique_ids_are_distinct(self):
        m=merge.generate()
        data=json.loads((merge.DEST/"catalogo_es.json").read_text(encoding="utf-8"))
        assert m["originales"]==1257
        assert m["duplicados_confirmados"]==5
        assert data["total"]==1257+m["nuevos_provisionales"]
        self.assertEqual(data["total"],len(data["ejercicios"]))
        ids=[x["id"] for x in data["ejercicios"]]
        self.assertEqual(len(ids),len(set(ids)))
        self.assertEqual(1257,sum(x["origen"]=="asterion" for x in data["ejercicios"]))
        self.assertTrue(all(x["categoria"] in merge.CATEGORIES for x in data["ejercicios"]))
        self.assertTrue(all(not x["gif_incluido"] for x in data["ejercicios"]))
        self.assertGreater(m["posibles_duplicados_en_revision"],0)
        self.assertGreater(m["nuevos_provisionales"],0)
        self.assertEqual(1324,m["nuevos_provisionales"]
                         +m["duplicados_confirmados"]
                         +m["posibles_duplicados_en_revision"])
    def test_known_duplicate_uses_original_id_not_external_copy(self):
        data=json.loads((merge.DEST/"catalogo_es.json").read_text(encoding="utf-8"))
        by_id={x["id"]:x for x in data["ejercicios"]}
        for external,legacy in merge.CONFIRMED.items():
            self.assertNotIn(external,by_id)
            self.assertIn(legacy,by_id)
            self.assertEqual("asterion",by_id[legacy]["origen"])
    def test_unknown_difficulty_never_enters_automatic_planner(self):
        data=json.loads((merge.DEST/"catalogo_es.json").read_text(encoding="utf-8"))
        for row in data["ejercicios"]:
            if row["origen"]=="dataset_mit":
                self.assertEqual("pendiente_revision",row["nivel_tecnico"])
                self.assertFalse(row["apto_para_rutinas_automaticas"])
    def test_unknown_movement_does_not_get_a_false_exact_duplicate(self):
        row={"id":"ext_9999","nombre_original":"unknown rare movement",
             "material_original":"body weight","categoria":"movilidad"}
        self.assertEqual("nuevo_provisional",merge.assess(row,{})[0])

if __name__=="__main__":
    unittest.main()
