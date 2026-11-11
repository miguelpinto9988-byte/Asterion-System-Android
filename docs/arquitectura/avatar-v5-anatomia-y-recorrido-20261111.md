# Asterion V5 — perfil corporal y arcos de extensión

Dirección artística: avatar 2D de adulto vestido de complexión cotidiana, sin imponer
un canon físico ni evaluar cuerpos. Apilado sobre PR #20, sólo revisión.

## Problemas diagnosticados en V4 y correcciones
1. **Brazo posterior visible por encima del hombro:** el pivote de ese brazo
   estaba demasiado cerca del contorno superior y su dibujo comenzaba con una
   cápsula de piel. La V5 lo posiciona por debajo y detrás del torso; dibuja
   primero manga y brazo posterior, después torso, y comienza la manga delantera
   ligeramente por debajo del pivote. Queda pendiente revisar oclusiones en
   una pantalla Android real.
2. **Piernas permanentemente flexionadas:** la separación cadera-tobillo
   inicial era menor que la suma de muslo + pantorrilla. V5 sitúa la cadera
   prácticamente en extensión, mantiene tobillos plantados en movimientos con
   apoyo y conserva el alcance al iniciar/finalizar el ciclo. En zancada,
   debido a los apoyos separados, no se exige la misma verticalidad que
   al estar con los pies juntos.
3. **Codos medio doblados:** los objetivos antiguos de muñeca estaban más cerca
   del hombro que la longitud total del brazo. Ahora cada muñeca recorre un
   arco de radio casi igual a antebrazo + brazo superior, definido por ángulo
   y no por interpolar un cordón corto. Elevación de brazos alcanza la
   orientación por encima del hombro; para evitar recortes el renderer
   reduce el cuerpo sólo en ese ejercicio y mantiene el suelo fijo.
4. **Rostro frontal en personaje lateral:** sustituye dos ojos y un óvalo
   simétrico por una sola vista de ojo, ceja, oreja, nariz y mentón. El
   cabello se curva con la línea lateral. Rostro y ropa no pretenden ser
   biométricos.
5. **Cuerpo de cápsulas y óvalos:** el torso lateral usa contornos de espalda,
   pecho, abdomen, cintura y pelvis con asimetría moderada. La textura de la
   camiseta se ajusta a la nueva forma. No introduce texturas pesadas.

## Referencias verificadas
- OpenStax, cintura escapular y hombro:
  https://openstax.org/books/anatomy-and-physiology-2e/pages/8-1-the-pectoral-girdle
- OpenStax, estructura de huesos de brazo y articulación del codo:
  https://openstax.org/books/anatomy-and-physiology/pages/8-2-bones-of-the-upper-limb
- OpenStax, articulaciones de hombro, codo y tobillo:
  https://openstax.org/books/anatomy-and-physiology-2e/pages/9-6-anatomy-of-selected-synovial-joints
- OpenStax, músculos estabilizadores del hombro:
  https://openstax.org/books/anatomy-and-physiology/pages/11-5-muscles-of-the-pectoral-girdle-and-upper-limbs
- Wikimedia Commons, modelos artísticos, licencia según cada ficha:
  https://commons.wikimedia.org/wiki/Human_body_diagrams
- Android Custom Drawing, curvas y Paint:
  https://developer.android.com/develop/ui/views/layout/custom-views/custom-drawing

Se consultaron las fuentes para comprender las regiones anatómicas, sin copiar
ilustraciones externas. Las dimensiones del avatar son unidades virtuales
de pantalla, no proporciones obligatorias de un cuerpo humano.

## Validación exigida
- JUnit de codos/rodillas extendidos en reposo, ciclo cerrado, brazo por
  encima del hombro en el pico de elevación y longitudes invariantes.
- JUnit previo de IK, FABRIK y ausencia de saltos en 301 fases.
- Contratos de diseño, Android Lint, debug build y AndroidTest.
- HTML comparativo V4/V5 e inspección visual real.

## Alcance
Cinco ejercicios ilustrativos para revisar la infraestructura; no representa
una medición biomecánica ni una instrucción para ejecutar todos los
ejercicios del catálogo. La cámara lateral puede ocultar acciones en
profundidad. No fusionar sin aprobación.
