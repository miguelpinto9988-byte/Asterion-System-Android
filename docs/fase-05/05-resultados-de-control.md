# Fase 05 — Resultados de control y alcance de la evidencia

**Fecha:** 2026-11-04. **No se ejecutó Gradle ni se compiló APK.**

## Evidencia obtenida

1. **Esquema SQLite:** se ejecutaron localmente en SQLite 3.46.1 sentencias equivalentes a las constantes declaradas en `AsterionDatabaseHelper.java`. Se comprobó migración v1→v2 con una fila heredada, preservación de timestamps y notas, alta, consulta, edición, eliminación, ordenación y rechazo de duplicados o timestamps inválidos. El script reproducible `tests/phase05_sqlite_smoke.py` queda incluido en el repositorio; la ejecución de Android SQLiteOpenHelper está pendiente.
2. **Previsualización HTML externa:** `node --check` confirmó la sintaxis del JavaScript; el HTML se procesó con `HTMLParser` sin IDs duplicados; una sesión de Chromium automatizada mediante Playwright comprobó navegación entre ambas pantallas, botón Atrás, simulación de recreación, alta, selección, edición, borrado y consulta del esquema.
3. **Integración estática:** se verificó que la arquitectura nueva se agregara a `:core` y `:app` sin modificar los tres XML Android ni el workflow de GitHub Actions. El workflow continúa exclusivamente con `workflow_dispatch`.

## Limitaciones explícitas

- Las pruebas SQLite descritas se realizaron en Python SQLite local y no son pruebas ejecutadas dentro del APK Android.
- No se ejecutaron las pruebas JUnit del código Java de fase 05 ni se compilaron fuentes Android con Gradle. Se incorporan clases de pruebas JUnit para una validación posterior.
- No se ha verificado todavía que la implementación `Cursor`/`SQLiteOpenHelper` ejecute correctamente en un dispositivo.
- El HTML simula datos con `localStorage` del navegador si está disponible: no usa ni verifica SQLite Android.
- La persistencia aún no se invoca desde la interfaz XML; esta fase entrega infraestructura, no una función visible de producción.

## Próxima autorización necesaria

Cualquier compilación de APK o prueba en dispositivo requiere orden expresa del propietario. La integración de almacenamiento en pantallas y el motor de reglas se abordarán únicamente en fases aprobadas.
