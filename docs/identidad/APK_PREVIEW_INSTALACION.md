# APK de prueba — instalación paralela

Las APK de depuración producidas por diferentes ejecutores de GitHub Actions
pueden estar firmadas con certificados distintos. Android no permite actualizar
una aplicación con una firma diferente mediante una instalación normal.

## Solución sin eliminar perfiles
- La variante Android `preview` usa el paquete
  `pe.asterionsystem.app.preview`, distinto del paquete principal
  `pe.asterionsystem.app`.
- Su nombre visible es **Asterion Prueba**; usa el icono oficial inalterado.
- Se instala junto a Asterion System sin borrar los datos antiguos.
- El registro de la app de prueba empieza vacío y es independiente.
- Las imágenes HD pendientes de integrar al repositorio todavía no aparecerán
  en esta versión.

## Requisitos
- Android 8.0 o posterior (minSdk 26).
- Al abrir el archivo APK, permitir la instalación de aplicaciones de la fuente
  desde la que se descarga, si Android lo solicita.

## Limitación de la firma
La variante preview también recibe una firma de depuración temporal en CI.
**No está garantizado que una futura APK preview pueda instalarse encima**;
para actualizaciones permanentes se necesita una clave de firma estable,
protegida fuera de GitHub y configurada como secreto de compilación.
No almacenar claves de firma privadas en este repositorio.

Si la instalación sigue fallando, registrar el mensaje exacto de Android
y la versión del sistema antes de modificar o desinstalar ninguna app.
