# Asterion — Etapa MP4 con gimnasio y aura azul (PR apilada)

## Identidad visual definitiva de producción
El usuario ha seleccionado un **personaje masculino anime original**, cabello
negro, atuendo deportivo negro, aura azul y un gimnasio moderno de fondo.
Mantener fijos a lo largo de todos los vídeos: personaje, proporciones, rostro,
ropa, aura, iluminación del gimnasio, cámara y suelo. No reproducir un personaje
registrado ni un GIF de terceros.

## Formato técnico
- MP4 H.264, sin sonido, vertical **480 × 640** para la ficha móvil.
- Ciclo base de 12 fotogramas a 120 ms por fotograma; duración adaptable
  según la técnica sin forzar gestos mecánicos a un bucle inapropiado.
- Compresión CRF 24–32, objetivo <=400 KB por vídeo.
- VideoView local: pantalla ajustada, sin recortes, pausa/reanudar, bucle,
  respeto a reducir animaciones y liberación del reproductor al salir.
- Los archivos deben llamarse exactamente:
  app/src/main/assets/exercise_mp4/v1/<ID>.mp4
- El sistema nunca reemplaza el vídeo de una variante por otra; si falta,
  muestra el aviso «MP4 de esta variante pendiente».
- Un GIF de la misma variante puede usarse como respaldo durante la migración.

## Prototipos existentes
Estos ficheros se entregan en paquete de descarga desde la conversación.
Aún NO se han subido como binarios a esta rama de GitHub.

| ID | Movimiento | Resolución | Peso MP4 real |
|---|---|---:|---:|
| flexiones_v04 | Flexión convencional | 480 × 640 | 113.322 bytes |
| sentadilla_bilateral_v03 | Sentadilla libre | 480 × 640 | 159.841 bytes |

Los dos prototipos tienen fondo de gimnasio original y aura azul y son
**borradores visuales**; su ejecución no se declara revisada por un especialista.

## Implementación en el repositorio
- tools/build_exercise_motion_specs.py añade 2.104 rutas MP4 únicas y cuenta
  archivos presentes sin inventar los faltantes.
- tools/encode_exercise_mp4.py convierte animaciones originales (GIF, WEBP,
  MP4) a MP4 local optimizado, verificando el ID y el formato final.
- tools/validate_exercise_mp4.py examina codec, resolución, duración, tamaño,
  audio y hashes duplicados; --require-all comprueba cobertura total.
- tools/plan_exercise_mp4_batches.py genera un CSV de producción por ID.
- ExerciseMotionDetailActivity prefiere el MP4 específico, con fallback
  al GIF del mismo ejercicio, y conserva el cuadro de especificaciones.
- OfflineExerciseMp4Player usa VideoView y caché privada de cada ID para
  reproducir vídeos locales sin red.
- .github/workflows/prepare-exercise-mp4.yml ejecuta la auditoría automática.

## Etapas pendientes
1. Validar el patrón exacto de cada ejercicio/variante, agarres, implementos
   y seguridad; el inventario incluye 847 variantes nuevas con técnica en
   revisión. No inferir movimiento mediante solo palabras comunes.
2. Dibujar secuencias originales manteniendo el gimnasio y personaje,
   contrastando cada una con su ficha. Un GIF de flexión convencional no
   sirve como flexión diamante ni como zancada.
3. Convertir y comprobar cada MP4. Comparar aspecto, movimiento real,
   continuidad del bucle y movilidad en el dispositivo.
4. Distribuir por **paquetes offline por categoría** u otro mecanismo
   opcional para no instalar miles de animaciones con la APK principal.
5. Registrar aprobación técnica y permisos visuales antes de habilitar
   demostraciones como instrucciones verificadas.

Los 2.104 nombres/rutas están preparados; esto NO implica que existan
2.104 clips. No se deben considerar completados hasta generar los MP4
individuales y pasar el validador en modo --require-all.

## Optimización de tamaño: cálculo orientativo
- Si todos pesaran 150 KB: 2.104 × 150 KB ≈ 316 MB en total.
- Dos prototipos reales pesan 113 y 160 KB, respectivamente.
- La cifra global depende de la complejidad visual y de cada movimiento;
  no se debe garantizar que todos tengan el peso de estos prototipos.
