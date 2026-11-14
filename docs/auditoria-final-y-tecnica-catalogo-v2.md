# Auditoría final de duplicados e información técnica · Asterion v2

Fecha: 14 de noviembre de 2026. Rama de revisión:
feature/unificar-catalogos-y-retirar-avatar-20261113.

## Alcance y procedencia
- Fuente nueva independiente: https://github.com/yuhonas/free-exercise-db
- Licencia de los datos de referencia: Unlicense (dominio público).
- Revisión: f00c92c7dcf1216a928a52c3706c7ce8e2f71ed5
- Archivo original: dist/exercises.json, 876 ejercicios.
- Catálogo de Asterion previo: 1.257 ejercicios, mantenidos íntegramente.
- No se han utilizado instrucciones, imágenes o GIF de Gym visual ni del
  anterior repositorio exercises-dataset. Los medios de terceros no están
  empaquetados y no se han creado animaciones nuevas.
- Informes de trazabilidad:
  docs/datos/auditoria_dominio_publico.csv (876 entradas),
  docs/datos/revision_22_casos_cerrados.csv (22 entradas).

## Resultado del cierre
| Estado final | Registros |
|---|---:|
| Asterion histórico, sin cambios | 1.257 |
| Variantes nuevas sin duplicado confirmado | 847 |
| Total inventario v2 | 2.104 |
| Equivalencias descartadas por ser duplicados | 29 |
| De las 22 pendientes: duplicados confirmados | 8 |
| De las 22 pendientes: variantes distintas incorporadas | 14 |
| Posibles duplicados automáticos sin decisión | 0 |

Se adoptaron criterios de postura, línea de tracción, equipamiento, ángulo
articular y nombre de la variante. «Duplicado» significa equivalencia
suficiente del gesto técnico para conservar un único registro histórico;
no significa que la ejecución de un ejercicio esté clínicamente validada.
Los 14 distintos se incluyen en modo consulta, no como sugerencias.

## Ocho equivalencias del lote conservando IDs de Asterion
| Fuente de dominio público | Asterion conservado | Razón |
|---|---|---|
| Cable Crunch | crunch_v06 | Crunch arrodillado en polea alta con cuerda |
| Dead Bug | dead_bug_v03 | Extensión alterna de una pierna sin cambiar apoyo |
| Dumbbell Shoulder Press | press_hombros_v02 | Press sentado con mancuernas |
| Rope Crunch | crunch_v06 | Mismo crunch de rodillas en polea con cuerda |
| Rowing Stationary | remo_ergometro_v02 | Remo cíclico en ergómetro |
| Seated Dumbbell Press | press_hombros_v02 | Press sentado, no Press Arnold |
| Seated Side Lateral Raise | elevacion_lateral_v04 | Elevación lateral sentada con mancuernas |
| Standing Calf Raises | dep_029_elevacion_de_talones_en_maquina_calf_r_v01 | Elevación de talones en máquina |

## Catorce variantes diferenciadas
- Barbell Lunge: paso adelante; no zancada estática
- Bent-Arm Dumbbell Pullover: codos flexionados frente a pullover genérico
- Calf Press: prensa de gemelos sentado
- Calf Press On The Leg Press Machine: ejecución en prensa de piernas
- Dumbbell Lunges: paso y retorno; no posición dividida estática
- Kneeling Cable Crunch With Alternating Oblique Twists: rotación oblicua alterna
- Leverage High Row: remada con trayectoria alta
- Leverage Iso Row: remada con asas independientes
- Low Cable Triceps Extension: extensión supina con polea baja
- Seated Bent-Over Two-Arm Dumbbell Triceps Extension: tronco inclinado
- Seated Dumbbell Inner Biceps Curl: trayectoria y rotación particulares
- Standing Dumbbell Triceps Extension: extensión de pie con agarre específico
- Stiff-Legged Dumbbell Deadlift: bisagra con rodilla casi fija
- Straight-Arm Dumbbell Pullover: brazos extendidos y técnica diferenciada

La auditoría guarda el ejercicio histórico comparado, el equipo de
ambas fichas y la URL al JSON público de origen. No se conserva una
copia adicional para los 29 equivalentes.

## Cobertura técnica de TODOS los ejercicios nuevos
Cada una de las 847 fichas nuevas incluye:
1. Nombre descriptivo español, grupo muscular, equipamiento y categoría.
2. Etiquetas de patrón de fuerza y mecánica, si están en la fuente.
3. Pautas de movimiento generales en español, estructuradas como pasos,
   con un estado explícito: orientacion_editorial_no_validada.
4. Nombre e ID del registro original, URL y licencia Unlicense; 842 fichas
   incluyen además pasos completos de la fuente en inglés para revisión.
5. Identificador reservado para su futura animación GIF propia.
6. Bloqueo de selección automática (nivel técnico = 0 pendiente de revisión).

Cinco registros de la fuente NO tienen instrucciones originales en inglés:
Iron Cross, One-Arm Kettlebell Swings, Push Press, Side Bridge y
Side Jackknife. Se preservan con orientación general en español,
sin atribuirles detalles técnicos no documentados ni permitir su
recomendación automática.

**Limitación editorial:** las pautas españolas son borradores orientativos
generados a partir de categorías y patrones; no son traducciones técnicas
individuales validadas de los pasos ingleses. No hay constancia de revisión
humana de todas las 847 fichas y no debe afirmarse lo contrario. En los
ejercicios complejos se indica expresamente que no hay secuencia práctica
autónoma y que requiere revisión especializada.

## Validación y conservación
- Script de generación: tools/build_public_domain_catalog.py.
- Descripción española y fuente técnica: tools/catalog_technical_es.py.
- Pruebas: tools/test_public_domain_catalog.py y
  PublicDomainCatalogV2Test.java (catálogo cargado y volumen por cada ID).
- Datos operativos: progress-engine/src/main/resources/asterion_exercises/v2/.
- Visor Android: app/src/main/assets/unified_exercises/v2/catalogo_es.json.
- Las 1.257 variantes originales conservan sus IDs, orden, series y
  referencias de volumen, sin fusionar historiales con las nuevas.
- Los GIFs solo deben incorporarse cuando sean creados para Asterion
  o tengan una licencia compatible documentada.
- Para habilitar una ficha en el planificador se requiere revisión
  individual de postura, trayectoria, material, supervisión, nombre
  técnico, unidad de volumen y el nivel; no se habilita por defecto.

## Referencias públicas contrastadas
- JSON de dominio público del proyecto:
  https://github.com/yuhonas/free-exercise-db/tree/f00c92c7dcf1216a928a52c3706c7ce8e2f71ed5
- Biblioteca ACE (consulta factual, NO reproducción de textos):
  https://www.acefitness.org/resources/everyone/exercise-library/
- ExRx (solo contraste factual):
  https://exrx.net/

El presente resultado cierra la lista de 22 casos de comparación,
NO sustituye la revisión individual de seguridad y lenguaje de cada ficha.
