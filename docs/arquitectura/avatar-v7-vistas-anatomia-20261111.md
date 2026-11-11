# Asterion V7: revisión estética y selección de cámaras

Rama basada en PR #22 (en borrador). El objetivo es mejorar el avatar
Android existente, **sin copiar diseños externos ni tocar main**.

## Diagnóstico
- El cuello lateral estaba oculta por la cabeza, debido a que el centro del
  cráneo se encontraba a sólo 18 unidades por encima del hombro; se desplaza
  a 33 unidades y un accesorio `drawShortNeck` lo une al cuello de camiseta.
- La frontal usaba posiciones absolutas para piernas y vestimenta: no podía
  representar una sentadilla real. Ahora `drawFront` recibe los puntos
  articulados calculados por cada ejercicio y los dibuja en orden correcto.
- La sentadilla abierta frontal requiere objetivos propios de ambas piernas;
  `frontWideSquat` tiene tobillos fijos y rodillas proyectadas hacia fuera,
  con la longitud del muslo y la pantorrilla invariantes.
- La superior antigua parecía una persona de pie representada hacia abajo;
  ahora la espalda oculta las manos situadas debajo de la zona del torso,
  se ve la coronilla y el calzado aparece en el extremo contrario al rostro.
  Una ampliación de manos y una minivista lateral siguen disponibles para
  expresar lo que la cámara cenital no deja ver.

## Referencias
- Planos anatómicos (OpenStax):
  https://openstax.org/books/anatomy-and-physiology-2e/pages/1-6-anatomical-terminology
- Orden de capas y accesorios en animación esquelética (Spine):
  https://en.esotericsoftware.com/spine-basic-concepts
- Skins y piezas reutilizables (Spine):
  https://en.esotericsoftware.com/spine-skins
- Transformaciones y pintura Canvas Android:
  https://developer.android.com/reference/android/graphics/Canvas
  https://developer.android.com/develop/ui/views/layout/custom-views/custom-drawing

La referencia de Spine es conceptual; no se integra su SDK ni se transfieren
ilustraciones. Se mantienen Gradients, Canvas y rutas Java propios.

## Cámaras
1. Lateral: cinco movimientos ilustrativos del renderer V5, con cuello corto.
2. Frontal de brazos: elevación completa con cuello, hombros a igual altura y
   codos independientes.
3. Frontal de sentadilla abierta: rodillas y piernas con dos IK independientes
   y tobillos anclados; torso y hombros bajan juntos.
4. Superior de flexión en diamante: tronco orientado hacia suelo, palmas ocultas
   parcialmente desde la proyección cenital, esquema de manos ampliadas y
   vista de altura auxiliar.

**Limitación:** cámaras 2D específicas, no rotación libre de un esqueleto 3D;
nada demuestra exactitud biomecánica clínica. Las posiciones son ilustrativas,
no recomendaciones para ejecutar ejercicios.

## Pruebas
- 241 muestras de sentadilla frontal en JUnit: longitudes fijas, hombros
  nivelados, rodillas opuestas y suelos anclados.
- Distancias entre cabeza, cuello y hombros en tres cámaras.
- AndroidTest para escena frontal adicional y accesibilidad.
- Contrato estático, pruebas de emulador y revisión HTML.
