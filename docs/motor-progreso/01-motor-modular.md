# Motor modular de planificación y progreso — Bloque independiente

**Estado:** motor de dominio preparado en fuente, versión inicial **sin activar patrones nuevos en la interfaz**. Compilación y pruebas JVM del nuevo módulo todavía pendientes de validación en el workflow CI de debug (automático por `push` a `main` o manual).

## Objetivo

Centralizar los accesos al catálogo de actividades, la planificación, las preferencias y el seguimiento mediante una fachada y **permitir sustituir el criterio de evaluación del progreso sin reescribir Android o SQLite**.

El motor no prescribe ejercicios, no modifica la planificación automáticamente y no interpreta scripts, fórmulas ejecutables ni patrones descargados de internet.

## Arquitectura

```text
        Android :app (Java y XML; UI existente sin cambios)
                          |
                  AsterionDomainEngine
                módulo :progress-engine
                   /    |     |     \
          Catálogo   Planificación  Configuración  Progreso
             |           |              |             |
        CatalogRepo  SchedulePlanner  PreferencesService  ProgressRepo
                   \       |            /
                       :core (Java puro)
                          |
           ProgressPatternEngine (Strategy Registry)
                    |                    |
            standard_v1        RuleBasedProgressPattern
             (activo al             (adaptador opcional
             inyectarlo)              a fase 06)
```

- `:core` mantiene los modelos y contratos existentes (fases 06–11), la fórmula actual de `ProgressSummary`, conflictos de programación y preferencias.
- `:progress-engine` es una **biblioteca Java 17 propia**, que declara `api project(':core')` y **no importa Android ni SQLite**.
- `:app` declara `implementation project(':progress-engine')`. Las pantallas actuales siguen consumiendo sus presentadores existentes; **la nueva fachada no está conectada todavía a la UI**.
- Solo los adaptadores actuales de `:app` siguen gestionando SQLite v5 y `SharedPreferences`; no hay tablas nuevas.

## Componentes

| Componente | Responsabilidad |
|---|---|
| `AsterionDomainEngine` | Fachada sobre catálogo, programación, configuración y seguimiento. Expone comandos de escritura explícitos y evaluación semanal de solo lectura |
| `ProgressPattern` | Contrato para estrategias Java de interpretación del progreso |
| `ProgressPatternConfig` | Identificador de estrategia y hasta 16 parámetros numéricos, inmutables y validados |
| `ProgressContext` | Fotografía semanal inmutable: entradas, resumen, preferencias y parámetros; evita asociar entradas de semanas distintas |
| `ProgressPatternEngine` | Registro inmutable de hasta 16 estrategias, con IDs únicos y fallo explícito ante patrones desconocidos |
| `StandardProgressPattern` | Comportamiento aprobado: porcentaje = realizadas / (realizadas + omitidas); pendientes no se evalúan; si no hay evaluadas, **sin porcentaje** |
| `RuleBasedProgressPattern` | Traduce métricas del contexto a `RuleContext` y usa el evaluador determinista ya existente de fase 06 |
| `ProgressOutcome` y `ProgressAssessment` | Resultados y diagnósticos inmutables; conservan el resumen original |

## Contrato de patrones

Se elige un patrón mediante `ProgressPatternConfig("standard_v1", parámetros)`. El motor verifica que ese patrón haya sido **registrado previamente como código Java**. La configuración no acepta instrucciones ejecutables, nombres arbitrarios de clases ni scripts.

Los parámetros validados pueden pasarse a una estrategia futura mediante `context.configuration().parameters()`. Si se usan reglas de fase 06, se publican como datos numéricos `param_<nombre>`. Las métricas de contexto son `total`, `completed`, `skipped`, `pending`, `evaluated`, `has_evaluated` y `completion_percent` solo cuando existe un resultado evaluado.

La propia estrategia puede calcular un indicador alternativo usando `context.entries()` (que contienen ID de actividad, fecha y duración), sin editar ningún registro. El ejemplo de prueba `duration_v1` calcula la proporción de minutos realizados respecto de los minutos evaluados, para **demostrar extensibilidad**, no para activar una regla predeterminada.

### Ejemplo de creación y selección

```java
ProgressPatternEngine patterns = ProgressPatternEngine.standard();
ProgressPatternConfig selected = ProgressPatternConfig.standard();

AsterionDomainEngine engine = new AsterionDomainEngine(
        catalogRepository, schedulePlanner, preferencesService,
        progressRepository, patterns);

// Se ejecuta en un worker: los repositorios SQLite no permiten el hilo principal.
ProgressAssessment week = engine.assessWeek(monday, selected);
if (week.scorePercent().isPresent()) {
    int percent = week.scorePercent().getAsInt();
}
```

Para otro patrón se implementa `ProgressPattern`, se incorpora a la lista del `ProgressPatternEngine` y se selecciona mediante su identificador; el motor y los servicios existentes no requieren cambios. La selección/persistencia de un patrón por usuario queda **intencionalmente fuera** de esta versión.

## Invariantes de seguridad y compatibilidad

1. Ningún patrón puede cambiar los estados guardados por el mero hecho de evaluar. Solo `recordOutcome`, `createPlan`, `updatePlan`, `deletePlan` y los métodos explícitos de configuración escriben mediante los servicios anteriores.
2. Un día sin resultados explícitos no equivale a 0 % de cumplimiento.
3. Para evaluar una semana, el período empieza lunes y las entradas deben pertenecer a ese período.
4. IDs de patrón y parámetros se validan; el motor rechaza duplicados, desconocidos y resultados porcentuales fuera de 0–100.
5. Las operaciones de repositorio son síncronas y **deben llamarse desde un hilo de trabajo Android**; el motor no crea un executor propio ni oculta operaciones bloqueantes.
6. Ningún cambio en SQLite v5, `SharedPreferences`, layout XML, navegación, firma, variante `release` ni distribución release; el CI debug automático por `push` a `main` fue autorizado posteriormente.

## Pruebas y validación pendiente

Las nuevas pruebas JUnit de `:progress-engine` cubren: casos vacíos, estados pendientes, fórmula original, estrategia alternativa, evaluación de reglas de fase 06, ausencia de porcentaje, selección desconocida, IDs duplicados, parámetros inmutables, límites numéricos, restricciones semanales, delegación de catálogo y planificación, independencia de escritura y comandos explícitos.

El workflow de GitHub Actions ejecuta automáticamente en cada `push` a `main`, y mantiene `workflow_dispatch` para ejecuciones manuales. Ejecutará `:core:test :progress-engine:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`. **Esta nueva versión del motor aún no se ha compilado ni probado en un dispositivo Android.**

## Próximas ampliaciones posibles (sin activar)

- **Perfil de patrones:** persistir el patrón seleccionado y sus parámetros en un contrato de datos versionado; permite conservar preferencias entre sesiones sin mezclar configuración visual y lógica.
- **Motor de reglas por categorías:** proporcionar criterios específicos para clases de actividades, con validaciones de límites; permite distintos reportes sobre un mismo historial sin reescribir SQLite.
- **Integración UI y pruebas de dispositivo:** usar la fachada en presentadores y seleccionar patrón desde una pantalla dedicada, con pruebas de navegación y accesibilidad; evita exponer funciones incompletas.

## Extensión: catálogo taxonómico de ejercicios v1

La fachada del motor acepta opcionalmente un `ExerciseCatalog` empaquetado, mediante un constructor sobrecargado que conserva la firma anterior. Expone `exerciseFamilies()`, `exerciseTypes(familyId)`, `exerciseVariants(typeId)` y `searchExerciseVariants(...)`. Esto conecta la clasificación en el plano de **consulta**, no escribe actividades ni modifica el progreso.

El adaptador Android `AssetExerciseCatalogRepository` lee el inventario de `progress-engine/src/main/resources/asterion_exercises/v1/` como assets de solo lectura en un hilo de trabajo. El catálogo no sustituye el almacenamiento actual de actividades de `SQLiteCatalogRepository`; para vincular nuevas variantes a programaciones se necesita una integración futura respetuosa de las claves foráneas.

Ver [estructura de las 39 familias y las 445 variantes](../catalogo-ejercicios/01-estructura.md) y el [inventario completo](../catalogo-ejercicios/02-inventario-completo.md).
