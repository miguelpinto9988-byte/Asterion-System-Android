# Fase 01 — Decisiones técnicas

## Elecciones acordadas

1. **Java:** código principal Android sin Kotlin ni JavaScript para el arranque.
2. **Android Views/XML:** pantalla estática neutra, sin anticipar el diseño de la fase 03.
3. **Android Gradle Plugin 8.13.2, Gradle 8.13 y JDK 17:** versiones fijadas y sujetas a comprobación en CI.
4. **SDK:** `compileSdk=36`, `targetSdk=36`, `minSdk=26`. Validar comportamientos edge-to-edge y compatibilidad antes de distribución real.
5. **Identificador provisional:** `pe.asterionsystem.app`; decidir antes de publicar en producción.
6. **Sin permisos Android sensibles:** el arranque no solicita red, contactos, fotos, ubicación ni notificaciones.
7. **Sin dependencias adicionales de terceros:** JUnit se utiliza solo para pruebas JVM.
8. **Sin licencia elegida:** el repositorio público no concede automáticamente licencia de reutilización.

## Gradle Wrapper pendiente

Se incluyen scripts Gradle, pero no `gradlew`, `gradlew.bat` ni `gradle-wrapper.jar`. Evitamos introducir binarios no verificados. GitHub Actions instala Gradle 8.13 mediante una acción reconocida.

Para uso local reproducible, generar el wrapper oficial con Gradle 8.13 y validar el origen de sus archivos.

## Decisiones posteriores

- Fase 02: responsabilidades y contratos de la arquitectura modular.
- Fase 05: almacenamiento real.
- Fase 06: implementación del motor de reglas.
- Fase 03: diseño visual definitivo, aún pendiente.

## Fuentes técnicas

- https://developer.android.com/build/releases/gradle-plugin
- https://developer.android.com/build/releases/about-agp
- https://developer.android.com/build/jdks
- https://docs.gradle.org/current/userguide/gradle_wrapper.html
