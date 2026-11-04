# Asterion System — Android nativo

**Estado:** fase 01 publicada en GitHub, compilación y pruebas de CI pendientes de verificación. Esta es una base técnica, no una aplicación funcional completa.

## Objetivo

Establecer un proyecto Android **nativo en Java**, sin WebView y sin lógica basada en JavaScript. La fase 01 incluye exclusivamente la estructura de compilación, una actividad inicial de verificación, una prueba JVM mínima, documentación y GitHub Actions.

No implementa motores de reglas, almacenamiento persistente, navegación funcional, multimedia, notificaciones, automatizaciones ni integraciones.

## Herramientas acordadas

| Herramienta | Versión/configuración |
|---|---|
| Java | JDK 17 para compilación |
| Android Gradle Plugin | 8.13.2 |
| Gradle | 8.13 |
| Android compileSdk / targetSdk | 36 / 36 |
| minSdk | 26 (Android 8.0) |
| Identificador provisional | `pe.asterionsystem.app` |
| Primera versión | `0.1.0` (`versionCode=1`) |

Las versiones se fijan para evitar actualizaciones implícitas.

## Preparación local

1. Instala Android Studio con JDK 17 (o selecciónalo para Gradle), Android SDK Platform 36 y Build Tools 35.0.0.
2. Abre la carpeta del proyecto en Android Studio.
3. **Genera el Gradle Wrapper oficial antes de compilar localmente.** El repositorio todavía no incluye `gradle-wrapper.jar` ni los scripts `gradlew` porque no se verificó un origen binario para esos archivos. Desde un entorno con Gradle 8.13 ejecuta:

   ```bash
   gradle wrapper --gradle-version 8.13 --distribution-type bin
   ```

   Después podrás ejecutar:

   ```bash
   ./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
   ```

   El workflow de GitHub Actions instala Gradle 8.13 directamente y no depende del wrapper.

4. Si se compila correctamente, la APK debug se encontrará en `app/build/outputs/apk/debug/app-debug.apk`.

## Estado

- [x] Proyecto Gradle para Java y Android.
- [x] Actividad inicial y recursos XML neutros.
- [x] Prueba unitaria mínima.
- [x] Workflow de compilación, lint, pruebas y firma debug.
- [x] Documentación de fase 01.
- [x] Repositorio público inicializado.
- [ ] Confirmar resultados de GitHub Actions.
- [ ] Confirmar APK compilada y firmada de depuración.
- [ ] Generar y versionar el Gradle Wrapper oficial (recomendado).
- [ ] Corregir nombre del repositorio: se creó con un punto final accidental (`Asterion-System-Android.`).

**No se afirma que una APK exista hasta que el workflow finalice correctamente.**

## Seguridad y propiedad intelectual

No se incorpora código ni recursos de Hunter System. La documentación de fase 00 no se publica automáticamente. No subas contraseñas, claves de firma, tokens ni datos personales.

No se ha elegido licencia de código abierto. Un repositorio público no concede por sí solo permisos de reutilización.

## Documentación

- [Alcance](docs/fase-01/01-plan-y-alcance.md)
- [Decisiones técnicas](docs/fase-01/01-decisiones-tecnicas.md)
- [Control de calidad](docs/fase-01/01-validacion.md)
- [Publicación GitHub](docs/fase-01/01-publicacion-github.md)

Próxima fase autorizable tras cerrar la fase 01: **02 — arquitectura interna**.
