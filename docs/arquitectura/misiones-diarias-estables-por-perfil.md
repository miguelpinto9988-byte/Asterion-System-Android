# Misiones diarias: aleatoriedad estable por fecha y perfil

**Fecha:** 10 de noviembre de 2026 · **Implementación:** rama `main`.

## Comportamiento esperado

La Biblioteca actual contiene 75 familias, 204 tipos y 817 variantes. El motor utiliza **solo** los candidatos compatibles con la categoría del día, equipamiento, experiencia, intensidad orientativa y reglas de recuperación. El historial de esfuerzo continúa segregado por categoría.

### Regeneración

| Acción | ¿Cambia la misión diaria? | Justificación |
| --- | --- | --- |
| Abrir Misiones diarias varias veces hoy | No | Se reutilizan los IDs de la fecha vigente |
| Cambiar al panel de Monarca, Biblioteca o Progreso y regresar | No | No se genera un nuevo sorteo por navegación |
| Cerrar la aplicación o reiniciar el dispositivo durante el mismo día | No | Se conserva una ficha local mínima |
| Marcar actividades realizadas o valorar esfuerzo durante la jornada | No | El seguimiento no invalida las propuestas pendientes del mismo día |
| Llegar a un día local diferente | Sí | Nuevo cálculo basado en fecha y perfil |
| Editar calendario, categoría, intensidad, objetivo, experiencia, número de actividades, equipamiento o modalidad de rutina | Sí | Cambia la firma de configuración, y se reemplaza la ficha del día |
| Cambiar solamente tema Blanco/Negro o aspecto visual | No | La apariencia no modifica la idoneidad de ejercicios |
| Señalar una lesión o malestar | Puede detener o reducir la actividad | Las reglas de seguridad tienen prioridad sobre la estabilidad |

## Qué se guarda y qué NO se guarda

`DailyMissionStore` crea **una sola ficha ligera** en preferencias locales:
- fecha local, por ejemplo `2026-11-10`;
- huella SHA-256 de las preferencias relevantes del perfil, sin guardar su texto original;
- huella del catálogo empaquetado, para invalidar IDs obsoletos tras una actualización;
- lista de **solo los identificadores** de los ejercicios seleccionados para ese día (máximo siete, usualmente menos).

No se copian nombres, descripciones, niveles, materiales, series, repeticiones, historial completo de sesiones ni filas enteras de los TSV. Cuando cambia la fecha o el perfil, la nueva ficha sustituye la anterior. El **historial de actividades completadas y esfuerzo** sí se mantiene por separado, pues es necesario para la evolución de la persona.

Los ejercicios se reconstruyen desde `ExerciseCatalog` y se vuelven a validar mediante `MissionPlanner.restoreDaily`. Si la ficha estuviera corrupta o ya no fuese compatible, se genera una nueva en vez de mostrar ejercicios incorrectos.

## Aleatoriedad reproducible

`MissionPlanner` calcula una semilla estable desde el día local y la huella de configuración mediante SHA-256. Una permutación pseudoaleatoria de los ejercicios elegibles evita que el catálogo aparezca siempre en un orden fijo. El motor mantiene preferencia por **familias de movimiento distintas** para diversificar la sesión.

Esto no implica aleatoriedad criptográfica ni un modelo de inteligencia artificial entrenado. **Es una selección pseudoaleatoria determinista**: misma fecha + mismo perfil + mismo catálogo + ficha vigente ⇒ mismos ejercicios; nueva fecha o configuración ⇒ nuevo sorteo (que por coincidencia puede conservar algún ejercicio).

## Intensidad en el perfil

Se agregó en el paso 2 de registro el campo `intensity` con dos opciones:
- **Baja:** selecciona únicamente variantes nuevas etiquetadas como `baja`, compatibles con el resto del perfil.
- **Moderada (predeterminada):** combina etiquetas `baja`, `moderada` y las 505 variantes anteriores que no tienen intensidad clasificada.

Las variantes explícitamente `alta` o `variable` permanecen en la Biblioteca como información, pero no se asignan automáticamente a una misión sin datos de seguridad adicionales. La dificultad técnica de 1 a 5 no es idéntica a la intensidad física.

## Interacción con el progreso por categoría

Para el análisis de 7 días se toman valoraciones de esa categoría **solo cuando existe una actividad completada perteneciente a esa misma categoría**. Para variantes anteriores se usa la última lista de identificadores **completados** dentro de los 21 días anteriores. No se guardan listas completas de propuestas diarias históricas. Los antiguos snapshots de propuestas se purgan una vez; se conservan las valoraciones, registros y datos SQLite existentes.

Las adaptaciones por esfuerzo alto o interrupciones continúan aplicándose antes de mostrar la sesión. Ninguna ficha diaria debe obligar a entrenar ante síntomas o sustituir el descanso.

## Pruebas

- `DailyMissionStabilityTest`: consistencia del mismo día, diversidad entre fechas, variaciones de firma, categorías, filtro de intensidad, rehidratación desde el catálogo de 817 variantes y exclusión de alta intensidad.
- `tests/daily_mission_stability_contract.py`: integración del almacenamiento compacto, invalidaciones de perfil y aislamiento del historial.
- `tests/weekly_category_review_contract.py`: valoración y finalización realmente asociadas a la categoría.

### Mejoras recomendadas, no incluidas

1. Agregar etiquetas de músculos principales y secundarios, para diversificar patrones sin agrupar movimientos no equivalentes.
2. Implementar pruebas instrumentadas de restauración tras reinicio real y cambio de día en un teléfono.
3. Revisar los materiales del perfil (pared, colchoneta, banda, etc.) mediante un selector explícito para aumentar las opciones compatibles sin suponer equipos disponibles.
