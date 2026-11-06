# Fase 16 — Estado de las siete etapas

| Etapa | Estado actualizado, 5 de noviembre de 2026 | Evidencia |
|---|---|---|
| 16.1 Auditoría previa | **Realizada en código fuente** | `16-01-auditoria-precompilacion.md` |
| 16.2 Correcciones previas | **Preparada y revisada estáticamente**; sin compilación | `MainActivity`, `activity_main.xml`, `tests/phase16_preflight_static.py`, `16-02-correcciones-y-preflight.md` |
| 16.3 Estrategia de empaquetado | **Propuesta documentada**, selección definitiva pendiente | `16-03-empaquetado-y-permisos.md` |
| 16.4 Compilación Android | **Tercer intento llegó a `assembleDebug`; lint falló 1 error / 58 advertencias. Migración AndroidX pendiente de validar** | [Run #42132119288](https://github.com/miguelpinto9988-byte/Asterion-System-Android/actions/runs/42132119288) |
| 16.5 Validación del artefacto | **Pendiente de una APK `debug` real** | No existe artefacto APK para verificar |
| 16.6 Entrega y documentación de instalación | **Pendiente de un artefacto verificable** | No se emitirán instrucciones definitivas para una APK inexistente |
| 16.7 Publicación y cierre de APK | **Pendiente** | La publicación de documentos de preparación no equivale a distribuir APK |

## Límites aprobados

- Autorización actual: auditoría y compilación **exclusivamente `debug`**, `:core:test`, `:app:testDebugUnitTest`, `:app:lintDebug`, `:app:assembleDebug`, verificación del APK y documentación.
- Tres ejecuciones manuales: primera falló una prueba de planificación; segunda falló un `import R`; tercera superó `assembleDebug` y falló `lintDebug` por `GestureBackNavigation`. Se migró el manejo de Atrás a AndroidX `OnBackPressedDispatcher`; **no se ha recompilado ni verificado una APK del commit migrado**.
- Autorizado posteriormente: disparador automático `push` exclusivamente sobre `main`, además de `workflow_dispatch`, para pruebas y APK `debug`.
- No autorizado: construir `release`, configurar firma de distribución o publicar un paquete sin validarlo.
- Se mantiene el repositorio Java y XML nativo, SQLite versión 5, SharedPreferences y el funcionamiento sin red.
- Fases 10, 12 y 13 excluidas; fase 03 con diseño oscuro vectorial integrado, aún pendiente de validación en un dispositivo Android.

## Informe de primera compilación autorizada

Consultar [16-04-resultado-y-bloqueos.md](16-04-resultado-y-bloqueos.md). El workflow se ejecutó tres veces el 5 de noviembre de 2026; el tercer intento produjo un ensamblado debug pero no pasó lint ni publicó el APK. La migración a AndroidX está en fuente y requiere una nueva ejecución manual para validarse.
