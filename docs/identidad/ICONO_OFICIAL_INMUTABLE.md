# Identidad visual de Asterion System — política editable

**Nueva decisión del propietario: 8 de noviembre de 2026.**

Esta decisión revoca la prohibición permanente del 5 de noviembre de 2026. El icono principal **puede ser sustituido, rediseñado, recoloreado, adaptado de tamaño y mejorado de resolución** después de una revisión visual. Ninguna huella SHA-256 anterior impide esos cambios.

## Estado del icono
- Se conserva el icono actualmente integrado en app/src/main/res/drawable-nodpi/asterion_icon_oficial.webp, hasta seleccionar uno nuevo.
- La ilustración anterior puede conservarse en un respaldo o en el historial Git.
- En futuros rediseños se puede cambiar el nombre, archivo, personaje, formato y composición; se deben actualizar correctamente android:icon y android:roundIcon en AndroidManifest.xml.

## Validación técnica
- El archivo histórico tests/branding_icon_lock.py ya **no exige un hash ni una imagen concreta**.
- El script comprueba únicamente que las referencias del manifiesto apunten a imágenes existentes y con contenedores válidos; debe seguir ejecutándose en CI.
- Antes de publicar un nuevo icono, revisar nitidez en tamaños pequeños, resolución, transparencia, apariencia del icono adaptativo y dispositivos Android.

El nombre heredado de este documento contiene INMUTABLE por compatibilidad con los enlaces existentes, pero la prohibición anterior queda expresamente revocada.
