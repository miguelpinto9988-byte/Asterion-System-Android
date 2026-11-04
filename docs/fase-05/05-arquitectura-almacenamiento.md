# Fase 05 — Arquitectura de almacenamiento SQLite

## Objetivo y límites

Preparar una infraestructura de persistencia nativa que pueda usar la arquitectura Android en fases posteriores. No se conecta aún a pantallas, al motor de reglas o a funciones de Hunter System. El diseño XML de fase 04 no se modifica.

## Capas

```text
:core (Java puro)
  data/TechnicalRecord                  entidad técnica inmutable
  data/TechnicalRecordRepository        interfaz CRUD + consulta
  data/RecordReader                     interfaz de lectura de fase 02
  data/BackgroundRecordOperations       puerta asíncrona con un solo worker
  data/StorageException                 excepción de almacenamiento, sin SDK Android
          |
          v
:app (Android Java)
  data/AsterionDatabaseHelper           SQLiteOpenHelper, esquema y migraciones
  data/SQLiteTechnicalRecordRepository  implementación nativa de consultas
```

Las direcciones de dependencia apuntan de `:app` a `:core`. Ningún paquete Java del núcleo importa `android.*`.

## Registro técnico

`TechnicalRecord` es una entidad auxiliar mínima para verificar CRUD. No representa tareas, rutinas, entrenamientos, ejercicios, recordatorios ni elementos del negocio.

Campos: `id` estable, `title`, `note`, `createdAtMs`, `updatedAtMs`. Validación en Java y restricciones SQL. Los timestamps son epoch milliseconds y se proporcionan explícitamente; no hay automatización por horario ni notificaciones.

## Contrato de repositorio

- `create(record)` inserta; ante conflicto o error de SQLite lanza `StorageException`.
- `findById(id)` devuelve `Optional`.
- `list(limit, offset)` pagina por `updated_at_ms DESC, id ASC`; `limit` entre 1 y 100.
- `update(record)` modifica título, nota y fecha de edición, conservando ID y fecha original; retorna `false` si el ID/fecha de creación no coincide.
- `delete(id)` elimina; retorna `false` si no existe.
- `count()` cuenta registros.

Todos los filtros por entrada de usuario emplean argumentos enlazados; no se concatenan textos de usuario al SQL. Los límites de paginación son numéricos y se validan antes de construir la cláusula LIMIT.

## Transacciones y concurrencia

- Todas las escrituras se ejecutan en transacciones SQLite con `setTransactionSuccessful()`.
- Cursores de consulta se cierran mediante try-with-resources.
- La conexión es administrada por `SQLiteOpenHelper`; su instancia debe cerrarse al finalizar el alcance propietario.
- `SQLiteTechnicalRecordRepository` rechaza explícitamente ejecuciones en el hilo principal Android.
- `BackgroundRecordOperations` expone `CompletableFuture` y serializa las llamadas en un worker. Se debe cerrar al terminar su propietario.
- Los callbacks de `CompletableFuture` se ejecutan normalmente fuera del hilo principal; cualquier futura modificación de vistas debe cambiar explícitamente al hilo de UI.

## Protección local

Base privada `asterion_internal.db` dentro del sandbox de la aplicación, sin permisos de almacenamiento externo. La configuración existente `android:allowBackup="false"` se mantiene. No hay conectores, exportaciones, nube ni permisos nuevos.

La base **no está cifrada a nivel de archivo**. No debe utilizarse para almacenar credenciales, claves o información altamente sensible sin un diseño adicional autorizado.

## Integración con la navegación

Actualmente `MainActivity` mantiene las dos pantallas XML de fase 04 sin abrir esta base. En consecuencia, el almacenamiento no aparece en la APK mientras no se integre en una futura fase autorizada.

La previsualización HTML que acompaña a esta entrega es un **laboratorio simulado**, no una implementación Android ni una llamada real a SQLite.
