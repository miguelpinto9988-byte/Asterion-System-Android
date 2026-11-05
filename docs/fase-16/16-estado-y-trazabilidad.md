# Fase 16 — Estado de las siete etapas

| Etapa | Estado actualizado, 5 de noviembre de 2026 | Evidencia |
|---|---|---|
| 16.1 Auditoría previa | **Realizada en código fuente** | `16-01-auditoria-precompilacion.md` |
| 16.2 Correcciones previas | **Preparada y revisada estáticamente**; sin compilación | `MainActivity`, `activity_main.xml`, `tests/phase16_preflight_static.py`, `16-02-correcciones-y-preflight.md` |
| 16.3 Estrategia de empaquetado | **Propuesta documentada**, selección definitiva pendiente | `16-03-empaquetado-y-permisos.md` |
| 16.4 Compilación Android | **Intentada; falló una prueba JUnit (`57/58` correctas)** | [Run #42091749135](https://github.com/miguelpinto9988-byte/Asterion-System-Android/actions/runs/42091749135); arreglo en fuente, repetición pendiente |
| 16.5 Validación del artefacto | **Pendiente de una APK `debug` real** | No existe artefacto APK para verificar |
| 16.6 Entrega y documentación de instalación | **Pendiente de un artefacto verificable** | No se emitirán instrucciones definitivas para una APK inexistente |
| 16.7 Publicación y cierre de APK | **Pendiente** | La publicación de documentos de preparación no equivale a distribuir APK |

## Límites aprobados

- Autorización actual: auditoría y compilación **exclusivamente `debug`**, `:core:test`, `:app:testDebugUnitTest`, `:app:lintDebug`, `:app:assembleDebug`, verificación del APK y documentación.
- Primera ejecución manual: falló en `SchedulePlannerBulkTitlesTest.emptyDayProducesNoListings`; las tareas `lintDebug` y `assembleDebug` no llegaron a ejecutarse. El caso se corrigió en fuente; no hay APK para el commit posterior.
- No autorizado: construir `release`, configurar firma de distribución, activar triggers automáticos, publicar un paquete sin validarlo.
- Se mantiene el repositorio Java y XML nativo, SQLite versión 5, SharedPreferences y el funcionamiento sin red.
- Fases 10, 12 y 13 excluidas; fase 03 con diseño oscuro vectorial integrado, aún pendiente de validación en un dispositivo Android.

## Informe de primera compilación autorizada

Consultar [16-04-resultado-y-bloqueos.md](16-04-resultado-y-bloqueos.md). El workflow manual se ejecutó el 5 de noviembre de 2026; falló una prueba. Se corrigió el código fuente y queda pendiente otra ejecución manual para comprobar el arreglo.
