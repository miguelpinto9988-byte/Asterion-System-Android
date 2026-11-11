# Asterion V10 — catálogo de orientación y controles biomecánicos del avatar 2D

## Diagnóstico y corrección del caso de flexión diamante
Antes, `AvatarMultiViewRenderer.drawDiamondTop` llamaba a
`AvatarArtDetails.drawProjectedSneaker(..., true)`, que proyectaba las
zapatillas con una franja de suela frontal: no distinguía la parte superior
visible desde el techo de la planta orientada al suelo.

Ahora `drawProneTopSneaker` pinta el empeine, cordones, talón cercano
a la rodilla, puntera más allá del tobillo y una pequeña zona de
contacto, *sin mostrar toda la suela mirando hacia arriba*. Deriva el
ángulo de la espinilla (`atan2`) y rechaza un eje de pie invertido. El
modo `TOP` valida antes de dibujar cabeza, cuello, caderas, rodillas
y tobillos a través de `requireProneTopPose`.

La cara lateral también gira de acuerdo con la inclinación del
torso, sobre todo al pasar de postura erguida a horizontal en el burpee:
`Canvas.rotate(sideFacingDegrees(hip,shoulder),head.x,head.y)`.

## Contrato geométrico reutilizable
`AvatarOrientationRules` es Java puro (no SDK nuevo):
- `Vector3`: ejes finitos, producto escalar, longitud, normalización.
- `BodyFrame`: eje de cabeza, normal facial, normal de planta y
  dirección de puntera.
- `Posture.STANDING` y `Posture.PRONE_GROUND_FACING`.
- `Camera.FRONT/SIDE/TOP`: cada cámara posee vector hacia observador.
- `project(v,camera)`: proyección ortográfica por cámara. Frontal
  (x,-y), lateral (z,-y), cenital (x,z).
- `visibleSurface(normal,camera)`: superficie visible si el producto
  escalar con la dirección al observador es positivo.
- `visibleShoeSurface(camera,frame)`: elige empeine o planta.
- `topToeSign(frame)`: confirma dirección de puntera hacia el extremo
  distal de la figura en la vista superior.
- `requireProneTopPose(p)`: rechaza el orden de huesos invertido.
- `sideFacingDegrees(hip,shoulder)`: sincroniza la cara con la columna
  en el plano lateral.

Los ejes son **virtuales y representativos**, no mediciones clínicas
ni sustituyen una simulación 3D.

## Catálogo de APIs y técnicas investigadas

| Capa | Funciones / técnicas | Uso recomendado |
|---|---|---|
| Android Canvas | `save`, `restore`, `translate`, `rotate`, `scale`, `concat`, `clipPath` | Accesorios relativos a huesos y cámara |
| Android Path | `moveTo`, `quadTo`, `cubicTo`, `transform`; `Canvas.drawPath` | Extremidades articuladas con costuras y oclusión |
| Álgebra 3D Android | `android.opengl.Matrix.setIdentityM`, `setRotateM`, `multiplyMM`, `multiplyMV`, `invertM`, `setLookAtM` | Coordenadas locales y proyecciones futuras |
| Espacio anatómico | normales unitarias, `dot`, `cross`, bases ortogonales, `atan2`, validación finita | Evitar superficies visibles al revés |
| Cinemática directa (FK) | jerarquía padre-hijo, pivotes y composición de rotaciones | Cara, cuello, tronco y manos siguen el esqueleto |
| Cinemática inversa (IK) | dos huesos analíticos, FABRIK, CCD, Jacobiano amortiguado (DLS) | Posicionar manos/pies sin estirar brazos ni piernas |
| Restricciones articulares | límites de ángulo, plano de flexión, pole vectors, longitud fija | Evitar inversión de codos y rodillas |
| Contactos | fase de apoyo/vuelo, objetivos anclados, normales de suelo, foot locking | Evitar patinaje de pies y manos flotantes |
| Orientación de superficies | normal exterior, dirección de puntera, visibilidad por producto escalar | No exhibir suela mirando al techo en postura prona |
| Interpolación | `smoothstep`, ángulo modular, `slerp` para quaterniones al pasar a rig 3D | Evitar salto brusco en rotaciones |
| Rig y render | huesos/slots por separado, draw order, pesos de vértice, blend skinning | Uniformidad de prendas y profundidad de capas |
| Validación de movimiento | cierre de ciclo, continuidad, longitudes, contactos, pies/rostro/suela, capturas multivista | Detectar errores antes de añadir ejercicios |

**No son «comandos mágicos» ni se deben descargar indiscriminadamente**.
Varios pertenecen a otros motores y se han estudiado como referencia, sin
integrarlos ni crear dependencias. Ningún conjunto finito de reglas puede
garantizar que toda futura animación sea biomecánicamente correcta; cada
nuevo movimiento requiere objetivos, límites, cámaras y pruebas propios.

## Documentación de referencia
- Android Canvas: https://developer.android.com/reference/android/graphics/Canvas
- Android Path: https://developer.android.com/reference/android/graphics/Path
- Android 3D matrices: https://developer.android.com/reference/android/opengl/Matrix
- Godot Skeleton2D y su jerarquía de huesos: https://docs.godotengine.org/en/4.5/classes/class_skeleton2d.html
- Godot animación cutout, IK de apoyo: https://docs.godotengine.org/en/4.4/tutorials/animation/cutout_animation.html
- Godot 2D skeleton deformaciones: https://docs.godotengine.org/en/4.4/tutorials/animation/2d_skeletons.html
- Spine draw order y slots: https://en.esotericsoftware.com/spine-slots
- Rive mallas: https://help.rive.app/editor/manipulating-shapes/meshes
- OpenStax terminología anatómica: https://openstax.org/books/anatomy-and-physiology-2e/pages/1-6-anatomical-terminology
- OpenStax miembro inferior: https://openstax.org/books/anatomy-and-physiology-2e/pages/8-4-bones-of-the-lower-limb
- Referencia visual cenital de flexión diamante: https://classpass.fr/movements/diamond-push-up

## Pruebas
- 7 pruebas Java de ejes, superficies, proyección, orientación de rostro
  y muestreo de las 361 fases de flexión diamante.
- Prueba instrumentada de ambas paletas que representa zapatilla superior,
  contacto distal, rechazo de pierna invertida y cinco fotogramas.
- Contrato Python en Android CI y el mismo control de estilos de V9.
- Comparador HTML V10 que reproduce la orientación en pantalla:
  **no es una captura real de APK**.

## Límites
No se ha incorporado un solver 3D ni una cámara libre. Las proyecciones
siguen siendo 2D específicas por ejercicio. Las normales de suelo y de
zapatilla modelan la pose ilustrada de la flexión, no todos los posibles
ángulos reales del pie. La siguiente ronda debe extender `BodyFrame`
a cada movimiento y validar contactos con fotogramas reales del emulador.
