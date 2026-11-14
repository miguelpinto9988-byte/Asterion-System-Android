# Prediseño de Asterion System para Android

**Vista conceptual, 14 de noviembre de 2026.** Nueve tipos de ventana en dos temas. Las imágenes son maquetas vectoriales del diseño propuesto; no son capturas de la APK actual. Los números de ejercicios, series y repeticiones son datos ilustrativos para evaluar la interfaz.

| Parte | Comparación oscuro/claro |
| --- | --- |
| Inicio, misiones y rutinas | [Lámina 1](01_inicio_misiones_rutinas.jpg) |
| Sesión, calendario y ficha del ejercicio | [Lámina 2](02_sesion_calendario_biblioteca.jpg) |
| Alta, progreso y perfil | [Lámina 3](03_alta_progreso_perfil.jpg) |
| Detalle ampliado de la sesión | [Lámina 4](04_sesion_detalle.jpg) |

## Dirección visual

La identidad de «cazador» y «misión Monarca» se conserva mediante motivos de portales, cian y violeta. El contenido de entrenamiento mantiene una jerarquía sencilla: una acción principal por vista, texto legible, controles de tamaño cómodo y el mismo orden en tema claro y oscuro. Se elimina la división visual actual entre el planificador oscuro, el registro claro y el catálogo de GIF.

## Funcionamiento propuesto

### Misiones como tareas

Una misión puede estar **pendiente**, **en curso**, **completada**, **omitida** o **pausada**. El usuario la marca o desmarca expresamente. Al finalizar una rutina guiada, la app puede ofrecer marcar la misión asociada, pero no debe hacerlo únicamente porque el temporizador llegó a cero. Los cambios se guardan por fecha y por ID de misión; repetir una acción no duplica el progreso. Las misiones opcionales y el descanso no causan penalización.

### Rutina guiada paso a paso

1. Elegir tiempo, categoría, equipo, nivel y una de las rutinas propuestas; revisar todos los movimientos antes de comenzar.
2. Abrir cada ejercicio con su GIF por ID exacto, nombre, instrucciones, material y una alternativa cuando corresponda.
3. Mostrar «ejercicio 2 de 4», «serie 1 de 2» y un contador manual, por ejemplo «8 de 10 repeticiones». Las cantidades son editables y no se elevan automáticamente.
4. Al registrar una serie, iniciar el descanso configurado. El reloj se pausa, reanuda o salta; su estado se conserva al cambiar de app o girar el dispositivo.
5. Pasar al siguiente ejercicio mediante confirmación visible. Siempre ofrecer omitir, pausar y terminar la sesión. Guardar solo lo que la persona declara haber realizado.
6. Mostrar un resumen al cerrar: ejercicios realizados, omitidos, tiempo y sensación de esfuerzo opcional.

El esquema corporal de las maquetas reserva el espacio visual del GIF. En la implementación se usarán los GIF existentes del catálogo, sin asumir que el esquema es una demostración técnica validada.

### Salud, progreso y accesibilidad

El calendario reúne plan, actividad realizada y descanso. El progreso muestra tendencias de movimiento y percepción de esfuerzo, sin usar peso corporal o un «nivel» de juego como indicador clínico. Los objetivos son configurables; cualquier progresión de volumen o intensidad requiere decisiones explícitas y revisión del contenido de ejercicios. El tema claro y oscuro se aplica a **todas** las vistas, incluidas fichas, diálogos, temporizadores y ajustes. Se deben validar contraste, tamaño de texto, lector de pantalla, foco y orientación.

Las guías públicas de la [OMS](https://www.who.int/publications/i/item/9789240015128) y los [CDC](https://www.cdc.gov/healthy-weight-growth/physical-activity/getting-started.html) orientan la gradualidad y la posibilidad de adaptar la actividad. Este prediseño no establece una prescripción personal de ejercicio.

## Alcance en el repositorio actual

| Vistas actuales | Diseño propuesto |
| --- | --- |
| Splash, acceso y cuatro pasos de registro | Alta y preferencias |
| Inicio y menú secundario | Centro del cazador |
| Misiones diarias, Monarca y biblioteca de misiones | Misiones |
| Rutinas y planificador semanal | Rutinas y calendario |
| Sesión paso a paso | Sesión guiada |
| Progreso y resumen | Progreso |
| Catálogo importado, catálogo local y ficha con GIF | Biblioteca y ficha de ejercicio |
| Perfil, ajustes y estado local | Perfil y ajustes avanzados |

La implementación es viable en la app nativa Java/XML existente: el repositorio ya incluye generación de rutinas, seguimiento local y GIF por variante. Quedan por desarrollar la experiencia visual común, el flujo de estados de misión, el registro manual de series/repeticiones, el temporizador persistente, la unión de catálogos en la navegación y las comprobaciones de accesibilidad y funcionamiento. Las maquetas no modifican todavía el comportamiento de la APK.
