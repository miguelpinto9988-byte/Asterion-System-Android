# Asterion System — Android nativo Java

**Estado:** implementadas fases 00, 01, 02, 04, 05, 06, 07, 08, 09 y **11 (personalización y preferencias)**. La fase 03 de identidad visual definitiva permanece pendiente.

Proyecto Java nativo Android con vistas XML. El HTML se utiliza **solo como previsualización externa**. No se han incorporado HTML, JavaScript, CSS, WebView o Compose a las pantallas Android.

## Arquitectura

- `:core`: Java puro; dominio, eventos, estado, navegación, reglas, catálogo, planificación, seguimiento y preferencias.
- `:app`: `MainActivity`, presentadores, pantallas XML, SQLiteOpenHelper, repositorios SQLite y `SharedPreferences` para configuración.
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

## Sin compilar APK

**No se compila ni genera APK sin autorización expresa.** `.github/workflows/android-ci.yml` conserva solamente `workflow_dispatch`. Las publicaciones en GitHub no activan trabajos automáticos de compilación.

Las verificaciones de las fases 08 y 09 incluyen SQLite de escritorio y previsualizaciones HTML interactivas; las pruebas Java JUnit permanecen preparadas, **sin ejecutarse por Gradle**. Para fase 09 se ejecutaron 23 comprobaciones de interacción con Chromium y 8 pruebas de restricciones equivalentes del nuevo esquema SQLite. No equivalen a pruebas Android nativas. La compilación y ejecución Android siguen pendientes.

Para comprobar archivos y referencias sin generar APK: `python3 tests/phase11_preferences_static_smoke.py`. Para una prueba JVM independiente, ejecutar `sh tests/phase11_jvm_smoke.sh`: compila exclusivamente tres clases Java de `:core` con `javac --release 17` y ejecuta 18 comprobaciones. Esta prueba local pasó durante el desarrollo, con hashes de archivos idénticos a los publicados. Las pruebas JUnit fuente permanecen disponibles, pero **no** se ejecutó Gradle, la compilación Android ni la generación de APK durante la fase 11.

## Documentación

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

Fases 03 y 14 pendientes de aprobación. Fases 15 y 16 están incluidas en el plan maestro para validación, optimización y publicación futura, sujetas a alcance y autorización. Fases 10, 12 y 13 excluidas.

El repositorio es público pero no se ha seleccionado licencia de reutilización; no incorporar contraseñas, certificados o datos sensibles.
