# Fase 14 — Validación y pendientes

## Verificaciones realizadas

- Auditoría de `AndroidManifest.xml`, SQLite v5, `NonDestructiveDatabaseErrorHandler`, puertos, workers y preferencias: no se introducen servicios externos, permisos de red, HTML dentro de Android ni migraciones destructivas.
- Comprobación estática de rutas de navegación y controles XML para diagnóstico y reintentos manuales de seis pantallas.
- Previsualización HTML v14: `node --check` sin errores sintácticos.
- Chromium + Playwright (HTML cargado con `page.set_content` por restricción de navegación de la plataforma): **25 comprobaciones** superadas. Incluyen diagnóstico normal, `quick_check` y claves foráneas **simuladas**, fallo por espacio, fallo de acceso, advertencia, reintento de consulta desde Catálogo, recuperación tras resolver un fallo simulado, navegación y conservación de registros de prueba. No se registraron errores JavaScript.
- **Prueba JVM independiente ejecutada:** `javac --release 17` compiló `LocalStorageReport`, `LocalStorageCheck`, `LocalStorageService`, `BackgroundLocalStorageChecks` y `LocalStorageSmoke`; la ejecución finalizó con `PASS local storage checks: 13`. Se verificó que los SHA de Git de esos cinco archivos locales coinciden exactamente con los blobs GitHub de la entrega. No requiere SDK Android ni Gradle.
- **Prueba SQLite equivalente ejecutada:** Python `sqlite3` local superó **6 pruebas** de `quick_check`, `foreign_key_check`, restricciones de horarios, reversión transaccional, reintento manual y persistencia tras cerrar/reabrir el archivo. Es un esquema equivalente reproducido fuera de Android, **no** la ejecución del script que extrae constantes Java del checkout.
- Se prepararon pruebas JUnit `LocalStorageServiceTest`, ejecutor reproducible `tests/phase14_core_jvm_smoke.sh`, prueba SQL sobre constantes Java `tests/phase14_sqlite_integrity_smoke.py` y verificador estático `tests/phase14_offline_static_smoke.py`. Estos últimos scripts requieren un checkout completo y permanecen pendientes de ejecución allí.

## Pruebas pendientes

- Validación del SDK y dispositivo real Android: **no ejecutada**.
- Gradle, JUnit oficial, pruebas instrumentadas, compilación y generación APK: **no ejecutadas**.
- Pruebas reproducibles del esquema con el script de GitHub: preparadas, pendientes de ejecutar sobre un checkout completo. Los **6 ensayos SQLite equivalentes de escritorio** no sustituyen `SQLiteOpenHelper` Android.
- Fallas físicas reales, sincronización de procesos y tiempos de apertura en dispositivos económicos.

## Limitaciones y afirmaciones precisas

La aplicación no solicita permiso de internet y sus funciones aprobadas utilizan SQLite/SharedPreferences. La simulación de conectividad en HTML **no desactiva realmente la red** y no verifica la integridad de una base SQLite real. Los fallos simulados tampoco dañan registros, que permanecen en el navegador. El trabajo implementa **diagnóstico y reintento manual**, no recuperación automática de bases corruptas.

La base SQLite continúa en versión 5 y conserva las tablas anteriores. El workflow `.github/workflows/android-ci.yml` sigue manual mediante `workflow_dispatch`. Este trabajo no autoriza ejecutarlo.

## Recomendaciones no implementadas

1. Ensayar `SQLiteOpenHelper` y transacciones bajo corte abrupto en un Android real.
2. Comparar `quick_check` y `integrity_check` completo bajo mantenimiento explícito y con consentimiento.
3. Diseñar estrategia de respaldo local cifrado solo si se aprueba una futura fase de exportación/backup (hoy excluida).
