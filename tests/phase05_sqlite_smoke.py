#!/usr/bin/env python3
"""Execute phase-05 Java-defined schema SQL with Python sqlite3. NO Android/APK build.

This verifies migration/constraint semantics but does not validate Android SQLiteOpenHelper
lifecycle, device-specific SQLite versions, or UI integration.
"""
from pathlib import Path
import re
import sqlite3
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA_SOURCE = ROOT / "app/src/main/java/pe/asterionsystem/app/data/AsterionDatabaseHelper.java"

NAMES = (
    "CREATE_V1", "ALTER_V1_TO_V2", "BACKFILL_V2", "CREATE_UPDATED_INDEX",
    "CREATE_INSERT_TRIGGER", "CREATE_UPDATE_TRIGGER",
)


from java_sql_constants import java_sql_constant


def java_sql(name: str) -> str:
    return java_sql_constant(JAVA_SOURCE, name)


SQL = {name: java_sql(name) for name in NAMES}


def install_v2(connection: sqlite3.Connection) -> None:
    for name in NAMES[1:]:
        connection.execute(SQL[name])


class SchemaTest(unittest.TestCase):
    def setUp(self) -> None:
        self.db = sqlite3.connect(":memory:")
        self.addCleanup(self.db.close)
        self.db.execute("PRAGMA foreign_keys=ON")

    def initialize(self) -> None:
        self.db.execute(SQL["CREATE_V1"])
        install_v2(self.db)

    def test_clean_install_create_read_update_delete(self) -> None:
        self.initialize()
        self.db.execute(
            "INSERT INTO technical_records (id,title,note,created_at_ms,updated_at_ms) "
            "VALUES (?,?,?,?,?)", ("a1", "Prueba", "Descripción", 100, 100))
        row = self.db.execute(
            "SELECT title,note FROM technical_records WHERE id=?", ("a1",)).fetchone()
        self.assertEqual(("Prueba", "Descripción"), row)
        self.assertEqual(
            1,
            self.db.execute(
                "UPDATE technical_records SET title=?, updated_at_ms=? WHERE id=? AND created_at_ms=?",
                ("Actualizado", 110, "a1", 100)).rowcount)
        self.assertEqual(
            "Actualizado",
            self.db.execute("SELECT title FROM technical_records WHERE id='a1'").fetchone()[0])
        self.assertEqual(
            1, self.db.execute("DELETE FROM technical_records WHERE id=?", ("a1",)).rowcount)
        self.assertEqual(0, self.db.execute("SELECT COUNT(*) FROM technical_records").fetchone()[0])

    def test_upgrade_from_v1_preserves_existing_rows(self) -> None:
        self.db.execute(SQL["CREATE_V1"])
        self.db.execute(
            "INSERT INTO technical_records (id,title,note,created_at_ms) VALUES (?,?,?,?)",
            ("legacy", "Anterior", "No borrar", 170))
        install_v2(self.db)
        self.assertEqual(
            ("legacy", "Anterior", "No borrar", 170, 170),
            self.db.execute(
                "SELECT id,title,note,created_at_ms,updated_at_ms FROM technical_records"
            ).fetchone())

    def test_constraints_protect_integrity(self) -> None:
        self.initialize()
        invalid = (
            ("", "Título", "", 1, 1),
            ("x" * 65, "Título", "", 1, 1),
            ("test", "", "", 1, 1),
            ("test", "t" * 121, "", 1, 1),
            ("test", "Título", "x" * 1001, 1, 1),
            ("test", "Título", "", -1, 0),
            ("test", "Título", "", 7, 6),
        )
        for row in invalid:
            with self.subTest(row=row[:2]), self.assertRaises(sqlite3.IntegrityError):
                self.db.execute(
                    "INSERT INTO technical_records (id,title,note,created_at_ms,updated_at_ms) "
                    "VALUES (?,?,?,?,?)", row)
        self.db.execute(
            "INSERT INTO technical_records (id,title,note,created_at_ms,updated_at_ms) "
            "VALUES (?,?,?,?,?)", ("ok", "Válido", "", 10, 10))
        with self.assertRaises(sqlite3.IntegrityError):
            self.db.execute(
                "INSERT INTO technical_records (id,title,note,created_at_ms,updated_at_ms) "
                "VALUES (?,?,?,?,?)", ("ok", "Repetido", "", 10, 10))
        with self.assertRaises(sqlite3.IntegrityError):
            self.db.execute(
                "UPDATE technical_records SET updated_at_ms=2 WHERE id=?", ("ok",))

    def test_sort_and_pagination(self) -> None:
        self.initialize()
        self.db.executemany(
            "INSERT INTO technical_records (id,title,note,created_at_ms,updated_at_ms) "
            "VALUES (?,?,?,?,?)",
            (("c", "C", "", 1, 20), ("a", "A", "", 1, 20), ("b", "B", "", 1, 30)))
        ids = [row[0] for row in self.db.execute(
            "SELECT id FROM technical_records ORDER BY updated_at_ms DESC,id ASC LIMIT 2 OFFSET 1")]
        self.assertEqual(["a", "c"], ids)

    def test_no_destructive_upgrades(self) -> None:
        source = JAVA_SOURCE.read_text(encoding="utf-8")
        self.assertNotIn("DROP TABLE", source.upper())
        self.assertIn("onDowngrade", source)
        self.assertIn("Migración no disponible", source)


if __name__ == "__main__":
    unittest.main(verbosity=2)
