# Fase 03 — Verificación de fuente y pendientes

## Contratos de integración

1. Se mantienen los IDs de controles originales de todas las pantallas para no romper los presentadores Java.
2. El tema cambia de `Theme.Material.Light.NoActionBar` a `Theme.Material.NoActionBar`, conservando el nombre `Theme.AsterionSystem` usado por el manifiesto.
3. El diseño utiliza solo `res/values`, `res/layout` y `res/drawable` XML nativos.
4. Se mantienen los contratos de accesibilidad de fase 15 y los objetivos táctiles de 48dp.
5. `tests/phase03_visual_source_contract.py` permite comprobar el diseño mediante Python estándar, sin Gradle.

## Pendiente

- Verificar en Android real estilos y drawables con aapt2, y ajustar si alguna propiedad no es compatible.
- Medir contraste de estados deshabilitados, texto secundario, spinners y listas dinámicas.
- Revisar escalado de fuentes, teclado, orientación, TalkBack y pantallas pequeñas.
- Validar imágenes originales de personajes antes de incluirlas en Android.
- Revisar la previsualización HTML existente con el usuario antes de considerar el diseño visual definitivamente cerrado.

No se ejecutó Gradle, no se compiló APK y no se activó GitHub Actions.
