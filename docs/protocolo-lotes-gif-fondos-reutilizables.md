# Producción de GIF transparentes por lotes — Asterion

## Estado del lote 0001 (14 de noviembre de 2026)

- **100 IDs reales** tomados de las primeras 100 filas de `docs/datos/produccion_mp4_por_variante.csv` y registrados en `docs/datos/lote_0001_gifs_transparentes.json`.
- **0 GIF nuevos** generados en este lote; **0 GIF del lote** importados a la APK.
- **9 fondos** clasificados y referenciados en `docs/datos/biblioteca_fondos_gif_v1.json` (7 categorías iniciales + plataforma/escalón y pared de apoyo). Las rutas indican el destino planificado y **no implican que existan archivos**.
- **Revisión técnica pendiente** en las fichas del CSV; la clasificación automática de fondos es provisional hasta comprobar apoyos, equipo, cámara y gesto de cada variante.
- El catálogo completo contiene 2.104 variantes. El lote 0001 abarca las primeras 100 en orden de la cola, no cualquier conjunto aleatorio.

## Contrato por ejercicio

Identificador único `id`; ruta GIF `exercise_gifs/v2/<id>.gif`; fondo reutilizable `exercise_backgrounds/v1/BG_XXX.webp`; anchura 551, altura 466; transparencia real; bucle; cuerpo completo; postura y contacto con el suelo acordes al equipo.

Conservar nombre, categoría, variante y material originales. Para ejercicios con silla, banco, barra, pared, escalón o suelo, el personaje debe interactuar coherentemente con ese equipo, no basta con colocar una ilustración detrás.

## Estados permitidos

`pendiente_generacion` → `generado_pendiente_revision` → `aprobado_para_integracion` → `integrado_y_validado`.

Registrar los bytes reales, SHA-256, fotogramas, duración, fondo asignado y revisión visual y técnica. No confundir un prototipo de otra variante con el archivo final. El modo `debug` no implica aprobación para publicar.

## Registro incremental

Cuando exista un GIF real en una carpeta de producción, ejecutar:

```sh
python3 -m pip install Pillow
python3 tools/register_exercise_gif_batch.py --input-dir ./lote_0001
# Opcional: copiar los archivos comprobados al conjunto de assets de depuración:
python3 tools/register_exercise_gif_batch.py --input-dir ./lote_0001 --stage-debug
```

El registrador verifica ID, GIF, transparencia, número de fotogramas, dimensiones y SHA, y rechaza bytes duplicados entre IDs. Actualiza el manifiesto solo tras validar el archivo. Si los archivos ya se registraron sin staging, copiar a debug debe tratarse como una operación distinta; una repetición con el mismo hash no vuelve a importar automáticamente.

Para mostrar la ficha en producción aún es necesario incorporar los fondos binarios, habilitar el visor transparente sobre fondo en Android, realizar las pruebas y revisar técnicamente cada animación. La generación gráfica de cada GIF no la hace este script por sí solo.

## Propuesta de lotes posteriores

0002: siguientes 100 variantes aún pendientes, conservando orden e IDs. No programar un lote nuevo hasta verificar la capacidad real de producción de animaciones correctas. Cada tanda debe tener un registro propio y no puede dar por terminados 100 videos solo porque existan sus rutas.
