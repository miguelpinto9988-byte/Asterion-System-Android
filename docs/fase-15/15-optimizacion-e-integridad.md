# Fase 15 — Optimización de consultas e integridad

## Planificador: de N+1 a lotes

Antes, `SchedulePlanner.listWithTitles` llamaba `catalog.findById` por cada `ScheduledActivity`, aunque varios registros referenciaran la misma actividad.

Ahora:

1. Obtiene la lista de programaciones del día una sola vez.
2. Reúne todos sus identificadores de actividad.
3. Llama una sola vez al contrato Java `CatalogRepository.titlesById(List<EntityId>)`.
4. `SQLiteCatalogRepository` elimina duplicados, valida identificadores y emite consultas `SELECT id,title ... WHERE id IN (?, ?, ...)` con **hasta 200 valores enlazados por lote**.
5. Construye la misma lista en el mismo orden, con texto de reserva para actividades no encontradas.

El método `default` del puerto conserva compatibilidad con otros repositorios, mientras que SQLite proporciona la implementación por lotes. La búsqueda normal y la validación de actividades siguen usando sus contratos anteriores.

Ejemplo ilustrativo: 450 identificadores distintos requerían 450 búsquedas individuales de título; se pueden consultar en `ceil(450/200)=3` lotes. No se registró una medición real de latencia; este dato describe el **número estimado de consultas de títulos**.

## Catálogo: debounce y control de ciclo de vida

`CatalogPresenter` aplica `SEARCH_DEBOUNCE_MS=280L` únicamente a cambios en el texto. Al escribir:

- Incrementa el token de generación para invalidar una respuesta anterior.
- Cancela la tarea pendiente del `Handler`.
- Espera 280 ms desde el último cambio para emitir la consulta.
- Si cambia el filtro de categoría/tipo o se solicita reintento, cancela la espera y consulta inmediatamente.
- Retira el `TextWatcher` anterior al volver a enlazar la pantalla o cerrarla, evitando escuchas acumuladas.

La paginación se mantiene limitada a 50 resultados por contrato `CatalogQuery`; no se amplía el catálogo a la memoria completa del dispositivo.

## Integridad y consistencia de diagnósticos

`SQLiteLocalStorageCheck.foreignKeysOk` verifica primero que `PRAGMA foreign_keys` esté activado y después que `PRAGMA foreign_key_check` no devuelva incidencias.

Los contadores del diagnóstico se obtienen en **una única sentencia SELECT**, con subconsultas de conteo sobre `catalog_items`, `planned_activities` y `tracking_results`; sus valores corresponden a la instantánea de esa sentencia, en lugar de tres sentencias potencialmente desalineadas.

`PRAGMA quick_check(1)` sigue siendo un control **rápido**, no una revisión completa ni reparación. El manejador no destructivo de corrupción se conserva.

No se alteraron tablas, triggers, restricciones, índice, WAL, la versión 5 o los métodos de escritura transaccional de fases anteriores.

## Pruebas

`tests/phase15_batched_titles_sqlite_smoke.py` reproduce la estructura de lotes y valida correspondencia con consultas individuales, lotes 1/199/200/201/400/450, IDs faltantes, vacío y un identificador con caracteres de inyección. Es una prueba **equivalente de SQLite de escritorio**; no ejecuta las clases Java Android.

`SchedulePlannerBulkTitlesTest` es una nueva fuente JUnit para verificar el puerto en una implementación falsa sin SQLite, con orden estable y referencia faltante. **Pendiente de ejecutar en un entorno Java con JUnit**; no se ejecutó Gradle.
