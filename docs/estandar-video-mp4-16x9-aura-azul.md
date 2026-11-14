# Asterion System — Estándar audiovisual aprobado
**Formato exclusivo:** MP4 H.264 (sin GIF), 640 × 360 píxeles reales, 16:9 horizontal y 25 FPS.
**Diseño del personaje:** hombre anime original con cabello negro, polo deportivo negro y aura azul profundo.
**Escenario:** gimnasio moderno de fondo fijo. Evitar variaciones de ventanas, luces, máquinas y otros detalles entre fotogramas.
**Movimiento:** distinto y específico para cada ID de las 2.104 variantes del catálogo, sin duplicar archivos ni usar el vídeo de una variante para otra.
**Nitidez:** no difuminar fotogramas ni generar interpolación óptica artificial que distorsione rostro, brazos o manos; se permiten fotogramas repetidos para ajustar duración sin inventar poses.
**Tamaño:** ideal alrededor de 400 KB, máximo 800 KB por archivo para alinearse con el reproductor offline.
**Técnica:** conservar postura y recorrido de la variante con revisiones de apoyos, equipamiento, agarre y articulaciones. Un vídeo físicamente presente no acredita aprobación técnica.
**Tiempo:** mantener duración real y continuidad en bucle; el futuro temporizador podrá vincular momentos de descenso, pausa y ascenso por metadatos propios.

## Entrega de prototipos
El paquete descargable de la conversación contiene dos MP4 reales bajo sus IDs y un manifest con SHA-256:
- flexiones_v04 — flexión convencional previamente aprobada visualmente — 406.791 bytes.
- sentadilla_bilateral_v03 — sentadilla libre, primer ejercicio nuevo del catálogo — 145.908 bytes.

Ambos tienen 640 × 360, H.264, 25 FPS y no contienen audio. La sentadilla usa una secuencia distinta, con el personaje completo y un gimnasio panorámico estabilizado en los extremos. Se mantiene en revisión visual y técnica.

**Estado honesto:** 2/2104 MP4 del estilo actual creados fuera de GitHub; **0/2104 binarios de este nuevo estilo subidos al repositorio o incluidos en la APK**; faltan 2102 vídeos adicionales. Las 2.104 rutas están preparadas, no implica que existan los archivos.

## Proceso
1. Elegir el siguiente ID exacto de la cola `docs/datos/produccion_mp4_por_variante.csv`.
2. Consultar su movimiento, equipo, apoyos y variante, sin inventar instrucciones técnicas faltantes.
3. Crear fotogramas originales en 16:9 y comprobar consistencia de personaje y fondo inmóvil.
4. Codificar únicamente a MP4 H.264 640x360, 25 FPS y sin sonido mediante `tools/encode_exercise_mp4.py`.
5. Verificar calidad visual, técnica, hash, resolución, ausencia de audio y tamaño mediante `tools/validate_exercise_mp4.py`.
6. Importar lote ZIP en modo debug con `tools/import_exercise_mp4_batch.py`. Para release se exige aprobación individual.
7. Distribuir vídeos por paquetes offline por categoría; no introducir automáticamente miles de vídeos en el APK base.
