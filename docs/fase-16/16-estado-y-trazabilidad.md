# Fase 16 — Estado de las siete etapas

| Etapa | Estado tras autorización de preparación | Evidencia |
|---|---|---|
| 16.1 Auditoría previa | **Realizada en código fuente** | `16-01-auditoria-precompilacion.md` |
| 16.2 Correcciones previas | **Preparada y revisada estáticamente**; sin compilación | `MainActivity`, `activity_main.xml`, `tests/phase16_preflight_static.py`, `16-02-correcciones-y-preflight.md` |
| 16.3 Estrategia de empaquetado | **Propuesta documentada**, selección definitiva pendiente | `16-03-empaquetado-y-permisos.md` |
| 16.4 Compilación Android | **Autorizada expresamente; bloqueada por entorno y no ejecutada** | Sin Gradle/Android SDK local ni función de disparo manual remoto |
| 16.5 Validación del artefacto | **Pendiente de una APK `debug` real** | No existe artefacto APK para verificar |
| 16.6 Entrega y documentación de instalación | **Pendiente de un artefacto verificable** | No se emitirán instrucciones definitivas para una APK inexistente |
| 16.7 Publicación y cierre de APK | **Pendiente** | La publicación de documentos de preparación no equivale a distribuir APK |

## Límites aprobados

- Autorización actual: auditoría y compilación **exclusivamente `debug`**, `:core:test`, `:app:testDebugUnitTest`, `:app:lintDebug`, `:app:assembleDebug`, verificación del APK y documentación.
- Ejecución actual: no realizada por falta de toolchain local y de acción de disparo remoto. No se debe declarar éxito ni inventar un APK.
- No autorizado: construir `release`, configurar firma de distribución, activar triggers automáticos, publicar un paquete sin validarlo.
- Se mantiene el repositorio Java y XML nativo, SQLite versión 5, SharedPreferences y el funcionamiento sin red.
- Fases 10, 12 y 13 excluidas; fase 03 de diseño visual definitivo pendiente.

## Informe de primera compilación autorizada

Consultar [16-04-resultado-y-bloqueos.md](16-04-resultado-y-bloqueos.md). El workflow manual fue preparado para incorporar las pruebas `:core:test` y validar identidad y firma del APK, pero no se ejecutó.
