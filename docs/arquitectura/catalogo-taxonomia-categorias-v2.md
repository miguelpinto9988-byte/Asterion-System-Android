# Catálogo unificado: categoría → familia → tipo → variante

> **Nota de vigencia (11 de noviembre de 2026):** este documento conserva el registro de la primera ampliación (100 familias, 254 tipos, 917 variantes). La ampliación multidisciplinaria posterior llega a **125 familias, 354 tipos y 1.017 variantes**, usa exclusivamente `tren_superior`, `tren_central`, `tren_inferior`, `movilidad` y `cardio` como categorías principales y añade 56 deportes como etiquetas. Véase [catálogo multideporte de cinco categorías](catalogo-multideporte-cinco-categorias.md).

Estado: propuesta independiente sobre la rama principal de Asterion System Android. Inventario añadido **sin sustituir** IDs existentes, SQLite, iconos, pantallas de carga ni el modo de consentimiento Monarca.

## Problema revisado

La versión anterior organizaba correctamente `families.tsv` → `types.tsv` → `variants.tsv`, pero las cinco categorías operativas del calendario se calculaban en varios lugares: `ExerciseCategoryMapper` para siete etiquetas históricas, bucles de familias en la biblioteca y un índice propio de tipos en `MissionPlanner`. En el catálogo no existía un índice inmutable y público para consultar directamente variantes o tipos por categoría.

## Modelo y relaciones

```
Categoría operativa (5: tren_superior, tronco, piernas, movilidad, cardio)
└── Familia de movimiento (family_id; categoría histórica y patrón)
    └── Tipo de ejercicio (type_id, family_id)
        └── Variante (variant_id, type_id; dificultad 1..5; material, ejecución, supervisión)
            └── Etiqueta de intensidad descriptiva (intensity.tsv; por variant_id)
```

- `ExerciseCategoryIndex` se construye al cargar el catálogo: todas las familias, tipos y variantes deben corresponder a una de las cinco categorías operativas. Expone las listas inmutables `families(category)`, `types(category)` y `variants(category)`, además de las relaciones inversas por ID.
- Los códigos históricos **coordinacion → movilidad** e **integrado → tronco** siguen vigentes. No se crean ejercicios bajo `descanso`.
- `MissionPlanner`, biblioteca de Misiones y `ExerciseMuscleFocus` consultan **el mismo índice**; el método existente `typesInFamily`, `variantsInType` y las búsquedas antiguas permanecen disponibles para compatibilidad.
- La lista se presenta por categoría, luego familia, luego tipo y variantes con etiqueta de intensidad/material/nivel. El contador por categoría muestra los tres niveles jerárquicos.

## Ampliación de la biblioteca

| Grupo | Familias existentes | Nuevas | Total familias | Tipos nuevos | Variantes nuevas |
|---|---:|---:|---:|---:|---:|
| Tren superior | 17 | 5 | 22 | 10 | 20 |
| Tronco | 13 | 5 | 18 | 10 | 20 |
| Piernas | 16 | 5 | 21 | 10 | 20 |
| Movilidad | 15 | 5 | 20 | 10 | 20 |
| Cardio | 14 | 5 | 19 | 10 | 20 |
| **Total** | **75** | **25** | **100** | **50** | **100** |

Resultado: **100 familias, 254 tipos, 917 variantes**, de las que **412** cuentan con intensidad explícita. Las 100 variantes nuevas tienen niveles técnicos 1–2, etiqueta de intensidad baja o moderada, sin recomendación de supervisión, y materiales limitados a `sin_equipo`, `silla`, `pared` y `colchoneta`. Los 817 IDs anteriores permanecen intactos.

El foco corporal orientativo de las 25 familias nuevas se incorpora explícitamente a `ExerciseMuscleFocus`, completando **100 de 100**. Su descripción no es una evaluación de salud ni un diagnóstico. La dificultad técnica tampoco define capacidad personal.

## Referencias para revisión editorial

Se investigaron 40 ejemplos de patrones y variantes organizados en cinco grupos mediante Nimble Web Search Agent. Las fuentes sirven de **referencia general**, no de validación clínica individual de cada nueva variante ni licencia para prescribir repeticiones o cargas.

- <https://www.nhs.uk/live-well/exercise/sitting-exercises/> — movimientos sentados y controlados
- <https://www.nhs.uk/live-well/exercise/strength-exercises/> — ejercicios de fuerza de fácil acceso
- <https://www.acefitness.org/resources/everyone/exercise-library/experience/beginner> — clasificación de ejercicios de nivel inicial
- <https://medlineplus.gov/spanish/ency/patientinstructions/000890.htm> — ejemplos de ejercicios de fuerza y de tronco
- <https://www.mayoclinic.org/es/healthy-lifestyle/fitness/in-depth/stretching/art-20546848> — consideraciones generales de flexibilidad

Las variantes editoriales añadidas se eligieron como movimientos controlados de bajo impacto y deben revisarse con un profesional del ejercicio antes de emplearse como indicaciones individuales. No hay secuencias obligatorias, pesos, objetivos de repeticiones, sesiones extremas ni participación en retos peligrosos.

## Validación y compatibilidad

1. `tests/exercise_category_hierarchy_contract.py`: integridad referencial, unicidad de nombres e IDs, intensidades, cinco categorías, 25 familias nuevas, 50 tipos, 100 variantes y conservación del prefijo de 75/204/817 originales.
2. `ExerciseCategoryIndexTest` (JUnit 4, validación de colecciones inmutables y consultas reversas), además de las pruebas existentes de misión diaria/Monarca y del catálogo.
3. GitHub Actions: contratos estáticos, JUnit, Android Lint, ensamblado debug e instrumentación de Android 16.
4. Ninguna migración de preferencias ni SQLite. Las misiones y las actividades guardadas siguen identificando variantes por sus mismos IDs; nunca se reetiquetan datos ya completados.

## Mejoras posteriores sugeridas

- Revisar cada variante con un especialista en actividad física, agregando descripciones accesibles y contraindicaciones claramente redactadas para cada movimiento.
- Separar las etiquetas clínicas/orientativas de los filtros de entrenamiento y permitir categorías secundarias de actividad sin duplicar IDs ni asignar una misión a dos categorías.
- Añadir navegación y búsqueda explícitas por categoría, familia, patrón, material e intensidad en vistas de la biblioteca, con pruebas de accesibilidad en un teléfono real.
