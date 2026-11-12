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
grep -q 'OK (13 tests)' "$OUT/native-avatar-render.txt"
echo "PASS: native avatar palettes, resolutions, themes and accessibility (13 tests)."





echo "Android instrumentation 8/8: export screenshots from real Canvas renderer"
REMOTE="/sdcard/Android/data/$PACKAGE/files/asterion-v11-evidence"
LOCAL="$OUT/asterion-v11-evidence"
mkdir -p "$LOCAL"
adb shell ls "$REMOTE"
adb pull "$REMOTE/." "$LOCAL/"
COUNT="$(find "$LOCAL" -maxdepth 1 -type f -name '*.png' | wc -l | tr -d ' ')"
test "$COUNT" -eq 42 || { echo "Expected 42 real emulator PNGs; found $COUNT"; exit 1; }
test -s "$LOCAL/azul_top_1.png"
test -s "$LOCAL/morado_top_1.png"
echo "PASS: 42 real Android renderer screenshots from seven scenes × three phases × two palettes."


echo "Android instrumentation 9/9: checkpoint resets retain prior logs without inheriting old volume"
RESET_TEST="pe.asterionsystem.app.VolumeProgressResetPersistenceTest"
adb shell am instrument -w -e class "$RESET_TEST" \
  "$TEST_PACKAGE/androidx.test.runner.AndroidJUnitRunner" \
  | tee "$OUT/volume-checkpoint-reset.txt"
grep -q 'OK (3 tests)' "$OUT/volume-checkpoint-reset.txt"
echo "PASS: checkpoint volume resets are persisted and historical records remain intact."

echo "Android instrumentation: 1257 avatar command specs + 18 side-motion PNG frames"
AVATAR_CATALOG_TEST="pe.asterionsystem.app.AvatarExerciseCommandCatalogInstrumentedTest"
adb shell am instrument -w -e class "$AVATAR_CATALOG_TEST" \
  "$TEST_PACKAGE/androidx.test.runner.AndroidJUnitRunner" \
  | tee "$OUT/avatar-catalog-commands.txt"
grep -q 'OK (4 tests)' "$OUT/avatar-catalog-commands.txt"
REMOTE_NEW="/sdcard/Android/data/$PACKAGE/files/asterion-avatar-command-samples"
LOCAL_NEW="$OUT/asterion-avatar-command-samples"
mkdir -p "$LOCAL_NEW"
adb shell ls "$REMOTE_NEW"
adb pull "$REMOTE_NEW/." "$LOCAL_NEW/"
NEW_COUNT="$(find "$LOCAL_NEW" -maxdepth 1 -type f -name '*.png' | wc -l | tr -d ' ')"
test "$NEW_COUNT" -eq 18 || {
  echo "Expected 18 real side-motion PNG frames; found $NEW_COUNT"
  exit 1
}
echo "PASS: catalog IDs and 18 authentic side-motion frames were preserved."

echo "Android avatar framing: export 24 additional frontal movement PNGs"
FRONT_REMOTE="/sdcard/Android/data/$PACKAGE/files/asterion-avatar-new-front-samples"
FRONT_LOCAL="$OUT/asterion-avatar-new-front-samples"
mkdir -p "$FRONT_LOCAL"
adb shell ls "$FRONT_REMOTE"
adb pull "$FRONT_REMOTE/." "$FRONT_LOCAL/"
FRONT_COUNT="$(find "$FRONT_LOCAL" -maxdepth 1 -type f -name '*.png' | wc -l | tr -d ' ')"
test "$FRONT_COUNT" -eq 24 || {
  echo "Expected 24 native new frontal PNGs; found $FRONT_COUNT"
  exit 1
}
echo "PASS: four frontal exercises produce 24 real Canvas frames in both palettes."

echo "Avatar command-map previews: 24 Android PNG frames across 4 pending variants"
MAP_REMOTE="/sdcard/Android/data/$PACKAGE/files/asterion-avatar-command-map-samples"
MAP_LOCAL="$OUT/asterion-avatar-command-map-samples"
mkdir -p "$MAP_LOCAL"
adb shell ls "$MAP_REMOTE"
adb pull "$MAP_REMOTE/." "$MAP_LOCAL/"
MAP_COUNT="$(find "$MAP_LOCAL" -maxdepth 1 -type f -name '*.png' | wc -l | tr -d ' ')"
test "$MAP_COUNT" -eq 24 || {
  echo "Expected 24 command-map PNG frames; found $MAP_COUNT"
  exit 1
}
echo "PASS: representative non-illustrated variants have honest animated commands."

echo "Android avatar: 94 real frames for fifteen authored floor, chair and standing scenes"
SUPPORTED_TEST="pe.asterionsystem.app.AvatarDedicatedSceneInstrumentedTest"
adb shell am instrument -w -e class "$SUPPORTED_TEST" \
  "$TEST_PACKAGE/androidx.test.runner.AndroidJUnitRunner" \
  | tee "$OUT/avatar-supported-mat-chair.txt"
grep -q 'OK (1 test)' "$OUT/avatar-supported-mat-chair.txt"
SUP_REMOTE="/sdcard/Android/data/$PACKAGE/files/asterion-avatar-supported-mat-chair"
SUP_LOCAL="$OUT/asterion-avatar-supported-mat-chair"
mkdir -p "$SUP_LOCAL"
adb shell ls "$SUP_REMOTE"
adb pull "$SUP_REMOTE/." "$SUP_LOCAL/"
SUP_COUNT="$(find "$SUP_LOCAL" -maxdepth 1 -type f -name '*.png' | wc -l | tr -d ' ')"
test "$SUP_COUNT" -eq 94 || {
  echo "Expected 94 dedicated exercise scene PNG frames; found $SUP_COUNT"
  exit 1
}
echo "PASS: 94 native Android scenes with validated ground, chair, mat and alternating shoe pivots."

# Every newly authored ID, not a hand-picked subset; both palettes and both
# halves of alternating motion. The artifact remains a reviewable preview.
adb shell am instrument -w -e class \
  "pe.asterionsystem.app.AvatarVariantSceneInstrumentedTest" \
  "$TEST_PACKAGE/androidx.test.runner.AndroidJUnitRunner" \
  | tee "$OUT/avatar-variant-scenes.txt"
grep -q 'OK (1 test)' "$OUT/avatar-variant-scenes.txt"
VAR_REMOTE="/sdcard/Android/data/$PACKAGE/files/asterion-avatar-variant-scenes"
VAR_LOCAL="$OUT/asterion-avatar-variant-scenes"
mkdir -p "$VAR_LOCAL"
adb pull "$VAR_REMOTE/." "$VAR_LOCAL/"
python3 tests/avatar_variant_scene_contract.py --frames "$VAR_LOCAL"
