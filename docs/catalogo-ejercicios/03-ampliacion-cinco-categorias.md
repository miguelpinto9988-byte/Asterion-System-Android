# Catálogo de Asterion — ampliación y cinco categorías de entrenamiento

**Fecha:** 2026-11-10 · **Rama:** `main` · **Estado:** datos incorporados al catálogo local; la verificación final depende de CI.

## Resultado y compatibilidad

El inventario local `progress-engine/src/main/resources/asterion_exercises/v1/` se ha ampliado **sin cambiar ni borrar los identificadores originales**, desde 39 familias / 80 tipos / 445 variantes hasta **49 familias / 100 tipos / 505 variantes**. Se mantienen los tres TSV originales y el mismo lector estricto de datos, sin migración de SQLite ni consultas de red en tiempo de ejecución.

La Biblioteca y el generador de misiones utilizan ahora una **categoría primaria unificada** mediante `ExerciseCategoryMapper`:

| Grupo de perfil / calendario | Código interno | Familias | Tipos | Variantes | Contenido principal |
| --- | --- | ---: | ---: | ---: | --- |
| Tren superior | `tren_superior` | 12 | 25 | 136 | Empuje, tracción, brazos, hombros, manos, muñeca |
| Tren central | `tronco` | 8 | 17 | 84 | Estabilidad, control postural, tronco, transportes integrados |
| Tren inferior | `piernas` | 11 | 25 | 131 | Cadera, rodilla, pie, piernas |
| Movilidad | `movilidad` | 10 | 20 | 96 | Flexibilidad, equilibrio, coordinación, control articular |
| Cardio | `cardio` | 8 | 13 | 58 | Marcha, carrera, bicicleta, danza, pasos aeróbicos |
| **Total** | **5 grupos** | **49** | **100** | **505** | |

**Descanso** es la sexta elección del calendario semanal (`index=5`) y permanece fuera del catálogo de actividades. Los colores de las seis categorías semanales no se cambian.

Los grupos descriptivos antiguos `coordinacion` y `integrado` **se conservan en las filas de origen** para evitar romper IDs y relaciones:
- `coordinacion` se coloca bajo **Movilidad**, como clasificación primaria neuromotora.
- `integrado` se coloca bajo **Tren central** por estabilidad de tronco; sigue siendo descriptivamente un ejercicio integrado, y puede implicar otras regiones corporales.

La categoría primaria es una **decisión operativa para crear misiones**, no un diagnóstico ni la afirmación de que un movimiento involucre únicamente esa región.

## Nuevas familias y tipos

| Categoría | Familia añadida | Tipos de ejercicio nuevos |
| --- | --- | --- |
| Tren superior | Agarre y control de manos | Prensión suave; separación de dedos |
| Tren superior | Control activo de muñeca | Flexión activa; extensión activa |
| Tren central | Estabilidad en cuadrupedia | Extensión contralateral; control en cuatro apoyos |
| Tren central | Control del tronco sentado | Alineación; alcances con tronco estable |
| Tren inferior | Control de los dedos del pie | Apertura/separación; prensión suave |
| Tren inferior | Extensión de cadera fuera de puente | Extensión de pie; extensión en cuatro apoyos |
| Movilidad | Movilidad suave de cuello | Rotación; inclinación suave |
| Movilidad | Movilidad de dedos y pulgar | Oposición del pulgar; flexión/extensión |
| Cardio | Pasos aeróbicos de bajo impacto | Toques alternados de talón; elevación alterna de rodillas |
| Cardio | Danza recreativa de bajo impacto | Pasos básicos de baile; desplazamiento lateral rítmico |

Cada nuevo tipo tiene **tres variantes etiquetadas**: 20 tipos × 3 variantes = **60**. Los nombres y parámetros están adaptados y redactados para Asterion; **no se han copiado instrucciones ni fotografías** de otros sitios.

## Uso por el motor Android

1. El perfil define el día del calendario (0–4 entrenamiento, 5 descanso), experiencia, objetivo y material.
2. `ExerciseCategoryMapper` asigna todas las familias de la fuente a uno de esos cinco grupos.
3. `MissionPlanner` selecciona variantes **únicamente** del grupo correspondiente y aplica los filtros existentes de equipamiento y dificultad técnica. También permite que el desafío Monarca utilice familias antes excluidas por el desfase de siete frente a cinco categorías.
4. `MissionsActivity` muestra los **cinco grupos** en la Biblioteca; desde cada familia se navega a tipos y variantes.
5. `RoutineStepActivity` utiliza el mismo mapeo de categoría para que una interrupción asociada al ejercicio no afecte erróneamente a otros grupos.
6. El registro de progreso y los identificadores históricos no se sobrescriben.

**Límite conocido:** la configuración de equipamiento actual distingue principalmente ejercicios sin equipo, mancuernas y determinados aparatos de gimnasio. Una variante que requiera silla, pelota, pared, banda, toalla u otro material puede navegarse en Biblioteca, pero **no se supone automáticamente que esté disponible en casa**. Incorporar una comprobación explícita de material por usuario sería una mejora posterior.

La complejidad técnica 1–5 es descriptiva y no equivale a intensidad fisiológica ni justifica aumentos automáticos de repeticiones o series. Las variantes que requieren supervisión siguen excluidas de las selecciones automáticas.

## Referencias de la investigación

- American Council on Exercise (ACE), Biblioteca de ejercicios: https://www.acefitness.org/resources/everyone/exercise-library/
- ACE, estabilidad en cuadrupedia («Bird-dog»): https://www.acefitness.org/resources/everyone/exercise-library/14/bird-dog/
- NHS, ejercicios de fuerza: https://www.nhs.uk/live-well/exercise/strength-exercises/
- NHS, ejercicios sentados: https://www.nhs.uk/live-well/exercise/sitting-exercises/
- NHS, ejercicios de flexibilidad: https://www.nhs.uk/live-well/exercise/flexibility-exercises/
- NHS, preparación aeróbica: https://www.nhs.uk/live-well/exercise/how-to-warm-up-before-exercising/
- NHS, vídeos de ejercicios aeróbicos: https://www.nhs.uk/live-well/exercise/aerobic-exercises/
- Arthritis Foundation, movimientos funcionales de mano: https://www.arthritis.org/health-wellness/healthy-living/physical-activity/other-activities/9-exercises-to-help-hand-arthritis

Las fuentes justifican **la existencia general de las familias de movimiento**; la selección exacta de las variantes, niveles técnicos y grupos primarios es **curaduría interna de Asterion**, no una clasificación clínica validada. Antes de describir una rutina como individualmente segura, se necesita información adicional sobre técnica y limitaciones concretas.

## Control de calidad

- `ExerciseCatalogLoaderTest`, `ExerciseCategoryMapperTest` comprueban jerarquía, IDs, tamaños y cobertura completa.
- `tests/phase16_preflight_static.py` exige la dimensión nueva del inventario.
- `tests/exercise_five_category_expansion_contract.py` comprueba que todas las familias se asignen a los cinco grupos, sin duplicados ni pérdida de registros.
- El CI ejecuta las pruebas del motor, ensamblado y comprobaciones Android.
