#!/usr/bin/env python3
"""Step-by-step missions, profile mode, and closed-choice safety gates."""
from pathlib import Path
import xml.etree.ElementTree as ET

root=Path(__file__).resolve().parents[1]
app=root/"app/src/main"
java=app/"java/pe/asterionsystem/app"
engine=root/"progress-engine/src/main/java/pe/asterionsystem/core/engine/missions"
ns="{http://schemas.android.com/apk/res/android}"
layout=ET.parse(app/"res/layout/screen_routine_step.xml")
ids={item.get(ns+"id") for item in layout.iter() if item.get(ns+"id")}
for field in ("routine_title","routine_subtitle","routine_content",
              "routine_category_color","routine_rest","routine_next",
              "routine_pause","routine_stop"):
    assert "@+id/"+field in ids,field
manifest=ET.parse(app/"AndroidManifest.xml").getroot()
assert any(a.get(ns+"name")==".RoutineStepActivity"
           and a.get(ns+"exported")=="false"
           for a in manifest.find("application").findall("activity"))
activity=(java/"RoutineStepActivity.java").read_text()
missions=(java/"MissionsActivity.java").read_text()
policy=(engine/"SessionInterruptionPolicy.java").read_text()
store=(java/"SessionSafetyStore.java").read_text()
flow=(java/"RoutineSessionStore.java").read_text()
profile=(java/"ProfileRegistrationSteps.java").read_text()

for keyword in ('"series".equals(savedProfile.optString("sequence","rondas"))',
                'savedProfile.optString("rest_seconds","60")',
                "new CountDownTimer(", 'case' if False else 'new AlertDialog.Builder(this)',
                "SessionInterruptionPolicy.assess(choice)",
                "SessionSafetyStore.report(this,affected,LocalDate.now(),assessment)",
                'setOnClickListener(v->questionnaire())',
                'getStringArrayListExtra("variants")',
                "MonarchSessionStore.ACTIVE.equals(snapshot.status)",
                "MissionTrackingStore.setDailyCompleted(",
                "MissionTrackingStore.markMonarch("):
    assert keyword in activity,keyword
for x in ("Rutinas paso a paso", "openRoutine(", "showMedicalHold()",
          "SessionSafetyStore.categoryPaused(", "SessionSafetyStore.lighterToday(",
          "SessionSafetyStore.recentInterruption("):
    assert x in missions,x
for x in ("GLOBAL_PAUSE_AND_SEEK_MEDICAL_CARE", "STOP_ONLY",
          "CATEGORY_PAUSE_UNTIL_REVIEW", "INTENSE_DIZZINESS_VOMITING"):
    assert x in policy,x
for x in ("recentInterruption(", "lighterToday(", "categoryPaused(",
          "acknowledgeProfessionalEvaluation(", "globallyPaused("):
    assert x in store,x
assert "RoutineSessionStore.save(" in activity
assert "sequence" in profile and "rest_seconds" in profile
assert "No se reanudará automáticamente" in policy
assert "No se reanudan automáticamente" in missions
assert "activeUntilExclusive()" in (java/"MonarchSessionStore.java").read_text()
assert "Math.max(1" in flow or "Math.max(0" in flow
print("OK: guía nativa por series/rondas, seguimiento local, lesión y recuperación aislada.")
