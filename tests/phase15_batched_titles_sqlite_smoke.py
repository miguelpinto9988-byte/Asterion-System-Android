#!/usr/bin/env python3
"""SQLite comparison of parameter-bound IN batching with per-ID lookups, no Android."""
import sqlite3
import unittest


class BatchedTitlesTest(unittest.TestCase):
    def setUp(self):
        self.db = sqlite3.connect(":memory:")
        self.addCleanup(self.db.close)
        self.db.execute("CREATE TABLE catalog_items (id TEXT PRIMARY KEY, title TEXT NOT NULL)")
        self.db.executemany("INSERT INTO catalog_items VALUES (?,?)",
                            [(f"id-{i}", f"Actividad {i}") for i in range(450)])

    def bulk(self, identifiers):
        result = {}
        # Same SQL shape and chunk size as the Java production adapter.
        unique = list(dict.fromkeys(identifiers))
        for i in range(0, len(unique), 200):
            subset = unique[i:i+200]
            if not subset:
                continue
            sql = "SELECT id,title FROM catalog_items WHERE id IN (" + ",".join("?" for _ in subset) + ")"
            for k, v in self.db.execute(sql, subset):
                result[k] = v
        return result

    def test_batch_matches_naive_lookup(self):
        keys = [f"id-{i}" for i in range(450)]
        keys += ["id-1", "missing", "id-20"]
        expected = {}
        for key in keys:
            row = self.db.execute("SELECT title FROM catalog_items WHERE id=?", (key,)).fetchone()
            if row:
                expected[key] = row[0]
        self.assertEqual(expected, self.bulk(keys))

    def test_empty_input(self):
        self.assertEqual({}, self.bulk([]))

    def test_missing_values_are_omitted(self):
        self.assertEqual({}, self.bulk(["missing"]))

    def test_sql_injection_is_treated_as_identifier(self):
        self.assertEqual({}, self.bulk(["id-1' OR 1=1 --"]))
        self.assertEqual(450, self.db.execute("SELECT COUNT(*) FROM catalog_items").fetchone()[0])

    def test_batch_edges(self):
        for n in (1, 199, 200, 201, 400, 450):
            with self.subTest(n=n):
                self.assertEqual(n, len(self.bulk([f"id-{i}" for i in range(n)])))


if __name__ == "__main__":
    unittest.main(verbosity=2)
