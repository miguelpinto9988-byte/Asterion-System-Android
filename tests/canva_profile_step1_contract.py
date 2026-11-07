#!/usr/bin/env python3
"""Static integration contract for approved Canva profile Step 1."""
from pathlib import Path
import struct

root = Path(__file__).resolve().parents[1]
main = root / "app/src/main"
asset = main / "res/drawable-nodpi/asterion_canva_paso1.png"
assert asset.is_file(), "Falta el PNG del Paso 1 importado de Canva."
data = asset.read_bytes()
assert len(data) > 100_000, "El asset de Canva está incompleto."
assert data[:8] == b"\x89PNG\r\n\x1a\n", "Asset Canva no es PNG válido."
w, h = struct.unpack(">II", data[16:24])
assert w >= 300 and h >= 500, "El recurso tiene una resolución insuficiente."
assert abs(w / h - 941 / 1672) < .01, "Proporciones del diseño Canva incorrectas."
activity = (main / "java/pe/asterionsystem/app/ProfileCreateActivity.java").read_text()
screen = (main / "java/pe/asterionsystem/app/CanvaProfileStepOneView.java").read_text()
for anchor in (
    "new CanvaProfileStepOneView(this,",
    "canvaStepOne.showSelectedAvatar(uri)",
    "step == 1 ? View.VISIBLE : View.GONE",
    "ProfileStore.saveRegistration(this,draft)",
):
    assert anchor in activity, "Contrato de registro roto: " + anchor
for anchor in (
    '"asterion_canva_paso1"',
    "void showSelectedAvatar(Uri uri)",
    "void onTextChanged(CharSequence s",
    "originalGender.check(",
    "onNext.run()",
    "onPhoto.run()",
):
    assert anchor in screen, "Control visual ausente: " + anchor
print(f"OK: Canva Paso 1 integrado ({w}x{h}), campos nativos, selección y navegación.")
