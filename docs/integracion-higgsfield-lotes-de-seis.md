# Flujo Asterion: tandas de exactamente seis y unión por ID

## Producción Higgsfield

- La cola contiene 2.104 ejercicios y variantes, con identificador único.
- **Lote de seis** se refiere a seis videos por envío, no a seis grupos de muchos vídeos. No enviar la tanda siguiente mientras haya un trabajo del lote anterior en espera.
- Estado de proveedor 2026-11-14: el trabajo `fb60ee94-03e8-463c-b477-8897b34fc885` para `sentadilla_bilateral_v01` continúa `waiting`. La solicitud para seis variantes con Seedance 2.0 fue rechazada con HTTP 429: 0/6 trabajos aceptados. **No gastar créditos de pago ni repetir trabajos en espera.**
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
- Trabajos Higgsfield aceptados y todavía pendientes: 1.
- Trabajos de una tanda de seis rechazada (429): 6.
- MP4 nuevos terminados en Higgsfield en esta fase: 0.
- MP4 incorporados por Higgsfield a la APK: 0.
- Prototipos MP4 anteriores disponibles fuera del repositorio: 5.
- Prototipos aprobados técnicamente: 0.

El seguimiento canónico está en `docs/datos/higgsfield_registry_v1.json`; los logs de GitHub issue #43 registran incidencias.