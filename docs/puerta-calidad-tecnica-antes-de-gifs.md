# Puerta de calidad antes de animaciones propias — Asterion v2

## Estado real
Inventario: app/src/main/assets/unified_exercises/v2/catalogo_es.json

- 2.104 ejercicios: 1.257 históricos y 847 nuevos.
- 847 nuevas fichas tienen orientación general en español, no validada por un especialista.
- 842 nuevas conservan pasos individualizados en inglés de Free Exercise DB (Unlicense).
- 5 nuevas no tienen instrucciones originales: Iron Cross, One-Arm Kettlebell Swings, Push Press, Side Bridge y Side Jackknife.
- Las 1.257 originales no contienen en el visor unificado una secuencia individual de ejecución.
- Ninguna aprobación profesional individual verificable figura en el registro de esta rama.
- Se conservan los datos del motor de rutinas y los historiales de volumen anteriores.

## Archivos de revisión por ejercicio
- docs/datos/estado_validacion_tecnica_v2.csv: una fila por ejercicio, con procedencia, pasos, estado, siguiente acción y fuente individual.
- docs/datos/resumen_validacion_tecnica_v2.json: estadísticas de completitud y bloqueo de publicación.
- docs/datos/revisiones_tecnicas_individuales.csv: registro vacío para revisores, sin firmas inventadas.
- docs/datos/preparacion_animaciones_originales_v2.csv: cola de GIF propios, identificadores y estado de autorización.
- tools/audit_technical_readiness.py: generador y verificador de las auditorías.
- tools/test_technical_readiness_audit.py: pruebas de cobertura de 2.104 IDs y protección ante validaciones ficticias.

## Lista de verificación técnica por ID
1. Identidad del movimiento: variante, postura, modalidad, agarre, apoyos y equipo precisos, sin equivalencia forzada.
2. Fuente: registrar el enlace exacto, la licencia y el texto técnico en español; no copiar instrucciones de terceros sin permiso.
3. Ejecución: postura inicial, fases, trayectoria, articulaciones, control del retorno, condiciones de finalización.
4. Seguridad: requisitos de supervisión, riesgos generales del movimiento, errores comunes y adaptaciones posibles sin prescribir cargas máximas ni progresiones obligatorias.
5. Volumen: aclarar si corresponde registrar repeticiones o segundos; verificar categoría, nivel técnico y elegibilidad individual.
6. Revisión: persona competente que aprueba, fecha, fuente revisada y comprobación independiente de sus credenciales. El software no puede acreditar esa competencia por sí solo.
7. Storyboard: diseñar animaciones originales por identificador, sin reutilizar imágenes o GIF protegidos, y contrastar cada postura con la ficha aprobada.
8. Publicación: habilitar demostraciones solo con aprobación de técnica y calidad visual documentada.

## Estado de las puertas
A. Inventario y deduplicación: terminado según la auditoría registrada. Los 22 casos dudosos se resolvieron documentalmente.
B. Controles automáticos de completitud: terminados. Cada registro tiene estado, fuente y acción pendiente.
C. Validación técnica profesional individual: PENDIENTE para las fichas sin revisión documentada. La ausencia de una firma no equivale a afirmar que el ejercicio sea erróneo.
D. Preparación interna de GIF: se pueden elaborar bocetos de referencia para las fichas con fuente individual, con etiqueta de no validado y sin publicación.
E. Publicación de GIF instructivos: BLOQUEADA hasta aprobación técnica y revisión de las animaciones.

## Casos prioritarios
Los cinco nombres sin instrucciones en la fuente requieren contrastar una referencia independiente antes de describir su técnica. Para movimientos técnicos complejos se necesita supervisión cualificada; la documentación no debe convertirse en una guía autónoma para levantar cargas o ejecutar desafíos riesgosos.

## Referencias públicas
- Fuente técnica reutilizable de dominio público: https://github.com/yuhonas/free-exercise-db
- Ejercicios y pautas generales (consultar hechos, no copiar texto protegido): https://www.acefitness.org/resources/everyone/exercise-library/
- Referencia biomecánica (contrastar hechos, no copiar descripciones): https://exrx.net/

La auditoría automática prueba integridad de datos, no idoneidad médica, seguridad individual ni validez biomecánica profesional. No declarar una ficha revisada solo por estar completa.


## Cinco expedientes que necesitan investigación de ejecución adicional
Las fichas del dataset de dominio público carecen de pasos originales. Estas
referencias contrastan hechos y categorías, no constituyen aprobación técnica.

| ID de fuente | Identidad o problema | Referencia de contraste | Publicación |
|---|---|---|---|
| Iron_Cross | Nombre ambiguo entre gesto con mancuernas y figura gimnástica distinta; confirmar variante por equipamiento | https://www.nasm.org/resource-center/exercise-library/iron-cross | Bloqueada |
| One-Arm_Kettlebell_Swings | Movimiento dinámico unilateral; postura, trayectoria y control del equipo requieren verificación especializada | https://exrx.net/Lists/KettlebellExercises | Bloqueada |
| Push_Press | Levantamiento técnico de barra por encima de la cabeza clasificado expert en la fuente | https://exrx.net/Lists/OlympicWeightlifting | Bloqueada |
| Side_Bridge | Posición estática lateral; la unidad de volumen debe ser tiempo y la postura debe contrastarse | https://www.acefitness.org/resources/everyone/exercise-library/303/side-plank/ | Bloqueada |
| Side_Jackknife | Variante exacta de flexión lateral no identificada por pasos; no sustituirla por abdominal genérico | https://exrx.net/Lists/Directory | Bloqueada |

Todo dato de una página con derechos reservados se utiliza solo para
contrastar hechos sobre el movimiento; nunca se reproducen imágenes ni
instrucciones protegidas. Ninguna de las cinco se recomienda automáticamente
ni está lista para crear un GIF instructivo publicable.
