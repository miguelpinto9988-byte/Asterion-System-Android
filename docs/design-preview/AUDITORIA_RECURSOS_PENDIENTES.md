# Auditoría de recursos pendientes — registro de perfil

Fecha: 2026-11-15
Auditoría basada en el boceto aprobado de los cuatro pasos y la biblioteca WebP consolidada.

## Hecho en la rama de diseño
- Inventario de la biblioteca WebP y nombres de los recursos, sin recursos repetidos por pantalla.
- `ProfileRegistrationSteps.java`: fondos semánticos pastel y tintas más oscuras en las seis categorías; nombres de assets `asterion_ic_{tren_superior,tren_central,tren_inferior,movilidad,cardio,descanso}`, conservando el orden del valor `week`.
- `ProfileWizardTheme.java`: preserva el color definido por la celda semanal al cambiar de tema.
- El calendario nativo ya alternaba las seis categorías con cada toque; no se modificó el almacenamiento ni el orden.
- Estos cambios **no se han fusionado** ni probado en APK.

## Recursos incluidos en paquete local (NO importados a GitHub)
- 74 iconos generales WebP (37 negros y 37 blancos).
- 6 iconos de entrenamiento a color, más oscuros que el fondo de la celda. Movilidad es un estiramiento, no un pictograma de accesibilidad.
- 2 cabeceras, 2 retratos conceptuales, 2 marcos de avatar.

## Recursos gráficos pendientes
- **Retrato final del cazador**: la imagen disponible es provisional. Falta verificar fidelidad al personaje que el propietario escogió en la imagen de referencia.
- **Acompañantes del sistema**: las miniaturas ilustradas de las opciones existentes (Igris, Beru, Tank, Tusk, Iron); aún no se suministran como WebP aislados en el paquete maestro.
- **Marcos de tarjetas del paso 3**: las variantes visuales de borde dorado, violeta, azul, etc., no están separadas como recursos artísticos aprobados.
- **Fondos de tarjeta**: miniaturas independientes para las cinco opciones vigentes (ruinas, niebla, obsidiana, aurora, bosque).
- **Diseño de tarjeta de cazador**: composición y capas de avatar, compañero, borde y fondo; no existe implementación nativa equivalente al boceto.
- **Componentes de pantalla**: fuentes, espaciado, estados de botón/campo, tarjetas y ventanas; deben resolverse con XML/Java, no con imágenes planas.
- **Resumen del paso 4**: falta render gráfico de las siete celdas y tarjeta nativa actualizada.
- Iconos especiales del boceto (sol con color, corona dorada y resaltados) podrían necesitar variantes coloreadas propias si las versiones generales negro/blanco no bastan.

## Validaciones bloqueadas por ausencia de binarios en repositorio
1. Transferir los WebP del ZIP al repositorio, con nombres únicos compatibles con Android.
2. Conectar la tabla de categorías a los iconos WebP y a la leyenda.
3. Compilar `assembleDebug`, probar fotos elegidas y comprobar temas claro y oscuro.
4. Revisar capturas de los cuatro pasos y comparar con las maquetas antes de hacer merge.

Este archivo distingue **recursos ya creados** de **recursos aún no diseñados o no integrados**.
