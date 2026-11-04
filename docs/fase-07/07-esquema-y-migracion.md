# Fase 07 — SQLite v3 y migración no destructiva

## Versiones

La base existente `asterion_internal.db` pasa de `DATABASE_VERSION=2` a `3`.

- **Instalación nueva:** `CREATE_V1`, migración v1→v2 y migración v2→v3.
- **Actualización desde v1:** se ejecutan las dos migraciones en orden, preservando `technical_records`.
- **Actualización desde v2:** solamente se añade el catálogo v3.
- **Versiones incompatibles o degradaciones:** generan una excepción explícita; nunca se borran tablas.

Las migraciones se realizan desde `SQLiteOpenHelper`, que ofrece transacciones para el ciclo de vida de creación y actualización. La opción de claves foráneas se activa en `onConfigure`.

## Tablas añadidas

| Tabla | Propósito | Integridad |
|---|---|---|
| `catalog_categories` | Identificadores, nombres y orden | PK, checks de longitud y de orden |
| `catalog_items` | Título, descripción, tipo, categoría | PK, FK, checks de longitud y tipo |
| `catalog_tags` | Etiquetas normalizadas | PK, etiqueta única |
| `catalog_item_tags` | Relación de muchos a muchos | PK compuesta, FK con borrado en cascada |

Índices: `idx_catalog_category_sort` y `idx_catalog_item_filters`.

La categoría referenciada no puede borrarse si tiene elementos (`ON DELETE RESTRICT`). El borrado de un elemento elimina sus asociaciones de etiquetas, sin borrar el catálogo general de etiquetas.

## Datos iniciales

Las categorías de demostración son General, Conocimiento y Práctica. Se crean cuatro elementos técnicos:

1. `demo-intro`: Introducción al catálogo (contenido).
2. `demo-notas`: Notas de ejemplo (contenido).
3. `demo-revision`: Actividad de comprobación (actividad).
4. `demo-navegacion`: Revisión de navegación (actividad).

Estos datos son ejemplos originales. No se importan ni copian colecciones de otras aplicaciones.

## Consultas seguras

`SQLiteCatalogRepository` utiliza `rawQuery` con parámetros `?`, nunca interpolación de filtros de usuario. Los caracteres especiales `!`, `%` y `_` se escapan antes de aplicar `LIKE ... ESCAPE '!'` para tratarlos como texto literal.

Los tipos y categorías se filtran por IDs controlados; la paginación admite `limit` de 1 a 50 y desplazamiento no negativo. El orden es estable: `title COLLATE NOCASE, id`.

SQLite estándar tiene limitaciones de comparación insensible a mayúsculas con caracteres acentuados. La búsqueda en esta fase no pretende ser una normalización lingüística española completa.

## Pruebas

- `tests/phase05_sqlite_smoke.py` verifica el esquema técnico existente.
- `tests/phase07_catalog_sqlite_smoke.py` está preparada para comprobar migraciones, integridad, consultas y paginación sin Android.
- `tests/java_sql_constants.py` extrae correctamente las constantes SQL del Java, incluso cuando contienen punto y coma dentro de literales.

Una compilación futura autorizada deberá validar `SQLiteOpenHelper`, `Cursor`, índices y consultas en el SDK real; las pruebas de escritorio no bastan para ese fin.
