# Asterion V13 — estabilidad y articulaciones orgánicas

## Alcance
La implementación añade un guardarraíl geométrico y una piel de
articulación compartida entre cámaras lateral, frontal y superior.
Mantiene el rig 2D y las proporciones ya aprobadas.

### Comandos internos
- `AvatarPoseGuard.requireLink(a,b)`: coordenadas finitas y segmento no colapsado.
- `AvatarPoseGuard.requireWidth(width)`: ancho finito dentro de límites de diseño.
- `AvatarPoseGuard.hingeAngle(root,hinge,tip)`: ángulo proyectado calculado de manera segura.
- `AvatarPoseGuard.requireRig(frame)`: valida ambos codos, ambas rodillas y cuello/cabeza.
- `AvatarOrganicJointSkin.draw(...)`: puente orgánico Bézier ligado a las
  coordenadas reales de las articulaciones, sin círculos independientes.
- `AvatarSkeletonFrame.fromSide/fromCamera`: longitudes y proporciones
  ya validadas por la capa IK anterior.
- `AvatarContactFrame`: estado de apoyos por movimiento y cámara.

### Cambios gráficos
- Perfil: se sustituyen los círculos de codos y rodillas por
  uniones continuas del mismo color que el segmento.
- Frontal: se elimina la placa rectangular de la rodilla y se dibuja
  un contorno suave que conecta muslo y pantorrilla.
- Superior: se integran los codos y rodillas en el mismo sistema.
- Se conservan las prendas, la cara, el calzado posterior V12 y las
  superficies visibles ya corregidas.

### Seguridad e integración
Las nuevas comprobaciones rechazan coordenadas no finitas, segmentos
degenerados y anchos extremos antes de emitir rutas gráficas a Canvas.
El renderizado no realiza peticiones de red ni introduce permisos o
dependencias adicionales. El dibujo utiliza Paint y Path reutilizados,
sin clipping costoso.

### Pruebas
`AvatarV13PoseGuardTest`: barrido de 181 fases para las seis escenas
laterales y cinco modos multivista, articulaciones conectadas, límites
numéricos y apoyos de flexión diamante/sentadilla.
`tests/avatar_v13_organic_rig_contract.py`: contrato estático.
El workflow ejecuta además pruebas anteriores, Android Lint,
compilación e instrumentación de Android 16.

### Fuentes
- Android Canvas: https://developer.android.com/reference/android/graphics/Canvas
- Android Path: https://developer.android.com/reference/android/graphics/Path
- Godot skeleton/cutout: https://docs.godotengine.org/en/4.4/tutorials/animation/2d_skeletons.html
- Rive bones: https://help.rive.app/editor/manipulating-shapes/bones

### Límite
Los ángulos son proyecciones ilustrativas 2D, no rangos clínicos
de articulaciones humanas. Un rig 3D completo y deformación
de malla continua requerirían una arquitectura distinta.
