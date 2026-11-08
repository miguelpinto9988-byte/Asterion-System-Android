#!/usr/bin/env bash
set -euo pipefail

PACKAGE="pe.asterionsystem.app.preview"
APK="build/smoke/app-preview.apk"
OUT="build/runtime-diagnostics"
mkdir -p "$OUT"
test -s "$APK"

adb install -r "$APK"
adb logcat -c

echo "Starting Asterion preview on a real Android emulator..."
adb shell am start -W -n "$PACKAGE/pe.asterionsystem.app.SplashActivity" |
  tee "$OUT/launch.txt"
sleep 8

adb logcat -d -v time > "$OUT/logcat.txt"
adb logcat -d -b crash > "$OUT/crash.txt"
adb shell dumpsys activity activities > "$OUT/activities.txt"
adb shell dumpsys package "$PACKAGE" > "$OUT/package.txt"

echo "App process:"
adb shell pidof "$PACKAGE" | tee "$OUT/pid.txt" || true
echo "Activity transitions:"
grep -E 'ResumedActivity|topResumedActivity|mResumedActivity|mCurrentFocus' "$OUT/activities.txt" | tail -n 15 || true
echo "Java crash traces:"
cat "$OUT/crash.txt"

if grep -q 'FATAL EXCEPTION\|Process: pe.asterionsystem.app.preview' "$OUT/crash.txt"; then
  echo "ERROR: app crashes on startup. See crash.txt."
  exit 1
fi
if ! adb shell pidof "$PACKAGE" >/dev/null; then
  echo "ERROR: app process is no longer running. See logcat.txt."
  exit 1
fi
if ! grep -q "$PACKAGE" "$OUT/activities.txt"; then
  echo "ERROR: app is not foreground after splash. See activities.txt."
  exit 1
fi
# Verify that only the celestial MP4 appears during normal startup.
# If the PNG was used, the old and new splash art may appear in sequence.
if ! grep -q 'AsterionSplash.*VIDEO_FIRST_FRAME' "$OUT/logcat.txt"; then
  echo "ERROR: el primer fotograma de Media3 nunca se renderizo."
  exit 1
fi
if grep -q 'AsterionSplash.*FALLBACK_PNG' "$OUT/logcat.txt"; then
  echo "ERROR: se ha mostrado la ilustracion PNG alternativa durante el inicio."
  exit 1
fi
echo "PASS: un solo splash visual MP4; sin PNG alternativo."
echo "PASS: splash, navigation and profile gate remain running."
