# Fase 06 — Trazabilidad de siete etapas

| Etapa aprobada | Evidencia |
|---|---|
| 06.1 Arquitectura | `RuleContext`, `RuleCatalog`, `RuleEvaluator`, `docs/fase-06/06-arquitectura-motor.md` |
| 06.2 Reglas y condiciones | `DecisionRule`, `NumericCondition` y operadores tipados |
| 06.3 Evaluación | `DeterministicRuleEngine`, `RuleEvaluation`, prioridades y resultados |
| 06.4 Validación y errores | límites de 32 datos/64 reglas, IDs únicos, `INVALID_RULESET`, `CONFLICT` |
| 06.5 Comunicación | `RuleEngineService`, `RuleEvaluationCompletedEvent`, puerto `RuleCatalog`; sin conexión SQL todavía |
| 06.6 Diagnósticos | traza completa por regla, diagnósticos codificados, estado estructurado |
| 06.7 Pruebas y previsualización | pruebas Java fuente, compilación `javac`, documentación y HTML externo |

La fase 06 no modifica interfaz Android XML ni genera una nueva APK. Las pantallas de diseño definitivo de fase 03 siguen sin aprobación. La persistencia de reglas necesita una fase posterior autorizada.
