> **DOCUMENTO HISTÓRICO / SUPERADO.** Los marcos ornamentales se descartaron después de esta versión. No seguir sus instrucciones de marcos ni importar sus paquetes. La especificación vigente es [REGISTRO_DEFINITIVO_SIN_MARCOS.md](REGISTRO_DEFINITIVO_SIN_MARCOS.md) (94 WebP, sin marcos).

# Informe de auditoría — creación y personalización de perfil

**Fecha:** 15/11/2026 — **rama de diseño:** `design/profile-wizard-components-20261115`.

## Verificaciones realizadas

- Se verificaron **99 WebP** del paquete visual v2 y **5 WebP** de acompañantes, 104 recursos en total, mediante tamaño y SHA-256.
- Los retratos, ocho marcos y acompañantes son WebP RGBA; se verificó que su canal alfa incluye píxeles totalmente transparentes. Los fondos de tarjeta son 16:9 1600×900.
- Se preparó el paquete `Asterion_Android_104_Recursos_WebP_Aprobados.zip`, con rutas `app/src/main/res/drawable-nodpi/` y `MANIFIESTO_ANDROID.json`, importador con detección de conflictos y verificación SHA-256 de los 104 elementos. La prueba local del importador reconoció correctamente recursos idénticos sin sobrescribirlos.
- Se generó el HTML autocontenido `Asterion_Registro_Perfil_Final_Interactivo.html`, con los cuatro pasos, calendario editable, tema claro/oscuro, cuatro cazadores, cinco fondos, ocho marcos y cinco acompañantes. **Pruebas automáticas en Chromium:** cero errores de JavaScript; imagen de fondo 1600×900, retrato y compañero 1024×1024, marco 1600×900; opciones 4/5/8/5 correctas; cambio de tema funcional; confirmación activa/inactiva correcta; vista móvil 375px sin desbordamiento horizontal.
- Correcciones nativas guardadas en esta rama: evitar tinte de imágenes completas; conservar color propio de los iconos negros/blancos; asociar los nombres reales de iconos; conservar el nombre blanco sobre el paisaje; colocar el cazador a la izquierda y el compañero en la tarjeta; reflejar el marco seleccionado en la revisión final.
- Se preservaron los IDs del perfil guardado, incluidos `card_silhouette`, `card_background`, `card_frame`, `companion`, `avatar_uri` y `week`; no se cambió el contrato de `ProfileStore`.

## Archivos de entrega creados en esta conversación

- `Asterion_Registro_Perfil_Final_Interactivo.html`: HTML autocontenido con imágenes reales.
- `Asterion_Personalizacion_Final_Preview.png`: captura de la personalización.
- `Asterion_Android_104_Recursos_WebP_Aprobados.zip`: archivos listos para introducir en Android (no dependencias externas).

El HTML no sustituye la aplicación y no guarda perfiles en el móvil; es una demostración local.

## Bloqueos que siguen abiertos

1. **Los 104 WebP binarios siguen sin subirse a GitHub.** La conexión permite editar texto, pero no acceder a los bytes del ZIP local para crear 104 blobs en el repositorio. No se ha afirmado lo contrario.
2. **APK no compilada.** En este entorno no existe SDK de Android ni Gradle ejecutable; no se pueden certificar build, instalación o pruebas instrumentadas.
3. Hay que ejecutar el importador en un clon autorizado, verificar que los 104 WebP existen en la rama, compilar, probar el registro y la persistencia con perfiles nuevos y anteriores, y luego decidir la fusión del PR.
4. No se ha fusionado `main`; toda la implementación está en la rama de diseño.

## Importación recomendada

Descargar el ZIP de 104 recursos de la conversación; luego, en un clon de la rama:

```sh
python3 scripts/import_asterion_all_resources.py /ruta/Asterion_Android_104_Recursos_WebP_Aprobados.zip .
git add app/src/main/res/drawable-nodpi
git commit -m "assets(profile): integrar 104 WebP aprobados"
git push origin design/profile-wizard-components-20261115
```

El importador verifica los 104 hashes antes de escribir y rechaza sobrescribir recursos diferentes. Evitar fusionar este PR hasta que GitHub confirme los binarios y la compilación funcione.
