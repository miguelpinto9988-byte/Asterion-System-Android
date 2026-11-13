# Migración controlada: exercises-dataset a Asterion (ES)

Origen: https://github.com/hasaneyldrm/exercises-dataset
Commit original fijado: 7455efae41b330c265e7cd4b78dfa848e7ce5ebd

## Contenido incluido en esta rama
- 1.324 fichas con instrucciones en español, IDs propios ext_XXXX,
  categoría Asterion, región y material en español.
- Categoría de ejercicios en 5 grupos: tren superior, tren central,
  tren inferior, movilidad y cardio.
- Explorador nativo, desde Rutinas → «Explorar 1.324 ejercicios en español».
- Filtro por categoría, búsqueda, ficha completa, exploración aleatoria
  reproducible basada en fecha/filtros y controles de consulta.
- Licencia y avisos del dataset MIT, conservados en third_party/.
- 1.324 referencias a nombres de archivos GIF/JPG, SIN incorporar ni
  enlazar/mostrar los medios sin autorización.

## Derechos y avisos
- Datos e instrucciones traducidas: MIT, según LICENSE original.
- Los 1.324 GIF (aprox. 129 MB) y 1.324 JPEG (aprox. 8,9 MB)
  pertenecen a © Gym visual. La licencia del dataset expresamente
  EXCLUYE los medios y exige permisos propios para su reutilización.
- Este trabajo no usa ni redistribuye los medios. El botón de detalles
  muestra un marcador de posición con la atribución en vez de GIF.
- Tras obtener licencia verificable, integrar los ficheros concretos
  autorizados y los requisitos de atribución en otra PR. Evitar enlazar
  remotamente GIF sin verificar también esos derechos.

## Procedimiento reproducible
- Importador: tools/import_exercises_dataset_es.py
- Pruebas: tools/test_import_exercises_dataset_es.py
- CI importación: .github/workflows/importar-ejercicios-es.yml
- Se usa una revisión fija de origen y se valida el hash Git del JSON.
- El workflow escribe los archivos bajo
  app/src/main/assets/external_exercises/v1/ y conserva LICENSE/NOTICE.
- El importador no descarga videos/ ni images/. No depende de red en la APK.

## Estado lingüístico
El dataset aporta instrucciones.es completas en las 1.324 fichas.
Los nombres de origen solo están en inglés, por lo que se generan títulos
descriptivos españoles a partir del tipo, región y equipo. Son
provisionales: la nomenclatura técnica individual requiere revisión de
traducción humana. Los nombres de origen se conservan como referencia
de trazabilidad, no como etiqueta principal de la interfaz.

## Niveles y progresión de volumen
El origen NO publica clasificación validada de nivel técnico ni de intensidad:
todos los nuevos registros llevan nivel_tecnico=pendiente_revision y
apto_para_rutinas_automaticas=false. La exploración aleatoria solo muestra
fichas para consultar; no sustituye el generador seguro de rutinas.
Las rutinas actuales y el motor de volumen siguen usando los IDs originales.
Al migrar registros se requiere matriz explícita ID externo ↔ variante Asterion,
con validación por ejercicio y unidades REPETITIONS/SECONDS; NO fusionar
historiales por coincidencia aproximada de nombres.

## Próxima integración propuesta
1. Revisar nombre español, categoría, características y advertencias de cada ficha.
2. Determinar dificultad 1..5 y elegibilidad para sugerencias automáticas.
3. Integrar selector de rutinas validado con filtrado por equipo, categoría,
   intensidad y experiencia; probar aleatoriedad reproducible.
4. Conectar perfiles individuales y reglas de volumen sin heredar historial
   de otros ejercicios. Respetar las pausas y el calendario actual.
5. Incorporar medios exclusivamente tras recibir una autorización de Gym visual.
6. Retirar el catálogo y las 138 demos históricas únicamente tras QA y
   aceptación, en otra PR; no alterar main por esta importación.

## Lo que se preserva
Se conservan 1.257 ejercicios del motor original, los registros de sesiones,
la progresión de volumen, la configuración del perfil y las animaciones
propias del avatar. No se elimina contenido antiguo en esta PR.
