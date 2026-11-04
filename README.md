# Asterion System — Android nativo Java

**Estado:** implementadas fases 00, 01, 02, 04, 05, 06, 07, 08, 09, 11, 14 y **15 (optimización, seguridad y accesibilidad)**. La fase 03 de diseño definitivo sigue pendiente.

Proyecto Java nativo Android con vistas XML. El HTML se utiliza **solo como previsualización externa**. No se han incorporado HTML, JavaScript, CSS, WebView o Compose a las pantallas Android.

## Arquitectura

- `:core`: Java puro; dominio, eventos, estado, navegación, reglas, catálogo, planificación, seguimiento, preferencias y diagnósticos locales.
- `:app`: `MainActivity`, presentadores, pantallas XML, SQLiteOpenHelper, repositorios SQLite, diagnósticos y `SharedPreferences` para configuración.
- SQLite `asterion_internal.db`: esquema v5, con migraciones acumulativas hasta 4→5, sin borrar registros anteriores.
- `docs/fase-08/`: especificaciones y pruebas de planificación.

## Fase 08

El planificador permite programar **actividades del catálogo** con fecha civil local, hora inicial y duración de 1–720 minutos sin cruzar medianoche. Un servicio Java y cuatro triggers SQLite previenen entradas superpuestas, incluidos conflictos durante escrituras concurrentes. Las pantallas `screen_plan.xml` y `screen_plan_form.xml` permiten consultar la semana, seleccionar días, crear, editar y eliminar programaciones. Ninguna programación ejecuta automáticamente una actividad ni produce recordatorios.

La previsualización HTML (entregada fuera de la app) simula ese comportamiento con almacenamiento del navegador; no equivale a la ejecución Android.

## Fase 09 — Seguimiento y progreso

El seguimiento permite registrar explícitamente `realizada` u `omitida` para una actividad programada, incluir una observación corta y devolverla a `pendiente` borrando únicamente el resultado. `Pendiente` es ausencia de registro, no un incumplimiento automático. `ProgressSummary` calcula los indicadores semanales sobre las actividades programadas; el porcentaje de cumplimiento divide las realizadas entre las evaluadas (realizadas + omitidas), y muestra «Sin resultados evaluados» cuando corresponda.

La base de datos usa SQLite v5 y una única tabla adicional `tracking_results` con clave foránea y borrado en cascada para evitar resultados huérfanos. La pantalla diaria y el resumen semanal están en `screen_progress.xml` y `screen_progress_summary.xml`, manejados en Java. La previsualización HTML independiente incluye estos comportamientos, pero solo guarda datos simulados en el navegador.

## Fase 11 — Personalización local

La pantalla Android `screen_settings.xml` incorpora exactamente tres preferencias:
- **Texto:** normal o grande (escala aproximada de 1,18 en textos de pantallas XML).
- **Listas:** densidad normal o compacta en filas de catálogo, planificador y seguimiento.
- **Sección inicial:** Inicio, Catálogo, Planificador o Seguimiento.

Las preferencias se guardan en `SharedPreferences`, archivo privado `asterion_preferences_v1`, mediante `AndroidPreferencesRepository` en `:app`. Los modelos `AppPreferences`, `PreferencesRepository` y `PreferencesService` permanecen en Java puro (`:core`). El usuario puede restablecer únicamente preferencias, sin borrar SQLite.

La sección inicial se aplica exclusivamente en un **arranque sin estado guardado**; al restaurar una Activity existente, la navegación guardada siempre tiene prioridad. La preferencia de inicio no activa actividades ni notificaciones.

La previsualización HTML externa de fase 11 permite probar estas opciones y conserva la navegación de las fases previas. Es una **simulación con localStorage**, no un WebView ni una conexión con SharedPreferences Android.

## Fase 14 — Offline-first e integridad local

Las funciones aprobadas siguen siendo locales y no requieren conexión a internet. La nueva pantalla nativa `screen_local_status.xml` ejecuta una inspección **de solo lectura** de SQLite (`PRAGMA quick_check(1)`, `PRAGMA foreign_key_check` y recuentos de registros), fuera del hilo principal. El manejador explícito `NonDestructiveDatabaseErrorHandler` evita la eliminación automática de la base por el manejador predeterminado de corrupción; no se repara ni borra la base automáticamente.

Catálogo, Planificador y Seguimiento incorporan reintentos manuales de consulta y mensajes controlados para problemas de almacenamiento, espacio o integridad; **las escrituras fallidas no se repiten automáticamente**. Se conservan el esquema SQLite v5, las transacciones y claves foráneas de las fases anteriores. No se han añadido permisos de internet, cuentas, red ni sincronización.

Solo se documentaron los contratos hipotéticos de sincronización futura en `docs/fase-14/14-contratos-sincronizacion-futura.md`; **no se implementó** sincronización ni transporte de datos. La previsualización HTML externa permite simular desconexión, acceso fallido, espacio insuficiente, advertencia y reintentos sin afectar a datos de Android.

Comprobaciones de fuente: `python3 tests/phase14_offline_static_smoke.py`. Pruebas SQLite de escritorio desde las constantes Java: `python3 tests/phase14_sqlite_integrity_smoke.py`. Prueba JVM solo Java puro: `sh tests/phase14_core_jvm_smoke.sh`. Ninguno de esos comandos genera APK. La verificación independiente del núcleo mediante `javac --release 17` superó 13 aserciones, y SQLite de escritorio superó 6 pruebas con un esquema equivalente; la ejecución de los scripts de comprobación contra el checkout completo sigue pendiente.

## Fase 15 — Optimización, integridad y accesibilidad

- **Planificador:** resuelve títulos del catálogo mediante `CatalogRepository.titlesById` y consultas SQLite por lotes de hasta 200 IDs, usando parámetros enlazados. Evita una consulta individual por cada programación.
- **Catálogo:** espera 280 ms tras la última edición del texto para ejecutar la búsqueda. Cancela solicitudes diferidas, invalida respuestas obsoletas y retira el `TextWatcher` al reemplazar la pantalla.
- **Diagnóstico:** verifica que las claves foráneas estén activas; obtiene los contadores de las tres tablas en una sentencia SQL consistente.
- **Seguridad:** conserva los permisos restringidos, `SharedPreferences` privado, SQLite v5, el manejo no destructivo de corrupción y las consultas parametrizadas.
- **Accesibilidad:** siete regiones de estado con avisos para lectores de pantalla, tres controles con `labelFor`, anuncios de día completo y fila compacta con altura mínima 48 dp.

Pruebas **sin Android** efectuadas: previsualización HTML externa con 36 verificaciones Chromium y cinco ensayos equivalentes SQLite de escritorio con el esquema de consultas por lotes. Se añadieron pruebas estáticas Python y fuente JUnit para una ejecución posterior autorizada. No constituyen pruebas de una APK Android real.

## Sin compilar APK

**No se compila ni genera APK sin autorización expresa.** `.github/workflows/android-ci.yml` conserva solamente `workflow_dispatch`. Las publicaciones en GitHub no activan trabajos automáticos de compilación.

Las verificaciones de las fases 08 y 09 incluyen SQLite de escritorio y previsualizaciones HTML interactivas; las pruebas Java JUnit permanecen preparadas, **sin ejecutarse por Gradle**. Para fase 09 se ejecutaron 23 comprobaciones de interacción con Chromium y 8 pruebas de restricciones equivalentes del nuevo esquema SQLite. No equivalen a pruebas Android nativas. La compilación y ejecución Android siguen pendientes.

Para comprobar archivos y referencias sin generar APK: `python3 tests/phase11_preferences_static_smoke.py`. Para una prueba JVM independiente, ejecutar `sh tests/phase11_jvm_smoke.sh`: compila exclusivamente tres clases Java de `:core` con `javac --release 17` y ejecuta 18 comprobaciones. Esta prueba local pasó durante el desarrollo, con hashes de archivos idénticos a los publicados. Las pruebas JUnit fuente permanecen disponibles, pero **no** se ejecutó Gradle, la compilación Android ni la generación de APK durante la fase 11.

## Documentación

- [Fase 15 — Auditoría y riesgos](docs/fase-15/15-auditoria-y-riesgos.md)
- [Fase 15 — Optimización e integridad](docs/fase-15/15-optimizacion-e-integridad.md)
- [Fase 15 — Seguridad y accesibilidad](docs/fase-15/15-seguridad-y-accesibilidad.md)
- [Fase 15 — Validación](docs/fase-15/15-validacion.md)
- [Fase 15 — Trazabilidad](docs/fase-15/15-trazabilidad.md)


- [Fase 14 — Auditoría offline](docs/fase-14/14-auditoria-offline.md)
- [Fase 14 — Integridad y recuperación](docs/fase-14/14-integridad-y-recuperacion.md)
- [Fase 14 — Contratos hipotéticos de sincronización](docs/fase-14/14-contratos-sincronizacion-futura.md)
- [Fase 14 — Validación](docs/fase-14/14-validacion.md)
- [Fase 14 — Trazabilidad](docs/fase-14/14-trazabilidad.md)


- [Fase 11 — Arquitectura y preferencias](docs/fase-11/11-arquitectura-preferencias.md)
- [Fase 11 — Validación y límites](docs/fase-11/11-validacion.md)
- [Fase 11 — Trazabilidad](docs/fase-11/11-trazabilidad.md)


- [Fase 09 — Arquitectura de seguimiento](docs/fase-09/09-arquitectura-seguimiento.md)
- [Fase 09 — SQLite y migración](docs/fase-09/09-sqlite-migracion.md)
- [Fase 09 — Validación y límites](docs/fase-09/09-validacion-y-limitaciones.md)
- [Fase 09 — Trazabilidad](docs/fase-09/09-trazabilidad.md)

- [Arquitectura de planificación](docs/fase-08/08-arquitectura.md)
- [Esquema y migración](docs/fase-08/08-esquema-y-migracion.md)
- [Validación y limitaciones](docs/fase-08/08-validacion.md)
- [Trazabilidad](docs/fase-08/08-trazabilidad.md)

## Fases

La fase 03 (diseño visual definitivo) continúa pendiente de aprobación. Fases 14 y 15 aprobadas y desarrolladas. La fase 16 (compilación, firma y entrega APK) no está autorizada. Fases 15 y 16 están incluidas en el plan maestro para validación, optimización y publicación futura, sujetas a alcance y autorización. Fases 10, 12 y 13 excluidas.

El repositorio es público pero no se ha seleccionado licencia de reutilización; no incorporar contraseñas, certificados o datos sensibles.
