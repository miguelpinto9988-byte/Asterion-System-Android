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

- 1–3/10: mantener la propuesta y permitir explorar variantes; nunca aumentar repeticiones automáticamente.
- 4–6/10: mantener la propuesta.
- 7–8/10: sugerir como máximo dos actividades y preferir variantes de menor complejidad técnica dentro de la categoría.
- 9–10/10: sugerir como máximo una actividad y preferir variantes de menor complejidad técnica; descanso permitido.
- Sin valoración previa: mantener la propuesta, sin inventar datos.
- Día de descanso: no proponer ejercicios.

La decisión se basa en la valoración **más reciente de esa misma categoría durante los 21 días anteriores**. Las valoraciones del día actual no modifican retroactivamente la misión del día. La valoración de tren inferior nunca se utiliza para ajustar una propuesta de tren superior, ni viceversa.

**Nota sobre progresión de repeticiones:** la versión actual no calcula aumentos automáticos de 3, 4 o 5 repeticiones porque el catálogo de ejercicios no incluye una prescripción individual verificada de series, repeticiones y limitaciones. Una revisión de progresión podría diseñarse más adelante con una base de referencia, información individual apropiada, aceptación del usuario y la opción de mantener o disminuir la exigencia en cualquier momento.

## Ejemplo de aceptación

Lunes: tren superior, valoración 3/10 → el miércoles el sistema presenta una revisión opcional únicamente para tren superior. Martes: tren inferior, valoración 5/10 → el viernes el sistema mantiene el nivel. Si el martes se registra 10/10 → el viernes se reduce la propuesta de tren inferior sin alterar la de tren superior.

## Archivos principales

- `progress-engine/src/main/java/pe/asterionsystem/core/engine/progression/CategoryEffortPolicy.java`
- `progress-engine/src/test/java/pe/asterionsystem/core/engine/progression/CategoryEffortPolicyTest.java`
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