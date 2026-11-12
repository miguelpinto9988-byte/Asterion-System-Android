# Recetas corporales explícitas por variante

## Alcance y estado real

Esta ampliación incorpora **51 escenas nuevas** al avatar Canvas existente:
**78 IDs registrados de 1.257 (6,21 %)**, **1.179 pendientes**. No completa la
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

## Verificación reproducible

- `AvatarVariantSceneTest`: 601 fotogramas por receta, longitudes rígidas,
  finitud, continuidad, cierre del ciclo, contacto de pies/silla, alternancia,
  diferencias de amplitud, pausas y entradas inválidas.
- `AvatarVariantCatalogTest`: carga de los 1.257 IDs; rechaza cambios de comando,
  cámara, implemento, ejecución, nivel o supervisión y registros no autorizados.
- `avatar_variant_scene_contract.py`: firma exacta respecto al catálogo,
  implementos presentes y cobertura de archivos exportados.
- `AvatarVariantSceneInstrumentedTest`: Android dibuja cada receta en ambos
  colores y cinco fases (0, 25, 50, 75 y 100 %): **510 PNG esperados**.
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

Faltan 1.179 escenas, incluidas posturas de suelo, transiciones, movimientos
tridimensionales, manos/pies, máquinas y gestos deportivos. Cada una requiere
implementación y revisión de su ejecución concreta. No se habilitan por renombrar
un mapa articular ni por asignarles un clip de otra variante.
