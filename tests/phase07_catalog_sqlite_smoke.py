#!/usr/bin/env python3
"""Test SQL constants from Android Java in a real local SQLite engine. No APK, no Android."""
from pathlib import Path
import sqlite3
import unittest

from java_sql_constants import java_sql_constant

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "app/src/main/java/pe/asterionsystem/app/data/AsterionDatabaseHelper.java"
REPOSITORY = ROOT / "app/src/main/java/pe/asterionsystem/app/data/SQLiteCatalogRepository.java"
V2 = ("ALTER_V1_TO_V2", "BACKFILL_V2", "CREATE_UPDATED_INDEX",
      "CREATE_INSERT_TRIGGER", "CREATE_UPDATE_TRIGGER")
V3 = ("CREATE_CATEGORIES", "CREATE_ITEMS", "CREATE_TAGS", "CREATE_ITEM_TAGS",
      "CREATE_CATEGORY_INDEX", "CREATE_ITEM_INDEX")


class CatalogSqliteSmokeTest(unittest.TestCase):
    def setUp(self):
        self.db = sqlite3.connect(":memory:")
        self.addCleanup(self.db.close)
        self.db.execute("PRAGMA foreign_keys=ON")

    def run_migration(self, constants):
        for name in constants:
            self.db.execute(java_sql_constant(JAVA, name))

    def create_v1(self):
        self.db.execute(java_sql_constant(JAVA, "CREATE_V1"))

    def create_v2(self):
        self.create_v1()
        self.run_migration(V2)

    def create_v3(self):
        self.create_v2()
        self.run_migration(V3)

    def seed(self):
        self.db.executemany(
            "INSERT INTO catalog_categories(id,name,sort_order) VALUES(?,?,?)",
            [("general", "General", 0), ("conocimiento", "Conocimiento", 1),
             ("practica", "Práctica", 2)])
        self.db.executemany(
            "INSERT INTO catalog_items(id,title,description,kind,category_id) "
            "VALUES(?,?,?,?,?)",
            [("demo-intro", "Introducción al catálogo", "Texto de prueba", "content", "general"),
             ("demo-notas", "Notas de ejemplo", "Contenido ilustrativo", "content", "conocimiento"),
             ("demo-revision", "Actividad de comprobación", "Caso de prueba", "activity", "practica"),
             ("demo-navegacion", "Revisión de navegación", "Caso ilustrativo", "activity", "general")])
        self.db.executemany(
            "INSERT INTO catalog_tags(id,label) VALUES(?,?)",
            [("demo", "Demostración"), ("lectura", "Lectura"), ("consulta", "Consulta")])
        self.db.executemany(
            "INSERT INTO catalog_item_tags(item_id,tag_id) VALUES(?,?)",
            [("demo-intro", "demo"), ("demo-intro", "lectura"),
             ("demo-notas", "demo"), ("demo-notas", "consulta")])

    def test_upgrades_preserve_technical_data(self):
        self.create_v2()
        self.db.execute(
            "INSERT INTO technical_records VALUES(?,?,?,?,?)",
            ("old", "Registro previo", "No borrar", 1000, 1100))
        self.run_migration(V3)
        self.assertEqual(
            ("old", "Registro previo", "No borrar", 1000, 1100),
            self.db.execute("SELECT * FROM technical_records").fetchone())
        self.assertEqual(4, self.db.execute(
            "SELECT COUNT(*) FROM sqlite_master WHERE type='table' "
            "AND name LIKE 'catalog_%'").fetchone()[0])

    def test_full_v1_to_v3_path(self):
        self.create_v1()
        self.db.execute(
            "INSERT INTO technical_records(id,title,note,created_at_ms) "
            "VALUES('legacy','Anterior','test',15)")
        self.run_migration(V2)
        self.run_migration(V3)
        self.assertEqual(
            (15, 15),
            self.db.execute("SELECT created_at_ms,updated_at_ms FROM technical_records").fetchone())

    def test_categories_kind_and_foreign_key_constraints(self):
        self.create_v3()
        self.seed()
        with self.assertRaises(sqlite3.IntegrityError):
            self.db.execute(
                "INSERT INTO catalog_items VALUES ('bad','Título','texto','invalid','general')")
        with self.assertRaises(sqlite3.IntegrityError):
            self.db.execute(
                "INSERT INTO catalog_items VALUES ('bad','Título','texto','content','missing')")
        with self.assertRaises(sqlite3.IntegrityError):
            self.db.execute("DELETE FROM catalog_categories WHERE id='general'")
        with self.assertRaises(sqlite3.IntegrityError):
            self.db.execute(
                "INSERT INTO catalog_item_tags(item_id,tag_id) VALUES ('bad','demo')")
        self.assertEqual(4, self.db.execute("SELECT COUNT(*) FROM catalog_items").fetchone()[0])

    def test_catalog_filters_search_and_paging(self):
        self.create_v3()
        self.seed()
        self.assertEqual(
            ["demo-intro", "demo-notas"],
            [row[0] for row in self.db.execute(
                "SELECT id FROM catalog_items WHERE kind=? ORDER BY title COLLATE NOCASE, id",
                ("content",))])
        self.assertEqual(
            ["demo-intro", "demo-navegacion"],
            [row[0] for row in self.db.execute(
                "SELECT id FROM catalog_items WHERE category_id=? ORDER BY id", ("general",))])
        self.assertEqual(
            ["demo-revision"],
            [row[0] for row in self.db.execute(
                "SELECT id FROM catalog_items WHERE title LIKE ? ESCAPE '!'",
                ("%comprobación%",))])
        self.assertEqual(
            ["demo-intro"],
            [row[0] for row in self.db.execute(
                "SELECT i.id FROM catalog_items i "
                "WHERE EXISTS (SELECT 1 FROM catalog_item_tags jt "
                "JOIN catalog_tags t ON t.id=jt.tag_id "
                "WHERE jt.item_id=i.id AND t.label LIKE ? ESCAPE '!')",
                ("%Lectura%",))])
        self.assertEqual(
            2, len(self.db.execute(
                "SELECT id FROM catalog_items ORDER BY id LIMIT ? OFFSET ?", (2, 1)).fetchall()))

    def test_literal_wildcards_and_indexes(self):
        self.create_v3()
        self.seed()
        self.assertEqual([], self.db.execute(
            "SELECT id FROM catalog_items WHERE title LIKE ? ESCAPE '!'",
            ("%!%%",)).fetchall())
        self.assertEqual([], self.db.execute(
            "SELECT id FROM catalog_items WHERE title LIKE ? ESCAPE '!'",
            ("%!_%",)).fetchall())
        indexes = {row[1] for row in self.db.execute("PRAGMA index_list(catalog_items)")}
        self.assertIn("idx_catalog_item_filters", indexes)

    def test_no_destructive_migration_or_sql_interpolation(self):
        database = JAVA.read_text(encoding="utf-8")
        adapter = REPOSITORY.read_text(encoding="utf-8")
        self.assertNotIn("DROP TABLE", database.upper())
        self.assertIn("migrateFromV2ToV3", database)
        self.assertIn("rawQuery", adapter)
        self.assertIn("LIKE ? ESCAPE '!'", adapter)
        self.assertIn("WHERE i.id = ?", adapter)
        self.assertIn("requireWorker()", adapter)
        for demo_id in ("demo-intro", "demo-notas", "demo-revision", "demo-navegacion"):
            self.assertIn(demo_id, database)


if __name__ == "__main__":
    unittest.main(verbosity=2)
