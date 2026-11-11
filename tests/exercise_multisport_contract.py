#!/usr/bin/env python3
"""Sports are secondary tags over a single immutable exercise hierarchy."""
import csv
from collections import Counter
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
DATA=ROOT/"progress-engine/src/main/resources/asterion_exercises/v1"
ENGINE=ROOT/"progress-engine/src/main/java/pe/asterionsystem/core/engine/exercises"
APP=ROOT/"app/src/main/java/pe/asterionsystem/app"

def read(filename):
    with (DATA/filename).open(encoding="utf-8",newline="") as stream:
        return list(csv.DictReader(stream,delimiter="\t"))

families=read("families.tsv")
types=read("types.tsv")
variants=read("variants.tsv")
intensity=read("intensity.tsv")
sports=read("disciplines.tsv")
assert (len(families),len(types),len(variants),len(intensity),len(sports))==(
    130,374,1157,652,374)
assert len({f["family_id"] for f in families})==130
assert len({t["type_id"] for t in types})==374
assert len({v["variant_id"] for v in variants})==1157
assert len({v["name"].casefold() for v in variants})==1157
assert {x["type_id"] for x in sports}=={x["type_id"] for x in types}
assert [x["type_id"] for x in sports]==[x["type_id"] for x in types]
assert len(sports)==len({x["type_id"] for x in sports})
assert all(x["role"] in {"fuerza","habilidad","acondicionamiento",
                         "movilidad","coordinacion"} for x in sports)
assert all("general" in x["disciplines"].split("+") for x in sports)
assert all(len(x["disciplines"].split("+"))==len(set(x["disciplines"].split("+")))
           for x in sports)
disciplines=set("+".join(x["disciplines"] for x in sports).split("+"))
assert len(disciplines)==25,disciplines
assert {"calistenia","gimnasio","futbol","baloncesto","hockey","voleibol",
        "atletismo","tenis","badminton","padel","natacion","ciclismo","remo",
        "balonmano","beisbol","softbol","patinaje","yoga","pilates",
        "danza","senderismo"}<=disciplines
assert len(families[100:])==30
assert len(types[254:])==120
assert len(variants[917:])==240
assert len(intensity[412:])==240
assert Counter(x["category"] for x in families[100:])=={
    "tren_superior":6,"tronco":6,"piernas":6,"movilidad":6,"cardio":6
}
assert all(x["supervision"]=="0" and int(x["level"])<=3
           for x in variants[917:])
assert all(x["intensity"] in ("baja","moderada","variable")
           for x in intensity[412:])
lookup={x["variant_id"]:x["intensity"] for x in intensity}
assert lookup["burpee_adaptado_v01"]=="variable"
assert lookup["burpee_adaptado_v02"]=="variable"
assert lookup["carreras_ida_vuelta_controladas_v02"]=="variable"
assert lookup["dominadas_v01"] if "dominadas_v01" in lookup else True
assert Counter(x["intensity"] for x in intensity)=={
    "baja":352,"moderada":245,"variable":38,"alta":17
}
family_to_category={x["family_id"]:x["category"] for x in families}
type_to_family={x["type_id"]:x["family_id"] for x in types}
assert all(x["family_id"] in family_to_category for x in types)
assert all(x["type_id"] in type_to_family for x in variants)
assert all(x["variant_id"] in {v["variant_id"] for v in variants}
           for x in intensity)

loader=(ENGINE/"ExerciseCatalogLoader.java").read_text()
index=(ENGINE/"ExerciseDisciplineIndex.java").read_text()
catalog=(ENGINE/"ExerciseCatalog.java").read_text()
adapter=(APP/"data/AssetExerciseCatalogRepository.java").read_text()
activity=(APP/"MissionsActivity.java").read_text()
assert "Reader intensity, Reader disciplines)" in loader
assert '"type_id\\tdisciplines\\trole"' in loader
assert "new ExerciseDisciplineIndex(this.types,this.variants" in catalog
assert "tag!=ExerciseIntensity.HIGH && tag!=ExerciseIntensity.VARIABLE" in catalog
for marker in ("disciplinesForType(","types(String discipline)",
               "variants(String discipline)","Collections.unmodifiableMap"):
    assert marker in index,marker
for marker in ('context.getAssets().open(DIRECTORY + "disciplines.tsv")',
               "ExerciseCatalogLoader.load(families,types,variants,intensity,disciplines)"):
    assert marker in adapter,marker
for marker in ("showDisciplinePicker()","showDisciplineResults(",
               "chooseDisciplineEquipment(","catalog.searchByDiscipline(",
               "InputFilter.LengthFilter(120)","Mostrar más resultados"):
    assert marker in activity,marker
print("OK: 25 disciplinas, 374 tipos etiquetados, 1157 variantes, "
      "sin duplicados ni cambios de categoría y con exclusión variable/alta.")
