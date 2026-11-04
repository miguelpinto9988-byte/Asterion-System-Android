# Fase 05 — Trazabilidad del alcance aprobado

| Etapa | Evidencia |
|---|---|
| 05.1 Arquitectura de almacenamiento | `core/data/TechnicalRecord*.java`, `docs/fase-05/05-arquitectura-almacenamiento.md` |
| 05.2 Motor SQLite | `app/data/AsterionDatabaseHelper.java` |
| 05.3 Repositorios Java | `TechnicalRecordRepository.java`, `SQLiteTechnicalRecordRepository.java` |
| 05.4 Integridad y validación | `TechnicalRecord.java`, constraints, triggers, SQL con argumentos enlazados |
| 05.5 Migraciones | `onUpgrade` y migración explícita v1→v2 sin borrados |
| 05.6 Rendimiento/seguridad | `BackgroundRecordOperations.java`, guardia `Looper`, cursores y transacciones |
| 05.7 Pruebas y vista HTML | JUnit fuente, prueba SQL en Python, documentación y simulador HTML independiente |

## Prohibiciones respetadas

- Interfaz Android continúa siendo exclusivamente XML y Java.
- No se cambian las pantallas `screen_home.xml` ni `screen_secondary.xml`.
- No se adelantan tablas de planificación, ejercicios, progreso, ajustes o sincronización.
- No se implementan multimedia, notificaciones, ni integraciones/exportaciones.
- No se compila APK ni se habilitan workflows automáticos.
- No se integra SQLite en las pantallas actuales ni se finge una funcionalidad terminada.
