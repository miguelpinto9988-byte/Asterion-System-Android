#!/bin/sh
# Compile ONLY four platform-neutral Java classes; NEVER use Android SDK or generate APK.
set -eu
ROOT="$(CDPATH= cd -- "$(dirname "$0")/.." && pwd)"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT HUP INT TERM
javac --release 17 -d "$TMP" \
  "$ROOT/core/src/main/java/pe/asterionsystem/core/offline/"*.java \
  "$ROOT/tests/LocalStorageSmoke.java"
java -ea -cp "$TMP" LocalStorageSmoke
