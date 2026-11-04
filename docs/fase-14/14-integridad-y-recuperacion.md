# Fase 14 — Integridad, diagnósticos y tratamiento de fallos

## Lecturas diagnósticas de solo lectura

`SQLiteLocalStorageCheck.inspect()` ejecuta en un worker:

1. `PRAGMA quick_check(1)`: verificación rápida de coherencia estructural SQLite. No sustituye una inspección completa `integrity_check`.
2. `PRAGMA foreign_key_check`: detecta referencias rotas entre tablas.
3. Consultas `SELECT COUNT(*)` de `catalog_items`, `planned_activities` y `tracking_results`.

El adaptador crea un `LocalStorageReport` inmutable (Java puro) con indicadores separados. Si las dos comprobaciones pasan, el estado es `HEALTHY`; si cualquiera falla, se informa `ATTENTION`. Si no se puede consultar la base, se devuelve un error controlado en la operación asíncrona.

Las tres cuentas son lecturas independientes; bajo cambios simultáneos podrían no representar una instantánea atómica. Se recomienda repetir el diagnóstico cuando las cifras sean temporalmente inconsistentes. La función de diagnóstico no ejecuta `INSERT`, `UPDATE`, `DELETE`, reparaciones ni cambio de esquema. En una instalación nueva, `SQLiteOpenHelper.getReadableDatabase()` podría necesitar crear el esquema inicial; eso es funcionamiento normal de apertura de SQLite, **no** una reparación.

## Prevención de eliminación automática al detectar corrupción

En Android, el manejador de corrupción predeterminado puede borrar un fichero SQLite dañado como mecanismo de recuperación. Para que eso **no ocurra silenciosamente** en Asterion System, `AsterionDatabaseHelper` suministra `NonDestructiveDatabaseErrorHandler` al constructor de `SQLiteOpenHelper`.

Su único comportamiento es registrar un aviso genérico sin la ruta de la base de datos ni datos personales. No ejecuta `deleteDatabase`, `DROP`, copia, recreación ni reparación. El acceso puede fallar y requerirá una revisión autorizada posterior. Esta protección no garantiza recuperar un fichero ya dañado y debe verificarse en Android real.

## Integridad transaccional existente

La fase preserva las garantías que ya existían:

- Base `asterion_internal.db`, esquema v5, migraciones ascendentes no destructivas.
- `SQLiteOpenHelper.onConfigure`: `foreign_keys=ON`.
- `setWriteAheadLoggingEnabled(true)` para la base compartida.
- Los repositorios de planificación y seguimiento usan transacciones para cambios.
- Triggers de planificación rechazan superposiciones y elementos no marcados `activity`.
- Restricciones de SQLite y claves foráneas evitan resultados de seguimiento huérfanos.

Se conservan múltiples helpers conectados a la misma base, cada uno cerrado al finalizar su worker. La prueba bajo concurrencia real de Android sigue pendiente.

## Mensajes y recuperación controlada

`OfflineErrorMessages` identifica causas tipadas de Android SQLite sin mostrar sentencias SQL ni rutas internas:

- `SQLiteDatabaseCorruptException`: posible problema de integridad, sin reparación automática.
- `SQLiteFullException`: falta de espacio, resolver manualmente antes de repetir operación.
- `SQLiteCantOpenDatabaseException` / `SQLiteDiskIOException`: recurso local no disponible.
- Otros: error genérico de acceso local, sin recomendar conectarse a internet.

En Catálogo (lista y detalle), Planificador (calendario y formulario) y Seguimiento (lista y resumen) se proporcionan botones de **reintento manual de consulta**. Los listeners renuevan el token de pantalla o de petición, para ignorar respuestas anteriores. Ningún botón reejecuta automáticamente una escritura.

La pantalla de diagnóstico muestra contadores orientativos, estado de `quick_check`, estado de referencias y control para volver a comprobar. El proceso no borra registros al identificar un fallo.

## Limitaciones y riesgos pendientes

- No hay copias de seguridad, importación/exportación ni recuperación de ficheros SQLite corruptos.
- No se puede inferir salud física del almacenamiento desde `quick_check`.
- El simulador HTML no valida el SDK ni los detalles de `SQLiteOpenHelper` en un teléfono.
- La falta de memoria, un apagado del dispositivo y operaciones simultáneas en hardware Android requieren pruebas específicas futuras.
- La verificación de referencias depende de tener activadas claves foráneas al escribir; esta comprobación las examina sin reparar relaciones ya dañadas.
