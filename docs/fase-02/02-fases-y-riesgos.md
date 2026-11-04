# Fase 02 — Trazabilidad y límites del alcance

| Etapa autorizada | Evidencia material |
|---|---|
| 02.1 Organización modular | `core/build.gradle`, `settings.gradle`, dependencia desde `app/build.gradle` |
| 02.2 Núcleo principal | `core/application`, `core/domain`, `core/platform` |
| 02.3 Arquitectura de eventos | `core/events` y pruebas de bus tipado |
| 02.4 Estado y ciclo de vida | `core/state`, `core/lifecycle`, adaptador Android y coordinador |
| 02.5 Contratos de motores especializados | `core/engine/RuleEvaluator.java`, sin implementación de negocio |
| 02.6 Integración Android | `MainActivity.java` usando `AndroidLifecycleAdapter`; XML original intacto |
| 02.7 Pruebas y documentación | Pruebas JVM de fuente, pruebas JUnit preparadas y documentos fase 02 |

## Fases posteriores no adelantadas

- **03 (pendiente):** colores y pantallas definitivas no diseñados.
- **04 (aprobada):** navegación, restauración de UI y estado persistente no implementados.
- **05 (aprobada):** tablas, migraciones y repositorios concretos no implementados.
- **06 (aprobada):** motor y reglas de negocio no implementados.
- **07, 08, 09, 11 y 14 (pendientes):** ningún sistema funcional activado.
- **10, 12 y 13 (excluidas):** ningún sistema multimedia, notificación o integración.
- **15 y 16 (aprobadas):** pruebas de arquitectura iniciales, sin auditoría integral ni nueva APK.

## Cambios que requieren autorización

Ejecutar cualquier tarea Gradle que genere una APK; volver a habilitar disparadores automáticos de GitHub Actions; incorporar navegación, pantallas, permisos Android, SQLite, lógica funcional o dependencias externas no contempladas.
