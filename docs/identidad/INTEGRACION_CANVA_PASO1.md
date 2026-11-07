# Integración del diseño Canva para el Paso 1

Diseño aprobado: Canva ID `DAHaL0a4BxM`, versión 941×1672 px.

## Alcance

- Se sustituye únicamente la presentación del paso 1, Identidad del cazador.
- El recurso visual definitivo es `app/src/main/res/drawable-nodpi/asterion_canva_paso1.png`; contiene la composición aprobada.
- Los controles reales se posicionan en `CanvaProfileStepOneView.java` y escriben en los EditText/RadioGroup existentes, conservando validaciones y persistencia local.
- La selección de foto usa `ActivityResultContracts.OpenDocument` con URI persistente, sin permisos adicionales.
- Los pasos 2, 3 y 4, `ProfileRegistrationDraft`, `ProfileStore`, icono oficial y pantallas de carga no se modifican.

## Importación y compilación

El primer commit instala temporalmente el importador `.github/workflows/asterion_canva_paso1_once.yml` que materializa el PNG completo desde el diseño aprobado y, en un segundo commit, elimina el importador temporal. Mientras el recurso no está incorporado, el código conserva como respaldo el fondo nativo existente, por lo que no rompe la compilación. El workflow normal de CI compila y publica `app-preview.apk` después de cambios en `main`.

## Verificación pendiente

Confirmar el éxito del importador temporal y del CI Android, descargar la APK `asterion-preview-instalable-apk` y comprobar en dispositivo la lectura, selección, validación y navegación. La composición usa coordenadas de un lienzo 941×1672 escalado uniformemente; algunas relaciones de aspecto mostrarán márgenes y necesitarán una iteración posterior para llenar toda la pantalla sin recortes.
