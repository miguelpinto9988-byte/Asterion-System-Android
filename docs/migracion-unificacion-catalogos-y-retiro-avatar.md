# Asterion — conciliación conservadora de dos catálogos

## Procedencia y derechos
- Asterion: 1.257 variantes originales, con IDs y referencias de volumen vigentes.
- Fuente adicional: https://github.com/hasaneyldrm/exercises-dataset
  revisión fijada 7455efae41b330c265e7cd4b78dfa848e7ce5ebd.
- Datos e instrucciones en español del origen: MIT, con LICENSE y NOTICE conservados.
- GIF/JPG: propiedad de Gym visual, NO amparados por la licencia MIT ni
  autorizados para redistribución en esta aplicación. No se incorporan.
  Encontrar el mismo movimiento en otra página no concede una licencia
  sobre su imagen, animación o redacción.

## Resultados de la primera comparación (provisionales)
| Estado | Total | Tratamiento |
|---|---:|---|
| Originales Asterion | 1.257 | Se conservan íntegros |
| Duplicados confirmados por ID | 5 | Se conserva el original y se excluye la copia |
| Posibles duplicados | 481 | No se agregan hasta revisión |
| Nuevos provisionales | 838 | Se incorporan al explorador unificado |
| Total visible en catálogo unificado | 2.095 | 1.257+838 |

Estas cantidades dependen de heurísticas conservadoras. El archivo
docs/datos/comparacion_catalogos_es.csv contiene un dictamen para
cada una de las 1.324 fichas externas, junto con la variante
histórica potencialmente relacionada. Un posible duplicado NO es una
equivalencia confirmada. Pueden existir duplicados todavía no detectados
cuando los movimientos reciben nombres muy diferentes.

## Archivos, algoritmos y visibilidad
- Comparador reproducible: tools/merge_catalogs_es.py.
- Pruebas: tools/test_merge_catalogs_es.py.
- Catálogo unido: app/src/main/assets/unified_exercises/v1/catalogo_es.json.
- Manifiesto de cifras: app/src/main/assets/unified_exercises/v1/manifest.json.
- Auditoría completa: docs/datos/comparacion_catalogos_es.csv.
- UI: pantalla «Explorar catálogo unificado en español» en Rutinas.
- Se muestra primero el catálogo original, conservando su ID,
  categoría, nivel y denominación española.
- Las nuevas fichas tienen IDs independientes ext_XXXX, títulos descriptivos
  provisionales e instrucciones MIT en español; se marcan pendiente_revision.

## Progresión, niveles y aleatoriedad
- La aleatoriedad del explorador es informativa; no prescribe rutinas.
- El generador de rutinas y el motor de progresión de volumen utilizan
  exclusivamente el catálogo original de 1.257 variantes.
- Se conservan las referencias TSV, el historial local y las preferencias.
- No se asignan niveles ficticios a los nuevos ejercicios. Deben revisarse
  nivel técnico, supervisión, material y unidad (repeticiones o segundos)
  para incorporarlos al generador; hasta entonces son consultables.
- No se heredan series ni repeticiones por parecido de nombres.

## Retirada de demostraciones antiguas
- Se elimina de la rama de migración el avatar 2D de ejercicios,
  sus pruebas, documentación y botones de acceso.
- El avatar de perfil personal continúa intacto.
- La PR anterior con 138 demos no está incorporada a main y no debería
  fusionarse si se reemplaza este sistema.
- La eliminación no altera main hasta fusión. El historial de Git conserva
  un respaldo recuperable de los archivos retirados.

## Siguientes validaciones
1. Resolver los 481 posibles duplicados comprobando movimiento, material,
   posición y técnica, no solo palabras similares.
2. Revisar los 838 nombres y categorías provisionales con fuentes
   propias o legalmente reutilizables.
3. Asignar nivel técnico y elegibilidad para sugerencias, caso por caso.
4. Integrar solo las variantes validadas al motor de rutinas y crear
   sus referencias de volumen sin heredar historiales de otros ejercicios.
5. Verificar APK y pruebas de regresión antes de fusionar.

No se usa otro sitio web para eludir derechos de autor: se conservan datos
MIT y hechos sobre ejercicios; las futuras animaciones deberán ser
creadas para Asterion o contar con derechos suficientes.
