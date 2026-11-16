# Objetivo personal escrito — conectado al motor de actividades (15/11/2026)

## Comprobación del comportamiento anterior

En el código anterior `custom_goal` se guardaba en el JSON de registro y se incluía en la huella de configuración de `MissionProfileAdapter`. **No se interpretaba para priorizar ejercicios**: editar el texto podía cambiar el sorteo de misiones, pero no favorecía necesariamente actividades relacionadas con el objetivo.

## Implementación en esta rama

- `ProfileRegistrationSteps.step2()`: mantiene el campo multilínea opcional `custom_goal` y su almacenamiento local.
- `ProfileStore`: conserva el JSON del registro sin convertir métricas corporales en puntuaciones.
- `MissionProfileAdapter.fromRegistration()`: pasa el objetivo escrito, sin perderlo dentro de la firma, a `MissionPlanner.Profile.personalGoal`.
- `PersonalGoalInterpreter`: interpreta sin conexión preferencias explícitas en español, normaliza mayúsculas y tildes, evita convertir expresiones negadas sencillas en preferencias positivas y reconoce términos de caminata, ciclismo, carrera, danza, equilibrio, flexibilidad, movilidad, postura, tronco, brazos, piernas, resistencia, fuerza y respiración.
- `MissionPlanner`: después de filtrar por día/categoría, equipamiento, complejidad técnica e intensidad, reordena de forma estable las alternativas compatibles por afinidad con el texto. En empates preserva el sorteo determinista y la diversidad previa. `adapted()` conserva el objetivo al suavizar la sesión.
- `RoutineGenerator`: usa la misma preferencia únicamente después de los filtros de seguridad y equipamiento, conservando el límite de duración y la selección voluntaria.
- `MissionsActivity`: explica al usuario cuándo el objetivo está siendo reconocido y cuándo no hay una intención específica identificable.
- `DailyMissionStore`: no requiere cambios; la firma del perfil incluye `personalGoal`, por lo que editarlo invalida los IDs del día anterior sin copiar texto personal al registro diario de misiones.

## Límites intencionales

**No es un modelo de lenguaje general ni un diagnóstico médico.** Solo interpreta reglas de vocabulario reconocible; no inventa ejercicios, equipos, categorías, intensidad, repeticiones, volumen o tratamientos. Un texto no reconocido permanece guardado pero no fuerza nuevos ejercicios. Palabras relacionadas con peso o imagen corporal no se convierten en instrucciones restrictivas. La preferencia se subordina al calendario, días de descanso, intensidad, técnica supervisada, historial de esfuerzo y equipo disponible.

## Pruebas añadidas

- `progress-engine/src/test/java/pe/asterionsystem/core/engine/missions/PersonalGoalInterpreterTest.java`: coincidencias en español, negación, objetivo no reconocido, límites de seguridad, descanso, determinismo, selección basada en afinidad y preservación durante adaptación.
- `app/src/androidTest/java/pe/asterionsystem/app/PersonalGoalRegistrationTest.java`: lectura real del JSON `custom_goal` por el adaptador Android e invalidación de la firma de misión al editarlo.

**Estado de verificación:** el código y las pruebas están publicados en la rama de diseño y el PR #47. No hay resultado de CI compilando este último commit; las pruebas se han agregado pero **no se han ejecutado ni certificado**. Se requiere correr `:progress-engine:test` y la instrumentación Android del módulo `app` antes de fusionar. Los 94 recursos WebP binarios del paquete sin marcos siguen pendientes de subida física al repositorio.
