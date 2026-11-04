# Fase 11 — Control de calidad y limitaciones

## Pruebas y verificaciones de esta fase (2026-11-04)

1. Previsualización HTML autocontenida: JavaScript validado con `node --check`, sin dependencias JavaScript externas.
2. Chromium y Playwright: navegación a Configuración, presencia de 2 opciones de texto, 2 de densidad y 4 de inicio; cambio de opciones; efecto visual CSS; persistencia de preferencias usando almacenamiento simulado; recreación que conserva la ruta; arranque en sección preferida; reinicio de opciones sin borrar las programaciones de prueba; vuelta a valores predeterminados; verificación de que el catálogo anterior continúa disponible. No se detectaron errores JavaScript en la sesión de prueba.
3. **Verificación JVM real sin APK:** compilación local de las tres fuentes exactas de `:core/settings` con `javac --release 17`, seguida de 18 aserciones de `PreferencesSmoke` sobre defaults, normalización de valores desconocidos, cambios, inmutabilidad, ausencia de escrituras redundantes y restablecimiento. Resultado: `PASS Java 17 core settings: 18 checks`. Los hashes Git de las fuentes y del runner local coinciden con los blobs preparados para GitHub. Repetible con `sh tests/phase11_jvm_smoke.sh`.
4. Se incluyeron tests JUnit (`PreferencesServiceTest`) para validación de defaults, enums desconocidos, inmutabilidad, escrituras sin cambio y restauración de valores.
5. `tests/phase11_preferences_static_smoke.py` comprueba los contratos, referencias Java/XML y el workflow de CI sin compilar ni generar APK.

## Límites declarados

- No se ejecutó Gradle, `assembleDebug`, pruebas Android instrumentadas ni compilación de APK. La validez real de las vistas Android, compatibilidad y persistencia en dispositivo requieren autorización posterior.
- Los tests JUnit están **preparados en el repositorio**; no deben describirse como una ejecución oficial de Android Gradle.
- La prueba del navegador empleó una implementación en memoria de `localStorage` por restricciones del entorno automatizado, suficiente para verificar interacciones del HTML, **no** la persistencia de SharedPreferences Android.
- El multiplicador 1,18 de texto y el espaciado de filas deben inspeccionarse con Android Studio/SDK y dispositivos reales. La fase 03 de identidad visual definitiva permanece pendiente.
- Se personalizan pantallas XML y listas principales; los diálogos de selección ya existentes no se modificaron de forma completa.
- La sección inicial preferida es una opción de apertura de la aplicación, no una automatización de actividades, ni una notificación.

## Restricciones respetadas

- La interfaz de producción continúa hecha exclusivamente con **Java y XML Android**; la previsualización HTML queda fuera de `:app`.
- No se añadieron claves, cuentas, servidores, permisos de red, exportación/importación ni funciones de fases posteriores.
- `AsterionDatabaseHelper` conserva el esquema v5 y las tablas existentes.
- El workflow `.github/workflows/android-ci.yml` sigue restringido a `workflow_dispatch`; no se inicia ninguna compilación automática.
