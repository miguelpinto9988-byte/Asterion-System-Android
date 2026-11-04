# Fase 15 — Matriz de trazabilidad

| Etapa | Entrega |
|---|---|
| 15.1 Auditoría | `15-auditoria-y-riesgos.md`, revisión de estructura Android, permisos, persistencia e incidencias |
| 15.2 Optimización | `CatalogRepository.titlesById`, `SQLiteCatalogRepository.titlesById`, `SchedulePlanner.listWithTitles`, debounce en `CatalogPresenter` |
| 15.3 Seguridad | Consultas por placeholders, IDs limitados, revisión de manifiesto/SharedPreferences y manejador SQLite no destructivo |
| 15.4 Integridad | `SQLiteLocalStorageCheck` confirma `PRAGMA foreign_keys` y realiza un `SELECT` conjunto de contadores |
| 15.5 Accesibilidad | XML con regiones vivas y etiquetas, `SchedulePresenter` anuncia días seleccionados, `PreferencesRowAdapter` filas de 48 dp |
| 15.6 Pruebas | `SchedulePlannerBulkTitlesTest` (fuente pendiente), dos scripts Python preparados; HTML con 36 controles y 5 ensayos SQLite equivalentes realmente ejecutados |
| 15.7 GitHub + HTML + documentación | Commit en `main`, previsualización HTML externa y `docs/fase-15` |

No se implementaron funciones futuras, migración de SQLite, sincronización remota ni cambios de diseño visual final. No se ejecutó Gradle ni se generó APK.
