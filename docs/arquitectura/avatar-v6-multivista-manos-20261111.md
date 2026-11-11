# Avatar Asterion V6 — cámaras animadas y manos específicas

**Implementación:** rama de revisión sobre PR #21; ningún cambio a main.

## Problema resuelto
Una lámina de personaje con frontal/perfil/superior **no es una animación**.
El render lateral existente continúa con sus cinco movimientos. Se han añadido
dos dibujos 2D independientes, sin girar simplemente una silueta lateral:

- **Frontal / elevación de brazos:** hombros izquierdo y derecho a la misma
  altura, cuello corto, cara frontal, camiseta y pantalón coherentes con la
  identidad visual. Ambos brazos recorren un arco completo, cada uno con
  brazo superior y antebrazo de longitud constante mediante IK.
- **Superior / flexión en diamante:** cámara mirando al suelo; se ve la coronilla
  y la espalda del personaje en una composición de cuerpo orientado en el plano
  horizontal. Dos brazos articulados convergen en manos próximas bajo el
  pecho; las manos y los pies permanecen anclados en su referencia de suelo.
  Un panel ampliado permite distinguir dedos/abertura central y una vista
  lateral auxiliar representa la subida/bajada que se pierde al mirar desde
  arriba.

La ampliación de las manos es **simbólica**. No es un verificador de técnica
ni un procedimiento normativo para colocar las manos o ejecutar flexiones.
La vista superior no puede mostrar por sí sola la altura del cuerpo.

## Arquitectura
- AvatarMultiViewKinematics: dos vistas ortográficas de pose derivadas del
  AvatarBodySpec y restricciones IK existentes.
- AvatarMultiViewRenderer: Canvas con capas propias de cada cámara, paleta
  compartida, detalles faciales correctos para cada proyección y paneles
  de manos/altura.
- NativeAvatarView: selector de cámara, envía a renderer lateral o multivista,
  mantiene pausa, fase, velocidad y descripción accesible.
- NativeAvatarActivity: selector de tres cámaras y guardado/restauración.
- No se añaden SDK, JNI, motor 3D, imágenes externas ni permisos.

## Límites y ampliación futura
Las cámaras son **proyecciones 2D diseñadas por separado**, no un rig 3D
coherente de una única geometría al rotar libremente. Para un catálogo completo
de ejercicios se deberá definir por ejercicio: cámara adecuada, colocación
de apoyos, trayectorias, poses legibles y ampliaciones de manos/pies según
corresponda. No se asignan automáticamente estos patrones al catálogo.

## Fuentes
- Android Canvas: https://developer.android.com/reference/android/graphics/Canvas
  (save/restore, scale, transformaciones por capa).
- Android Custom Drawing:
  https://developer.android.com/develop/ui/views/layout/custom-views/custom-drawing
- Referencia ilustrada sobre agrupación de manos (no copiada):
  https://illustratedworkout.com/exercises/diamond-push-up/

## Validación
Contratos Python, pruebas JUnit de longitudes/continuidad/segmentos anclados,
instrumentación Android en ambas cámaras, Android Lint y APK debug. Revisar en
teléfono físico antes de integrar. La previsualización HTML es una recreación
ilustrativa interactiva, no una captura del emulador.
