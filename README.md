# Asterion System — Android nativo Java

**Estado:** fases 00 (documentación), 01 (proyecto base), 02 (arquitectura), 04 (navegación) y **05 (infraestructura de datos SQLite)**. La fase 03 de diseño visual definitivo sigue pendiente.

Proyecto independiente de Agenda Clara, con código propio basado en principios de arquitectura modular. El núcleo usa Java puro; las interfaces Android usan Java y XML nativos.

## Módulos

- **`:core`** — Java sin Android: eventos, estado, navegación, contratos de motores y datos, `TechnicalRecord`, contrato CRUD, worker asíncrono.
- **`:app`** — Android Java: pantalla inicial, navegación entre dos pantallas XML, `SQLiteOpenHelper`, migraciones y adaptador SQLite.
- **`docs/fase-0*`** — documentación y trazabilidad.
- **`tests/phase05_sqlite_smoke.py`** — validación del SQL definido en Java mediante SQLite de Python, sin SDK ni APK.

## Fase 05: almacenamiento

La base técnica `asterion_internal.db`, esquema v2, contiene únicamente la tabla `technical_records`: ID, título, nota y timestamps. Implementa CRUD, transacciones, índices, validaciones y migración no destructiva v1→v2. El repositorio SQLite impide consultas en el hilo principal; las operaciones se pueden enviar a un worker mediante `BackgroundRecordOperations`.

**Importante:** el almacenamiento todavía no está conectado a las pantallas Android; hacerlo necesitará una fase y alcance aprobados. El laboratorio de datos en HTML, entregado por separado, es una simulación con almacenamiento de navegador, no SQLite de Android.

## Pantallas y previsualización

Las pantallas Android siguen en `app/src/main/res/layout/screen_home.xml` y `screen_secondary.xml`. En Android Studio se visualizan con **Design** o **Split**. La previsualización HTML interactiva es exclusivamente una herramienta externa, no forma parte de la aplicación.

## Política de compilación

**Prohibido compilar/generar APK sin autorización expresa.** `.github/workflows/android-ci.yml` mantiene exclusivamente `workflow_dispatch`; los commits no generan APK. No se ejecutará Gradle ni `assembleDebug` en fase 05.

## Configuración

Java 17, Android XML Views, Android Gradle Plugin 8.13.2, Gradle 8.13, compile/target SDK 36 y mínimo SDK 26. El repositorio no integra WebView, HTML, CSS, JavaScript ni Compose en el código de la aplicación.

## Documentación

- [Fase 04 — Navegación](docs/fase-04/04-arquitectura-y-pantallas.md)
- [Fase 05 — Arquitectura de almacenamiento](docs/fase-05/05-arquitectura-almacenamiento.md)
- [Fase 05 — Esquema y migraciones](docs/fase-05/05-esquema-y-migraciones.md)
- [Fase 05 — Validación y límites](docs/fase-05/05-validacion.md)
- [Fase 05 — Trazabilidad](docs/fase-05/05-trazabilidad.md)

## Fases futuras

La siguiente fase incluida es **06 — Motor central de reglas** y requiere autorización individual. Fase 03 sigue pendiente. Fases 07, 08, 09, 11 y 14 pendientes; fases 10, 12 y 13 excluidas.

El repositorio es público, pero no se ha seleccionado una licencia de código abierto. No publiques credenciales, certificados o datos sensibles.
