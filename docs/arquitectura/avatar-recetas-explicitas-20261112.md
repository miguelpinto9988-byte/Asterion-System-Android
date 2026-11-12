# Recetas corporales explícitas por variante

## Alcance y estado real

Esta ampliación incorpora **111 recetas corporales explícitas** (51 iniciales y dos lotes incrementales de 30) al avatar Canvas existente:
**138 IDs registrados de 1.257 (10,98 %)**, **1.119 pendientes**. No completa la
solicitud de todas las demostraciones. El porcentaje cuenta implementaciones
registradas; no es una certificación clínica ni una aprobación visual independiente.
Los mapas de comandos de los IDs pendientes siguen identificados como tales.

El inventario completo se obtiene con:

```sh
python3 tests/avatar_completion_progress.py --format json --pending-csv build/avatar-completion/pendientes.csv
```

## Implementación

`AvatarVariantScene` declara cada ID, nombre, postura, amplitud, alternancia,
pausa, implemento, nivel y supervisión. No existe asociación por similitud del
nombre, categoría o nivel. Un ID sin receta no recibe un movimiento genérico.

- Flexiones de codo con banda, mancuernas y barra: postura de pie/sentada,
  simultánea/alterna y cargas visibles.
- Elevaciones frontales y alcances sentados con amplitudes diferenciadas.
- Remo sentado/de pie con anclaje explícito de banda y variantes sin resistencia.
- Extensión de rodilla, marcha sentada y deslizamientos con talones en suelo,
  incluidos ciclos alternos y toalla.
- Bisagra de cadera, sentadillas y zancadas con cargas y pausa según el ID.

El render reutiliza `AvatarRenderer.drawFigure`: mismo avatar, prendas, detalles,
colores y articulaciones. Silla, suelo, toalla, mancuernas, barra y bandas se
colocan respecto a los contactos corporales. Las longitudes no se interpolan;
se usan rotaciones de segmentos e IK con rechazo de contactos inalcanzables.

La regla anterior que prohibía cualquier ilustración avanzada se conserva para
los clips históricos. Una receta nueva admite el nivel/supervisión concreto del
catálogo únicamente si coincide toda su firma. La recomendación de supervisión
permanece visible. No se cambia la progresión, prescripción, catálogo de ejercicios
ni historial del usuario.

El panel de fases de estas escenas calcula cambios angulares desde la geometría
real de la receta; añade instantes de ambos semiciclos para que las alternancias
no parezcan inmóviles al consultar solamente el 50 %.

## Lote incremental 01 (30 variantes nuevas)
- Curl martillo con banda / mancuernas (3): agarre vertical del implemento.
- Bisagra con banda en hombros y peso muerto con mancuerna, kettlebell o barra (4).
- Extensión terminal con banda y flexión de rodilla de pie con pared / silla (3).
- Alcance, deslizamiento y apoyo de manos con contacto fijo en pared (6).
- Marcha con tronco estable, alternancia de brazos, elevación de rodillas,
  toques de talón y pasos hacia atrás con ciclo de dos lados (12).
- Retracción de brazos sin implemento y remo con banda anclada (2).

En todos los casos el identificador y material deben coincidir con el TSV.
Esta es cobertura **implementada / registrada**, no validación de técnica.
Las escenas necesitan capturas y comprobación individual en Android antes de
cerrar su seguimiento. Los recursos y el rig oficial se conservan.

## Lote 02 — treinta recetas adicionales
- Elevación de talones y puntas: cuatro escenas con controles de contacto.
- Movilidad de tobillo y rodilla: cinco escenas de apoyo fijo.
- Pasos sobre suelo y escalón: tres escenas con recorrido y apoyo.
- Toques de talón y marcha asistida: dos escenas con pared o silla.
- Marcha en sitio con y sin balanceo: cinco escenas.
- Palmada y paso alterno: dos escenas, manos coinciden en la fase de palmada.
- Press de hombros: tres escenas con banda, silla o mancuernas.
- Empujes de pecho/brazos: cinco escenas de resistencia o sin carga.
- Remo con mancuernas: una escena con inclinación del tronco.

Los movimientos se implementan como dibujos Canvas 2D; el registro de
cobertura no certifica técnica, biomécanica ni calidad visual. Antes de
aprobar cada escena deben revisarse las capturas nativas en las cinco fases
y las dos paletas y verificarse que el implemento corresponde al catálogo.

## Verificación reproducible

- `AvatarVariantSceneTest`: 601 fotogramas por receta, longitudes rígidas,
  finitud, continuidad, cierre del ciclo, contacto de pies/silla, alternancia,
  diferencias de amplitud, pausas y entradas inválidas.
- `AvatarVariantCatalogTest`: carga de los 1.257 IDs; rechaza cambios de comando,
  cámara, implemento, ejecución, nivel o supervisión y registros no autorizados.
- `avatar_variant_scene_contract.py`: firma exacta respecto al catálogo,
  implementos presentes y cobertura de archivos exportados.
- `AvatarVariantSceneInstrumentedTest`: Android dibuja cada receta en ambos
  colores y cinco fases (0, 25, 50, 75 y 100 %): **1.110 PNG esperados**.
  Los archivos son evidencia para revisar, no una aprobación automática del dibujo.
- Los contratos y pruebas anteriores se conservan. El artefacto CI
  `asterion-runtime-diagnostics/asterion-avatar-variant-scenes` contiene la
  previsualización nativa; `asterion-avatar-progreso` contiene el inventario restante.

Consultar los resultados del commit exacto antes de afirmar que la compilación,
Android Lint o las pruebas instrumentadas han pasado. No fusionar el borrador
por el solo hecho de tener perfiles para los 1.257 ejercicios.

## Referencias de implementación

Documentación Android consultada mediante Context7:
https://developer.android.com/develop/ui/views/layout/custom-views/optimizing-view
https://developer.android.com/develop/ui/views/layout/custom-views/custom-drawing

Fuentes de técnica localizadas mediante Nimble para contrastar postura, apoyo
e implementos (no equivalen a validar individualmente las 51 recetas):
https://www.acefitness.org/resources/everyone/exercise-library/
https://www.nasm.org/resource-center/exercise-library
https://www.performancehealth.com/thera-band-instr-manual

## Pendiente

Faltan 1.119 escenas, incluidas posturas de suelo, transiciones, movimientos
tridimensionales, manos/pies, máquinas y gestos deportivos. Cada una requiere
implementación y revisión de su ejecución concreta. No se habilitan por renombrar
un mapa articular ni por asignarles un clip de otra variante.
