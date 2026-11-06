# Ventana del Sistema: perfil (diseño aprobado)

Se integra la propuesta elegida por el propietario como interfaz **nativa Android Java/XML**, con controles reales. Se conserva el icono oficial de aplicación y ambas pantallas Full HD inmutables de carga.

## Flujo autorizado

1. El lanzador sigue siendo `SplashActivity` con horario permanente 00:00–11:59 / 12:00–23:59.
2. Al terminar la carga, `ProfileStore.hasProfile(context)` consulta la identidad local privada.
3. Si no existe: aparece `ProfileGateActivity` con la ventana y dos acciones reales.
4. **Crear nuevo perfil:** abre `ProfileCreateActivity`, captura únicamente un nombre de cazador, lo guarda localmente de manera explícita y abre `MainActivity`. En aperturas futuras, la pantalla de decisión se omite.
5. **Restaurar perfil:** muestra un diálogo informativo. No se afirma que exista copia o nube. El flujo de importación todavía no está definido ni implementado.
6. Las fases posteriores podrán ampliar el perfil con objetivos, nivel, preferencias y disponibilidad; ninguna de esas estadísticas se presupone ni conecta todavía al motor de progreso.

## Interfaz: capas independientes

- Fondo escénico: `res/drawable-nodpi/asterion_profile_background.webp`, derivado y **optimizado para el APK** a partir del fondo aprobado (540 × 960 px, WebP).
- Íconos: `asterion_profile_create.png`, `asterion_profile_restore.png`, `asterion_profile_seal.png`; PNG transparentes optimizados de 128 × 128 px. Los maestros PNG de 2048 × 2048 px y SVG permanecen en el paquete de diseño aprobado.
- Marcos: `NeonFrameDrawable` dibuja contornos octogonales con brillo azul o violeta, ajusta sus dimensiones a la pantalla y responde a estados de pulsación/foco.
- Botones: elementos táctiles nativos con textos, iconos, zonas de accesibilidad y callbacks distintos. No se usan hotspots de imagen ni una captura única.
- Sin conexiones a internet, cuentas o permisos adicionales.

## Integridad y límites

`tests/profile_gate_contract.py` comprueba huellas SHA-256 de los tres PNG, WebP y contratos de arranque, manteniendo además intactos los bloqueos `branding_icon_lock.py` y `branding_splash_lock.py`. La verificación se ejecuta en GitHub Actions. Las huellas protegen el recurso optimizado integrado, no sustituyen los maestros de diseño. La comprobación no evita que un administrador pueda cambiarla deliberadamente.

La restauración de perfil y el enlace de preferencias al motor `:progress-engine` son extensiones futuras, pendientes de especificación. El diseño de la pantalla de creación completa también se podrá ampliar mediante aprobación del propietario.
