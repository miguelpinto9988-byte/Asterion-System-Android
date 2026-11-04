# Publicación de Asterion System en GitHub

**Cuenta conectada:** `miguelpinto9988-byte`
**Nombre acordado:** `Asterion-System-Android`
**Visibilidad:** público

## Repositorio existente

Se detectó un punto final añadido al nombre real: `Asterion-System-Android.`. El repositorio ya fue inicializado con los archivos de fase 01.

### Corrección del nombre

1. Abre el repositorio en GitHub.
2. Entra en **Settings → General → Repository name**.
3. Sustituye `Asterion-System-Android.` por `Asterion-System-Android` (sin el punto final).
4. Confirma **Rename**.

Enlace definitivo esperado: https://github.com/miguelpinto9988-byte/Asterion-System-Android

## Revisión de la compilación

1. Abre la pestaña **Actions**.
2. Entra en el workflow **Android — fase 01**.
3. Revisa el job de pruebas JVM, lint y compilación.
4. Exige resultado verde en `testDebugUnitTest`, `lintDebug`, `assembleDebug` y `apksigner verify`.
5. Descarga `asterion-phase01-debug-apk` solo cuando exista un artefacto validado.
6. Documenta el commit, el resultado y cualquier corrección requerida.

El workflow tiene permisos de lectura sobre el repositorio y genera un APK debug, no una versión de producción.

No introduzcas contraseñas, claves privadas ni tokens en el repositorio público.
