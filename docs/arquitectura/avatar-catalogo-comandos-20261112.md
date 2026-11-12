# Asterion — comandos articulares y catálogo completo de avatares (primera integración)

Fecha: 12 de noviembre de 2026. Rama: `feature/avatar-comandos-catalogo-integral-20261112`.
No presupone un efecto sobre los objetivos de entrenamiento. El avatar es
ilustrativo y no evalúa técnica humana ni tolerancia del usuario.

## Estado verificable

- **1.257 variantes** vinculadas por ID a un comando y cámara sugerida.
- **155 familias** y **474 tipos**; clases y etiquetas desde el catálogo real,
  no desde coincidencias por nombre durante la ejecución.
- **51 programas de comando** definidos en
  `AvatarJointTimeline.Command`, con cobertura semántica para las cinco
  categorías y **24 ejes articulares de referencia 2D**.
- **Trece variantes con ilustración corporal esquemática** activada y validable.
  Las otras **1.244** disponen de especificación de comandos, pero tienen
  estado `SPEC_ONLY`: **no hay animación específica terminada**. En lugar
  de presentar una sentadilla como sustituto, el visor oculta el render
  y muestra el comando, el equipo, el nivel y las fases articulares.
- Se conservan los seis clips laterales históricos, las nueve cámaras
  adicionales, ambas paletas, los controles y las pruebas anteriores.
- Se incorporan dos clips nuevos: `ARM_CURL` (flexión de codo en perfil)
  y `KNEE_CURL` (flexión de rodilla de pie con un pie libre y otro fijo).

## Archivo de integración

`app/src/main/assets/asterion_exercises/v1/avatar_motion_commands.tsv`:

| Campo | Propósito |
|---|---|
| `variant_id` | Relación exacta 1:1 con `variants.tsv` |
| `command` | Programa de articulaciones específico de familia/patrón |
| `camera` | Proyección preferente, sin simular 3D |
| `clip` | Clip real disponible o `NONE` |
| `status` | `ILLUSTRATIVE` o `SPEC_ONLY` |
| `category` | Categoría real de `families.tsv` |
| `level` | Complejidad técnica 1–5 |
| `execution` | Modalidad de la variante |
| `equipment` | Implemento real de la variante |
| `supervision` | Metadato de supervisión recomendado |

`AvatarExerciseCommandCatalog.load` rechaza IDs faltantes, registros duplicados,
cámaras o comandos no definidos, datos técnicos que no coincidan con el catálogo,
clips con estado incompatible y animaciones activadas para técnicas avanzadas.
La carga se hace fuera del hilo principal mediante
`AssetAvatarExerciseCommandRepository`.

## Especificación geométrica y temporal

La referencia técnica representa los movimientos en el plano 2D con 24
ejes: inclinación y giro del cuello; flexión y abducción de ambos hombros;
flexión de ambos codos; ambas muñecas; dos escápulas; inclinación y giro
del tronco; pelvis; flexión y abducción de ambas caderas; flexión de ambas
rodillas y ambos tobillos.

Cada comando asigna una desviación explícita de la **pose ilustrativa neutral**.
Un eje no señalado mantiene la desviación cero. Los valores son offsets
de dibujo, **no grados clínicos de articulación ni consejos de amplitud**.

| Fase normalizada | Propósito | Condición |
|---|---|---|
| 0,00–0,08 | Preparación y apoyo | Todos los ejes en su pose neutral |
| 0,08–0,42 | Movimiento principal | Interpolación C² suave hasta la pose objetivo |
| 0,42–0,58 | Fase de control | Pose ilustrativa central estable |
| 0,58–0,94 | Retorno | Interpolación C² hasta neutral |
| 0,94–1,00 | Finalización | Retorno a pose neutral y cierre del bucle |

Se utiliza la curva `6t^5 - 15t^4 + 10t^3`: velocidad y aceleración
continuas en las uniones. `AvatarJointTimeline.offset(joint,phase)` permite
inspeccionar **cada articulación en preparación, fase central, retorno y final**.
La prueba `AvatarJointTimelineTest` recorre las 51 instrucciones, verifica
todos los ejes, cierre de bucle, continuidad y ausencia de NaN.

**Distinción importante:** esta trayectoria es una **especificación
de comandos**. No debe confundirse con una animación orgánica individual
ya implementada: únicamente los clips declarados `ILLUSTRATIVE` se
conectan al render 2D. El render existente utiliza su propio sistema de
cinemática inversa, longitudes de segmentos, contacto, orientación de
calzado y piel Bézier. Antes de habilitar más clips habrá que enlazar las
trayectorias nuevas al render por plano, diseñar contacto/superficies y
revisar las posiciones del cuerpo durante todo el ciclo.

## Catálogo de comandos por región

- **Tren inferior:** sentadilla, zancada, bisagra de cadera, extensión de
  cadera, escalón, tobillo, flexión/extensión de rodilla, abducción de
  cadera, control del pie, isometría, salto, transición de silla y
  desplazamientos de pies.
- **Tren superior:** empuje, tracción, flexión/extensión de codo,
  elevación/abducción/rotación de hombro, control escapular, muñeca,
  mano y gestos coordinados con implementos.
- **Tren central:** flexión/extensión/rotación de tronco, control central
  sostenido, cuadrupedia, alcances, respiración y apoyo lateral.
- **Movilidad:** equilibrio, coordinación, movilidad de cadera/columna/
  hombro/tobillo/cuello, flexibilidad y control respiratorio.
- **Cardio:** marcha, carrera, ciclismo, remo, elíptica, escalones,
  danza, natación y gestos recreativos deportivos.

Todos los movimientos históricos del visor siguen visibles en **Rutinas
→ Ver avatares 2D animados**. Desde un paso de rutina se abre
`NativeAvatarActivity` con el extra exacto `variant_id`. Su modo guiado
bloquea los selectores de cámara/movimiento para impedir una demostración
incompatible. Si el clip está pendiente, se informa y no se muestra una
animación de otro ejercicio. El visor es completamente local y no
requiere red.

## Muestreo de ejercicios que antes no tenían ficha integrada

| Ejercicio del catálogo | Comando | Estado visual actual |
|---|---|---|
| Curl de bíceps con banda ligera (`curl_biceps_v01`) | Flexión de codo | Ilustración de articulación, **banda no dibujada** |
| Flexión de rodilla de pie (`flexion_rodilla_bipeda_v03`) | Flexión de rodilla | Nueva ilustración de pie libre y pie plantado |
| Puente de glúteos (`puente_gluteo_v02`) | Extensión de cadera | Especificación, render de suelo pendiente |
| Giro cervical suave (`rotacion_cervical_suave_v02`) | Movilidad cervical | Especificación, giro 3D no representable en perfil sin revisión |
| Equilibrio unipodal (`equilibrio_unipodal_v04`) | Equilibrio | Animación frontal ilustrativa nueva con un pie de apoyo fijo |
| Alcance unipodal (`alcance_unipodal_v01`) | Equilibrio | Especificación; requiere modelar el apoyo externo |

La CI recoge **12 PNG reales** para los dos movimientos laterales nuevos y **24 PNG reales adicionales** para cuatro escenas frontales: dos paletas × inicio/mitad/final.
Las cuatro escenas son elevación lateral de brazos, equilibrio unilateral, paso lateral sin banda e inclinación lateral cervical.
El archivo `build/avatar-command-samples/muestras_catalogo.json` contiene
el muestreo de los nueve IDs con sus estados verdaderos.

## Pruebas y protección de calidad

- `tests/avatar_catalog_motion_contract.py`: cobertura exacta de 1.257
  IDs, 155 familias, 51 comandos, cinco categorías, tipado, supervisión,
  estado visual y mecanismo de fallback sin escenas incorrectas.
- `AvatarJointTimelineTest`: 49 × 24 ejes, fases y continuidad.
- `AvatarMotionEngineTest`: ocho clips laterales, preservación de
  longitud, apoyos y comienzo/fin sin salto.
- `AvatarExerciseCommandCatalogInstrumentedTest`: carga real desde
  assets Android, recuentos, acceso seguro y exportación de 12 fotogramas.
- Se mantienen `NativeAvatarRenderTest` y los contratos V2–V13.
- GitHub Actions: pruebas JVM, Lint, APK y prueba real del emulador Android 16.

## Trabajo restante antes de afirmar fidelidad de los 1.257 ejercicios

Los 1.244 perfiles `SPEC_ONLY` aún necesitan ilustraciones terminadas.
Para cada uno deberá verificarse el tipo de apoyo, material representado,
pose inicial/final, planos correctos (especialmente suelo y gestos de
rotación), proximidad y superposición visual de articulaciones, y
continuidad geométrica en todas las fases. Algunas variantes del mismo
tipo podrán reutilizar una animación y otras requerirán escenas propias.
No se debe activar una ilustración solo para elevar artificialmente
el porcentaje de cobertura visual.

## Ampliación visual V2 — 12 de noviembre de 2026

La cámara frontal incorpora cuatro proyecciones nuevas, sin alterar los seis
movimientos históricos ni los dos nuevos movimientos laterales:

| ID activado | Cámara | Movimiento representado | Contactos |
|---|---|---|---|
| `elevacion_lateral_v01` | `FRONT_LATERAL` | Elevación lateral de ambos brazos hasta altura de hombros; sustituye una elevación incorrectamente completa sobre la cabeza | Ambas plantas fijas |
| `equilibrio_unipodal_v04` | `FRONT_BALANCE` | Elevación controlada de un pie, posición estable, retorno | Pie derecho fijo; izquierdo libre solo en fase activa |
| `caminata_banda_v01` | `FRONT_SIDE_STEP` | Paso lateral **sin banda**, retorno del pie desplazado | Pie izquierdo fijo; derecho móvil durante desplazamiento |
| `inclinacion_cervical_suave_v01` | `FRONT_NECK_TILT` | Inclinación lateral ilustrativa con referencia cuello-cabeza de longitud fija; **no** representa rotación cervical | Ambas plantas fijas |

Estas escenas son proyecciones 2D ilustrativas sin medición clínica. El movimiento
cervical requiere una revisión visual específica de la unión cuello-cabeza; los
ejercicios de rotación cervical, puentes de glúteos, alcance unipodal y posturas
sentadas siguen como `SPEC_ONLY` hasta contar con postura y apoyos fieles.

La cobertura ilustrativa resulta en **13 variantes del catálogo** y **1.246
especificaciones sin animación validada**. El contador de comandos permanece en
51 y el catálogo en 1.257. Se conservan los controles de cámara y paleta.

Pruebas añadidas: `AvatarNewFrontalMotionsTest` recorre 361 puntos por escena,
verifica brazos/piernas de longitud constante, cabeza/cuello, cierre sin saltos,
contactos y posiciones. `AvatarV13PoseGuardTest` valida también los cuatro
nuevos modos. La prueba instrumental exporta **24 PNG** (4 escenas × 3 fases ×
2 avatares) en `build/runtime-diagnostics/asterion-avatar-new-front-samples/`,
publicados como artefacto de ejecución de GitHub Actions.

## Dos comandos articulares adicionales para las nuevas escenas

Se preservan los **49 comandos anteriores** y se introducen dos nuevos:

- `LATERAL_STEP`: desplazamiento unilateral del pie derecho; cadera,
  rodilla y tobillo derechos cambian durante la trayectoria. La pierna
  izquierda y su apoyo permanecen neutros. Se asigna a
  `caminata_banda_v01`, que no requiere equipo.
- `NECK_SIDE_TILT`: inclinación lateral del cuello, eje
  `NECK_SIDE`; no marca flexión sagital ni rotación axial. Se asigna
  a las tres variantes de `inclinacion_cervical_suave`, aunque solo la
  variante de pie sin equipo está habilitada para render.

Con estas extensiones hay **51 comandos** y **24 ejes ilustrativos**.
La interpolación C², el cierre de bucle y las limitaciones clínicas
siguen inalterados.

## Fase 3 — esquema articular animado universal y dos escenas adicionales

### Cobertura honesta de la visualización

Desde el visor Android, las **1.257 variantes** ahora tienen una visualización
temporal asociada al comando: **13** muestran ilustración corporal en Canvas y
**1.244** muestran un **mapa de comandos articulares animado**. El mapa NO
representa la ejecución completa del ejercicio, ni sustituye la posición
corporal, los apoyos o implementos de una ilustración revisada.

El mapa es propio de cada `variant_id` y hereda su
`AvatarJointTimeline`: hasta 24 ejes sobre una silueta anatómica fija,
agujas y arcos que indican referencias de movimiento (no ángulos clínicos),
interpolación C², inicio y final neutros, y control de fase. Los usuarios
pueden pausar, avanzar, reiniciar, cambiar la velocidad y seleccionar
avatar azul/morado, incluso si una variante todavía no tiene clip corporal.
El texto de la pantalla recalca que es un **diagrama**, no una demostración.

El visor utiliza `ScrollView` y reserva un área Canvas de **448 dp**
de alto, evitando que la animación se comprima a cero al mostrar fichas,
controles y fases articulares en pantallas pequeñas.

### Nuevas ilustraciones corporales

- `extension_cadera_bipeda_v01`: perfil `HIP_EXTENSION_STAND`, pie de
  apoyo fijo y pie móvil hacia atrás y arriba. Cinemática analítica de
  dos segmentos y continuidad de ciclo; sin aparatos.
- `paso_lateral_y_retorno_v01`: reutiliza el clip frontal
  `FRONT_SIDE_STEP` porque la variante es de paso lateral sin equipo,
  con el mismo pie fijo, pie móvil y retorno. No se reutiliza para
  caminatas alternas ni variantes con banda.

El clip lateral nuevo es el noveno movimiento del motor
`AvatarMotionEngine`, preservando los ocho anteriores y las diez
proyecciones de cámara existentes.

### Pruebas y evidencia

- `AvatarMotionEngineTest` valida las nueve escenas laterales y que
  `HIP_EXTENSION_STAND` libera solo un pie y termina en neutral.
- `tests/avatar_catalog_motion_contract.py` exige exactamente 1.257
  fichas, 13 clips reales y un mapa de comandos registrado para
  todas las `SPEC_ONLY`.
- `AvatarExerciseCommandCatalogInstrumentedTest` comprueba la
  clasificación 13/1.244 y exporta 24 PNG de mapas técnicos reales
  para puente de glúteos, giro cervical, alcance unipodal y natación,
  además de las 36 capturas corporales de fases anteriores.
- GitHub Actions ejecuta validaciones de Android Java, Lint,
  APK y emulador Android 16. Consultar el último resultado
  antes de fusionar.

**Alcance:** la tarea de finalizar los 1.244 renders corporales no está
terminada. Los mapas articulares son una herramienta adicional para
inspeccionar comandos sin inducir a error al mostrar un ejercicio ajeno.
Para completar el catálogo corporal falta desarrollar y revisar poses
de suelo, sedestación, rotación, equipamiento, deportes e intensidad
variable con contactos y trayectorias propios.
