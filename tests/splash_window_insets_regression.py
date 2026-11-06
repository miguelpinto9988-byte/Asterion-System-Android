#!/usr/bin/env python3
"""Regression guard against Android 16 early-WindowInsets startup crash."""
from pathlib import Path
source=(Path(__file__).resolve().parents[1]/
        "app/src/main/java/pe/asterionsystem/app/SplashActivity.java").read_text()
on_create=source.split("protected void onCreate(",1)[1].split(
    "public void onWindowFocusChanged(",1)[0]
focus=source.split("public void onWindowFocusChanged(",1)[1].split(
    "protected void onResume(",1)[0]
assert "getInsetsController()" not in on_create, (
    "No consultar PhoneWindow.getInsetsController() en onCreate."
)
assert "getWindow().getDecorView().getWindowInsetsController()" in focus
assert "if (hasFocus && Build.VERSION.SDK_INT >= 30)" in focus
assert "R.layout.activity_splash" in on_create
print("OK: controlador de barras diferido hasta focus; Android 16.")
