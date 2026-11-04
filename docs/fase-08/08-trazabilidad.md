# Fase 08 — Matriz de trazabilidad

| Etapa aprobada | Evidencia |
|---|---|
| 08.1 Modelo de planificación | `:core/planning/ScheduledActivity.java`, `ScheduleRepository.java` |
| 08.2 Gestor de programación | `SchedulePlanner.java`, `BackgroundScheduleOperations.java` |
| 08.3 Validación y conflictos | `ScheduledActivity.overlaps`, `ScheduleConflictException`, triggers SQLite |
| 08.4 Persistencia | `AsterionDatabaseHelper` v4, `SQLiteScheduleRepository` |
| 08.5 Pantallas XML | `screen_plan.xml`, `screen_plan_form.xml`, `SchedulePresenter.java` |
| 08.6 Integración | `NavigationRenderer`, `MainActivity`, `NavigationRoute`, `CatalogRepository` |
| 08.7 Pruebas y HTML | `SchedulePlannerTest`, prueba SQL sin SDK, HTML interactivo externo y documentación |

## Restricciones

Se mantiene Java/XML como interfaz Android; HTML solo previsualiza. No hay sincronización, recordatorios, multimedia, seguimiento, recurrencia ni reglas automáticas del motor 06. El workflow `.github/workflows/android-ci.yml` continúa sin disparadores `push` o `pull_request` y requiere ejecución manual explícita. Ninguna APK se compila.
