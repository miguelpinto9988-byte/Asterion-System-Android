# Asterion V9 — identidad visual y continuidad entre perspectivas

La V8 mostraba diferencias notables entre lateral y frontal/superior no
originadas por IK: dos renderers tenían **ilustraciones distintas** para
codos, mangas, denim, zapatillas, manos y rostro. La V9 cambia únicamente
los acabados gráficos, conservando los ejercicios V8 y sus poses.

## Criterio aprobado: preservar la identidad lateral
- **Rostro:** el frontal reutiliza la geometría cromática (piel, sombreado,
  cabello asimétrico y mecha, cejas, boca discreta) de la vista lateral.
  El cenital pinta la coronilla con ese mismo peinado, sin ojos flotantes.
  La escala de cabeza deriva de `AvatarBodySpec.headScale`, no se reajusta
  de manera arbitraria por cámara.
- **Hombros:** la manga de front/top cubre aproximadamente el 32% del
  brazo superior; el resto es piel, con la misma ubicación de codos y
  muñecas del rig. Se corrige el fallo de V8 donde todo el brazo superior
  cenital aparecía azul.
- **Codos y antebrazos:** relleno continuo y contornos orgánicos de
  ancho variable mediante curvas `Path.cubicTo`, sin discos ornamentales
  que agrandan una articulación. Puño/costura sigue el radio real de hueso.
- **Jeans:** los tres perfiles llaman al módulo
  `AvatarArtDetails.drawDenimLeg` para costuras y refuerzo de rodilla,
  superpuesto sobre extremidades que tienen ancho derivado de
  `AvatarBodySpec.thighWidth/calfWidth`.
- **Zapatillas:** una rutina para proyecciones frontal/cenital utiliza
  los mismos materiales y capas del perfil: zapatilla blanca, panel azul,
  lengüeta, cordones, talón y suela clara.
- **Manos:** en frontal/cenital dejan de ser círculos sólidos y reciben
  palma/pulgar y pliegue fino con la misma tinta de piel lateral. En la
  flexión en diamante permanece el recuadro auxiliar para dedos.
- **Ropa:** tonalidades de gradiente armonizadas con el renderer lateral,
  sin imponer proporciones físicas idealizadas. El cuello corto sigue
  representado en las tres cámaras.

## Arquitectura
- `AvatarArtDetails`: nueva superficie pública reutilizable para
  `drawHeadFront`, `drawHeadTop`, `drawProjectedSneaker`,
  `drawProjectedHand`, `drawCameraJeans`, y antebrazo por paleta.
- `AvatarMultiViewRenderer`: usa ese módulo y un `tapered` Bézier
  de trazo continuo, con el mismo `AvatarBodySpec` que la lateral.
- `NativeAvatarView`: comparte las proporciones con ambos renderers.
- `AvatarRenderer`: pasa la paleta al acabado del antebrazo.

No se modifica cinemática inversa, longitudes óseas, fases, efectos de
pantalla ni permisos. La panorámica superior continúa siendo una
ilustración específica 2D, no una rotación 3D literal.

## Referencias
- Android `Canvas.drawPath`, `Path.cubicTo`, `save()/restore()`:
  https://developer.android.com/reference/android/graphics/Canvas
  https://developer.android.com/reference/android/graphics/Path
- Android, custom drawing y reutilización de Paint:
  https://developer.android.com/develop/ui/views/layout/custom-views/custom-drawing
- Godot, jerarquías de piezas recortadas y pivotes:
  https://docs.godotengine.org/en/4.4/tutorials/animation/cutout_animation.html
- Spine Basic Concepts, draw order, attachments y slots:
  https://en.esotericsoftware.com/spine-basic-concepts
- OpenStax, articulaciones del hombro y codo:
  https://openstax.org/books/anatomy-and-physiology-2e/pages/9-6-anatomy-of-selected-synovial-joints

Las referencias enseñan decisiones de proyección y capas, sin copiar
ilustraciones ni instalar esos motores.

## Validación
- Contrato estático de uso compartido de detalles de dibujo.
- Instrumentación Android: dibujar cabeza frontal y cenital, manos y zapatos,
  más 6 cámaras x 6 fases x 2 paletas y tamaños previos de prueba.
- Android Lint, tests unitarios, APK debug, emulador 16 y capturas reales
  antes de fusionar. HTML es recreación independiente, no screenshot de APK.

**Alcance:** trabajo estético sobre el avatar ilustrativo. La biomecánica
por ejercicio y la perspectiva ideal quedan para la próxima etapa,
previa revisión de la apariencia.
