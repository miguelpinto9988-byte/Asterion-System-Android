# Asterion — progresión de volumen coordinada (v4, valores iniciales recalibrados)

## Objetivo y alcance

El selector existente `MissionPlanner` sigue decidiendo **qué ejercicios** sugerir.
`VolumeProgressionEngine` revisa **cuántas series y repeticiones o segundos se registraron realmente**.
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

## Reinicio de volumen al cambiar ejercicio o técnica (hitos 7/14/21)

**Regla:** la progresión numérica pertenece a un **bloque de volumen** de una
variante concreta, no a una categoría en general. Tras una revisión técnica
se comparan las variantes de la categoría actualmente programada con la
selección anterior de esa misma categoría.

- **Mismo ID y nivel técnico:** conserva el volumen histórico de su bloque.
- **ID distinto o cambio de complejidad técnica:** la variante seleccionada
  recibe una **nueva fecha de inicio de bloque**. Los registros anteriores a
  esa fecha ya no alimentan sus propuestas de series o repeticiones.
- **Variante antigua que vuelve a aparecer:** si no estaba en la selección
  anterior, también empieza un bloque nuevo; **no reutiliza** metas acumuladas
  en una etapa previa.
- **Si el usuario cambia el nivel de experiencia:** se reinician también las
  referencias numéricas de las variantes seleccionadas, incluso antes del
  siguiente hito de siete días.
- **Misma fecha, reapertura de pantalla:** no dispara un segundo reinicio.
- **Descanso y otras categorías:** no alteran el historial de otras zonas.
- **Historial completado:** no se borra. El selector puede seguir utilizando
  valoraciones anteriores para proteger recuperación y reportes históricos,
  pero el motor **numérico** filtra lo anterior al inicio del nuevo bloque.

La primera referencia del nuevo bloque se obtiene del archivo **versionado,
específico para esa variante** `volume_profiles.tsv` (1.257 filas
validadas). No se copia ninguna cantidad anterior de otro ejercicio.
Al completar una sesión, los datos realmente realizados pasan a ser la
evidencia para los cálculos posteriores. El valor inicial se presenta como
referencia opcional revisable, nunca como obligación.

Ejemplo técnico (datos ilustrativos, no recomendación de ejercicio):

| Fase | Selección | Registro numérico y resultado |
|---|---|---|
| Antes del hito 7 | Variante A, nivel 1 | Existían 2×8, luego 2×11 |
| Hito 7 | Variante B, nivel 2 | Reinicio: B usa exclusivamente su referencia inicial específica |
| Primera sesión con B | Variante B, nivel 2 | Se registra su propio punto de partida |
| Hito 14 sin cambio de variante | Variante B, nivel 2 | Continúa su historia desde el inicio con B |
| Hito 21 con vuelta a A | Variante A, nivel 1 | Reinicio: vuelve a su referencia inicial, NO hereda el antiguo 2×11 |

**Implementación y pruebas:** `VolumeResetPolicy` (motor puro),
`VolumeProgressStore.observeCheckpointSelection` (persistencia Android),
`progressionHistoryForCategory` (filtro por fecha e ID), y
`VolumeProgressResetPersistenceTest` (instrumentación Android real) además de
pruebas JUnit y un contrato Python en CI.

## Cobertura total de ejercicios: repeticiones y segundos

`asterion_exercises/v1/volume_profiles.tsv` contiene una fila explícita,
verificada por ID, para **cada una de las 1.257 variantes existentes**:

- **924 variantes** expresadas en `REPETITIONS`.
- **333 variantes** expresadas en `SECONDS`, incluidas posturas sostenidas,
  equilibrio, desplazamientos continuos y actividad cardiovascular.
- Cada fila especifica: **ID de variante, unidad, series iniciales, valor
  inicial por serie, mínimo, máximo y factor de cambio**.
- Los valores iniciales se elaboraron mediante clasificación semántica del
  catálogo por tipo, nombre, ejecución, dificultad técnica y categoría. Son
  **referencias de interfaz revisables, no prescripciones clínicas**: los casos
  ambiguos requieren revisión manual de su clasificación.
- Las variantes de mayor complejidad o supervisión tienen perfil explícito
  para registrar información pero **no** habilitan progresión automática.

**Ejemplos informáticos, no instrucciones para entrenar:**

| Variante | Unidad del motor | Referencia inicial de ejemplo |
|---|---|---|
| Sentadilla libre | Repeticiones | 1 serie, 8 repeticiones |
| Flexión inclinada | Repeticiones | 1 serie, 10 repeticiones |
| Plancha de pared | Segundos | 1 serie, 15 segundos |
| Marcha exterior cómoda | Segundos | 1 serie, 75 segundos |

El mismo algoritmo de esfuerzo opera sobre el **valor de la unidad**, nunca
sobre un número ambiguo: en repeticiones el factor de escala es 1, y en
segundos puede ser 1 para una postura o 3 para un movimiento continuo. Por
tanto, una valoración de esfuerzo 2/10 significa +3 repeticiones en un
ejercicio de conteo, +3 segundos en una postura o +9 segundos en un ejercicio
de tiempo continuo, siempre limitado por la referencia máxima del ejercicio.
Los rangos 4–7 mantienen, y 8–10 reducen la referencia.

El registro permite introducir hasta **3600 segundos reales por serie** para actividades prolongadas. Este límite de captura es distinto del **máximo conservador de referencia inicial/progresiva**, que sigue siendo específico de cada variante y no prescribe el tiempo que la persona deba realizar.

La persistencia incorpora un campo de unidad y conserva compatibilidad con
los registros históricos de diez campos, interpretados explícitamente como
repeticiones. **Nunca se convierten registros de repeticiones antiguas en
segundos**. Dos ejercicios pueden compartir categoría sin compartir unidad
ni referencia numérica.

`RoutineStepActivity` muestra la **referencia inicial propia** cuando aún
no existe historial o tras reinicio; deja registrar el número de series y
la cantidad realizada en repeticiones o segundos. Cuando la variante se
mide en segundos, presenta un **contador de ejercicio opcional e
independiente del temporizador de descanso**. El contador no marca el
ejercicio como completado, no inicia nuevas rondas ni otorga XP.

`MissionsActivity` integra el selector con `CoordinatedTrainingEngine`
y **recalcula el volumen después** de la aplicación de los reinicios 7/14/21,
siempre con el perfil exacto de la variante seleccionada.

**Regla de mantenimiento:** si se añade cualquier variante al catálogo sin
una fila correspondiente, falla la validación de referencias en vez de
inventar un volumen genérico.

## Recalibración de los valores de inicio · versión 4

Se incrementó **initial_value** en las **1.257 variantes** sin alterar su
unidad, el número de series iniciales, las repeticiones/segundos realmente
registrados, los mínimos y máximos de seguridad ni las reglas 1–10 de esfuerzo.

- **924 perfiles por repeticiones:** los de nivel 1 suman **3** repeticiones;
  los de niveles 2–3, **2**; los de niveles 4–5, **1**.
- **333 perfiles por segundos:** los **191** movimientos continuos suman
  **15 segundos**; otras **101** variantes de tiempo de menor complejidad,
  **5 segundos**; y las restantes **41**, **3 segundos**.
- Todos los nuevos valores se comprobaron individualmente para no superar
  el máximo configurado de su propia variante. No hubo perfiles omitidos.
- El número de series de arranque permanece en **una**: elevar simultáneamente
  repeticiones/tiempo y series duplicaría innecesariamente el salto de volumen.
- `ExerciseVolumeProfileCatalogTest` verifica el número de perfiles, el
  arranque conservador en una serie y ejemplos de cada tipo y nivel técnico.

La recalibración se aplica cuando el sistema necesita una **nueva referencia**
(sin historial comparable o tras el reinicio por cambio de ejercicio/técnica en
7/14/21 días). Nunca reescribe un registro real ni altera un día fijado
anteriormente. Quien entrena puede hacer menos, descansar o cancelar sin
penalización. Estos valores son ajustes generales de la configuración,
**no recomendaciones individualizadas**.

## Pruebas

`progress-engine/src/test/java/.../VolumeProgressionEngineTest.java` cubre ausencia de historial, una sesión fácil, dos sesiones comparables, separación temporal, sesiones incompletas, esfuerzo alto, aislamiento de categoría, ausencia de transferencia entre variantes, pausa y revisión de series.

`.../ExerciseVolumeProfileCatalogTest.java` cubre las 1.257 referencias y unidades, `TimedVolumeProgressionTest.java` prueba progresión de segundos y los datos anteriores, y `VolumeProgressResetPersistenceTest.java` verifica la persistencia Android. `.../CoordinatedTrainingEngineTest.java` usa el catálogo real de 1257 variantes, comprueba que volumen alto simplifica la selección, que la selección configura las sugerencias del otro motor, que el volumen moderado permite conservar una variante comparable y que un día de descanso no adquiere actividades.

Gradle: `./gradlew :progress-engine:test` (o `gradle :progress-engine:test` en la CI configurada); resultados JUnit XML en `progress-engine/build/test-results/test/`.

## Separación de responsabilidades y próximos incrementos

La v2 **calcula referencias numéricas** de manera automática pero no las impone: falta aprobación guardada explícitamente de una referencia futura, historial visible de propuestas, revisión humana de unidades ambiguas y transferencia controlada entre modalidades comparables. Para la siguiente fase, solicitar revisión del registro/objetivo por parte del usuario y habilitar reversión antes de incrementar las propuestas.

Este módulo no sustituye la evaluación profesional de la técnica, las necesidades individuales ni las pausas por malestar. La AAP subraya importancia de supervisión adecuada y evitar sobreentrenamiento en programas de fuerza de jóvenes: [HealthyChildren](https://www.healthychildren.org/English/news/Pages/Guidance-on-Resistance-Training-for-Children.aspx); [informe sobre sobreuso](https://publications.aap.org/pediatrics/article/153/2/e2023065129/196435/Overuse-Injuries-Overtraining-and-Burnout-in-Young).
