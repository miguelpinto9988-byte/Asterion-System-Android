#!/usr/bin/env python3
"""Local SQLite v5 recovery checks from authoritative Java constants. No Android/APK."""
from pathlib import Path
from tempfile import TemporaryDirectory
import sqlite3
import unittest
from java_sql_constants import java_sql_constant

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "app/src/main/java/pe/asterionsystem/app/data/AsterionDatabaseHelper.java"
V2 = ("ALTER_V1_TO_V2", "BACKFILL_V2", "CREATE_UPDATED_INDEX",
      "CREATE_INSERT_TRIGGER", "CREATE_UPDATE_TRIGGER")
V3 = ("CREATE_CATEGORIES", "CREATE_ITEMS", "CREATE_TAGS", "CREATE_ITEM_TAGS",
      "CREATE_CATEGORY_INDEX", "CREATE_ITEM_INDEX")
V4 = ("CREATE_PLAN_TABLE", "CREATE_PLAN_INDEX", "CREATE_PLAN_OVERLAP_INSERT",
      "CREATE_PLAN_OVERLAP_UPDATE", "CREATE_PLAN_ACTIVITY_INSERT", "CREATE_PLAN_ACTIVITY_UPDATE")
V5 = ("CREATE_TRACKING_RESULTS",)


class OfflineIntegritySQLiteTest(unittest.TestCase):
    def setUp(self):
        self.temp = TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.path = Path(self.temp.name) / "local.db"
        self.db = sqlite3.connect(self.path, isolation_level=None)
        self.addCleanup(self.db.close)
        self.db.execute("PRAGMA foreign_keys=ON")
        self.db.execute("PRAGMA journal_mode=WAL")
        self.db.execute(java_sql_constant(JAVA, "CREATE_V1"))
        for key in V2 + V3 + V4 + V5:
            self.db.execute(java_sql_constant(JAVA, key))
        self.db.execute(
            "INSERT INTO catalog_categories VALUES (?,?,?)",
            ("general", "General", 0))
        self.db.execute(
            "INSERT INTO catalog_items VALUES (?,?,?,?,?)",
            ("activity", "Actividad", "Prueba", "activity", "general"))
        self.db.execute(
            "INSERT INTO technical_records VALUES (?,?,?,?,?)",
            ("existing", "Dato anterior", "No borrar", 1, 1))

    def add_plan(self, key, start=540, date="2026-11-04"):
        self.db.execute("INSERT INTO planned_activities VALUES (?,?,?,?,?,?,?)",
                        (key, "activity", date, start, 60, 1, 1))

    def test_readonly_integrity_and_fk_pragmas(self):
        self.assertEqual("ok",
                         self.db.execute("PRAGMA quick_check(1)").fetchone()[0])
        self.assertEqual([], self.db.execute("PRAGMA foreign_key_check").fetchall())
        self.assertEqual(1, self.db.execute(
            "SELECT COUNT(*) FROM catalog_items").fetchone()[0])

    def test_failed_transaction_rolls_back_without_erasing_previous_rows(self):
        self.db.execute("BEGIN IMMEDIATE")
        try:
            self.add_plan("one", 540)
            with self.assertRaises(sqlite3.IntegrityError):
                self.add_plan("overlap", 550)
        finally:
            self.db.execute("ROLLBACK")
        self.assertEqual(0, self.db.execute(
            "SELECT COUNT(*) FROM planned_activities").fetchone()[0])
        self.assertEqual("No borrar", self.db.execute(
            "SELECT note FROM technical_records WHERE id='existing'").fetchone()[0])

    def test_manual_retry_after_conflict_is_safe(self):
        self.add_plan("one", 540)
        with self.assertRaises(sqlite3.IntegrityError):
            self.add_plan("bad", 550)
        self.add_plan("two", 600)
        self.assertEqual(2, self.db.execute(
            "SELECT COUNT(*) FROM planned_activities").fetchone()[0])

    def test_fk_check_detects_invalid_row_without_auto_repair(self):
        self.db.execute("PRAGMA foreign_keys=OFF")
        self.db.execute(
            "INSERT INTO tracking_results VALUES (?,?,?,?)",
            ("orphan", "completed", "", 10))
        self.assertNotEqual([], self.db.execute("PRAGMA foreign_key_check").fetchall())
        self.assertEqual("ok", self.db.execute("PRAGMA quick_check(1)").fetchone()[0])
        # An explicit authorized cleanup in this test fixture only.
        self.db.execute("DELETE FROM tracking_results WHERE schedule_id=?", ("orphan",))
        self.db.execute("PRAGMA foreign_keys=ON")
        self.assertEqual([], self.db.execute("PRAGMA foreign_key_check").fetchall())

    def test_outcome_and_catalog_survive_close_and_reopen(self):
        self.add_plan("one", 540)
        self.db.execute("INSERT INTO tracking_results VALUES (?,?,?,?)",
                        ("one", "completed", "Hecho", 10))
        self.db.close()
        self.db = sqlite3.connect(self.path, isolation_level=None)
        self.db.execute("PRAGMA foreign_keys=ON")
        self.assertEqual(("completed", "Hecho"),
                         self.db.execute("SELECT status,note FROM tracking_results "
                                         "WHERE schedule_id='one'").fetchone())
        self.assertEqual(1, self.db.execute(
            "SELECT COUNT(*) FROM catalog_items").fetchone()[0])

    def test_database_remains_v5_with_no_destructive_migrations(self):
        source = JAVA.read_text(encoding="utf-8")
        self.assertIn("DATABASE_VERSION = 5", source)
        self.assertNotIn("DROP TABLE", source.upper())
        self.assertNotIn("migrateFromV5ToV6", source)


if __name__ == "__main__":
    unittest.main(verbosity=2)
