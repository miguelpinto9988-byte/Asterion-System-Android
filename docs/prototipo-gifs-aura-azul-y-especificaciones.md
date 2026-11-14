# Asterion: GIF propios por variante y ficha móvil

## Identidad aprobada
Personaje masculino original con estética anime de cazador oscuro,
ropa deportiva negra y aura azul. Mantener el mismo personaje a través
de todos los movimientos; NO reproducir la identidad de personajes protegidos.

## Contrato móvil
- Objetivo por GIF: 480 × 640 píxeles (vertical 3:4).
- 12 fotogramas orientativos, 120 ms/fotograma, reproducción en bucle.
- Formato GIF89a y objetivo máximo 800 KB.
- Mostrar manos, pies, articulaciones y apoyos sin cortes.
- Reproductor Android offline con FIT_CENTER, sin recorte y con botón de pausa.
- Si falta el GIF exacto del ID, mostrar «pendiente» y nunca otra variante.
- Todas las animaciones requieren revisión técnica antes de publicarse.

## Cobertura comprobada
- 2.104 ejercicios con un ID y una ruta GIF esperada diferentes.
- 0 GIF nuevos creados por el generador de cuadros.
- tools/build_exercise_motion_specs.py: genera las fichas técnicas preliminares.
- tools/test_exercise_motion_specs.py: verifica cobertura y diferencias.
- tools/render_exercise_gifs.py: convierte fotogramas originales de cada ID en GIF.
- tools/validate_exercise_gifs.py: impide duplicados binarios y archivos erróneos.
- app/src/main/assets/exercise_gifs/v1/specs_es.json: índice de 2.104 cuadros.
- El GIF del personaje aura azul para flexión convencional se entrega
  por separado como prototipo visual; no equivale a validación biomecánica.

## Ejemplo de variantes: flexiones

| Variante | ID | Colocación distintiva | GIF propio |
|---|---|---|---|
| Estándar | flexiones_v04 | Manos al ancho aproximado de hombros | flexiones_v04.gif |
| Agarre cerrado | pd_e_0565 | Manos próximas, sin presuponer diamante | pd_e_0565.gif |
| Amplia | pd_e_0564 | Manos más separadas | pd_e_0564.gif |
| Inclinada | pd_e_0347 | Manos en apoyo elevado | pd_e_0347.gif |
| Inclinada cerrada | pd_e_0348 | Manos próximas y apoyo elevado | pd_e_0348.gif |

Una flexión en diamante es otra variante: pulgares e índices forman
una figura de rombo. Tiene un posicionamiento distinto del agarre
cerrado genérico, y puede requerir precaución adicional para muñecas.
No hay en este catálogo una ficha «diamante» aprobada. Si se agrega,
tendrá un ID propio, especificaciones individualizadas y su GIF propio.

## Proceso por lote sin falsificar movimientos
1. Revisar la variante, agarre, postura y trayectoria con criterio técnico.
2. Crear los fotogramas originales para ese ID y mantener personaje, ropa,
   altura, cámara y aura consistentes.
3. Guardar PNG numerados en arte_original/fotogramas/ID/001.png, etc.
4. Ejecutar python tools/render_exercise_gifs.py --frames-dir arte_original/fotogramas --id ID
   (se necesita instalar Pillow==11.3.0).
5. Revisar el bucle y las fases en dispositivo Android.
6. Ejecutar python tools/validate_exercise_gifs.py.
   Para exigir cobertura total, añadir --require-all.
7. Registrar aprobación humana antes de publicar como demostración.

2.104 GIF de 600 KB ocuparían unos 1,26 GB, excesivo para una APK única.
Se recomienda futuro empaquetado modular offline por categorías. El APK
actual no solicita internet ni descarga automáticamente contenido.

## Mejoras siguientes
- Producir y revisar de verdad los otros 2.103 GIFs diferenciados.
- Paquetes offline por categoría y manifest de versiones.
- Modo reducido de movimiento con imagen de póster estática.
- Revisión de técnica individual de nuevas fichas antes de habilitar
  sugerencias automáticas.
