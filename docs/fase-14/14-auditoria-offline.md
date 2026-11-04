# Fase 14 — Auditoría y consolidación offline-first

## Resultado de auditoría estática

Asterion System está organizado como aplicación Android nativa Java + XML. Todos los módulos aprobados hasta la fase 11 utilizan puertos Java y almacenamiento en el propio dispositivo; no se incorporaron redes, servidores ni SDK de sincronización.

| Módulo | Fuente y ruta | Almacenamiento | Dependencia de internet |
|---|---|---|---|
| Catálogo (07) | `CatalogPresenter` → `BackgroundCatalogOperations` → `SQLiteCatalogRepository` | SQLite `catalog_*` | No |
| Planificador (08) | `SchedulePresenter` → `BackgroundScheduleOperations` → `SQLiteScheduleRepository` | SQLite `planned_activities` | No |
| Seguimiento (09) | `ProgressPresenter` → `BackgroundProgressOperations` → `SQLiteProgressRepository` | SQLite `tracking_results` | No |
| Preferencias (11) | `SettingsPresenter` → `PreferencesService` → `AndroidPreferencesRepository` | `SharedPreferences` privado | No |
| Diagnóstico (14) | `LocalStatusPresenter` → `BackgroundLocalStorageChecks` → `SQLiteLocalStorageCheck` | Inspección SQLite, no escribe | No |

El `AndroidManifest.xml` no solicita `INTERNET` ni `ACCESS_NETWORK_STATE`, mantiene `android:usesCleartextTraffic=false` y `android:allowBackup=false`. Se conserva `DATABASE_VERSION=5`, los índices, triggers y las migraciones antiguas no destructivas. La fase 14 añade un `NonDestructiveDatabaseErrorHandler` explícito para que Android no aplique la eliminación automática del archivo mediante el manejador de corrupción predeterminado. Esta auditoría verifica el código fuente y el manifiesto; las pruebas de ausencia de red en un dispositivo real quedan pendientes hasta que se autorice la ejecución Android.

## Decisiones de arquitectura de la fase

1. No introducir modo de espera de conectividad: los servicios actúan directamente sobre recursos locales.
2. No bloquear rutas de Catálogo, Planificador ni Seguimiento cuando no exista red.
3. No duplicar SQLite con un segundo almacén ni añadir tablas de sincronización o colas remotas.
4. Diagnosticar el almacenamiento bajo petición en un *worker*, utilizando las mismas interfaces Java independientes del SDK Android.
5. Mantener un camino explícito de reintento **solo de lectura** para consultar de nuevo tras un fallo.
6. No volver a ejecutar automáticamente escrituras que pudieron haberse completado antes de una interrupción.

## No confundir conceptos

- **Offline-first:** las funciones autorizadas residen en el dispositivo; no requieren un servidor para operar.
- **Integridad:** SQLite ofrece transacciones, claves foráneas y constraints. La comprobación `quick_check` es un diagnóstico, no una garantía absoluta.
- **Recuperación:** mensajes seguros y reintentos manuales de lectura. Esta fase **no** restaura copias de seguridad, no repara páginas dañadas y no garantiza salvar información de hardware averiado.
- **Sincronización:** deliberadamente no se ha implementado. Se documentan únicamente consideraciones contractuales en otro archivo.

## Interfaz

`screen_local_status.xml` es una pantalla Android XML conectada a `NavigationRoute.LOCAL_STATUS`. Inicio incluye una opción para abrirla. Las seis pantallas de consulta existentes incorporan botones de reintento manual. El acceso a SQLite desde presentadores continúa fuera del hilo principal Android.

El HTML autónomo entregado en la conversación reproduce las pantallas y permite *simular* desconexiones e incidencias sin acceder al dispositivo ni modificar una APK. No se incluye en el proyecto Android.
