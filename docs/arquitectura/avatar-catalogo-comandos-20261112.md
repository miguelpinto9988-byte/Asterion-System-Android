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

La CI recoge **18 PNG reales** para tres movimientos laterales nuevos y **24 PNG reales adicionales** para cuatro escenas frontales: dos paletas × inicio/mitad/final.
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
  además de las 42 capturas corporales de fases anteriores.
- GitHub Actions ejecuta validaciones de Android Java, Lint,
  APK y emulador Android 16. Consultar el último resultado
  antes de fusionar.

**Alcance:** la tarea de finalizar los 1.244 renders corporales no está
terminada. Los mapas articulares son una herramienta adicional para
inspeccionar comandos sin inducir a error al mostrar un ejercicio ajeno.
Para completar el catálogo corporal falta desarrollar y revisar poses
de suelo, sedestación, rotación, equipamiento, deportes e intensidad
variable con contactos y trayectorias propios.

### Muestreo adicional de extensión de cadera

Se exportan ahora **18 capturas laterales** del emulador: los dos clips
`ARM_CURL` y `KNEE_CURL` previamente integrados, más
`HIP_EXTENSION_STAND`; cada uno con dos colores y tres fases.
Sumadas a las 24 capturas frontales y las 24 capturas de mapas
articulares, la batería de la extensión actual recopila **66 PNG**.

### Avatares esqueléticos animados para todos los comandos

`AvatarCommandFigurePose` transforma los 51 programas articulares
existentes en un **esqueleto esquemático móvil**: cabeza y cuello,
ambos hombros, codos, manos, pelvis, caderas, rodillas, tobillos y
puntas de pie. Las rotaciones se calculan respecto a articulaciones
padre; longitudes de brazos, antebrazos, muslos, piernas y cuello
permanecen constantes en todo momento. La capa de visualización
`AvatarJointCommandView` mantiene los arcos que identifican cada
eje activo, pero ahora dibuja también los segmentos articulados
en movimiento durante el ciclo, con dos paletas y 24 ejes.

**Ámbito de validez:** el esqueleto de referencia NO contiene suelo,
contactos comprobados, paredes, silla, aparatos ni reconstrucción 3D.
Los desplazamientos son proyecciones ilustrativas 2D. No reemplaza
las demostraciones corporales de cada variante. Una técnica con
equipamiento o suelo sigue requiriendo un rig y una escena específicos.

`AvatarCommandFigurePoseTest` recorre los 51 comandos durante
361 fases cada uno, comprueba integridad y longitud de enlaces,
ausencia de saltos de cabeza/rodillas/muñecas y coincidencia exacta
de la pose del comienzo con la del cierre. También verifica
que flexión de rodilla y elevación de hombro afectan regiones distintas.

## Fase 5 — correspondencia por ejercicio, apoyos y equipo

**Registro actual: 15 variantes con demostración corporal ilustrativa
y 1.242 pendientes de demostración individual.** Las 1.257 variantes
conservan visualización animada de comandos y los 51 comandos previos.

La ampliación implementa tres escenas físicas diferenciadas del avatar
de pie:

| ID | Pose representada | Equipo presente | Trayectoria y contactos |
|---|---|---|---|
| `puente_gluteo_v01` | Decúbito supino; puente corto | Colchoneta | Hombros/cabeza sobre colchoneta, ambos pies fijos y pelvis de elevación corta, retorno |
| `puente_gluteo_v02` | Decúbito supino; puente básico | Colchoneta | Mismos apoyos, elevación mayor de pelvis, retorno |
| `extension_rodilla_sin_carga_v01` | Sedente en silla | Silla | Pelvis y rodilla fija; una pierna extiende mientras la otra conserva apoyo y ambas retornan a inicio |

Además, `sentadilla_bilateral_v04` utiliza el ciclo específico de
sentadilla con fase sostenida en el punto central; sin implementos.

**Correcciones contra falsas equivalencias:**

- `curl_biceps_v01` usa banda, que el clip anterior no mostraba; dejó
  de figurar como demostración corporal hasta disponer de banda anclada
  y movimiento representativo.
- `marcha_v02` es caminata sobre terreno plano y antes reutilizaba una
  marcha en el sitio. Se retiró la demostración corporal: caminar no es
  elevar la rodilla sin desplazamiento.

El control `AvatarDemonstrationManifest` limita las 15 ilustraciones
a IDs, cámara, equipamiento y ejecución expresamente identificados.
Cualquier asignación sin constancia, cambio de material o cambio de
posición se rechaza en la lectura del catálogo. No se habilita en bloque
ninguna de las otras 1.242 variantes.

### Pruebas de la fase 5

- `AvatarDedicatedSceneTest` comprueba en 301 puntos por ciclo que
  fémur, tibia, brazos y torso no cambian longitud; asimismo prueba la
  fijación de la silla, la rodilla de referencia, los hombros y pies en
  colchoneta, amplitudes diferentes y cierre exacto del bucle.
- `AvatarDedicatedSceneInstrumentedTest` genera **18 PNG nativos**
  (3 escenas × 3 fases × 2 paletas) del render de Android 16.
- El script `ci/android_daily_instrumented_smoke.sh` ejecuta esa
  instrumentación y exige exactamente las 18 imágenes en artefactos de
  diagnóstico, además del muestreo anterior.
- La prueba contractual de Python exige 1.257 registros, 15
  ilustraciones, correspondencia de implementos y la retirada de las
  dos sustituciones engañosas.

El cierre de compilación no equivale a verificación profesional de
biomecánica. Ni estas ilustraciones son capturas de movimiento 3D
ni garantizan un gesto clínicamente exacto.

### Pendientes de demostración corporal verificada

| Categoría | Ejercicios que aún requieren escena propia |
|---|---:|
| Tren inferior | 283 |
| Tren superior | 279 |
| Movilidad | 241 |
| Tren central | 230 |
| Cardio | 209 |
| **Total** | **1.242** |

Por equipamiento, 143 requieren silla, 131 colchoneta, 65 banda,
62 pared y 370 no requieren aparatos; el resto usa implementos y
situaciones heterogéneas (máquinas, piscina, deportes, etc.).
Para considerar terminado el catálogo, cada variante debe disponer
de su postura adecuada, contactos, implementos, articulaciones,
trayectoria desde inicio hasta fin y revisión visual específica;
los mapas esqueléticos actuales no son sustitutos certificados.

## Fase 6 — dorsiflexión bilateral sentada y alcance frontal sentado

Se amplía el manifiesto de ilustraciones a **17 variantes**. Las **1.240**
restantes conservan el esquema articular móvil; no se consideran
demostraciones exactas. Se conservan los **51 comandos originales** y
se añaden dos especializados, para un total de **53**.

### Especificación mecánica y temporal de nuevas escenas

Las dos escenas comparten el ciclo `depthAtPhase(t)`: 0–0,08
preparación inmóvil; 0,08–0,42 recorrido suave C¹; 0,42–0,55
mantenimiento; 0,55–0,92 retorno suave; 0,92–1 fin neutro.
Los límites de fase son valores de animación, no recomendaciones de
tiempo, duración ni intensidad para usuarios.

| Articulación / elemento | `dorsiflexion_tibial_v01` (silla) | `alcances_tronco_sentado_v02` (silla) |
|---|---|---|
| Pelvis / asiento | Centro fijo en la silla | Centro fijo en la silla |
| Tronco | Longitud y orientación neutras | Rota hacia delante sobre caderas, longitud pelvis-hombro constante; retorno inverso |
| Cabeza / cuello | Orientación neutra | Centro de cabeza acompaña el tronco conservando distancia al cuello |
| Escápulas / hombros | Posición inicial inalterada | Ambos hombros acompañan al tronco |
| Codos / muñecas | Estables | Extensión ilustrativa bilateral hacia el frente, dos segmentos de longitud fija |
| Caderas / muslos | Sentados; muslos y rodillas estáticos | Sentados; muslos y rodillas estáticos |
| Rodillas / tibias | Permanecen en su lugar | Permanecen en su lugar |
| Tobillos / zapatos | Zapatos giran hasta 22° alrededor del talón como pivote visual; **talones fijos**, elevación de puntas y retorno | Sin rotación, pies permanecen apoyados |
| Implementos | Silla dibujada detrás del avatar, sin banda ni mancuerna | Silla dibujada detrás del avatar, sin implementos |
| Inicio / final | Coinciden: puntas apoyadas, tronco neutral | Coinciden: tronco erguido y brazos de reposo |

`AvatarDedicatedScene` modela las poses con segmentos rígidos y
contactos declarados; `AvatarDedicatedSceneRenderer` dibuja silla,
zapatos y ropa. `AvatarDemonstrationManifest` comprueba
`variant_id`, cámara, ejecución y equipamiento. La prueba
`AvatarDedicatedSceneTest` recorre 301 fases por escena, y Android
exporta **30 PNG** para las cinco escenas de silla/colchoneta, tres
fases y dos paletas.

**Límites:** los 22° se aplican como grados de dibujo en Canvas; no
son rango fisiológico medido. La inclinación del tronco y las
extensiones de brazos representan cinemática 2D educativa. No existe
captura de movimiento, geometría 3D personalizada ni evaluación
clínica individual para estas escenas.

**Referencias técnicas de comparación:**
- Android Developers, optimización de vistas personalizadas:
  https://developer.android.com/develop/ui/views/layout/custom-views/optimizing-view
- NHS, ejercicios en silla:
  https://www.nhs.uk/live-well/exercise/sitting-exercises/
- University Hospitals Sussex NHS, guía de elevación de puntas en
  posición sentada:
  https://www.uhsussex.nhs.uk/wp-content/uploads/2026/09/2759-Chair-exercises-with-seated-arm-exercises-2026.pdf

### Estado pendiente por categoría

| Categoría | Variantes sin ilustración corporal |
|---|---:|
| Tren inferior | 282 |
| Tren superior | 279 |
| Movilidad | 241 |
| Tren central | 229 |
| Cardio | 209 |
| **Total** | **1.240** |

Las tareas de implementación y revisión continúan documentadas en el
issue #37. La PR #36 no debe cerrarse ni fusionarse como «catálogo
completo» mientras esas demostraciones sigan sin validación visual.

### Comandos específicos añadidos tras cotejar los fotogramas

- `SEATED_TOE_RAISE`: tobillo izquierdo = tobillo derecho = **22°**
  en la fase central; caderas, rodillas, columna, hombros y codos sin
  desvíos respecto a la postura sentada. El render gira el zapato
  alrededor del talón y retorna a 0° en la fase final.
- `SEATED_FORWARD_REACH`: `SPINE_BEND = 21°` y
  `LEFT_SHOULDER = RIGHT_SHOULDER = 64°` en el centro; los brazos
  mantienen longitud y las caderas permanecen fijas. El render
  interpola ~0,36 rad de tronco y ~1,12 rad de movimiento relativo
  de brazos: los objetivos son valores esquemáticos redondeados.
  No representa una amplitud clínica medida.

`AvatarJointTimelineTest` comprueba el estado de ambos comandos a
inicio, centro y fin. `tests/avatar_catalog_motion_contract.py`
exige **53 programas**, mantiene los 1.257 IDs e impide usar un
comando distinto al específico para estas dos demostraciones.

## Fase 7 — secuencias de rodilla alternadas y pausas comprobables

**Avance implementado:** 20 de 1.257 variantes (**1,59 %**) tienen
escenas corporales ilustrativas declaradas en
`AvatarDemonstrationManifest`. Quedan **1.237** sin escena corporal
específica. Los 1.257 siguen disponiendo de sus fichas y mapa de
comandos, que **no cuenta como demostración corporal**.

### Tres variantes nuevas

| ID exacto | Posición y apoyos | Inicio | Movimiento / mantenimiento | Retorno y final |
|---|---|---|---|---|
| `puente_gluteo_v03` | Supino sobre colchoneta; cabeza, hombros y ambas plantas mantienen su apoyo | Pelvis apoyada | Pelvis sube 42 unidades de dibujo con torso y huesos rígidos; se mantiene entre 27 y 73 % del ciclo | Desciende gradualmente y vuelve al apoyo inicial |
| `extension_rodilla_sin_carga_v02` | Silla estable; pelvis y ambas rodillas fijas; pie contrario como apoyo | Dos piernas flexionadas | Pierna próxima se extiende en el primer medio ciclo y vuelve a neutral; la otra se extiende en el segundo medio ciclo, **sin elevar ambas juntas** | Las dos piernas y pies vuelven a neutral en la fase final |
| `extension_rodilla_sin_carga_v03` | Silla, pelvis y rodilla fijas; pierna opuesta conserva apoyo | Rodilla flexionada | Una rodilla se extiende según el ángulo ilustrativo de ~65°, con fase de mantenimiento del 27–73 % del ciclo | Tibia retorna por la misma trayectoria; termina sentado |

### Corrección de fidelidad articular

Se introduce `SEATED_KNEE_EXTENSION_SINGLE` en lugar de usar
`KNEE_EXTENSION` genérico en
`extension_rodilla_sin_carga_v01`: solo una rodilla se extiende;
la otra conserva apoyo.

Los 53 comandos anteriores permanecen. Se suman cuatro programas:
`GLUTE_BRIDGE_PAUSE`,
`SEATED_KNEE_EXTENSION_SINGLE`,
`SEATED_KNEE_EXTENSION_PAUSE` y
`SEATED_KNEE_EXTENSION_ALTERNATING`.
**Total: 57 comandos.**

`AvatarJointTimeline.heldWeight(t)` crea una trayectoria
suave C² con mantenimiento entre el 27 y el 73 % del ciclo.
La alternancia utiliza dos semiciclos continuos: codo y hombro
sin movimientos añadidos, cadera y rodilla de asiento estáticas,
tibia móvil con longitud constante, pie contrario estable y
neutralidad en el 50 % y el 100 %. Todos los valores se usan para
el render 2D; **no son medidas clínicas ni instrucciones de ejercicio**.

### Pruebas y porcentajes

- `AvatarDedicatedSceneTest` recorre 301 fases de **ocho
  escenas dedicadas** y valida longitudes constantes, contactos,
  pausas prolongadas, secuencia alternada y cierre de bucle.
- `AvatarJointTimelineTest` valida 57 comandos, dos picos
  de alternancia en los cuartos del ciclo y posición neutra en
  el medio y al cerrar; también los mantenimientos.
- La prueba instrumental Android 16 exporta **50 PNG** de las
  ocho escenas dedicadas: 7 escenas × 3 fases × 2 paletas, más
  1 escena alternada × 4 fases × 2 paletas.
- `tests/avatar_completion_progress.py` calcula el porcentaje
  desde `avatar_motion_commands.tsv`, coteja los IDs
  `ILLUSTRATIVE` con `AvatarDemonstrationManifest` y
  ofrece formato JSON para informes automáticos.
- El manifiesto incluye además el comando exacto por ID, cámara,
  ejecución y equipamiento para impedir sustituciones visuales
  engañosas.

Los porcentajes representan **cobertura declarada e implementada**
en la rama, no certificación de corrección biomecánica individual.
La compilación Android, el emulador y la inspección de las nuevas
capturas deben aprobarse antes de promover la fase a validada.

### Pendientes por categoría tras esta ampliación

| Categoría | Demostraciones corporales pendientes |
|---|---:|
| Tren inferior | 279 |
| Tren superior | 279 |
| Tren central | 229 |
| Movilidad | 241 |
| Cardio | 209 |
| **Total** | **1.237** |

Seguimiento: [issue #37](https://github.com/miguelpinto9988-byte/Asterion-System-Android/issues/37).

## Ampliación de demostraciones con equipo y apoyos — fase 8 (12-11-2026)

**Avance registrado:** 24/1.257 = **1,91 %** de variantes con ilustración corporal;
**1.233** variantes conservan solamente el mapa articular técnico. La
implementación no equivale a validación profesional del gesto. Los **60
comandos** se conservan y el `AvatarDemonstrationManifest` exige que ID,
comando, clip, cámara, ejecución y equipamiento coincidan exactamente.

| ID | Representación de cuerpo/equipo | Contactos y diferencias |
| --- | --- | --- |
| `puente_gluteo_v04` | Puente acostado y colchoneta con **banda visible por encima de las rodillas** | Hombros y cabeza sobre colchoneta, ambos pies fijos, elevación y retorno de pelvis; la banda no tiene anclaje inventado |
| `elevacion_gemelos_v01` | Elevación bilateral de talones sentado en silla | Ambos antepiés se mantienen anclados; los tobillos siguen arcos circulares, el solucionador IK conserva las longitudes de fémur y tibia |
| `balanceo_talon_punta_sentado_v01` | Elevación suave de talones sentado | Igual posición y contactos que la elevación bilateral, pero un recorrido menor de referencia (12° frente a 22°) |
| `dorsiflexion_tobillo_v01` | Dorsiflexión unilateral de un pie sentado | Un pie gira alrededor del talón, el otro queda neutral y ambos miembros permanecen sentados |

Los 22° y 12° son **parámetros ilustrativos de Canvas**, no mediciones
clínicas. En las elevaciones de talones, la suela se dibuja respecto a un
pivote de antepié constante y el tobillo se desplaza solidariamente; la
rodilla puede reajustarse por IK para evitar segmentos elásticos. En la
dorsiflexión unilateral, solo cambia el pie cercano al observador.

**Validaciones añadidas:** prueba JVM de longitudes, pivotes, ausencia de
deriva de los antepiés y cierre del ciclo para todas las variantes de
`AvatarDedicatedScene`; contrato Python de 24 ilustraciones;
`AvatarExerciseCommandCatalogInstrumentedTest` compara 24 IDs;
`AvatarDedicatedSceneInstrumentedTest` recoge **74 PNG** nativos de 12
escenas con 2 paletas, inicio, fase central y final (4 fases para la
extensión alternada). El pipeline de Android 16 exige las 74 capturas
para completar su prueba instrumental.

**Restantes sin demostración propia por categoría:** tren inferior 276;
tren superior 279; tren central 229; movilidad 240; cardio 209.
Total: **1.233**. El issue #37 conserva los criterios de aceptación
individuales. No declarar 100 %, ni fusionar la PR como catálogo
completo, hasta que haya una escena específica revisada para cada ID.

## Lote adicional: balanceo de talón y punta sentado — versión del 12-11-2026

Se ha añadido una vigesimoquinta demostración corporal ilustrativa,
`balanceo_talon_punta_sentado_v02`, con silla real en el lienzo,
comando específico `SEATED_HEEL_TOE_ALTERNATION`, y sin reutilizar
una elevación unilateral ni una escena de pie.

**Dos mitades diferentes de la animación:**

- Fases 0,00 a 0,50: el antepié es el apoyo fijo y ambos talones
  ascienden mediante rotación sobre los puntos de punta. El tobillo
  sigue una trayectoria circular y el solucionador IK mantiene las
  longitudes fémur-tibia.
- Fases 0,50 a 1,00: los talones permanecen anclados y las puntas
  ascienden mediante rotación del zapato; rodillas, tibias y caderas
  no se trasladan. Cada mitad tiene preparación, fase activa,
  mantenimiento, retorno y posición neutra.
- Centro del ciclo 0,50: ambas plantas retornan a la neutralidad
  antes de cambiar el punto de apoyo; cierre 1,00 = apertura 0,00.

La ficha, el render y el `AvatarDemonstrationManifest` exigen el
mismo ID, cámara `SIDE`, comando, equipamiento `silla` y ejecución
`controlado`. Las pruebas instrumentales suman 8 fotogramas Android
específicos de esta escena (dos paletas × cuatro fases), para un total
de **82 capturas de escenas de colchoneta y silla**. Las pruebas JVM
verifican los apoyos, no deformación de segmentos, ambos máximos,
pivotes opuestos, retorno y continuidad de 301 fases.

**Cobertura del catálogo en esta revisión: 25 / 1.257 = 1,99 %**,
con **1.232 variantes** todavía sin demostración corporal específica.
El mapa esquemático articular de esas 1.232 variantes continúa
disponible, pero no cuenta como demostración exacta. El número
refleja escenas registradas, sujetas a CI, y NO una evaluación
biomecánica clínica ni una certificación de ejecución.

Referencia pública para diferenciar elevación de talones y puntas:
University Hospitals Sussex NHS Foundation Trust,
https://www.uhsussex.nhs.uk/resources/chair-exercises-with-seated-arm-exercises/
