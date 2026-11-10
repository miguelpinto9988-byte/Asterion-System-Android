# Centro Asterion — arquitectura del acompañamiento adaptativo

**Estado:** propuesta técnica y visual, no sistema de IA productivo.
**Destino de cambios futuros:** rama principal `main`.
**Previsualización:** [Prototipo HTML interactivo](../previews/centro-asterion-asistente-adaptativo.html).
**Última revisión de diseño:** noviembre de 2026.

## 1. Inventario real de la aplicación

- La APK usa Android nativo Java/XML.
- Los componentes actuales de `MainActivity` ofrecen navegación, catálogo SQLite, calendario, seguimiento y preferencias.
- `progress-engine/AsterionDomainEngine` ya separa catálogo, planificación, configuración, progreso y estrategias de evaluación semanal.
- `ProgressPatternEngine.standard()` evalúa progreso ya registrado: realizados / (realizados + omitidos). Los pendientes no cuentan como fallos.
- La fachada del motor todavía no coordina automáticamente la pantalla principal. La existencia de clases de reglas no implica IA conversacional ni personalización automática.
- El registro del perfil contiene tema blanco/negro, objetivos, preferencias y categorías semanales. Mantener sus claves y opciones.
- La planificación semanal tiene **cinco categorías activas y Descanso**; sus seis colores originales y su leyenda son obligatorios en ambas apariencias.

## 2. Experiencia de usuario propuesta

Pantalla principal austera y adaptable, con fondo **blanco** como opción predeterminada y **negro** como alternativa seleccionada en el paso 3 del perfil. Los colores de las seis categorías semanales son la excepción deliberada a la monocromía.

1. **Estado de hoy:** el usuario puede indicar voluntariamente cómo se encuentra, el tiempo disponible o que prefiere descansar. Se evita interpretar parámetros físicos como métricas de rendimiento.
2. **Sugerencia explicada:** mostrar evidencia consultada, razón de la decisión, limitaciones y alternativas. La sugerencia no modifica el plan por sí sola.
3. **Mi semana:** usar entradas reales de calendario y seguimiento (incluidos pendientes); no inventar porcentajes en períodos sin actividades evaluadas.
4. **Plan y catálogo:** acceso a módulos existentes sin duplicar navegación ni el contenido almacenado.
5. **Conocimiento y aprendizaje:** biblioteca legible de explicaciones verificadas del inventario de ejercicios; preguntas frecuentes y técnicas revisadas; no inventar instrucciones.
6. **Memoria y control:** informar qué se recuerda, cuándo, por qué y ofrecer revisión, exportación y borrado de preferencias permitidas.

### Comportamiento de ajustes

El sistema debe proponer acciones sin ejecutarlas:

- Si hay molestias expresadas por el usuario, priorizar pausa y orientación general para pedir ayuda si persisten. No diagnosticar.
- Si se solicita descanso, respetarlo sin penalizaciones, rachas obligatorias ni cargas compensatorias.
- Ante poca energía, sugerir flexibilidad o descanso; nunca aumentar automáticamente dificultad o duración.
- Con poco tiempo, ofrecer revisión de una versión más corta, sujeta a aprobación.
- Ante actividades omitidas, revisar la programación sin culpabilizar.
- Con datos insuficientes, responder «sin evaluar» y ofrecer navegación manual; no inventar historial.
- Haber completado tareas no implica una recomendación automática de más ejercicio.

## 3. Arquitectura y límites entre módulos

```text
Android UI (Home / Plan / Catálogo / Seguimiento / Ajustes)
                  |
       HomeSupportCoordinator (presentación)
                  |
        AsterionAdaptiveFacade
         /       |        \\
Perfil local   Evidencia    Conocimiento versionado
               semanal         del catálogo
         \\       |        /
          RecommendationPolicy (reglas puras, versionadas)
                  |
            Suggestion DTO
    evidencia + explicación + opciones + límites
                  |
            Usuario revisa
                  |
      SOLO con aprobación explícita:
   SchedulePlanner / ProgressService (repositorios existentes)
```

**Regla de arquitectura:** separar consultas y sugerencias de comandos que escriben en SQLite. La evaluación es pura y determinista; las escrituras se hacen en un caso de uso explícito, en segundo plano y transaccional. No crear cambios autónomos ni extensiones con scripts remotos.

### Contratos de datos sugeridos

- `UserPreferencesV1`: objetivos, modalidad y equipos autorizados; fuente: configuración de perfil actual.
- `DailyCheckIn`: elección temporal, tiempo disponible y fecha; por defecto, solo en memoria. No transmitir o conservar datos de salud sin permiso y justificación.
- `WeeklyEvidence`: fecha de inicio del período, actividades previstas, realizadas, omitidas y pendientes; fuente: repositorios SQLite.
- `RecommendationV1`: identificador estable, versión de política, hechos utilizados, razón textual, límites y conjunto de alternativas; **no es** una orden.
- `ProposedPlanChange`: revisión opcional reversible con vista «antes/después», impacto y conflictos detectados.
- `UserConsent` y `ChangeAudit`: aprobación, marca temporal, versión y posibilidad de restaurar el plan previo.

Los datos desconocidos permanecen desconocidos. Evitar almacenar de forma predeterminada interpretaciones sensibles de salud. No inferir ni calificar el rendimiento a partir del peso, estatura o apariencia.

## 4. Etapas de construcción

| Fase | Producto verificable | Restricción |
|---|---|---|
| 0 — Diseño (actual) | Prototipo HTML, especificación y pruebas de interacción simuladas | **No modifica la APK ni sus datos** |
| 1 — Motor local explicable | API Java pura, pruebas unitarias, coordinación Android con consultas reales a SQLite | Sin IA externa, sin cambios automáticos del plan |
| 2 — Cambios controlados | Propuesta antes/después, aprobación, validación de conflictos, auditoría y deshacer | Sin escrituras sin aceptación |
| 3 — Conocimiento | Lectura de contenidos revisados, explicaciones, búsqueda por familia y variante, versiones | Sin inventar técnicas ni accesos en red |
| 4 — Memoria configurable | Persistencia consentida, exportación, borrado, controles de retención, pruebas de privacidad | Privacidad por defecto |
| 5 — IA opcional | Proveedor externo detrás de una interfaz, control de calidad, límites de respuestas y modo offline | Conexión voluntaria; las salidas no eluden políticas locales |
| 6 — Evaluación continua | Casos de prueba nuevos, accesibilidad, rendimiento, recuperación, monitoreo de fallos y sesgos | Despliegue reversible |

**La adaptabilidad a toda situación futura no puede garantizarse.** Ante datos inusuales, fallos o decisiones de riesgo, el sistema debe ofrecer una alternativa conservadora y control manual en lugar de asumir certeza.

## 5. Criterios de aceptación antes de activar la nueva pantalla en APK

1. Mantener exactamente los identificadores de navegación de `MainActivity` y los módulos existentes.
2. Verificar que un cambio de tema del perfil se refleje en la pantalla de inicio sin mezclar apariencias.
3. Conservar seis colores de semana y leyenda accesible; el contraste del texto debe ser suficiente sobre cada color.
4. Con historial vacío, no mostrar una tasa de cumplimiento de «0 %»; usar «sin evaluar».
5. Consultas SQLite siempre fuera del hilo principal; cancelar trabajos cuando la pantalla se destruye.
6. Ninguna sugerencia cambia calendario o marca resultados sin acción aprobada.
7. Pruebas de poco tiempo, descanso, actividad omitida, datos inconsistentes y falta de red.
8. Permitir desistir de la recomendación, corregir datos y revisar el motivo.
9. Conservar funcionamiento de la app offline, perfil ya guardado y módulos de catálogo/calendario/progreso.
10. Tras integración autorizada, compilar paquete principal `pe.asterionsystem.app` desde `main`, ejecutar tests, Lint y prueba en emulador; no publicar como «IA completa» hasta conectar y verificar capacidades reales.

## 6. Estado de la previsualización

El HTML contiene **datos y decisiones simulados** para comprobar diseño, navegación, dos temas, respuestas de hoy, tiempo disponible, motivos de sugerencias y colores semanales. No está conectado a SQLite, a una cuenta de IA o a una API de modelos. Por ahora es un artefacto de diseño y no debe usarse como justificación de que estas funciones existen en la APK.
