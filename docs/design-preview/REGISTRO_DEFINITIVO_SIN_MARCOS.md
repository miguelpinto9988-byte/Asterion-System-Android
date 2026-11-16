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

**94 WebP:** 4 retratos originales cuadrados, 5 paisajes horizontales, 5 acompañantes, 2 cabeceras y 78 iconos de interfaz/categorías. El paquete local `Asterion_Android_94_WebP_Sin_Marcos.zip` incluye `MANIFIESTO_ANDROID.json` con hashes SHA-256. Los 8 recursos `asterion_frame_*.webp` y los dos iconos `asterion_*_ic_marco.webp` han quedado excluidos.

Para importar en un clon de la rama de diseño:
```bash
python3 scripts/import_asterion_all_resources.py /ruta/Asterion_Android_94_WebP_Sin_Marcos.zip .
git add app/src/main/res/drawable-nodpi
git commit -m "assets(profile): integrar 94 WebP sin marcos"
git push origin design/profile-wizard-components-20261115
```

El script verifica todos los hashes, rechaza los marcos y **no sobrescribe recursos diferentes**.

## Estado verificable

- Las correcciones Java, el importador de 94 recursos, el resumen y esta nota están guardados en GitHub en `design/profile-wizard-components-20261115`, sin modificar `main`.
- **Los 94 archivos WebP binarios todavía no se han transferido a GitHub**. Su preparación en un ZIP local no equivale a la subida de archivos de imagen al repositorio.
- **APK pendiente de compilación y pruebas** con perfiles nuevos y ya existentes antes de aprobar/mergear el PR.
- No reintroducir marcos ni reconstruirlos en próximas entregas, salvo solicitud expresa nueva del usuario.
