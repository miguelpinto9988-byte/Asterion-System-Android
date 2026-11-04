# Asterion System — Android nativo (Java + XML)

**Fases implementadas:** 00 (análisis), 01 (proyecto base), 02 (arquitectura), 04 (navegación), 05 (SQLite), 06 (motor de reglas) y **07 (catálogo de contenidos y actividades)**. La fase 03 de diseño visual definitivo sigue pendiente de autorización.

## Arquitectura

- **`:core`** — Java puro: dominio, eventos, navegación, estado, contratos de datos, motor de reglas y catálogo.
- **`:app`** — Android Java con pantallas XML nativas, adaptadores Android, SQLite y presentadores de UI.
- **`docs/fase-0*`** — documentación y criterios de aceptación de cada fase.
- **`tests/`** — pruebas SQLite de escritorio, independientes de Android.

El proyecto es original e independiente de Agenda Clara; no copia código ni activos de Hunter System.

## Fase 07: catálogo

Las pantallas `screen_catalog.xml` y `screen_catalog_detail.xml` permiten consultar cuatro fichas técnicas de demostración clasificadas en tres categorías, con etiquetas, búsqueda y filtros. `screen_home.xml` incluye un botón de acceso. `MainActivity` preserva filtros y selección temporal en `Bundle`.

La infraestructura SQLite migra a **esquema v3**, añadiendo `catalog_categories`, `catalog_items`, `catalog_tags` y `catalog_item_tags` sin eliminar `technical_records`. Se emplean consultas parametrizadas y un worker Java para evitar trabajar en el hilo principal Android.

**Las fichas son datos de prueba, no contenidos definitivos.** Todavía no existen programación, seguimiento, multimedia ni edición de catálogo.

## Previsualización

La revisión del diseño y comportamiento se realiza mediante el HTML independiente `Asterion-System_Fase-07_Previsualizacion_Interactiva.html` entregado en la conversación. Incluye catálogo, filtros, detalle y simuladores técnicos de las fases 05 y 06. El HTML **no** se incorpora a la APK ni se conecta a SQLite Android. Las pantallas reales se mantienen exclusivamente en XML y Java.

## Prohibición de compilación APK

No ejecutar `assembleDebug`, `assembleRelease` ni producir ninguna APK sin autorización expresa. El workflow `.github/workflows/android-ci.yml` permanece limitado a `workflow_dispatch`; publicar commits no inicia compilaciones automáticas.

## Tecnologías

Java 17, Android Views XML, Android Gradle Plugin 8.13.2, Gradle 8.13; `compileSdk=36`, `targetSdk=36`, `minSdk=26`. Sin WebView, JavaScript ni Compose como interfaz de la aplicación.

## Referencias

- [Arquitectura del catálogo](docs/fase-07/07-arquitectura-catalogo.md)
- [SQLite v3 y migración](docs/fase-07/07-esquema-y-migracion.md)
- [Validación y límites](docs/fase-07/07-validacion.md)
- [Trazabilidad](docs/fase-07/07-trazabilidad.md)

## Próximas decisiones

Fases 08, 09, 11 y 14 siguen pendientes de aprobación, igual que la fase 03. La siguiente fase por orden es **08 (planificación y programación)**, no autorizada todavía. Las fases 10, 12 y 13 siguen excluidas.

No hay licencia de código abierto seleccionada. No almacenes ni publiques credenciales y claves de firma.
