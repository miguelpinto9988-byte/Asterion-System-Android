# Misiones: transición gradual por nivel de experiencia

**Rama:** `feature/misiones-niveles-adaptativos-20261110` (PR independiente, basado en la rama de variedad del PR #7).
**Estado:** cambio en revisión; no está integrado en `main` y requiere pruebas finales y aprobación.
**No se cambian** los IDs del catálogo de 817 variantes, SQLite, los registros de esfuerzo, los descansos, el número de actividades, las series/repeticiones, el icono oficial ni los fondos de carga.

## Qué significa "nivel"
El catálogo mide **complejidad técnica** del ejercicio del 1 al 5; esta escala no es igual a intensidad, fuerza, capacidad física ni edad. La selección inicial mantiene el filtro de equipo, categoría semanal, experiencia declarada, intensidad, restricciones de supervisión y recuperación.

| Perfil elegido | Propuesta durante la primera semana | Tras evidencias favorables |
| --- | --- | --- |
| Principiante / básico | Exclusivamente variantes técnicas 1 y 2 | Se mantiene dentro de 1–2; no cambia de nivel de perfil automáticamente |
| Intermedio | Mayoría de variantes 1–2 y una parte del nivel 3 | Etapas 2 y 3 aumentan moderadamente la proporción de nivel 3 |
| Avanzado / difícil | Mezcla prioritaria de nivel 2 con una porción de nivel 3 | Etapas 2 y 3 dan más espacio al nivel 3, sin superar filtros de seguridad |

Con tres actividades compatibles, el objetivo inicial del intermedio es **2 sencillas y 1 intermedia**. Tras dos revisiones semanales de facilidad real, el objetivo puede llegar a **1 sencilla y 2 intermedias**. En avanzado, el objetivo también parte de un predominio del nivel 2 y puede pasar a mayor proporción del 3. Si el usuario configura una o dos actividades, los porcentajes no pueden reproducirse de forma exacta.

Ejemplo verificable en el catálogo: **flexiones con rodillas apoyadas** `flexiones_v03`, complejidad 2, requieren colchoneta; **flexiones estándar** `flexiones_v04`, complejidad 3, sin equipo. Solo se convierten en candidatos cuando se cumplen los filtros, incluido declarar la colchoneta en el perfil para la primera variante. No se exige realizar ninguna de ellas.

**Los niveles técnicos 4 y 5 y las variantes marcadas para supervisión quedan excluidos de las sugerencias automáticas**, incluso en el perfil avanzado. Siguen disponibles solo como referencias catalogadas. El algoritmo jamás aumenta automáticamente intensidad, series, repeticiones, carga o sesiones.

## Regla de ascenso de etapa
Las tres etapas son **proporciones del sorteo**, no ascensos de experiencia. Una persona que eligió Intermedio sigue siendo Intermedio. Un cambio posterior de nivel en el perfil restablece las etapas de esta función (no borra seguimiento).

1. La primera visita a Misiones después de seleccionar un nivel registra la fecha local de entrada. Los primeros **7 días** comienzan en la etapa 1; el paso del tiempo por sí solo no provoca ascensos.
2. Cada categoría semanal (superior, tronco, piernas, movilidad, cardio) mantiene su etapa independiente.
3. Desde el día 8, una categoría solo puede avanzar una etapa si la revisión semanal contiene **al menos dos sesiones completadas y valoradas como fáciles** (1–3/10), sin reportes de esfuerzo alto, y no existe una interrupción reciente en esa categoría.
4. Se requiere un intervalo **mínimo de 7 días** entre aumentos; solo hay tres etapas y no existen ascensos ilimitados.
5. Un reporte de esfuerzo alto conserva la adaptación previa que reduce la cantidad de opciones y vuelve a las variantes técnicas más sencillas. Una pausa clínica o de categoría bloquea las propuestas; no se reactivan automáticamente.
6. Durante un mismo día, la ficha de IDs ya guardada sigue siendo la misma, aunque se haya registrado una valoración después de verla. Un día nuevo puede utilizar una proporción actualizada.

La medida de "muy fácil" se basa en **actividades voluntariamente marcadas como realizadas y valoradas por la misma persona**. No basta con abrir la pantalla, ni con completar ejercicios de otra categoría, ni con que transcurra una semana.

La fecha inicial corresponde a la **primera consulta de Misiones** tras configurar el nivel, no necesariamente al instante de crear o editar el perfil. Por seguridad, perfiles ya existentes comienzan en la etapa 1 cuando se instale esta mejora.

## Compatibilidad y datos
- La selección se calcula localmente con la semilla reproducible de fecha y firma de preferencias de entrenamiento.
- `DailyMissionStore` sigue guardando solo fecha, firmas e IDs de la misión del día.
- `MissionLevelProgressStore` guarda únicamente el nivel seleccionado, fecha de primera consulta, etapa 0–2 y fecha del último cambio por categoría. No duplica sesiones, notas, información personal ni datos históricos.
- Se agregan tres casillas **opcionales** al paso 2: colchoneta, pared y silla estable. Si no se marcan, el motor no asume que el material está disponible.
- La misión Monarca conserva su límite previo de complejidad por perfil y el consentimiento obligatorio; sus reglas no se elevan por la mezcla diaria.

## Pruebas incluidas
- JUnit sobre la biblioteca real: básico 1–2; intermedio 2+1 inicialmente y 1+2 después; avanzado 2+1 y 1+2; esfuerzo alto vuelve a dificultad 1 y menos actividades; recursos 4–5 quedan fuera; restablecer un pin del día no cambia.
- Contrato fuente `tests/mission_level_mix_contract.py`.
- Android instrumentado: etapa inicial conservada entre procesos, primera semana sin ascenso, interrupción bloquea, categoría ajena no influye, dos revisiones semanales fáciles para las etapas 2–3 y cambio de nivel reinicia etapas.
- Continúan las pruebas de persistencia de misión, filtros de intensidad y arranque Android 16 heredadas de la rama de variedad.

## Mejoras que podrían valorarse después de validar
1. Un resumen más claro por categoría: «etapa inicial», «variedad intermedia disponible» y «sin datos suficientes»; facilita explicar los cambios sin presionar por ascender.
2. Revisión editorial de equivalencias entre variantes (no asumir que todos los movimientos con complejidad 3 son apropiados para todas las personas).
3. Pruebas en teléfonos reales, accesibilidad y cambio de fecha local real; las pruebas automatizadas simulan la entrada de fecha sin alterar el reloj.
