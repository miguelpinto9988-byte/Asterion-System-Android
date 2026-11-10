# Asterion System — Progresión contextual por categoría

**Implementado en `main`:** memoria local de valoración del esfuerzo para cada categoría (índices 0 a 4), decisiones conservadoras de la siguiente sesión, interfaz de valoración al completar misiones y pantalla de Progreso con estado por categoría.

## Categorías

| Índice | Categoría | Color del calendario |
|---|---|---|
| 0 | Tren superior | `#2088B7` |
| 1 | Tren central | `#8456CA` |
| 2 | Tren inferior | `#D39437` |
| 3 | Movilidad | `#258D70` |
| 4 | Cardio | `#B95172` |
| 5 | Descanso | `#64748B` |

La valoración de 1 a 10 corresponde a la categoría seleccionada por el calendario y la fecha de la actividad. El registro persistente usa `category_effort_<fecha>_<índice>`; el registro agregado anterior `day_effort_<fecha>` sigue utilizándose para las revisiones globales de 7, 14 y 21 días. **Los registros agregados anteriores no se convierten retrospectivamente en registros de una categoría.**

## Comportamiento

- 1–3/10: el motor intenta cambiar automáticamente una variante por otra del **mismo tipo de movimiento y del mismo nivel técnico**, si existe y el material coincide; conserva el número de actividades y no aumenta repeticiones.
- 4–6/10: el motor conserva las variantes de la sesión anterior de la misma categoría, cuando siguen siendo compatibles.
- 7–8/10: el motor genera como máximo dos actividades y prefiere variantes técnicamente sencillas dentro de esa categoría.
- 9–10/10: el motor genera como máximo una actividad de baja complejidad técnica; descanso y suspensión de la actividad siempre posibles.
- Sin valoración previa: mantener la propuesta, sin inventar datos.
- Día de descanso: no proponer ejercicios.

La decisión se basa en la valoración **más reciente de esa misma categoría durante los 21 días anteriores**. Las valoraciones del día actual no modifican retroactivamente la misión del día. La valoración de tren inferior nunca se utiliza para ajustar una propuesta de tren superior, ni viceversa.

**Nota sobre progresión de repeticiones:** la versión actual no calcula aumentos automáticos de 3, 4 o 5 repeticiones porque el catálogo de ejercicios no incluye una prescripción individual verificada de series, repeticiones y limitaciones. Una revisión de progresión podría diseñarse más adelante con una base de referencia, información individual apropiada, evaluación de seguridad y capacidad permanente de mantener o disminuir la exigencia.

## Ejemplo de aceptación

Lunes: tren superior, valoración 3/10 → el miércoles el motor intenta seleccionar automáticamente una variante equivalente de tren superior, sin intervención manual. Martes: tren inferior, valoración 5/10 → el viernes el sistema mantiene el nivel. Si el martes se registra 10/10 → el viernes se reduce la propuesta de tren inferior sin alterar la de tren superior.

## Revisión automática al séptimo día

La **primera revisión operativa** se habilita después de siete días naturales desde la primera valoración registrada. Se vuelve a evaluar la evidencia de los **siete días anteriores** cada vez que corresponda entrenar esa categoría, sin un botón de selección manual.

- El motor exige al menos **dos actividades completadas y valoradas como poco exigentes (1–3/10)**, procedentes de **días diferentes de una misma categoría**, para habilitar una nueva variante equivalente.
- Una valoración de esfuerzo alto (7–10/10) en esa ventana da prioridad a la recuperación, aunque la actividad haya quedado incompleta.
- Las categorías no comparten observaciones ni modificaciones; un informe de tren inferior no decide el progreso de tren superior.
- Si solo hay una valoración, no hay suficiente evidencia, o el usuario descansó, **se conserva el plan**. El descanso no cuenta como fracaso.
- El cambio autónomo consiste en seleccionar una variante del **mismo tipo de movimiento y nivel técnico**, cuando esté disponible y sea compatible con el material configurado; no se duplican series ni se imponen incrementos de repeticiones.
- Las revisiones de 14 y 21 días continúan disponibles para interpretar tendencias generales, y nunca anulan señales de esfuerzo elevado.

La lógica distingue **variedad del desafío** de **intensidad física**: pasar a otra variante del mismo nivel técnico no garantiza que la carga sea equivalente para una persona concreta. El usuario puede descansar, detenerse o corregir su valoración en cualquier momento. No se deduce seguridad de una calificación baja por sí sola.

## Autonomía real y límites de inferencia

**Implementado:** al regenerar la misión, `MissionsActivity` consulta el historial de variantes mostradas para esa categoría mediante `MissionTrackingStore.previousCategoryVariants`; el motor recibe esas variantes y la valoración pertinente, construye automáticamente la propuesta y conserva el resultado en `rememberCategoryProposal`. El usuario no tiene que escoger manualmente una variante.

La lógica es **determinista y basada en reglas**, no un modelo de lenguaje ni una inteligencia entrenada que comprenda por sí sola la biomecánica. Dos variantes del mismo tipo y nivel técnico pueden sentirse diferentes: la etiqueta de nivel técnico NO demuestra que sean equivalentes en volumen ni carga. Por eso el cambio automatizado se limita a esa clasificación y nunca fuerza trabajo adicional.

El registro actual de esfuerzo corresponde a **toda la categoría**, no a cada ejercicio individual. Para afirmar qué ejercicio fue más fácil se necesitarían nuevas observaciones por ejercicio o datos fiables adicionales. Los registros incompletos se consideran desconocidos.

El programa general y la autorización de Monarca siguen siendo independientes: la aprobación inicial de una misión Monarca de cinco días no es reemplazada por la automatización de misiones diarias.

### Verificaciones técnicas

- Conservación de categorías, descansos, horarios del perfil y materiales autorizados.
- Ante 3/10, intercambio posible de `sentadilla_bilateral_v01` a `sentadilla_bilateral_v02` en la siguiente sesión **de la misma categoría**, si ambas están disponibles; sin subir el nivel técnico.
- Ante 5/10, mantener la variante previa si sigue siendo compatible.
- Ante 10/10, disminuir candidatos y priorizar dificultad técnica 1.
- Un valor de tren inferior no puede aplicarse a tren superior (prueba explícita de rechazo).
- Propuestas reproducibles para el mismo día y la misma memoria, sin requerir otro clic.
- No inferir ni imponer metas de repeticiones, cargas, ni progresión forzosa en días 7/14/21.

## Archivos principales

- `progress-engine/src/main/java/pe/asterionsystem/core/engine/progression/CategoryEffortPolicy.java`
- `progress-engine/src/test/java/pe/asterionsystem/core/engine/progression/CategoryEffortPolicyTest.java`
- `progress-engine/src/main/java/pe/asterionsystem/core/engine/progression/WeeklyCategoryChallengePolicy.java`
- `progress-engine/src/test/java/pe/asterionsystem/core/engine/progression/WeeklyCategoryChallengePolicyTest.java`
- `progress-engine/src/main/java/pe/asterionsystem/core/engine/missions/MissionPlanner.java`
- `progress-engine/src/test/java/pe/asterionsystem/core/engine/missions/AutonomousCategoryMissionTest.java`
- `app/src/main/java/pe/asterionsystem/app/MissionTrackingStore.java`
- `app/src/main/java/pe/asterionsystem/app/MissionsActivity.java`
- `tests/mission_tracking_progress_contract.py`
- `docs/previews/asterion-progreso-por-categoria.html`

## Garantías que deben mantenerse

- No modificar la distribución de siete días ni los seis colores de categoría.
- No aumentar automáticamente volumen o dificultad a partir de una sola calificación.
- Conservar el almacenamiento histórico de SQLite y los ciclos de revisión de 7, 14 y 21 días.
- Validar la valoración 1–10 y no registrarla sin acción del usuario.
- Las misiones Monarca siguen siendo independientes y requieren aceptación explícita.
- Permitir descanso, variantes más fáciles y control del usuario aun en el día 21.