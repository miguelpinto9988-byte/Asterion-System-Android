#!/usr/bin/env python3
"""Approved Asterion profile screens must preserve their four-stage UI contract."""
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

root=Path(__file__).resolve().parents[1]
app=root/"app/src/main"
ui=(app/"java/pe/asterionsystem/app/ProfileRegistrationSteps.java").read_text(encoding="utf-8")
activity=(app/"java/pe/asterionsystem/app/ProfileCreateActivity.java").read_text(encoding="utf-8")
xml=(app/"res/layout/screen_profile_create.xml").read_text(encoding="utf-8")

def expect(needle, source,where):
    if needle not in source:
        raise AssertionError("Falta "+repr(needle)+" en "+where)

def main():
    ET.fromstring(xml)
    expect('@+id/profile_scenery',xml,'layout')
    for n in (2,3,4):
        expect('asterion_step'+str(n)+'_background',activity,'pantalla')
        expect('View step'+str(n)+'()',ui,'flujo')
    for key in ('01 — Experiencia','02 — Objetivo principal',
                '03 — Objetivo personalizado','04 — Modalidad',
                '05 — Planificación semanal','06 — Equipamiento'):
        expect(key,ui,'paso 2')
    for key in ('"30","60","90"','"misiones","rutinas"',
                '"1","2","3","4","5","6","7"',
                '"rondas","series"','(index[d]+1)%6',
                'CAT_ASSETS[index[d]]'):
        expect(key,ui,'modo y calendario')
    for key in ('"dark","light"','"Azul","Morado","Rosa","Verde","Rojo","Blanco"',
                'addCardPreview(', 'accentChoices.check('):
        expect(key,ui,'paso 3')
    for key in ('addExpandableReview(', '01 — Verificación',
                '02 — Confirmación final'):
        expect(key,ui,'paso 4')
    expect('R.drawable.asterion_guardian',activity,'personaje oficial paso 1')
    expect('private int iconResource(',ui,'reutilización de recursos')
    if 'tutorial' in ui.lower() or 'introducción al sistema' in ui.lower():
        raise AssertionError('La introducción/tutorial fue descartada.')
    print("OK: pasos 2, 3 y 4 con estructura, iconos, calendario y revisión.")
if __name__=="__main__":
    try:main()
    except (AssertionError,OSError,ET.ParseError) as exc:
        print("ERROR:",exc,file=sys.stderr)
        sys.exit(1)
