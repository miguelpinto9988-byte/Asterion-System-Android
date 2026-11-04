# Fase 02 — Catálogo de contratos

| Contrato | Ubicación | Invariante / decisión |
|---|---|---|
| `UseCase<I,O>.execute(I)` | `core.application` | Solo firma; sin caso de negocio concreto. |
| `EntityId.value()` | `core.domain` | Identificador no nulo ni vacío; recorta espacios periféricos. |
| `RuleEvaluator<C,R>.evaluate(C)` | `core.engine` | Puerto de evaluación, sin reglas ni efectos implementados. |
| `RecordReader<ID,T>.findById(ID)` | `core.data` | Lectura opcional, sin implementación de base de datos. |
| `ClockPort.now()` | `core.platform` | Fuente de tiempo sustituible en futuras pruebas. |
| `EventBus.subscribe/publish` | `core.events` | Entrega sincrónica por clase exacta y cierre de suscripción. |
| `StateStore<S>.current/replace` | `core.state` | Estado en memoria no nulo; sin persistencia. |
| `LifecycleObserver.onLifecycleChanged` | `core.lifecycle` | Puerto sin tipos de Android. |
| `AndroidLifecycleAdapter` | `app.platform` | Mapea 1 a 1 los callbacks de la `Activity`. |
| `ApplicationCoordinator` | `app.application` | Actualiza estado estructural y publica `LifecycleChangedEvent`. |

## Fronteras

- El núcleo no importa `android.*`, `androidx.*`, `SQLite*`, `WebView`, ni APIs de interfaz visual.
- El módulo `:app` depende de `:core`. No existe ninguna dependencia inversa.
- No hay almacenamiento de datos de usuario, sincronización, recursos multimedia dedicados ni notificaciones.
- El uso de `StateStore` en esta fase **no** equivale a desarrollar la gestión completa del estado de UI de la fase 04.
- El motor de reglas, el repositorio y los casos de uso funcionales permanecen como **interfaces**, no como implementaciones adelantadas.

## Convenios de extensión

Incorporar una implementación solo cuando se apruebe su fase, manteniendo métodos con tipos concretos y pruebas JVM; no utilizar un registro global mutable ni reflexión para cargar motores. La ampliación visual se hará exclusivamente mediante recursos XML Android y Java nativo.
