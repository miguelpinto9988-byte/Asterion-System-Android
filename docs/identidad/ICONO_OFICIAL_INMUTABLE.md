# Regla obligatoria: identidad visual permanente de Asterion System

**Estatus:** decisión definitiva del propietario (5 de noviembre de 2026, hora de Perú).

## Regla principal — ICONO INMUTABLE

El diseño aprobado del ícono de Asterion System, que presenta un pequeño caballero sombra de mirada fría, casco oscuro, penacho rojo, espada luminosa y aura de llamas espectrales azul-violeta, es **la identidad visual definitiva y permanente de la aplicación**.

**Queda prohibido** sustituir, regenerar, reinterpretar, rediseñar, recolorear, recortar, deformar o alterar este ícono en versiones, actualizaciones, campañas, refactorizaciones, cambios de arquitectura, migraciones de Android o rediseños de interfaz. Ninguna fase posterior de desarrollo autoriza por sí misma una modificación. La pantalla de carga y el resto de las superficies visuales deben respetar este mismo ícono.

## Archivo aprobado y trazabilidad

- Recurso Android oficial actualmente incorporado: `app/src/main/res/drawable-nodpi/asterion_icon_oficial.webp`.
- Formato de distribución actual: WebP de 192 × 192 px, derivado del arte aprobado para el lanzador Android. La adaptación técnica de tamaño y compresión **no constituye autorización para rediseñar su apariencia**.
- SHA-256 del recurso integrado: `5852d7185e4676b9301761ec4a886fdebc88615331c01c837e34dc6c125dd3f3`.
- Fuente maestra original aprobada: PNG de 1254 × 1254 px, SHA-256 `3324896adbe1c5dae07170bfbd8156dbbf9d4d7c862889b4671178294569d02d`. Esta huella identifica la referencia de máxima resolución; el PNG original deberá conservarse como respaldo. La versión de 192 px incluida aquí no sustituye ese respaldo maestro.
- La referencia visual aprobada fue la **última imagen del caballero sombra**, con aura espectral, no los bocetos anteriores.

## Medidas técnicas de cumplimiento

1. El manifiesto Android debe apuntar a `@drawable/asterion_icon_oficial` para `android:icon` y `android:roundIcon`.
2. El recurso oficial no debe eliminarse ni cambiar de contenido.
3. `tests/branding_icon_lock.py` verifica la huella SHA-256 y las referencias del manifiesto.
4. El flujo `.github/workflows/android-ci.yml` ejecuta esa comprobación en cada `push` a `main`. Una diferencia provoca el fallo de CI y debe considerarse bloqueo de la actualización.
5. El mismo arte oficial será la referencia obligatoria cuando se diseñen la pantalla de carga y el flujo de creación de perfil. **No se implementan esas pantallas en esta decisión**.

### Alcance real de la protección

Esta política y la comprobación CI bloquean cambios accidentales o no autorizados que se sometan a la verificación; no hacen matemáticamente imposible que una persona con permisos administrativos reescriba el repositorio o el propio validador. Para reforzar su cumplimiento, se recomienda proteger `main` y exigir comprobaciones CI antes de cada fusión, además de restringir cambios en archivos de marca.
