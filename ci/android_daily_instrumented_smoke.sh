#!/usr/bin/env bash
set -euo pipefail

PACKAGE="pe.asterionsystem.app"
TEST_PACKAGE="pe.asterionsystem.app.test"
TEST_APK="build/smoke/app-debug-androidTest.apk"
TEST_CLASS="pe.asterionsystem.app.DailyMissionPersistenceTest"
OUT="build/runtime-diagnostics"
mkdir -p "$OUT"
test -s "$TEST_APK"

adb install -r "$TEST_APK"

echo "Android instrumentation 1/2: persist daily IDs using a test date"
adb shell am instrument -w -e class "$TEST_CLASS#seedPin" \
  "$TEST_PACKAGE/androidx.test.runner.AndroidJUnitRunner" \
  | tee "$OUT/daily-pin-seed.txt"
grep -q 'OK (1 test)' "$OUT/daily-pin-seed.txt"

# Kill the app process between instrumentation runs. The second invocation
# obtains a different process and reads the same persisted SharedPreferences.
adb shell am force-stop "$PACKAGE"
adb shell am force-stop "$TEST_PACKAGE" || true

echo "Android instrumentation 2/2: verify restart, day rollover and profile change"
adb shell am instrument -w -e class "$TEST_CLASS#verifyAfterRestartAndDayChange" \
  "$TEST_PACKAGE/androidx.test.runner.AndroidJUnitRunner" \
  | tee "$OUT/daily-pin-restore.txt"
grep -q 'OK (1 test)' "$OUT/daily-pin-restore.txt"

echo "PASS: persisted daily IDs survive process restart; simulated local-day and profile changes invalidate them."

echo "Android instrumentation 3/4: generate and accept Monarca preview"
adb shell am instrument -w -e class "$TEST_CLASS#seedMonarchPreviewAndAccept" \
  "$TEST_PACKAGE/androidx.test.runner.AndroidJUnitRunner" \
  | tee "$OUT/monarch-preview-accept.txt"
grep -q 'OK (1 test)' "$OUT/monarch-preview-accept.txt"

adb shell am force-stop "$PACKAGE"
adb shell am force-stop "$TEST_PACKAGE" || true

echo "Android instrumentation 4/4: accepted Monarca remains stable; profile edits pause safely"
adb shell am instrument -w -e class "$TEST_CLASS#verifyMonarchAfterRestartAndProfileReview" \
  "$TEST_PACKAGE/androidx.test.runner.AndroidJUnitRunner" \
  | tee "$OUT/monarch-active-review.txt"
grep -q 'OK (1 test)' "$OUT/monarch-active-review.txt"

echo "PASS: seeded Monarca previews, explicit consent and safe pause on profile changes."

CUSTOM_TEST="pe.asterionsystem.app.RoutineExperiencePersistenceTest"
echo "Android instrumentation 5/6: seed self-reported custom routine XP and steps"
adb shell am instrument -w -e class "$CUSTOM_TEST#seedCompletedRoutineOnce" \
  "$TEST_PACKAGE/androidx.test.runner.AndroidJUnitRunner" \
  | tee "$OUT/custom-routine-seed.txt"
grep -q 'OK (1 test)' "$OUT/custom-routine-seed.txt"
adb shell am force-stop "$PACKAGE"
adb shell am force-stop "$TEST_PACKAGE" || true

echo "Android instrumentation 6/6: verify XP cannot be duplicated across restarts"
adb shell am instrument -w -e class "$CUSTOM_TEST#verifyAfterProcessRestart" \
  "$TEST_PACKAGE/androidx.test.runner.AndroidJUnitRunner" \
  | tee "$OUT/custom-routine-restart.txt"
grep -q 'OK (1 test)' "$OUT/custom-routine-restart.txt"
echo "PASS: custom routine persistence and once-per-day XP."

echo "Android instrumentation 7/7: native Canvas paints eight multiview and articulated motion tests"
adb shell am instrument -w -e class \
  "pe.asterionsystem.app.NativeAvatarRenderTest" \
  "$TEST_PACKAGE/androidx.test.runner.AndroidJUnitRunner" \
  | tee "$OUT/native-avatar-render.txt"
grep -q 'OK (8 tests)' "$OUT/native-avatar-render.txt"
echo "PASS: native avatar palettes, resolutions, themes and accessibility (8 tests)."



