#!/usr/bin/env bash
set -euo pipefail

mkdir -p build/ui-screens
adb install -r build/smoke/app-debug.apk
adb install -r build/smoke/app-debug-androidTest.apk
adb shell am instrument -w -e class pe.asterionsystem.app.UiSnapshotTest#captureScreens \
  pe.asterionsystem.app.test/androidx.test.runner.AndroidJUnitRunner \
  | tee build/ui-screens/instrumentation.txt
adb pull /sdcard/Android/data/pe.asterionsystem.app/files/ui-screens/. build/ui-screens/
grep -q 'OK (1 test)' build/ui-screens/instrumentation.txt
test "$(find build/ui-screens -maxdepth 1 -name '*.png' | wc -l)" -ge 24
