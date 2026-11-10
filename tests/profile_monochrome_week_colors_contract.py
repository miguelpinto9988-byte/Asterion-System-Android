#!/usr/bin/env python3
"""Monochrome registration themes must never wash out weekly training colors."""
from pathlib import Path
import re
import xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"app/src/main/java/pe/asterionsystem/app"
LAYOUT=ROOT/"app/src/main/res/layout/screen_profile_create.xml"

activity=(JAVA/"ProfileCreateActivity.java").read_text()
palette=(JAVA/"ProfileWizardTheme.java").read_text()
step3=(JAVA/"SimpleProfileStepThreeView.java").read_text()
step2=(JAVA/"ProfileRegistrationSteps.java").read_text()
xml=LAYOUT.read_text()
ET.fromstring(xml)

assert 'if (!draft.has("theme")) put("theme","light");' in activity
assert 'get("theme","light")' in activity or 'optString("theme","light")' in activity
assert '"theme", THEMES' in step3
assert 'THEMES = {"light","dark"}' in step3
assert 'THEME_LABELS = {"Blanco","Negro"}' in step3
assert "profile_theme_light" not in xml and "profile_theme_dark" not in xml
assert '#FFFFFF' in xml
assert "ProfileWizardTheme.applyWizard(" in activity
assert "onThemeChanged.run()" in step3
assert "new SimpleProfileStepThreeView(" in activity
assert "box(light" in palette
assert "LIGHT_BG = Color.WHITE" in palette
assert "DARK_BG = 0xFF080808" in palette

for name in ("Tren superior","Tren central","Tren inferior",
             "Movilidad","Cardio","Descanso"):
    assert f'"{name}"' in step2,name
for rgb in ("32,136,183","132,86,202","211,148,55",
            "37,141,112","185,81,114","100,116,139"):
    assert f"Color.rgb({rgb})" in step2,rgb
for fragment in (
    "index[d]=(index[d]+1)%6",
    'put("week",formatWeek(index))',
    "CAT_COLORS[index[d]]",
    "CAT_COLORS[k]",
    'chip.setTag("asterion_week_color")',
    'legend.setTag("asterion_week_legend")',
    '"Leyenda: " + CATS[k]',
):
    assert fragment in step2,fragment
for fragment in ('"asterion_week_color".equals(tag)',
                 '"asterion_week_legend".equals(tag)'):
    assert fragment in palette,fragment

assert len(re.findall(r"Color.rgb\\([0-9,]+\\)",step2)) >= 6
print("OK: blancos/negros uniformes, tema en paso 3 y 6 colores con leyenda conservados.")
