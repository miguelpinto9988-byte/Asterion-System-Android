# Fase 04 — Validación y limitaciones

## Verificaciones realizadas sin APK

| Verificación | Resultado | Método |
|---|---|---|
| Sintaxis de los cuatro XML nuevos/modificados | Correcto | Analizador XML local |
| Código Java nuevo de `:core` | Correcto | `javac --release 17` con contratos de fase 02 |
| Integración de firmas Java de `:app` | Correcto en entorno simulado | `javac --release 17` con dobles mínimos de API Android |
| Navegación e invariantes | 21 comprobaciones superadas | Programa Java local, sin Android |
| Adaptador de Bundle | 3 comprobaciones superadas | Doble local de `android.os.Bundle` |
| Métodos de las pruebas nuevas | 7 superados | Compilación e invocación con sustitutos mínimos de JUnit |

**Importante:** los dobles de Android y JUnit solamente verifican firmas y lógica simple; no sustituyen Android SDK, JUnit real ni una ejecución de aplicación. El proyecto no se ha compilado con Gradle en esta fase, no se han ejecutado pruebas Android y **no se ha generado ninguna APK**.

## Verificaciones pendientes hasta nueva autorización

- Compilar Android y confirmar resolución real de recursos, compatibilidad de Gradle y comportamiento de `OnBackInvokedCallback`.
- Probar Atrás en Android 8–16, recreación por rotación y restauración tras proceso finalizado.
- Revisar accesibilidad, distintas densidades y comportamiento de los insets del sistema.
- Ejecutar JUnit real mediante Gradle sin ejecutar `assembleDebug`, solo si el propietario autoriza la validación necesaria.

## Política GitHub Actions

El workflow `.github/workflows/android-ci.yml` conserva exclusivamente `workflow_dispatch`. No se cambia ni activa compilación automática. La publicación de código no ejecuta generación de APK.

## Criterios de aceptación de esta fase

- Navegación encapsulada en Java puro.
- XML como única forma de definir las pantallas.
- Retroceso y estado temporal conectados al ciclo de vida.
- Sin motores funcionales de reglas, persistencia SQLite ni ajustes visuales definitivos.
- Pruebas estáticas y de Java independientes; limitaciones de prueba Android transparentes.
