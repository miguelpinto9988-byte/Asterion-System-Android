# Registro del cazador — integracion visual de pasos 2, 3 y 4

La estructura aprobada de 4 pasos no cambia. Esta fase actualiza los **controles
nativos** que ya existen, su navegacion, los iconos contextuales y sus marcos.

## Paso 2 — Entrenamiento
1. Experiencia principiante/intermedio/avanzado.
2. Objetivo principal (las 5 opciones del registro).
3. Objetivo personalizado con texto literal conservado.
4. Modalidad: 30/60/90 s de descanso; misiones 1-7; o rutinas
   de duracion disponible y sesiones por rondas/series.
5. Semana L/M/M/J/V/S/D. Cada pulsacion avanza categoria y color:
   superior, central, inferior, movilidad, cardio y descanso gris.
   La categoria muestra ademas su icono local si esta disponible;
   antes de incorporar los WebP se utiliza el glifo nativo.
6. Equipamiento: corporal, mancuernas (pesos) y gimnasio (equipos).

## Paso 3 — Personalizacion
- Tema claro u oscuro.
- Los 6 colores aprobados: azul, morado, rosa, verde, rojo y blanco.
  Las opciones son mutuamente excluyentes.
- Cinco acompanantes que representan al mismo motor de progreso.
- Tarjeta del cazador: silueta + fondo y vista previa interactiva.
  El elemento elegido se representa con un drawable local cuando
  exista; mientras tanto se utiliza el icono/escenario original
  que ya compila en el proyecto.
- Tipografia en negrita global segun la politica existente.

## Paso 4 — Activacion
- Las tres secciones (identidad, entrenamiento, personalizacion)
  pueden expandirse para revisar los valores reales guardados.
- Confirmacion final antes del guardado.
- Pantalla de 'SISTEMA ACTIVADO' unicamente despues de guardar.
- NO se incluye tutorial ni introduccion.

## Recursos y formato
Los PNG maestros Full HD de escenarios, las figuras PNG con transparencia
y 26 iconos individuales PNG/WebP fueron preparados aparte, en el paquete
`Asterion_Registro_Pasos_2_3_4_Integracion.zip` de la conversacion.
**Los recursos binarios de ese paquete NO estan aun en este repositorio**.
El código Android usa los identificadores de los futuros drawables compilados
(`asterion_step2_background`, `asterion_ic_...`, etc.) cuando se agreguen
a `app/src/main/res/drawable-nodpi/`; entre tanto utiliza archivos locales
existentes. No hay descargas ni generacion de imagenes en tiempo de ejecucion.

Para finalizar las ilustraciones tal como se aprobaron, incorporar los
WebP definitivos del paquete al directorio `drawable-nodpi`, sin cambiar
los nombres, y ejecutar toda la compilacion/pruebas Android.

Los marcos de ventana siguen siendo `NeonFrameDrawable`: escalables,
pulsables y separados de cualquier captura. El icono oficial y ambos
splash permanentes no se modifican.

## Verificacion
`tests/profile_wizard_visual_contract.py` se ejecuta en CI,
ademas de los bloqueos de identidad oficiales preexistentes.
