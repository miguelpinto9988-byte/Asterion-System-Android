# Fase 04 — Navegación nativa y estado temporal

**Alcance aprobado:** navegación entre dos pantallas de demostración, historial, botón Atrás, restauración de estado y conexiones con la arquitectura de fase 02. No se introducen módulos de negocio ni base de datos.

## Dependencias

- `:core` contiene `NavigationRoute`, `NavigationState`, `NavigationController` y `NavigationChangedEvent`. Todos son Java puro, sin Android.
- `:app` contiene `NavigationBundleAdapter` (estado temporal en `Bundle`) y `NavigationRenderer` (inflado de XML nativo).
- `MainActivity` realiza la composición de `ApplicationCoordinator`, `AndroidLifecycleAdapter`, `InMemoryEventBus` y `NavigationController`.

Los controladores de navegación no conocen Activity, View ni recursos XML. La dependencia apunta desde el adaptador Android al núcleo, no al revés.

## Flujo de navegación

1. `MainActivity` carga `activity_main.xml`, cuyo contenedor `navigation_host` muestra una sola pantalla.
2. `NavigationRenderer` infla `screen_home.xml` y conecta el botón **Abrir pantalla secundaria**.
3. El botón llama a `NavigationController.navigateTo(SECONDARY)`.
4. El controlador modifica `StateStore<NavigationState>` y publica `NavigationChangedEvent` si hubo cambio.
5. La Activity escucha el evento, selecciona `screen_secondary.xml` y actualiza el manejo de Atrás.
6. El botón **Volver al inicio**, o el gesto/botón Atrás del sistema, ejecuta `NavigationController.goBack()`.
7. En la pantalla HOME no se intercepta Atrás del sistema en API 33+; Android conserva el comportamiento de salir de la Activity.

`NavigationState` es inmutable. El historial siempre comienza en HOME, no admite HOME duplicada, no repite la ruta superior y tiene una cota de 16 entradas preparada para futuras rutas. En fase 04 solo hay dos rutas.

## Restauración temporal

- `NavigationBundleAdapter.save` escribe una lista de identificadores estables y una versión de formato en `onSaveInstanceState`.
- `NavigationBundleAdapter.read` valida versión, historial e identificadores antes de restaurar.
- Datos ausentes, corruptos o incompatibles restablecen HOME de manera segura.
- La restauración soporta la recreación de una actividad con estado entregado por Android; **no** constituye persistencia en disco ni retiene el historial después de un cierre normal y nuevo lanzamiento.

## Retroceso Android

En API 33 o posterior se registra `OnBackInvokedCallback` solamente cuando hay una pantalla anterior; se retira al regresar a HOME y en `onDestroy`. En API 26–32 el método `onBackPressed` actúa como alternativa.

No se prometen transiciones visuales personalizadas ni soporte predictivo animado: su validación física se reserva para una futura autorización de compilación y pruebas Android.

## Previsualización XML sin APK

En Android Studio abrir individualmente:

- `app/src/main/res/layout/screen_home.xml` → Design o Split.
- `app/src/main/res/layout/screen_secondary.xml` → Design o Split.
- `app/src/main/res/layout/activity_main.xml` es únicamente el contenedor. En el editor puede verse vacío: Java inserta las pantallas en tiempo de ejecución.

No existe versión HTML, WebView ni Compose. Estos XML son provisionales y no reemplazan la fase 03 pendiente.
