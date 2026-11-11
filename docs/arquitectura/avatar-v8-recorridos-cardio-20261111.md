# Asterion V8 — recorridos completos y cardio (vistas 2D)

Fecha de diseño: 2026-11-11. Rama WIP apilada sobre PR #23; `main`
intacta. No es una guía de entrenamiento ni validación clínica.

## Correcciones verificables
- **Sentadilla abierta frontal**: la V7 dejaba la cadera en y=238 y los pies
  en y=411 con desplazamiento lateral de 47 unidades; distancia real ≈179.3
  frente a muslo + pantorrilla =190. La V8 usa hip y=244, tobillo y=428
  y separación lateral 47, distancia ≈189.91 al inicio y fin. Se conservan
  los dos segmentos por pierna y el suelo sin movimiento; la cadera desciende
  hasta y=293 en el punto más bajo del ejemplo. La mayor separación entre
  piernas debe revisarse con vista frontal en un dispositivo real.
- **Elevación de brazos**: conserva codos casi extendidos como corresponde al
  gesto ilustrado de elevación. Se añade otra vista de **flexión de codos**
  para demostrar que los dos pivotes sí articulan realmente con longitudes
  de 58/63 unidades constantes.
- **Saltos de tijera**: cámara frontal dedicada. Brazos simétricos rotan
  desde los costados hasta una pose elevada mientras tobillos se separan;
  se incorpora desplazamiento vertical temporal del cuerpo durante la
  transición. IK analítica en cuatro extremidades y ciclo cerrado.
- **Burpee**: demostración lateral de múltiples estados (de pie, cuclillas,
  manos en el suelo, cuerpo horizontal, variación de altura, regreso).
  Se mantienen los huesos 94/96 y 58/63 y la unión cadera/hombro de 118,
  con contacto de manos en los segmentos sobre el suelo. Los pies pueden
  cambiar de apoyo sólo durante los tramos de desplazamiento.
- **Top/diamante**: permanece como cámara cenital y panel ilustrativo de
  manos, para revisar posturas en el plano horizontal sin inventar una
  proyección frontal.

## Referencias externas revisadas
- ExRx, sentadilla ilustrada y análisis de movimiento:
  https://exrx.net/WeightExercises/Quadriceps/BWSquat
  https://exrx.net/Kinesiology/Squats
- ExRx, burpee con varias posturas:
  https://exrx.net/Aerobic/Exercises/Burpee
- ACE, biblioteca de movimientos de sentadilla:
  https://acefitness.org/resources/everyone/exercise-library/135/bodyweight-squat
- WorkoutLabs, ilustraciones de jumping jack / star jump:
  https://workoutlabs.com/exercise-guide/jumping-jacks
- Godot cutout animation, pivot hierarchy:
  https://docs.godotengine.org/en/4.4/tutorials/animation/cutout_animation.html
- Android Canvas custom View:
  https://developer.android.com/develop/ui/views/layout/custom-views/custom-drawing

Los ejemplos visuales se consultaron como referencias de fases y proyección
sin reutilizar imágenes, texturas o motores externos. Las variables de pantalla
no son medidas reales ni prescriben técnica o intensidad.

## Alcance y limitaciones
Las escenas se diseñaron específicamente para el plano que hace más legible
cada movimiento. No son un cuerpo 3D único que se pueda girar y no constituyen
un sistema biomecánico validado. No se asigna aún automáticamente una cámara
a cada ejercicio del catálogo; esa decisión pertenece a la próxima fase de
especificaciones por ejercicio.

## Control de calidad
JUnit densos: longitud, restauración de pose, apoyos, continuidad, apertura de
brazos y piernas, articulación explícita de codos; AndroidTest para ambas
paletas; CI de ensamblado y Android Lint más emulador Android 16. El HTML
independiente permite inspección de todos los modos y marcar pivotes.
