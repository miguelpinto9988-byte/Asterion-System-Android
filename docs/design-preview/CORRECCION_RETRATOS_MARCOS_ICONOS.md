> **DOCUMENTO HISTÓRICO / SUPERADO.** Los marcos ornamentales se descartaron después de esta versión. No seguir sus instrucciones de marcos ni importar sus paquetes. La especificación vigente es [REGISTRO_DEFINITIVO_SIN_MARCOS.md](REGISTRO_DEFINITIVO_SIN_MARCOS.md) (94 WebP, sin marcos).

# Corrección visual: retratos cuadrados, marcos y pictogramas (15/11/2026)

Se revisó la fuente real de los recursos gráficos, no se generaron retratos nuevos.

## Qué estaba mal y qué se corrigió

1. **Cazadores:** los cuatro originales aprobados son WebP transparentes de 1024 × 1024. El HTML ahora los muestra como cuadrados íntegros (sin máscara ni recorte circular) tanto en la selección como sobre la tarjeta. Android también utiliza su capa independiente.
2. **Marcos:** las ocho ilustraciones individuales de 1600 × 900 tenían márgenes de transparencia alrededor de su dibujo, que causaban un borde visible separado de la tarjeta. Se normalizó el espacio transparente de CADA archivo individual y se escaló a 1600 × 900 para ajustarlo al contorno 16:9. No son recortes de la lámina de ocho diseños; los originales se conservaron intactos en el paquete maestro anterior.
3. **Iconografía:** se restituyeron las 74 imágenes WebP negro/blanco aprobadas para tema claro/oscuro, y los 6 iconos de categorías de entrenamiento. Los encabezados del paso de personalización en Android ahora usan `asterion_light_ic_*` o `asterion_dark_ic_*` según el tema, sin teñir sus imágenes de violeta.
4. **Tarjeta nativa:** contenedor medido en proporción 16:9, imagen cuadrada sin máscara y marco sobrepuesto. Se preservan los IDs guardados.
5. **HTML autocontenido:** `Asterion_Registro_Perfil_Corregido_Imagenes_Marcos_Iconos.html` y vista previa `Asterion_Corregido_Visual_1440.png` entregados en la conversación; los archivos no están publicados dentro del repositorio.

## Pruebas verificadas del HTML

- Chromium de escritorio 1440px y móvil 375px, cero errores JavaScript y sin scroll horizontal imprevisto.
- 27 iconos mostrados y 27 cargados correctamente en el tema de prueba.
- Las cuatro capas de tarjeta se cargaron: paisaje 1600×900, retrato 1024×1024, acompañante 1024×1024, marco 1600×900.
- Caja del marco coincide con el tamaño de la caja de tarjeta (0px de diferencia en x, y, ancho y alto).

## Paquete Android corregido

`Asterion_Android_104_WebP_Marcos_Ajustados.zip` contiene los mismos 104 recursos aprobados, salvo que sus 8 marcos se normalizaron para encajar al borde. El `MANIFIESTO_ANDROID.json` se actualizó con los nuevos SHA-256 para esos ocho WebP. Usar en el clon:

```sh
python3 scripts/import_asterion_all_resources.py /ruta/Asterion_Android_104_WebP_Marcos_Ajustados.zip .
```

**Precaución de no sobrescritura:** si hay recursos de versiones anteriores en `drawable-nodpi` con nombres idénticos y contenido diferente, el script se detendrá sin sustituir ninguno. Es preferible revisar el conflicto y conservar respaldo antes de migrar.

**Pendiente:** subir físicamente los 104 WebP a GitHub, compilar la APK y validar la apariencia en Android real. Esta rama no está fusionada con `main`.
