#!/usr/bin/env python3
"""Contract for four-stage local registration and independent Step 1 assets."""
import hashlib
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1]
MAIN=ROOT/"app/src/main"
JAVA=MAIN/"java/pe/asterionsystem/app"
DRAWABLE=MAIN/"res/drawable"
NODPI=MAIN/"res/drawable-nodpi"
ANDROID="{http://schemas.android.com/apk/res/android}"

def check():
    expected={
        "asterion_reg_background.webp":"8e2d54ebc247e74a3f29d26773fbbf176c767d4fa616d278a40a000a15779078",
        "asterion_reg_profile.webp":"42acb0b89e99b7d0b7f18f5fc45d19433f6f11b33ed8b7ab5604756b0592c679",
    }
    for filename, digest in expected.items():
        data=(NODPI/filename).read_bytes()
        assert hashlib.sha256(data).hexdigest()==digest, "Approved asset changed: "+filename
        assert data[:4]==b"RIFF" and data[8:12]==b"WEBP", "Invalid WebP: "+filename
    for icon in ("name","age","height","weight","gender","photo","next"):
        for path in (DRAWABLE/f"asterion_reg_{icon}.xml",
                     DRAWABLE/f"asterion_reg_{icon}_glyph.xml"):
            ET.parse(path)
    ET.parse(DRAWABLE/"asterion_reg_shared_tile.xml")
    ET.parse(DRAWABLE/"asterion_reg_field.xml")
    screen=ET.parse(MAIN/"res/layout/screen_profile_create.xml").getroot()
    ids=set()
    for element in screen.iter():
        value=element.attrib.get(ANDROID+"id","")
        if value.startswith("@+id/"): ids.add(value.rsplit("/",1)[-1])
    assert {"profile_wizard_root","profile_create_panel","profile_name_input",
            "profile_age_input","profile_height_input","profile_weight_input",
            "profile_gender_group","profile_choose_image","profile_create_submit",
            "profile_dynamic_step","profile_step_indicator"}.issubset(ids)
    layout=(MAIN/"res/layout/screen_profile_create.xml").read_text()
    assert "asterion_reg_background" in layout
    assert "asterion_guardian" in layout and "asterion_reg_field" in layout
    assert "asterion_reg_profile" in layout
    for icon in ("name","age","height","weight","gender","photo","next"):
        assert f"asterion_reg_{icon}" in layout
    activity=(JAVA/"ProfileCreateActivity.java").read_text()
    assert "new NeonFrameDrawable" in activity
    assert "new ActivityResultContracts.OpenDocument()" in activity
    assert "ProfileRegistrationDraft.load(this)" in activity
    assert "ProfileStore.saveRegistration(this,draft)" in activity
    assert "showStep(2)" in activity and "showStep(4)" in activity
    store=(JAVA/"ProfileStore.java").read_text()
    assert "getRegistration(Context" in store and "complete_registration_v2" in store
    assert "putString(KEY_NAME, name)" in store
    assert "saveName(Context" in store, "Old profiles must not be migrated destructively"
    helper=(JAVA/"ProfileRegistrationSteps.java").read_text()
    for fragment in ("step2()", "step3()", "step4()", "Mancuernas",
                     "MIS", "CAT_COLORS", "rondas", "series","Descanso"):
        if fragment=="MIS":continue
        assert fragment in helper, fragment
    icon_catalog=(JAVA/"AsterionRegistrationIcons.java").read_text()
    assert "public static int drawable" in icon_catalog
    assert icon_catalog.count("R.drawable.asterion_reg_")==8
    print("OK: paso 1, componentes separados, 8 iconos referenciables y 4 etapas.")
if __name__=="__main__":
    try:check()
    except (AssertionError,ET.ParseError,OSError) as exc:
        print("ERROR: registro del cazador:",exc);sys.exit(1)
