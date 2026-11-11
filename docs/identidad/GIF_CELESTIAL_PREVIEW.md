# Asterion System — animación celestial Full HD

La restricción histórica de los recursos de marca se revocó el 8 de noviembre de 2026.

## Recursos preparados
- GIF: 1080 × 1920, ocho fotogramas de 200 ms, 1,6 segundos por ciclo, aprox. 11,9 MB.
- WebP animado: 1080 × 1920, aprox. 1,1 MB, mejora la relación entre detalle y tamaño.
- PNG de referencia Full HD.
- Imagen original proporcionada por el propietario: 864 × 1536.

La animación **todavía no se ha subido al repositorio** como recurso binario. Para incluirla se debe copiar el archivo HD a app/src/main/res/raw/asterion_splash_celestial.gif. El ZIP entregado en ChatGPT contiene ese archivo y los cambios.

La vista nativa AnimatedSplashView es un reproductor local sin internet que respeta el encuadre y detiene la reproducción al pausar. SplashActivity puede mostrar la animación en la APK normal o de prueba cuando se empaquete el archivo, y conserva los PNG existentes como respaldo ante ausencia o error del GIF.

## Antes de completar la integración
- Subir el GIF binario a la rama de revisión.
- Ejecutar compilación, lint, test unitarios y un ensayo en un teléfono o emulador.
- Valorar WebP animado por la diferencia de peso antes de fusionar; Android API 26-27 necesita una ruta de compatibilidad distinta a AnimatedImageDrawable de API 28+.
- No sustituir el icono mientras no se haya aprobado la ilustración nueva.
