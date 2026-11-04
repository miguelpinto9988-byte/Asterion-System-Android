# Fase 14 — Trazabilidad de las siete etapas autorizadas

| Etapa | Evidencia concreta |
|---|---|
| 14.1 Auditoría offline | `14-auditoria-offline.md`, manifiesto, almacenamiento e interfaces de fases 05–11 |
| 14.2 Consolidación local | `LocalStorageCheck` y `LocalStorageService` en `:core`; SQLite y SharedPreferences existentes |
| 14.3 Integridad | `SQLiteLocalStorageCheck`, `PRAGMA quick_check(1)`, `PRAGMA foreign_key_check`, `NonDestructiveDatabaseErrorHandler`; restricciones SQLite v5 intactas |
| 14.4 Recuperación ante errores | `OfflineErrorMessages`, controles de reintento manual, validación de callbacks |
| 14.5 Pantallas Android XML | `screen_local_status.xml`, `LocalStatusPresenter`, botones de reintento en seis XML; ruta `LOCAL_STATUS` |
| 14.6 Contratos de sincronización futura | `14-contratos-sincronizacion-futura.md`: **documentación solamente** |
| 14.7 Pruebas, GitHub y HTML | `LocalStorageServiceTest`, `LocalStorageSmoke`, scripts estáticos/SQLite, simulador HTML externo y documentación |

**Límites:** ninguna API remota, cola, migración SQLite nueva, WebView, exportación o función posterior; no compilación APK ni workflows automáticos.
