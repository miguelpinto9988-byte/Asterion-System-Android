# Fase 03 — Integración vectorial, componentes y pantallas

## Objetivo y alcance

Tras aprobar la estética oscura inspirada en el manhwa *Solo Leveling*, se completó un segundo paso visual sobre Android nativo **Java + XML**, sin tocar lógica de actividades, repositorios SQLite, manifest, versionado de compilación, permisos, firma ni CI. Se utilizaron diseños y personajes **originales**, no material oficial de la franquicia.

## Ilustraciones originales

- `asterion_guardian.xml`: personaje original vectorial con cabello oscuro, capa geométrica, aura azul y violeta. Se muestra en el encabezado de Inicio con descripción accesible.
- `asterion_shadow_sentinel.xml`: figura secundaria abstracta, usada como ornamento en el bloque Estado local, excluida de la navegación accesible.
- `asterion_crest.xml`: emblema propio de Asterion con trazo angular y órbita; aparece en la cabecera y en nueve pantallas secundarias.
- `asterion_ic_catalog.xml`, `asterion_ic_plan.xml`, `asterion_ic_progress.xml`, `asterion_ic_settings.xml`, `asterion_ic_status.xml`: iconos de navegación originales con trazos vectoriales.

Los recursos son `VectorDrawable` de Android: no necesitan imágenes de red, WebView, fuentes comerciales ni archivos de imagen a gran resolución. Su aspecto final debe comprobarse mediante el sistema Android de renderizado.

## Componentes reutilizables

- `asterion_hero_panel.xml`: fondo degradado con contorno del encabezado.
- `asterion_card_selector.xml`: apariencia y estados enfocado/pulsado de tarjetas.
- `asterion_list_card.xml` y `asterion_list_row_selector.xml`: superficies separadas y estados seleccionables de las filas.
- `asterion_day_selector.xml` y `asterion_day_selected.xml`: estilo del calendario semanal, enlazado al estado `setSelected()` ya usado por `SchedulePresenter`.
- `NativePreferencesApplier`: espaciado de listas de 10dp normal y 6dp compacto, manteniendo la opción de texto ampliado.
- `PreferencesRowAdapter`: filas oscuras, legibles, con alturas mínimas 56dp y 48dp, padding y fondos de enfoque.

## Pantalla Inicio

Se ha rediseñado `screen_home.xml` con:

1. Emblema original y marca Asterion.
2. Encabezado con Guardián vectorial, explicación de organización y estado local.
3. Accesos de dos columnas a Catálogo, Planificador, Seguimiento y Configuración.
4. Tarjeta Estado local con centinela ornamental.
5. Acceso secundario de demostración existente y nota del diseño.

**Se preservan todos los IDs originales** de navegación: `open_catalog`, `open_plan`, `open_progress`, `open_settings`, `open_local_status` y `open_secondary`. Los controladores Java existentes conservan sus callbacks.

## Resto de pantallas

Catálogo, detalle, Planificador, formulario, Seguimiento, resumen, Configuración, Estado local y pantalla secundaria incorporan un emblema visual no accionable y mantienen los controles originales. El calendario utiliza siete selectores nativos para los días, el resumen semanal muestra métricas sobre tarjetas separadas y las preferencias se agrupan en superficies oscuras.

## Rendimiento y accesibilidad

No se añaden servicios ni procesamiento en segundo plano. Los recursos decorativos son XML locales. El contenido interactivo conserva etiquetas accesibles, regiones de estado, controles de al menos 48dp, tamaños de texto ajustables y contrastes altos en la paleta principal. El personaje del encabezado tiene un `contentDescription`; iconos redundantes y siluetas puramente decorativas usan `importantForAccessibility=no`.

**Limitación:** no hay evidencias de rendimiento ni de apariencia en Android real, porque Gradle, compilación e instrumentación no están autorizadas. El HTML externo es un simulador visual independiente, no una renderización de Android XML.
