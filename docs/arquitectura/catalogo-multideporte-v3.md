# Catálogo multideporte: disciplinas transversales y ampliación de ejercicios

**Rama de revisión:** `feature/catalogo-multideporte-disciplinas-20261110`. Esta extensión depende de `feature/catalogo-taxonomia-categorias-v2-20261110` (PR #11), que todavía no se ha integrado en `main`.

## Objetivo y alcance real

Catalogar movimientos reconocibles de calistenia, gimnasio, atletismo, fútbol, baloncesto, hockey, voleibol, balonmano, deportes de raqueta, béisbol y sóftbol, patinaje, pilates, yoga, natación y otras actividades recreativas. No existe una base única y exhaustiva de **todos** los ejercicios de Internet; la investigación y los datos añadidos son una curaduría editorial ampliable.

**Resultado acumulado de esta rama:** 130 familias, 374 tipos, 1157 variantes y 652 etiquetas de intensidad explícitas. Esta aportación añade **30 familias, 120 tipos, 240 variantes**, distribuidas de manera equilibrada en las cinco categorías operativas, y 374 filas de etiquetas deportivas. Se conserva sin alterar todo el catálogo inicial de 100 familias / 254 tipos / 917 variantes, así como los identificadores de antes.

## Modelo de clasificación

```
Categoría primaria (5 categorías de calendario; nunca "descanso")
  Familia de movimiento (family_id)
    Tipo único (type_id)
      Variantes (variant_id; dificultad, equipo, ejecución, orientación)
      Intensidad descriptiva (intensity.tsv; no prescribe carga)
      Etiquetas deportivas (disciplines.tsv, por type_id, 1 a 8 etiquetas)
```

**La disciplina no sustituye a la categoría**. El mismo tipo puede ser pertinente a varias disciplinas, sin duplicar sus variantes, progreso ni historial. Por ejemplo, una dominada existente se identifica desde calistenia y desde gimnasio, pero sigue siendo el mismo `type_id`. Los códigos heredados `coordinacion` e `integrado` siguen resolviéndose a `movilidad` y `tronco`.

`ExerciseDisciplineIndex` conserva índices inmutables de disciplinas → tipos / variantes y el rol orientativo del movimiento (fuerza, habilidad, acondicionamiento, movilidad o coordinación). `ExerciseCatalogLoader.load(f,t,v,i,d)` analiza el quinto TSV opcional: la API anterior con tres o cuatro lectores continúa funcionando sin estos tags. `AssetExerciseCatalogRepository` carga `disciplines.tsv` de los mismos recursos locales empaquetados, sin red ni base de datos adicional.

La búsqueda `ExerciseCatalog.searchByDiscipline(...)` cruza disciplina, texto normalizado sin acentos, categoría, material, ejecución, dificultad técnica y paginación. En Android la biblioteca contiene **Explorar por deporte o disciplina y material**, elección de equipo, búsqueda de nombre y páginas de resultados. Las intensidades altas y variables y la complejidad técnica avanzada no se ofrecen como actividades automáticas.

## Inventario por categoría operativa

| Categoría | Familias tras esta ampliación | Familias nuevas | Tipos nuevos | Variantes nuevas |
|---|---:|---:|---:|---:|
| Tren superior | 28 | 6 | 24 | 48 |
| Tronco | 24 | 6 | 24 | 48 |
| Piernas | 27 | 6 | 24 | 48 |
| Movilidad | 26 | 6 | 24 | 48 |
| Cardio | 25 | 6 | 24 | 48 |
| **Total** | **130** | **30** | **120** | **240** |

Se incluyeron como *códigos de disciplina para búsqueda*: `general`, `gimnasio`, `calistenia`, `atletismo`, `futbol`, `baloncesto`, `hockey`, `voleibol`, `balonmano`, `tenis`, `badminton`, `padel`, `tenis_mesa`, `beisbol`, `softbol`, `patinaje`, `natacion`, `ciclismo`, `remo`, `yoga`, `pilates`, `danza`, `senderismo`, `educacion_fisica` y `entrenamiento_funcional`.

### Materiales y contextos

El filtro de equipo usa el inventario real de cada `ExerciseVariant`: `sin_equipo`, `pared`, `silla`, `colchoneta`, `banda`, `barra_dominadas`, `maquina_gimnasio`, `raqueta`, `pelota`, `balon`, `balon_futbol`, `balon_voleibol`, `palo_hockey`, `testigo_relevo` y otros códigos ya existentes. Los códigos son descriptivos, no enlaces a proveedores ni recomendaciones de compra. Las misiones requieren que el perfil declare el material disponible; un ejercicio incompatible no se presenta automáticamente.

### Nombres conocidos de entrenamiento exigente

Se incluyen dos referencias editoriales: `burpee_adaptado` (transición adaptada, sin entrenamiento de agotamiento) y `carreras_ida_vuelta_controladas` (versión recreativa del desplazamiento de ida y vuelta, sin cronometrar ni imponer máximos). Los ejemplos más exigentes se etiquetan con **intensidad variable** para excluirlos de sugerencias automáticas. No se generan repeticiones, tiempos máximos, cargas, retos ni penalizaciones; el catálogo no sustituye la supervisión del entrenamiento deportivo.

## Investigación y criterios de procedencia

Se realizó una investigación de amplitud utilizando Nimble Web Search Agent, con **100 fichas de ejercicios encontradas en diferentes disciplinas** y varias decenas de páginas identificadas. Se contrastó la arquitectura de las pruebas JUnit 4 mediante Context7. El resultado investigado no equivale a una auditoría exhaustiva de Internet ni a una verificación clínica de cada nueva etiqueta. Los nombres y variantes se adaptaron editorialmente, evitando copiar pasajes de las fuentes. Algunas fichas investigadas eran movimientos ya existentes y fueron descartadas para no duplicarlos.

**Referencias editoriales principales para contrastar la terminología:**

- [ACE Fitness, biblioteca general de ejercicios](https://www.acefitness.org/resources/everyone/exercise-library/) — categorías de fuerza, técnica y material
- [ACE, ejercicios con barra de dominadas](https://www.acefitness.org/resources/everyone/exercise-library/equipment/pull-up-bar/) — calistenia y agarres
- [FIFA Training Centre, control del balón](https://www.fifatrainingcentre.com/en/practice/grassroots/8-to-12/dribble-with-control.php) — fútbol formativo
- [Jr. NBA, planes iniciales](https://jr.nba.com/basketball-practice-plans/starter/) — bote, pase, apoyo y habilidades básicas
- [USA Volleyball, planes de enseñanza](https://usavolleyball.org/resources-for-coaches/lesson-plans/) — recepción, colocación y saque
- [USA Hockey, planes por nivel](https://www.usahockey.com/goaltendingplans) — movimientos básicos y patrones de coordinación
- [World Athletics Kids' Athletics](https://worldathletics.org/es/kids-athletics/teaching-athletics/sprinting-running-relay) — coordinación y relevos
- [Concept2, técnica de remo](https://www.concept2.com/training/rowing-technique) — técnica general y control postural
- [Concept2, ejercicios de técnica](https://www.concept2.com/training/rowing-drills) — gestos desglosados de remo
- [NHS, ejercicios de fuerza generales](https://www.nhs.uk/live-well/exercise/strength-exercises/) — patrones de fuerza accesibles
- [USA Volleyball, habilidades específicas](https://usavolleyball.org/resources-for-athletes/volleyball-specific-skills/) — técnicas de voleibol

Las páginas pueden cambiar de disponibilidad. El uso de una fuente como referencia no implica aprobación de cada adaptación. Ninguna secuencia clínica o instrucción de alto riesgo se incorpora en la aplicación.

## Conservación de compatibilidad

- Los 100/254/917 registros y los 412 tags de intensidad anteriores se conservan exactamente como prefijo del TSV; 30/120/240/240 nuevos registros son aditivos.
- `DailyMissionStore`, preferencias, SQLite, Splash, registro, consentimiento Monarca y recursos gráficos no requieren migraciones ni cambios.
- Las etiquetas deportivas son locales, descriptivas y no afectan la semilla pseudoaleatoria de misiones.
- Las intensidades `alta` y `variable` mantienen la exclusión que ya implementa `ExerciseCatalog.eligibleForAutomaticSuggestion`.
- El rol `habilidad` distingue gestos técnicos deportivos de patrones generales, aunque no prescribe práctica, velocidad ni condiciones.

## Pruebas y revisión antes de integrar

1. `tests/exercise_multisport_contract.py`: 25 disciplinas, 374 tipos etiquetados, integridad referencial, 240 variantes nuevas, exclusiones de seguridad, roles y enlace en Android.
2. Contratos estáticos previos y `ExerciseDisciplineIndexTest` (JUnit 4): cinco categorías, filtros por deporte/material/texto, búsquedas históricas, resultados inmutables y compatibilidad con lectores antiguos.
3. Workflow GitHub Actions: tests, Android Lint, ensamblado y emulador Android 16 (persistencia de misiones, consentimiento/pausa Monarca).
4. Revisión editorial pendiente: validar nombres, equipo y seguridad con especialistas por deporte. Las habilidades específicas y adaptaciones no son instrucciones para práctica autónoma.

## Próximas mejoras recomendadas

- Añadir una descripción didáctica revisada y metadatos de contexto (instalación, compañero, nivel de instrucción) por variante, de modo que el usuario distinga ejercicios de consulta y actividades adecuadas para una sesión.
- Añadir traducciones y sinónimos localizados (por ejemplo, `futbol`/soccer y `baloncesto`/basketball) sin generar IDs duplicados.
- Ofrecer un filtro explícito "solo consultar" / "candidatos compatibles" separado del filtro de intensidad para evitar confusión.
- Realizar una revisión física de interfaz con accesibilidad, búsquedas y paginación, antes de convertir este PR en una actualización distribuible.
