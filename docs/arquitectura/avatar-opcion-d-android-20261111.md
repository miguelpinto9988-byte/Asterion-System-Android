# Avatar Asterion — opción D modular en Android Java

## Estado y alcance
Implementación experimental sobre `feature/avatar-silueta-organica-20261111` (PR #17).
La dirección visual D fue aprobada, pero **la integración en main y el APK release no están autorizados**.

La escena conserva los personajes azul y morado ya accesibles en la APK de desarrollo,
su ejemplo ilustrativo de sentadilla, sus controles y las imágenes de marca existentes.

## Arquitectura
- `AvatarBodySpec` contiene longitudes y anchuras virtuales inmutables (no mediciones humanas ni recomendaciones corporales).
- `AvatarPose` almacena articulaciones congeladas por fotograma y separadas de Android.
- `AvatarKinematics` mantiene cinemática inversa de pierna y brazo, con sobrecarga `squat(depth, spec)` para ensayar proporciones.
- `AvatarPalette` contiene paletas inmutables existentes de azul y morado.
- `AvatarRenderer` dibuja en Canvas un orden de capas establecido: extremidades alejadas, extremidades próximas, pelvis/ropa, torso, cuello/cabeza, brazos/manos y detalles.
- `NativeAvatarView` conserva solo ciclo de vida, accesibilidad, animación, escalado y controles.
- `AvatarPlaybackController` sigue reteniendo preferencias de reproducción entre cambios de visibilidad.

## Detalles de diseño
Trazos con curvas Bézier, uniones solapadas sin anillos oscuros, cintura y pecho perfilados, acentos deportivos de la opción D y anchos parametrizables. Las posiciones se expresan en **unidades virtuales de un Canvas 400×480**; no se convierten en centímetros ni se usan para evaluar cuerpos. La figura es un recurso visual para identificar un ejercicio, **no enseña técnicas clínicas ni evalúa la postura de una persona**.

## Estado de la animación
Solamente la sentadilla ilustrativa está implementada. Las otras actividades no deben asociarse a nuevas animaciones hasta contar con poses específicas revisadas. Los tobillos permanecen apoyados en esta secuencia; huesos de brazos y piernas conservan las longitudes de `AvatarBodySpec`.

## Validaciones
- Contratos Python de separación de capas, paletas, rig y continuidad visual.
- JUnit: longitudes constantes en ciclo, proporciones alternativas, entradas inválidas y trayectoria continua.
- AndroidTest: paletas, accesibilidad y render en varias resoluciones y temas.
- GitHub Actions: debug, lint, JVM y emulador Android 16.
- Pendiente: evaluación en teléfono físico y aprobación explícita de la integración.

## Referencias
- https://developer.android.com/develop/ui/views/layout/custom-views/custom-drawing
- https://developer.android.com/topic/performance/issues/render
