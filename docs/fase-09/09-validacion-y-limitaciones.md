# Fase 09 — Control de calidad, alcance y pendientes

## Pruebas preparadas

- `core/src/test/java/pe/asterionsystem/core/progress/ProgressSummaryTest.java`: valores vacíos, pendientes, división entre evaluadas, redondeo y restricciones de observación.
- `core/src/test/java/pe/asterionsystem/core/progress/ProgressServiceTest.java`: cambios de estado, reversión a pendiente y validación de inicio semanal.
- `tests/phase09_progress_sqlite_smoke.py`: migración SQLite v4→v5 con contenido anterior, consultas por fecha/semana, FK, estados válidos, no duplicidad, actualización y eliminación en cascada.
- Previsualización HTML de fase 09: navegación, registro explícito, edición, restablecimiento a pendiente y porcentaje semanal, aislados en el navegador.

## Resultados realizados sin APK

- El HTML se validó con `node --check` y 23 interacciones de Chromium/Playwright: navegación, selección de estados, edición de notas, borrado del resultado y reversión a pendiente, resumen 50%/100% según las evaluadas, cambio de semana y restauración de ruta sin errores JavaScript.
- Un ejercicio local de SQLite 3.46 probó **8 restricciones equivalentes** de la nueva tabla de resultados: PK única, FK, borrado en cascada, estados válidos, nota acotada, edición y pendientes por ausencia de fila. El script del repositorio contiene pruebas adicionales que extraen la sentencia Java; su ejecución contra esos archivos exactos sigue pendiente.
- Las comprobaciones estáticas abarcan referencias de recursos Android XML y Java, conservación de la rama y de los módulos previos. No se ejecutaron tareas Android Gradle.

## Limitaciones técnicas

- **No se ha compilado ninguna APK ni se ha ejecutado Gradle.** Las pruebas JUnit están escritas, pero solo se considerarán superadas al ejecutarse con un runner autorizado.
- La verificación SQLite de escritorio no equivale a una ejecución de Android `SQLiteOpenHelper` y `Cursor`.
- Los cálculos de progreso no miden calidad, esfuerzo, intensidad ni bienestar; son exclusivamente estados binarios registrados para actividades programadas.
- La desaparición de una programación elimina su seguimiento en SQLite (cascada). La interfaz puede mostrar únicamente las actividades programadas del día; si está vacío, se remite al planificador.
- El porcentaje excluye las pendientes, incluso si la actividad se programó en el pasado, hasta que el usuario registre un resultado.
- Los datos del HTML son simulados mediante almacenamiento local del navegador. El HTML no forma parte de la aplicación Android.

## Política de compilación

El workflow `.github/workflows/android-ci.yml` permanece limitado a `workflow_dispatch`. Publicar commits no ejecuta ni autoriza compilaciones. Ningún build o APK debe generarse sin autorización expresa.
