# Asterion System — Android nativo Java

**Estado:** fases 00 (documentación), 01 (proyecto base) y 02 (arquitectura interna). La fase 02 no genera APK ni nuevas interfaces de usuario.

Repositorio público del proyecto independiente de Agenda Clara. La arquitectura se inspira en los principios funcionales estudiados de Hunter System, pero el código aquí es original.

## Arquitectura disponible

- **`:app`**: shell Android en Java, `MainActivity`, adaptador de ciclo de vida y coordinador. Pantalla existente en `app/src/main/res/layout/activity_main.xml`.
- **`:core`**: módulo Java puro sin dependencias Android, con contratos para casos de uso, motor de reglas, datos y reloj, y utilidades estructurales de eventos, identificadores, estado y ciclo de vida.
- **Documentación**: `docs/fase-01/` y `docs/fase-02/`.

## Tecnologías

Java 17 para código fuente; Android nativo con vistas XML; Android Gradle Plugin 8.13.2, Gradle 8.13, `compileSdk/targetSdk 36` y `minSdk 26`. El proyecto no usa WebView, HTML, CSS, JavaScript ni Compose como interfaz.

## Política de compilación

**No compilar ninguna APK sin autorización explícita del propietario.** El único workflow Android, `.github/workflows/android-ci.yml`, tiene activación `workflow_dispatch` y no responde a `push` ni a `pull_request`. Publicar código **no** dispara una compilación Android.

En fase 02 solo se efectuó una verificación local de Java puro mediante `javac --release 17` y una prueba rápida de sus contratos; no se utilizó Gradle ni se generó APK. Las clases de prueba JUnit del proyecto están preparadas, pero todavía no se han ejecutado mediante Gradle.

El Gradle Wrapper oficial sigue pendiente de incorporación. Los scripts Gradle están listos para abrir el proyecto en Android Studio sin ejecutar ninguna compilación hasta que la solicites.

## Documentación de fase 02

- [Arquitectura y política de eventos](docs/fase-02/02-arquitectura.md)
- [Contratos Java](docs/fase-02/02-contratos.md)
- [Verificaciones y limitaciones](docs/fase-02/02-validacion.md)
- [Trazabilidad y riesgos](docs/fase-02/02-fases-y-riesgos.md)

## Límites de fases

En esta etapa **no** se desarrolla la interfaz definitiva (fase 03 pendiente), navegación/restauración (04), SQLite/Room (05), reglas funcionales (06), ni contenidos, planificación, seguimiento, personalización o sincronización (07/08/09/11/14 pendientes). Los sistemas multimedia (10), notificaciones (12) e integraciones (13) están excluidos.

La previsualización visual se realiza sobre archivos XML de Android, no mediante HTML. Los recursos XML existentes no han cambiado en fase 02.

No se ha elegido una licencia de código abierto. Los secretos y certificados de firma no deben incorporarse al repositorio público.
