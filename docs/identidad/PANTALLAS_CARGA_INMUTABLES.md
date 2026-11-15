# Política de pantallas de carga — revisión aprobada por el propietario

Las ilustraciones anteriores se sustituyen por dos composiciones verticales a pantalla completa sin rótulos de hora. Esta aprobación actualiza la política anterior; el **ícono oficial del lanzador** continúa protegido e inalterado.

## Recursos vigentes

| Horario local del teléfono | Recurso usado por la aplicación | SHA-256 |
|---|---|---|
| 00:00:00–11:59:59 | `app/src/main/res/drawable-nodpi/asterion_loading_manana_4k.webp` | `2124c87d5bfc9d793fae5a9e558b91eb34884fbbb3c9d5df36667617f0f4b434` |
| 12:00:00–23:59:59 | `app/src/main/res/drawable-nodpi/asterion_loading_tarde_noche_4k.webp` | `dc1c38e2d6699c0a7c18506e1c2dc1b382be1796166f4f858f91e246e19ac494` |

- Cada ilustración es **WebP de 2160 × 4800** con alto nivel de calidad y menor peso que PNG.
- Se creó a partir de la composición vertical aprobada mediante un remuestreo de buena calidad y enfoque suave. No se afirma que se haya añadido detalle real equivalente a generación nativa a 4K.
- No hay etiquetas de hora en las ilustraciones. Se mantiene el personaje delante del portal, el título y la barra decorativa de carga.
- El `ImageView` usa `centerCrop` para cubrir físicamente toda la pantalla y evitar márgenes o bandas; según la relación de aspecto, se recortan bordes exteriores. No se deforma la imagen.
- `SplashActivity` oculta las barras de sistema mientras es visible y utiliza `Calendar.getInstance().get(Calendar.HOUR_OF_DAY)` para seleccionar automáticamente.
- La barra dibujada **no representa progreso real de descarga**. La ilustración se muestra aproximadamente 1,6 segundos; luego sigue el flujo normal de perfil e inicio.
- El ícono oficial `asterion_icon_oficial.webp` y su prueba de integridad permanecen sin cambios.

## Verificación

`tests/branding_splash_lock.py` comprueba la integridad SHA-256, dimensiones reales WebP, selección horaria, manifest, referencias 4K y `centerCrop`. `tests/splash_window_insets_regression.py` mantiene la protección contra el acceso prematuro al controlador de barras en Android 16. La actualización del contenido de las pantallas fue solicitada explícitamente por el propietario.
