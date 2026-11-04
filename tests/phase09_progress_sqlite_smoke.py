#!/usr/bin/env python3
"""Phase 09 SQLite v4->v5 smoke tests from Java constants. No Android/Gradle/APK."""
from pathlib import Path
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


class TrackingSQLiteTest(unittest.TestCase):
    def setUp(self):
        self.db = sqlite3.connect(":memory:")
        self.addCleanup(self.db.close)
        self.db.execute("PRAGMA foreign_keys=ON")
        self.db.execute(java_sql_constant(JAVA, "CREATE_V1"))
        for key in V2 + V3 + V4:
            self.db.execute(java_sql_constant(JAVA, key))
        self.db.execute("INSERT INTO technical_records VALUES (?,?,?,?,?)",
                        ("old", "Registro", "Conservar", 100, 100))
        self.db.execute("INSERT INTO catalog_categories VALUES (?,?,?)",
                        ("general", "General", 0))
        self.db.execute("INSERT INTO catalog_items VALUES (?,?,?,?,?)",
                        ("act", "Actividad", "Demo", "activity", "general"))
        self.db.execute("INSERT INTO planned_activities VALUES (?,?,?,?,?,?,?)",
                        ("p1", "act", "2026-11-04", 540, 30, 100, 100))
        self.db.execute("INSERT INTO planned_activities VALUES (?,?,?,?,?,?,?)",
                        ("p2", "act", "2026-11-04", 600, 30, 100, 100))
        self.db.execute("INSERT INTO planned_activities VALUES (?,?,?,?,?,?,?)",
                        ("p3", "act", "2026-11-05", 540, 30, 100, 100))
        self.db.execute(java_sql_constant(JAVA, "CREATE_TRACKING_RESULTS"))

    def put(self, plan="p1", state="completed", note="", recorded=200):
        self.db.execute("INSERT INTO tracking_results VALUES (?,?,?,?)",
                        (plan, state, note, recorded))

    def test_upgrade_preserves_all_prior_tables_and_rows(self):
        self.assertEqual("Conservar", self.db.execute(
            "SELECT note FROM technical_records WHERE id='old'").fetchone()[0])
        self.assertEqual(3, self.db.execute("SELECT COUNT(*) FROM planned_activities").fetchone()[0])
        self.assertEqual(1, self.db.execute("SELECT COUNT(*) FROM catalog_items").fetchone()[0])

    def test_pending_is_absence_not_persisted_state(self):
        result = self.db.execute(
            "SELECT p.id, COALESCE(r.status,'pending') FROM planned_activities p "
            "LEFT JOIN tracking_results r ON r.schedule_id=p.id "
            "WHERE p.local_date=? ORDER BY p.start_minute",
            ("2026-11-04",)).fetchall()
        self.assertEqual([("p1", "pending"), ("p2", "pending")], result)

    def test_complete_and_skip_same_day(self):
        self.put("p1", "completed", "Bien", 201)
        self.put("p2", "skipped", "No realizada", 202)
        rows = self.db.execute(
            "SELECT status, COUNT(*) FROM tracking_results GROUP BY status").fetchall()
        self.assertEqual([("completed", 1), ("skipped", 1)], rows)

    def test_update_result_no_duplicate_records(self):
        self.put()
        self.assertEqual(1, self.db.execute(
            "UPDATE tracking_results SET status=?,note=?,recorded_at_ms=? WHERE schedule_id=?",
            ("skipped", "Corregido", 300, "p1")).rowcount)
        self.assertEqual(1, self.db.execute("SELECT COUNT(*) FROM tracking_results").fetchone()[0])
        self.assertEqual(("skipped", "Corregido"), self.db.execute(
            "SELECT status,note FROM tracking_results").fetchone())

    def test_clearing_restores_pending(self):
        self.put()
        self.db.execute("DELETE FROM tracking_results WHERE schedule_id=?", ("p1",))
        self.assertEqual("pending", self.db.execute(
            "SELECT COALESCE(r.status,'pending') FROM planned_activities p "
            "LEFT JOIN tracking_results r ON r.schedule_id=p.id WHERE p.id='p1'").fetchone()[0])

    def test_rejects_invalid_status_or_oversize_note(self):
        for value in ("pending", "unknown", ""):
            with self.subTest(value=value), self.assertRaises(sqlite3.IntegrityError):
                self.put(state=value)
        with self.assertRaises(sqlite3.IntegrityError):
            self.put(note="x" * 501)
        with self.assertRaises(sqlite3.IntegrityError):
            self.put(recorded=-1)

    def test_one_outcome_per_planned_activity(self):
        self.put()
        with self.assertRaises(sqlite3.IntegrityError):
            self.put()

    def test_reference_required_and_cascading_delete(self):
        with self.assertRaises(sqlite3.IntegrityError):
            self.put(plan="missing")
        self.put()
        self.db.execute("DELETE FROM planned_activities WHERE id=?", ("p1",))
        self.assertEqual(0, self.db.execute("SELECT COUNT(*) FROM tracking_results").fetchone()[0])

    def test_week_query_does_not_include_next_monday(self):
        self.put("p1")
        self.db.execute("INSERT INTO planned_activities VALUES (?,?,?,?,?,?,?)",
                        ("next-week", "act", "2026-11-09", 540, 30, 100, 100))
        q = self.db.execute(
            "SELECT COUNT(*) FROM planned_activities "
            "WHERE local_date >= ? AND local_date < ?", ("2026-11-02", "2026-11-09"))
        self.assertEqual(3, q.fetchone()[0])

    def test_java_migration_is_non_destructive(self):
        source = JAVA.read_text(encoding="utf-8")
        self.assertIn("DATABASE_VERSION = 5", source)
        self.assertIn("migrateFromV4ToV5", source)
        self.assertNotIn("DROP TABLE", source.upper())


if __name__ == "__main__":
    unittest.main(verbosity=2)
