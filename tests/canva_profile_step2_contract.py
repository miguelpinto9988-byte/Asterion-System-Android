#!/usr/bin/env python3
"""Step 2 is now the original native factory, not screenshot coordinates."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
src=root/"app/src/main/java/pe/asterionsystem/app"
activity=(src/"ProfileCreateActivity.java").read_text()
native=(src/"ProfileRegistrationSteps.java").read_text()
assert "dynamic.addView(forms.step2())" in activity
assert "new CanvaProfileStepTwoView(" not in activity
for fragment in ('"experience"', '"goal"', '"custom_goal"', '"mode"',
                 '"missions"', '"rest_seconds"', '"sequence"', '"minutes"',
                 '"week"', '"bodyweight"', '"dumbbells"', '"dumbbell_weights"',
                 '"gym"', "readWeek()", "CAT_ASSETS[index[d]]",
                 'new String[]{"misiones","rutinas"}',
                 'new String[]{"1","2","3","4","5","6","7"}'):
    assert fragment in native,fragment
assert "ProfileRegistrationDraft.save(this,draft)" in activity
print("OK: paso 2 nativo; misiones, rutinas, semana y equipo preservados.")
