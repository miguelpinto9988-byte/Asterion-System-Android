# Fase 09 — SQLite versión 5, migración 4→5

Base privada `asterion_internal.db`, ahora con `DATABASE_VERSION = 5`.

## Tabla nueva

`tracking_results`:

| Columna | Tipo | Integridad |
|---|---|---|
| `schedule_id` | TEXT PRIMARY KEY | FK `planned_activities(id)` ON DELETE CASCADE |
| `status` | TEXT | solo `completed` o `skipped` |
| `note` | TEXT | no nulo, longitud 0–500 |
| `recorded_at_ms` | INTEGER | milisegundos epoch, no negativo |

**No existe fila para `pending`**. La clave primaria limita a **un resultado por programación**. La FK hace que eliminar una programación también elimine su resultado obsoleto. No se borra ninguna tabla ni registro existente durante la migración.

## Consultas y actualización

`SQLiteProgressRepository` lee `planned_activities` y `catalog_items` con un `LEFT JOIN` a `tracking_results`. Devuelve pendientes cuando no hay fila. Las consultas usan parámetros enlazados y aprovechan el índice de planificación de fase 08.

El guardado está protegido por una transacción: primero actualiza la fila por clave primaria; si no existe, la inserta. `clear` elimina únicamente el resultado y deja intacta la programación. El adaptador prohíbe I/O de SQLite desde el hilo principal Android.

## Evolución de esquemas

- Nueva instalación: v1→v2→v3→v4→v5.
- Instalación previa v4: agrega exclusivamente la nueva tabla.
- Versiones no reconocidas o downgrade: error explícito sin eliminación destructiva.

El script `tests/phase09_progress_sqlite_smoke.py` extrae la constante SQL declarada en Java y comprueba localmente migraciones, restricciones, edición, cascada y agregación. No sustituye `SQLiteOpenHelper` real de Android.
