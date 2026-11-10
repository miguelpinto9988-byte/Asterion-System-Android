# Propuesta de mejora — variedad funcional de misiones diarias

**Rama de trabajo:** `feature/misiones-variedad-muscular-20261110`.
**Estado:** experimental; pendiente de revisión de PR, resultados CI y prueba manual en teléfono.
**Base:** `main` en `b731cb2939468e246c54d4491e3d36af9db54b26`.

## 1. Objetivos y límites
Mejorar la diversidad sin prescribir cargas ni provocar más ejercicio:
- Mantener las propuestas idénticas durante un mismo día mientras no cambie la configuración del perfil.
- Preferir variantes **no completadas** en los **14 días anteriores**, exclusivamente dentro de la misma categoría, cuando existan alternativas compatibles. Si la biblioteca compatible es pequeña, permitir repetir.
- Diferenciar el foco corporal predominante (y secundario) por familia con metadatos editoriales no clínicos.
- Mostrar un resumen de variedad por categoría sobre marcas de ejercicios **realizados**, no sobre propuestas de la aplicación.

**No** cambian la duración, el esfuerzo permitido, las series, las repeticiones, los pesos, las categorías del calendario, el consentimiento Monarca, el catálogo de 817 variantes, los IDs existentes, SQLite ni los recursos oficiales.

## 2. Filtro de seguridad antes del sorteo
Se conservan: categoría programada, experiencia y complejidad técnica, equipo disponible, preferencias de intensidad, supervisión, condiciones de descanso, pausas por molestias y recomendaciones de recuperación. La función de variedad no habilita una variante previamente descartada por estos filtros.

La selección es **pseudoaleatoria reproducible**, no azar criptográfico ni IA generativa: la fecha local y la firma de preferencias del perfil producen una semilla. Los candidatos elegibles se mezclan de manera estable. El selector:
1. Prioriza alternativas compatibles no completadas durante los 14 días anteriores.
2. Distribuye las propuestas entre focos funcionales diferentes.
3. Si faltan opciones, diversifica por familia y por tipo de movimiento.
4. Solo cuando faltan opciones no repetidas, reutiliza candidatos previamente completados.
5. No duplica una variante dentro de una propuesta.

La lista diaria de **solo IDs** sigue en `DailyMissionStore`. Se mantiene estable al cambiar de pantalla, cerrar la app o marcar un ejercicio realizado el mismo día. Cambia con la fecha local o preferencias relevantes.

## 3. Clasificación de focos
`ExerciseMuscleFocus.java` contiene **75 entradas explícitas** (una por familia del catálogo). Las etiquetas expresan un **foco corporal o funcional orientativo** y un foco de apoyo. No son una evaluación biomecánica individual; la musculatura efectiva puede variar según la técnica y circunstancias. Para familias de movilidad o cardio se utiliza lenguaje funcional en lugar de inventar músculos precisos.

No se modifica el formato TSV original ni los IDs existentes. Un test compara la cobertura de familias con `families.tsv`. Las filas sin clasificación en futuras ampliaciones tienen un marcador neutral y requieren revisión editorial antes de publicarse.

## 4. Historial de variedad por categoría
Se reutilizan las marcas existentes en `MissionTrackingStore`:
- `recentCompletedForCategory`: hasta 14 días previos de IDs únicos completados, sin incluir hoy y sin contaminación entre categorías.
- `recentDiversityForCategory`: resumen de los 14 días más recientes, incluyendo hoy: días con registros, marcas de finalización, variantes distintas y focos funcionales distintos.

El panel «Progreso y evolución» presenta los resúmenes de las cinco categorías. **Sin nuevo almacén de propuestas ni duplicación del historial**. Si la persona no marca un ejercicio como realizado, ese ejercicio no se considera completado a efectos de rotación; puede reaparecer otro día.

## 5. Pruebas
- JVM: nuevos casos de cobertura de las 75 familias, evitar repeticiones recientes cuando existe alternativa compatible, fallback sin duplicaciones, aislamiento entre categorías, orden de historial independiente, restauración del mismo día.
- Contrato estático: `tests/mission_diversity_contract.py`.
- Instrumentación Android real, en emulador CI aislado: `DailyMissionPersistenceTest`.
  - Primera invocación: guarda la ficha de IDs y una marca de finalización de la víspera.
  - Se hace `adb force-stop` de la aplicación entre invocaciones.
  - Segunda invocación: verifica que la ficha y el historial sobreviven al reinicio, y que **inyectar como entrada** la fecha siguiente o un perfil diferente invalida la ficha anterior.
  - **Limitación:** no cambia el reloj real del dispositivo, ni reemplaza una prueba manual de cambio de medianoche en teléfono.
- CI debug de PR: compila `:app:assembleDebugAndroidTest` y ejecuta ambos tramos en Android 16, además de pruebas JVM, lint y APK de desarrollo.

## 6. Pendientes de aceptación
- Revisar la calidad editorial de los focos primarios/secundarios con especialistas en ejercicio si se publicarán como información educativa.
- Validar pantallas en teléfonos de tamaños diferentes y funcionamiento al atravesar la medianoche real.
- Verificar que los nuevos resúmenes no se interpreten como metas obligatorias, rachas o penalizaciones.
- Confirmar visualmente los textos en MagicPath cuando vuelva a permitir llamadas de la cuenta.

## Handoff MagicPath (sin ejecutar por cuota externa)
Actualizar la vista de Misiones para añadir un subtítulo «Foco orientativo» bajo cada actividad y el panel Progreso con una tarjeta de «Variedad observada en 14 días» por categoría (días registrados, variantes distintas, focos funcionales). No mostrar rachas, porcentajes de cumplimiento obligatorios ni mensajes que presionen a aumentar carga. Mantener exactamente la identidad visual, los temas Blanco/Negro del registro y las pantallas de carga oficiales. No simular sincronización: MagicPath es un prototipo visual y GitHub contiene la lógica Android.
