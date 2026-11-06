#!/usr/bin/env python3
"""Prevent regressions in Asterion's approved all-bold native typography."""
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "app/src/main"
ANDROID = "{http://schemas.android.com/apk/res/android}"
TEXT_TAGS = {"TextView", "EditText", "Button", "RadioButton", "CheckBox",
             "CheckedTextView", "Switch", "ToggleButton", "AutoCompleteTextView"}

def main() -> int:
    root = ET.parse(BASE / "res/values/styles.xml").getroot()
    styles = {s.attrib.get("name"): s for s in root.findall("style")}
    theme_items = {i.attrib["name"]: (i.text or "").strip()
                   for i in styles["Theme.AsterionSystem"].findall("item")}
    if theme_items.get("android:textViewStyle") != "@style/Asterion.BoldTextView":
        raise AssertionError("Missing global bold TextView style")
    for style in ("Asterion.BoldTextView", "Asterion.ActionButton", "Asterion.TextInput"):
        props = {i.attrib["name"]: (i.text or "").strip()
                 for i in styles[style].findall("item")}
        if props.get("android:textStyle") != "bold":
            raise AssertionError(f"Missing bold in {style}")
    screens = 0
    controls = 0
    for path in sorted((BASE / "res/layout").glob("*.xml")):
        screens += 1
        for node in ET.parse(path).getroot().iter():
            if node.tag.rsplit("}", 1)[-1] in TEXT_TAGS:
                controls += 1
                if node.attrib.get(ANDROID + "textStyle") != "bold":
                    raise AssertionError(f"Non-bold text: {path.name} {node.tag}")
    applier = (BASE / "java/pe/asterionsystem/app/presentation/NativePreferencesApplier.java").read_text(encoding="utf-8")
    if "Typeface.create(" not in applier or "Typeface.BOLD" not in applier:
        raise AssertionError("Dynamic text must remain bold")
    print(f"OK: all-bold typography checked in {screens} screens, {controls} text widgets")
    return 0

if __name__ == "__main__":
    try:
        sys.exit(main())
    except (AssertionError, OSError, ET.ParseError, KeyError) as exc:
        print("ERROR: typography contract:", exc)
        sys.exit(1)
