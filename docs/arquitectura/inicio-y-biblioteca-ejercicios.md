# Pantalla de inicio aprobada y distribución del catálogo de ejercicios

**Nota de actualización (2026-11-10):** esta página es la especificación histórica del primer diseño, anterior a la integración Android. La versión actual de `main` ya incluye la Biblioteca nativa y propuestas de misiones conectadas al catálogo con **817 variantes y cinco categorías de entrenamiento**. Para el inventario y el mapeo en producción, consultar [ampliación vigente](../catalogo-ejercicios/03-ampliacion-cinco-categorias.md). Las frases de “pendiente” en esta página describen su fecha original y no la implementación actual.

Estado: estructura de diseño aprobada el 9 de noviembre de 2026. La pantalla Java/XML de producción todavía necesita adaptar el orden de módulos y vincular los nuevos destinos. Los prototipos HTML no son la APK.

## Orden fijo de la pantalla principal

1. **Misiones Monarca** — módulo de misiones especiales; todavía no existe un motor activo ni deben inventarse misiones.
2. **Tarjeta de perfil** — lectura de la identidad previamente guardada; no crear un perfil duplicado.
3. **Misiones diarias** — futuro panel de tareas; por ahora existen calendario y seguimiento, no una generación automática confirmada.
4. **Progreso y evolución** — entrada al seguimiento real; los registros pendientes no cuentan como fallos ni como progreso 0 %.
5. **Biblioteca de ejercicios** — explorador para la clasificación local de 39 familias, 80 tipos y 445 variantes.

Interfaz: blanco predeterminado; negro alternativo según el tema configurado en el perfil. Las categorías semanales conservan los seis colores y su leyenda original.

- [Pantalla de inicio HTML de 5 módulos](../previews/asterion-inicio-modular-5-ventanas.html)
- [Biblioteca interactiva HTML](../previews/asterion-biblioteca-ejercicios-distribucion.html)

## Distribución exacta del motor de ejercicios v1

| Grupo | Familias | Tipos | Variantes |
|---|---:|---:|---:|
| Piernas y cadera | 9 | 21 | 119 |
| Tren superior | 10 | 21 | 124 |
| Tronco y estabilidad | 5 | 11 | 62 |
| Equilibrio y coordinación | 2 | 4 | 22 |
| Trabajo integrado | 1 | 2 | 10 |
| Actividad cardiovascular | 6 | 9 | 46 |
| Movilidad, respiración y postura | 6 | 12 | 62 |
| **Total** | **39** | **80** | **445** |

Jerarquía: región/función → familia → tipo → variante. Cada variante contiene un nivel técnico del 1 al 5, material, modalidad y una marca de supervisión. Distribución por nivel: 102 / 108 / 96 / 92 / 47. No convertir el nivel en una medida personal de rendimiento.

Fuente de verdad: `progress-engine/src/main/resources/asterion_exercises/v1/{families,types,variants}.tsv` y `ExerciseCatalogLoader`.

## Separación que falta resolver

La pantalla `screen_catalog.xml` todavía consulta `SQLiteCatalogRepository` (tablas SQLite v5) y este catálogo contiene cuatro registros técnicos de demostración. La taxonomía completa TSV se consulta por `AssetExerciseCatalogRepository`, fuera del hilo principal, sin formar parte todavía de esa pantalla. **No afirmar que las 445 variantes ya se puedan navegar desde el APK.**

`planned_activities.catalog_item_id` apunta a `catalog_items.id` mediante clave foránea. Las variantes TSV tienen otros identificadores: no son programables directamente sin adaptador o migración versionada, con integridad referencial y compatibilidad con planes anteriores. No fusionar catálogos por coincidencia de títulos.

## Siguiente integración técnica

- Implementar la vista nativa de Biblioteca como consulta jerárquica de la taxonomía empaquetada.
- Filtros: texto, región, implementos, modalidad y complejidad; con estados vacíos, carga y errores.
- Separar **consultar una ficha** de **añadirla al calendario**; cualquier programación debe pasar por una acción explícita del usuario y el planificador existente.
- Conservar lectura fuera del hilo UI y compatibilidad offline.
- Verificar accesibilidad, búsqueda sin tildes, selección, rotación, rendimiento, persistencia y los temas Blanco/Negro.
- No dar por implementadas las misiones Monarca ni la generación de misiones diarias hasta que tengan contratos y pruebas propias.
