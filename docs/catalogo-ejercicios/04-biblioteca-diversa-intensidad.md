# Biblioteca Asterion — ampliación diversa y etiquetado de intensidad

**Estado:** implementado en `main` el 10 de noviembre de 2026. La verificación compilada debe comprobarse en el CI asociado al commit final.

## Inventario operativo

La taxonomía sigue siendo **familia → tipo → variante**, con 4 TSV locales empaquetados en la APK:

- `families.tsv`: 75 familias.
- `types.tsv`: 204 tipos.
- `variants.tsv`: 817 variantes.
- `intensity.tsv`: 312 etiquetas explícitas de intensidad.

La ampliación agrega **26 familias, 104 tipos y 312 variantes**, desde las **49 / 100 / 505** anteriores. Las 505 variantes antiguas se han preservado con los mismos IDs, posición y campos. No hay migración de SQLite ni reemplazo de misiones ya guardadas.

| Categoría operativa del perfil | Familias | Tipos | Variantes |
| --- | ---: | ---: | ---: |
| Tren superior | 17 | 45 | 196 |
| Tren central | 13 | 37 | 144 |
| Tren inferior | 16 | 45 | 191 |
| Movilidad | 15 | 40 | 156 |
| Cardio | 14 | 37 | 130 |
| **Total** | **75** | **204** | **817** |

**Descanso** no forma parte del catálogo: es una opción distinta del calendario semanal. Se preservan sus colores y los demás códigos existentes.

## Intensidad orientativa (no equivale a dificultad técnica)

Las 312 variantes nuevas se clasifican así:

| Intensidad | Nuevas variantes | Descripción |
| --- | ---: | --- |
| Baja | 173 | Movimientos normalmente suaves o con baja exigencia cardiovascular, según condiciones |
| Moderada | 87 | Actividades de exigencia media orientativa |
| Alta | 17 | Actividades deportivas vigorosas informativas, excluidas de misión automática |
| Variable | 35 | La exigencia depende especialmente de carga, ritmo o contexto; excluidas de misión automática |
| Sin clasificar | 505 | Variantes anteriores conservadas sin inferir intensidad artificialmente |

**No confundir las etiquetas:** `level` (1–5) ya existía y expresa complejidad técnica; `intensity` es una nueva clase descriptiva independiente. Ambas son aproximaciones editoriales, no pruebas de seguridad ni indicadores personales. Una misma actividad puede percibirse de forma muy diferente según la persona, tal como señalan las guías del CDC.

Las variantes con intensidad `alta` o `variable` se pueden consultar en Biblioteca. **El motor `MissionPlanner` no las incluye automáticamente** en misiones diarias o Monarca. Las variantes nuevas de nivel 4 también conservan `supervision=1`.

Las 505 variantes originales siguen con clase `sin_clasificar` para no alterar su descripción sin revisión. Se preserva su lógica de elegibilidad previa, con filtros de material, categoría, experiencia y supervisión.

## Familias añadidas (26)

### Tren superior (5)

- Control del serrato y cintura escapular.
- Press horizontal con apoyos y ángulos.
- Remos con soporte y resistencia.
- Pronación y supinación del antebrazo.
- Coordinación de brazos con pelota.

### Tren central (5)

- Pilates de control en suelo.
- Estabilidad de tronco de pie.
- Patrones diagonales del tronco.
- Posturas suaves de yoga y control postural.
- Apoyos laterales y control del costado.

### Tren inferior (5)

- Isometrías de piernas y apoyos estables.
- Control lateral de cadera acostado.
- Movimiento de rodilla mediante deslizamiento.
- Pasos funcionales en distintas direcciones.
- Saltos recreativos catalogados: la variante vigorosa es informativa.

### Movilidad (5)

- Flexibilidad de manos y antebrazos.
- Movilidad torácica de pie.
- Flexibilidad de cadera en el suelo.
- Equilibrio de talón y punta.
- Yoga de movilidad y posturas restaurativas.

### Cardio (6)

- Natación y actividades aeróbicas acuáticas.
- Bailes recreativos de diferentes estilos.
- Actividades recreativas con pelota.
- Saltos recreativos con cuerda.
- Deportes recreativos de raqueta.
- Recreación aeróbica al aire libre.

No se han copiado ilustraciones, vídeos ni textos de instrucciones de fuentes externas. Los nombres españoles, equipamientos y niveles se redactaron como clasificación propia de Asterion, inspirada en familias de actividades reconocibles. Los registros son **datos informativos**, no una prescripción de series, repeticiones, velocidades o saltos.

## Integración Java y Android

1. `ExerciseIntensity` define `LOW`, `MODERATE`, `HIGH`, `VARIABLE` y `UNCLASSIFIED`.
2. `ExerciseCatalogLoader` admite un cuarto TSV opcional y conserva `load(families,types,variants)` para compatibilidad.
3. `ExerciseCatalog` vincula metadatos por ID de variante y ofrece `eligibleForAutomaticSuggestion` con exclusión explícita de `HIGH` y `VARIABLE`.
4. `AssetExerciseCatalogRepository` carga los cuatro TSV fuera del hilo principal.
5. `MissionPlanner` consulta la nueva elegibilidad además de los filtros existentes por perfil, equipo y nivel.
6. `MissionsActivity` presenta la intensidad y permite filtrar las variantes por baja, moderada, alta, variable o sin clasificar. Ver una variante de intensidad alta no activa ninguna misión.

Las etiquetas describen **actividades típicas**, no aseguran una intensidad concreta: esta varía por técnica, duración, terreno, equipo, carga y respuesta del usuario. Para rutinas individuales de mayor intensidad hacen falta más controles que una clase estática.

## Investigación y fuentes generales

- [American Council on Exercise (ACE) — Exercise Library](https://www.acefitness.org/resources/everyone/exercise-library/): diversidad de movimientos, implementos y regiones del cuerpo.
- [ACE — Full Body / Integrated](https://www.acefitness.org/resources/everyone/exercise-library/body-part/full-body-integrated/full-body-integrated/): movimientos integrados y complejidad técnica.
- [NHS — Exercise](https://www.nhs.uk/live-well/exercise/): movilidad, fuerza, equilibrio, actividad aeróbica y variedad.
- [NHS — How to improve strength and flexibility](https://www.nhs.uk/live-well/exercise/how-to-improve-strength-flexibility/): distinción de ejercicio de fuerza y flexibilidad.
- [CDC — How to Measure Physical Activity Intensity](https://www.cdc.gov/physical-activity-basics/measuring/): esfuerzo relativo, moderado y vigoroso.
- [CDC — What Counts for Children and Teens](https://www.cdc.gov/physical-activity-basics/adding-children-adolescents/what-counts.html): ejemplos de actividades físicas variadas y adecuadas a la edad.

Estas referencias respaldan el **marco de clasificación**, no validan individualmente cada nivel técnico ni intensidad editorial de las 312 variantes nuevas.

## Calidad y límites

- Se comprueba la unicidad de IDs y nombres, filiación `familia → tipo → variante`, orden de niveles, metadatos sin IDs huérfanos y asignación a las cinco categorías.
- `ExerciseIntensityCatalogTest` confirma que las intensidades altas no puedan asignarse automáticamente, incluso si una variante es de nivel técnico bajo.
- El archivo histórico `02-inventario-completo.md` describe la colección inicial de 445 variantes, no esta versión operativa.
- Queda recomendado crear un campo **músculos principales/secundarios** y etiquetas de **impacto** para mejorar la precisión al generar misiones y revisar la clasificación técnica con profesionales cualificados.
