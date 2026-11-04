# Asterion System — Android nativo Java

**Estado:** fases 00 (documentación), 01 (proyecto base), 02 (arquitectura interna) y 04 (navegación y estado temporal) implementadas. La fase 03 de diseño visual definitivo sigue pendiente.

Repositorio público de una aplicación nueva, independiente de Agenda Clara, que adopta principios arquitectónicos estudiados de Hunter System con código original.

## Módulos existentes

- **`:core`** — Java puro: contratos de dominio, motores, eventos, estado, ciclo de vida y navegación inmutable. No depende del SDK de Android.
- **`:app`** — Android Java: `MainActivity`, coordinador de ciclo de vida, adaptador de `Bundle` y renderizador de pantallas XML nativas.
- **`docs/`** — registros de fases 01, 02 y 04.

## Pantallas XML actuales

Los diseños provisionales son `app/src/main/res/layout/screen_home.xml` y `screen_secondary.xml`. Se visualizan con **Android Studio → Design** o **Split** abriendo cada archivo. `activity_main.xml` define el contenedor de navegación; por sí mismo no representa el contenido de las dos pantallas.

El código Java únicamente elige qué XML inflar y conecta los eventos de pulsación. **No** se utiliza HTML, CSS, JavaScript, WebView ni Jetpack Compose.

## Estado de navegación

`NavigationController` mantiene la ruta activa y el historial en `StateStore<NavigationState>`. Permite ir a la pantalla secundaria y retroceder, evita destinos duplicados y publica `NavigationChangedEvent` al cambiar el estado. El estado temporal puede restaurarse al recrear la Activity mediante `onSaveInstanceState`.

No se almacena información en una base de datos todavía y la aplicación no contiene funcionalidades de Hunter.

## Política estricta de compilación

**No compilar ni generar APK sin autorización expresa del propietario.** El workflow `.github/workflows/android-ci.yml` está configurado exclusivamente con `workflow_dispatch`; publicar cambios no activa compilación automática.

La fase 04 se comprobó con `javac --release 17`, pruebas de lógica JVM y validación XML. **No** se ha compilado el proyecto Android ni ejecutado Gradle en esta fase; la validez real en dispositivo queda pendiente de autorización.

## Tecnologías

Java 17; Android XML Views; Android Gradle Plugin 8.13.2; Gradle 8.13; `compileSdk/targetSdk=36`; `minSdk=26`.

## Documentación

- [Fase 02 — Arquitectura](docs/fase-02/02-arquitectura.md)
- [Fase 04 — Arquitectura y XML](docs/fase-04/04-arquitectura-y-pantallas.md)
- [Fase 04 — Validación](docs/fase-04/04-validacion-y-limitaciones.md)
- [Fase 04 — Trazabilidad](docs/fase-04/04-trazabilidad.md)

## Fases futuras

La siguiente fase incluida en el plan es **05 — Base de datos y almacenamiento**, pendiente de autorización individual. La fase 03 sigue pendiente de decisión; las fases 10, 12 y 13 están excluidas.

No hay licencia de código abierto elegida. Los secretos y certificados de firma no deben incorporarse al repositorio público.
