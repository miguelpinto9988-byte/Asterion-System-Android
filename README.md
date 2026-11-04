# Asterion System — Android nativo Java

**Estado:** implementadas fases 00, 01, 02, 04, 05, 06, 07 y **08 (planificación y programación)** y **09 (seguimiento y progreso)**. La fase 03 (diseño definitivo) permanece pendiente.

Proyecto Java nativo Android con vistas XML. El HTML se utiliza **solo como previsualización externa**. No se han incorporado HTML, JavaScript, CSS, WebView o Compose a las pantallas Android.

## Arquitectura

- `:core`: Java puro; dominio, eventos, estado, navegación, reglas, catálogo y planificación.
- `:app`: `MainActivity`, adaptadores Android, pantallas XML, SQLiteOpenHelper y repositorios SQLite.
- SQLite `asterion_internal.db`: esquema v5, con migraciones acumulativas hasta 4→5, sin borrar registros anteriores.
- `docs/fase-08/`: especificaciones y pruebas de planificación.

## Fase 08

El planificador permite programar **actividades del catálogo** con fecha civil local, hora inicial y duración de 1–720 minutos sin cruzar medianoche. Un servicio Java y cuatro triggers SQLite previenen entradas superpuestas, incluidos conflictos durante escrituras concurrentes. Las pantallas `screen_plan.xml` y `screen_plan_form.xml` permiten consultar la semana, seleccionar días, crear, editar y eliminar programaciones. Ninguna programación ejecuta automáticamente una actividad ni produce recordatorios.

La previsualización HTML (entregada fuera de la app) simula ese comportamiento con almacenamiento del navegador; no equivale a la ejecución Android.

## Fase 09 — Seguimiento y progreso

El seguimiento permite registrar explícitamente `realizada` u `omitida` para una actividad programada, incluir una observación corta y devolverla a `pendiente` borrando únicamente el resultado. `Pendiente` es ausencia de registro, no un incumplimiento automático. `ProgressSummary` calcula los indicadores semanales sobre las actividades programadas; el porcentaje de cumplimiento divide las realizadas entre las evaluadas (realizadas + omitidas), y muestra «Sin resultados evaluados» cuando corresponda.

La base de datos usa SQLite v5 y una única tabla adicional `tracking_results` con clave foránea y borrado en cascada para evitar resultados huérfanos. La pantalla diaria y el resumen semanal están en `screen_progress.xml` y `screen_progress_summary.xml`, manejados en Java. La previsualización HTML independiente incluye estos comportamientos, pero solo guarda datos simulados en el navegador.

## Sin compilar APK

**No se compila ni genera APK sin autorización expresa.** `.github/workflows/android-ci.yml` conserva solamente `workflow_dispatch`. Las publicaciones en GitHub no activan trabajos automáticos de compilación.

Las verificaciones de las fases 08 y 09 incluyen SQLite de escritorio y previsualizaciones HTML interactivas; las pruebas Java JUnit permanecen preparadas, **sin ejecutarse por Gradle**. Para fase 09 se ejecutaron 23 comprobaciones de interacción con Chromium y 8 pruebas de restricciones equivalentes del nuevo esquema SQLite. No equivalen a pruebas Android nativas. La compilación y ejecución Android siguen pendientes.

## Documentación

- [Fase 09 — Arquitectura de seguimiento](docs/fase-09/09-arquitectura-seguimiento.md)
- [Fase 09 — SQLite y migración](docs/fase-09/09-sqlite-migracion.md)
- [Fase 09 — Validación y límites](docs/fase-09/09-validacion-y-limitaciones.md)
- [Fase 09 — Trazabilidad](docs/fase-09/09-trazabilidad.md)

- [Arquitectura de planificación](docs/fase-08/08-arquitectura.md)
- [Esquema y migración](docs/fase-08/08-esquema-y-migracion.md)
- [Validación y limitaciones](docs/fase-08/08-validacion.md)
- [Trazabilidad](docs/fase-08/08-trazabilidad.md)

## Fases

Fases 03, 11 y 14 pendientes de aprobación. Fases 15 y 16 están incluidas en el plan maestro para validación, optimización y publicación futura, sujetas a alcance y autorización. Fases 10, 12 y 13 excluidas.

El repositorio es público pero no se ha seleccionado licencia de reutilización; no incorporar contraseñas, certificados o datos sensibles.
