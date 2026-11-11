# Motor de rutinas, calendario y ventanas de misiones · 2026-11-11

## Dependencia

Rama apilada sobre **PR #11**, catálogo multideporte con 1.017 variantes, 354 tipos, 125 familias y únicamente cinco categorías operativas. La rama principal `main` no cambia con esta propuesta.

## 1. Seudoaleatoriedad con el catálogo completo

`MissionPlanner` mantiene dos selecciones independientes, ambas locales y reproducibles:
- Diarias: semilla SHA-256 calculada con fecha local y firma de las preferencias que afectan al entrenamiento. Los IDs publicados se fijan por día; reabrir la aplicación o registrar un movimiento no cambia las otras opciones.
- Monarca: semilla SHA-256 independiente del ciclo de cinco días y de la firma del perfil. La selección del catálogo se ordena por ID antes del barajado determinista. Se priorizan alternativas de distintas categorías y del ciclo anterior cuando existen.
- La lista aceptada en Monarca se conserva en su almacén local. Cualquier cambio de configuración que invalide los filtros impone revisión y consentimiento renovado. La vista previa no equivale a activación.
- Las variantes elegibles se filtran por categoría, equipo disponible, intensidad y complejidad. No hay promociones automáticas de carga, volumen ni dificultad tras completar una rutina.

## 2. Rutinas personalizadas: elección voluntaria

`RoutineGenerator` devuelve **hasta seis propuestas** para una solicitud:
- Una de las categorías: `tren_superior`, `tren_central`, `tren_inferior`, `movilidad`, `cardio`.
- Tiempo disponible indicativo: 10, 15, 20, 30, 45 o 60 minutos.
- Nivel técnico seleccionado por el usuario: básico (nivel 1), intermedio (hasta nivel 2), avanzado (hasta nivel 3 sin supervisión).
- Fecha actual, firma del perfil y contador de variación manual para disponer de opciones distintas si el usuario lo solicita.

Cada propuesta contiene entre tres y seis variantes, según el tiempo indicado. El motor ordena por IDs, baraja con `Random(seed)` reproducible y prioriza movimientos de familias/tipos diferentes tanto dentro de una propuesta como entre las seis opciones. Las propuestas quedan congeladas durante una vista mientras no cambien sus parámetros.

Se filtran explícitamente **todos** los registros `manualOnly`, las variantes con supervisión, intensidad alta/variable/no clasificada, niveles por encima del elegido y material no disponible. El tiempo es un margen orientativo; no constituye un temporizador de esfuerzo ni una prescripción.

En la pantalla Android `RoutinesActivity` se revisan las opciones individualmente mediante `AlertDialog`. Ninguna rutina comienza sin pulsar **Elegir y comenzar**. Después se valida que su ID, orden de ejercicios y perfil sigan correspondiendo a una oferta válida.

## 3. Sesiones guiadas y XP

Se extiende `RoutineStepActivity` con `kind=custom`; conserva sus modos existentes diaria y Monarca. `RoutineSessionStore` guarda la posición y el bloque para restablecer una sesión interrumpida. Los ejercicios se registran de forma secuencial; el usuario puede pausar, detenerse por malestar o finalizar.

`RoutineExperienceStore` concede un **registro fijo de 10 XP por el primer reporte de sesión completa del día**, nunca por intensidad, duración, repeticiones, rondas extra o varias sesiones. Es local, idempotente, opcional, no afecta al plan de misiones y no modifica SQLite. Se usan `SharedPreferences.commit()` para persistencia.

La experiencia no equivale a una evaluación del nivel físico y no impone objetivos de rendimiento.

## 4. Calendario mensual funcional

`MonthCalendar` usa `java.time.YearMonth` y `LocalDate`, comenzando cada semana en lunes. Recalcula longitud de mes, febrero bisiesto, seis filas de calendario y límites válidos de fecha. `CalendarActivity` permite:
- mes anterior/siguiente y año anterior/siguiente;
- seleccionar directamente un año mediante entrada numérica;
- seleccionar cualquier día del mes para consultar categoría semanal y registros voluntarios;
- volver al día actual;
- consultar indicaciones de misiones y rutinas registradas, sin generar misiones históricas ni futuras.

No se introduce un límite arbitrario de años; se aplica el rango real admitido por `java.time`. La navegación es repetible y los saltos de diciembre a enero preservan correctamente el año.

## 5. Pantallas y ventanas

- Inicio: muestra cinco tarjetas rectangulares ordenadas verticalmente: (1) Monarca, (2) Perfil, (3) Misiones diarias, (4) Progreso, (5) Biblioteca. No hay botones de navegación rápida extra ni accesos duplicados al calendario o rutinas. El avatar de 52dp de la esquina superior derecha es el único botón para abrir y editar el perfil; la segunda tarjeta sigue mostrando sus datos de manera informativa.
- Navegación inferior de Inicio, Misiones, Rutinas y Calendario: **solo Inicio, Rutinas y Calendario** mediante `SimpleBottomNav`, con objetivos táctiles de al menos 48dp. Las categorías Monarca y Diarias no están en la barra inferior, sino en tarjetas de Inicio y pestañas de contenido de Misiones. La pantalla de Misiones no añade botones superiores redundantes hacia Rutinas/Calendario.
- Perfil: `ProfileDetailsActivity` permite consultar el registro desde el avatar de Inicio. `ProfileCreateActivity` admite `edit_profile=true`, precarga el perfil **ya confirmado**, edita en el formulario de cuatro etapas y guarda únicamente tras la confirmación final. Las modificaciones no pisan un borrador anterior ni reinician la sesión del dispositivo. El menú secundario de perfil concentra Planificación, Configuración, Estado local y herramientas históricas, sin llenar Inicio de enlaces.\n- Misiones diarias y Monarca: se añaden ventanas de vista previa nativas (`AlertDialog`) que muestran la lista y metadatos, sin completar ni activar misiones.
- Rutinas: selector de categoría/tiempo/nivel, seis opciones, vista previa independiente y confirmación de selección.
- Sesión: ejercicio actual, controles de avance, descanso voluntario, pausa y detención.
- Calendario: navegación por meses y años, marcas de registro y ficha del día seleccionado.

La maqueta interactiva descargable de la conversación es **una simulación de interfaz**; no debe presentarse como captura real de un dispositivo. Para validar la apariencia exacta se recomienda revisar la APK en emulador/teléfono.

## 6. Verificaciones

- `RoutineGeneratorTest` JUnit: estabilidad por fecha/semilla, seis ofertas, cinco categorías, seguridad, diversidad, cambio explícito de sorteo y calendario bisiesto.
- `tests/routines_calendar_contract.py` y `tests/home_navigation_architecture_contract.py`: filtros, semilla, XP, jerarquía exacta de cinco tarjetas, avatar de perfil, editor seguro y barra inferior de tres destinos.
- `RoutineExperiencePersistenceTest` Android instrumentado: evita XP repetidos y confirma persistencia tras terminar y reiniciar el proceso.
- Contratos anteriores y pruebas instrumentadas de misiones/Monarca deben seguir funcionando sin migraciones de bases de datos.

## Mejoras que quedan para revisión

1. Capturar y revisar pantallas del APK real en un teléfono o emulador con perfiles y tamaños de pantalla diversos.
2. Evaluar accesibilidad, tamaños táctiles, rotación, lectura de etiquetas, contraste y navegación con TalkBack.
3. Añadir revisión opcional de esfuerzo al concluir rutinas sin modificar la selección ni generar incremento de intensidad.
4. Conectar el calendario con registros de rutinas históricas más ricos, manteniendo la privacidad y sin convertirlo en calendario de retos obligatorios.
