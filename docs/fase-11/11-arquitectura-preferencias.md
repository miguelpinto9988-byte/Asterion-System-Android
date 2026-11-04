# Fase 11 — Arquitectura de preferencias locales

## Objetivo y límites aprobados

Añadir una pantalla Android XML para adaptar presentación y sección de inicio, sin modificar esquema SQLite v5, catálogo, programación, resultados de seguimiento, reglas o servicios externos.

Las tres y **únicas** opciones implementadas son:

| Preferencia | Valores permitidos | Valor predeterminado |
|---|---|---|
| Tamaño de texto | `normal`, `large` | `normal` |
| Densidad de listas | `normal`, `compact` | `normal` |
| Sección inicial | `home`, `catalog`, `plan`, `progress` | `home` |

Ninguna opción altera contenidos, datos de planificación o seguimiento. No hay cuentas ni sincronización.

## Diseño en capas

```text
:core  (Java puro)
  settings/AppPreferences          objeto inmutable y enums tipados
  settings/PreferencesRepository   contrato de lectura, escritura y reset
  settings/PreferencesService      lógica de cambios explícitos y valores iguales
                ^
                |
:app   (Java + Android XML)
  platform/AndroidPreferencesRepository
  presentation/SettingsPresenter
  presentation/NativePreferencesApplier
  presentation/PreferencesRowAdapter
  res/layout/screen_settings.xml
  MainActivity + NavigationRenderer
```

`AppPreferences` restringe sus valores a enums y no depende del SDK Android. `PreferencesService.update` evita escrituras cuando el valor no cambia. La capa Android implementa el puerto con `SharedPreferences`.

## Persistencia y validación

- Archivo `asterion_preferences_v1` dentro del sandbox de la aplicación, con `Context.MODE_PRIVATE`.
- Claves: `presentation.text_size`, `presentation.list_density`, `navigation.start_section`.
- Escrituras mediante `SharedPreferences.Editor.apply()` sin abrir SQLite ni requerir permisos nuevos.
- Los valores desconocidos y claves con tipo incompatible recuperan de manera segura el valor predeterminado por propiedad.
- `reset()` limpia **solo este archivo de preferencias** mediante su editor. No borra la base `asterion_internal.db`, las tablas de catálogo, planificación o seguimiento. No es una operación de reinicio de cuenta ni importación/exportación.

La configuración no almacena secretos, contraseñas, perfiles o datos privados de terceros. El manifiesto existente conserva `allowBackup=false`.

## Presentación nativa

`NavigationRenderer` infla el layout XML de cada pantalla. `NativePreferencesApplier` ajusta una única vez el tamaño de `TextView` recién inflados, incluidos los botones, al 118% cuando se elige tamaño grande. La escala es un multiplicador sobre la dimensión de texto expresada en píxeles por Android, por lo que respeta la escala del sistema.

`PreferencesRowAdapter` aplica el tamaño de letra y el espaciado vertical a las filas de texto de las listas nativas de catálogo, planificación y seguimiento. La densidad compacta utiliza menor padding y altura mínima que la normal. El resto de las pantallas se mantiene visualmente provisional porque la fase 03 no ha sido aprobada.

Las vistas XML nuevas o reinfladas se actualizan tras guardar preferencias; el presentador de configuración solicita una reinflación controlada. Los cuadros de diálogo creados dinámicamente en fases anteriores no quedan íntegramente estilizados por esta fase.

## Inicio preferido, estado y navegación

`NavigationRoute.SETTINGS` abre la pantalla de configuración desde Inicio. El estado de navegación sigue usando `NavigationController` y la pila de fase 04.

El orden de prioridad de arranque es:

1. Si Android aporta un `Bundle` válido de estado, restaurar esa navegación. **No** sustituirla por la sección preferida.
2. Si no existe estado guardado, empezar con HOME como raíz y, si el usuario eligió Catálogo, Planificador o Seguimiento, añadir esa ruta a la pila.
3. La nueva selección de inicio se aplicará en el siguiente lanzamiento en frío; no expulsa de la pantalla actual.

La ruta HOME permanece como raíz para el comportamiento normal del botón Atrás. Las secciones disponibles no incluyen el detalle de catálogo, formulario de programación ni resumen semanal para evitar aperturas huérfanas.

## Vista HTML

La previsualización HTML entregada externamente conserva los laboratorios de fases anteriores y simula preferencias mediante `localStorage`. **No contiene el repositorio de producción ni se incorpora a la aplicación Android.**
