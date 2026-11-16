# Cinco acompañantes chibi definitivos — WebP transparentes

Estado: **creados y verificados localmente** el 15/11/2026. Esta página y el importador se encuentran en la rama de diseño. **Los cinco archivos binarios todavía no se han cargado físicamente en GitHub**; la conexión de GitHub no puede leer directamente el ZIP local para convertirlo en blobs.

Archivo maestro aprobado para importar: `Asterion_5_Acompanantes_WebP_Definitivos.zip`. Incluye cinco WebP de 1024×1024, `MANIFIESTO.json` y una vista previa. Los 5 archivos tienen canal RGBA y píxeles totalmente transparentes.

| ID guardado | WebP Android | Bytes | SHA-256 |
| --- | --- | ---: | --- |
| igris | asterion_companion_igris.webp | 165064 | 7569bc121a710c27a0c55df620affd34e9daf4b55b345b46313f4e862f662d2e |
| beru | asterion_companion_beru.webp | 136298 | e02fd869f2ceb4532d7992d89d3a49749f427d1ddaa129aaa83e518d88fa61cc |
| tank | asterion_companion_tank.webp | 126126 | 2e3d93306c665ee996224b86099a7e09037c0696d4097b0e2b37d1aa37b3a30b |
| tusk | asterion_companion_tusk.webp | 164002 | 12f3e4575f4b2015954a9ce037e7889fb3d094443101700f561a72940130c3ce |
| iron | asterion_companion_iron.webp | 132428 | 57bfa002918f7d26797e1a8abbf267bce609efc738f89a3e6d6a2e89dccea649 |

Igris, Beru y Tusk tienen transparencia original. En las últimas versiones de Tank e Iron se eliminó el fondo cuadriculado y se reconstruyó un canal alfa para preservar su estética seria.

## Importación a GitHub sin pérdida de archivos

Desde un clon del repositorio con la rama `design/profile-wizard-components-20261115` activa:

```bash
python3 scripts/import_asterion_companions.py /ruta/Asterion_5_Acompanantes_WebP_Definitivos.zip .
git add app/src/main/res/drawable-nodpi/asterion_companion_*.webp
git commit -m "assets(profile): agregar cinco acompañantes chibi WebP aprobados"
git push origin design/profile-wizard-components-20261115
```

El importador verifica el contenido completo y los cinco SHA-256 **antes de escribir nada**. Si encuentra un archivo existente con diferente contenido, detiene la operación y no lo sobrescribe. La selección `companion` ya conserva los identificadores `igris`, `beru`, `tank`, `tusk`, `iron`. La vista previa en `SimpleProfileStepThreeView` carga el acompañante sobre la tarjeta si los WebP están disponibles y no borra la selección si faltan.

No declarar compilación ni subida de los binarios hasta verificar los archivos reales dentro de GitHub y completar una prueba Android.
