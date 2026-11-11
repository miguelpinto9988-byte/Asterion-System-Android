# Catálogo multideporte: cinco categorías únicas

## Regla principal

Cada movimiento registrado en Asterion tiene exactamente **una** categoría principal y un encadenamiento jerárquico inequívoco:

```text
Categoría principal (5 únicas)
└── Familia de movimiento (family_id)
    └── Tipo de ejercicio (type_id)
        └── Variante (variant_id: equipo, complejidad, supervisión, intensidad)
```

Las categorías principales y sus códigos canónicos son:

1. `tren_superior` — tren superior
2. `tren_central` — tren central
3. `tren_inferior` — tren inferior
4. `movilidad` — movilidad, flexibilidad, yoga, pilates y equilibrio
5. `cardio` — actividad cardiovascular, desplazamientos y resistencia general

**El descanso no es una categoría de ejercicios**: es una opción del calendario. La posición semanal antigua se preserva: 0 superior, 1 central, 2 inferior, 3 movilidad, 4 cardio, 5 descanso.

Para compatibilidad, las consultas antiguas se normalizan mediante `ExerciseCategoryMapper`:
- `tronco` e `integrado` → `tren_central`
- `piernas` → `tren_inferior`
- `coordinacion` → `movilidad`

Los identificadores persistidos de ejercicios y el índice semanal del perfil **no se cambian**. Los registros TSV de familias usan exclusivamente los cinco códigos canónicos.

## Deportes: filtros, no categorías nuevas

`sport_registry.tsv` declara **56 deportes y modalidades identificables** por código y nombre. `sport_tags.tsv` vincula un tipo de ejercicio con múltiples deportes, un objetivo descriptivo, una fuente HTTPS de referencia y el indicador de revisión manual; el mismo movimiento no se replica por deporte.

Ejemplos de cobertura:

- Deportes colectivos: fútbol, futsal, básquet, voleibol, balonmano, rugby, fútbol americano, hockey de hielo y césped, béisbol, sóftbol, críquet, lacrosse y ultimate
- Raqueta: tenis, pádel, bádminton, squash y pickleball
- Atletismo y resistencia: atletismo, marcha, carrera, trail, triatlón, ciclismo, ciclismo de montaña y senderismo
- Acuáticos: natación, waterpolo, remo, kayak, canotaje y surf
- Control corporal: calistenia, gimnasia, parkour, yoga, pilates, taichí, escalada, gimnasio y entrenamiento funcional
- Modalidades adaptadas: deporte adaptado, básquet y rugby en silla de ruedas
- Otras: esquí, snowboard, patinaje, danza, equitación, golf, tiro con arco y preparación física sin contacto

El objetivo de un deporte como etiqueta es ayudar a **encontrar movimientos transferibles**, no afirmar que una misma actividad representa un programa oficial de cada disciplina.

## Ampliación realizada

La consulta estructurada con Nimble recopiló 123 candidatos de distintas fuentes. Se curaron **100 nuevos tipos de movimiento**, incluidos burpee y carrera de ida y vuelta, ambos reservados para revisión manual. Adicionalmente se añadieron etiquetas deportivas a **25 tipos ya existentes** (como sentadillas, dominadas, flexiones, marcha y planchas) para ofrecer búsqueda cruzada **sin duplicar sus ejercicios**. Las 125 fichas deportivas apuntan a los mismos tipos y variantes del catálogo.

| Inventario | Antes del PR #11 | Catálogo v2 inicial | Multideporte |
|---|---:|---:|---:|
| Familias | 75 | 100 | **125** |
| Tipos | 204 | 254 | **354** |
| Variantes | 817 | 917 | **1.017** |
| Etiquetas de intensidad | — | 412 | **512** |
| Deportes identificados | — | — | **56** |

La tabla curada `docs/investigacion/multideporte-curado-20261111.tsv` conserva, por movimiento, nombre original en español, categoría, familia, complejidad técnica, material, intensidad, deportes asociados, objetivo y URL exacta de la fuente. Se consultaron recursos de **ACE Fitness, NHS, World Athletics, USA Swimming y programas de preparación física**, además de materiales especializados; las fuentes no sustituyen la revisión editorial/biomecánica individual.

Ejemplos de referencias incluidas:
- https://www.acefitness.org/resources/everyone/exercise-library/32/front-plank/
- https://www.acefitness.org/resources/everyone/exercise-library/306/burpee/
- https://www.nhs.uk/live-well/exercise/strength-exercises/
- https://www.nhs.uk/live-well/exercise/sitting-exercises/
- https://www.nhs.uk/live-well/exercise/balance-exercises/
- https://usaswimming.org/docs/default-source/coaching-resourcesdocuments/adm-competence-progressions.pdf
- https://worldathletics.org/download/downloadnsa?filename=77d72573-46b5-43ad-8291-3f4f4f3026ce.pdf&urlslug=warm-up-essentials

Los enlaces son referencias editoriales, no autorización para reproducir sus textos.

## Estructura y seguridad en Java

- `ExerciseCategoryIndex`: vistas inmutables por las cinco categorías y consultas inversas de familia/tipo/variante
- `ExerciseSportIndex`: registros de deporte, fuentes de referencia, objetivo, búsqueda y bandera `manualOnly`
- `ExerciseCatalog.search`: búsqueda combinada por texto, categoría, equipamiento, ejecución, deporte, nivel técnico y paginación
- `MissionPlanner`: selección por las cinco categorías existentes, sin duplicar etiquetas deportivas ni cambiar el registro semanal
- `MissionsActivity`: biblioteca por familia, exploración adicional por deporte y por material
- `AssetExerciseCatalogRepository`: carga local sin Internet ni cambios en SQLite

**Distinción de seguridad:** registrar un ejercicio NO implica recomendar su práctica. Las variantes complejas, de alto impacto o contextos especiales (acuáticos, ecuestres, equipo específico) se marcan con supervisión/revisión manual y nunca entran en sugerencias automáticas. No se prescriben repeticiones, cargas, distancias ni retos de intensidad. El catálogo conserva los filtros existentes del perfil, el consentimiento explícito para Monarca y los límites de misiones.

## Verificación

- `tests/multisport_catalog_contract.py`: cinco categorías únicas, integridad referencial, 100 tipos multideporte, 56 códigos deportivos, fuentes, equipo, intensidades, exclusión de propuestas automáticas y conservación de IDs antiguos
- `ExerciseSportIndexTest`: lectura real TSV, filtro combinado, errores de código desconocido, inmutabilidad y exclusión manual
- Contratos anteriores de taxonomía, progresión y variedad: recuentos actualizados sin perder la comprobación de las 25 familias y 100 variantes de la etapa previa
- Gradle/Android Lint y pruebas instrumentadas de arranque, misiones diarias y Monarca

## Próximas mejoras recomendadas

1. Ampliar referencias verificadas a federaciones menos representadas para diversificar ejercicios verdaderamente específicos de cada deporte.
2. Añadir revisión técnica individual de cada variante, incluidos riesgos, accesibilidad y alternativas más sencillas, antes de recomendarla a una persona concreta.
3. Incluir búsqueda con texto libre y combinación simultánea de deporte/material/nivel en la interfaz, respetando accesibilidad y rendimiento.
4. Evaluar carga de catálogo con miles de registros usando índices persistentes cuando el tamaño futuro lo justifique.
