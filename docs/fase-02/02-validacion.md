# Fase 02 — Informe de verificación

## Alcance de pruebas realizadas

1. Compilación **exclusiva de fuentes Java** del módulo `:core` y de las clases `ApplicationCoordinator`, `LifecycleChangedEvent` y `AndroidLifecycleAdapter` con `javac --release 17` en entorno local: correcta.
2. Prueba independiente ejecutada con `java -ea`: entrega de eventos tipados, orden de suscriptores, cierre idempotente, errores de listeners, rechazo de eventos nulos, actualización de estado, traducción del ciclo de vida e igualdad/validación de identificadores: correcta.
3. Pruebas JUnit agregadas al código, **pendientes de ejecución con Gradle**. No se afirma que hayan sido ejecutadas.
4. `activity_main.xml` se conserva sin cambios; las modificaciones de Java no crean pantallas nuevas.
5. El workflow de GitHub Actions debe permanecer con activación **solo manual**.

## No realizado, por instrucción expresa

- No se invocó `assembleDebug`, `assembleRelease`, `apksigner`, ni una compilación Gradle Android.
- No se generó APK, AAB, firma ni artefacto Android nuevo.
- No se ejecutaron instrumentación, emulador, visualizador Android Studio ni pruebas sobre hardware.

## Criterios de aceptación de arquitectura

| Criterio | Revisión |
|---|---|
| Dos módulos (`:app` / `:core`) con dependencia unidireccional | Verificar `settings.gradle` y `app/build.gradle` |
| Núcleo Java sin APIs Android | Inspeccionar imports en `core/src/main/java` |
| Eventos desacoplados, tipados y cancelables | Revisar pruebas `InMemoryEventBusTest` |
| Estado y ciclo de vida estructurales | Revisar `InMemoryStateStore` y adaptador |
| Puertos para casos de uso, motores, reloj y datos | Confirmar presencia de contratos |
| XML nativo sin cambios | Verificar SHA o diff de `activity_main.xml` |
| Sin funciones de fases 03–14 | Revisar árbol de código y especificación |
| Sin compilación automática | Confirmar `workflow_dispatch` como único disparador CI |

## Revisión pendiente

Las pruebas JUnit Android locales y la validación integral de los módulos mediante Gradle quedan pendientes hasta una autorización apropiada para ejecutarlas sin generar APK o hasta la fase de pruebas autorizada. Ningún resultado se extrapola a rendimiento en dispositivos.
