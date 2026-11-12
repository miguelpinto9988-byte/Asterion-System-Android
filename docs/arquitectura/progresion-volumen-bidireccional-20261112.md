# Asterion — progresión de volumen coordinada (v1, revisión)

## Objetivo y alcance

El selector existente `MissionPlanner` sigue decidiendo **qué ejercicios** sugerir.
`VolumeProgressionEngine` revisa **cuántas series y repeticiones se registraron realmente**.
`CoordinatedTrainingEngine` une ambos; no prescribe entrenamientos obligatorios.

```text
Calendario + perfil + catálogo       Registro voluntario de volumen
                |                              |
                v                              v
        MissionPlanner <--> CoordinatedTrainingEngine
                |                              |
                v                              v
        Selección de variantes    VolumeProgressionEngine
                |                              |
                +--------> Propuesta <---------+
                              |
                        Aceptación opcional;
                     nunca aumenta por defecto
```

## Integración bidireccional real

1. **Volumen → selección:** el esfuerzo reciente del volumen (última observación de la **misma categoría**, hasta 7 días) se combina conservadoramente con la valoración de categoría; un esfuerzo alto reduce las actividades / complejidad usando `CategoryEffortPolicy`. Si existe volumen completado moderado en una variante antes mostrada, el coordinador permite conservarla para comparar sesiones, excepto cuando la recuperación o la revisión semanal favorezcan otra opción.
2. **Selección → volumen:** los consejos de volumen se calculan **solamente** para las variantes concretas que el selector propuso. Otra variante del mismo tipo o dificultad no hereda repeticiones o series de una variante anterior. Una nueva selección parte sin objetivo numérico.
3. La propuesta diaria sigue **fijada durante el día**. Los registros del día actual no reordenan una misión ya visible.
4. Los días de descanso y las pausas por seguridad mantienen prioridad y nunca se sustituyen por ejercicios.

## Datos reales y persistencia

- El usuario puede completar sin cifras como siempre. En `RoutineStepActivity`, un botón **opcional** permite registrar series realizadas, repeticiones por serie y esfuerzo percibido.
- `VolumeProgressStore` guarda solo fecha, categoría, ID de catálogo, tipo, complejidad técnica, volumen y esfuerzo en `SharedPreferences` locales. No modifica datos SQLite históricos ni registros previos de finalización. Las entradas se agregan conservadoramente en rondas repetidas del mismo día; lectura limitada a 21 días, retención máxima de 90 días.
- Sin registro manual, el motor muestra **sin historial**, no inventa objetivos.
- Solo se persiste al confirmar **Guardar y completar**; cancelar no marca actividad como hecha.
- El historial de una categoría nunca modifica directamente otra categoría.

## Reglas revisadas v2 · propuesta automática, nunca ejecución obligatoria

Cuando existe un registro **completado** de la misma variante y categoría, la referencia
numérica de la siguiente sesión se calcula automáticamente de acuerdo con su última
valoración, incluso si solo existe una sesión anterior.

| Esfuerzo | Delta de repeticiones por serie | Estado |
|---|---:|---|
| 1/10 | +5 | Referencia calculada automáticamente, NO obligatoria |
| 2/10 | +3 | Referencia calculada automáticamente, NO obligatoria |
| 3/10 | +1 | Referencia calculada automáticamente, NO obligatoria |
| 4–7/10 | 0 | Mantener referencia |
| 8/10 | −1 | Simplificar referencia |
| 9/10 | −2 | Simplificar referencia |
| 10/10 | −3 | Simplificar referencia |

Con tres sesiones fáciles y **completadas**, en tres días distintos, en la misma
variante, manteniendo igual cantidad de series, y separadas al menos por siete días
entre la primera y la última, se calcula una propuesta de **una serie adicional**
hasta un máximo de tres. Cuando se revisa el número de series, **no** se agregan
repeticiones en esa misma revisión. Las repeticiones calculadas se acotan a
20 por serie y las reducciones no descienden de una repetición en la referencia.
Estos límites son del software, no recomendaciones físicas para una persona.

Los valores de referencia son **informativos y no obligatorios**. La persona puede
descansar, detenerse, registrar un volumen diferente, omitir una sesión o no aceptar
un aumento, sin penalización. El motor no cambia peso, duración, calendario ni
carga externa. Los ajustes nunca cruzan categorías ni variantes.

### Revisión técnica de 7, 14 y 21 días

`TechnicalCheckpointPolicy` revisa la categoría del día con evidencia semanal
de actividades completadas y valoradas:

- A los **7 días transcurridos**, con dos valoraciones fáciles completadas y sin
  alto esfuerzo, puede habilitarse la siguiente fase de mezcla técnica.
- A los **14 días transcurridos**, se reevalúa la categoría; si sigue cumpliendo
  los requisitos, puede habilitarse una fase adicional.
- A los **21 días transcurridos**, el sistema mantiene el techo técnico automático
  de complejidad 3 y puede variar movimientos equivalentes; nunca exige subir
  a complejidad 4 o 5.
- Valoraciones **8–10** o interrupciones impiden aumentar la complejidad;
  las de **4–7** no justifican una promoción. Las fases nunca dependen de una
  categoría diferente.

El día exacto de revisión depende del primer inicio del nivel de experiencia y
del historial de la categoría; si la categoría no está programada ese día,
se revisa en su siguiente aparición. No se alteran días de descanso.

La revisión es automática en el software. **No se obliga a realizar** los
ejercicios, las repeticiones ni las series sugeridas.

## Pruebas

`progress-engine/src/test/java/.../VolumeProgressionEngineTest.java` cubre ausencia de historial, una sesión fácil, dos sesiones comparables, separación temporal, sesiones incompletas, esfuerzo alto, aislamiento de categoría, ausencia de transferencia entre variantes, pausa y revisión de series.

`.../CoordinatedTrainingEngineTest.java` usa el catálogo real de 1257 variantes, comprueba que volumen alto simplifica la selección, que la selección configura las sugerencias del otro motor, que el volumen moderado permite conservar una variante comparable y que un día de descanso no adquiere actividades.

Gradle: `./gradlew :progress-engine:test` (o `gradle :progress-engine:test` en la CI configurada); resultados JUnit XML en `progress-engine/build/test-results/test/`.

## Separación de responsabilidades y próximos incrementos

La v2 **calcula referencias numéricas** de manera automática pero no las impone: falta aprobación guardada explícitamente de una referencia futura, historial visible de propuestas, transferencia entre modalidades comparables y ajustes de movimientos por duración (p. ej., cardio o planchas). Para la siguiente fase, solicitar revisión del registro/objetivo por parte del usuario y habilitar reversión antes de incrementar las propuestas.

Este módulo no sustituye la evaluación profesional de la técnica, las necesidades individuales ni las pausas por malestar. La AAP subraya importancia de supervisión adecuada y evitar sobreentrenamiento en programas de fuerza de jóvenes: [HealthyChildren](https://www.healthychildren.org/English/news/Pages/Guidance-on-Resistance-Training-for-Children.aspx); [informe sobre sobreuso](https://publications.aap.org/pediatrics/article/153/2/e2023065129/196435/Overuse-Injuries-Overtraining-and-Burnout-in-Young).
