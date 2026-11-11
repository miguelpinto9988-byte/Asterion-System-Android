# Estudio de avatar orgánico y articulación estable — 11 de noviembre de 2026

**Estado:** propuesta técnica en rama independiente. No está fusionada en producción.
Base: `feature/avatar-controles-estables-20261111` (PR #16).

## Dirección de arte
La vista anterior usaba piezas tubulares con anillos oscuros alrededor de cada articulación. Este prototipo sustituye el contorno por curvas Bézier rellenas con extremos redondeados, superposición suave en los codos y rodillas y un contorno más sutil. Se dibuja un tronco perfilado por una curva longitudinal con cintura, pecho y hombros diferenciados, sin crear un cuerpo idealizado ni interpretar la fisiología de una persona.

Se conservan exactamente las paletas azul y morada, el cabello, calzado, la escena, los controles existentes y la animación didáctica de sentadilla. No se cambia la configuración de perfil, el menú principal, el catálogo ni los recursos oficiales de marca.

## Motor de articulación
`AvatarKinematics` conserva la cinemática inversa de cadera-rodilla-tobillo y el pie fijo. Adicionalmente los brazos usan cinemática inversa de dos segmentos (hombro-codo-muñeca), con longitudes constantes de 58 y 63 unidades virtuales. Los objetivos de muñeca interpolan una trayectoria continua. La posición del codo se obtiene siempre de la misma rama geométrica para evitar saltos entre poses.

Las proporciones y poses son **ilustrativas**: no miden la postura, no aseguran una técnica correcta ni sustituyen orientación profesional. No se agregan entrenamientos ni recomendaciones de intensidad.

## Pruebas
- JUnit: conservación de longitudes de ambos brazos, continuidad en 201 muestras de profundidad, rechazo de longitudes inválidas, más las pruebas anteriores de pierna/torso.
- Contrato Python: tronco orgánico, curvas, capa de unión sin anillos oscuros y ausencia de motor externo.
- CI: pruebas existentes, `assembleDebug`, Android Lint y ejecución instrumentada Android 16.
- Revisión visual manual pendiente en teléfonos de distintos tamaños y temas claro/oscuro.

## Limitaciones y propuesta de siguiente fase
No se recibió una fotografía de referencia para cotejar fidelidad facial, vestimenta o proporciones. La previsualización HTML permite comparar diseño actual y nueva propuesta; la aprobación del aspecto visual debe preceder a cualquier cambio definitivo en `main`.

No se introducen mallas de deformación ni esqueletos de Rive/Spine; el renderer sigue usando Java + Canvas y no necesita dependencias nuevas.

Referencias oficiales: https://developer.android.com/develop/ui/views/layout/custom-views/custom-drawing.html y https://developer.android.com/develop/ui/views/graphics/hardware-accel
