# Fase 16.3 — Estrategia provisional de empaquetado y autorización

Esta especificación **no autoriza compilar, firmar, ejecutar workflows o publicar APK**.

## Primera variante recomendada

**Depuración / `debug`** para la primera validación Android real, cuando el propietario lo apruebe por separado.

- ID: `pe.asterionsystem.app`
- `versionCode`: `1` (actual, provisional)
- `versionName`: `0.1.0` (actual, provisional)
- Android `minSdk`: `26`; `compileSdk` y `targetSdk`: `36`.
- Producto: aplicación Android nativa Java/XML, SQLite local v5, sin permisos de red.
- El artefacto debug de Android se firma habitualmente mediante la clave de desarrollo del entorno de compilación. **No se publica esa clave, no se debe interpretar esta firma como la firma definitiva de una release.**

## Variante release (sin aprobar)

- Exige decisión de distribución, versión, política de firma y entorno seguro de gestión de claves.
- No crear una `signingConfig` con credenciales incrustadas en GitHub, documentos o chats.
- Requiere verificar empaquetado, manifiesto fusionado, firma, compatibilidad y pruebas de instalación/actualización.
- No publicar `release` hasta confirmar el diseño final de fase 03 si ese es un requisito del producto.
- La fase 16 actual **no** configura keystore ni firma de release, tampoco genera un APK sin firmar.

## Barrera de autorización

La etapa 16.4 queda explícitamente **detenida** hasta que exista un mensaje separado del propietario que identifique una autorización para compilar una variante concreta. También deberá aclararse si se permite ejecutar tareas Gradle de pruebas y lint, además de la tarea de compilación.

Ejemplo de mensaje futuro, **no autorizado por este documento**:

> Autorizo expresamente una compilación Android debug de Asterion System y las tareas Gradle de prueba/lint necesarias, sin publicar ninguna clave de firma ni activar CI automáticamente.

Si la autorización no está presente, **no ejecutar** comandos `gradle`, `./gradlew`, `assembleDebug`, `assembleRelease`, `testDebugUnitTest`, `lintDebug` ni ninguna ejecución de `workflow_dispatch`.

## Etapas restantes

- **16.4 Compilación:** bloqueada, no ejecutada.
- **16.5 Validación de APK:** bloqueada, no existe APK generada.
- **16.6 Entrega e instalación:** bloqueada, sin artefacto verificable.
- **16.7 Cierre y publicación del paquete:** bloqueada hasta la aprobación y validación del artefacto. Publicar **solo documentación o cambios fuente** durante preparación no equivale a entregar APK.

La generación de un artefacto exige una verificación explícita de su ruta real, tamaño, firma y hash. No se inventarán enlaces de APK.
