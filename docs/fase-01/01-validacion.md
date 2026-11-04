# Fase 01 — Control de calidad

## Matriz de aceptación

| Criterio | Evidencia exigida | Estado actual |
|---|---|---|
| Repositorio público | Repositorio GitHub accesible | Creado con punto final en el nombre |
| Gradle/Android | Archivos de configuración coherentes | Verificación estática |
| Pantalla mínima | MainActivity, XML y manifiesto | Código publicado; ejecución sin verificar |
| Prueba JVM | `testDebugUnitTest` verde | Pendiente del workflow |
| Lint | `lintDebug` verde | Pendiente del workflow |
| APK debug | `assembleDebug` verde | Pendiente del workflow |
| Firma debug | `apksigner verify` | Pendiente del workflow |
| Integridad de fuentes | Archivos y estructura verificados | Publicados |
| Alcance | Ausencia de fases posteriores | Revisión estática |

## Limitaciones

En el entorno de preparación no estaban instalados Android SDK ni Gradle. Por ello no se ejecutaron la compilación Android, las pruebas JVM, lint ni un emulador.

Los resultados del workflow GitHub Actions se comprobarán por separado. Una configuración correcta no asegura una compilación exitosa.

## Criterio de cierre

La fase 01 se declarará terminada cuando el nombre del repositorio quede corregido, el workflow haya compilado una APK debug y se verifiquen las pruebas, lint y firma. Los errores se tratarán dentro del alcance de esta fase.
