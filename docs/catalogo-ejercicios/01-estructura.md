# Estructura de integración del catálogo de ejercicios v1

**Nota de vigencia (2026-11-10):** este documento registra el diseño inicial del catálogo de 445 variantes. La versión operativa en `main` contiene **49 familias, 100 tipos y 505 variantes**, utiliza `ExerciseCategoryMapper` para las **cinco categorías del perfil**, y ya alimenta propuestas diarias y Monarca. Véase [la ampliación vigente](03-ampliacion-cinco-categorias.md). Los conteos y pendientes históricos de esta página se conservan como referencia de la primera etapa.

## Alcance inicial

Se propone un sistema de datos versionado para **39 familias, 80 tipos y 445 variantes**. Estos elementos son conocimiento descriptivo para una aplicación de rutinas; **no crean rutinas ni programaciones por sí mismos**.

**Jerarquía:** familia de movimiento → tipo concreto → variante ejecutable. Una familia puede contener varios tipos; cada tipo posee varias variantes clasificadas por complejidad técnica.

```text
Aplicación Android (Java/XML)
   ├─ Catálogo actual SQLite v5: se conserva sin cambios
   └─ AssetExerciseCatalogRepository (solo lectura, fuera del hilo UI)
         └─ TSV v1 (families / types / variants)
               └─ ExerciseCatalogLoader (Java 17 puro)
                     └─ ExerciseCatalog (índice inmutable)
                           ├─ families()
                           ├─ typesInFamily(familyId)
                           ├─ variantsInType(typeId)
                           └─ search(texto, categoría, implemento, modalidad, dificultad)
                                  |
                          AsterionDomainEngine (inyección opcional)
```

## Esquema de datos

| Registro | Columnas | Uso |
|---|---|---|
| Familia | `family_id`, `name`, `category`, `pattern` | Organización funcional y patrón motor |
| Tipo | `type_id`, `family_id`, `name` | Forma principal de ejercicio dentro de una familia |
| Variante | `variant_id`, `type_id`, `name`, `level`, `equipment`, `execution`, `supervision` | Clasificación técnica por material, modalidad, complejidad y supervisión |

Los IDs son estables y no contienen texto ejecutable. El separador `+` identifica combinaciones de implementos; los códigos de material y modalidad están normalizados en minúsculas, sin espacios. Los datos usan UTF-8 y se incluyen dentro de la APK, sin permisos de internet.

## Interpretación del nivel

| Nivel | Significado |
|---|---|
| 1 · Iniciación | Apoyos externos o patrones sencillos de coordinación |
| 2 · Básico | Variante convencional con soporte o técnica sencilla |
| 3 · Intermedio | Mayor coordinación o implementos adicionales |
| 4 · Avanzado | Mayor exigencia técnica; requiere orientación |
| 5 · Alta complejidad | Variante de mayor coordinación técnica; requiere orientación |

Los niveles **no** equivalen a peso corporal, fuerza máxima, apariencia física, edad, rendimiento ni un orden obligatorio para todas las personas. La dificultad puede variar según experiencia, acceso a material y estado de salud. La clasificación es provisional y merece revisión de especialistas antes de generar rutinas personalizadas. Los niveles 4 y 5 no son elegibles para sugerencias automáticas.

## Cobertura de v1

| Región / función | Familias | Tipos | Variantes |
|---|---:|---:|---:|
| Piernas y cadera | 9 | 21 | 119 |
| Tren superior | 10 | 21 | 124 |
| Tronco y estabilidad | 5 | 11 | 62 |
| Equilibrio y coordinación | 2 | 4 | 22 |
| Trabajo integrado | 1 | 2 | 10 |
| Actividad cardiovascular | 6 | 9 | 46 |
| Movilidad, respiración y postura | 6 | 12 | 62 |

Distribución por niveles: L1: 102 · L2: 108 · L3: 96 · L4: 92 · L5: 47.

**Inventario completo:** [02-inventario-completo.md](02-inventario-completo.md).

## Compatibilidad con la base de datos y el planificador

La nueva taxonomía no sustituye ni elimina registros de `catalog_items`. En SQLite v5, `planned_activities.catalog_item_id` referencia esas actividades mediante clave foránea; **los IDs del TSV todavía no pueden usarse para guardar planes** sin un adaptador o migración explícita que preserve la integridad referencial. Esta primera etapa solo incorpora infraestructura, assets y API de consulta. No se activan cambios en pantallas ni generación automática de rutinas.

Próxima etapa de integración propuesta: vincular identificadores de variante a actividades persistidas mediante una migración versionada o sincronización local idempotente, revisar referencias existentes, incorporar filtros en la pantalla Catálogo y pruebas instrumentadas en dispositivo. Nada de eso se activa de forma implícita.

## Seguridad, privacidad y fuentes

No se incorporaron prescripciones de series, repeticiones, pesos, calorías, máximos ni tiempos de esfuerzo. No se copiaron instrucciones, fotografías o vídeos externos. Para ejercicio juvenil, las recomendaciones de sociedades deportivas y médicas subrayan supervisión cualificada, adecuación al desarrollo y técnica segura. Las variantes avanzadas son registros informativos, no retos ni objetivos que deban intentarse.

Fuentes consultadas para la **estructura de clasificación** y ejemplos nominales: [ACE Exercise Library](https://www.acefitness.org/resources/everyone/exercise-library/), [NASM Exercise Library](https://www.nasm.org/workout-exercise-guidance/), [AOSSM Youth Strength Training](https://www.sportsmed.org/youth-strength-training), [CDC What Counts](https://www.cdc.gov/physical-activity-basics/adding-children-adolescents/what-counts.html). La asignación concreta de niveles y la traducción de etiquetas son decisiones editoriales de Asterion, pendientes de revisión profesional.

## Control de calidad

- Validación de unicidad e integridad `familia → tipo → variante`, orden de niveles y campos.
- Pruebas JUnit para búsquedas, filtrado de material/modalidad y fallos de datos inválidos.
- `tests/phase16_preflight_static.py` comprueba las **445 variantes** antes de Gradle.
- El workflow CI automático validará la compilación Java y `debug`, Lint y el empaquetado; la ejecución real del commit debe confirmarse con los registros de GitHub.
