# Motor de avatar 2D directamente dentro de Android (sin Godot)

**Fecha:** 11 de noviembre de 2026. **Base:** PR #13 (rutinas/calendario), a su vez dependiente del catálogo del PR #11. Esta implementación es una primera demostración, no una biblioteca terminada de ejercicios.

## Motivación

Evitar el uso de Godot, MP4 por movimiento, reproducción en red o servicios de generación de IA durante el funcionamiento de la APK. El Android existente está escrito en Java y usa Views nativas.

Android documenta `View.onDraw(Canvas)`, `Paint`, `Path.cubicTo()` y `postInvalidateOnAnimation()` como primitivas compatibles para dibujo 2D acelerado por hardware (según el dispositivo): https://developer.android.com/develop/ui/views/layout/custom-views/custom-drawing.html y https://developer.android.com/develop/ui/views/graphics/hardware-accel

## Arquitectura del prototipo

- `AvatarKinematics` contiene únicamente matemáticas Java; es completamente independiente de Android y se puede probar con JUnit local.
- `AvatarKinematics.Pose` dispone de pelvis, hombros, cabeza, codos, muñecas, ambas rodillas y tobillos.
- La pelvis sigue una trayectoria 2D suave. El esqueleto calcula las rodillas mediante intersección de dos circunferencias (cinemática inversa analítica de dos segmentos). Conserva longitudes aproximadas de fémur/tibia y ambos tobillos fijos durante la sentadilla.
- El ciclo incluye inicio, descenso con aceleración/desaceleración gradual, pausa ilustrativa, subida gradual y retorno a inicio. No establece cargas, número de repeticiones ni tiempos de entrenamiento.
- `NativeAvatarView` dibuja los avatares azul y morado por `Canvas` con contornos de curvas Bézier cerradas, suavizado, ropa y pelo. No importa sprites, bitmaps por fotograma, animaciones de otros motores ni librerías externas.
- `NativeAvatarActivity` muestra los dos personajes y ofrece reproducción, pausa, reinicio, velocidad ilustrativa 0,5×/1× y desplazamiento manual por la línea de tiempo; detiene los fotogramas cuando sale de pantalla para evitar trabajo innecesario.
- Se accede desde Rutinas mediante «Ver avatares 2D animados» y desde los pasos cuyo nombre de variante contiene «sentadilla». No agrega tarjetas a Inicio ni modifica el menú inferior de solo Inicio/Rutinas/Calendario.
- Permite funcionamiento local y desconectado; no hay permiso INTERNET ni migración de datos.

## Limitaciones y precisión

1. **No es el avatar gráfico final**: reproduce sus dos paletas y una ilustración orgánica de cuerpo entero, pero no separa las ilustraciones fotorrealistas anteriores en mallas deformables. Para mantener exactamente los rostros, peinados y prendas del concepto será necesario dibujar piezas vectoriales editables de cada personaje y asignarlas al mismo esqueleto.
2. **No es un verificador biomecánico**. La cinemática 2D preserva longitudes y continuidad de un movimiento esquemático; no demuestra alineación de rodilla en los tres ejes, técnica individual, tolerancia articular ni seguridad de realización.
3. **Una sola animación de ejemplo:** sentadilla sin carga. No se muestra como ejemplo de flexiones, dominadas, carrera u otros movimientos no animados; esa biblioteca visual vendrá después, mediante ID de animación por tipo de ejercicio.
4. **Sin previsualización de pantalla certificada** hasta ejecutar la APK en el emulador y revisar el render, tamaños de texto, controles y rendimiento.

La investigación biomecánica resalta que la sentadilla cambia con la anchura y rotación de los pies, inclinación del tronco, posición de tibia y profundidad; no existe una sola ejecución válida para todos los cuerpos: https://pubmed.ncbi.nlm.nih.gov/38576836/

## Extensión futura (no incluida)

- Crear un formato versionado `avatar_animations/*.json` con longitudes, fotogramas clave y restricciones por movimiento para no escribir código nuevo por ejercicio.
- Reemplazar la ilustración procedimental por siluetas Bézier más detalladas y mallas con deformación de la piel y ropa alrededor de hombros, caderas y rodillas.
- Mapear `type_id` o `variant_id` a un mismo movimiento 2D reutilizable; si no hay animación, mostrar la ficha normal en lugar de una demostración equivocada.
- Pruebas visuales de los dos avatares a lo largo de toda la secuencia, revisión profesional de movimiento y accesibilidad.
- Reproducción optativa a MP4 local mediante `MediaCodec` en un futuro, únicamente si hiciera falta compartir videos; no es necesaria para la APK.

## Validación automatizada

- JUnit `AvatarKinematicsTest`: segmentos de longitud constante, tobillos fijos, desplazamiento continuo de pelvis y fases interpoladas.
- Contrato Python `tests/native_avatar_2d_contract.py`: dependencias nativas, entrada desde Rutinas/guía de sentadilla, no cambios en la estructura de Inicio.
- Android CI: JUnit, Lint, `assembleDebug` y emulador Android 16.
