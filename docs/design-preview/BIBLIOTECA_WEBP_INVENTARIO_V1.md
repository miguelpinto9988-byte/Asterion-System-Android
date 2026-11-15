# Biblioteca visual WebP — inventario consolidado v1
Fecha: 2026-11-15
Rama de diseño: `design/profile-wizard-components-20261115`

**Estado:** recursos producidos y verificados en el entorno de la conversación; **los archivos binarios WebP aún no están en el repositorio ni en la APK**. No fusionar este documento como si terminara la integración.

## Paquete maestro
- Nombre: `Asterion_Biblioteca_Visual_Completa_v1.zip`
- Contiene **86 WebP** sin duplicar recursos entre pasos:
  - `iconos/tema_claro_negro/`: 37 iconos generales negros, 384×384, con transparencia.
  - `iconos/tema_oscuro_blanco/`: 37 iconos generales blancos, 384×384, con transparencia.
  - `iconos/categorias_color/`: 6 iconos semánticos con fondo transparente y tono más oscuro que su celda.
  - `imagenes/cabeceras/`: paisaje de tema claro y de tema oscuro, reutilizados en los cuatro pasos.
  - `imagenes/personaje/`: retrato conceptual en dos temas.
  - `imagenes/marcos_avatar/`: marcos ornamentales claros y oscuros, separados de la fotografía, transparentes.
- Incluye `MANIFIESTO.json` con SHA-256, dimensiones, indicador alfa y peso de cada archivo; `LEEME.md` e `Biblioteca_Visual_Interactiva.html` offline (WebP embebidos).

## Categorías y paleta elegida para el rediseño
No cambiar el **índice persistido** en `week` ni el orden de ciclo ya existente.

| Índice | Clave/asset | Categoría | Fondo de celda | Ícono más oscuro |
|---|---|---|---|---|
| 0 | `ic_tren_superior.webp` | Tren superior | `#F3BBC0` | `#AF2737` |
| 1 | `ic_tren_central.webp` | Tren central | `#F6D9A2` | `#A96712` |
| 2 | `ic_tren_inferior.webp` | Tren inferior | `#BFE9D0` | `#20754B` |
| 3 | `ic_movilidad.webp` | Movilidad | `#DAC7FA` | `#6E37B3` |
| 4 | `ic_cardio.webp` | Cardio | `#B8DBFA` | `#176DAF` |
| 5 | `ic_descanso.webp` | Descanso | `#D4D8E0` | `#4C596A` |

Movilidad: silueta original haciendo estiramiento; **no** símbolo de silla de ruedas o accesibilidad. Las seis imágenes son WebP transparentes e independientes del color de fondo de la celda.

## Dependencias para la integración nativa

1. Copiar en `app/src/main/res/drawable-nodpi/` los WebP con **nombres Android exclusivos**. Los assets generales deben llevar sufijos de tema, por ejemplo `ic_nombre_negro.webp` y `ic_nombre_blanco.webp`; los seis de categoría son comunes a ambos temas.
2. Usar la misma paleta centralizada en las celdas semanales, su leyenda y el resumen final.
3. Para cada toque a un día: `(indice+1)%6`, sin afectar otras celdas; conservar guardado `week` existente.
4. En la foto de perfil superponer `ImageView` de fotografía, `ImageView` de marco WebP y **botón real** de selección de foto; no usar el botón pintado como objetivo táctil.
5. Reutilizar los paisajes y retratos (no copiar por pantalla); mantener los textos y campos como vistas nativas y la tipografía en negrita.
6. En `SimpleProfileStepThreeView.java` sustituir la vista previa textual por tarjeta gráfica real con avatar, tema, compañero, acento, silueta y fondo; cambios inmediatos.
7. Verificar que ambos temas mantienen contraste y que el registro sigue funcionando en pantallas pequeñas.
8. Compilar APK debug, probar visualmente, verificar persistencia y tomar capturas antes de fusionar a `main`.

## Pendientes identificados
- Los recursos WebP del paquete todavía **deben transferirse al repositorio** para compilar el producto.
- El personaje disponible procede del prototipo conceptual y **no es aún un recorte idéntico** del boceto de referencia. Ajustar si se requiere fidelidad exacta.
- Falta integrar la tarjeta de cazador nativa y unificar colores en la implementación actual.
- Faltan pruebas de compilación, interacción y capturas sobre un Android real o emulador.

Documento enlazado al plan `docs/design-preview/PLAN_IMPLEMENTACION_REGISTRO_PERFIL.md` y a la referencia aprobada `docs/design-preview/REGISTRO_PERFIL_DISENO_APROBADO.md`.
