# Fase 16.4 — Intento de compilación debug autorizada: resultado y bloqueos

**Fecha:** 2026-11-05  
**Repositorio:** `miguelpinto9988-byte/Asterion-System-Android`  
**Rama:** `main`  
**Variante autorizada:** `debug` exclusivamente

## Autorización expresa recibida

El propietario autorizó específicamente:

- Primera compilación `APK debug`.
- Pruebas JUnit y lint de Gradle necesarias.
- Corrección de errores de compilación dentro del alcance autorizado.
- Verificación del artefacto y documentación de resultados.

No se autorizó `release`, firma de distribución, activación automática del CI ni ninguna función de una fase posterior.

## Resultado real

**La compilación no pudo iniciarse en esta sesión y no se generó una APK de Asterion System.**

### Bloqueos comprobados

1. **Entorno de ejecución:** JDK local `openjdk 21` (el proyecto fija Java 17); no hay comando `gradle`, `sdkmanager`, `adb` ni variables configuradas `ANDROID_HOME` o `ANDROID_SDK_ROOT`. Sin Android SDK ni Gradle no puede compilarse el módulo `:app` ni ejecutarse `lintDebug`.
2. **Red:** la conexión saliente del contenedor no resuelve los hosts de descarga de Google/Gradle/GitHub. No es viable instalar ahora el SDK o la distribución Gradle desde el contenedor.
3. **Acceso GitHub:** la conexión de repositorio permite leer/modificar archivos, consultar ejecuciones y artefactos, pero **no dispone de una acción para crear un nuevo evento `workflow_dispatch`**. No se ejecutó una compilación remota.
4. **Ejecuciones previas:** la API de Actions mostró dos ejecuciones anteriores del **4 de noviembre de 2026**, sobre commits antiguos; ninguna corresponde a la integración visual y el commit de preparación actual. Sus resultados **no** validan la versión actual.

### Modificaciones preparadas para el workflow manual

El archivo `.github/workflows/android-ci.yml` mantiene como único disparador:

```yaml
on:
  workflow_dispatch:
```

La secuencia de Gradle preparada es:

```sh
gradle --no-daemon --stacktrace :core:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

En éxito, el workflow verifica el archivo `app/build/outputs/apk/debug/app-debug.apk` mediante:

- Comprobación de archivo no vacío.
- `apksigner verify --verbose --print-certs`.
- `aapt dump badging` y comprobación del identificador `pe.asterionsystem.app`, `versionCode=1`, `versionName=0.1.0`.
- Tamaño del archivo y SHA-256.
- Subida del **APK debug** como artefacto `asterion-fase16-debug-apk` durante siete días.

El workflow no incorpora tareas release, configuración de firma de distribución ni disparadores `push`, `pull_request` o `schedule`.

## Revisión complementaria de recursos, antes de compilar

Se consultaron las 11 pantallas XML y los recursos base del repositorio en la rama `main`. No se identificaron referencias inexistentes en los atributos `@string`, `@color` o `@drawable` examinados. Se comprobaron también las referencias `R.id` utilizadas por `MainActivity`, `NavigationRenderer`, `CatalogPresenter`, `SchedulePresenter`, `ProgressPresenter`, `SettingsPresenter` y `LocalStatusPresenter` contra los IDs declarados en los XML correspondientes: no hubo faltantes. Se verificó además que SQLite sigue en versión 5, las excepciones de recuperación son no destructivas y los patrones de exclusión de archivos APK y claves están presentes en `.gitignore`.

Estas comprobaciones **no usan `aapt2`**, por lo que no garantizan una compilación sin errores. El workflow manual añade la ejecución de `tests/phase16_preflight_static.py` y `tests/phase03_visual_source_contract.py` antes de las tareas Gradle; esas pruebas Python están **preparadas, no ejecutadas** en el checkout completo.

## Primera ejecución real del workflow — 5 de noviembre de 2026

- **Run ID:** [42091749135](https://github.com/miguelpinto9988-byte/Asterion-System-Android/actions/runs/42091749135).
- **Commit ejecutado:** `7da6b9af3e9b32a1d6dbe6f3328a4f6b77f5037a`.
- **Evento:** `workflow_dispatch` iniciado manualmente por el propietario.
- **Estado final:** `failure`.
- **Preparación JDK 17, Gradle 8.13 y SDK Android 36:** completada.
- **Verificaciones estáticas fase 03 y 16:** superadas.
- **Tarea `:core:test`:** 58 pruebas, **57 superadas y 1 fallida**.
- **Prueba fallida:** `SchedulePlannerBulkTitlesTest.emptyDayProducesNoListings`; el doble de prueba reclamaba tres IDs al resolver un día vacío.
- **Tareas `:app:testDebugUnitTest`, `:app:lintDebug`, `:app:assembleDebug`:** no ejecutadas debido a la detención en `:core:test`.
- **Verificación de APK / artifact:** omitidas; ningún APK generado.

### Corrección acotada tras la ejecución fallida

Se corrigieron únicamente `SchedulePlanner.listWithTitles`, su prueba JUnit y el comando de diagnóstico del workflow:

1. Para un día sin actividades, `listWithTitles` devuelve la lista vacía sin consultar títulos al catálogo.
2. La prueba exige ahora **cero consultas** de catálogo, tanto agrupadas como individuales, para ese caso.
3. Se incorpora `--continue` a Gradle para que futuras ejecuciones intenten ejecutar las demás tareas independientes aunque alguna prueba falle; **Gradle seguirá devolviendo error si hay fallos**, y la verificación/subida de la APK permanecerán bloqueadas hasta el éxito del paso completo.

**Importante:** estas correcciones todavía no se han validado mediante una segunda ejecución Gradle. No se modifica ninguna regla de negocio, firma de release, SQLite, Android SDK ni disparadores automáticos.

## Procedimiento necesario para completar la compilación

1. Abrir [GitHub Actions del repositorio](https://github.com/miguelpinto9988-byte/Asterion-System-Android/actions/workflows/android-ci.yml).
2. Seleccionar el workflow **Asterion Android — debug manual**, rama **main**.
3. Pulsar **Run workflow** manualmente. Este paso no puede realizarse desde la conexión disponible aquí.
4. Consultar los pasos `:core:test`, `:app:testDebugUnitTest`, `:app:lintDebug` y `:app:assembleDebug` del job.
5. En caso de error, revisar los logs y corregir los archivos implicados; repetir el workflow solamente mediante ejecución manual.
6. Si se completa, revisar `apksigner`, `aapt`, el SHA-256 y descargar el archivo ZIP del artefacto `asterion-fase16-debug-apk`. Validar e instalar la APK solo con un dispositivo de pruebas autorizado.

## Tabla de evidencias

| Elemento | Resultado |
|---|---|
| Autorización debug | **Confirmada** |
| Workflow solo manual | **Conservado** |
| Prueba `:core:test` en CI | **Preparada, no ejecutada** |
| JUnit Android `:app:testDebugUnitTest` | **Preparada, no ejecutada** |
| `:app:lintDebug` | **Preparada, no ejecutada** |
| `:app:assembleDebug` | **Preparada, no ejecutada** |
| APK generado para commit actual | **No** |
| Firma debug verificada | **Pendiente** |
| Identificador / versión reales de APK | **Pendiente** |
| SHA-256 de APK | **Pendiente** |
| Entrega de APK | **Pendiente** |

## Recomendaciones

- Utilizar una ejecución manual de GitHub Actions en un runner con Gradle 8.13, JDK 17 y Android SDK 36 para obtener evidencia real sin habilitar compilación automática.
- Resolver los errores **que aparezcan en logs reales**, sin modificaciones especulativas fuera del alcance.
- Mantener la firma release separada; no subir claves o contraseñas al repositorio.

**No debe afirmarse que la APK compila, que pasó lint o que es instalable hasta que existan los logs y el artefacto verificable.**
