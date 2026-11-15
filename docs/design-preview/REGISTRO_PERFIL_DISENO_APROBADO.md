# Registro de perfil — diseño aprobado y preservación

Fecha de aprobación: 2026-11-15. Estado: **prototipo HTML aprobado, implementación Android pendiente**.

## Entregables de referencia

- Demostración autocontenida: `Asterion_Perfil_Demo_Interactiva.html` (HTML con CSS/JS e imágenes WebP embebidas, 1 013 591 bytes). SHA-256: `3f3c7cdf1fb787cf8983589fefc61d4b5fc5655ccd5794769b2666b7332a0234`.
- Captura de referencia: `Asterion_Perfil_Preview.png` (339 924 bytes antes de una captura de prueba adicional).
- Estos archivos fueron entregados como artefactos en la conversación y **no están aún subidos a GitHub**. Evitar afirmar que son parte de la APK o del repositorio hasta haber copiado los binarios.

## Elementos aprobados

1. Cuatro pasos: identidad, entrenamiento, personalización y activación.
2. Cabeceras panorámicas de castillos y montañas de estilo occidental, variantes de tema claro y oscuro. Un recurso por tema, reutilizado en los cuatro pasos.
3. Tipografía **en negrita en toda la interfaz**, incluidos los campos de entrada.
4. Campos nativos con superficies blancas en claro y oscuras en oscuro, con contraste adecuado y contornos visibles.
5. Tarjetas violetas y botones con estados de foco, selección y activación.
6. Planificación semanal interactiva: siete celdas, una por día; **cada toque cicla** Tren superior → Tren central → Tren inferior → Movilidad → Cardio → Descanso → Tren superior. Cada categoría conserva su color y su icono. Mostrar leyenda y etiquetas accesibles. Guardar en `week` como siete índices separados por comas, preservando los códigos actuales.
7. Personalización con cambios inmediatos de tema, acento, compañero, silueta y fondo; tarjeta de cazador con imagen visible y cambios en vivo.
8. Resumen final de perfil, incluidas las siete categorías asignadas.
9. Mantener datos y lógica existentes; **no sustituir campos funcionales por imágenes**, fondos WebP solo como decoración.
10. No duplicar imágenes repetidas.

## Prueba del prototipo HTML

Se probó con Chromium mediante `test_asterion_demo.py`: imagen del avatar cargada; navegación a paso 2; toque sobre lunes cambia de «Tren superior» a «Tren central»; cambio a oscuro; tarjeta de previsualización y su fondo cargados; **sin errores JavaScript**.

## Próximo trabajo en rama de diseño

- Exportar los assets WebP a `app/src/main/res/drawable-nodpi/` y los iconos a vectores XML cuando sea apropiado.
- Construir encabezado y paneles reutilizables con tema claro/oscuro.
- Extraer la selección semanal a un componente Android nativo y usar la misma representación en el resumen.
- Crear la tarjeta de vista previa realmente gráfica y conectada a los valores del borrador.
- Compilar, ejecutar pruebas y capturar los cuatro pasos con ambos temas antes de fusionar.

## Control de integración

El PR que contiene este documento es de **diseño/documentación**, no de código Android listo. `main` queda sin cambios y el prototipo aprobado se conserva como archivo autocontenido descargable en la conversación. No declarar integración terminada antes de comprobar una APK.
