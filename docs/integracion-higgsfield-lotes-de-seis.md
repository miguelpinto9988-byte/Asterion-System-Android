# Flujo Asterion: tandas de exactamente seis y unión por ID

## Producción Higgsfield

- La cola contiene 2.104 ejercicios y variantes, con identificador único.
- **Lote de seis** se refiere a un máximo de seis videos nuevos por envío, no a seis grupos de muchos vídeos. En condiciones normales, no enviar la tanda siguiente mientras haya un trabajo activo del lote anterior. **Excepción documentada por el usuario (14/11/2026):** el trabajo que lleva demasiado tiempo en `waiting` queda apartado de la cola operativa y deja de bloquear nuevos lotes; no debe reenviarse ni contarse como terminado. Esta excepción no equivale a cancelarlo en Higgsfield.
- Estado de proveedor 2026-11-14: el trabajo `fb60ee94-03e8-463c-b477-8897b34fc885` para `sentadilla_bilateral_v01` continúa `waiting` y se apartó de la tanda operativa para reanudar producción con **otros IDs**. La solicitud para seis variantes con Seedance 2.0 fue rechazada con HTTP 429: 0/6 trabajos aceptados. **No gastar créditos de pago, no repetir el trabajo apartado ni intentar eludir límites del proveedor.**
- La automatización del chat comprobará resultados una vez por hora, frecuencia máxima admitida. No equivale a un proceso permanentemente ejecutado en GitHub.
- Cuando el proveedor permita retomar, enviar solo una tanda de seis; almacenar por ID único el `job_id` y el URL binario real; el modelo generado por IA requiere revisión visual y de técnica.

## Integración progresiva Android

El catálogo usa el vínculo permanente `exercise_mp4/v1/<id>.mp4`, exclusivo de cada ejercicio. No usar nombres similares ni rutas aleatorias.

1. Validar que el ID esté en el catálogo y que la animación sea realmente esa variante.
2. Convertir a MP4 H.264, **640×360, 25fps y sin audio**, sin estiramiento ni desenfoque artificial.
3. Registrar SHA-256, bytes, ID, proveedor, job_id, validación y estado de revisión.
4. Importar como **debug** usando `python3 tools/import_exercise_mp4_batch.py lote.zip --mode debug`, que bloquea automáticamente IDs inexistentes, hashes incorrectos y MP4 duplicados.
5. Para **release**, exigir aprobación individual de la técnica, derechos visuales y respaldo en ficha. Un asset descargado no es una demostración aprobada.
6. Para mantener pequeña la descarga del APK, organizar los vídeos aprobados como paquetes offline por categorías y actualizar el reproductor Android antes de declarar que la APK ya incluye todos los recursos.

Existe un paquete comprobado de **cinco prototipos descargables en la conversación** con manifest ajustado a `import_exercise_mp4_batch.py`, pero sus bytes siguen fuera del repositorio: `Asterion-Lote-5-Importable-Debug-640x360.zip`. La ruta GitHub solo contiene las asociaciones y herramientas, no los archivos de vídeo.

### Control de honestidad

- Solicitudes preparadas: 2.104.
- Trabajos Higgsfield aceptados todavía en espera remota: 1 (apartado de la cola operativa, no cancelado en el proveedor).
- Trabajos de una tanda de seis rechazada (429): 6.
- MP4 nuevos terminados en Higgsfield en esta fase: 0.
- MP4 incorporados por Higgsfield a la APK: 0.
- Prototipos MP4 anteriores disponibles fuera del repositorio: 5.
- Prototipos aprobados técnicamente: 0.

El seguimiento canónico está en `docs/datos/higgsfield_registry_v1.json`; los logs de GitHub issue #43 registran incidencias. **Nuevo criterio:** la automatización puede intentar hasta seis variantes distintas que no tengan `job_id` activo ni estén excluidas; ante HTTP 429 se detendrá ese intento y conservará la programación horaria. Si el proveedor no permite iniciar videos, no se declarará avance.