# Registro de cazador — pasos 1 a 4

## Alcance implementado
- `ProfileCreateActivity` es una pantalla Android Java/XML completamente nativa; el paso 1 recrea el diseño aprobado, con fondo original suavemente oscuro/difuminado y recursos separados.
- El fondo se conserva en `drawable-nodpi/asterion_reg_background.webp` y el avatar icono en `drawable-nodpi/asterion_reg_profile.webp`. El personaje lateral reutiliza el recurso existente `asterion_guardian`, sin duplicarlo.
- El marco usa `NeonFrameDrawable` (dibujado por Android, no sobre una captura). Los contornos de los campos usan `asterion_reg_field.xml`.
- Los ocho símbolos de la forma se referencian mediante `AsterionRegistrationIcons`. El perfil usa el WebP original; los otros siete usan composiciones vectoriales nativas con marco compartido: `asterion_reg_shared_tile.xml` y `asterion_reg_*_glyph.xml`. Los PNG maestros individuales de la previsualización se entregan en un paquete aparte para sustituir las versiones vectoriales si se aprueba una correspondencia visual exacta.
- Los recursos están predefinidos en el paquete Android. No hay descarga, red, IA o regeneración de ilustraciones al abrir la aplicación.
- El selector de fotografía abre el selector de documentos Android con acceso persistente a la imagen, sin permisos de almacenamiento ampliados.

## Comportamiento del asistente
1. **Identidad:** nombre y edad obligatorios; fotografía, estatura, peso y género opcionales. Nunca se usa peso/estatura/género para decidir rangos o intensidad física.
2. **Entrenamiento:** experiencia, objetivo y texto libre, tiempo de descanso, misiones/rutinas, rondas/series, calendario por pulsaciones y equipamiento.
3. **Personalización:** tema, seis colores de borde, cinco acompañantes provisionalmente nominales, silueta y fondo de tarjeta. Las ilustraciones de estos personajes no se inventan antes de la siguiente aprobación.
4. **Activación:** resumen real de los datos, confirmación explícita, guardado atómico y pantalla `SISTEMA ACTIVADO` antes de entrar a Inicio.

Los tres últimos pasos tienen interfaces **nativas funcionales provisionales** y se rediseñarán visualmente tras aprobar su arte final. El borrador se guarda localmente en `asterion_registration_draft_v2`, sin activar al usuario. Solo `ProfileStore.saveRegistration` escribe el perfil completo y el nombre de cazador de manera atómica. Perfiles antiguos que ya tengan nombre guardado no se eliminan ni obligan a repetir el registro.

## Garantías de continuidad y tareas pendientes
- Icono oficial del lanzador y ambas pantallas de carga permanecen protegidos por sus hashes originales y el CI.
- El fondo y el icono de avatar aprobados de la primera pantalla tienen SHA-256 bloqueados por `tests/profile_registration_step1_contract.py`.
- Para que todas las representaciones de iconos coincidan píxel a píxel con los PNG maestros, queda una tarea específica de sustituir los siete vectores funcionales por los siete WebP originales optimizados; requiere transferir esos archivos binarios al repositorio y volver a validar la compilación.
- Las preferencias ya se guardan en el perfil; todavía falta el adaptador de lectura desde `:progress-engine`, las reglas de generación y la revisión especializada del catálogo.
