# Asterion — progresión de volumen coordinada (v1, revisión)

## Objetivo y alcance

El selector existente \`MissionPlanner\` sigue decidiendo **qué ejercicios** sugerir.
\`VolumeProgressionEngine\` revisa **cuántas series y repeticiones se registraron realmente**.
\`CoordinatedTrainingEngine\` une ambos; no prescribe entrenamientos obligatorios.

\`\`\`text
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
\`\`\`

## Integración bidireccional real

1. **Volumen → selección:** el esfuerzo reciente del volumen (última observación de la **misma categoría**, hasta 7 días) se combina conservadoramente con la valoración de categoría; un esfuerzo alto reduce las actividades / complejidad usando \`CategoryEffortPolicy\`. Si existe volumen completado moderado en una variante antes mostrada, el coordinador permite conservarla para comparar sesiones, excepto cuando la recuperación o la revisión semanal favorezcan otra opción.
2. **Selección → volumen:** los consejos de volumen se calculan **solamente** para las variantes concretas que el selector propuso. Otra variante del mismo tipo o dificultad no hereda repeticiones o series de una variante anterior. Una nueva selección parte sin objetivo numérico.
3. La propuesta diaria sigue **fijada durante el día**. Los registros del día actual no reordenan una misión ya visible.
4. Los días de descanso y las pausas por seguridad mantienen prioridad y nunca se sustituyen por ejercicios.

## Datos reales y persistencia

- El usuario puede completar sin cifras como siempre. En \`RoutineStepActivity\`, un botón **opcional** permite registrar series realizadas, repeticiones por serie y esfuerzo percibido.
- \`VolumeProgressStore\` guarda solo fecha, categoría, ID de catálogo, tipo, complejidad técnica, volumen y esfuerzo en \`SharedPreferences\` locales. No modifica datos SQLite históricos ni registros previos de finalización. Las entradas se agregan conservadoramente en rondas repetidas del mismo día; lectura limitada a 21 días, retención máxima de 90 días.
- Sin registro manual, el motor muestra **sin historial**, no inventa objetivos.
- Solo se persiste al confirmar **Guardar y completar**; cancelar no marca actividad como hecha.
- El historial de una categoría nunca modifica directamente otra categoría.

## Reglas de v1 (conservadoras, no prescripción)

| Evidencia en la misma variante | Acción | Ejemplo con histórico 2 series × 8 |
|---|---|---|
| Ninguna | Sin objetivo | No hay cifra que comparar |
| 1 sesión fácil completada | Mantener | 2 × 8 históricos, sin aumento |
| 2 sesiones fáciles completadas, con al menos 2 días entre ellas y 7 días desde la primera, consistencia de series/reps, sin alto esfuerzo reciente | **Opción voluntaria**, nunca aplicada automáticamente | 2 × 8 → considerar 2 × 9 |
| Esfuerzo >=7/10 o interrupción en la categoría en los últimos 7 días | Recuperación, no incremento | Mantener/simplificar/descansar |
| Sesiones incompletas, no comparables, variaciones de volumen | Mantener o iniciar historia nueva | No transfiere automáticamente |
| Repeticiones >=20 | Revisión de series/técnica con orientación | No eleva ni series ni repeticiones |
| Pausa de categoría | Ninguna progresión | Respetar pausa |

**Importante:** \`2 × 9\` es un ejemplo de oferta revisable tras evidencia repetida, NO una obligación, ni una indicación clínica personal. La v1 nunca añade una serie ni cambia cargas. El registro de series permite comparar el volumen real; cambios de series quedan para una evaluación posterior y requieren criterios separados.

Se preservan los filtros previos de seguridad: complejidad técnica automática <=3, sin variantes que requieren supervisión, equipo compatible, sin subir la cantidad de ejercicios por feedback.

## Pruebas

\`progress-engine/src/test/java/.../VolumeProgressionEngineTest.java\` cubre ausencia de historial, una sesión fácil, dos sesiones comparables, separación temporal, sesiones incompletas, esfuerzo alto, aislamiento de categoría, ausencia de transferencia entre variantes, pausa y revisión de series.

\`.../CoordinatedTrainingEngineTest.java\` usa el catálogo real de 1257 variantes, comprueba que volumen alto simplifica la selección, que la selección configura las sugerencias del otro motor, que el volumen moderado permite conservar una variante comparable y que un día de descanso no adquiere actividades.

Gradle: \`./gradlew :progress-engine:test\` (o \`gradle :progress-engine:test\` en la CI configurada); resultados JUnit XML en \`progress-engine/build/test-results/test/\`.

## Separación de responsabilidades y próximos incrementos

La v1 deja **sin implementar** objetivos numéricos automáticos, transferencia de cargas externas, prescripción de series, adaptación de movimientos de duración (p. ej. cardio o planchas), aprobación explícita guardada de una oferta futura y gráficos longitudinales de volumen. Para la siguiente fase, solicitar revisión del registro/objetivo por parte del usuario y habilitar reversión antes de incrementar las propuestas.

Este módulo no sustituye la evaluación profesional de la técnica, las necesidades individuales ni las pausas por malestar. La AAP subraya importancia de supervisión adecuada y evitar sobreentrenamiento en programas de fuerza de jóvenes: [HealthyChildren](https://www.healthychildren.org/English/news/Pages/Guidance-on-Resistance-Training-for-Children.aspx); [informe sobre sobreuso](https://publications.aap.org/pediatrics/article/153/2/e2023065129/196435/Overuse-Injuries-Overtraining-and-Burnout-in-Young).
