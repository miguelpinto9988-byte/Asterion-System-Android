# Tipografia global de Asterion System

Regla aprobada: todos los textos editables de la interfaz Android deben mostrarse **en negrita**: titulos, descripciones, etiquetas, formularios, botones y opciones.

- `Theme.AsterionSystem` incorpora `Asterion.BoldTextView`.
- Los estilos de botones y campos de texto incorporan `android:textStyle="bold"`.
- Todos los controles textuales de las pantallas XML existentes poseen el atributo explicito `android:textStyle="bold"`; los nuevos deben hacer lo mismo.
- `NativePreferencesApplier` preserva la familia tipografica (serif/sans) y obliga al peso negrita al presentar pantallas.
- La prueba `tests/global_bold_contract.py` corre en GitHub Actions y detecta nuevas pantallas no conformes.
- Los textos rasterizados en imagenes oficiales inmutables (icono de la app y dos splash) no son widgets y se dejan intactos.
- Los disenos de siluetas y fondos para la futura tarjeta del cazador aun requieren aprobacion; no se agregan en este cambio.
