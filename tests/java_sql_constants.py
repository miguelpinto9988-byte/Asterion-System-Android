"""Extract simple concatenated Java String constants without treating SQL semicolons as Java terminators."""
from pathlib import Path
import json
import re


def java_sql_constant(source_file: Path, name: str) -> str:
    source = source_file.read_text(encoding="utf-8")
    declaration = re.search(
        r"public\s+static\s+final\s+String\s+" + re.escape(name) + r"\s*=",
        source,
    )
    if declaration is None:
        raise AssertionError("No se encontró constante: " + name)
    position = declaration.end()
    parts = []
    while position < len(source):
        while position < len(source) and source[position].isspace():
            position += 1
        if source[position:position + 1] == ";":
            if not parts:
                raise AssertionError("Cadena vacía: " + name)
            return "".join(parts)
        if source[position:position + 1] == "+":
            position += 1
            continue
        if source[position:position + 1] != '"':
            raise AssertionError("Expresión Java no soportada en: " + name)
        start = position
        position += 1
        escaped = False
        while position < len(source):
            character = source[position]
            position += 1
            if escaped:
                escaped = False
            elif character == "\\":
                escaped = True
            elif character == '"':
                break
        else:
            raise AssertionError("Literal incompleto: " + name)
        parts.append(json.loads(source[start:position]))
    raise AssertionError("Falta terminador de constante: " + name)
