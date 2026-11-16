> **DOCUMENTO HISTÓRICO / SUPERADO.** Los marcos ornamentales se descartaron después de esta versión. No seguir sus instrucciones de marcos ni importar sus paquetes. La especificación vigente es [REGISTRO_DEFINITIVO_SIN_MARCOS.md](REGISTRO_DEFINITIVO_SIN_MARCOS.md) (94 WebP, sin marcos).

# Paquete visual definitivo v2 — aprobado el 15 de noviembre de 2026

## Recursos aprobados y verificados localmente

**99 archivos WebP**: cuatro retratos transparentes (sin círculo), cinco fondos 16:9, ocho marcos 16:9 **redibujados individualmente desde cero** (NO recortes de lámina), 80 iconos y dos cabeceras de tema claro/oscuro.

Archivo maestro entregado en el chat: `Asterion_Paquete_Visual_Definitivo_v2.zip`. Contiene `MANIFIESTO.json` con dimensiones, longitud en bytes y SHA-256 de cada archivo. Ese manifiesto es la fuente de integridad y permite garantizar que se transfieran los binarios correctos, sin cambiar sus diseños.

**Estado verificable en esta rama:** se añadieron/importaron los mapeos Java, el selector de marcos, la vista previa horizontal nativa con tres capas y un importador para los 99 archivos. **Los WebP NO están todavía físicamente subidos a GitHub.** No se fusionó esta rama con `main` y no se compiló un APK.

## Cargar el paquete en un clon local con Git

1. Descargar del chat `Asterion_Paquete_Visual_Definitivo_v2.zip`.
2. Abrir el repositorio Android local y ejecutar `git switch design/profile-wizard-components-20261115`.
3. Ejecutar `python3 scripts/import_asterion_webp.py /ruta/Asterion_Paquete_Visual_Definitivo_v2.zip .`.
4. Revisar `git status`; el script agrega los archivos a `app/src/main/res/drawable-nodpi/` con nombres Android válidos. No elimina archivos y **se niega a sobrescribir** los que tengan contenido diferente.
5. Compilar y verificar selección, persistencia y temas. Luego `git add app/src/main/res/drawable-nodpi`, `git commit -m "assets(profile): importar 99 recursos WebP definitivos"` y `git push origin design/profile-wizard-components-20261115`.
6. Confirmar en el PR los 99 recursos antes de marcar la integración terminada.

## Mapeo de marcos y tarjetas

| Opción | ID persistido | Nombre WebP final |
| --- | --- | --- |
| Tecnológico | tecnologico | asterion_frame_tech.webp |
| Celestial | celestial | asterion_frame_celestial.webp |
| Épico | epico | asterion_frame_epic.webp |
| Natural | natural | asterion_frame_nature.webp |
| Arcano | arcano | asterion_frame_arcane.webp |
| Glacial | glacial | asterion_frame_glacial.webp |
| Oriental | oriental | asterion_frame_oriental.webp |
| Legendario | legendario | asterion_frame_legendary.webp |

Para seleccionar el marco se añadió la clave `card_frame` al borrador del perfil; se preservan `card_background`, `card_silhouette` y `avatar_uri`. Los marcos son WebP **RGBA 1600×900** con interior transparente. El retrato se pinta encima del paisaje y el marco encima del retrato.

La ausencia de un recurso durante la instalación debe mostrar un fondo de reserva, no borrar la elección guardada.

## Pendiente para cerrar el módulo de perfil

1. Subir efectivamente los 99 binarios WebP a GitHub y verificar su SHA-256.
2. Revisar cómo se ven los 8 marcos en distintos tamaños de pantalla, y que la app compile sin errores.
3. Terminar las ilustraciones de acompañantes (sus opciones existen, pero su arte no está aprobado).
4. Pulir los componentes de los cuatro pasos y conectar la vista final con la información guardada.
5. Ejecutar pruebas manuales del tema claro/oscuro, persistencia y migración de perfiles anteriores.
6. Revisar el PR y fusionar en `main` solo tras validar los pasos anteriores.

El usuario aprobó específicamente cuatro retratos y rechazó añadir un quinto. No recuperar el antiguo personaje esmeralda.
