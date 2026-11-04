# Fase 05 — Esquema, evolución y reglas de integridad

## Versión y alcance

Base `asterion_internal.db`, esquema `DATABASE_VERSION=2`, tabla `technical_records`.

| Campo | SQL | Restricción |
|---|---|---|
| `id` | TEXT PRIMARY KEY | no nulo, longitud 1–64 |
| `title` | TEXT | no nulo, longitud 1–120 |
| `note` | TEXT | no nulo, longitud 0–1000 |
| `created_at_ms` | INTEGER | no nulo, no negativo |
| `updated_at_ms` | INTEGER | no nulo, no negativo; nunca anterior a creación |

Índice: `idx_technical_records_updated` sobre `updated_at_ms DESC, id ASC`.

Dos triggers previenen insertar o actualizar registros cuyo `updated_at_ms` sea anterior a `created_at_ms`. La validación del dominio complementa, pero no reemplaza, las restricciones en SQLite.

## Migración 1 → 2

La versión 1 contenía únicamente ID, título, nota y fecha de creación. La migración:

1. Agrega `updated_at_ms` con valor inicial cero.
2. Copia `created_at_ms` a `updated_at_ms` para todos los registros preexistentes.
3. Crea un índice para consultas ordenadas.
4. Instala triggers de consistencia temporal.

Un alta nueva crea el esquema v1 y aplica la misma migración, de modo que el resultado es el esquema v2. `SQLiteOpenHelper` gestiona estas operaciones en su transacción de creación/actualización.

No se elimina ninguna tabla ni se realiza `DROP TABLE`. Versiones superiores no reconocidas o degradaciones generan excepción; **no** se realiza un borrado destructivo como alternativa.

## Mantenimiento futuro

Las migraciones futuras deben ser escalonadas (2→3, 3→4, etc.), transaccionales y probadas con datos de versiones anteriores. La base de fase 05 es de pruebas técnicas: no se incorporan todavía entidades definitivas ni se realiza traslado de datos desde Hunter o Agenda Clara.

## SQL verificable

Los seis enunciados canónicos de creación/migración están en `AsterionDatabaseHelper.java`. El script `tests/phase05_sqlite_smoke.py` los extrae del Java y los prueba con SQLite de Python. Este mecanismo **no** equivale a ejecutar `SQLiteOpenHelper` en Android, pero reduce el riesgo de incoherencia del esquema.

Para ejecutarlo localmente sin SDK y sin compilar APK:

```bash
python3 tests/phase05_sqlite_smoke.py
```
