# Asterion: comandos articulares del catálogo y visor 2D (V14, PR #35)

## Estado y alcance

Las 1.257 variantes en `variants.tsv` tienen ahora un **registro explícito**
en `avatar_command_profiles.tsv`. El registro comprende ID de variante,
nombre, tipo, familia de movimiento, cámara, estado visual, tempo de animación
en milisegundos, amplitud relativa del gesto, y ocho canales articulares
(cuello, hombro, codo, muñeca, tronco, cadera, rodilla y tobillo).

**Esto NO significa 1.257 animaciones biomecánicas distintas y verificadas.**
La implementación usa comandos reutilizables, reutiliza las seis secuencias
anteriores y añade varios movimientos esquemáticos de articulaciones. Cuando
el rig actual no puede dibujar una variante con suficiente fidelidad, el
registro y su descripción permanecen disponibles, pero la animación se
desactiva: es más seguro que mostrar una sentadilla o un salto equivocados.

- **601** variantes: `SCHEMATIC`, representación ilustrativa.
- **656** variantes: `TEXT_ONLY`, descripción completa y sin animación que
  pueda confundirse con la ejecución correcta.
- Cobertura del índice: **1.257/1.257**, sin IDs duplicados ni omitidos.

Estas cantidades corresponden a la revisión del catálogo V14 y deben
actualizarse si se añaden variantes o nuevas ilustraciones.

## Gramática de comandos y compatibilidad

`AvatarCommandCatalog.Command` incorpora gestos ilustrativos de sentadilla,
bisagra, zancada, subida a escalón, trabajo de tobillos, movilidad de rodillas,
extensión de cadera, flexión/extensión de codos, presión/tracción de brazos,
rotación de hombro, estabilidad, inclinación de tronco, movilidad cervical,
respiración, estiramiento, marcha, pedaleo esquemático y saltos de tijera.

Los seis presets de `AvatarMotionEngine.Motion` se mantienen intactos:
`SQUAT`, `HIP_HINGE`, `ARM_RAISE`, `MARCH`, `LUNGE` y `BURPEE`.
También permanecen las cámaras originales frontal, frontal con sentadilla,
frontal con tijeras, frontal de codos y superior de flexión diamante.

Cada comando contiene tres frases distintas:

1. **Inicio:** apoyos y alineaciones ilustradas antes del gesto.
2. **Acción:** qué parte del cuerpo se desplaza, cuáles articulaciones
   actúan y qué apoyos se conservan.
3. **Final/retorno:** regreso del movimiento a la posición de partida
   sin salto entre el último y primer fotograma.

Los ocho canales guardan `grados_inicio > grados_activos > grados_final`,
con el primero igual al último. **Son marcas de diseño para una proyección
2D, NO grados clínicos de las articulaciones de una persona**. Para vistas
laterales ilustrables, el botón `Comandos articulares` muestra además
14 coordenadas de articulaciones reales del rig (cerca y lejos) en
**inicio (0), gesto activo (0,49) y final (1)**. Esas coordenadas se
calculan directamente con `AvatarCommandKinematics.pose` y son las mismas
que utiliza `Canvas`; se expresan en unidades virtuales de 400×480.

## Continuidad, transiciones y restricciones

El ciclo usa `AvatarKinematics.depthAtPhase` con aceleración/desaceleración
suave, pausa breve y retorno; la velocidad cambia el tiempo del ciclo,
no el número de fotogramas. Las fases del motor son:

| Fase del ciclo | Intervalo | Comportamiento |
|---|---|---|
| Preparación | 0–0,08 | Postura inicial y apoyos fijados |
| Movimiento hacia el punto activo | 0,08–0,42 | Interpolación cúbica continua |
| Punto activo | 0,42–0,55 | Pausa ilustrativa |
| Retorno | 0,55–0,92 | Interpolación cúbica inversa |
| Cierre | 0,92–1 | Igual posición inicial; se reinicia el ciclo |

Los movimientos generados comparten `AvatarRigConstraints.twoBone`:
fémur/tibia y brazo/antebrazo conservan las longitudes configuradas.
Se rechazan distancias no alcanzables o entradas no finitas. Los seis
movimientos originales usan su propia cinemática ya probada.

La vista y el tempo se seleccionan desde el ID específico; las variantes
más complejas usan menor amplitud **como representación ilustrativa**, no
como indicación de entrenamiento. Los comandos no prescriben intensidades,
tiempos, repeticiones ni correcciones de técnica.

## Visualización y límites honestos

El visor conserva los personajes azul y morado, el renderer orgánico de
codos/rodillas y todas sus cámaras. Se mantiene la vista normal desde
Rutinas, pero **cada paso** tiene ahora un botón para abrir directamente
su `variant_id`; el visor general también permite buscar por nombre.

Las variantes `TEXT_ONLY` incluyen, entre otras, movimientos tumbados,
equilibrio bilateral incompatible con el gesto unilateral disponible,
equipamiento no dibujado, acciones acuáticas, giros axiales y movimientos
que requieren una cámara distinta. No se hace un render falso usando
el gesto de una variante diferente. El sistema no añade captura de
movimiento, reconstrucción 3D ni evaluación clínica.

### Muestreo real para revisión

La prueba instrumentada exporta 24 PNG del **Canvas Android real**:
dos paletas × tres fases × cuatro variantes antes no vinculadas:

- `curl_biceps_v03`: curl alterno de pie (arquetipo flexión de codo).
- `flexion_rodilla_bipeda_v03`: flexión de rodilla de pie con pausa.
- `extension_cadera_bipeda_v01`: extensión de cadera de pie sin carga.
- `equilibrio_unipodal_v04`: equilibrio unilateral sin apoyo.

Las imágenes son visualizaciones **esquemáticas del rig existente**,
no fotografías ni instrucciones anatómicas verificadas. En particular,
los implementos, la alternancia contralateral y el contexto ambiental
de cada variante aún requieren más recursos visuales.

Los archivos se generan en `build/runtime-diagnostics/asterion-avatar-catalog-preview/`
del artefacto del emulador, si sus 14 pruebas instrumentadas pasan.
Las 42 capturas originales siguen generándose por separado.

## Pruebas y criterios para avanzar

- `AvatarCommandCatalogTest` valida 1.257 IDs, curvas cerradas,
  parámetros, continuidad y longitud de segmentos de las ilustraciones
  laterales, y que se bloqueen variantes no representables.
- `tests/avatar_command_catalog_contract.py` comprueba todos los
  registros y la conexión entre catálogo, Rutinas y visor.
- Android 16 renderiza las nuevas vistas reales junto a las anteriores.
- No se afecta Monarca, cálculos de volumen ni seguimiento de rutinas.

**Mejoras futuras recomendadas y no implementadas:** rigs tumbados,
sentados, de máquinas y deportes acuáticos; cámaras frontales adicionales
para abducción/giros; representación de implementos; controles de
alternancia izquierda/derecha; evaluación visual humana de las secuencias
antes de cambiar una variante de `TEXT_ONLY` a `SCHEMATIC`.

Referencias técnicas:
[View.postInvalidateOnAnimation](https://developer.android.com/reference/android/view/View#postInvalidateOnAnimation()),
[ACE Exercise Library](https://www.acefitness.org/resources/everyone/exercise-library/).
