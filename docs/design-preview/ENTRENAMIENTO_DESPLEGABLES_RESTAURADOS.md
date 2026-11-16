# Restauración de opciones desplegables en Entrenamiento (paso 2)

Fecha: 15/11/2026. Esta corrección **no cambia el módulo de personalización sin marcos**, ni borra ninguna preferencia ya guardada.

## Modalidades
- **Misiones** muestra el selector de cantidad diaria (1–7; valor inicial 3). Las misiones incluyen actividades ligeras y descansos, no implican entrenar varias veces al día.
- **Rutinas** muestra descanso entre rondas/series (30, 60 o 90 segundos), organización en rondas o series y minutos de sesión (10, 15, 20, 30, 45 o 60).
- Cambiar modalidad oculta el otro bloque, sin borrar los valores previos. Claves: `mode`, `missions`, `rest_seconds`, `sequence`, `minutes`.

## Equipamiento
- **Peso corporal** ahora despliega y oculta los tres apoyos opcionales: `mat` (colchoneta), `wall` (pared firme) y `chair` (silla estable). Clave principal: `bodyweight`.
- **Mancuernas** muestra al marcarse el campo `dumbbell_weights` para los pesos disponibles. Clave principal: `dumbbells`.
- **Gimnasio** muestra al marcarse `gym_polea`, `gym_maquina`, `gym_banco`, `gym_barra`, `gym_bicicleta`, `gym_remo`, `gym_eliptica`. Clave principal: `gym`.
- En los tres casos se conservan los valores al cerrar y reabrir el panel; se mantiene el sistema de persistencia del registro.

## HTML local corregido
`Asterion_Registro_Perfil_Sin_Marcos_Entrenamiento_Restaurado.html` se entregó en el chat. Contiene controles funcionales para los apartados anteriores, intensidad baja/moderada, objetivo libre y el calendario semanal por categorías, sin reintroducir marcos. Exporta un JSON de ejemplo con las claves de configuración.

**Prueba en Chromium de 390 px:** cambio de modalidad y visibilidad condicional, cambios en descanso, apertura/cierre de los tres paneles de equipamiento, persistencia temporal de pesos y casillas al cambiar modalidad, ausencia de errores JavaScript y ausencia de desbordamiento horizontal.

## Estado en GitHub
La modificación Java de `ProfileRegistrationSteps.java` para los apoyos condicionales se guardó en la rama `design/profile-wizard-components-20261115`. Los paneles de Mancuernas, Gimnasio, Misiones y Rutinas ya existían en Java; el problema principal fue que el HTML no los reproducía. Los 94 WebP binarios definitivos ya se subieron a la rama de diseño; la compilación y las pruebas de APK siguen pendientes. No fusionar `main` hasta completar la validación.
