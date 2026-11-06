# Asterion System — Android nativo Java

**Estado:** implementadas fases 00, 01, 02, 04, 05, 06, 07, 08, 09, 11, 14 y **15 (optimización, seguridad y accesibilidad)**. La fase 03 de diseño definitivo sigue pendiente.

Proyecto Java nativo Android con vistas XML. El HTML se utiliza **solo como previsualización externa**. No se han incorporado HTML, JavaScript, CSS, WebView o Compose a las pantallas Android.

## Motor modular de progreso (bloque independiente)

Se creó el módulo **`:progress-engine`**, una biblioteca Java 17 sin Android ni SQLite, conectada por interfaces a los servicios actuales de `:core`. Su fachada `AsterionDomainEngine` coordina lectura del catálogo de actividades, planificación, preferencias y seguimiento. `ProgressPatternEngine` permite evaluar patrones intercambiables; `standard_v1` conserva el porcentaje actual y `RuleBasedProgressPattern` permite reutilizar el motor determinista de reglas de fase 06.

La evaluación por patrones es **de solo lectura**; la creación de planes y el registro de resultados requieren llamadas explícitas. Los parámetros de futuros patrones están validados pero aún **no se guardan** en SQLite ni aparecen en las pantallas. La app Android continúa mostrando el progreso actual hasta que se apruebe la integración funcional de los nuevos patrones.

- [Arquitectura, contratos y extensión futura](docs/motor-progreso/01-motor-modular.md)
- Pruebas JVM: `gradle :progress-engine:test`, incorporadas al workflow CI de debug para cada publicación en `main` (también disponible de forma manual). La ejecución del nuevo módulo debe verificarse en los registros de GitHub Actions.

## Arquitectura

- `:core`: Java puro; dominio, eventos, estado, navegación, reglas, catálogo, planificación, seguimiento, preferencias y diagnósticos locales.
- `:app`: `MainActivity`, presentadores, pantallas XML, SQLiteOpenHelper, repositorios SQLite, diagnósticos y `SharedPreferences` para configuración.
- SQLite `asterion_internal.db`: esquema v5, con migraciones acumulativas hasta 4→5, sin borrar registros anteriores.
- `docs/fase-08/`: especificaciones y pruebas de planificación.

## Fase 08

El planificador permite programar **actividades del catálogo** con fecha civil local, hora inicial y duración de 1–720 minutos sin cruzar medianoche. Un servicio Java y cuatro triggers SQLite previenen entradas superpuestas, incluidos conflictos durante escrituras concurrentes. Las pantallas `screen_plan.xml` y `screen_plan_form.xml` permiten consultar la semana, seleccionar días, crear, editar y eliminar programaciones. Ninguna programación ejecuta automáticamente una actividad ni produce recordatorios.

La previsualización HTML (entregada fuera de la app) simula ese comportamiento con almacenamiento del navegador; no equivale a la ejecución Android.

## Fase 09 — Seguimiento y progreso

El seguimiento permite registrar explícitamente `realizada` u `omitida` para una actividad programada, incluir una observación corta y devolverla a `pendiente` borrando únicamente el resultado. `Pendiente` es ausencia de registro, no un incumplimiento automático. `ProgressSummary` calcula los indicadores semanales sobre las actividades programadas; el porcentaje de cumplimiento divide las realizadas entre las evaluadas (realizadas + omitidas), y muestra «Sin resultados evaluados» cuando corresponda.

La base de datos usa SQLite v5 y una única tabla adicional `tracking_results` con clave foránea y borrado en cascada para evitar resultados huérfanos. La pantalla diaria y el resumen semanal están en `screen_progress.xml` y `screen_progress_summary.xml`, manejados en Java. La previsualización HTML independiente incluye estos comportamientos, pero solo guarda datos simulados en el navegador.

## Fase 11 — Personalización local

La pantalla Android `screen_settings.xml` incorpora exactamente tres preferencias:
- **Texto:** normal o grande (escala aproximada de 1,18 en textos de pantallas XML).
- **Listas:** densidad normal o compacta en filas de catálogo, planificador y seguimiento.
- **Sección inicial:** Inicio, Catálogo, Planificador o Seguimiento.

Las preferencias se guardan en `SharedPreferences`, archivo privado `asterion_preferences_v1`, mediante `AndroidPreferencesRepository` en `:app`. Los modelos `AppPreferences`, `PreferencesRepository` y `PreferencesService` permanecen en Java puro (`:core`). El usuario puede restablecer únicamente preferencias, sin borrar SQLite.

La sección inicial se aplica exclusivamente en un **arranque sin estado guardado**; al restaurar una Activity existente, la navegación guardada siempre tiene prioridad. La preferencia de inicio no activa actividades ni notificaciones.

La previsualización HTML externa de fase 11 permite probar estas opciones y conserva la navegación de las fases previas. Es una **simulación con localStorage**, no un WebView ni una conexión con SharedPreferences Android.

## Fase 14 — Offline-first e integridad local

Las funciones aprobadas siguen siendo locales y no requieren conexión a internet. La nueva pantalla nativa `screen_local_status.xml` ejecuta una inspección **de solo lectura** de SQLite (`PRAGMA quick_check(1)`, `PRAGMA foreign_key_check` y recuentos de registros), fuera del hilo principal. El manejador explícito `NonDestructiveDatabaseErrorHandler` evita la eliminación automática de la base por el manejador predeterminado de corrupción; no se repara ni borra la base automáticamente.

Catálogo, Planificador y Seguimiento incorporan reintentos manuales de consulta y mensajes controlados para problemas de almacenamiento, espacio o integridad; **las escrituras fallidas no se repiten automáticamente**. Se conservan el esquema SQLite v5, las transacciones y claves foráneas de las fases anteriores. No se han añadido permisos de internet, cuentas, red ni sincronización.

Solo se documentaron los contratos hipotéticos de sincronización futura en `docs/fase-14/14-contratos-sincronizacion-futura.md`; **no se implementó** sincronización ni transporte de datos. La previsualización HTML externa permite simular desconexión, acceso fallido, espacio insuficiente, advertencia y reintentos sin afectar a datos de Android.

Comprobaciones de fuente: `python3 tests/phase14_offline_static_smoke.py`. Pruebas SQLite de escritorio desde las constantes Java: `python3 tests/phase14_sqlite_integrity_smoke.py`. Prueba JVM solo Java puro: `sh tests/phase14_core_jvm_smoke.sh`. Ninguno de esos comandos genera APK. La verificación independiente del núcleo mediante `javac --release 17` superó 13 aserciones, y SQLite de escritorio superó 6 pruebas con un esquema equivalente; la ejecución de los scripts de comprobación contra el checkout completo sigue pendiente.

## Fase 15 — Optimización, integridad y accesibilidad

- **Planificador:** resuelve títulos del catálogo mediante `CatalogRepository.titlesById` y consultas SQLite por lotes de hasta 200 IDs, usando parámetros enlazados. Evita una consulta individual por cada programación.
- **Catálogo:** espera 280 ms tras la última edición del texto para ejecutar la búsqueda. Cancela solicitudes diferidas, invalida respuestas obsoletas y retira el `TextWatcher` al reemplazar la pantalla.
- **Diagnóstico:** verifica que las claves foráneas estén activas; obtiene los contadores de las tres tablas en una sentencia SQL consistente.
- **Seguridad:** conserva los permisos restringidos, `SharedPreferences` privado, SQLite v5, el manejo no destructivo de corrupción y las consultas parametrizadas.
- **Accesibilidad:** siete regiones de estado con avisos para lectores de pantalla, tres controles con `labelFor`, anuncios de día completo y fila compacta con altura mínima 48 dp.

Pruebas **sin Android** efectuadas: previsualización HTML externa con 36 verificaciones Chromium y cinco ensayos equivalentes SQLite de escritorio con el esquema de consultas por lotes. Se añadieron pruebas estáticas Python y fuente JUnit para una ejecución posterior autorizada. No constituyen pruebas de una APK Android real.

## Fase 03 — Integración visual nativa de fantasía oscura

Se incorporó al código fuente una primera versión del sistema visual oscuro de Asterion System, basado en la previsualización revisada: paleta azul-violeta, fondos degradados, botones de estados accesibles, tipografía serif para encabezados y controles táctiles de 48dp. Se conservaron todas las rutas y funcionalidades Java/XML existentes.

La integración incorpora **ilustraciones vectoriales originales** del Guardián de Asterion y un centinela de sombras, así como un emblema y cinco iconos nativos. No se usan personajes oficiales ni fuentes de terceros. El resultado sigue **pendiente de compilación y validación visual en Android real**.

- [Identidad y alcance](docs/fase-03/03-identidad-visual.md)
- [Verificaciones y pendientes](docs/fase-03/03-validacion-y-pendientes.md)
- [Prueba estática sin Android](tests/phase03_visual_source_contract.py)
- [Integración vectorial y componentes](docs/fase-03/03-integracion-vectorial.md)

## Fase 16 — Primera compilación debug autorizada y diagnóstico inicial

**La fase 16 cuenta ya con autorización expresa para la primera compilación APK `debug`, y para ejecutar las pruebas y lint de Gradle necesarios.** El **5 de noviembre de 2026** se inició [la primera ejecución manual de GitHub Actions](https://github.com/miguelpinto9988-byte/Asterion-System-Android/actions/runs/42091749135): 58 pruebas JUnit, 57 correctas y una fallida. El fallo de día vacío se corrigió en fuente y la segunda ejecución superó las pruebas `:core:test`; la tercera ejecución (`42132119288`) llegó hasta `assembleDebug`, pero falló en `lintDebug` por la navegación Atrás obsoleta. La navegación ha sido migrada a AndroidX y está pendiente de una cuarta ejecución manual. La versión `release` y la firma de distribución siguen excluidas.

- Se revisó AGP **8.13.2**, Gradle **8.13** en CI manual, JDK **17**, `minSdk 26` y `targetSdk/compileSdk 36`.
- Se preparó un ajuste de áreas de seguridad Android API 35+ en `MainActivity` y `activity_main.xml` para compatibilidad con edge-to-edge, **todavía no probado en Android**.
- Se añadió `tests/phase16_preflight_static.py`, que examina código, XML, manifiesto, privacidad y CI usando solamente Python. La verificación estática se superó en el primer workflow; Gradle se ejecutó, pero se detuvo en una prueba antes de generar una APK.
- No hay Gradle Wrapper versionado; el workflow CI instala Gradle 8.13. Release no tiene firma de distribución configurada.
- La variante `debug` está autorizada. El tercer intento completó Java, pruebas y `assembleDebug`, pero falló lint con 1 error y 58 advertencias. La corrección de navegación mediante AndroidX está preparada y necesita validación real.

Documentación:
- [Auditoría previa](docs/fase-16/16-01-auditoria-precompilacion.md)
- [Corrección y verificador estático](docs/fase-16/16-02-correcciones-y-preflight.md)
- [Empaquetado y permisos](docs/fase-16/16-03-empaquetado-y-permisos.md)
- [Estados de las siete etapas](docs/fase-16/16-estado-y-trazabilidad.md)

## Fase 16.4 — Tres ejecuciones: corregida la navegación Atrás para lint

Se autorizó compilar `debug` y ejecutar JUnit y lint de Gradle. El workflow `.github/workflows/android-ci.yml` ejecuta automáticamente la variante **debug** en cada `push` a `main` (y admite `workflow_dispatch` manual), con tareas `:core:test`, `:app:testDebugUnitTest`, `:app:lintDebug` y `:app:assembleDebug`, y validación posterior mediante `apksigner`, `aapt` y `sha256sum`.

La APK de la fase 16 superó pruebas, lint y validación en la [ejecución #42146520486](https://github.com/miguelpinto9988-byte/Asterion-System-Android/actions/runs/42146520486), correspondiente a una versión anterior al nuevo módulo de progreso. El nuevo módulo se comprobará mediante CI automático después de publicar sus cambios. Consulta el [informe de errores y correcciones](docs/fase-16/16-04-resultado-y-bloqueos.md).

## Compilación CI: modo automático aprobado

Por autorización expresa, cada `push` a `main` ejecuta las pruebas estáticas, JUnit de `:core` y `:progress-engine`, pruebas Android debug, `lintDebug`, `assembleDebug` y verificaciones del APK firmado de desarrollo. `workflow_dispatch` permanece disponible. **No se activa release, firma de distribución ni despliegue automático a dispositivos**. Los artefactos debug se conservan siete días.

Las verificaciones de las fases 08 y 09 incluyen SQLite de escritorio y previsualizaciones HTML interactivas; las pruebas Java JUnit permanecen preparadas, **sin ejecutarse por Gradle**. Para fase 09 se ejecutaron 23 comprobaciones de interacción con Chromium y 8 pruebas de restricciones equivalentes del nuevo esquema SQLite. No equivalen a pruebas Android nativas. La compilación y ejecución Android siguen pendientes.

Para comprobar archivos y referencias sin generar APK: `python3 tests/phase11_preferences_static_smoke.py`. Para una prueba JVM independiente, ejecutar `sh tests/phase11_jvm_smoke.sh`: compila exclusivamente tres clases Java de `:core` con `javac --release 17` y ejecuta 18 comprobaciones. Esta prueba local pasó durante el desarrollo, con hashes de archivos idénticos a los publicados. Las pruebas JUnit fuente permanecen disponibles, pero **no** se ejecutó Gradle, la compilación Android ni la generación de APK durante la fase 11.

## Documentación

- [Fase 15 — Auditoría y riesgos](docs/fase-15/15-auditoria-y-riesgos.md)
- [Fase 15 — Optimización e integridad](docs/fase-15/15-optimizacion-e-integridad.md)
- [Fase 15 — Seguridad y accesibilidad](docs/fase-15/15-seguridad-y-accesibilidad.md)
- [Fase 15 — Validación](docs/fase-15/15-validacion.md)
- [Fase 15 — Trazabilidad](docs/fase-15/15-trazabilidad.md)


- [Fase 14 — Auditoría offline](docs/fase-14/14-auditoria-offline.md)
- [Fase 14 — Integridad y recuperación](docs/fase-14/14-integridad-y-recuperacion.md)
- [Fase 14 — Contratos hipotéticos de sincronización](docs/fase-14/14-contratos-sincronizacion-futura.md)
- [Fase 14 — Validación](docs/fase-14/14-validacion.md)
- [Fase 14 — Trazabilidad](docs/fase-14/14-trazabilidad.md)


- [Fase 11 — Arquitectura y preferencias](docs/fase-11/11-arquitectura-preferencias.md)
- [Fase 11 — Validación y límites](docs/fase-11/11-validacion.md)
- [Fase 11 — Trazabilidad](docs/fase-11/11-trazabilidad.md)


- [Fase 09 — Arquitectura de seguimiento](docs/fase-09/09-arquitectura-seguimiento.md)
- [Fase 09 — SQLite y migración](docs/fase-09/09-sqlite-migracion.md)
- [Fase 09 — Validación y límites](docs/fase-09/09-validacion-y-limitaciones.md)
- [Fase 09 — Trazabilidad](docs/fase-09/09-trazabilidad.md)

- [Arquitectura de planificación](docs/fase-08/08-arquitectura.md)
- [Esquema y migración](docs/fase-08/08-esquema-y-migracion.md)
- [Validación y limitaciones](docs/fase-08/08-validacion.md)
- [Trazabilidad](docs/fase-08/08-trazabilidad.md)

## Fases

La fase 03 incorpora el diseño oscuro vectorial publicado. Las fases 14 y 15 están publicadas. La primera compilación `debug` de fase 16 está **autorizada, con un intento fallido y una corrección pendiente de nueva prueba**; la firma de distribución y la versión `release` no están autorizadas. Las fases 10, 12 y 13 permanecen excluidas.

El repositorio es público pero no se ha seleccionado licencia de reutilización; no incorporar contraseñas, certificados o datos sensibles.
