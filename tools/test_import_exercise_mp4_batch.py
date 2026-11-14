import hashlib
import json
import shutil
import subprocess
import tempfile
import unittest
import zipfile
from pathlib import Path

from import_exercise_mp4_batch import import_batch


@unittest.skipUnless(shutil.which("ffmpeg") and shutil.which("ffprobe"),
                     "Se necesitan ffmpeg y ffprobe")
class ImportExerciseMp4BatchTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(prefix="asterion_test_videos_")
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.records = []
        for id, color in [("flexiones_v04", "blue"),
                          ("puente_gluteo_v02", "green")]:
            file = self.root / (id + ".mp4")
            subprocess.run(
                ["ffmpeg", "-hide_banner", "-loglevel", "error", "-y",
                 "-f", "lavfi", "-i",
                 "color=c=" + color + ":size=640x360:rate=10",
                 "-t", "1.2", "-an", "-c:v", "libx264",
                 "-pix_fmt", "yuv420p", str(file)],
                check=True)
            content = file.read_bytes()
            self.records.append({
                "id": id,
                "archivo": file.name,
                "sha256": hashlib.sha256(content).hexdigest(),
                "tamano_bytes": len(content),
                "estado": "prototipo_visual_sin_validacion_tecnica",
            })
        self.zip = self.root / "lote.zip"
        self.write_zip()

    def write_zip(self, modify=None):
        manifest = {
            "total_catalogo": 2104,
            "videos_reales_en_lote": len(self.records),
            "videos": [dict(row) for row in self.records],
        }
        if modify:
            modify(manifest)
        with zipfile.ZipFile(self.zip, "w") as archive:
            archive.writestr("manifest.json", json.dumps(manifest))
            for record in self.records:
                file = self.root / record["archivo"]
                archive.write(file, arcname=file.name)

    def test_imports_both_videos_only_to_debug_directory(self):
        directory = self.root / "debug_video_assets"
        result = import_batch(self.zip, destination=directory)
        self.assertEqual(2, result["videos_verificados"])
        self.assertEqual("debug", result["modo"])
        self.assertFalse(result["aprobacion_tecnica_automatica"])
        self.assertEqual(
            {"flexiones_v04.mp4", "puente_gluteo_v02.mp4"},
            {path.name for path in directory.glob("*.mp4")})
        self.assertTrue((directory / "import_receipt.json").exists())

    def test_refuses_unapproved_videos_in_production(self):
        with self.assertRaisesRegex(ValueError, "sin evidencia"):
            import_batch(self.zip, mode="release",
                         destination=self.root / "release")

    def test_refuses_wrong_hash(self):
        def corrupt(manifest):
            manifest["videos"][0]["sha256"] = "0" * 64
        self.write_zip(corrupt)
        with self.assertRaisesRegex(ValueError, "Hash SHA256"):
            import_batch(self.zip, destination=self.root / "invalid")

    def test_refuses_id_not_in_catalogue(self):
        def corrupt(manifest):
            manifest["videos"][0]["id"] = "ejercicio_que_no_existe"
        self.write_zip(corrupt)
        with self.assertRaisesRegex(ValueError, "Archivos no declarados"):
            import_batch(self.zip, destination=self.root / "invalid")

    def test_refuses_rerun_over_existing_asset_without_replace(self):
        output = self.root / "once"
        import_batch(self.zip, destination=output)
        with self.assertRaisesRegex(ValueError, "previamente instalado"):
            import_batch(self.zip, destination=output)


if __name__ == "__main__":
    unittest.main()
