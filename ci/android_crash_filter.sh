#!/usr/bin/env bash
# Filter crash-buffer records by the exact Asterion Android process.
# Emulators may contain unrelated crashes (wallpaper, launcher, Play Services).
# Do not suppress real crashes in the main app or app-named subprocesses.
android_app_crashed() {
  local crash_file="$1"
  local app_package="$2"
  grep -Fq "Process: ${app_package}," "$crash_file" ||
    grep -Fq "Process: ${app_package}:" "$crash_file"
}
