# Asterion V11: corrección de calzado y consolidación de vistas

## Defecto que explica por qué la V10 seguía mostrando los pies al revés

La V10 usó `topToeSign(PRONE_FRAME)` con signo positivo, de modo que
un avatar boca abajo con la cabeza arriba y las piernas abajo dibujaba
la **puntera de cada zapatilla hacia la parte inferior de la imagen**.
Esto se había justificado a partir de la dirección visual de la espinilla
(rodilla → tobillo), pero en la flexión ilustrada con apoyo de punteras
**la zapatilla está flexionada en el tobillo: la puntera apunta hacia la
cabeza**. Un cambio cosmético anterior ocultó una parte de la suela sin
corregir la dirección axial. Por eso el usuario seguía detectando el error.

Se corrige:
- `AvatarOrientationRules.PRONE_FRAME.toeAxis = (0,0,-1)`, dirección
  del pie hacia la cabeza (pantalla superior, Y negativo), independiente
  del eje de la espinilla.
- `AvatarFootOrientation.proneTop()` calcula la dirección de ambos zapatos
  desde el eje cabeza↔centro de caderas y los conserva paralelos durante
  toda la animación; puntera a 17 unidades hacia la cabeza, talón 12 unidades
  en dirección contraria, tobillo en el mismo pivote de la pierna.
- `AvatarArtDetails.drawProneTopSneaker` usa esa dirección, con puntera
  al extremo superior, un panel de empeine visible, tres cordones y un
  talón distinto en el extremo inferior. No dibuja una gran suela
  hacia la cámara.
- Las prendas y la textura del avatar lateral permanecen compartidas.

## Puntos 1–3 del control de calidad

**1. Capturas reales Android.** La prueba instrumentada
`exportRealAndroidMultiviewFramesForReview` captura el verdadero
`NativeAvatarView.draw(Canvas)` a 400×480, con dos paletas, siete
escenas (sentadilla y burpee laterales, elevación, sentadilla abierta,
tijeras, codos, flexión diamante) y tres fases (0, .49, 1).
El job de emulador usa `adb pull` para exportar **42 PNG auténticos** a
`build/runtime-diagnostics/asterion-v11-evidence`, que se publica en
el artefacto `asterion-runtime-diagnostics`. Un HTML nunca sustituye
esta evidencia.

**2. Esqueleto compartido.** `AvatarSkeletonFrame` convierte los puntos
de `AvatarPose` y `AvatarMultiViewKinematics.Pose` a un inventario
de 14 articulaciones canónicas, con el mismo `AvatarBodySpec`,
verifica que los huesos no colapsen y valida longitudes de los cuatro
segmentos de brazos y piernas antes de pintar. Las proyecciones de
brazos cenitales usan el factor conocido .8, sin interpretarlo como
cambio anatómico. Las proyecciones continúan siendo 2D independientes,
no una estructura 3D girable.

**3. Orientación y apoyos.** `AvatarFootOrientation.FootProjection`
expone tobillo, talón, punta, contacto en puntera, cara dorsal
visible y rotación de zapato. `AvatarOrientationRules` conserva las
normales faciales, del torso y de manos, las cámaras y la comprobación
de superficies. Una comprobación de pares prohíbe que cada zapatilla
rote por separado en pantalla por diferencias entre espinillas.

## Control explícito de contactos para todas las escenas

`AvatarContactFrame` registra de manera separada qué está apoyado:
`Ground.SOLE`, `Ground.TOE`, `Ground.PALM` o `Ground.NONE`.
Esto se asigna desde el movimiento, nunca desde la silueta proyectada:

- Superior / flexión diamante: ambas palmas y ambas punteras apoyadas.
- Frontal / sentadilla, elevación y codos: ambos pies apoyados por la suela.
- Frontal / saltos de tijera: fase aérea separada de las fases con apoyo.
- Lateral / marcha: un pie permanece en el suelo y el otro alterna contacto.
- Lateral / burpee: manos apoyadas en la etapa horizontal y pies en
  desplazamiento en las fases de transición.

`NativeAvatarView` consulta `forCamera`/`forSide` antes de pintar
y la cámara cenital comprueba `assertProneUpperCamera`. Se evita que
una variación del dibujo infiera por error que un pie o palma está apoyado.

## Hallazgos de inspección de Android real

Las 42 capturas obtenidas de Android 16 evidenciaron un defecto adicional:
el cabello de la postura lateral erguida quedaba parcialmente fuera del
borde superior. Se redujo el zoom lateral de 1.00 a 0.94, usando como
pivote la línea de los zapatos `(200,364)`: la cabeza entra dentro de
la tarjeta sin levantar ni desplazar los apoyos.

## Pruebas
- `AvatarV11OrientationTest`: punteras, ejes simétricos, longitud de
  segmentos, cámaras frontal y cenital, las seis variantes de perfil y
  validación de argumentos.
- `AvatarContactFrameTest`: seis tests de estados de contacto explícitos
  y fases de apoyo, vuelo o transferencia de peso.
- `AvatarOrientationRulesTest`: corrige la aserción de V10, que
  incorrectamente esperaba que la puntera apuntara hacia los pies.
- `NativeAvatarRenderTest`: rendering real de la parte superior del
  zapato, puntera y talón; exportación de 42 PNGs por instrumentación.
- `tests/avatar_v11_rig_and_shoes_contract.py`: guardarraíl para
  impedir que vuelva a usarse `knee→ankle` como eje de puntera.

## Fuentes técnicas
- Android `Canvas.rotate`, `Canvas.save`, `Canvas.drawPath`:
  https://developer.android.com/reference/android/graphics/Canvas
- Android `Bitmap.compress` (PNG del renderer real):
  https://developer.android.com/reference/android/graphics/Bitmap
- Godot animación recortada, huesos/slots:
  https://docs.godotengine.org/en/4.4/tutorials/animation/cutout_animation.html
- OpenStax, huesos y articulaciones del miembro inferior:
  https://openstax.org/books/anatomy-and-physiology-2e/pages/8-4-bones-of-the-lower-limb

**Límite:** son ilustraciones 2D para visualización de un ejercicio.
La definición de apoyo/pronación es específica de este movimiento. No
valida postura de personas ni modela rotación libre de cuerpo 3D.
