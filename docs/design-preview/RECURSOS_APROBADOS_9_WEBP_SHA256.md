# Recursos gráficos definitivos aprobados — 15/11/2026

**Estado real: pendiente de transferir los nueve binarios WebP a GitHub.** En el repositorio están el importador, las rutas esperadas y esta lista de integridad; NO se ha modificado `main`.

## Fuentes de verdad

Paquete autocontenido compartido en la conversación: `Asterion_Recursos_Aprobados_20261115.zip` con `MANIFIESTO.json`. Se creó una copia de los originales **sin modificarlos**. Los valores SHA-256 siguientes permiten verificar que no se pierde ni sustituye ningún recurso.

| Recurso original en ZIP | Nombre Android previsto | Bytes | SHA-256 |
| --- | --- | ---: | --- |
| retratos/01_cazador_azul.webp | asterion_hunter_blue.webp | 645654 | fd30a012832fdf8df2cfd31563c95bf2cf7a611692aad2993852280ae2ac15b1 |
| retratos/02_cazadora_blanco_dorado.webp | asterion_hunter_gold.webp | 726946 | ebcccdb52852d6833619e8cceb053bfd6cd29d7b6c4db48172e3b6bcc763a692 |
| retratos/03_guerrero_oscuro.webp | asterion_hunter_dark.webp | 652064 | 2fdf413f9bf6235f8d32c8448b0286b727bd08d106462e0c24e64f442d7bf142 |
| retratos/04_estratega_blanco.webp | asterion_hunter_white.webp | 609002 | 7180e6f365770f88f0c80bbdfa77c814edd8390f7d4f7cfc3d674616ecef2479 |
| fondos_tarjeta/01_temporal_oriental.webp | asterion_card_temple.webp | 455788 | 6a890bc93c1e36dc73f7955f5e3354d64de6c152533a781450769775316d04aa |
| fondos_tarjeta/02_playa_tropical.webp | asterion_card_beach.webp | 332662 | b045524370474406c5519b0c9a8f63c649331308dca1958d32c66c998398c02d |
| fondos_tarjeta/03_ciudad_occidental.webp | asterion_card_city.webp | 386354 | 2221ac5f1f79d5cbeb1dc978cfc37ae3690009b856b39def9a017967b8d44573 |
| fondos_tarjeta/04_desierto.webp | asterion_card_desert.webp | 261994 | 7b2208d212ad3decec0cd048bf1108a071f18f36015618591569a988ad9c8da1 |
| fondos_tarjeta/05_bosque_oriental.webp | asterion_card_forest.webp | 443174 | af58d2bfe9d6d60a8dfb9443a470e44e48ee1aa9e46e5f8e8cc84bc584dfcea8 |

- Los cuatro retratos son WebP **RGBA, 1024×1024**, con transparencia comprobada. Están diseñados sin marco circular para superposición en tarjeta horizontal.
- Los cinco fondos son WebP **RGB, 1600×900**. No requieren transparencia: son la capa trasera de la tarjeta.
- Se descartó el quinto retrato esmeralda; nunca agregarlo al selector.
- No sustituir estos recursos definitivos por imágenes similares o miniaturas de baja resolución.

## Instrucción de importación segura

1. Descargar `Asterion_Recursos_Aprobados_20261115.zip` de la conversación.
2. Con el repositorio local en la rama de diseño, ejecutar `python3 scripts/import_asterion_webp.py /ruta/Asterion_Recursos_Aprobados_20261115.zip .`.
3. El script verifica el hash de los nueve originales **antes de escribir alguno**. Si un nombre ya existe con contenido distinto, se detiene sin sobrescribir.
4. Revisar los nueve nuevos archivos en `app/src/main/res/drawable-nodpi/`, compilar debug y comprobar las dos variantes de tema.
5. Confirmar en GitHub que los nueve WebP aparecen como ficheros binarios en la rama antes de declarar la integración terminada.

## Compatibilidad y no pérdida de datos

- Mantener `avatar_uri` y la elección de fotografía del usuario existente.
- Mantener `card_background` y `card_silhouette` para perfiles previos; no reescribir sus claves en una actualización puramente visual sin migración comprobada.
- Usar vistas Android reales para cambiar el retrato, mostrar el fondo y superponer textos: los WebP no contienen controles.
- No fusionar a `main` antes de compilar y validar persistencia, selección del retrato y de fondo, además del tema claro y oscuro.

## Pendiente explícito

La API GitHub conectada permite crear blobs base64, pero no recibe directamente las rutas de la unidad local en que están los nueve WebP. Por esa razón **el commit de la rama no contiene aún los binarios**, únicamente el importador con protección e inventario exacto. No describir un PR de documentación como una APK integrada.
