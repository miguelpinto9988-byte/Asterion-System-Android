# MagicPath → Asterion Android: especificación de previsualización
**Estado:** handoff de diseño. No se ha creado ni modificado un lienzo MagicPath en esta consolidación.
**Objetivo:** visualizar las cuatro etapas del registro y el tablero principal sin alterar lógica ni recursos oficiales.

## Fuente de verdad
La funcionalidad y los nombres de campos existentes están en:
- `app/src/main/java/pe/asterionsystem/app/ProfileCreateActivity.java`
- `app/src/main/java/pe/asterionsystem/app/ProfileRegistrationSteps.java`
- `app/src/main/java/pe/asterionsystem/app/SimpleProfileStepThreeView.java`
- `app/src/main/java/pe/asterionsystem/app/ProfileWizardTheme.java`
- `app/src/main/res/layout/screen_profile_create.xml`

MagicPath sirve como **referencia visual interactiva**, no como reemplazo de Java/XML ni como almacenamiento de perfiles. Toda previsualización HTML o JS debe usar valores de demostración y no prometer sincronización con Android.

## Dirección visual aprobada para el registro
- **Blanco predeterminado** en pasos 1–4: fondo `#FFFFFF`, primer plano `#101010`, borde `#CECECE`, texto secundario `#5F5F5F`.
- **Negro opcional**, seleccionable **solo en paso 3**: fondo `#080808`, primer plano `#F8F8F8`, borde `#454545`, texto secundario `#B9B9B9`.
- Etiquetas en negrita; botones con altura mínima **48 dp**; campos reales, encabezados visibles y navegación Atrás/Siguiente.
- Tarjetas sin degradados ni arte superpuesto. La única excepción cromática del formulario es la clasificación del **calendario semanal**, con seis colores y leyenda distintiva.
- Mantener la identidad del ícono oficial intacta. **No introducir íconos o personajes nuevos** en las pantallas de carga oficiales.
- La biblioteca, el progreso y el inicio pueden conservar sus recursos nativos actuales; no imponer el monocromo del registro sobre toda la aplicación.

## Pantallas requeridas para la previsualización
1. **Identidad**: nombre y edad obligatorios; estatura, peso, género y avatar opcionales. Mostrar validación y estados vacíos sin rellenar datos ficticios como reales.
2. **Entrenamiento**: experiencia, objetivo, texto libre, modalidad, misiones (1–7), descanso, rondas/series, duración, calendario de seis categorías y equipamiento. Usar los valores/etiquetas existentes.
3. **Personalización**: conmutador Blanco/Negro, seis colores de contorno, cinco acompañantes, cinco siluetas y cinco fondos de tarjeta. Usar los **IDs de opciones existentes**; no crear otras alternativas.
4. **Activación**: resumen legible, casilla de confirmación y botón inactivo hasta aceptar. Después del guardado local, pantalla «Sistema activado» con entrada al Inicio.
5. **Inicio / navegación**: accesos a Perfil, Biblioteca (817 variantes), Misiones diarias, Monarca optativo y Progreso, distinguiendo claramente estado de propuesta y estado aceptado.

## Interacciones que debe simular el lienzo
- Navegar hacia delante y atrás conservando los datos introducidos.
- Cambiar el tema desde el paso 3 y conservarlo al visitar pasos anteriores.
- Mostrar errores de campos requeridos e impedir guardar sin confirmación final.
- Mantener el calendario semanal con sus seis colores incluso en tema monocromo.
- Probar estado sin perfil, perfil ya guardado, cancelación de Monarca y día de descanso.
- No usar imágenes planas con hotspots en lugar de controles; no simular que el lienzo escribe en SQLite.

## Previsualización y aprobación
Crear vistas de **360, 390 y 420 dp** de ancho y comprobar: etiquetas sin recorte, contraste de ambos temas, teclado/tap targets, orientación, scroll, lector de pantalla y coherencia de componentes.
La propuesta de Media3/MP4 (PR #2) será una **previsualización separada**; nunca debe reemplazar automáticamente las dos imágenes permanentes del splash.

## Criterios de aceptación antes de trasladar cambios a Android
- Aprobación del propietario sobre las capturas y estados.
- Correspondencia uno a uno con las claves de los campos del Android actual.
- Conservar nombres, edad, borrador, fotos, calendario, progreso y confirmación; no borrar datos previos.
- Implementación nativa y pruebas reales posteriores. La aprobación del diseño **no** equivale a aprobación de una release.

## Prompt reutilizable para MagicPath
> Diseñar una previsualización móvil interactiva de Asterion System con los cuatro pasos nativos de registro y tablero principal. El registro usa Blanco por defecto (#FFFFFF/#101010) y Negro alternativo (#080808/#F8F8F8), selector de tema solo en el paso 3, botones táctiles de mínimo 48 dp, tipografía en negrita y seis colores exclusivos para categorías del calendario. Mantener los campos, opciones y confirmación listados aquí; no modificar ícono ni pantallas de carga oficiales. Probar a 360, 390 y 420 dp. Los datos son simulados y el diseño no sincroniza con Android.
