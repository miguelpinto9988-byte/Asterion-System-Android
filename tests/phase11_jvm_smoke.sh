#!/bin/sh
# Standalone Java-only smoke check. Does NOT use Gradle or Android SDK, cannot create APK.
set -eu
ROOT="$(CDPATH= cd -- "$(dirname "$0")/.." && pwd)"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT HUP INT TERM
javac --release 17 -d "$TMP"     "$ROOT/core/src/main/java/pe/asterionsystem/core/settings/"*.java     "$ROOT/tests/PreferencesSmoke.java"
java -ea -cp "$TMP" PreferencesSmoke
