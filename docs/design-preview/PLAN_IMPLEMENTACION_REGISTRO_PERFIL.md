# Plan de implementación — registro de perfil Asterion System

Estado: especificación técnica para revisión, sin reemplazar aún pantallas en producción.
Rama: `design/profile-wizard-components-20261115`.

## Objetivo visual

Reproducir **cuatro pantallas nativas y funcionales** de registro, tanto en tema claro como oscuro. La referencia es el boceto aprobado en el chat (paisaje occidental de fantasía, encabezado ASTERION SYSTEM, tarjetas claras/oscuro-violeta, botones violetas, tipografía legible). Los bocetos son referencias visuales, **no imágenes interactivas**. Mantener las opciones actuales y sus identificadores de almacenamiento.

## Estado observado en `main`

- `ProfileCreateActivity.java` ya implementa el flujo de cuatro pasos (identidad, entrenamiento, personalización, activación), navegación, guardado de borradores y validación.
- `screen_profile_create.xml` contiene la base nativa del formulario del primer paso.
- `ProfileRegistrationSteps.java` ya implementa opciones de experiencia, objetivos, modo, equipamiento y **planificación semanal por toques**.
- `ProfileRegistrationSteps.java` ya guarda cada toque en la clave JSON `week` como siete índices separados por comas, de lunes a domingo. El orden actual es `Tren superior → Tren central → Tren inferior → Movilidad → Cardio → Descanso → Tren superior`. **Conservar índices y orden**.
- `ProfileWizardTheme.java` aplica blanco/oscuro y preserva deliberadamente los colores de categorías para los días.
- `SimpleProfileStepThreeView.java` ofrece selección de tema, acento, acompañante, silueta y fondo; su vista previa actual es **texto**, no una tarjeta gráfica.
- `ProfileStore.java` y `ProfileRegistrationDraft.java` guardan los mismos datos en almacenamiento privado del dispositivo. No migrar ni resetear datos para una actualización visual.

## Separación de componentes y formatos

| Componente | Implementación | Formato y ubicación sugerida | Estado |
|---|---|---|---|
| Diseño global y colores de temas | Tokens de color + estilos de texto, bordes y paneles | `res/values/colors.xml`, `res/values/styles.xml`, drawables XML y `ProfileWizardTheme.java` | Base existente; ajustar |
| Paisaje occidental en cabecera (día/noche) | Solo decoración; no incrustar campos o botones | Dos archivos **WebP** en `res/drawable-nodpi/` (sin texto ni controles) | Arte pendiente de preparar |
| Logo y ornamentos repetidos | Arte reutilizable, independiente del formulario | Vector drawable XML para ornamentos, WebP para arte complejo | Separar |
| Indicador 1/4–4/4 | Vista Android accesible, refleja el paso actual | XML + Java, no imagen de texto | Base existente; rediseñar |
| Formulario de identidad | `EditText`, avatar `ImageView`, controles accesibles | XML y Java existentes | Funcional; rediseñar |
| Objetivos, modalidad, tiempo, equipo | Controles Android nativos, sin duplicar ni perder opciones | `ProfileRegistrationSteps.java` y componentes reutilizables | Funcional; reorganizar |
| Plan semanal | 7 celdas nativas táctiles, cada toque cambia color + símbolo + valor guardado | Extraer a `WeeklyCategoryPickerView.java` sin modificar semántica de `week` | **La lógica ya existe**; aislar para reutilizar |
| Iconos de categorías | Seis símbolos diferentes, tinta blanca sobre fondos distintos; etiqueta accesible por categoría | Vector drawables XML (`asterion_ic_upper`, `core`, `lower`, `mobility`, `cardio`, `rest`) | Fallback textual ya existe; perfeccionar |
| Selector claro/oscuro | Cambiar todas las superficies en vivo, sin reiniciar el registro | Java + recursos de temas; clave existente `theme` | Funcional; mejorar vista |
| Colores, compañero, silueta y fondo | Mantener identificadores persistidos (`accent`, `companion`, `card_silhouette`, `card_background`) | Controles Android + miniaturas WebP locales | Funcional en texto; rediseñar |
| Vista previa del cazador | Tarjeta nativa reutilizable: avatar, nombre, acento, compañero, fondo y nivel mostrado solo si existe; actualización inmediata | Nuevo `HunterCardPreviewView.java` + WebP fondo/personaje, XML para marco | **Pendiente: hoy es texto** |
| Confirmación | Resumen real de campos y siete días con sus colores y símbolos; activa solo mediante confirmación explícita | `ProfileRegistrationSteps.java`, reutilizar tarjeta y resumen semanal | Reorganizar |
| Persistencia y validación | Conservar los métodos originales y compatibilidad con perfiles anteriores | `ProfileStore.java`, `ProfileRegistrationDraft.java` | Ya funcional: no cambiar salvo pruebas |

**Importante:** los colores y símbolos del boceto son conceptuales. La aplicación tiene un mapeo existente `CAT_COLORS`, `CAT_GLYPHS` y `CATS`. Consolidar en una fuente única de verdad para que formulario, resumen y futuras vistas muestren exactamente la misma categoría. No cambiar el significado de los índices guardados. El tema claro/oscuro debe modificar superficies y textos, **no los seis colores semánticos**.

## Comportamiento aprobado de planificación

1. Mostrar siete celdas correspondientes a lunes–domingo, cada una con abreviatura inequívoca, símbolo y fondo de color propio.
2. Un toque cambia **solo ese día** al siguiente índice de seis categorías y lo persiste inmediatamente en `week`.
3. Después de descanso se vuelve a tren superior.
4. Se conserva la selección al retroceder, avanzar, cerrar y reabrir el asistente.
5. Mostrar leyenda visible con las seis categorías, con accesibilidad por nombre (nunca depender solo del color).
6. La activación reproduce exactamente los mismos siete valores y colores.
7. No introducir cuotas de ejercicios ni modificar el motor de planificación.

## Comportamiento aprobado de vista previa

- En el paso 3 se renderiza una **tarjeta real** (no una captura de pantalla y no solo texto).
- Cada elección de tema, acento, acompañante, silueta o fondo actualiza de inmediato su representación, conservando el borrador existente.
- El avatar elegido y el nombre del paso 1 se reflejan cuando existen. Sin imagen elegida, usar recurso local genérico.
- Al pasar de claro a oscuro cambian panel, contraste, marco y texto; no se borran selecciones.
- En pantallas estrechas, la tarjeta se sitúa debajo de los selectores o en un bloque de ancho completo; no usar dos columnas diminutas.
- El cuarto paso muestra la tarjeta y un resumen compacto del horario real. El diseño puede omitir métricas personales de la vista previa que no son necesarias.

## Reglas de maquetación

- Pantallas **Android nativas**: XML/Java, `dp` para tamaños, `sp` para texto; no HTML, Canvas como sustituto de formularios, ni controles incrustados en PNG.
- Fondos de fantasía: **WebP** optimizado, local y decorativo; paisajes sin interfaz dibujada. Recortar mediante `centerCrop` solo en zonas decorativas; proteger texto/campos de recortes.
- Iconos vectoriales XML cuando sea posible. Imagen de personaje/escenario y retratos: WebP con recursos nombrados individualmente.
- Estados de pulsación y selección reales; áreas de toque de al menos 48dp y descripciones accesibles para lector de pantalla.
- Permitir desplazamiento, teclado visible, cambio de orientación, tamaños de fuente grandes y móviles estrechos.
- Evitar personajes de terceros reproducidos de forma literal; usar ilustración original para el cazador.

## Orden de construcción propuesto

1. **Tokens y contenedores**: crear componentes de encabezado, progreso, panel y botón, con variantes claro/oscuro.
2. **Planificación reutilizable**: extraer el ciclo de toques ya existente a vista propia, añadir iconos vectoriales, leyenda y pruebas de persistencia.
3. **Paso 1**: adaptar campos, avatar y opciones de identidad al boceto sin cambiar validación.
4. **Paso 2**: agrupar entrenamiento y planificación con jerarquía visual, conservar modo/misiones/rutinas/equipo.
5. **Paso 3**: crear tarjeta de vista previa en vivo y controles de personalización.
6. **Paso 4**: resumen real, semana por colores, confirmación y activación.
7. **Arte definitivo**: reemplazar solo decoraciones por WebP optimizados y comprobar apariencia de día/noche visual y tema claro/oscuro (el fondo del perfil no depende automáticamente de la hora; lo gobierna el tema que el usuario elija).
8. **Validación**: ejecutar pruebas unitarias/contratos existentes, compilar APK debug, capturas en emulador con tema claro/oscuro, comprobar estados y comparar contra bocetos.

## Criterios de aceptación antes de fusionar

- Los 4 pasos son navegables y completos en ambos temas.
- Ningún campo actual desaparece ni cambia el valor almacenado.
- Cada toque en un día recorre exactamente seis categorías; ambos temas muestran su color y símbolo correcto; leyenda presente.
- Vista previa actualiza realmente avatar/nombre/tema/acento/compañero/silueta/fondo.
- Al regresar al asistente, los datos siguen guardados; la activación no se adelanta.
- Accesibilidad razonable, compatibilidad con pantallas pequeñas y sin ilustraciones usadas como controles.
- Compilación y pruebas pasan y se revisan capturas de la APK (los mockups no demuestran funcionamiento).

Este documento es el **primer entregable de separación**, no la implementación visual completa.
