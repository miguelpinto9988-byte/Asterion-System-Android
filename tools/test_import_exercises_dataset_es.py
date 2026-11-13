import unittest
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).parent.resolve()))
import import_exercises_dataset_es as importer

class ImportTest(unittest.TestCase):
    def test_categories_are_compatible(self):
        self.assertEqual("tren_inferior",importer.code_for("upper legs","squat"))
        self.assertEqual("tren_central",importer.code_for("waist","sit-up"))
        self.assertEqual("tren_superior",importer.code_for("chest","chest press"))
        self.assertEqual("movilidad",importer.code_for("chest","stretch"))
        self.assertEqual("cardio",importer.code_for("cardio","running"))
    def test_labels_are_in_spanish(self):
        title=importer.title_es("barbell full squat","upper legs","barbell")
        self.assertIn("Sentadilla",title)
        self.assertIn("barra",title)
        self.assertNotIn("barbell",title)
    def test_unknown_bodypart_rejected(self):
        with self.assertRaises(ValueError):importer.code_for("inexistente","x")
    def test_refuses_empty_or_incomplete_catalogue(self):
        with self.assertRaises(ValueError):importer.transform([])
    def test_versions_are_fixed(self):
        self.assertEqual("7455efae41b330c265e7cd4b78dfa848e7ce5ebd",
                         importer.UPSTREAM)
        self.assertEqual("3cbb77b678c40d7342897d38e4c61e9502f829a7",
                         importer.BLOB_SHA)

if __name__=="__main__":unittest.main()
