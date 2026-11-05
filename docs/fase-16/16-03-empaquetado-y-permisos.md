# Fase 16.3 — Estrategia provisional de empaquetado y autorización

**Actualización, 5 de noviembre de 2026:** el propietario autorizó expresamente una primera compilación **APK debug** con pruebas y lint Gradle. La autorización **no comprende** release ni firma de distribución. La compilación está pendiente por impedimentos técnicos de este entorno.

## Primera variante recomendada

**Depuración / `debug`** para la primera validación Android real, aprobada para una primera compilación, todavía no ejecutada.

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

**Esa aprobación adicional ya se recibió para la variante `debug` y para pruebas/lint de Gradle.** El impedimento actual es técnico: este entorno no dispone de Gradle ni Android SDK y la conexión de GitHub disponible no incorpora el comando de iniciar workflow manual. No es necesario solicitar de nuevo la autorización; hace falta ejecutar el workflow desde una interfaz habilitada.

Ejemplo de mensaje futuro, **no autorizado por este documento**:

> Autorizo expresamente una compilación Android debug de Asterion System y las tareas Gradle de prueba/lint necesarias, sin publicar ninguna clave de firma ni activar CI automáticamente.

Para cualquier variante o fase futura sin autorización, **no ejecutar** comandos `gradle`, `./gradlew`, `assembleDebug`, `assembleRelease`, `testDebugUnitTest`, `lintDebug` ni ninguna ejecución de `workflow_dispatch`.

## Etapas restantes

- **16.4 Compilación:** autorizada para debug, pero técnicamente no ejecutada.
- **16.5 Validación de APK:** pendiente de que la ejecución genere una APK real.
- **16.6 Entrega e instalación:** bloqueada, sin artefacto verificable.
- **16.7 Cierre y publicación del paquete:** bloqueada hasta la aprobación y validación del artefacto. Publicar **solo documentación o cambios fuente** durante preparación no equivale a entregar APK.

La generación de un artefacto exige una verificación explícita de su ruta real, tamaño, firma y hash. No se inventarán enlaces de APK.
