# Asterion System — Android nativo Java

**Estado:** fases 00 (documentación), 01 (proyecto base), 02 (arquitectura), 04 (navegación), 05 (almacenamiento SQLite) y **06 (motor central de reglas en Java puro)**. La fase 03 de identidad visual sigue pendiente de autorización.

Proyecto nuevo y de código original. La aplicación Android utiliza Java + vistas XML nativas; HTML se utiliza únicamente para previsualizaciones externas.

## Arquitectura

- `:core`: Java 17 puro. Eventos, navegación, estado, datos y motor de reglas.
- `:app`: Android Java + XML. Dos pantallas provisionales `screen_home.xml` y `screen_secondary.xml`, navegación, adaptadores de ciclo de vida, e infraestructura SQLite técnica.
- `docs/fase-06/`: arquitectura, validaciones y trazabilidad.

## Motor central, fase 06

Las clases `RuleContext`, `NumericCondition`, `DecisionRule`, `RuleCatalog`, `DeterministicRuleEngine`, `RuleEvaluation`, `RuleEngineService` y `RuleEvaluationCompletedEvent` implementan un evaluador **numérico, tipado, determinista, inmutable y acotado**.

- Hasta 32 valores de entrada y 64 reglas por evaluación.
- Prioridades descendentes y desempate estable por identificador.
- Detección de conflictos cuando dos reglas de máxima prioridad producen resultados diferentes.
- Resultados `MATCH`, `NO_MATCH`, `CONFLICT`, `INVALID_RULESET`.
- Trazas y diagnósticos para cada evaluación.
- Reutilización del `EventBus` de fase 02; el motor no modifica la base de datos.
- `RuleCatalog` es un contrato de lectura para una futura fuente de reglas, sin implementación SQLite en fase 06.

**No hay todavía reglas específicas de negocio ni pantallas Android que ejecuten este motor.** La base SQLite técnica de fase 05 permanece desacoplada.

## Previsualización HTML

El archivo HTML interactivo de fase 06 se entrega separadamente: simula las pantallas XML existentes, el laboratorio CRUD de fase 05 y las condiciones/decisiones del motor de fase 06. La simulación **no ejecuta el Java real ni forma parte de la APK**.

## Pruebas y restricción de compilación

El núcleo se compiló con `javac --release 17`; diez métodos de pruebas de lógica pasaron con sustitutos mínimos de JUnit, no con Gradle/JUnit real. El JavaScript del simulador pasó comprobaciones sintácticas y seis escenarios con DOM simulado. Pruebas de dispositivo y Android pendientes.

**No compilar APK sin autorización expresa.** El workflow `.github/workflows/android-ci.yml` conserva solo el disparador manual `workflow_dispatch`. Esta fase no modifica ese archivo ni ejecuta Gradle.

## Documentación

- [Fase 04: navegación](docs/fase-04/04-arquitectura-y-pantallas.md)
- [Fase 05: SQLite](docs/fase-05/05-arquitectura-almacenamiento.md)
- [Fase 06: arquitectura](docs/fase-06/06-arquitectura-motor.md)
- [Fase 06: validación](docs/fase-06/06-validacion.md)
- [Fase 06: trazabilidad](docs/fase-06/06-trazabilidad.md)

## Próximas decisiones

Las fases aprobadas de optimización y publicación son posteriores; las fases de catálogo, planificación, seguimiento, personalización y sincronización siguen pendientes. No se añadirá ninguna funcionalidad de fases pendientes sin autorización. Las fases 10, 12 y 13 se mantienen excluidas.

Repositorios públicos no implican por sí mismos licencia libre. No incorporar secretos ni claves de firma.
