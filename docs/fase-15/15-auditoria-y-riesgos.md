# Fase 15 — Auditoría técnica y riesgos

## Alcance autorizado

Auditoría, optimización, seguridad, integridad, accesibilidad y pruebas de fuente/escritorio. La fase no compila Android, no produce APK, no activa workflows y no incluye funciones de distribución (fase 16) ni cambios de identidad visual definitiva (fase 03, pendiente).

## Situación de partida (fase 14)

- Aplicación Android nativa Java + XML con módulos `:core` y `:app`.
- SQLite privado `asterion_internal.db` versión **5**, migraciones ascendentes no destructivas, WAL y claves foráneas activas.
- Preferencias privadas mediante `SharedPreferences`; AndroidManifest con `allowBackup=false`, `usesCleartextTraffic=false` y sin permiso `INTERNET`.
- Almacenamiento offline y diagnóstico local no destructivo.
- Pantallas catálogo, planificador, seguimiento, configuración y estado local, sin WebView ni archivos HTML en `:app`.
- GitHub Actions configurado con `workflow_dispatch` manual.

## Hallazgos y tratamientos de fase 15

| Área | Hallazgo técnico | Acción implementada | Validación pendiente |
|---|---|---|---|
| Rendimiento de planificación | Resolver cada título desde `findById` ocasionaba N+1 consultas | Nuevo puerto `titlesById` y adaptador SQLite con parámetros por lotes de hasta 200 | Tiempo real y plan de ejecución en Android |
| Búsqueda | Una consulta por cada cambio en el campo | Debounce de 280 ms, invalidación de respuestas antiguas y retirada del TextWatcher al cambiar pantalla | Medición con perfilador Android |
| Diagnóstico | Recuento de tablas ejecutado en sentencias independientes | Una sentencia SQL para los tres contadores (instantánea de esa sentencia), comprobación explícita de `PRAGMA foreign_keys=1` | Prueba con SQLiteOpenHelper bajo concurrencia real |
| Accesibilidad | Estados asíncronos y campos sin algunas asociaciones accesibles | Siete regiones de anuncio, tres etiquetas `labelFor`, fecha hablada completa y estados de día seleccionado | TalkBack, tamaños de fuente y dispositivos pequeños |
| Toque | Fila compacta con mínimo de 44 dp | Mínimo de 48 dp en filas compactas y campos esenciales | Medición de objetivos táctiles reales |

## Riesgos residuales

1. No se verificó compilación Android, compatibilidad del APK, JUnit oficial, instrumentación o prueba con dispositivo.
2. Algunos cuadros de diálogo existentes todavía requieren una auditoría de accesibilidad con TalkBack.
3. La búsqueda es `LIKE` local, no índice de texto completo; los catálogos muy extensos requieren análisis medido antes de cambiar índices o esquema.
4. Los lotes SQL reducen consultas pero no demuestran por sí mismos una mejora de tiempo en hardware real.
5. La pantalla de estado no repara bases corruptas ni sustituye copias de seguridad.
6. Se conserva la versión de base SQLite 5 y el comportamiento funcional autorizado; no se añadieron nuevas categorías ni seguimiento automático.
