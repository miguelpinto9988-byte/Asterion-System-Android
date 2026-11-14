# Catálogo Asterion v2 — integración independiente de dominio público

## Propósito
Reemplazar el catálogo de importación anterior por un inventario de procedencia
independiente cuya licencia permita construir aplicaciones y crear animaciones
propias, manteniendo todos los identificadores, referencias de volumen y
progresiones históricas de Asterion.

## Origen comprobable
- Fuente pública: https://github.com/yuhonas/free-exercise-db
- Compilación usada: f00c92c7dcf1216a928a52c3706c7ce8e2f71ed5
- Archivo público: dist/exercises.json (876 ejercicios)
- Hash Git de objeto: 37beaac6031b34f7a7322b3150d201182c49a71b
- Licencia publicada: Unlicense (dedicación a dominio público).
- Los datos se extraen de esta fuente pública. Se generan títulos de
  visualización en español para Asterion; son descriptivos y algunos
  requieren revisión terminológica. Los campos fuente son solo hechos
  estructurados: nombre, nivel orientativo, músculo, equipo y categoría.
- No se descarga ni se incorpora material gráfico del repositorio público;
  Asterion prevé integrar sus propias animaciones.

## Licencia anterior: separación completa
- Se eliminaron del árbol actual las instrucciones traducidas, JSON,
  multimedia referenciada, avisos de derechos y scripts importadores
  del repositorio exercises-dataset usado en la investigación previa.
- Las referencias históricas pueden permanecer en commits Git anteriores:
  la historia de Git conserva trazabilidad. Ningún asset del origen anterior
  se empaqueta en la versión v2.
- Cambiar el nombre de los archivos de una obra no elimina sus derechos de
  autor; la independencia aquí proviene de otra fuente pública y de
  información factual, no de borrar un aviso.
- No se incluyen GIFs, capturas ni fotos de ninguna fuente ajena.

## Duplicados y decisiones
El script tools/build_public_domain_catalog.py evalúa los 876 elementos de
la fuente pública frente a las 1.257 variantes legadas de Asterion.
El archivo docs/datos/auditoria_dominio_publico.csv explica, por registro:
- id_fuente_publico: registro técnico del repositorio público.
- nuevo_por_evaluar: no se encontró coincidencia plausible bajo los
  criterios conservadores y pasa al catálogo v2 informativo.
- posible_duplicado: se conserva el antiguo y se excluye el nuevo hasta
  verificación individual de equipo, músculo y gesto técnico.
- duplicado_verificado: exclusivamente una equivalencia positiva revisada.

Los posibles duplicados no son verificaciones concluyentes. No deben
convertirse en «duplicados confirmados» automáticamente.

## Integración operativa
- El catálogo histórico v1 con 1.257 variantes se mantiene BYTE POR BYTE
  (familias, tipos, variantes, intensidades, volúmenes y deportes).
- Los siete TSV v2 están en progress-engine/src/main/resources/
  asterion_exercises/v2/.
- La app lee el catálogo y el volumen de v2 en repositorios Android.
- Las variantes nuevas tienen id=pd_e_XXXX, un tipo/familia válidos, material,
  categoría y perfil de referencia de volumen.
- Nuevo estado nivel=0 significa «dificultad pendiente de clasificación».
  Toda ficha nivel=0 queda excluida del planificador automático, y la
  referencia de volumen es exclusivamente consultable/manual.
- El motor nunca hereda series, repeticiones, segundos ni historial de otro
  ejercicio por similitud de nombre.
- Los antiguos ejercicios conservan los mismos IDs y reglas.
- La pantalla Rutinas → Explorar catálogo independiente en español
  muestra primero Asterion y después las nuevas fichas informativas.
- El avatar 2D histórico de demostraciones sigue retirado; el avatar
  personal del perfil continúa.

## Trabajo pendiente para recomendaciones reales
1. Revisar manualmente equivalencias dudosas antes de descartarlas o añadirlas.
2. Verificar títulos técnicos españoles, clasificadores y técnica desde
   conocimiento propio o fuentes que lo permitan.
3. Evaluar el nivel y los requisitos de supervisión uno por uno.
4. Cambiar cada nivel 0 solo cuando un revisor registre su evaluación y
   se hayan probado las reglas de planificación y volumen correspondientes.
5. Incorporar GIFs creados para Asterion cuando estén disponibles.

NOTA: este proceso no declara verificados automáticamente ejercicios
de fuerza complejos ni promete seguridad clínica de una base de datos pública.
