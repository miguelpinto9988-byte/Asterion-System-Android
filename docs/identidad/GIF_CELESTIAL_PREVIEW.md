# GIF celestial opcional en la APK de prueba

Este cambio prepara una **prueba separada**, solicitada por el propietario, sin alterar el icono ni las dos imágenes inmutables de mañana/tarde-noche.

- El archivo opcional debe añadirse en `app/src/main/res/raw/asterion_splash_celestial.gif`.
- Únicamente la variante `:app:assemblePreview` utiliza el GIF. El paquete instalado es `pe.asterionsystem.app.preview`, independiente de la APK principal.
- La vista `AnimatedSplashView` reproduce fotogramas con `android.graphics.Movie`, sin internet, conserva la proporción completa y pausa el repintado cuando la actividad se detiene.
- Si el archivo aún no está presente o no puede leerse, ambas imágenes oficiales originales siguen disponibles como respaldo.
- Se conserva la duración del arranque de 1,6 segundos y la navegación a perfil o pantalla principal.
- La prueba no necesita dependencias externas y no modifica `tests/branding_splash_lock.py` ni la política `PANTALLAS_CARGA_INMUTABLES.md`.

**Importante: falta adjuntar el recurso GIF binario a esta rama.** El parche completo con la animación se entrega aparte como `asterion_splash_preview_android_patch.zip`. Hasta que el GIF esté en `res/raw`, esta rama no mostrará una animación nueva. No fusionar esta propuesta hasta haber adjuntado el GIF, compilado la variante preview y comprobado el resultado.

Recurso preparado: GIF de 380 × 679 píxeles, 6 fotogramas, 180 ms cada uno, SHA-256 `2bde99997765d37b4c99f3e1f02ba2302f41251e1cefa32cf487647ae24a699f`.
