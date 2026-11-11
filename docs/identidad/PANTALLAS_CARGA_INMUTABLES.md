# Pantallas de carga Asterion System — política editable

**Nueva decisión del propietario: 8 de noviembre de 2026.**

Se revoca la prohibición anterior de retocar o sustituir las pantallas de carga. Se pueden modificar personajes, composición, color, tipografía, efectos, duración y horario y usar GIF, WebP o video con una implementación Android apropiada. Las huellas SHA-256 anteriores no son un bloqueo.

## Fondos y animaciones
- Se conservan las imágenes PNG antiguas de mañana y tarde/noche como respaldo, ambas de 1080 × 1920.
- Se prepara una animación celestial en GIF a 1080 × 1920, ocho fotogramas, ciclo de 1,6 segundos; su archivo binario debe incorporarse en app/src/main/res/raw/asterion_splash_celestial.gif.
- Existe también un WebP animado Full HD de menor tamaño para valorar una integración de mayor calidad.
- El GIF toma como fuente una ilustración de 864 × 1536. El escalado conserva más detalle que el GIF pequeño pero no crea detalles nuevos.

## Control de calidad
- El script histórico tests/branding_splash_lock.py ya **no valida hashes de los dibujos, composiciones ni horarios obligatorios**. Comprueba recursos válidos y arranque Android correcto.
- Antes de publicar, verificar reproducción en Android, consumo de memoria, funcionamiento sin red, legibilidad y tiempos de apertura.
- Mantener una variante APK de prueba independiente para comparar el diseño sin afectar a los datos de la instalación existente.

El nombre del documento contiene INMUTABLES únicamente para mantener enlaces anteriores; la restricción artística queda revocada.
