#!/usr/bin/env python3
"""Regression: Android emulator's unrelated process crashes must not fail Asterion CI."""
from pathlib import Path
import subprocess
import tempfile

ROOT=Path(__file__).resolve().parents[1]
HELPER=ROOT/"ci/android_crash_filter.sh"
SMOKE=ROOT/"ci/android_runtime_smoke.sh"
script=SMOKE.read_text(encoding="utf-8")
helper=HELPER.read_text(encoding="utf-8")
assert 'source ci/android_crash_filter.sh' in script
assert 'android_app_crashed "$OUT/crash.txt" "$PACKAGE"' in script
assert "FATAL EXCEPTION\\|Process:" not in script
assert 'grep -Fq "Process: ${app_package},"' in helper
assert 'grep -Fq "Process: ${app_package}:"' in helper

def flagged(raw: str) -> bool:
    with tempfile.TemporaryDirectory() as folder:
        path=Path(folder)/"crash.txt"
        path.write_text(raw,encoding="utf-8")
        result=subprocess.run(
            ["bash","-c",
             'source ci/android_crash_filter.sh; android_app_crashed "$1" "$2"',
             "bash",str(path),"pe.asterionsystem.app"],
            cwd=ROOT,capture_output=True,text=True,check=False)
        assert result.returncode in (0,1),result.stderr
        return result.returncode==0

unrelated=(
    "11-10 E AndroidRuntime: FATAL EXCEPTION: DefaultDispatcher-worker-1\n"
    "11-10 E AndroidRuntime: Process: com.google.android.apps.wallpaper, PID: 2399\n"
    "11-10 E AndroidRuntime: java.util.ConcurrentModificationException\n"
)
asterion=(
    "11-10 E AndroidRuntime: FATAL EXCEPTION: main\n"
    "11-10 E AndroidRuntime: Process: pe.asterionsystem.app, PID: 2103\n"
)
assert not flagged(unrelated),"System wallpaper crashes must not fail our app"
assert flagged(asterion),"Asterion app crashes must still fail"
assert flagged(unrelated+asterion),"Asterion crashes hidden among others must fail"
assert flagged("E AndroidRuntime: Process: pe.asterionsystem.app:worker, PID: 100\n")
assert not flagged("E AndroidRuntime: Process: pe.asterionsystem.appdemo, PID: 200\n")
assert not flagged("E AndroidRuntime: FATAL EXCEPTION: main\n")
print("OK: CI crash filter ignores wallpaper but detects Asterion and child processes.")
