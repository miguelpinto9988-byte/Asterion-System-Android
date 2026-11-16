# Configuración definitiva SIN marcos — 15/11/2026

El usuario ha descartado por completo los marcos ornamentales de la tarjeta del cazador. Este documento **sustituye** cualquier plan, maqueta, inventario, paquete e instrucción anterior que incluyera los ocho marcos o la opción `card_frame`.

## Implementación en esta rama

- Paso 3: retiradas la sección `Marco decorativo` y sus ocho opciones. El color de acento sigue disponible como color de la aplicación, **no** como marco.
- Tarjeta: paisaje horizontal (16:9), retrato original cuadrado sin máscara/crop circular, acompañante y texto. **Sin ImageView de marco**.
- Paso 4: retirado el campo Marco del resumen.
- `ProfileCardVisualAssets`: eliminado `frameDrawable`; se mantienen `hunterDrawable`, `backgroundDrawable` y `companionDrawable`.
- Integridad de datos: **no borrar** `card_frame` si ya estaba en un perfil antiguo; simplemente dejar de leer, dibujar o ofrecer esa clave. Se preservan las demás preferencias y las URI de fotos.
- El HTML local `Asterion_Registro_Perfil_Sin_Marcos.html` muestra las cuatro etapas y mantiene 4 cazadores, 5 paisajes, 5 acompañantes, 2 temas e iconos aprobados. **Prueba en Chromium:** sin errores JS, sin opción de marcos, 4/5/5 elecciones cargadas, retrato 1024×1024, fondo 1600×900, viewport 390 px sin desbordamiento.

## Recursos gráficos vigentes

**94 WebP:** 4 retratos originales cuadrados, 5 paisajes horizontales, 5 acompañantes, 2 cabeceras y 78 iconos de interfaz/categorías. El paquete local `Asterion_Android_94_WebP_Sin_Marcos_Cuadrados.zip` incluye `MANIFIESTO_ANDROID.json` con hashes SHA-256. Los 8 recursos `asterion_frame_*.webp` y los dos iconos `asterion_*_ic_marco.webp` han quedado excluidos.

**Integrados directamente en GitHub:** los 94 `.webp` individuales ya figuran en `app/src/main/res/drawable-nodpi/` de la rama de diseño. Commit de recursos: `50747b9f18e11a0715de29428ba1cb04583dffe2`. El ZIP solo fue un medio de transferencia; no se ha agregado al repositorio. Se verificó presencia y tamaño de los 94 archivos contra el manifiesto. El script de importación queda como herramienta de recuperación, no es necesario para esta rama.

## Estado verificable

- Las correcciones Java, el importador de 94 recursos, el resumen y esta nota están guardados en GitHub en `design/profile-wizard-components-20261115`, sin modificar `main`.
- **Los 94 WebP definitivos SÍ están subidos a GitHub** como archivos separados; comprobados por nombre y tamaño en la rama. Junto a 10 WebP anteriores, la carpeta contiene 104 WebP. No contiene marcos ni ZIP.
- **APK pendiente de compilación y pruebas** con perfiles nuevos y ya existentes antes de aprobar/mergear el PR.
- No reintroducir marcos ni reconstruirlos en próximas entregas, salvo solicitud expresa nueva del usuario.

## Corrección específica: dos cazadores que estaban mal en el ZIP anterior

Se comprobó que los WebP del primer paquete Android aún tenían recortes circulares en `asterion_hunter_blue.webp` y `asterion_hunter_gold.webp`. Fueron sustituidos en el paquete actual por las versiones cuadradas transparentes de 1024×1024 aprobadas en el HTML reciente, sin tocar los demás 92 recursos. Los cuatro retratos tienen canal alfa RGBA.

SHA-256: azul `8aefb2b619...` y blanco/dorado `4f66be94d2...`. Los SHA-256 **completos** están en `MANIFIESTO_ANDROID.json` dentro del ZIP de 94 recursos. El importador se niega a sobrescribir versiones antiguas diferentes; si existen, realizar copia de seguridad y reemplazo revisado explícitamente, no automáticamente.
