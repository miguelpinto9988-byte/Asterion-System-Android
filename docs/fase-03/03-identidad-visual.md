# Fase 03 — Identidad visual oscura (primera integración nativa)

**Dirección aprobada:** estética de fantasía oscura inspirada en manhwa de progresión, con identidad original Asterion System. No se copian logotipos, tipografías protegidas ni personajes existentes.

## Sistema de diseño

- Fondo principal `#090E1D`, superficies `#141D37` y `#202B4A`.
- Texto principal `#F4F7FF`, secundario `#C3CDE5`.
- Acentos cian `#76C7FF`, violeta `#A79BFF`, contornos `#6477BE`, éxito `#91F4CC`.
- Jerarquía: títulos con familia serif Android integrada; texto funcional en sans del sistema.
- Recursos XML de fondos degradados, panel ornamental, botones normales y estados pulsado/enfocado.
- Elementos táctiles: botones con mínimo de 48dp. Se conservan los controles y los identificadores XML existentes.

## Pantallas

La integración inicial aplica fondo oscuro a Inicio, Catálogo, detalle, Planificador, formulario, Seguimiento, resumen, Configuración, Estado local y pantalla secundaria. La pantalla Inicio usa botones de ancho completo y un panel de bienvenida ornamental. Las preferencias existentes de texto grande y densidad se conservan.

Se han añadido dos ilustraciones vectoriales originales (`asterion_guardian.xml`, `asterion_shadow_sentinel.xml`), un emblema vectorial y cinco iconos de módulos. El guardián aparece en el encabezado Inicio y el centinela en la tarjeta Estado local. No se incluyen personajes oficiales de Solo Leveling, fuentes externas ni recursos de terceros sin licencia.

## Compatibilidad y límites

Se mantiene Android Java/XML, sin Compose, HTML o WebView dentro del módulo app. No se cambia SQLite v5, SharedPreferences, las rutas de navegación ni la lógica de actividades. La corrección de edge-to-edge de fase 16 permanece.

**No compilado:** los estilos XML deben validarse mediante Android aapt/Gradle únicamente después de autorización independiente. La inspección estática no prueba cómo dibuja Android cada control, ni la accesibilidad con TalkBack.
