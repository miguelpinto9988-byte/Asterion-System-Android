# Asterion V13 — integración robusta, uniones orgánicas y apoyos de las tres cámaras

## Defectos corregidos

1. **Codos/rodillas con esferas superpuestas:** la lateral superponía
   círculos de hasta 12.5 unidades a segmentos más finos; esto producía
   protuberancias visibles. En V13 cada articulación se dibuja mediante
   `AvatarSoftContour.hinge`, que comparte el mismo ancla del rig y limita
   su tamaño a 32% del menor diámetro de los segmentos adyacentes.
   No se dibuja un anillo ni una bola separada.
2. **Tapas Bézier demasiado largas:** los extremos de las piezas podían
   extenderse 40% del grosor más allá del codo o rodilla. El módulo
   compartido `AvatarSoftContour.segment` limita esa extensión a
   16% del diámetro y 9% del largo del hueso, sin trazo perimetral
   que genere una línea dura en las uniones.
3. **Brazos morados distintos:** un camino alternativo dibujaba el brazo
   morado sin manga y con cilindros de grosor fijo 19/16. Ahora ambos
   usan el mismo brazo/pivote y la misma manga corta; cambia la paleta,
   no la anatomía.
4. **Rodilleras duplicadas:** la frontal dibujaba una placa rectangular
   adicional sobre la placa de denim compartida. Se eliminó esa placa.
   `AvatarArtDetails.drawDenimLeg` presenta una única forma suave de
   tela con puntadas y los bajos sin grandes bandas geométricas.
5. **Apoyos desplazados sin detección:** `AvatarFrameSafety` revisa
   los puntos de contacto declarados por `AvatarContactFrame` antes
   de dibujar: suelas laterales, suelas frontales y palmas/punteras
   superiores. Solo compara anclas del modelo 2D; no evalúa fuerzas
   físicas ni la postura de personas reales.

## Seguridad de renderizado e integración

- Valida coordenadas finitas, límites espaciales y anchos de cada
  silueta antes de llegar a `Path.cubicTo`. No permite huesos
  colapsados ni anchos no numéricos.
- `NativeAvatarView.setPhase` limita valores infinitos a los
  extremos del ciclo y evita que NaN contamine la animación.
- Si las proporciones suministradas generan una postura imposible,
  `NativeAvatarView.onDraw` muestra un aviso neutro en vez de una figura
  dislocada o un fallo de la vista; `wasLastFrameRejected()` permite
  comprobar la recuperación al corregir la configuración.
- `NativeAvatarView.onDraw` equilibra `Canvas.save/restore` con
  `try/finally`, incluso si un chequeo rechaza una postura inválida.
- Sin nuevas dependencias, permisos, internet, bibliotecas nativas
  ni motores de renderizado.
- `AvatarSkeletonFrame` sigue verificando las longitudes geométricas
  y `AvatarOrientationRules` la dirección de superficie y cámara.
  El diseño posterior de las zapatillas V12 se conserva sin cambios.

## Verificación

- `AvatarFrameSafetyTest`: cuatro pruebas Java, muestreo de 241 fases
  de seis clips laterales y cinco escenas de otras cámaras.
- Tres pruebas instrumentadas adicionales: un codo Bézier estrecho no
  debe producir un óvalo suelto y todos los modos deben dibujar de
  forma segura entre distintas fases y dos paletas, sin fuga
  de estado de Canvas, y una prueba de recuperación tras
  proporciones imposibles.
- `avatar_v13_soft_joint_contract.py`: garantiza que todos los renderers
  llaman al mismo `AvatarSoftContour`, conservan el contacto y no
  reintroducen artefactos geométricos conocidos.
- El job Android 16 publica 42 imágenes reales de 7 escenas ×
  3 fases × 2 paletas. Una previsualización HTML de estas imágenes
  es inspección, no recreación del motor de Android.

## Fuentes técnicas

- Android `Canvas`: https://developer.android.com/reference/android/graphics/Canvas
- Android `Path.cubicTo`: https://developer.android.com/reference/android/graphics/Path
- Android `Paint`: https://developer.android.com/reference/android/graphics/Paint
- Godot cutout y huesos: https://docs.godotengine.org/en/4.4/tutorials/animation/cutout_animation.html
- Godot Skeleton2D: https://docs.godotengine.org/en/4.5/classes/class_skeleton2d.html
- Spine slots y orden: https://en.esotericsoftware.com/spine-slots

## Limitaciones

El sistema continúa siendo un avatar 2D de proyecciones por ejercicio,
no un rig 3D arbitrariamente girable. Las restricciones detectan muchos
errores mecánicos y de diseño, pero ningún conjunto de reglas garantiza
que toda futura animación sea biomecánicamente exacta o perfecta.
Cada escena nueva debe proporcionar sus contactos y pruebas.
