# Asterion — comandos articulares y catálogo completo de avatares (primera integración)

Fecha: 12 de noviembre de 2026. Rama: `feature/avatar-comandos-catalogo-integral-20261112`.
No presupone un efecto sobre los objetivos de entrenamiento. El avatar es
ilustrativo y no evalúa técnica humana ni tolerancia del usuario.

## Estado verificable

- **1.257 variantes** vinculadas por ID a un comando y cámara sugerida.
- **155 familias** y **474 tipos**; clases y etiquetas desde el catálogo real,
  no desde coincidencias por nombre durante la ejecución.
- **49 programas de comando** definidos en
  `AvatarJointTimeline.Command`, con cobertura semántica para las cinco
  categorías y **23 ejes articulares de referencia 2D**.
- **Ocho variantes con ilustración esquemática** activada y validable.
  Las otras **1.249** disponen de especificación de comandos, pero tienen
  estado `SPEC_ONLY`: **no hay animación específica terminada**. En lugar
  de presentar una sentadilla como sustituto, el visor oculta el render
  y muestra el comando, el equipo, el nivel y las fases articulares.
- Se conservan los seis clips laterales históricos, las cinco cámaras
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

La referencia técnica representa los movimientos en el plano 2D con 23
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
La prueba `AvatarJointTimelineTest` recorre las 49 instrucciones, verifica
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
| Equilibrio unipodal (`equilibrio_unipodal_v04`) | Equilibrio | Especificación, contacto unilateral pendiente de render específico |
| Alcance unipodal (`alcance_unipodal_v01`) | Equilibrio | Especificación; requiere modelar el apoyo externo |

La CI recoge **12 imágenes PNG reales del emulador Android** para
los dos clips nuevos: azul/morado × inicio/mitad/final.
El archivo `build/avatar-command-samples/muestras_catalogo.json` contiene
el muestreo de los seis IDs con sus estados verdaderos.

## Pruebas y protección de calidad

- `tests/avatar_catalog_motion_contract.py`: cobertura exacta de 1.257
  IDs, 155 familias, 49 comandos, cinco categorías, tipado, supervisión,
  estado visual y mecanismo de fallback sin escenas incorrectas.
- `AvatarJointTimelineTest`: 49 × 23 ejes, fases y continuidad.
- `AvatarMotionEngineTest`: ocho clips laterales, preservación de
  longitud, apoyos y comienzo/fin sin salto.
- `AvatarExerciseCommandCatalogInstrumentedTest`: carga real desde
  assets Android, recuentos, acceso seguro y exportación de 12 fotogramas.
- Se mantienen `NativeAvatarRenderTest` y los contratos V2–V13.
- GitHub Actions: pruebas JVM, Lint, APK y prueba real del emulador Android 16.

## Trabajo restante antes de afirmar fidelidad de los 1.257 ejercicios

Los 1.249 perfiles `SPEC_ONLY` aún necesitan ilustraciones terminadas.
Para cada uno deberá verificarse el tipo de apoyo, material representado,
pose inicial/final, planos correctos (especialmente suelo y gestos de
rotación), proximidad y superposición visual de articulaciones, y
continuidad geométrica en todas las fases. Algunas variantes del mismo
tipo podrán reutilizar una animación y otras requerirán escenas propias.
No se debe activar una ilustración solo para elevar artificialmente
el porcentaje de cobertura visual.
