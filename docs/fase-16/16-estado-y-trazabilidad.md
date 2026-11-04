# Fase 16 — Estado de las siete etapas

| Etapa | Estado tras autorización de preparación | Evidencia |
|---|---|---|
| 16.1 Auditoría previa | **Realizada en código fuente** | `16-01-auditoria-precompilacion.md` |
| 16.2 Correcciones previas | **Preparada y revisada estáticamente**; sin compilación | `MainActivity`, `activity_main.xml`, `tests/phase16_preflight_static.py`, `16-02-correcciones-y-preflight.md` |
| 16.3 Estrategia de empaquetado | **Propuesta documentada**, selección definitiva pendiente | `16-03-empaquetado-y-permisos.md` |
| 16.4 Compilación Android | **No autorizada / no ejecutada** | Sin Gradle, APK o workflow |
| 16.5 Validación del artefacto | **Bloqueada** | No existe artefacto APK para verificar |
| 16.6 Entrega y documentación de instalación | **Bloqueada** | No se emitirán instrucciones definitivas para una APK inexistente |
| 16.7 Publicación y cierre de APK | **Bloqueada** | La publicación de documentos de preparación no equivale a distribuir APK |

## Límites aprobados

- Autorización actual: auditoría, preparación técnica, correcciones fuente anteriores a compilación.
- No autorizado: ejecutar Gradle, compilar Android, generar APK, activar CI, configurar firma o publicar un paquete.
- Se mantiene el repositorio Java y XML nativo, SQLite versión 5, SharedPreferences y el funcionamiento sin red.
- Fases 10, 12 y 13 excluidas; fase 03 de diseño visual definitivo pendiente.
