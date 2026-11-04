# Asterion System — Fase 16.1, auditoría previa a la compilación

**Fecha de revisión:** 2026-11-04. **Alcance:** inspección de código y configuración, sin ejecutar Gradle, Android Studio Build, JUnit Android, GitHub Actions o APK.

## Configuración encontrada en main

| Elemento | Valor declarado | Evaluación |
|---|---|---|
| Proyecto | `Asterion-System-Android` | Incluye `:app` y `:core` |
| Android Gradle Plugin | 8.13.2 | Compatible sobre el papel con Gradle 8.13 |
| Gradle | 8.13 en workflow manual | No existe Gradle Wrapper versionado en el repositorio |
| JDK | 17 en CI y Java toolchain de core | Requiere instalación disponible al compilar |
| compileSdk / targetSdk | 36 / 36 | Se requieren Android SDK platform 36 y soporte de interfaz edge-to-edge |
| minSdk | 26 | `java.time` está disponible desde Android API 26 |
| Build Tools (workflow) | 35.0.0 | Es el mínimo predeterminado para AGP 8.13 |
| Android namespace / applicationId | `pe.asterionsystem.app` | Id del paquete sin cambiar |
| Versión | `versionCode=1`, `versionName=0.1.0` | Valor provisional, sin publicar |
| SQLite | `asterion_internal.db` versión 5 | Migraciones no destructivas anteriores preservadas |
| Interfaz | Java + XML nativo | Ningún WebView/Compose/HTML en `:app` |
| CI | `workflow_dispatch` únicamente | No se autorizó activar ningún workflow |
| Firma release | No configurada | Bloquea distribuir un APK release instalable sin una decisión posterior |

Fuentes oficiales de compatibilidad:
- https://developer.android.com/build/releases/agp-8-13-0-release-notes
- https://developer.android.com/build/releases/about-agp
- https://developer.android.com/about/versions/16/behavior-changes-16

## Hallazgos

**H01 — Ajuste de borde a borde (corrección aplicada en preparación).** Android 16 (target 36) ya no permite desactivar edge-to-edge. El contenedor inicial dependía de `fitsSystemWindows`; se preparó un manejo explícito de `WindowInsets.Type.systemBars()` y `displayCutout()` para dispositivos API 35+, sin aplicar dos veces las áreas de seguridad. **Requiere validación real de comportamiento y compilación Android antes de considerarse probado.**

**H02 — Falta de Gradle Wrapper (pendiente, no bloquea CI configurado).** No están versionados `gradlew`, `gradlew.bat` ni `gradle/wrapper/gradle-wrapper.jar`. La acción manual instala Gradle 8.13 desde `gradle/actions/setup-gradle@v4`, por lo que el workflow no necesita el wrapper. Una compilación local futura debe preparar Gradle 8.13 externamente o añadir un wrapper completo y verificado mediante aprobación específica. No se generaron scripts de wrapper incompletos.

**H03 — Firma release pendiente.** `app/build.gradle` tiene `debug` y `release`, pero ninguna configuración privada de firma de distribución. `.gitignore` excluye `*.jks`, `*.keystore`, `*.p12`, `*.pem`, `*.key`, APK/AAB y secretos. No solicitar, registrar ni publicar claves privadas.

**H04 — Etiquetas heredadas del workflow.** CI conserva nombres de la fase 01 y publica `asterion-phase01-debug-apk`. Son metadatos antiguos que convendrá actualizar **solo cuando se apruebe la etapa de empaquetado**; se mantiene su comportamiento manual inalterado.

**H05 — Cobertura de Android real no comprobada.** Los tests Python, JVM puro y el simulador HTML son útiles pero no prueban `aapt`, Android Gradle Plugin, `SQLiteOpenHelper`, Android 16 edge-to-edge, instalación, permisos ni dispositivos reales.

**H06 — Identidad visual.** La fase 03 de diseño definitivo no está aprobada. La preparación no altera colores, tipografía, iconos ni arquitectura visual del producto.

## Política de actuación

Se permite corregir código fuente o configuración **sin ejecutarlo en Android** cuando exista motivo documentado; la compilación, firma, prueba instrumental o generación de APK necesitan autorización nueva y expresa. No ejecutar `gradle`, `./gradlew`, `assemble*`, `testDebugUnitTest` ni workflow de GitHub Actions durante esta preparación.
