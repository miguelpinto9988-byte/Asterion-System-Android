# Fase 08 — Planificación local de actividades

## Alcance autorizado

Crear, consultar, editar y borrar programaciones de **actividades existentes del catálogo**. Una programación contiene fecha civil local, hora de inicio, duración, identificador de la actividad y fechas de auditoría. No hay repetición, reglas de ejecución automática, notificaciones, estadísticas de progreso ni sincronización.

## Capas

- `:core/planning/ScheduledActivity`: modelo Java inmutable con fecha `LocalDate`, minuto inicial [0,1439] y duración [1,720]. Rechaza cruzar medianoche.
- `:core/planning/ScheduleRepository`: puerto CRUD.
- `:core/planning/SchedulePlanner`: valida que el elemento del catálogo exista y sea `ACTIVITY`; detecta conflictos por intervalos semiabiertos [inicio, fin).
- `:core/planning/ScheduleListing`: entrada de calendario con título resuelto desde el catálogo.
- `:core/planning/BackgroundScheduleOperations`: ejecución serial en worker dedicado; callbacks no actualizan directamente las vistas.
- `:app/data/SQLiteScheduleRepository`: transacciones SQLite y consultas parametrizadas; rechaza operaciones en el hilo principal.
- `:app/presentation/SchedulePresenter`: conecta las pantallas XML, presentación semanal y formulario con el worker Java. Protege los callbacks mediante tokens de pantalla.
- `:app/res/layout/screen_plan.xml` y `screen_plan_form.xml`: diseños XML provisionales, sin HTML ni WebView.

## Reglas temporales

Se programan **horarios civiles locales**: una fecha `AAAA-MM-DD` y minutos desde las 00:00. Dos programaciones se superponen solo si tienen la misma fecha y sus intervalos [inicio, fin) se intersectan; una comienza exactamente al terminar la otra sin conflicto.

La versión inicial permite horarios dentro de un mismo día, desde 2000 hasta 2100, duración máxima 720 minutos. No convierte horarios a UTC ni resuelve zonas horarias o transiciones DST; no se deben interpretar estos registros como instantes absolutos para alarmas. Ese procesamiento está fuera del alcance aprobado.

## Navegación y estado

`NavigationRoute` incorpora `PLAN` y `PLAN_FORM` a la navegación existente. Desde Inicio puede abrirse el planificador; desde allí, una programación existente o el botón «Programar» abren el formulario. El botón Atrás regresa al calendario. `SchedulePresenter` almacena temporalmente la fecha elegida y el identificador editado en `Bundle` durante la recreación de la Activity.

El estado duradero se guarda en SQLite, no en `Bundle`. No se tocan los servicios del motor de reglas de fase 06: los horarios son validados mediante un planificador específico y los contratos de catálogo.

## Previsualización HTML independiente

El archivo entregado por separado representa el contenido XML y el comportamiento implementado. Guarda datos **simulados en localStorage del navegador** si está disponible; nunca abre SQLite Android ni forma parte de la APK. Conserva las pantallas previas y añade planificador/calendario/formulario.
