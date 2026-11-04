# Fase 06 — Motor central de reglas

## Alcance implementado

Un evaluador **Java 17 puro** que procesa reglas numéricas tipadas y deterministas. El motor no evalúa scripts, expresiones arbitrarias ni código externo. No contiene reglas específicas de ejercicios, planificación, seguimiento, automatizaciones o videojuegos. Tampoco se muestra desde la aplicación Android: las pantallas continúan exclusivamente en XML y esta infraestructura espera una futura integración autorizada.

## Componentes

- `RuleContext`: mapa inmutable y validado de hasta 32 valores enteros largos identificados por nombres de campo estables.
- `NumericCondition`: predicado tipado de un campo comparado con un número, con seis operadores (`>`, `>=`, `<`, `<=`, `==`, `!=`). Devuelve `MATCH`, `NO_MATCH` o `MISSING_FACT` sin excepción por ausencia de dato.
- `DecisionRule`: definición inmutable con ID, prioridad -1000..1000, estado habilitado, condición y código de resultado; valida tamaños y sintaxis.
- `RuleCatalog`: **puerto de lectura** para cargar reglas. No depende de SQLite. En esta fase se utiliza una implementación suministrada mediante una función Java; no se agrega tabla ni adaptador SQL para reglas.
- `DeterministicRuleEngine`: implementa el contrato existente `RuleEvaluator<RuleContext,RuleEvaluation>` y ejecuta una pasada acotada sobre hasta 64 reglas.
- `RuleEvaluation`: resultado inmutable con estado, regla seleccionada, código de salida, traza ordenada y diagnósticos técnicos.
- `RuleEngineService` y `RuleEvaluationCompletedEvent`: adaptador opcional que publica el resultado a través del `EventBus` de fase 02. **No son notificaciones Android**.

## Algoritmo de selección

1. Obtiene las reglas de un `RuleCatalog`; rechaza más de 64, IDs duplicados, entradas nulas o fallos del proveedor con estado `INVALID_RULESET` y diagnóstico codificado.
2. Ordena por prioridad descendente y, en empates, por identificador ascendente. El orden de entrada no altera la decisión.
3. Evalúa cada regla habilitada **una sola vez**. Una regla deshabilitada o sin dato recibe su estado de traza correspondiente.
4. Si ninguna regla coincide, devuelve `NO_MATCH`.
5. Si dos reglas coincidentes de prioridad máxima generan **códigos de salida distintos**, devuelve `CONFLICT` sin seleccionar ninguna.
6. Si las reglas de prioridad máxima coincidentes proponen el mismo resultado, elige la de ID menor y registra `TIE_RESOLVED_BY_RULE_ID`.
7. Las coincidencias de menor prioridad se ignoran para decidir, pero permanecen en la traza y generan `LOWER_PRIORITY_MATCH_IGNORED`.

No hay reglas anidadas, ejecución recursiva, mutación de contexto, bucles controlados por datos ni `eval`: se evitan cadenas de reglas circulares por diseño. El resultado no escribe datos ni ejecuta acciones de negocio. El evento del servicio es síncrono y se emite una vez por evaluación.

## Relación con fases anteriores

- **Fase 02:** reutiliza `RuleEvaluator`, `EventBus` y los contratos de eventos.
- **Fase 04:** el controlador de navegación y los XML no se modifican.
- **Fase 05:** el puerto `RuleCatalog` permite conectar una implementación de datos en el futuro, pero `technical_records`, `SQLiteOpenHelper` y `TechnicalRecordRepository` no se reutilizan para guardar reglas. No hay cambios a SQL ni migraciones.

## Ejemplo exclusivamente técnico

`principal` evalúa si `score >= 50`, con salida `APROBADO`; otra regla puede evaluar `score >= 0` con prioridad menor. Son códigos de prueba, no decisiones de la app Hunter ni funcionalidades del usuario.
