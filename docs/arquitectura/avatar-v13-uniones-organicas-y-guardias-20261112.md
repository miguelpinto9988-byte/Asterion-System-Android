# Asterion V13 — suavizado orgánico, guardias y revisión de integración

## Alcance y causa detectada
El avatar V12 compartía puntos articulares matemáticamente correctos
mediante IK, pero el dibujo añadía círculos independientes en codos/rodillas,
y rodilleras rectangulares que sobresalían cuando las cadenas se flexionaban.
Se conservaron las proporciones, paletas y el nuevo diseño posterior de
zapatillas; se cambió únicamente el modo de representar las uniones.

## Cambios Android Java + Canvas
- `AvatarOrganicVolume`: nuevo contorno continuo de dos huesos
  `root → joint → end` mediante curvas `Path.cubicTo`.
  Utiliza una normal bisectriz y un **radio de puente limitado** por
  ancho del miembro y longitudes reales; lo comprime al plegarse.
  No dibuja esferas superpuestas en las bisagras. Cuando el pliegue
  aproxima 180 grados, sustituye la bisectriz degenerada por la normal
  entrante sin picos de tipo miter.
- `AvatarRenderer`: ambos brazos y piernas tienen una envolvente
  continua, con mangas ancladas al hombro y el puño existente. Las
  antiguas rutinas `drawLimb` siguen disponibles para accesorios,
  pero no se emplean como dos cilindros desunidos en las rodillas
  o codos principales.
- `AvatarMultiViewRenderer`: piernas y piel de brazos frontales/cenitales
  comparten el mismo algoritmo, sin discos de rodilla de otro color.
- `AvatarArtDetails`: el antiguo refuerzo de rodilla de 21x22 píxeles
  se sustituyó por una **costura Bézier orgánica**, más estrecha y
  contenida en el contorno del pantalón.
- `AvatarFrameSafety`: contrato por cámara antes del render: mantiene
  el esqueleto canónico V11 (14 articulaciones), verifica cabeza/cuello,
  pies plantados, contactos de palmas y punteras, fases aéreas y
  alineación de hombros frontales. Errores geométricos no finitos o
  colapsados generan excepciones explícitas, no un dibujo deformado.
- `NativeAvatarView`: invoca las guardias antes de dibujar cada frame.

## Verificación automatizada
- `AvatarOrganicVolumeTest`: anchuras acotadas, continuidad y
  muestreos completos de las vistas frontal, cardio y cenital.
- `AvatarFrameSafetyTest`: todos los presets laterales y cámaras,
  241 fases por escena, y rechazo de un pie marcado erróneamente
  como plantado durante un salto.
- `NativeAvatarRenderTest`: añade comprobación de píxeles orgánicos:
  el pivote tiene cobertura, sin disco desprendido fuera de la cadena.
- Se conservan las pruebas V12, los 42 PNG del renderer Android real
  (7 escenas × 3 fases × 2 paletas), Android Lint, JUnit y emulador
  Android 16; el workflow entrega los fotogramas en el artefacto
  `asterion-runtime-diagnostics`.

## Referencias técnicas, sin reutilizar activos
- Android `Path.cubicTo`: https://developer.android.com/reference/android/graphics/Path
- Android Canvas, `drawPath` y `save()/restore()`:
  https://developer.android.com/reference/android/graphics/Canvas
- Godot 2D Skeletons, pesos y deformación de polígonos:
  https://docs.godotengine.org/en/stable/tutorials/animation/2d_skeletons.html
- Godot cutout animation, jerarquía de piezas:
  https://docs.godotengine.org/en/stable/tutorials/animation/cutout_animation.html
- Spine slots y orden de capas:
  https://en.esotericsoftware.com/spine-slots

Las referencias se usaron para principios de dibujado, sin importar
código, motores, permisos, assets o bibliotecas externas.

## Seguridad y límites
Son comprobaciones de **integridad geométrica del avatar**, no
instrucciones de ejercicio ni certificación de biomecánica humana.
Los modos continúan siendo varias proyecciones 2D diseñadas para cada
escena, no una malla 3D que permita una cámara giratoria libre.

No se modifica `main` ni se debe integrar la PR hasta revisar las
capturas reales y aprobar la apariencia.
