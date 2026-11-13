# Guía de desacoplamiento, adaptación y retirada de la UI

## Componentes de origen

| Original | Papel en la app Asterion | Tratamiento |
|---|---|---|
| `progress-engine/.../exercises/*.java` | catálogo, familias/tipos/variantes, categorías | **Extraído** como clases Java puras |
| `progress-engine/.../progression/VolumeProgressionEngine.java` | cálculo de referencia por historial real | **Extraído** |
| `.../ExerciseVolumeProfileCatalog.java` | perfil por ID y validación de TSV | **Extraído** |
| `.../VolumeResetPolicy.java` | reinicio numérico en hitos 7/14/21 | **Extraído** |
| `.../TechnicalCheckpointPolicy.java`, `.../WeeklyCategoryChallengePolicy.java` | revisiones de categoría | **Extraído** |
| `progress-engine/src/main/resources/asterion_exercises/v1` | TSV de taxonomía, intensidad, deportes y volumen | **Copia versionada** |
| `app/VolumeProgressStore.java` | historial en SharedPreferences, fechas de reinicio | **No copiado**; implementar adaptador propio |
| `app/data/AssetExerciseCatalogRepository.java`, `AssetExerciseVolumeProfileRepository.java` | lectura desde Android assets | **Sustituidos** por `PortableTrainingModule` |
| `app/MissionsActivity.java`, `RoutinesActivity.java`, `RoutineStepActivity.java` | interfaz, planificación y registro | **No copiadas** |
| `app/ProfileRegistrationSteps.java`, `ProfileStore.java` | onboarding del usuario | **No copiados** |

## Contratos al adaptar a otra aplicación

- **Catálogo:** conservar la relación family_id → type_id → variant_id;
  no reutilizar IDs para ejercicios diferentes. Las categorías del motor
  son cinco y los códigos históricos se normalizan. Personalizar únicamente
  la etiqueta de presentación sin modificar IDs.
- **Perfil inicial:** elegir categorías, nivel, actividades por día, zona horaria
  y preferencias de accesibilidad. No activar cálculos de volumen por defecto.
- **Registro:** persistir por usuario y variante fecha local, categoría,
  tipo, nivel, unidad REPETITIONS/SECONDS, series, cantidad, esfuerzo,
  completado/interrumpido y fecha de inicio de bloque. No inferir sesiones.
- **Progresión:** llamar `review` solo con datos reales y del bloque vigente.
  `Suggestion.requiresConsent()` requiere aprobación para incrementos;
  cualquier propuesta siempre es opcional.
- **Revisiones:** a los 7/14/21 días aplicar `VolumeResetPolicy.evaluate`
  tras la elección real de variantes; guardar nuevas fechas de bloque sin
  borrar los registros anteriores. No cruzar categorías, ni unidades,
  ni transferir cantidades a ejercicios diferentes.
- **Pruebas:** cargar 1.257 perfiles, comprobar nombres de categorías,
  reinicios, pausas, accesibilidad y migración de preferencias.

## Plan para retirar del Asterion original

No borrar todavía los motores originales: `MissionsActivity` y
`RoutineStepActivity` dependen de ellos para la biblioteca, la selección y
la persistencia. Primero portar la interfaz o sustituir sus referencias.
La retirada debe ser una **PR independiente**, con las nuevas pantallas y
pruebas de regresión Android. Así la APK actual no pierde funcionalidad
ni datos sin migración.

Esta rama sirve para reutilización y documentación. No fusiona nada a main.
