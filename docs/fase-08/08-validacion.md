# Fase 08 — Verificación y limitaciones

## Comprobaciones locales realizadas (2026-11-04)

- **SQLite de escritorio:** script local ejecutó el esquema v1→v3 y la migración v3→v4 reproduciendo las constantes Java. Se superaron 18 comprobaciones sobre escritura, adyacencia, superposición, edición, borrado, claves foráneas, límites temporales, triggers, índices y conservación de datos previos.
- **Previsualización HTML:** `node --check` validó su JavaScript. Chromium mediante Playwright con contenido HTML embebido verificó navegación, siete días de la semana, alta, conflicto, cambio de hora, edición, eliminación con confirmación y recreación, sin errores JavaScript reportados.
- **Implementación en repositorio:** se revisaron las nuevas rutas, referencias a XML, constantes del esquema y contrato de catálogo con la rama `main`.
- **Política de compilación:** ningún comando Gradle, `assemble*`, Android Studio Build ni ejecución de workflow se solicitó en esta fase.

## Limitaciones

- **No se ha compilado Android ni creado una APK**; aún no se han ejecutado las nuevas pruebas JUnit oficiales ni validado el comportamiento con SDK/dispositivo.
- La ejecución SQLite de escritorio no prueba `SQLiteOpenHelper` ni `Cursor` reales.
- La previsualización HTML usa JavaScript y almacenamiento del navegador, no se comunica con Android Java ni SQLite Android.
- Los diseños XML son provisionales (fase 03 sigue pendiente). La accesibilidad y adaptación a pantallas Android reales requieren pruebas futuras.
- La captura de hora está limitada a un día y no comprende zonas horarias/DST ni repetición.
- El formulario obtiene inicialmente hasta 50 actividades desde el catálogo (límite existente); si una actividad no está en ese conjunto, una edición se bloquea para evitar asignaciones incorrectas.

## Pruebas preparadas en el repositorio

- `core/src/test/java/pe/asterionsystem/core/planning/SchedulePlannerTest.java`
- `tests/phase08_planning_sqlite_smoke.py`

Se recomienda ejecutar JUnit y validar SQLite Android cuando el propietario autorice pruebas que puedan requerir compilación.
