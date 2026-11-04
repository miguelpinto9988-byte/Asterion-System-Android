# Fase 05 — Verificación sin APK

## Validación planificada

| Verificación | Herramienta | Límite |
|---|---|---|
| Compilación del núcleo Java | `javac --release 17` | No comprueba el SDK Android |
| Pruebas de entidad y worker | Pruebas JUnit definidas en `:core` | Se ejecutarán cuando se autorice el uso de Gradle, o mediante un runner JVM de prueba |
| SQL de creación y migraciones | `python3 tests/phase05_sqlite_smoke.py` | SQLite de Python, no Android |
| Consultas y restricciones | Pruebas SQLite con datos ficticios | No valida la implementación `Cursor` de Android |
| Hilo principal bloqueado | Revisión de `Looper` | Prueba en dispositivo pendiente |
| Revisión de permisos y backup | `AndroidManifest.xml` | Verificación estática |
| Previsualización HTML | HTML/CSS/JS en navegador | Simulación independiente, no usa la base real |

## Casos de aceptación

- Alta, lectura, modificación, borrado y paginación de registros técnicos.
- Protección frente a títulos vacíos, IDs duplicados, longitudes excesivas y timestamps incoherentes.
- Migración v1→v2 que preserve registros, timestamps y esquema.
- Errores de migración no reconocidos deben detener la operación, nunca borrar datos.
- SQL parametrizado y cierre de cursores.
- APIs Android acotadas a `:app`; contratos puros en `:core`.
- **Cero tareas `assembleDebug` o generación APK.**
- Workflow Android restringido a `workflow_dispatch`.

## Precisión

La ejecución de pruebas reales del adaptador SQLite sobre Android está **pendiente**. Esta fase constituye una implementación de infraestructura, no una comprobación de la aplicación instalada. La simulación HTML usa almacenamiento local del navegador (si está permitido) y no representa una conexión SQLite real.

## Riesgos pendientes

- Versiones concretas de SQLite del dispositivo y gestión efectiva de migraciones.
- Compatibilidad del código de `:app` con Android Gradle Plugin y SDK en una compilación futura autorizada.
- Latencia y concurrencia en dispositivos reales.
- Cierre oportuno del worker y del helper al integrar UI en futuras fases.
- Política de cifrado si algún día se manejan datos sensibles.
