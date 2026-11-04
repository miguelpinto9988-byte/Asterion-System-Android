# Fase 07 — Pruebas y limitaciones

## Comprobaciones sin APK

1. **GitHub:** revisión del árbol y de las referencias de recursos XML/Java. Se verificó la existencia de `R.id`, `R.string` y `R.array` utilizados por los controladores nuevos: no se encontraron referencias ausentes.
2. **Arquitectura:** `:core/catalog` no importa clases `android.*`; el repositorio SQLite existe únicamente dentro de `:app`.
3. **Navegación:** el sistema de rutas incluye `CATALOG` y `CATALOG_DETAIL`; las pantallas XML se inflan desde `NavigationRenderer`.
4. **Migración:** revisión de la cadena 1→2→3, tablas v3, claves foráneas y sentencias parametrizadas.
5. **HTML:** sintaxis JavaScript verificada con `node --check`; IDs HTML únicos; **20 interacciones** ejecutadas en Chromium headless mediante Playwright, incluidas búsqueda, filtros combinados, detalle, Atrás, recreación y laboratorios anteriores. No se observaron errores JavaScript.

6. **SQLite de escritorio:** diez comprobaciones con copias verificadas de las sentencias SQL declaradas en Java: conservación del registro v1/v2, migración v3, filtros, etiquetas, claves foráneas, restricciones e índices. Las pruebas reproducibles del repositorio quedan preparadas para ejecución posterior en un checkout; esta prueba local no ejecutó el helper Android.

## Pruebas preparadas, no ejecutadas en Android

- `CatalogModelTest`: validaciones, inmutabilidad de etiquetas y filtros de paginación.
- `BackgroundCatalogOperationsTest`: ejecución secuencial en trabajador independiente.
- `CatalogNavigationTest`: historial, rutas y vuelta al inicio.
- `phase07_catalog_sqlite_smoke.py`: migración e integridad en SQLite local.

**No se ejecutó Android Gradle, JUnit oficial, compilación de APK ni validación en dispositivo.** El HTML es una simulación y no constituye prueba de SQLite Android.

## Riesgos que quedan pendientes de pruebas Android

- Compilación real de `CatalogPresenter`, uso de `Spinner` y resolución Android de recursos.
- Migración `SQLiteOpenHelper` en dispositivos con bases v1 y v2 reales.
- Reacción a cambios rápidos de filtros, recreaciones de Activity y cierre con operaciones pendientes.
- Accesibilidad con TalkBack, fuentes grandes y tamaños de pantalla extremos.
- Rendimiento de búsquedas con catálogos grandes; por ahora solo hay cuatro fichas de demostración.

## Política de compilación

La automatización de Android conserva exclusivamente `workflow_dispatch`. La publicación de código no puede activar el workflow por `push` o `pull_request`. No se generó ninguna APK en fase 07.
