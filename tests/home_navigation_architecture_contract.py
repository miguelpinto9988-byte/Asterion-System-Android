#!/usr/bin/env python3
"""User-approved five-card home, three-tab nav and safe avatar profile editor."""
from pathlib import Path
from xml.etree import ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
APP=ROOT/"app/src/main"
JAVA=APP/"java/pe/asterionsystem/app"
NS="{http://schemas.android.com/apk/res/android}"
home=ET.parse(APP/"res/layout/screen_home.xml").getroot()
missions=ET.parse(APP/"res/layout/screen_missions.xml").getroot()
manifest=ET.parse(APP/"AndroidManifest.xml").getroot()
ids=lambda root:{n.get(NS+"id","") for n in root.iter()}
home_ids=ids(home)
mission_ids=ids(missions)
ordered=[n.get(NS+"id","").replace("@+id/","")
         for n in home.iter() if n.get(NS+"id","").startswith("@+id/home_card_")]
assert ordered==["home_card_monarch","home_card_profile","home_card_daily",
                "home_card_progress","home_card_library"],ordered
for rid in ("home_avatar","home_bottom_bar","open_monarch","open_daily",
            "open_progress","open_library","home_profile_name"):
    assert "@+id/"+rid in home_ids,rid
for rid in ("open_profile","open_routines","open_calendar","open_catalog",
            "open_plan","open_settings","open_secondary","open_local_status",
            "open_legacy_progress"):
    assert "@+id/"+rid not in home_ids,rid
avatar=next(v for v in home.iter()
            if v.get(NS+"id")=="@+id/home_avatar")
assert avatar.tag=="ImageButton"
assert avatar.get(NS+"contentDescription","").strip()
assert avatar.get(NS+"layout_width")=="52dp"
assert "@+id/missions_bottom_bar" in mission_ids
assert "@+id/missions_tab_monarch" in mission_ids
assert "@+id/missions_tab_daily" in mission_ids
assert "@+id/missions_tab_library" in mission_ids
assert "@+id/missions_open_calendar" not in mission_ids
assert "@+id/missions_open_routines" not in mission_ids

read=lambda name:(JAVA/name).read_text()
nav=read("SimpleBottomNav.java")
assert nav.count("add(activity,bar,")==3
assert all('"'+label+'"' in nav for label in ("Inicio","Rutinas","Calendario"))
assert "ProfileDetailsActivity.class" in read("presentation/HomeMissionBinder.java")
assert "R.id.home_avatar" in read("presentation/HomeMissionBinder.java")
for removed in ("R.id.open_routines","R.id.open_calendar","R.id.open_profile",
                "R.id.open_plan","R.id.open_settings"):
    assert removed not in read("presentation/HomeMissionBinder.java"),removed
    assert removed not in read("presentation/NavigationRenderer.java"),removed

profile=read("ProfileDetailsActivity.java")
editor=read("ProfileCreateActivity.java")
main=read("MainActivity.java")
assert '"edit_profile",true' in profile
assert "editMode?ProfileStore.getRegistration(this)" in editor
assert 'ProfileStore.saveRegistration(this,draft)' in editor
assert "if(editMode)" in editor and "finish();" in editor
assert "if(!editMode)ProfileRegistrationDraft.save" in editor
assert 'consumeRequestedSection(getIntent())' in main
for activity in (".ProfileDetailsActivity",".RoutinesActivity",".CalendarActivity"):
    assert any(node.get(NS+"name")==activity
               and node.get(NS+"exported")=="false"
               for node in manifest.find("application").findall("activity"))
assert "SimpleBottomNav.ROUTINES" in read("RoutinesActivity.java")
assert "SimpleBottomNav.CALENDAR" in read("CalendarActivity.java")
assert "SimpleBottomNav.HOME" in read("MissionsActivity.java")
print("OK: five ordered home cards, accessible avatar editing and only three global navigation destinations")
