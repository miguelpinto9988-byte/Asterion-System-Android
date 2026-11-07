#!/usr/bin/env python3
"""Canva Paso 2: two distinct packaged states and functional native controls."""
from pathlib import Path
import struct
import hashlib

root=Path(__file__).resolve().parents[1]
res=root/"app/src/main/res/drawable-nodpi"
sources=root/"app/src/main/java/pe/asterionsystem/app"
assets=[]
for name in ("asterion_canva_paso2_misiones.png","asterion_canva_paso2_rutinas.png"):
    p=res/name
    assert p.is_file(),f"Falta la vista aprobada de Canva: {name}"
    data=p.read_bytes()
    assert data.startswith(b"\x89PNG\r\n\x1a\n"),f"PNG inválido: {name}"
    w,h=struct.unpack(">II",data[16:24])
    assert w>=300 and h>=500,f"Imagen incompleta: {name}, {w}x{h}"
    assets.append(hashlib.sha256(data).digest())
assert assets[0]!=assets[1],"Los dos estados deben tener arte diferente."

view=(sources/"CanvaProfileStepTwoView.java").read_text()
activity=(sources/"ProfileCreateActivity.java").read_text()
for text in (
    "asterion_canva_paso2_misiones","asterion_canva_paso2_rutinas",
    'put("mode","misiones")','put("mode","rutinas")',
    'put("missions",count)','put("rest_seconds",seconds)',
    'put("sequence","rondas")','put("sequence","series")',
    'put("minutes",minutes)','editDumbbells()','editGym()',
    'put("custom_goal",s.toString())','put("week",',
    'this::editWeek','picture.setImageResource(',
):
    assert text in view,"Falta control nativo: "+text
for text in ("new CanvaProfileStepOneView", "new CanvaProfileStepTwoView",
             "step == 2 ? View.VISIBLE : View.GONE",
             "ProfileStore.saveRegistration(this,draft)"):
    assert text in activity,"Se rompió navegación o persistencia: "+text
assert "HttpURLConnection" not in view and "WebView" not in view
print("OK: dos imágenes distintas, Paso 2 offline y preferencias nativas conectadas.")
