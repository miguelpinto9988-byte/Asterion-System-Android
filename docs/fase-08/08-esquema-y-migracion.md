# Fase 08 — SQLite versión 4 y migración 3 → 4

Base: `asterion_internal.db`; `DATABASE_VERSION=4`.

## Nueva tabla

`planned_activities`

| Columna | Tipo | Integridad |
|---|---|---|
| `id` | TEXT PRIMARY KEY | 1–64 caracteres |
| `catalog_item_id` | TEXT FK | referencia a `catalog_items(id)`, no se permite borrar actividad referenciada |
| `local_date` | TEXT | longitud 10, día civil; el modelo Java valida `LocalDate` |
| `start_minute` | INTEGER | 0–1439 |
| `duration_minutes` | INTEGER | 1–720 y fin antes o a las 24:00 |
| `created_at_ms` | INTEGER | no negativo |
| `updated_at_ms` | INTEGER | mayor o igual a creación |

Índice: `idx_plan_date_start(local_date, start_minute, id)`.

## Triggers

- `trg_plan_overlap_insert`: bloquea inserción superpuesta el mismo día.
- `trg_plan_overlap_update`: bloquea edición superpuesta, excluyendo el registro actualizado.
- `trg_plan_activity_insert`: exige un registro `catalog_items.kind='activity'`.
- `trg_plan_activity_update`: mantiene la restricción anterior al editar.

Estos triggers son el control de integridad **atómico** que complementa la comprobación previa en Java. Impiden que dos escrituras concurrentes eludan el detector del servicio.

## Migración

La creación de una nueva base aplica las etapas 1→2→3→4. La actualización de una existente desde v3 añade la nueva tabla, índice y cuatro triggers sin modificar las tablas antiguas ni su contenido.

No hay `DROP TABLE`, exportaciones, borrados ni degradaciones automáticas. Las migraciones no reconocidas fallan explícitamente en `SQLiteOpenHelper`.

## Prueba de escritorio sin APK

`python3 tests/phase08_planning_sqlite_smoke.py` extrae directamente de `AsterionDatabaseHelper.java` las sentencias SQL canónicas, ejecuta migraciones hasta v4 y comprueba superposición, horarios adyacentes, claves foráneas, intervalos inválidos, edición y conservación de datos. No reemplaza una prueba en Android.
