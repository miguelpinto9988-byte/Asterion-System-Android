#!/usr/bin/env python3
"""Step 1 remains functional, but no longer relies on the old Canva screenshot."""
from pathlib import Path
import xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1]
src=root/"app/src/main"
layout=(src/"res/layout/screen_profile_create.xml").read_text()
activity=(src/"java/pe/asterionsystem/app/ProfileCreateActivity.java").read_text()
ET.fromstring(layout)
for id_name in ("profile_name_input","profile_age_input","profile_height_input",
                "profile_weight_input","profile_gender_group",
                "profile_gender_male","profile_gender_female",
                "profile_gender_unspecified","profile_choose_image",
                "profile_avatar_preview","profile_create_submit"):
    assert f'@+id/{id_name}' in layout,id_name
assert 'new CanvaProfileStepOneView(' not in activity
assert 'new ActivityResultContracts.OpenDocument()' in activity
assert 'saveStepOne(true)' in activity
assert 'showStep(2)' in activity
assert 'R.layout.screen_profile_create' in activity
print("OK: paso 1 nativo, todos los campos y selectores sin imagen de Canva.")
