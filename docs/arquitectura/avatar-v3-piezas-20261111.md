# Avatar Asterion V3 — rediseño individual de piezas

**Dirección de arte:** opción D aprobada. Rama experimental apilada sobre PR #18.
**Objetivo:** aumentar el detalle VISUAL visible, no volver a modificar únicamente arquitectura de clases.

## Piezas dibujadas y ensambladas
- **Cabeza:** nueva silueta, oreja, cabello por curvas, cejas, ojos con brillo, nariz, mejilla y sonrisa.
- **Torso:** camiseta con panel lateral y costura técnica, trama de puntos y pequeño motivo geométrico sin sustituir el logo oficial.
- **Brazos:** manga con dobladillo, textura de antebrazo y accesorio de muñeca; la decoración se ancla al hueso correspondiente.
- **Pelvis:** cinturilla independiente y marcas de bolsillo sobre el pantalón.
- **Muslos y pantorrillas:** pantalón deportivo ajustado de acabado denim oscuro con paneles de rodilla, pespuntes, pliegues y puño de tobillo.
- **Zapatillas:** talón, parte superior, puntera, tres cordones y suela separados al dibujar.
- **Manos:** palma y pulgar más expresivos, vinculados a la posición de muñeca.

`AvatarArtDetails` contiene las capas estéticas; `AvatarRenderer` las ensambla y `AvatarKinematics` determina las articulaciones. La secuencia actual sigue siendo una sentadilla ilustrativa, no un modelo biomecánico ni una instrucción de ejercicio. `NativeAvatarView` mantiene tiempos y accesibilidad, sin nuevos permisos, SDK ni imágenes binarias de la APK.

## Vista frontal vs. animación
El concepto frontal completo se compone de 16 capas SVG de referencia externa; el programa Android continúa usando el punto de vista lateral de la sentadilla. La decoración lateral se inspira en las mismas prendas y colores, pero no es una proyección exacta 3D del dibujo frontal. Los 16 SVG no se empaquetan en la APK, de acuerdo con el contrato existente.

## Control de calidad
- Android JUnit, lint, assembleDebug.
- Pruebas instrumentadas de ambos avatares, resoluciones y detalles V3.
- Contratos de separación de rig/visual y de ausencia de nuevos motores.
- Visor HTML independiente con personaje ensamblado, despiece y movimiento.
- Revisión visual manual en hardware físico y aprobación antes de fusionar.

Fuentes de render Android: https://developer.android.com/develop/ui/views/layout/custom-views/custom-drawing y https://developer.android.com/topic/performance/issues/render .
