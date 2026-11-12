#!/usr/bin/env python3
"""Audit explicit recipes against immutable taxonomy and optional Android evidence."""
import argparse,csv,json,re
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'app/src/main/java/pe/asterionsystem/app/AvatarVariantScene.java'
def recipes():
    rows=[]
    for args in re.findall(r'add\(rows,(.+?)\);',JAVA.read_text()):
        # Java literals used by the explicit records are JSON plus float suffixes.
        args=re.sub(r'(?<=[0-9])f(?=,)', '', args)
        rows.append(json.loads('['+args+']'))
    return rows

def main():
    parser=argparse.ArgumentParser();parser.add_argument('--frames',type=Path)
    args=parser.parse_args()
    rows=recipes()
    assert len(rows)==111
    assert len({r[0] for r in rows})==len(rows)
    def load(path):
        with path.open() as f:return {r['variant_id']:r for r in csv.DictReader(f,delimiter='\t')}
    tax=load(ROOT/'progress-engine/src/main/resources/asterion_exercises/v1/variants.tsv')
    catalog=load(ROOT/'app/src/main/assets/asterion_exercises/v1/avatar_motion_commands.tsv')
    for id,name,motion,posture,amp,pattern,prop,hold,equipment,execution,level,supervision in rows:
        assert id in tax and tax[id]['name']==name,id
        assert equipment==tax[id]['equipment'] and execution==tax[id]['execution'],id
        assert level==int(tax[id]['level']) and supervision==(tax[id]['supervision']=='1'),id
        assert catalog[id]['clip']=='AUTHORED_VARIANT' and catalog[id]['status']=='ILLUSTRATIVE',id
        assert 0<amp<=85 and pattern in ('BOTH','NEAR','ALTERNATING'),id
        assert (posture=='SEATED')==('silla' in equipment.split('+') and prop!='CHAIR_SUPPORT'),id
        required={'DUMBBELLS':'mancuerna','GOBLET':'mancuerna','FRONT_DUMBBELLS':'mancuerna',
                  'BAR':'barra','BAR_BACK':'barra','BAR_FRONT':'barra',
                  'BAND_FEET':'banda','BAND_ANCHOR':'banda','TOWEL':'toalla',
                  'HAMMER_DUMBBELLS':'mancuerna','KETTLEBELL':'kettlebell',
                  'BAND_SHOULDERS':'banda','BAND_KNEE':'banda','WALL':'pared',
                  'CHAIR_SUPPORT':'silla','STEP':'escalon','BAND_BACK':'banda'}
        material=set(equipment.split('+'))-{'sin_equipo'}
        if prop!='CHAIR_SUPPORT':
            material.discard('silla')
        assert material==({required[prop]} if prop!='NONE' else set()),id
    if args.frames:
        expected={f'{palette}_{r[0]}_{phase}.png' for r in rows
                  for palette in ('azul','morado') for phase in range(5)}
        actual={p.name for p in args.frames.glob('*.png')}
        assert actual==expected,(expected-actual,actual-expected)
        for name in expected:
            data=(args.frames/name).read_bytes()
            assert data.startswith(b'\x89PNG\r\n\x1a\n') and len(data)>2000,name
    print(f'OK: {len(rows)} authored recipes, exact equipment/metadata'+
          (', 1110 native Android frames' if args.frames else ''))
if __name__=='__main__':main()
