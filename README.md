# Asterion System — Android nativo Java

**Estado:** implementadas fases 00, 01, 02, 04, 05, 06, 07 y **08 (planificación y programación)**. La fase 03 (diseño definitivo) permanece pendiente.

Proyecto Java nativo Android con vistas XML. El HTML se utiliza **solo como previsualización externa**. No se han incorporado HTML, JavaScript, CSS, WebView o Compose a las pantallas Android.

## Arquitectura

- `:core`: Java puro; dominio, eventos, estado, navegación, reglas, catálogo y planificación.
- `:app`: `MainActivity`, adaptadores Android, pantallas XML, SQLiteOpenHelper y repositorios SQLite.
- SQLite `asterion_internal.db`: esquema v4 con migración adicional 3→4, no destructiva.
- `docs/fase-08/`: especificaciones y pruebas de planificación.

## Fase 08

El planificador permite programar **actividades del catálogo** con fecha civil local, hora inicial y duración de 1–720 minutos sin cruzar medianoche. Un servicio Java y cuatro triggers SQLite previenen entradas superpuestas, incluidos conflictos durante escrituras concurrentes. Las pantallas `screen_plan.xml` y `screen_plan_form.xml` permiten consultar la semana, seleccionar días, crear, editar y eliminar programaciones. Ninguna programación ejecuta automáticamente una actividad ni produce recordatorios.

La previsualización HTML (entregada fuera de la app) simula ese comportamiento con almacenamiento del navegador; no equivale a la ejecución Android.

## Sin compilar APK

**No se compila ni genera APK sin autorización expresa.** `.github/workflows/android-ci.yml` conserva solamente `workflow_dispatch`. Las publicaciones en GitHub no activan trabajos automáticos de compilación.

La lógica SQL se probó localmente con SQLite de escritorio. La previsualización se verificó mediante Chromium y las pruebas Java JUnit permanecen preparadas, **sin ejecutarse por Gradle**. La compilación y ejecución Android siguen pendientes.

## Documentación

- [Arquitectura de planificación](docs/fase-08/08-arquitectura.md)
- [Esquema y migración](docs/fase-08/08-esquema-y-migracion.md)
- [Validación y limitaciones](docs/fase-08/08-validacion.md)
- [Trazabilidad](docs/fase-08/08-trazabilidad.md)

## Fases

Fases 03, 09, 11 y 14 pendientes de aprobación. Fases 15 y 16 están incluidas en el plan maestro para validación, optimización y publicación futura, sujetas a alcance y autorización. Fases 10, 12 y 13 excluidas.

El repositorio es público pero no se ha seleccionado licencia de reutilización; no incorporar contraseñas, certificados o datos sensibles.
