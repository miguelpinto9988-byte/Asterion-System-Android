# Fase 04 — Trazabilidad de las siete etapas

| Etapa | Implementación |
|---|---|
| 04.1 Controlador de navegación | `core/navigation/NavigationRoute.java`, `NavigationState.java`, `NavigationController.java` |
| 04.2 Pantallas XML | `app/src/main/res/layout/screen_home.xml`, `screen_secondary.xml` y `activity_main.xml` |
| 04.3 Adelante y Atrás | `NavigationController.navigateTo/goBack`, botones XML y `MainActivity` |
| 04.4 Gestión de estado | `NavigationState` inmutable y `InMemoryStateStore` de fase 02 |
| 04.5 Restauración | `NavigationBundleAdapter` conectado a `onSaveInstanceState` |
| 04.6 Integración Java/Android | `NavigationRenderer`, `NavigationChangedEvent`, `AndroidLifecycleAdapter`, `ApplicationCoordinator` |
| 04.7 Pruebas y documentación | Dos clases nuevas de pruebas JVM y documentos `docs/fase-04/` |

## Límites del alcance

- **Fase 03, pendiente:** no se define imagen corporativa, iconografía ni componentes finales.
- **Fase 05, aprobada pero no iniciada:** ninguna entidad SQLite/Room ni almacenamiento permanente.
- **Fase 06, aprobada pero no iniciada:** ninguna regla funcional de Hunter ni motor de negocio.
- **Fases 07, 08, 09, 11, 14, pendientes:** sin contenidos, planificación, seguimiento, ajustes ni sincronización.
- **Fases 10, 12 y 13, excluidas:** sin módulos multimedia, notificaciones ni integraciones.
- **Fase 15, aprobada pero no iniciada:** comprobaciones básicas no sustituyen la auditoría integral.
- **Fase 16, aprobada pero no iniciada:** sin nueva compilación ni publicación de APK.
