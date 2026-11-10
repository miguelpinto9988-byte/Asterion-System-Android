#!/usr/bin/env python3
"""Profile creation: original draft/profile keys and native UI are preserved."""
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/pe/asterionsystem/app"
RES=ROOT/"app/src/main/res"
ANDROID="{http://schemas.android.com/apk/res/android}"

def check():
    root=ET.parse(RES/"layout/screen_profile_create.xml").getroot()
    ids={v.get(ANDROID+"id","") for v in root.iter()}
    for name in ("profile_wizard_root","profile_wizard_scroll",
                 "profile_name_input","profile_age_input","profile_height_input",
                 "profile_weight_input","profile_gender_group",
                 "profile_choose_image","profile_create_submit",
                 "profile_dynamic_step","profile_step_indicator"):
        assert "@+id/"+name in ids,name
    activity=(JAVA/"ProfileCreateActivity.java").read_text()
    assert "ProfileRegistrationDraft.load(this)" in activity
    assert "ProfileRegistrationDraft.save(this,draft)" in activity
    assert "ProfileStore.saveRegistration(this,draft)" in activity
    assert "new ActivityResultContracts.OpenDocument()" in activity
    assert "saveStepOne(true)" in activity
    assert "showStep(2)" in activity and "showStep(4)" in activity
    assert "dynamic.addView(forms.step2())" in activity
    assert "dynamic.addView(forms.step4())" in activity
    assert "new SimpleProfileStepThreeView(" in activity
    store=(JAVA/"ProfileStore.java").read_text()
    assert "getRegistration(Context" in store and "complete_registration_v2" in store
    assert "putString(KEY_NAME, name)" in store
    assert "saveName(Context" in store
    native=(JAVA/"ProfileRegistrationSteps.java").read_text()
    assert "View step2()" in native and "View step4()" in native
    assert "Mancuernas" in native and "rondas" in native and "series" in native
    print("OK: creación de perfil completa, opciones y guardado existente.")

if __name__=="__main__":
    try: check()
    except (AssertionError,OSError,ET.ParseError) as error:
        print("ERROR:",error,file=sys.stderr)
        sys.exit(1)
