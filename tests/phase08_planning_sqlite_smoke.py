#!/usr/bin/env python3
"""Execute Java-defined phase-08 SQLite schema/migrations in local sqlite3; NO APK."""
import sqlite3
import unittest
from pathlib import Path
from java_sql_constants import java_sql_constant

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "app/src/main/java/pe/asterionsystem/app/data/AsterionDatabaseHelper.java"
V2 = ("ALTER_V1_TO_V2", "BACKFILL_V2", "CREATE_UPDATED_INDEX",
      "CREATE_INSERT_TRIGGER", "CREATE_UPDATE_TRIGGER")
V3 = ("CREATE_CATEGORIES", "CREATE_ITEMS", "CREATE_TAGS", "CREATE_ITEM_TAGS",
      "CREATE_CATEGORY_INDEX", "CREATE_ITEM_INDEX")
V4 = ("CREATE_PLAN_TABLE", "CREATE_PLAN_INDEX", "CREATE_PLAN_OVERLAP_INSERT",
      "CREATE_PLAN_OVERLAP_UPDATE", "CREATE_PLAN_ACTIVITY_INSERT", "CREATE_PLAN_ACTIVITY_UPDATE")


class PlanningSQLiteTest(unittest.TestCase):
    def setUp(self):
        self.db = sqlite3.connect(":memory:")
        self.addCleanup(self.db.close)
        self.db.execute("PRAGMA foreign_keys=ON")
        self.db.execute(java_sql_constant(JAVA, "CREATE_V1"))
        for key in V2 + V3:
            self.db.execute(java_sql_constant(JAVA, key))
        self.db.execute("INSERT INTO technical_records VALUES (?,?,?,?,?)",
                        ("old", "Anterior", "No borrar", 100, 100))
        self.db.execute("INSERT INTO catalog_categories VALUES (?,?,?)",
                        ("practica", "Práctica", 0))
        self.db.execute("INSERT INTO catalog_items VALUES (?,?,?,?,?)",
                        ("activity-one", "Actividad 1", "Prueba", "activity", "practica"))
        self.db.execute("INSERT INTO catalog_items VALUES (?,?,?,?,?)",
                        ("activity-two", "Actividad 2", "Prueba", "activity", "practica"))
        self.db.execute("INSERT INTO catalog_items VALUES (?,?,?,?,?)",
                        ("not-activity", "Contenido", "Prueba", "content", "practica"))
        for key in V4:
            self.db.execute(java_sql_constant(JAVA, key))

    def put(self, key, activity="activity-one", day="2026-11-04", start=540, duration=60):
        self.db.execute(
            "INSERT INTO planned_activities VALUES (?,?,?,?,?,?,?)",
            (key, activity, day, start, duration, 100, 100))

    def test_keeps_previous_data_and_catalog(self):
        self.assertEqual("No borrar", self.db.execute(
            "SELECT note FROM technical_records WHERE id='old'").fetchone()[0])
        self.assertEqual(3, self.db.execute("SELECT COUNT(*) FROM catalog_items").fetchone()[0])

    def test_add_and_list_by_date(self):
        self.put("first")
        self.put("other", "activity-two", start=600, duration=30)
        self.put("tomorrow", day="2026-11-05")
        result = self.db.execute(
            "SELECT id FROM planned_activities WHERE local_date=? ORDER BY start_minute,id",
            ("2026-11-04",)).fetchall()
        self.assertEqual([("first",), ("other",)], result)

    def test_rejects_overlapping_insert(self):
        self.put("first")
        with self.assertRaises(sqlite3.IntegrityError):
            self.put("overlap", "activity-two", start=599, duration=30)
        self.assertEqual(1, self.db.execute("SELECT COUNT(*) FROM planned_activities").fetchone()[0])

    def test_rejects_overlapping_update_and_keeps_record(self):
        self.put("first")
        self.put("second", start=600)
        with self.assertRaises(sqlite3.IntegrityError):
            self.db.execute("UPDATE planned_activities SET start_minute=? WHERE id=?",
                            (570, "second"))
        self.assertEqual(600, self.db.execute(
            "SELECT start_minute FROM planned_activities WHERE id='second'").fetchone()[0])

    def test_rejects_content_and_missing_catalog(self):
        with self.assertRaises(sqlite3.IntegrityError):
            self.put("content", "not-activity")
        with self.assertRaises(sqlite3.IntegrityError):
            self.put("missing", "missing-id")

    def test_prevents_cross_midnight_and_invalid_duration(self):
        with self.assertRaises(sqlite3.IntegrityError):
            self.put("late", start=1430, duration=30)
        with self.assertRaises(sqlite3.IntegrityError):
            self.put("zero", duration=0)

    def test_delete_and_foreign_key_integrity(self):
        self.put("first")
        with self.assertRaises(sqlite3.IntegrityError):
            self.db.execute("DELETE FROM catalog_items WHERE id='activity-one'")
        self.assertEqual(1, self.db.execute(
            "DELETE FROM planned_activities WHERE id=?", ("first",)).rowcount)

    def test_schema_has_guards_and_no_destructive_migration(self):
        source = JAVA.read_text(encoding="utf-8")
        self.assertIn("DATABASE_VERSION = 4", source)
        self.assertIn("migrateFromV3ToV4", source)
        self.assertNotIn("DROP TABLE", source.upper())
        self.assertEqual(4, self.db.execute(
            "SELECT COUNT(*) FROM sqlite_master WHERE type='trigger' "
            "AND name LIKE 'trg_plan_%'").fetchone()[0])


if __name__ == "__main__":
    unittest.main(verbosity=2)
