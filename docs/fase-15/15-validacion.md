# Fase 15 — Validaciones realizadas y pruebas pendientes

**Política:** ninguna prueba de fase 15 puede ejecutar Gradle, compilar Android, generar APK ni activar GitHub Actions. Los archivos de test Java se añaden como fuente, sin ejecutarse en Android.

## Ejecuciones independientes realizadas

### 1. HTML / Chromium

La previsualización HTML externa se verificó con `node --check` sobre el script embebido y un navegador Chromium de escritorio (Playwright, `page.set_content`). **36 comprobaciones** superadas, sin errores JavaScript: navegación, operación sin internet simulada, búsqueda agrupada de 280 ms, cambio de categoría, cálculo de lotes 200/201/450, calendarios semanales, selección accesible, seguimiento y porcentaje, preferencias de texto/densidad, sección inicial, diagnóstico simulado, espacio insuficiente, claves foráneas simuladas, reintento manual y preservación de datos de prueba.

El HTML es **un simulador**: los contadores de consultas, la red y el estado SQLite no provienen de Android. Sus datos de ejemplo no representan información real del usuario.

### 2. SQLite de escritorio

Un runner independiente de Python `sqlite3` ejecutó **5 tests** de consultas por lotes equivalentes: comparación con consulta individual para 450 entradas, entrada vacía, ID ausente, caracteres SQL potencialmente problemáticos y límites 1/199/200/201/400/450. Todos pasaron. El script reproducible `tests/phase15_batched_titles_sqlite_smoke.py` contiene los mismos casos. **No** se compiló ni ejecutó la clase `SQLiteCatalogRepository` de Android.

### 3. Validaciones estáticas

Se revisaron sin Android SDK las referencias Java/XML, los nuevos recursos de accesibilidad, el contrato de títulos por lotes, el control de generación de búsquedas y el esquema SQLite existente. Se añadió `tests/phase15_source_contract.py` para repetir estas comprobaciones tras un checkout del repositorio.

## Pendientes (no ejecutar sin una aprobación independiente)

- Compilación de Android con Gradle/AGP y verificación de recursos aapt.
- JUnit oficial de core, incluyendo `SchedulePlannerBulkTitlesTest`.
- Tests instrumentados, persistencia SQLite Android, estado del SDK y migraciones reales en dispositivo.
- Medición de rendimiento de búsquedas y lotes con perfilador y conjuntos de datos representativos.
- Validación de accesibilidad con TalkBack, fuente grande y diversas pantallas.
- Pruebas de seguridad dinámicas, análisis de dependencias y auditoría de despliegue.
- Firma, empaquetado, distribución y **generación de APK** (fase 16, no autorizada).

Ninguna inferencia a partir del navegador o SQLite de escritorio se presenta como prueba de Android real.

## Verificación de restricciones

- HTML, CSS y JS de demostración **fuera del repositorio Android**, suministrados como archivo independiente.
- `AndroidManifest.xml`: sin permisos de internet.
- `AsterionDatabaseHelper.DATABASE_VERSION=5`; no hay una migración nueva.
- Workflow `android-ci.yml` permanece con `workflow_dispatch` únicamente.
- Se conservaron catálogo, planificador, seguimiento, configuración y diagnósticos locales.
