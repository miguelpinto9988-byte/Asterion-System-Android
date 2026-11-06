# Catálogo completo de ejercicios Asterion — v1.0.0

**Inventario:** 39 familias · 80 tipos · 445 variantes. Este archivo se genera conceptualmente a partir de los tres TSV versionados en `progress-engine/src/main/resources/asterion_exercises/v1/`. Las denominaciones y clasificaciones son originales de Asterion; las bibliotecas públicas de ejercicios se consultaron como referencias generales, sin reproducir sus instrucciones, imágenes o vídeos.

**Lectura:** `L1` iniciación · `L2` básico · `L3` intermedio · `L4` avanzado · `L5` alta complejidad. Estas etiquetas describen la coordinación, el apoyo y la técnica de la variante, **no la capacidad de una persona**. Un nivel superior no es una meta ni una recomendación de entrenamiento. `Orientación` indica que se recomienda supervisión cualificada; las variantes L4–L5 nunca entran en sugerencias automáticas.

**Campos:** material = implementos que identifican esa variante; modalidad = forma de ejecución; ID = identificador técnico estable para enlazar rutinas en una futura integración. No se incluyen instrucciones de realización ni prescripciones de entrenamiento.

## Resumen por categoría

| Categoría | Familias | Tipos | Variantes |
|---|---:|---:|---:|
| Piernas y cadera | 9 | 21 | 119 |
| Tren superior | 10 | 21 | 124 |
| Tronco y estabilidad | 5 | 11 | 62 |
| Equilibrio y coordinación | 2 | 4 | 22 |
| Trabajo integrado | 1 | 2 | 10 |
| Actividad cardiovascular | 6 | 9 | 46 |
| Movilidad, respiración y postura | 6 | 12 | 62 |

## Piernas y cadera

### Sentadillas (`sentadillas`)

Patrón motor: `sentadilla`.

**Sentadilla bilateral** — 7 variantes (`sentadilla_bilateral`)

- L1 · Sentadilla a silla — material: `silla`; modalidad: `asistido` · ID: `sentadilla_bilateral_v01`
- L1 · Sentadilla asistida en pared — material: `pared`; modalidad: `asistido` · ID: `sentadilla_bilateral_v02`
- L2 · Sentadilla libre — material: `sin_equipo`; modalidad: `bilateral` · ID: `sentadilla_bilateral_v03`
- L3 · Sentadilla con pausa — material: `sin_equipo`; modalidad: `controlado` · ID: `sentadilla_bilateral_v04`
- L3 · Sentadilla goblet — material: `mancuerna`; modalidad: `carga_externa` · ID: `sentadilla_bilateral_v05`
- L4 · Sentadilla frontal con mancuernas — material: `mancuerna`; modalidad: `carga_externa` · orientación recomendada · ID: `sentadilla_bilateral_v06`
- L5 · Sentadilla frontal con barra — material: `barra`; modalidad: `carga_externa` · orientación recomendada · ID: `sentadilla_bilateral_v07`

**Sentadilla unilateral** — 6 variantes (`sentadilla_unilateral`)

- L1 · Sentadilla dividida con apoyo — material: `silla`; modalidad: `asistido` · ID: `sentadilla_unilateral_v01`
- L2 · Sentadilla dividida libre — material: `sin_equipo`; modalidad: `unilateral` · ID: `sentadilla_unilateral_v02`
- L3 · Sentadilla búlgara asistida — material: `banco`; modalidad: `asistido` · ID: `sentadilla_unilateral_v03`
- L4 · Sentadilla búlgara con mancuernas — material: `banco + mancuerna`; modalidad: `carga_externa` · orientación recomendada · ID: `sentadilla_unilateral_v04`
- L4 · Sentadilla unilateral a caja alta — material: `silla`; modalidad: `unilateral` · orientación recomendada · ID: `sentadilla_unilateral_v05`
- L5 · Sentadilla unilateral a caja baja — material: `banco`; modalidad: `unilateral` · orientación recomendada · ID: `sentadilla_unilateral_v06`

**Sentadilla lateral** — 6 variantes (`sentadilla_lateral`)

- L1 · Traslado de peso lateral — material: `sin_equipo`; modalidad: `controlado` · ID: `sentadilla_lateral_v01`
- L2 · Sentadilla lateral parcial — material: `sin_equipo`; modalidad: `lateral` · ID: `sentadilla_lateral_v02`
- L3 · Sentadilla lateral completa — material: `sin_equipo`; modalidad: `lateral` · ID: `sentadilla_lateral_v03`
- L4 · Sentadilla lateral con mancuerna — material: `mancuerna`; modalidad: `carga_externa` · orientación recomendada · ID: `sentadilla_lateral_v04`
- L4 · Sentadilla cosaca asistida — material: `silla`; modalidad: `asistido` · orientación recomendada · ID: `sentadilla_lateral_v05`
- L5 · Sentadilla cosaca libre — material: `sin_equipo`; modalidad: `unilateral` · orientación recomendada · ID: `sentadilla_lateral_v06`

### Zancadas y desplantes (`zancadas`)

Patrón motor: `zancada`.

**Zancada estática** — 6 variantes (`zancada_estatica`)

- L1 · Posición dividida con apoyo — material: `silla`; modalidad: `asistido` · ID: `zancada_estatica_v01`
- L2 · Zancada estática corta — material: `sin_equipo`; modalidad: `controlado` · ID: `zancada_estatica_v02`
- L2 · Zancada estática — material: `sin_equipo`; modalidad: `bilateral` · ID: `zancada_estatica_v03`
- L3 · Zancada estática con pausa — material: `sin_equipo`; modalidad: `controlado` · ID: `zancada_estatica_v04`
- L4 · Zancada estática con mancuernas — material: `mancuerna`; modalidad: `carga_externa` · orientación recomendada · ID: `zancada_estatica_v05`
- L5 · Zancada estática con barra — material: `barra`; modalidad: `carga_externa` · orientación recomendada · ID: `zancada_estatica_v06`

**Zancada dinámica** — 6 variantes (`zancada_dinamica`)

- L1 · Paso atrás con apoyo — material: `silla`; modalidad: `asistido` · ID: `zancada_dinamica_v01`
- L2 · Zancada atrás — material: `sin_equipo`; modalidad: `alterno` · ID: `zancada_dinamica_v02`
- L3 · Zancada adelante — material: `sin_equipo`; modalidad: `alterno` · ID: `zancada_dinamica_v03`
- L3 · Zancada caminando — material: `sin_equipo`; modalidad: `dinamico` · ID: `zancada_dinamica_v04`
- L4 · Zancada caminando con mancuernas — material: `mancuerna`; modalidad: `carga_externa` · orientación recomendada · ID: `zancada_dinamica_v05`
- L5 · Zancada atrás desde escalón — material: `escalon`; modalidad: `unilateral` · orientación recomendada · ID: `zancada_dinamica_v06`

**Zancada lateral** — 5 variantes (`zancada_lateral`)

- L1 · Paso lateral con flexión ligera — material: `sin_equipo`; modalidad: `lateral` · ID: `zancada_lateral_v01`
- L2 · Zancada lateral con apoyo — material: `silla`; modalidad: `asistido` · ID: `zancada_lateral_v02`
- L3 · Zancada lateral libre — material: `sin_equipo`; modalidad: `lateral` · ID: `zancada_lateral_v03`
- L3 · Zancada lateral alterna — material: `sin_equipo`; modalidad: `alterno` · ID: `zancada_lateral_v04`
- L4 · Zancada lateral con mancuerna — material: `mancuerna`; modalidad: `carga_externa` · orientación recomendada · ID: `zancada_lateral_v05`

### Bisagra de cadera (`bisagra_cadera`)

Patrón motor: `bisagra`.

**Peso muerto** — 6 variantes (`peso_muerto`)

- L1 · Bisagra con palo de referencia — material: `palo`; modalidad: `controlado` · ID: `peso_muerto_v01`
- L2 · Peso muerto con kettlebell elevado — material: `kettlebell + escalon`; modalidad: `asistido` · ID: `peso_muerto_v02`
- L3 · Peso muerto con mancuerna — material: `mancuerna`; modalidad: `carga_externa` · ID: `peso_muerto_v03`
- L3 · Peso muerto sumo con kettlebell — material: `kettlebell`; modalidad: `carga_externa` · ID: `peso_muerto_v04`
- L4 · Peso muerto con barra desde bloques — material: `barra + escalon`; modalidad: `carga_externa` · orientación recomendada · ID: `peso_muerto_v05`
- L5 · Peso muerto convencional con barra — material: `barra`; modalidad: `carga_externa` · orientación recomendada · ID: `peso_muerto_v06`

**Peso muerto rumano** — 6 variantes (`peso_muerto_rumano`)

- L1 · Bisagra de cadera sin carga — material: `sin_equipo`; modalidad: `controlado` · ID: `peso_muerto_rumano_v01`
- L2 · Bisagra con banda — material: `banda`; modalidad: `resistencia` · ID: `peso_muerto_rumano_v02`
- L3 · Peso muerto rumano con mancuernas — material: `mancuerna`; modalidad: `carga_externa` · ID: `peso_muerto_rumano_v03`
- L4 · Peso muerto rumano en apoyo unilateral — material: `silla + mancuerna`; modalidad: `asistido` · orientación recomendada · ID: `peso_muerto_rumano_v04`
- L4 · Peso muerto rumano unilateral — material: `mancuerna`; modalidad: `unilateral` · orientación recomendada · ID: `peso_muerto_rumano_v05`
- L5 · Peso muerto rumano con barra — material: `barra`; modalidad: `carga_externa` · orientación recomendada · ID: `peso_muerto_rumano_v06`

**Buenos días** — 5 variantes (`buenos_dias`)

- L1 · Buenos días sentado sin carga — material: `silla`; modalidad: `controlado` · ID: `buenos_dias_v01`
- L2 · Buenos días de pie sin carga — material: `sin_equipo`; modalidad: `bilateral` · ID: `buenos_dias_v02`
- L3 · Buenos días con banda — material: `banda`; modalidad: `resistencia` · ID: `buenos_dias_v03`
- L4 · Buenos días con mancuerna al pecho — material: `mancuerna`; modalidad: `carga_externa` · orientación recomendada · ID: `buenos_dias_v04`
- L5 · Buenos días con barra ligera — material: `barra`; modalidad: `carga_externa` · orientación recomendada · ID: `buenos_dias_v05`

### Puentes y extensión de cadera (`puentes_cadera`)

Patrón motor: `extension_cadera`.

**Puente de glúteos** — 6 variantes (`puente_gluteo`)

- L1 · Puente corto de glúteos — material: `colchoneta`; modalidad: `bilateral` · ID: `puente_gluteo_v01`
- L2 · Puente de glúteos básico — material: `colchoneta`; modalidad: `bilateral` · ID: `puente_gluteo_v02`
- L2 · Puente de glúteos con pausa — material: `colchoneta`; modalidad: `isometrico` · ID: `puente_gluteo_v03`
- L3 · Puente de glúteos con banda — material: `banda + colchoneta`; modalidad: `resistencia` · ID: `puente_gluteo_v04`
- L4 · Puente de glúteos alternando apoyo — material: `colchoneta`; modalidad: `alterno` · orientación recomendada · ID: `puente_gluteo_v05`
- L5 · Puente de glúteos unilateral — material: `colchoneta`; modalidad: `unilateral` · orientación recomendada · ID: `puente_gluteo_v06`

**Elevación de cadera con apoyo** — 6 variantes (`hip_thrust`)

- L1 · Elevación de cadera en suelo — material: `colchoneta`; modalidad: `bilateral` · ID: `hip_thrust_v01`
- L2 · Elevación de cadera en banco sin carga — material: `banco`; modalidad: `bilateral` · ID: `hip_thrust_v02`
- L3 · Elevación de cadera con banda — material: `banco + banda`; modalidad: `resistencia` · ID: `hip_thrust_v03`
- L3 · Elevación de cadera con mancuerna — material: `banco + mancuerna`; modalidad: `carga_externa` · ID: `hip_thrust_v04`
- L4 · Elevación de cadera unilateral con banco — material: `banco`; modalidad: `unilateral` · orientación recomendada · ID: `hip_thrust_v05`
- L5 · Elevación de cadera con barra — material: `banco + barra`; modalidad: `carga_externa` · orientación recomendada · ID: `hip_thrust_v06`

### Subidas y descensos de escalón (`subida_escalon`)

Patrón motor: `paso_vertical`.

**Subida al escalón** — 5 variantes (`step_up`)

- L1 · Subida a escalón bajo con apoyo — material: `escalon + silla`; modalidad: `asistido` · ID: `step_up_v01`
- L2 · Subida a escalón bajo — material: `escalon`; modalidad: `unilateral` · ID: `step_up_v02`
- L3 · Subida a escalón medio — material: `escalon`; modalidad: `unilateral` · ID: `step_up_v03`
- L4 · Subida a escalón con mancuerna — material: `escalon + mancuerna`; modalidad: `carga_externa` · orientación recomendada · ID: `step_up_v04`
- L4 · Subida lateral a escalón — material: `escalon`; modalidad: `lateral` · orientación recomendada · ID: `step_up_v05`

**Descenso controlado** — 5 variantes (`step_down`)

- L1 · Bajada de escalón bajo con apoyo — material: `escalon + silla`; modalidad: `asistido` · ID: `step_down_v01`
- L2 · Bajada de escalón bajo — material: `escalon`; modalidad: `controlado` · ID: `step_down_v02`
- L3 · Bajada lateral de escalón — material: `escalon`; modalidad: `lateral` · ID: `step_down_v03`
- L4 · Bajada de escalón con pausa — material: `escalon`; modalidad: `controlado` · orientación recomendada · ID: `step_down_v04`
- L5 · Bajada de escalón con mancuerna — material: `escalon + mancuerna`; modalidad: `carga_externa` · orientación recomendada · ID: `step_down_v05`

### Pantorrillas y tobillo (`pantorrillas`)

Patrón motor: `flexion_tobillo`.

**Elevación de talones** — 6 variantes (`elevacion_gemelos`)

- L1 · Elevación de talones sentado — material: `silla`; modalidad: `bilateral` · ID: `elevacion_gemelos_v01`
- L1 · Elevación de talones de pie con apoyo — material: `silla`; modalidad: `asistido` · ID: `elevacion_gemelos_v02`
- L2 · Elevación de talones de pie — material: `sin_equipo`; modalidad: `bilateral` · ID: `elevacion_gemelos_v03`
- L3 · Elevación de talones sobre escalón — material: `escalon`; modalidad: `bilateral` · ID: `elevacion_gemelos_v04`
- L3 · Elevación de talón unilateral con apoyo — material: `silla`; modalidad: `asistido` · ID: `elevacion_gemelos_v05`
- L4 · Elevación de talón unilateral con mancuerna — material: `mancuerna`; modalidad: `carga_externa` · orientación recomendada · ID: `elevacion_gemelos_v06`

**Elevación de puntas** — 5 variantes (`dorsiflexion_tibial`)

- L1 · Elevación de puntas sentado — material: `silla`; modalidad: `bilateral` · ID: `dorsiflexion_tibial_v01`
- L2 · Elevación de puntas de pie con apoyo — material: `pared`; modalidad: `asistido` · ID: `dorsiflexion_tibial_v02`
- L2 · Elevación de puntas de pie — material: `sin_equipo`; modalidad: `bilateral` · ID: `dorsiflexion_tibial_v03`
- L3 · Dorsiflexión con banda sentado — material: `banda`; modalidad: `resistencia` · ID: `dorsiflexion_tibial_v04`
- L4 · Elevación tibial con pausa — material: `pared`; modalidad: `isometrico` · orientación recomendada · ID: `dorsiflexion_tibial_v05`

### Flexión de rodilla (`flexion_rodilla`)

Patrón motor: `flexion_rodilla`.

**Curl femoral** — 6 variantes (`curl_femoral`)

- L1 · Curl femoral de pie sin carga — material: `silla`; modalidad: `asistido` · ID: `curl_femoral_v01`
- L2 · Curl femoral con banda — material: `banda`; modalidad: `resistencia` · ID: `curl_femoral_v02`
- L3 · Curl femoral tumbado con banda — material: `colchoneta + banda`; modalidad: `resistencia` · ID: `curl_femoral_v03`
- L3 · Curl femoral sentado en máquina — material: `maquina`; modalidad: `maquina` · orientación recomendada · ID: `curl_femoral_v04`
- L4 · Curl femoral tumbado en máquina — material: `maquina`; modalidad: `maquina` · orientación recomendada · ID: `curl_femoral_v05`
- L5 · Curl femoral unilateral en máquina — material: `maquina`; modalidad: `unilateral` · orientación recomendada · ID: `curl_femoral_v06`

**Curl femoral deslizante** — 5 variantes (`curl_deslizante`)

- L1 · Deslizamiento parcial de talones — material: `toalla + colchoneta`; modalidad: `asistido` · ID: `curl_deslizante_v01`
- L2 · Curl de talones deslizante bilateral — material: `toalla + colchoneta`; modalidad: `bilateral` · ID: `curl_deslizante_v02`
- L3 · Curl de talones con pausa — material: `toalla + colchoneta`; modalidad: `controlado` · ID: `curl_deslizante_v03`
- L4 · Curl deslizante excéntrico bilateral — material: `toalla + colchoneta`; modalidad: `controlado` · orientación recomendada · ID: `curl_deslizante_v04`
- L5 · Curl deslizante unilateral asistido — material: `toalla + colchoneta`; modalidad: `unilateral` · orientación recomendada · ID: `curl_deslizante_v05`

### Abducción y aducción de cadera (`control_cadera`)

Patrón motor: `control_cadera`.

**Abducción de cadera** — 6 variantes (`abduccion`)

- L1 · Abducción de cadera de pie con apoyo — material: `silla`; modalidad: `asistido` · ID: `abduccion_v01`
- L2 · Abducción de cadera en decúbito lateral — material: `colchoneta`; modalidad: `unilateral` · ID: `abduccion_v02`
- L2 · Abducción con banda sentado — material: `banda + silla`; modalidad: `resistencia` · ID: `abduccion_v03`
- L3 · Abducción de cadera con banda de pie — material: `banda`; modalidad: `resistencia` · ID: `abduccion_v04`
- L4 · Abducción de cadera en polea — material: `polea`; modalidad: `carga_externa` · orientación recomendada · ID: `abduccion_v05`
- L4 · Abducción unilateral en máquina — material: `maquina`; modalidad: `maquina` · orientación recomendada · ID: `abduccion_v06`

**Aducción de cadera** — 6 variantes (`aduccion`)

- L1 · Aducción de cadera sentado con pelota — material: `pelota + silla`; modalidad: `controlado` · ID: `aduccion_v01`
- L2 · Aducción en decúbito lateral — material: `colchoneta`; modalidad: `unilateral` · ID: `aduccion_v02`
- L2 · Aducción isométrica con pelota — material: `pelota`; modalidad: `isometrico` · ID: `aduccion_v03`
- L3 · Aducción de cadera con banda — material: `banda`; modalidad: `resistencia` · ID: `aduccion_v04`
- L4 · Aducción de cadera en polea — material: `polea`; modalidad: `carga_externa` · orientación recomendada · ID: `aduccion_v05`
- L4 · Aducción de cadera en máquina — material: `maquina`; modalidad: `maquina` · orientación recomendada · ID: `aduccion_v06`

**Caminata lateral con banda** — 5 variantes (`caminata_banda`)

- L1 · Paso lateral sin banda — material: `sin_equipo`; modalidad: `lateral` · ID: `caminata_banda_v01`
- L2 · Paso lateral con minibanda ligera — material: `banda`; modalidad: `lateral` · ID: `caminata_banda_v02`
- L3 · Caminata lateral con minibanda — material: `banda`; modalidad: `resistencia` · ID: `caminata_banda_v03`
- L4 · Caminata diagonal con minibanda — material: `banda`; modalidad: `resistencia` · orientación recomendada · ID: `caminata_banda_v04`
- L4 · Caminata lateral con banda y pausa — material: `banda`; modalidad: `controlado` · orientación recomendada · ID: `caminata_banda_v05`

### Extensión de rodilla (`extension_rodilla`)

Patrón motor: `extension_rodilla`.

**Extensión de cuádriceps** — 5 variantes (`extension_cuadriceps`)

- L1 · Extensión de rodilla sentado sin carga — material: `silla`; modalidad: `unilateral` · ID: `extension_cuadriceps_v01`
- L2 · Extensión terminal de rodilla con banda — material: `banda`; modalidad: `resistencia` · ID: `extension_cuadriceps_v02`
- L3 · Extensión de cuádriceps en máquina — material: `maquina`; modalidad: `maquina` · orientación recomendada · ID: `extension_cuadriceps_v03`
- L4 · Extensión unilateral en máquina — material: `maquina`; modalidad: `unilateral` · orientación recomendada · ID: `extension_cuadriceps_v04`
- L5 · Extensión de cuádriceps con pausa en máquina — material: `maquina`; modalidad: `controlado` · orientación recomendada · ID: `extension_cuadriceps_v05`

## Tren superior

### Empuje horizontal (`empuje_horizontal`)

Patrón motor: `empuje_horizontal`.

**Flexiones de brazos** — 7 variantes (`flexiones`)

- L1 · Flexión de pared — material: `pared`; modalidad: `asistido` · ID: `flexiones_v01`
- L1 · Flexión inclinada sobre apoyo estable — material: `banco`; modalidad: `asistido` · ID: `flexiones_v02`
- L2 · Flexión de rodillas — material: `colchoneta`; modalidad: `asistido` · ID: `flexiones_v03`
- L3 · Flexión de brazos estándar — material: `sin_equipo`; modalidad: `bilateral` · ID: `flexiones_v04`
- L3 · Flexión de agarre estrecho — material: `sin_equipo`; modalidad: `bilateral` · ID: `flexiones_v05`
- L4 · Flexión con pies elevados — material: `banco`; modalidad: `bilateral` · orientación recomendada · ID: `flexiones_v06`
- L5 · Flexión arqueada con apoyo alterno — material: `sin_equipo`; modalidad: `unilateral` · orientación recomendada · ID: `flexiones_v07`

**Press de pecho** — 7 variantes (`press_pecho`)

- L1 · Press de pecho con banda de pie — material: `banda`; modalidad: `resistencia` · ID: `press_pecho_v01`
- L2 · Press de pecho en máquina — material: `maquina`; modalidad: `maquina` · orientación recomendada · ID: `press_pecho_v02`
- L2 · Press de pecho en suelo con mancuernas — material: `mancuerna + colchoneta`; modalidad: `carga_externa` · ID: `press_pecho_v03`
- L3 · Press de pecho con mancuernas en banco — material: `mancuerna + banco`; modalidad: `carga_externa` · ID: `press_pecho_v04`
- L3 · Press inclinado de pecho con mancuernas — material: `mancuerna + banco`; modalidad: `carga_externa` · ID: `press_pecho_v05`
- L4 · Press de pecho con barra — material: `barra + banco`; modalidad: `carga_externa` · orientación recomendada · ID: `press_pecho_v06`
- L5 · Press unilateral con mancuerna — material: `mancuerna + banco`; modalidad: `unilateral` · orientación recomendada · ID: `press_pecho_v07`

**Fondos asistidos y paralelas** — 5 variantes (`fondos`)

- L1 · Extensión de tríceps en pared — material: `pared`; modalidad: `asistido` · ID: `fondos_v01`
- L2 · Fondo asistido en máquina — material: `maquina`; modalidad: `maquina` · orientación recomendada · ID: `fondos_v02`
- L3 · Fondo asistido con pies en apoyo — material: `paralelas + silla`; modalidad: `asistido` · orientación recomendada · ID: `fondos_v03`
- L4 · Fondo en paralelas con asistencia elástica — material: `paralelas + banda`; modalidad: `asistido` · orientación recomendada · ID: `fondos_v04`
- L5 · Fondo en paralelas libre — material: `paralelas`; modalidad: `bilateral` · orientación recomendada · ID: `fondos_v05`

### Empuje vertical (`empuje_vertical`)

Patrón motor: `empuje_vertical`.

**Press de hombros** — 6 variantes (`press_hombros`)

- L1 · Empuje de hombros con banda ligera — material: `banda`; modalidad: `resistencia` · ID: `press_hombros_v01`
- L2 · Press de hombros sentado con mancuernas — material: `mancuerna + silla`; modalidad: `carga_externa` · ID: `press_hombros_v02`
- L3 · Press de hombros de pie con mancuernas — material: `mancuerna`; modalidad: `carga_externa` · ID: `press_hombros_v03`
- L3 · Press Arnold con mancuernas — material: `mancuerna`; modalidad: `rotacion` · ID: `press_hombros_v04`
- L4 · Press unilateral de hombro — material: `mancuerna`; modalidad: `unilateral` · orientación recomendada · ID: `press_hombros_v05`
- L5 · Press militar con barra — material: `barra`; modalidad: `carga_externa` · orientación recomendada · ID: `press_hombros_v06`

**Flexiones en pica** — 6 variantes (`flexion_pica`)

- L1 · Flexión inclinada para hombros en pared — material: `pared`; modalidad: `asistido` · ID: `flexion_pica_v01`
- L2 · Flexión en pica con apoyo alto — material: `banco`; modalidad: `asistido` · ID: `flexion_pica_v02`
- L2 · Flexión en pica con rodillas flexionadas — material: `sin_equipo`; modalidad: `bilateral` · ID: `flexion_pica_v03`
- L3 · Flexión en pica estándar — material: `sin_equipo`; modalidad: `bilateral` · ID: `flexion_pica_v04`
- L4 · Flexión en pica con pies elevados — material: `banco`; modalidad: `bilateral` · orientación recomendada · ID: `flexion_pica_v05`
- L5 · Flexión en pica con pausa — material: `banco`; modalidad: `controlado` · orientación recomendada · ID: `flexion_pica_v06`

### Aperturas de pecho (`aperturas_pecho`)

Patrón motor: `aduccion_hombro`.

**Aperturas pectorales** — 6 variantes (`apertura_pecho`)

- L1 · Apertura de pecho con banda ligera — material: `banda`; modalidad: `resistencia` · ID: `apertura_pecho_v01`
- L2 · Apertura de pecho en máquina — material: `maquina`; modalidad: `maquina` · orientación recomendada · ID: `apertura_pecho_v02`
- L2 · Apertura de pecho en suelo con mancuernas — material: `mancuerna + colchoneta`; modalidad: `carga_externa` · ID: `apertura_pecho_v03`
- L3 · Apertura con mancuernas en banco — material: `mancuerna + banco`; modalidad: `carga_externa` · ID: `apertura_pecho_v04`
- L4 · Apertura de pecho con poleas — material: `polea`; modalidad: `carga_externa` · orientación recomendada · ID: `apertura_pecho_v05`
- L5 · Apertura unilateral con polea — material: `polea`; modalidad: `unilateral` · orientación recomendada · ID: `apertura_pecho_v06`

### Extensión de codo (`extension_codo`)

Patrón motor: `extension_codo`.

**Extensiones de tríceps** — 6 variantes (`extension_triceps`)

- L1 · Extensión de tríceps con banda ligera — material: `banda`; modalidad: `resistencia` · ID: `extension_triceps_v01`
- L2 · Extensión de tríceps sobre cabeza con banda — material: `banda`; modalidad: `resistencia` · ID: `extension_triceps_v02`
- L2 · Extensión de tríceps en polea con cuerda — material: `polea`; modalidad: `maquina` · orientación recomendada · ID: `extension_triceps_v03`
- L3 · Extensión de tríceps con mancuerna sentado — material: `mancuerna + silla`; modalidad: `carga_externa` · ID: `extension_triceps_v04`
- L4 · Extensión unilateral de tríceps en polea — material: `polea`; modalidad: `unilateral` · orientación recomendada · ID: `extension_triceps_v05`
- L4 · Extensión de tríceps sobre cabeza con mancuernas — material: `mancuerna`; modalidad: `carga_externa` · orientación recomendada · ID: `extension_triceps_v06`

**Press de agarre cerrado** — 6 variantes (`press_cerrado`)

- L1 · Empuje corto con banda estrecha — material: `banda`; modalidad: `resistencia` · ID: `press_cerrado_v01`
- L1 · Flexión de brazos con apoyo estrecho en pared — material: `pared`; modalidad: `asistido` · ID: `press_cerrado_v02`
- L2 · Flexión inclinada de agarre estrecho — material: `banco`; modalidad: `asistido` · ID: `press_cerrado_v03`
- L3 · Press cerrado con mancuernas en suelo — material: `mancuerna + colchoneta`; modalidad: `carga_externa` · ID: `press_cerrado_v04`
- L4 · Press cerrado con mancuernas en banco — material: `mancuerna + banco`; modalidad: `carga_externa` · orientación recomendada · ID: `press_cerrado_v05`
- L5 · Press de banca con agarre cerrado — material: `barra + banco`; modalidad: `carga_externa` · orientación recomendada · ID: `press_cerrado_v06`

### Tracción horizontal (`traccion_horizontal`)

Patrón motor: `traccion_horizontal`.

**Remo de fuerza** — 8 variantes (`remo`)

- L1 · Remo sentado con banda ligera — material: `banda + silla`; modalidad: `resistencia` · ID: `remo_v01`
- L1 · Remo de pie con banda — material: `banda`; modalidad: `resistencia` · ID: `remo_v02`
- L2 · Remo en máquina con apoyo de pecho — material: `maquina`; modalidad: `maquina` · orientación recomendada · ID: `remo_v03`
- L2 · Remo con mancuerna apoyada — material: `mancuerna + banco`; modalidad: `asistido` · ID: `remo_v04`
- L3 · Remo con dos mancuernas — material: `mancuerna`; modalidad: `carga_externa` · ID: `remo_v05`
- L3 · Remo en polea sentado — material: `polea`; modalidad: `maquina` · orientación recomendada · ID: `remo_v06`
- L4 · Remo unilateral en polea — material: `polea`; modalidad: `unilateral` · orientación recomendada · ID: `remo_v07`
- L5 · Remo inclinado con barra — material: `barra`; modalidad: `carga_externa` · orientación recomendada · ID: `remo_v08`

**Remo invertido** — 5 variantes (`remo_invertido`)

- L1 · Retracción escapular con anillas bajas — material: `anillas`; modalidad: `asistido` · orientación recomendada · ID: `remo_invertido_v01`
- L2 · Remo inclinado con suspensión elevada — material: `suspension`; modalidad: `asistido` · orientación recomendada · ID: `remo_invertido_v02`
- L3 · Remo invertido con rodillas flexionadas — material: `barra_baja`; modalidad: `bilateral` · ID: `remo_invertido_v03`
- L4 · Remo invertido con piernas extendidas — material: `barra_baja`; modalidad: `bilateral` · orientación recomendada · ID: `remo_invertido_v04`
- L5 · Remo invertido con pies elevados — material: `barra_baja + banco`; modalidad: `bilateral` · orientación recomendada · ID: `remo_invertido_v05`

### Tracción vertical (`traccion_vertical`)

Patrón motor: `traccion_vertical`.

**Dominadas y asistencias** — 7 variantes (`dominadas`)

- L1 · Descenso escapular con banda — material: `banda`; modalidad: `controlado` · ID: `dominadas_v01`
- L2 · Dominada asistida en máquina — material: `maquina`; modalidad: `asistido` · orientación recomendada · ID: `dominadas_v02`
- L3 · Dominada con banda elástica asistida — material: `barra_dominadas + banda`; modalidad: `asistido` · orientación recomendada · ID: `dominadas_v03`
- L3 · Dominada con agarre supino asistida — material: `barra_dominadas + banda`; modalidad: `asistido` · orientación recomendada · ID: `dominadas_v04`
- L4 · Dominada de agarre prono — material: `barra_dominadas`; modalidad: `bilateral` · orientación recomendada · ID: `dominadas_v05`
- L4 · Dominada de agarre supino — material: `barra_dominadas`; modalidad: `bilateral` · orientación recomendada · ID: `dominadas_v06`
- L5 · Dominada con pausa controlada — material: `barra_dominadas`; modalidad: `controlado` · orientación recomendada · ID: `dominadas_v07`

**Jalones verticales** — 6 variantes (`jalon`)

- L1 · Jalón de banda hacia pecho sentado — material: `banda + silla`; modalidad: `resistencia` · ID: `jalon_v01`
- L2 · Jalón vertical en máquina agarre neutro — material: `maquina`; modalidad: `maquina` · orientación recomendada · ID: `jalon_v02`
- L3 · Jalón vertical con polea agarre ancho — material: `polea`; modalidad: `maquina` · orientación recomendada · ID: `jalon_v03`
- L3 · Jalón vertical con polea agarre estrecho — material: `polea`; modalidad: `maquina` · orientación recomendada · ID: `jalon_v04`
- L4 · Jalón unilateral de polea alta — material: `polea`; modalidad: `unilateral` · orientación recomendada · ID: `jalon_v05`
- L4 · Jalón de brazos rectos con polea — material: `polea`; modalidad: `controlado` · orientación recomendada · ID: `jalon_v06`

### Flexión de codo (`flexion_codo`)

Patrón motor: `flexion_codo`.

**Curl de bíceps** — 6 variantes (`curl_biceps`)

- L1 · Curl de bíceps con banda ligera — material: `banda`; modalidad: `resistencia` · ID: `curl_biceps_v01`
- L2 · Curl de bíceps sentado con mancuernas — material: `mancuerna + silla`; modalidad: `carga_externa` · ID: `curl_biceps_v02`
- L2 · Curl de bíceps de pie alterno — material: `mancuerna`; modalidad: `alterno` · ID: `curl_biceps_v03`
- L3 · Curl de bíceps bilateral con mancuernas — material: `mancuerna`; modalidad: `bilateral` · ID: `curl_biceps_v04`
- L4 · Curl de bíceps en banco inclinado — material: `mancuerna + banco`; modalidad: `carga_externa` · orientación recomendada · ID: `curl_biceps_v05`
- L4 · Curl de bíceps con barra — material: `barra`; modalidad: `carga_externa` · orientación recomendada · ID: `curl_biceps_v06`

**Curl de agarre neutro** — 6 variantes (`curl_martillo`)

- L1 · Curl neutro con banda — material: `banda`; modalidad: `resistencia` · ID: `curl_martillo_v01`
- L2 · Curl martillo sentado alterno — material: `mancuerna + silla`; modalidad: `alterno` · ID: `curl_martillo_v02`
- L2 · Curl martillo de pie — material: `mancuerna`; modalidad: `bilateral` · ID: `curl_martillo_v03`
- L3 · Curl martillo cruzado — material: `mancuerna`; modalidad: `alterno` · ID: `curl_martillo_v04`
- L4 · Curl martillo en banco inclinado — material: `mancuerna + banco`; modalidad: `controlado` · orientación recomendada · ID: `curl_martillo_v05`
- L5 · Curl martillo unilateral en polea — material: `polea`; modalidad: `unilateral` · orientación recomendada · ID: `curl_martillo_v06`

### Elevaciones de hombro (`elevaciones_hombro`)

Patrón motor: `elevacion_hombro`.

**Elevación lateral** — 6 variantes (`elevacion_lateral`)

- L1 · Separación lateral de brazos sin carga — material: `sin_equipo`; modalidad: `controlado` · ID: `elevacion_lateral_v01`
- L2 · Elevación lateral con banda ligera — material: `banda`; modalidad: `resistencia` · ID: `elevacion_lateral_v02`
- L2 · Elevación lateral con mancuernas — material: `mancuerna`; modalidad: `carga_externa` · ID: `elevacion_lateral_v03`
- L3 · Elevación lateral sentado con mancuernas — material: `mancuerna + silla`; modalidad: `carga_externa` · ID: `elevacion_lateral_v04`
- L4 · Elevación lateral unilateral con polea — material: `polea`; modalidad: `unilateral` · orientación recomendada · ID: `elevacion_lateral_v05`
- L4 · Elevación lateral con pausa — material: `mancuerna`; modalidad: `isometrico` · orientación recomendada · ID: `elevacion_lateral_v06`

**Elevación frontal** — 6 variantes (`elevacion_frontal`)

- L1 · Elevación frontal sin carga — material: `sin_equipo`; modalidad: `controlado` · ID: `elevacion_frontal_v01`
- L2 · Elevación frontal con banda — material: `banda`; modalidad: `resistencia` · ID: `elevacion_frontal_v02`
- L2 · Elevación frontal alterna con mancuerna — material: `mancuerna`; modalidad: `alterno` · ID: `elevacion_frontal_v03`
- L3 · Elevación frontal bilateral con mancuernas — material: `mancuerna`; modalidad: `bilateral` · ID: `elevacion_frontal_v04`
- L4 · Elevación frontal con disco — material: `disco`; modalidad: `carga_externa` · orientación recomendada · ID: `elevacion_frontal_v05`
- L4 · Elevación frontal con polea — material: `polea`; modalidad: `carga_externa` · orientación recomendada · ID: `elevacion_frontal_v06`

**Apertura posterior** — 5 variantes (`pajaros`)

- L1 · Apertura posterior de brazos sin carga — material: `sin_equipo`; modalidad: `controlado` · ID: `pajaros_v01`
- L2 · Apertura posterior con banda — material: `banda`; modalidad: `resistencia` · ID: `pajaros_v02`
- L2 · Apertura posterior en máquina — material: `maquina`; modalidad: `maquina` · orientación recomendada · ID: `pajaros_v03`
- L3 · Apertura posterior con mancuernas sentado — material: `mancuerna + silla`; modalidad: `carga_externa` · ID: `pajaros_v04`
- L4 · Apertura posterior en banco inclinado — material: `mancuerna + banco`; modalidad: `carga_externa` · orientación recomendada · ID: `pajaros_v05`

### Control escapular (`escapulas`)

Patrón motor: `control_escapular`.

**Tracción hacia la cara** — 5 variantes (`face_pull`)

- L1 · Retracción de banda a la altura de rostro — material: `banda`; modalidad: `controlado` · ID: `face_pull_v01`
- L2 · Tracción a rostro con banda — material: `banda`; modalidad: `resistencia` · ID: `face_pull_v02`
- L3 · Tracción a rostro con polea y cuerda — material: `polea`; modalidad: `maquina` · orientación recomendada · ID: `face_pull_v03`
- L4 · Tracción a rostro con rotación externa — material: `polea`; modalidad: `rotacion` · orientación recomendada · ID: `face_pull_v04`
- L5 · Tracción a rostro unilateral — material: `polea`; modalidad: `unilateral` · orientación recomendada · ID: `face_pull_v05`

**Retracción escapular** — 5 variantes (`retraccion_escapular`)

- L1 · Aproximación de escápulas sentado — material: `silla`; modalidad: `controlado` · ID: `retraccion_escapular_v01`
- L1 · Retracción escapular contra pared — material: `pared`; modalidad: `controlado` · ID: `retraccion_escapular_v02`
- L2 · Retracción escapular con banda — material: `banda`; modalidad: `resistencia` · ID: `retraccion_escapular_v03`
- L3 · Retracción escapular en polea — material: `polea`; modalidad: `maquina` · orientación recomendada · ID: `retraccion_escapular_v04`
- L4 · Retracción escapular en suspensión — material: `suspension`; modalidad: `isometrico` · orientación recomendada · ID: `retraccion_escapular_v05`

### Rotación del hombro (`rotacion_hombro`)

Patrón motor: `rotacion_hombro`.

**Rotación externa** — 5 variantes (`rotacion_externa`)

- L1 · Rotación externa sin carga con apoyo — material: `silla`; modalidad: `controlado` · ID: `rotacion_externa_v01`
- L2 · Rotación externa con banda ligera — material: `banda`; modalidad: `resistencia` · ID: `rotacion_externa_v02`
- L3 · Rotación externa de lado con mancuerna — material: `mancuerna + colchoneta`; modalidad: `controlado` · ID: `rotacion_externa_v03`
- L4 · Rotación externa de hombro en polea — material: `polea`; modalidad: `resistencia` · orientación recomendada · ID: `rotacion_externa_v04`
- L5 · Rotación externa a 90 grados con banda — material: `banda`; modalidad: `controlado` · orientación recomendada · ID: `rotacion_externa_v05`

**Rotación interna** — 5 variantes (`rotacion_interna`)

- L1 · Rotación interna sin carga — material: `sin_equipo`; modalidad: `controlado` · ID: `rotacion_interna_v01`
- L2 · Rotación interna con banda ligera — material: `banda`; modalidad: `resistencia` · ID: `rotacion_interna_v02`
- L2 · Rotación interna con banda de pie — material: `banda`; modalidad: `resistencia` · ID: `rotacion_interna_v03`
- L3 · Rotación interna en polea — material: `polea`; modalidad: `maquina` · orientación recomendada · ID: `rotacion_interna_v04`
- L5 · Rotación interna a 90 grados con polea — material: `polea`; modalidad: `controlado` · orientación recomendada · ID: `rotacion_interna_v05`

## Tronco y estabilidad

### Extensión y estabilidad posterior (`extension_espalda`)

Patrón motor: `extension_tronco`.

**Extensión en suelo** — 5 variantes (`superman`)

- L1 · Elevación alterna de brazos boca abajo — material: `colchoneta`; modalidad: `alterno` · ID: `superman_v01`
- L1 · Elevación alterna de piernas boca abajo — material: `colchoneta`; modalidad: `alterno` · ID: `superman_v02`
- L2 · Elevación contralateral en el suelo — material: `colchoneta`; modalidad: `alterno` · ID: `superman_v03`
- L3 · Extensión de tronco corta — material: `colchoneta`; modalidad: `controlado` · ID: `superman_v04`
- L4 · Extensión de tronco con pausa breve — material: `colchoneta`; modalidad: `isometrico` · orientación recomendada · ID: `superman_v05`

**Extensión en banco** — 5 variantes (`extension_banco`)

- L1 · Extensión lumbar corta con apoyo — material: `banco`; modalidad: `asistido` · ID: `extension_banco_v01`
- L2 · Extensión de espalda con apoyo estable — material: `banco`; modalidad: `controlado` · ID: `extension_banco_v02`
- L3 · Extensión de espalda en banco inclinado — material: `banco`; modalidad: `controlado` · ID: `extension_banco_v03`
- L4 · Extensión de espalda con pausa — material: `banco`; modalidad: `isometrico` · orientación recomendada · ID: `extension_banco_v04`
- L5 · Extensión de espalda con carga ligera — material: `banco + disco`; modalidad: `carga_externa` · orientación recomendada · ID: `extension_banco_v05`

### Estabilidad anterior del tronco (`core_antextension`)

Patrón motor: `anti_extension`.

**Plancha frontal** — 6 variantes (`plancha_frontal`)

- L1 · Plancha de pared — material: `pared`; modalidad: `isometrico` · ID: `plancha_frontal_v01`
- L1 · Plancha inclinada apoyada en banco — material: `banco`; modalidad: `isometrico` · ID: `plancha_frontal_v02`
- L2 · Plancha de antebrazos con rodillas apoyadas — material: `colchoneta`; modalidad: `asistido` · ID: `plancha_frontal_v03`
- L3 · Plancha de antebrazos estándar — material: `colchoneta`; modalidad: `isometrico` · ID: `plancha_frontal_v04`
- L4 · Plancha alta con apoyo alterno de mano — material: `colchoneta`; modalidad: `alterno` · orientación recomendada · ID: `plancha_frontal_v05`
- L5 · Plancha frontal con brazo extendido — material: `colchoneta`; modalidad: `unilateral` · orientación recomendada · ID: `plancha_frontal_v06`

**Control contralateral supino** — 6 variantes (`dead_bug`)

- L1 · Activación abdominal tumbado — material: `colchoneta`; modalidad: `controlado` · ID: `dead_bug_v01`
- L1 · Dead bug con brazos estacionarios — material: `colchoneta`; modalidad: `controlado` · ID: `dead_bug_v02`
- L2 · Dead bug con una pierna alterna — material: `colchoneta`; modalidad: `alterno` · ID: `dead_bug_v03`
- L3 · Dead bug contralateral — material: `colchoneta`; modalidad: `alterno` · ID: `dead_bug_v04`
- L4 · Dead bug con banda — material: `banda + colchoneta`; modalidad: `resistencia` · orientación recomendada · ID: `dead_bug_v05`
- L5 · Dead bug con extensión alterna pausada — material: `colchoneta`; modalidad: `controlado` · orientación recomendada · ID: `dead_bug_v06`

**Extensión abdominal controlada** — 5 variantes (`rueda_abdominal`)

- L1 · Deslizamiento de brazos sobre pared — material: `pared`; modalidad: `asistido` · ID: `rueda_abdominal_v01`
- L2 · Extensión corta con pelota de estabilidad — material: `pelota`; modalidad: `asistido` · ID: `rueda_abdominal_v02`
- L3 · Deslizamiento de antebrazos con toalla — material: `toalla + colchoneta`; modalidad: `controlado` · ID: `rueda_abdominal_v03`
- L4 · Rodamiento corto con rueda desde rodillas — material: `rueda + colchoneta`; modalidad: `controlado` · orientación recomendada · ID: `rueda_abdominal_v04`
- L5 · Rodamiento de rueda con mayor recorrido y apoyo — material: `rueda + colchoneta`; modalidad: `controlado` · orientación recomendada · ID: `rueda_abdominal_v05`

### Control antirrotación (`core_anti_rotacion`)

Patrón motor: `anti_rotacion`.

**Press antirrotación** — 6 variantes (`press_pallof`)

- L1 · Antirrotación sin resistencia en apoyo — material: `silla`; modalidad: `controlado` · ID: `press_pallof_v01`
- L2 · Sostén antirrotación con banda ligera — material: `banda`; modalidad: `isometrico` · ID: `press_pallof_v02`
- L2 · Press Pallof con banda de pie — material: `banda`; modalidad: `resistencia` · ID: `press_pallof_v03`
- L3 · Press Pallof con polea — material: `polea`; modalidad: `resistencia` · orientación recomendada · ID: `press_pallof_v04`
- L4 · Press Pallof de rodillas — material: `banda + colchoneta`; modalidad: `isometrico` · orientación recomendada · ID: `press_pallof_v05`
- L5 · Press Pallof en posición dividida — material: `polea`; modalidad: `equilibrio` · orientación recomendada · ID: `press_pallof_v06`

**Plancha lateral** — 6 variantes (`plancha_lateral`)

- L1 · Apoyo lateral contra pared — material: `pared`; modalidad: `asistido` · ID: `plancha_lateral_v01`
- L2 · Plancha lateral corta con rodillas — material: `colchoneta`; modalidad: `asistido` · ID: `plancha_lateral_v02`
- L3 · Plancha lateral de antebrazo — material: `colchoneta`; modalidad: `isometrico` · ID: `plancha_lateral_v03`
- L3 · Plancha lateral con elevación de cadera — material: `colchoneta`; modalidad: `dinamico` · ID: `plancha_lateral_v04`
- L4 · Plancha lateral con apoyo alterno — material: `colchoneta`; modalidad: `equilibrio` · orientación recomendada · ID: `plancha_lateral_v05`
- L5 · Plancha lateral con elevación de pierna — material: `colchoneta`; modalidad: `unilateral` · orientación recomendada · ID: `plancha_lateral_v06`

### Flexión de tronco (`core_flexion`)

Patrón motor: `flexion_tronco`.

**Encogimiento abdominal** — 6 variantes (`crunch`)

- L1 · Basculación pélvica tumbado — material: `colchoneta`; modalidad: `controlado` · ID: `crunch_v01`
- L2 · Crunch abdominal corto — material: `colchoneta`; modalidad: `controlado` · ID: `crunch_v02`
- L2 · Crunch abdominal con manos cruzadas — material: `colchoneta`; modalidad: `bilateral` · ID: `crunch_v03`
- L3 · Crunch abdominal con piernas apoyadas — material: `silla + colchoneta`; modalidad: `controlado` · ID: `crunch_v04`
- L4 · Crunch con balón de estabilidad — material: `pelota`; modalidad: `controlado` · orientación recomendada · ID: `crunch_v05`
- L5 · Crunch con polea de rodillas — material: `polea + colchoneta`; modalidad: `resistencia` · orientación recomendada · ID: `crunch_v06`

**Elevación de rodillas** — 6 variantes (`elevacion_rodillas`)

- L1 · Marcha de rodillas sentado — material: `silla`; modalidad: `alterno` · ID: `elevacion_rodillas_v01`
- L2 · Elevación alterna de rodillas tumbado — material: `colchoneta`; modalidad: `alterno` · ID: `elevacion_rodillas_v02`
- L2 · Elevación bilateral de rodillas tumbado — material: `colchoneta`; modalidad: `bilateral` · ID: `elevacion_rodillas_v03`
- L3 · Elevación de rodillas sentado en banco — material: `banco`; modalidad: `controlado` · ID: `elevacion_rodillas_v04`
- L4 · Elevación de rodillas en apoyo de antebrazos — material: `maquina`; modalidad: `controlado` · orientación recomendada · ID: `elevacion_rodillas_v05`
- L5 · Elevación de rodillas colgado con apoyo — material: `barra_dominadas`; modalidad: `controlado` · orientación recomendada · ID: `elevacion_rodillas_v06`

### Rotación de tronco (`core_rotacion`)

Patrón motor: `rotacion_tronco`.

**Rotación de tronco controlada** — 6 variantes (`rotacion_tronco`)

- L1 · Rotación suave de tronco sentado — material: `silla`; modalidad: `movilidad` · ID: `rotacion_tronco_v01`
- L1 · Rotación torácica de pie sin carga — material: `sin_equipo`; modalidad: `rotacion` · ID: `rotacion_tronco_v02`
- L2 · Rotación de tronco en colchoneta — material: `colchoneta`; modalidad: `controlado` · ID: `rotacion_tronco_v03`
- L3 · Rotación de tronco con banda ligera — material: `banda`; modalidad: `resistencia` · ID: `rotacion_tronco_v04`
- L4 · Rotación de tronco sentado con balón — material: `pelota`; modalidad: `rotacion` · orientación recomendada · ID: `rotacion_tronco_v05`
- L5 · Rotación de tronco en posición dividida con banda — material: `banda`; modalidad: `equilibrio` · orientación recomendada · ID: `rotacion_tronco_v06`

**Rotación con resistencia** — 5 variantes (`rotacion_polea`)

- L1 · Rotación corta con banda ligera — material: `banda`; modalidad: `controlado` · ID: `rotacion_polea_v01`
- L2 · Rotación horizontal con banda — material: `banda`; modalidad: `resistencia` · ID: `rotacion_polea_v02`
- L3 · Rotación horizontal en polea — material: `polea`; modalidad: `maquina` · orientación recomendada · ID: `rotacion_polea_v03`
- L4 · Rotación en polea con apoyo unilateral — material: `polea`; modalidad: `equilibrio` · orientación recomendada · ID: `rotacion_polea_v04`
- L5 · Rotación diagonal controlada en polea — material: `polea`; modalidad: `rotacion` · orientación recomendada · ID: `rotacion_polea_v05`

## Equilibrio y coordinación

### Equilibrio y control postural (`equilibrio`)

Patrón motor: `equilibrio`.

**Equilibrio sobre una pierna** — 6 variantes (`equilibrio_unipodal`)

- L1 · Equilibrio con ambos pies y apoyo — material: `silla`; modalidad: `asistido` · ID: `equilibrio_unipodal_v01`
- L1 · Equilibrio en apoyo tándem — material: `sin_equipo`; modalidad: `equilibrio` · ID: `equilibrio_unipodal_v02`
- L2 · Equilibrio unilateral con apoyo de mano — material: `silla`; modalidad: `asistido` · ID: `equilibrio_unipodal_v03`
- L3 · Equilibrio unilateral sin apoyo — material: `sin_equipo`; modalidad: `equilibrio` · ID: `equilibrio_unipodal_v04`
- L4 · Equilibrio unilateral con giro suave de cabeza — material: `sin_equipo`; modalidad: `equilibrio` · orientación recomendada · ID: `equilibrio_unipodal_v05`
- L5 · Equilibrio unilateral con alcance de brazos — material: `sin_equipo`; modalidad: `equilibrio` · orientación recomendada · ID: `equilibrio_unipodal_v06`

**Alcances con apoyo unilateral** — 5 variantes (`alcance_unipodal`)

- L1 · Alcance frontal con apoyo estable — material: `silla`; modalidad: `asistido` · ID: `alcance_unipodal_v01`
- L2 · Alcance lateral con apoyo estable — material: `silla`; modalidad: `asistido` · ID: `alcance_unipodal_v02`
- L3 · Alcance frontal en apoyo unilateral — material: `sin_equipo`; modalidad: `equilibrio` · ID: `alcance_unipodal_v03`
- L4 · Alcance lateral en apoyo unilateral — material: `sin_equipo`; modalidad: `equilibrio` · orientación recomendada · ID: `alcance_unipodal_v04`
- L5 · Alcance en tres direcciones con apoyo unilateral — material: `sin_equipo`; modalidad: `equilibrio` · orientación recomendada · ID: `alcance_unipodal_v05`

### Coordinación y desplazamientos (`coordinacion`)

Patrón motor: `coordinacion`.

**Escalera de coordinación** — 6 variantes (`escalera_agilidad`)

- L1 · Caminata entre marcas en suelo — material: `marcas_suelo`; modalidad: `coordinacion` · ID: `escalera_agilidad_v01`
- L2 · Paso simple adelante en escalera plana — material: `escalera_suelo`; modalidad: `coordinacion` · ID: `escalera_agilidad_v02`
- L2 · Paso lateral simple en escalera plana — material: `escalera_suelo`; modalidad: `lateral` · ID: `escalera_agilidad_v03`
- L3 · Secuencia alterna en escalera plana — material: `escalera_suelo`; modalidad: `coordinacion` · ID: `escalera_agilidad_v04`
- L4 · Secuencia de cambios de dirección guiados — material: `escalera_suelo`; modalidad: `coordinacion` · orientación recomendada · ID: `escalera_agilidad_v05`
- L5 · Secuencia combinada de coordinación guiada — material: `escalera_suelo`; modalidad: `coordinacion` · orientación recomendada · ID: `escalera_agilidad_v06`

**Pasos laterales** — 5 variantes (`desplazamiento_lateral`)

- L1 · Paso lateral con apoyo de pared — material: `pared`; modalidad: `asistido` · ID: `desplazamiento_lateral_v01`
- L1 · Paso lateral a ritmo cómodo — material: `sin_equipo`; modalidad: `lateral` · ID: `desplazamiento_lateral_v02`
- L2 · Pasos laterales entre marcas — material: `marcas_suelo`; modalidad: `coordinacion` · ID: `desplazamiento_lateral_v03`
- L3 · Desplazamiento lateral con cambios de dirección — material: `marcas_suelo`; modalidad: `coordinacion` · ID: `desplazamiento_lateral_v04`
- L4 · Desplazamiento lateral con patrones combinados — material: `marcas_suelo`; modalidad: `coordinacion` · orientación recomendada · ID: `desplazamiento_lateral_v05`

## Trabajo integrado

### Transportes de cargas (`transportes`)

Patrón motor: `transporte`.

**Paseo con cargas simétricas** — 5 variantes (`paseo_granjero`)

- L1 · Caminata sin carga con postura neutra — material: `sin_equipo`; modalidad: `controlado` · ID: `paseo_granjero_v01`
- L2 · Transporte de dos cargas ligeras con apoyo adecuado — material: `mancuerna`; modalidad: `carga_externa` · ID: `paseo_granjero_v02`
- L3 · Paseo del granjero con mancuernas — material: `mancuerna`; modalidad: `bilateral` · ID: `paseo_granjero_v03`
- L4 · Paseo del granjero con kettlebells — material: `kettlebell`; modalidad: `bilateral` · orientación recomendada · ID: `paseo_granjero_v04`
- L5 · Transporte bilateral con cambio controlado de dirección — material: `mancuerna`; modalidad: `controlado` · orientación recomendada · ID: `paseo_granjero_v05`

**Paseo con carga asimétrica** — 5 variantes (`paseo_maleta`)

- L1 · Sostén asimétrico con carga ligera — material: `mancuerna`; modalidad: `isometrico` · ID: `paseo_maleta_v01`
- L2 · Caminata corta con carga a un lado — material: `mancuerna`; modalidad: `unilateral` · ID: `paseo_maleta_v02`
- L3 · Paseo maleta con mancuerna — material: `mancuerna`; modalidad: `unilateral` · ID: `paseo_maleta_v03`
- L4 · Paseo maleta con kettlebell — material: `kettlebell`; modalidad: `unilateral` · orientación recomendada · ID: `paseo_maleta_v04`
- L5 · Paseo maleta con cambio de dirección controlado — material: `kettlebell`; modalidad: `equilibrio` · orientación recomendada · ID: `paseo_maleta_v05`

## Actividad cardiovascular

### Caminatas y marcha (`caminata`)

Patrón motor: `marcha`.

**Marcha y caminata** — 6 variantes (`marcha`)

- L1 · Marcha en el sitio con apoyo — material: `silla`; modalidad: `asistido` · ID: `marcha_v01`
- L1 · Caminata cómoda en terreno plano — material: `sin_equipo`; modalidad: `aerobico` · ID: `marcha_v02`
- L2 · Marcha en cinta con barandillas — material: `cinta`; modalidad: `asistido` · ID: `marcha_v03`
- L2 · Caminata exterior con trayecto sencillo — material: `sin_equipo`; modalidad: `aerobico` · ID: `marcha_v04`
- L3 · Marcha con cambios suaves de dirección — material: `sin_equipo`; modalidad: `coordinacion` · ID: `marcha_v05`
- L4 · Caminata con variaciones controladas de ritmo — material: `cinta`; modalidad: `aerobico` · orientación recomendada · ID: `marcha_v06`

**Caminata con inclinación** — 5 variantes (`caminata_inclinada`)

- L1 · Caminata plana en cinta — material: `cinta`; modalidad: `aerobico` · ID: `caminata_inclinada_v01`
- L2 · Caminata con inclinación baja — material: `cinta`; modalidad: `aerobico` · ID: `caminata_inclinada_v02`
- L2 · Caminata en pendiente suave al aire libre — material: `sin_equipo`; modalidad: `aerobico` · ID: `caminata_inclinada_v03`
- L3 · Caminata inclinada con cambios suaves de pendiente — material: `cinta`; modalidad: `aerobico` · ID: `caminata_inclinada_v04`
- L4 · Caminata en sendero de pendiente variable supervisada — material: `sin_equipo`; modalidad: `coordinacion` · orientación recomendada · ID: `caminata_inclinada_v05`

### Carrera y trote recreativo (`carrera`)

Patrón motor: `carrera`.

**Trote recreativo** — 5 variantes (`trote`)

- L1 · Marcha activa en pista plana — material: `sin_equipo`; modalidad: `aerobico` · ID: `trote_v01`
- L2 · Alternancia suave marcha y trote — material: `sin_equipo`; modalidad: `aerobico` · ID: `trote_v02`
- L3 · Trote cómodo en terreno plano — material: `sin_equipo`; modalidad: `aerobico` · ID: `trote_v03`
- L3 · Trote con cambios suaves de dirección — material: `sin_equipo`; modalidad: `coordinacion` · ID: `trote_v04`
- L4 · Trote en circuito técnico supervisado — material: `sin_equipo`; modalidad: `coordinacion` · orientación recomendada · ID: `trote_v05`

**Alternancia de marcha y carrera** — 5 variantes (`carrera_intervalos`)

- L1 · Caminata con pausas libres — material: `sin_equipo`; modalidad: `aerobico` · ID: `carrera_intervalos_v01`
- L2 · Alternancia de caminata y marcha activa — material: `sin_equipo`; modalidad: `aerobico` · ID: `carrera_intervalos_v02`
- L2 · Alternancia de caminata y trote fácil — material: `sin_equipo`; modalidad: `aerobico` · ID: `carrera_intervalos_v03`
- L3 · Alternancia de trote y caminata en circuito — material: `sin_equipo`; modalidad: `aerobico` · ID: `carrera_intervalos_v04`
- L4 · Cambios moderados de ritmo en circuito guiado — material: `sin_equipo`; modalidad: `coordinacion` · orientación recomendada · ID: `carrera_intervalos_v05`

### Ciclismo (`bicicleta`)

Patrón motor: `ciclismo`.

**Bicicleta estática** — 5 variantes (`ciclo_estatico`)

- L1 · Pedaleo sentado con bicicleta de respaldo — material: `bicicleta_recumbente`; modalidad: `asistido` · ID: `ciclo_estatico_v01`
- L1 · Pedaleo cómodo en bicicleta estática — material: `bicicleta_estatica`; modalidad: `aerobico` · ID: `ciclo_estatico_v02`
- L2 · Bicicleta estática con cambios suaves de cadencia — material: `bicicleta_estatica`; modalidad: `aerobico` · ID: `ciclo_estatico_v03`
- L3 · Bicicleta estática con resistencia moderada controlada — material: `bicicleta_estatica`; modalidad: `aerobico` · ID: `ciclo_estatico_v04`
- L4 · Bicicleta de ciclismo indoor con cambios guiados — material: `bicicleta_estatica`; modalidad: `coordinacion` · orientación recomendada · ID: `ciclo_estatico_v05`

**Bicicleta exterior** — 5 variantes (`ciclo_exterior`)

- L1 · Pedaleo en entorno cerrado y seguro — material: `bicicleta`; modalidad: `asistido` · ID: `ciclo_exterior_v01`
- L2 · Ciclismo recreativo en terreno plano — material: `bicicleta`; modalidad: `aerobico` · ID: `ciclo_exterior_v02`
- L3 · Ciclismo recreativo con curvas amplias — material: `bicicleta`; modalidad: `coordinacion` · ID: `ciclo_exterior_v03`
- L4 · Ciclismo en trayecto con cambios suaves de terreno — material: `bicicleta`; modalidad: `coordinacion` · orientación recomendada · ID: `ciclo_exterior_v04`
- L5 · Ciclismo en circuito técnico guiado — material: `bicicleta`; modalidad: `coordinacion` · orientación recomendada · ID: `ciclo_exterior_v05`

### Remo cardiovascular (`remo_cardio`)

Patrón motor: `remo_cardio`.

**Remo en ergómetro** — 5 variantes (`remo_ergometro`)

- L1 · Técnica segmentada de remo sin resistencia — material: `remo_ergometro`; modalidad: `controlado` · ID: `remo_ergometro_v01`
- L2 · Remo ergómetro de ritmo suave — material: `remo_ergometro`; modalidad: `aerobico` · ID: `remo_ergometro_v02`
- L3 · Remo ergómetro con cadencia estable — material: `remo_ergometro`; modalidad: `aerobico` · ID: `remo_ergometro_v03`
- L4 · Remo ergómetro con cambios de cadencia guiados — material: `remo_ergometro`; modalidad: `coordinacion` · orientación recomendada · ID: `remo_ergometro_v04`
- L5 · Remo ergómetro con patrón técnico variado guiado — material: `remo_ergometro`; modalidad: `coordinacion` · orientación recomendada · ID: `remo_ergometro_v05`

### Elíptica cardiovascular (`eliptica`)

Patrón motor: `eliptica`.

**Trabajo en elíptica** — 5 variantes (`eliptica_cardio`)

- L1 · Elíptica con apoyo de manillares fijos — material: `eliptica`; modalidad: `asistido` · ID: `eliptica_cardio_v01`
- L2 · Elíptica con paso uniforme — material: `eliptica`; modalidad: `aerobico` · ID: `eliptica_cardio_v02`
- L3 · Elíptica con movimiento coordinado de brazos — material: `eliptica`; modalidad: `coordinacion` · ID: `eliptica_cardio_v03`
- L3 · Elíptica con cambios suaves de resistencia — material: `eliptica`; modalidad: `aerobico` · ID: `eliptica_cardio_v04`
- L4 · Elíptica con cambios guiados de cadencia — material: `eliptica`; modalidad: `coordinacion` · orientación recomendada · ID: `eliptica_cardio_v05`

### Escalones aeróbicos (`escalon_aerobico`)

Patrón motor: `escalon`.

**Paso aeróbico sobre escalón** — 5 variantes (`step_cardio`)

- L1 · Paso en suelo sin elevación — material: `sin_equipo`; modalidad: `aerobico` · ID: `step_cardio_v01`
- L2 · Paso alterno en escalón bajo — material: `escalon`; modalidad: `alterno` · ID: `step_cardio_v02`
- L2 · Paso lateral en escalón bajo — material: `escalon`; modalidad: `lateral` · ID: `step_cardio_v03`
- L3 · Secuencia de pasos básica en escalón — material: `escalon`; modalidad: `coordinacion` · ID: `step_cardio_v04`
- L4 · Secuencia de pasos combinada supervisada — material: `escalon`; modalidad: `coordinacion` · orientación recomendada · ID: `step_cardio_v05`

## Movilidad, respiración y postura

### Movilidad de cadera (`movilidad_cadera`)

Patrón motor: `movilidad_cadera`.

**Rotaciones de cadera** — 6 variantes (`rotacion_cadera`)

- L1 · Rotación de cadera sentado con apoyo — material: `silla`; modalidad: `movilidad` · ID: `rotacion_cadera_v01`
- L1 · Rotación externa de cadera sentado — material: `silla`; modalidad: `movilidad` · ID: `rotacion_cadera_v02`
- L2 · Rotación interna de cadera sentado — material: `silla`; modalidad: `movilidad` · ID: `rotacion_cadera_v03`
- L3 · Transición 90/90 de cadera con apoyo — material: `colchoneta`; modalidad: `movilidad` · ID: `rotacion_cadera_v04`
- L4 · Transición 90/90 sin apoyo de manos — material: `colchoneta`; modalidad: `controlado` · orientación recomendada · ID: `rotacion_cadera_v05`
- L5 · Rotación de cadera en apoyo unilateral guiada — material: `silla`; modalidad: `equilibrio` · orientación recomendada · ID: `rotacion_cadera_v06`

**Movilidad del flexor de cadera** — 5 variantes (`flexor_cadera`)

- L1 · Movilidad suave de cadera tumbado — material: `colchoneta`; modalidad: `movilidad` · ID: `flexor_cadera_v01`
- L1 · Flexión de cadera sentado — material: `silla`; modalidad: `movilidad` · ID: `flexor_cadera_v02`
- L2 · Movilidad de flexor de cadera de pie con apoyo — material: `silla`; modalidad: `asistido` · ID: `flexor_cadera_v03`
- L3 · Movilidad de flexor en posición dividida — material: `colchoneta`; modalidad: `movilidad` · ID: `flexor_cadera_v04`
- L4 · Movilidad de flexor con elevación controlada — material: `colchoneta`; modalidad: `controlado` · orientación recomendada · ID: `flexor_cadera_v05`

### Movilidad de columna (`movilidad_columna`)

Patrón motor: `movilidad_columna`.

**Gato y vaca** — 5 variantes (`gato_vaca`)

- L1 · Movilidad vertebral sentado — material: `silla`; modalidad: `movilidad` · ID: `gato_vaca_v01`
- L1 · Gato y vaca sentado — material: `silla`; modalidad: `movilidad` · ID: `gato_vaca_v02`
- L2 · Gato y vaca en cuadrupedia — material: `colchoneta`; modalidad: `movilidad` · ID: `gato_vaca_v03`
- L3 · Gato y vaca segmentado — material: `colchoneta`; modalidad: `controlado` · ID: `gato_vaca_v04`
- L4 · Movilidad segmentaria de columna guiada — material: `colchoneta`; modalidad: `coordinacion` · orientación recomendada · ID: `gato_vaca_v05`

**Rotación torácica** — 6 variantes (`rotacion_toracica`)

- L1 · Rotación torácica sentado con apoyo — material: `silla`; modalidad: `movilidad` · ID: `rotacion_toracica_v01`
- L2 · Rotación torácica de pie — material: `sin_equipo`; modalidad: `movilidad` · ID: `rotacion_toracica_v02`
- L2 · Rotación torácica tumbado de lado — material: `colchoneta`; modalidad: `movilidad` · ID: `rotacion_toracica_v03`
- L3 · Rotación torácica en cuadrupedia — material: `colchoneta`; modalidad: `movilidad` · ID: `rotacion_toracica_v04`
- L4 · Rotación torácica con banda ligera — material: `banda`; modalidad: `resistencia` · orientación recomendada · ID: `rotacion_toracica_v05`
- L5 · Rotación torácica con control de equilibrio — material: `colchoneta`; modalidad: `coordinacion` · orientación recomendada · ID: `rotacion_toracica_v06`

### Movilidad de hombro (`movilidad_hombro`)

Patrón motor: `movilidad_hombro`.

**Círculos de brazos** — 5 variantes (`circulos_brazo`)

- L1 · Círculos pequeños de brazos sentado — material: `silla`; modalidad: `movilidad` · ID: `circulos_brazo_v01`
- L1 · Círculos de brazos de pie — material: `sin_equipo`; modalidad: `movilidad` · ID: `circulos_brazo_v02`
- L2 · Círculos alternos de brazos — material: `sin_equipo`; modalidad: `alterno` · ID: `circulos_brazo_v03`
- L3 · Círculos de brazos con cambio de dirección — material: `sin_equipo`; modalidad: `coordinacion` · ID: `circulos_brazo_v04`
- L4 · Círculos combinados de brazos y escápulas — material: `sin_equipo`; modalidad: `coordinacion` · orientación recomendada · ID: `circulos_brazo_v05`

**Deslizamientos en pared** — 5 variantes (`deslizamiento_pared`)

- L1 · Deslizamiento corto de manos en pared — material: `pared`; modalidad: `asistido` · ID: `deslizamiento_pared_v01`
- L2 · Deslizamiento de brazos contra pared — material: `pared`; modalidad: `movilidad` · ID: `deslizamiento_pared_v02`
- L2 · Deslizamiento en pared con pausa — material: `pared`; modalidad: `controlado` · ID: `deslizamiento_pared_v03`
- L3 · Deslizamiento de antebrazos con banda ligera — material: `pared + banda`; modalidad: `resistencia` · ID: `deslizamiento_pared_v04`
- L4 · Deslizamiento de brazos con control escapular — material: `pared`; modalidad: `controlado` · orientación recomendada · ID: `deslizamiento_pared_v05`

### Movilidad de tobillo (`movilidad_tobillo`)

Patrón motor: `movilidad_tobillo`.

**Movilidad de dorsiflexión** — 5 variantes (`dorsiflexion_tobillo`)

- L1 · Dorsiflexión sentado sin carga — material: `silla`; modalidad: `movilidad` · ID: `dorsiflexion_tobillo_v01`
- L2 · Dorsiflexión en pared con apoyo — material: `pared`; modalidad: `asistido` · ID: `dorsiflexion_tobillo_v02`
- L2 · Dorsiflexión de tobillo de pie con apoyo — material: `silla`; modalidad: `movilidad` · ID: `dorsiflexion_tobillo_v03`
- L3 · Movilidad de dorsiflexión en posición dividida — material: `sin_equipo`; modalidad: `movilidad` · ID: `dorsiflexion_tobillo_v04`
- L4 · Dorsiflexión controlada con banda — material: `banda`; modalidad: `resistencia` · orientación recomendada · ID: `dorsiflexion_tobillo_v05`

**Círculos de tobillo** — 5 variantes (`circulos_tobillo`)

- L1 · Círculos de tobillo sentado — material: `silla`; modalidad: `movilidad` · ID: `circulos_tobillo_v01`
- L1 · Círculos de tobillo con pierna apoyada — material: `silla`; modalidad: `movilidad` · ID: `circulos_tobillo_v02`
- L2 · Alfabeto de tobillo sentado — material: `silla`; modalidad: `coordinacion` · ID: `circulos_tobillo_v03`
- L3 · Círculos alternos de tobillo — material: `silla`; modalidad: `alterno` · ID: `circulos_tobillo_v04`
- L4 · Movilidad de tobillo en equilibrio con apoyo — material: `silla`; modalidad: `equilibrio` · orientación recomendada · ID: `circulos_tobillo_v05`

### Flexibilidad de piernas (`flexibilidad_piernas`)

Patrón motor: `flexibilidad`.

**Estiramiento de isquiotibiales** — 5 variantes (`estiramiento_isquios`)

- L1 · Movilidad de pierna tumbado con rodilla flexionada — material: `colchoneta`; modalidad: `movilidad` · ID: `estiramiento_isquios_v01`
- L1 · Estiramiento suave de isquiotibiales sentado — material: `silla`; modalidad: `movilidad` · ID: `estiramiento_isquios_v02`
- L2 · Estiramiento de isquiotibiales con toalla — material: `toalla + colchoneta`; modalidad: `asistido` · ID: `estiramiento_isquios_v03`
- L3 · Movilidad de isquiotibiales de pie con apoyo — material: `silla`; modalidad: `movilidad` · ID: `estiramiento_isquios_v04`
- L4 · Movilidad dinámica de isquiotibiales guiada — material: `sin_equipo`; modalidad: `controlado` · orientación recomendada · ID: `estiramiento_isquios_v05`

**Estiramiento de cuádriceps** — 5 variantes (`estiramiento_cuadriceps`)

- L1 · Movilidad de rodilla sentado — material: `silla`; modalidad: `movilidad` · ID: `estiramiento_cuadriceps_v01`
- L2 · Estiramiento de cuádriceps de lado — material: `colchoneta`; modalidad: `movilidad` · ID: `estiramiento_cuadriceps_v02`
- L2 · Estiramiento de cuádriceps de pie con apoyo — material: `silla`; modalidad: `asistido` · ID: `estiramiento_cuadriceps_v03`
- L3 · Estiramiento de cuádriceps en banco con apoyo — material: `banco`; modalidad: `movilidad` · ID: `estiramiento_cuadriceps_v04`
- L4 · Estiramiento unilateral de cuádriceps con equilibrio — material: `sin_equipo`; modalidad: `equilibrio` · orientación recomendada · ID: `estiramiento_cuadriceps_v05`

### Respiración y postura (`respiracion_postura`)

Patrón motor: `control_postural`.

**Respiración diafragmática** — 5 variantes (`respiracion_diafragmatica`)

- L1 · Respiración tranquila sentado — material: `silla`; modalidad: `controlado` · ID: `respiracion_diafragmatica_v01`
- L1 · Respiración diafragmática tumbado — material: `colchoneta`; modalidad: `controlado` · ID: `respiracion_diafragmatica_v02`
- L2 · Respiración nasal con atención al ritmo — material: `sin_equipo`; modalidad: `controlado` · ID: `respiracion_diafragmatica_v03`
- L3 · Respiración costal lateral guiada — material: `colchoneta`; modalidad: `controlado` · ID: `respiracion_diafragmatica_v04`
- L4 · Coordinación respiratoria con movilidad suave — material: `colchoneta`; modalidad: `coordinacion` · orientación recomendada · ID: `respiracion_diafragmatica_v05`

**Control postural** — 5 variantes (`control_postura`)

- L1 · Conciencia de postura sentado — material: `silla`; modalidad: `controlado` · ID: `control_postura_v01`
- L1 · Alineación neutra de pie con pared — material: `pared`; modalidad: `controlado` · ID: `control_postura_v02`
- L2 · Control postural con cambios de apoyo — material: `sin_equipo`; modalidad: `equilibrio` · ID: `control_postura_v03`
- L3 · Control postural durante marcha suave — material: `sin_equipo`; modalidad: `coordinacion` · ID: `control_postura_v04`
- L4 · Coordinación postural con carga liviana supervisada — material: `mancuerna`; modalidad: `coordinacion` · orientación recomendada · ID: `control_postura_v05`

## Condiciones de integración

El inventario es **de solo lectura** y no modifica automáticamente las tablas SQLite v5, planes ni resultados de seguimiento. El adaptador Android `AssetExerciseCatalogRepository` lee los TSV empaquetados; `AsterionDomainEngine` permite consultar familias, tipos y variantes después de inyectar el catálogo. Para habilitar selección de estas variantes desde la interfaz y guardar planes con sus IDs, se necesita una siguiente etapa de integración con el catálogo existente y sus claves foráneas.

Los ejercicios más complejos figuran únicamente como referencia taxonómica y no deben asignarse automáticamente a menores ni a ningún usuario sin valoración adecuada.

Fuentes de referencia metodológica: [ACE](https://www.acefitness.org/resources/everyone/exercise-library/), [NASM](https://www.nasm.org/workout-exercise-guidance/), [AOSSM](https://www.sportsmed.org/youth-strength-training), [CDC](https://www.cdc.gov/physical-activity-basics/adding-children-adolescents/what-counts.html). Datos y etiquetas escritos específicamente para Asterion, no extraídos automáticamente ni copiados de esos sitios.
