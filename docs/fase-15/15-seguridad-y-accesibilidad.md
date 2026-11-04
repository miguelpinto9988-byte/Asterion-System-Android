# Fase 15 — Revisión de seguridad y accesibilidad

## Seguridad local

La revisión conserva explícitamente:

- `AndroidManifest.xml` sin permisos de internet ni estado de red, `android:allowBackup=false`, `android:usesCleartextTraffic=false`.
- `SharedPreferences` de aplicación con `Context.MODE_PRIVATE`; restablecer opciones no borra la base SQLite.
- SQLite versión 5, claves foráneas activas y manejador de corrupción que no ejecuta eliminación destructiva.
- Consultas SQL de títulos con placeholders `?` y argumentos separados; **nunca** concatenan los valores de identificadores al SQL.
- Límite de 200 placeholders por lote y límite de 64 caracteres por identificador.
- Interfaces Android en XML y código Java; no se incorpora HTML de simulación a `:app`, no se introducen permisos ni transporte de datos externos.
- Mensajes de error local sin incluir rutas internas, SQL crudo ni datos de usuario.

Esta revisión reduce riesgos conocidos, pero **no equivale a una auditoría de penetración ni certifica seguridad absoluta**. La base local no se cifra automáticamente por estos cambios, y esta fase no instala un sistema de backup o autenticación.

## Accesibilidad implementada

- `android:accessibilityLiveRegion="polite"` en siete campos de estado o título asíncronos de Catálogo, Planificador, Seguimiento y Diagnóstico.
- Etiquetas `android:labelFor` para fecha, hora y duración del formulario.
- `contentDescription` del campo de búsqueda y ejemplos de formato en recursos `strings.xml` (sin cadenas literales inglesas).
- Los siete botones del calendario anuncian el nombre completo del día y la fecha, más su estado seleccionado; el día elegido permanece habilitado para mantener su objetivo táctil.
- Se eleva a **48 dp** la altura mínima de filas compactas, preservando 56 dp como mínimo normal, y se establece una altura mínima para los campos de entrada.
- Se mantienen las relaciones de contraste actuales: texto principal `#1C2638` sobre blanco ≈ **15,18:1**; secundario `#526072` sobre blanco ≈ **6,41:1** (cálculo sRGB estático).

## Pendiente de autorización

Comprobar TalkBack, modo accesibilidad de Android, navegación táctil real, superposición con tamaño de fuente aumentado, contraste de todos los estados y colores del sistema; medir botones reales en dispositivos estrechos. Los contrastes indicados solo evalúan dos pares de colores fijos, **no** toda la interfaz.

La previsualización HTML utiliza `aria-live`, etiquetas y `aria-pressed` para hacer revisable la intención, pero no sustituye TalkBack ni el renderizado de las vistas XML Android.
