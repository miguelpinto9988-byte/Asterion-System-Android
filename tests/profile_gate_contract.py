#!/usr/bin/env python3
"""Static contract for the approved native profile gate, artwork and transitions."""
import hashlib
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "app/src/main"
ASSETS = {
    "asterion_profile_background.webp": "6e51ed58069020969723b4292714935b5965aa293763ab2b16ec3e73d35c3e71",
    "asterion_profile_create.png": "b381a71f398c62c42a8369e66b7a92626ba4cd3c7b1ed30eabbf62f299865d0f",
    "asterion_profile_restore.png": "082520bb666ad1286ec9a783e6261b2696916242cbd45e7208f066bc4d29ffd5",
    "asterion_profile_seal.png": "844a8efe42495f69a2a62df163abb688d27b68decb167d2c02bee21e3bd29f06",
}

def verify() -> None:
    for asset, sha in ASSETS.items():
        content = (APP / "res/drawable-nodpi" / asset).read_bytes()
        assert hashlib.sha256(content).hexdigest() == sha, "Recurso perfil alterado: " + asset
    splash = (APP / "java/pe/asterionsystem/app/SplashActivity.java").read_text()
    assert "ProfileStore.hasProfile(this)" in splash
    assert "ProfileGateActivity.class" in splash and "MainActivity.class" in splash
    gate = (APP / "java/pe/asterionsystem/app/ProfileGateActivity.java").read_text()
    assert "R.layout.screen_profile_gate" in gate
    assert "ProfileCreateActivity.class" in gate
    assert "profile_restoration_unavailable_body" in gate
    assert "new NeonFrameDrawable" in gate
    layout = (APP / "res/layout/screen_profile_gate.xml").read_text()
    for value in ("profile_gate_create", "profile_gate_restore", "asterion_profile_background",
                  "asterion_profile_create", "asterion_profile_restore"):
        assert value in layout, "Recurso UI faltante: " + value
    store = (APP / "java/pe/asterionsystem/app/ProfileStore.java").read_text()
    assert "Context.MODE_PRIVATE" in store
    manifest = (APP / "AndroidManifest.xml").read_text()
    for activity in (".ProfileGateActivity", ".ProfileCreateActivity"):
        assert activity in manifest
    print("OK: ventana perfil, recursos y transición después de carga verificados.")

if __name__ == "__main__":
    try:
        verify()
    except (AssertionError, OSError) as exc:
        print("ERROR:", exc)
        sys.exit(1)
