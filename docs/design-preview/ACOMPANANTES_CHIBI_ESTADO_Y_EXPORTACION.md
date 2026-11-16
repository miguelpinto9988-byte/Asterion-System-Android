# Acompañantes chibi: Igris, Beru, Tank, Tusk e Iron

Fecha: 15/11/2026. Solicitud: cinco ilustraciones **independientes**, recreadas sin recortar la lámina inicial, para importar en **WebP transparente**.

## Estado real

- Se generaron cinco ilustraciones independientes en el servicio creativo conectado, una para cada compañero. No son recortes.
- No se pudo recuperar todavía los cinco archivos de imagen dentro del contenedor de trabajo de GitHub ni confirmar que su canal alfa esté intacto. **No declarar la exportación WebP ni la subida de los cinco binarios como completadas.**
- El mapeo Java de recursos existe en `ProfileCardVisualAssets.companionDrawable()`; devuelve `asterion_companion_igris`, `asterion_companion_beru`, `asterion_companion_tank`, `asterion_companion_tusk` y `asterion_companion_iron`.
- No se ha cambiado la persistencia del perfil, el repositorio `main`, ni las selecciones previas.

## Revisión de fidelidad visual

- Igris: silueta de caballero de sombra compacto, casco angular y penacho rojizo.
- Beru: hormiga de sombra, cabeza y extremidades de insecto, antenas y alas; NO convertirlo en caballero humanoide.
- Tank: oso de sombra compacto.
- Tusk: chamán orco con capucha y colmillos.
- Iron: defensor de armadura pesada, **casco cerrado y escudo ancho**; evitar diseño de demonio con cuernos.
- Todos: proporciones chibi pequeñas y amables, detalles en violeta/azul moderados, sin nombre, placa, texto ni marco en los archivos finales.

## Pasos de integración pendientes

1. Recuperar los cinco archivos individuales desde sus tareas originales del generador; no sustituirlos por recortes o imágenes compuestas.
2. Recortar únicamente el *lienzo sobrante* de cada imagen individual si hace falta, eliminar el fondo de cada una y verificar el canal alfa con una herramienta de imágenes.
3. Exportar en WebP RGBA con nombres Android: `asterion_companion_igris.webp`, `asterion_companion_beru.webp`, `asterion_companion_tank.webp`, `asterion_companion_tusk.webp`, `asterion_companion_iron.webp`.
4. Anotar SHA-256 de los cinco binarios, subirlos a `app/src/main/res/drawable-nodpi/` y comprobar que pueden recuperarse intactos desde GitHub.
5. Mostrar las ilustraciones en el selector de acompañantes, compilar y probar con temas claro/oscuro antes de fusionar.

Los cinco originales están asociados a estos IDs de generación del servicio (no son rutas de GitHub): Igris `6a9957d6-3227-4d42-a56e-99da60feaa74`; Beru `ca90b208-e499-4805-b4b1-9253494e6aeb`; Tank `1b9156ec-827f-43da-80ea-f7ccf386dcce`; Tusk `1779554f-0e96-4887-91ec-1e13d8c831f8`; Iron `f2d4e701-4590-47a1-89e9-033a133bc91d`.
