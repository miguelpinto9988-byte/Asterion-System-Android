#!/usr/bin/env python3
"""Generate a local H.264 MP4 from the committed, high-definition animated WebP.

Used by the feature-branch CI before Gradle compiles an APK. No remote media
URLs, network calls or APK modifications are involved.
Requirements: Pillow and ffmpeg with libx264.
"""
import argparse
import json
from pathlib import Path
import subprocess
import tempfile
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "app/src/main/assets/asterion/asterion_splash_celestial.webp"
TARGET = ROOT / "app/src/main/res/raw/asterion_splash_celestial.mp4"
FRAME_MILLIS = 200
DURATION = 1.6


def build(source: Path, target: Path) -> None:
    if not source.is_file():
        raise FileNotFoundError(f"Falta el recurso WebP fuente: {source}")
    with Image.open(source) as image:
        if image.format != "WEBP" or image.size != (1080, 1920):
            raise ValueError(f"Imagen de origen no es WebP Full HD vertical: {image.format}, {image.size}")
        frames = getattr(image, "n_frames", 1)
        if frames != 8:
            raise ValueError(f"Se esperan ocho fotogramas de origen, recibidos: {frames}")
        with tempfile.TemporaryDirectory(prefix="asterion_mp4_") as working:
            frame_dir = Path(working)
            lines = ["ffconcat version 1.0"]
            for i in range(frames):
                image.seek(i)
                name = f"frame_{i:03d}.png"
                image.convert("RGB").save(frame_dir / name, optimize=False)
                lines.extend((f"file '{name}'", f"duration {FRAME_MILLIS / 1000:.3f}"))
            # ffmpeg's concat demuxer needs its last frame repeated for duration.
            lines.append(f"file 'frame_{frames - 1:03d}.png'")
            manifest = frame_dir / "frames.ffconcat"
            manifest.write_text("\n".join(lines) + "\n", encoding="utf-8")
            target.parent.mkdir(parents=True, exist_ok=True)
            command = [
                "ffmpeg", "-hide_banner", "-loglevel", "error", "-y",
                "-f", "concat", "-safe", "0", "-i", str(manifest),
                "-vf", "fps=24,format=yuv420p", "-t", str(DURATION),
                "-an", "-c:v", "libx264", "-preset", "medium", "-crf", "20",
                "-movflags", "+faststart", str(target)
            ]
            subprocess.run(command, check=True)
    if not target.is_file() or target.stat().st_size < 100000:
        raise RuntimeError("El MP4 generado no existe o parece incompleto")
    info = subprocess.run([
        "ffprobe", "-v", "error", "-select_streams", "v:0",
        "-show_entries", "stream=codec_name,width,height,nb_frames",
        "-show_entries", "format=duration", "-of", "json", str(target)
    ], check=True, capture_output=True, text=True)
    details = json.loads(info.stdout)
    track = details["streams"][0]
    if track["codec_name"] != "h264" or (track["width"], track["height"]) != (1080, 1920):
        raise RuntimeError(f"Se generó un video incompatible: {track}")
    print(f"MP4 generado: {target} ({target.stat().st_size} bytes); "
          f"fotogramas: {track.get('nb_frames')}, duración: {details['format']['duration']} s")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--source", type=Path, default=SOURCE)
    parser.add_argument("--output", type=Path, default=TARGET)
    arguments = parser.parse_args()
    build(arguments.source, arguments.output)
