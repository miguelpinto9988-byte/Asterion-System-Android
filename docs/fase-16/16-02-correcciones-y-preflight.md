# Fase 16.2 — Correcciones previas y comprobación estática

## Corrección aplicada

- **`MainActivity.configureSafeSystemInsets()`:** en API 35 o superior, utiliza `View.setOnApplyWindowInsetsListener` y `WindowInsets.Type.systemBars() | displayCutout()` para aplicar padding de seguridad al contenedor principal. Las API inferiores siguen usando el ajuste normal del sistema; el método no se ejecuta en esas versiones.
- **`activity_main.xml`:** se eliminó `android:fitsSystemWindows="true"` del contenedor raíz para evitar aplicación redundante de márgenes de sistema.
- La implementación no introduce AndroidX, Compose, WebView, sincronización, servicios, permisos, recursos gráficos ni cambios de base SQLite.

**Estado de la corrección:** inspeccionada como código fuente; **todavía no compilada ni validada en dispositivos**. Las interacciones con teclado, recortes de pantalla y barras transitorias deben comprobarse después de una autorización.

## Verificador agregado

`tests/phase16_preflight_static.py` opera con **solo Python estándar**. Comprueba:

1. Módulos `:app` / `:core`, AGP 8.13.2, SDK 36, minSdk 26 y Java 17.
2. Manifiesto Android: launcher, exportación explícita, backup deshabilitado y ausencia de `INTERNET`.
3. Referencias estáticas de recursos XML, IDs y `R.*` en código Java, y archivos XML bien formados.
4. Contrato de `WindowInsets` y ausencia de `fitsSystemWindows` duplicado.
5. SQLite v5, configuración privada de preferencias, manejo no destructivo de corrupción.
6. Ausencia de HTML/JS en el código Android, APK, AAB o ficheros de firma.
7. Workflow `.github/workflows/android-ci.yml` solo manual, Gradle 8.13 fijado.

Ejecución **posterior permitida sin compilación Android**, tras obtener un checkout limpio:
```sh
python3 tests/phase16_preflight_static.py
```

El comando no invoca Gradle, Java, el SDK de Android ni crea artefactos. El script advierte si falta el wrapper sin modificarlo.

## Evidencia y limitaciones de esta entrega

Se revisaron directamente el manifiesto, los scripts de Gradle, el workflow, el contenedor XML, `MainActivity`, los contratos del catálogo, los repositorios SQLite y los archivos de recursos relevantes. La documentación oficial confirma los requisitos de AGP 8.13.

**No se ejecutó el nuevo script sobre un checkout completo en esta sesión.** Se prepara para ejecución reproducible en un entorno que disponga de los archivos del repositorio. Una revisión estática no demuestra que Android sea compilable: quedan pendientes `aapt`, Gradle, JUnit Android, lint y pruebas de dispositivos.

## Riesgos restantes

- Falta confirmar que todos los recursos y referencias sean reconocidos por `aapt2`.
- El tratamiento de insets API 35+ debe medirse visualmente y con teclado, fuentes grandes y navegación por gestos.
- No se modificó ni validó firma release ni versión destinada a distribución.
- No se ha ejecutado análisis de dependencias ni auditoría dinámica de seguridad.
- Las bases SQLite ya instaladas deben preservarse durante pruebas autorizadas; nunca limpiar datos automáticamente.
