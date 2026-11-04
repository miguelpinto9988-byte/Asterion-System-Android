# Fase 09 — Seguimiento de actividades y progreso

## Alcance

Se registra explícitamente el resultado de una **actividad programada** (fase 08), sin modificar su fecha, hora, duración ni referencia al catálogo (fase 07).

Un resultado puede ser:

- `PENDING`: sin fila de seguimiento, no registrado todavía.
- `COMPLETED`: usuario marca actividad realizada.
- `SKIPPED`: usuario marca actividad omitida.

Una actividad programada nunca se marca automáticamente como realizada. El usuario puede corregir `COMPLETED` a `SKIPPED`, regresar a `PENDING` o modificar su observación de hasta 500 caracteres.

## Capas

- `:core/progress/TrackingStatus.java`: estados tipados, código almacenado estable.
- `:core/progress/TrackingRecord.java`: resultado explícito inmutable y validado.
- `:core/progress/ProgressEntry.java`: programación + título del catálogo + estado.
- `:core/progress/ProgressRepository.java`: puerto de consultas y modificaciones.
- `:core/progress/ProgressSummary.java`: métricas derivadas, sin Android.
- `:core/progress/ProgressService.java`: reglas de escritura y agregación semanal.
- `:core/progress/BackgroundProgressOperations.java`: serialización de I/O en un único worker.
- `:app/data/SQLiteProgressRepository.java`: adaptador SQLite con lectura de tablas `planned_activities`, `catalog_items` y `tracking_results`.
- `:app/presentation/ProgressPresenter.java`: traduce operaciones Java en cambios visuales en pantalla Android y rechaza callbacks tardíos.
- `screen_progress.xml` y `screen_progress_summary.xml`: dos layouts Android XML nativos y provisionales.

## Indicador de cumplimiento

La semana abarca de lunes (incluido) al siguiente lunes (excluido), en la fecha civil almacenada en el planificador.

- **Programadas**: todas las actividades del período.
- **Realizadas**: registradas `COMPLETED`.
- **Omitidas**: registradas `SKIPPED`.
- **Pendientes**: programadas sin registro explícito.
- **Evaluadas**: realizadas + omitidas.
- **Cumplimiento**: realizadas / evaluadas × 100, redondeado al entero más próximo.

Si **evaluadas=0**, la interfaz muestra «Sin resultados evaluados» y no interpreta `0%` como una tasa significativa. No se considera pendiente automáticamente a una actividad omitida por el transcurso del tiempo, ni se contabilizan actividades futuras como incumplimientos.

Por ejemplo: 8 programadas, 5 realizadas, 1 omitida y 2 pendientes equivale a 83% entre las seis evaluadas, no a 63% entre todas.

## Integración y restricciones

`PROGRESS` y `PROGRESS_SUMMARY` son nuevas rutas del historial de fase 04. Inicio abre Seguimiento; el botón Resumen permite ver la semana. El avance de día o semana solo altera la consulta, nunca el estado persistido. El almacenamiento usa el aislamiento de `:core`, un worker por presentador y callbacks al hilo principal.

No se implementan recompensas, ejecución automática de actividades, recordatorios, multimedia, sincronización ni importaciones. Las pantallas Android son **Java + XML**; la previsualización HTML entregada es un artefacto separado con simulación de datos en navegador.
