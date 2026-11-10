#!/usr/bin/env python3
"""All four steps use native controls; step 3 retains the exact option IDs."""
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/pe/asterionsystem/app"
LAYOUT=ROOT/"app/src/main/res/layout/screen_profile_create.xml"
def expect(fragment, text):
    assert fragment in text,fragment
def main():
    xml=LAYOUT.read_text()
    ET.fromstring(xml)
    assert "@+id/profile_theme_light" not in xml
    assert "@+id/profile_theme_dark" not in xml
    assert "asterion_canva_paso" not in xml
    activity=(JAVA/"ProfileCreateActivity.java").read_text()
    for fragment in ('"light"', "saveStepOne(true)",
                     "dynamic.addView(forms.step2())",
                     "new SimpleProfileStepThreeView(",
                     "dynamic.addView(forms.step4())",
                     "ProfileStore.saveRegistration(this,draft)"):
        expect(fragment,activity)
    helper=(JAVA/"ProfileRegistrationSteps.java").read_text()
    for fragment in ('View step2()', 'View step4()', 'CAT_ASSETS[index[d]]',
                     'addExpandableReview(', '"rest_seconds"', '"week"',
                     '"missions"', '"dumbbells"'):
        expect(fragment,helper)
    step3=(JAVA/"SimpleProfileStepThreeView.java").read_text()
    for fragment in ('"dark","light"', '"theme"', '"accent"', '"companion"',
                     '"card_silhouette"', '"card_background"',
                     '"igris","beru","tank","tusk","iron"',
                     '"ruinas","niebla","obsidiana","aurora","bosque"',
                     '"sung_jinwoo","cha_haein","kim_dokja","yoo_joonghyuk","yoo_sangah"',
                     '"#258FE6","#9A68F2","#ED72B8","#34BC91","#E65260","#F2F5FF"',
                     "migrateLegacy()", "persist.run()"):
        expect(fragment,step3)
    print("OK: cuatro pasos minimalistas y mismas opciones.")
if __name__=="__main__":
    try: main()
    except (AssertionError,ET.ParseError,OSError) as error:
        print("ERROR:",error,file=sys.stderr);sys.exit(1)
