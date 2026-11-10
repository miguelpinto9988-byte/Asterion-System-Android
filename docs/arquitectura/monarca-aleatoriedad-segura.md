# Monarca — selección reproducible, variedad por ciclo y autorización

**Rama de propuesta:** `feature/monarca-aleatoriedad-estable-20261110` sobre `feature/misiones-niveles-adaptativos-20261110` (PR #8), a su vez basado en PR #7. No se modifica directamente `main`.

## Hallazgos de la revisión
1. El motor anterior usaba `starts.toEpochDay()/5L` como semilla Monarca. La fecha producía variedad, pero **la semilla no incluía la configuración del perfil**. El perfil seguía filtrando qué ejercicios eran elegibles.
2. `MonarchSessionStore.current` renovaba la vista previa después de cinco días y guardaba los identificadores seleccionados, pero **una edición del perfil durante un ciclo ya aceptado restablecía la misión a vista previa**. Eso podía ocultar inesperadamente la misión y sus avances visuales.
3. El antiguo `accept` comprobaba el número de candidatos y la fecha, pero no comparaba todos sus identificadores ni verificaba de nuevo la firma del perfil.
4. La selección anterior diversificaba familias, aunque no reservaba explícitamente variedad por **categorías programadas**.

## Comportamiento implementado
- **Aleatoriedad pseudoaleatoria determinista**, no azar físico: la semilla del ciclo se deriva de SHA-256 con el espacio `asterion.monarch.v3`, fecha inicial y firma del perfil relevante para las misiones.
- El catálogo se filtra **antes** del sorteo. El motor respeta días/áreas programados, equipamiento, complejidad técnica, intensidad permitida y supervisión; no habilita ejercicios descartados.
- El orden se sortea de manera reproducible. Primero se elige una opción compatible por **categoría diferente**, hasta cinco categorías, y luego se completa hasta cinco opciones evitando repetir identificadores.
- Para un **ciclo nuevo**, los IDs del ciclo anterior se ordenan como última preferencia cuando existen alternativas válidas. **No garantiza** evitar toda coincidencia si hay pocas opciones compatibles.
- La vista previa se conserva durante los cinco días del ciclo, aunque se reabra la aplicación. La aceptación continúa requiriendo revisar las cinco opciones y confirmar explícitamente; **no hay activación automática**.
- Una misión ya aceptada conserva sus IDs hasta el final de sus cinco días; si el perfil o los datos compatibles cambian durante el ciclo, pasa a `REVIEW_REQUIRED`. Se bloquea su ejecución hasta que la persona solicite de forma expresa **una nueva previsualización**, que deberá aceptar otra vez.
- Un usuario puede rechazar, abandonar o detener una misión sin penalización. La fecha de vencimiento del ciclo aceptado se cuenta desde **la fecha de aceptación**, no desde la primera vista previa.
- Los niveles técnicos 4–5 siguen **fuera** de toda selección automática. La selección Monarca permanece conservadora: nivel 1 en básico y máximo 2 en intermedio/avanzado; la progresión de mezcla de dificultad del PR #8 corresponde a misiones diarias y no amplía automáticamente este modo especial.
- No hay metas obligatorias, aumento automático de series, peso, repeticiones ni intensidad. Monarca ofrece cinco actividades **a lo largo de cinco días**; no cinco ejercicios obligatorios en una sola sesión.

## Estado local y privacidad
- Monarca conserva un registro ligero de **fecha de vista previa, cinco IDs como máximo, estado, fecha de aceptación y firma de compatibilidad del perfil** en preferencias privadas.
- Se reutiliza el registro previo para evitar repetición en el ciclo siguiente, pero **no se archivan propuestas históricas completas**.
- Los avances voluntarios marcados por el usuario permanecen en `MissionTrackingStore`. Un cambio de propuesta no borra esas marcas ni altera el esquema SQLite.
- La compatibilidad de los cinco IDs guardados se revalida contra el catálogo actual antes de permitir utilizarlos.

## Pruebas y límites
- JUnit `MonarchRandomnessTest`: reproducción de fecha/perfil, diversidad entre múltiples ciclos, preferencia por variantes nuevas, distribución de cinco categorías, elegibilidad y rechazo de IDs corruptos o incompatibles.
- Instrumentación Android: persistencia tras reinicio real del proceso; aceptación explícita; suspensión sin reemplazar una misión activa al editar el perfil; creación de nueva vista previa después de una acción del usuario; vencimiento desde aceptación; rechazo.
- Contrato estático `tests/monarch_randomness_contract.py`.
- Se mantiene la prueba manual pendiente en dispositivo físico y la aprobación del propietario antes de fusionar.

## Nota sobre Fallow Code Analysis
Fallow analiza principalmente proyectos **JavaScript/TypeScript**; este motor de Asterion está implementado en **Java para Android**. No hay análisis de Fallow atribuible a la lógica Monarca. Para esta rama se usan inspección del código Java, pruebas JUnit, contratos de repositorio, Android Lint y emulador; no se confunden esas comprobaciones con resultados de Fallow.
