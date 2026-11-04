# Fase 06 — Control de calidad y limitaciones

## Validación ejecutada sin Android ni APK

- `javac --release 17` compiló los ocho archivos Java nuevos del motor junto a los puertos `RuleEvaluator`, `AppEvent`, `EventBus` y bus de eventos preexistentes.
- Diez métodos de prueba `@Test` verificaron: prioridades, traza estable, conflictos, desempate por ID, reglas deshabilitadas, contexto incompleto, límites de reglas, proveedor defectuoso, inmutabilidad, seis operadores, y publicación de eventos. Se ejecutaron con un runner JVM y **sustitutos mínimos de JUnit**, no con JUnit real ni Gradle.
- El HTML de previsualización incluye las dos pantallas XML existentes, la simulación CRUD de la fase 05 y el laboratorio de evaluación de esta fase.
- La sintaxis de los dos scripts JavaScript de la previsualización se comprobó con `node --check`, sin acceder a servidores externos.
- Se ejecutaron seis escenarios JavaScript del evaluador con un DOM simulado en Node.js. **No equivale a una prueba real en navegador**.

## Pendiente

- Ejecutar las pruebas JUnit oficiales con Gradle cuando el propietario autorice esa tarea; **no** se ejecutó Gradle en la fase 06.
- Compilar y ejecutar la aplicación Android **solo cuando se autorice expresamente generar APK**; esta fase no la generó.
- Una integración futura, sujeta a autorización, deberá cargar reglas desde una fuente persistente y conectarlas a pantallas XML. Actualmente no se invocan reglas desde `MainActivity`.
- El laboratorio HTML **reproduce** el algoritmo Java, pero no ejecuta Java en el navegador ni valida SQLite Android. Los resultados de las herramientas son ilustrativos, no salidas de una APK.
- Las pruebas automatizadas con navegador están bloqueadas por restricciones de acceso del entorno, así que no se reportan como superadas.

## Política de compilación

El workflow `.github/workflows/android-ci.yml` conserva solo `workflow_dispatch`, sin disparadores `push` ni `pull_request`. No se cambió para esta fase. La publicación de código no genera APK.

## Alcance

La fase incluye exclusivamente motor técnico, condiciones tipadas, evaluación, diagnósticos, eventos y tests. No añade dominios de planificación, catálogos, sincronización, multimedia, notificaciones o integraciones. La fase 03 de identidad visual sigue pendiente.
