# Fase 07 — Catálogo de contenidos y actividades

## Objetivo, tecnologías y límites

Se añade un catálogo consultable de elementos clasificados como **contenido** o **actividad**. La interfaz Android utiliza exclusivamente XML y controladores Java; el simulador HTML se entrega por separado para revisión. No se añaden multimedia, planificación, seguimiento, programación de tareas, sincronización ni reglas de negocio específicas.

Los únicos elementos incluidos inicialmente son **cuatro fichas técnicas de demostración**, creadas para verificar la navegación y las consultas. No son un catálogo definitivo ni material importado de Hunter System.

## Organización de módulos

```text
:core (Java puro)
  catalog/
    CatalogItemKind, CatalogCategory, CatalogItem, CatalogQuery
    CatalogRepository, BackgroundCatalogOperations
  navigation/
    NavigationRoute (+ CATALOG, CATALOG_DETAIL)
:app (Android Java)
  data/
    AsterionDatabaseHelper (+ migración v2→v3)
    SQLiteCatalogRepository
  presentation/
    NavigationRenderer, CatalogPresenter
  MainActivity
  res/layout/screen_catalog.xml
  res/layout/screen_catalog_detail.xml
  res/layout/screen_home.xml (acceso al catálogo)
  res/values/strings.xml, catalog_arrays.xml
```

El módulo `:core` no importa ninguna API Android y contiene un modelo inmutable y validado. El repositorio `CatalogRepository` es de solo lectura, por lo que la fase 07 no incorpora formularios de edición o publicación de contenidos.

## Clasificación

- Categorías: `general`, `conocimiento`, `practica`, cada una con orden definido.
- Tipos: `content` y `activity`, con valores de almacenamiento estables.
- Etiquetas: normalizadas en `catalog_tags` con tabla de asociación `catalog_item_tags`.
- Filtros: texto (título, descripción, etiquetas), categoría, tipo.
- Consulta paginada: máximo 50 filas por página, ordenadas por título y luego ID.

## Integración con la navegación

El flujo es `HOME → CATALOG → CATALOG_DETAIL`. La pantalla secundaria de fase 04 sigue accesible. La ficha seleccionada se conserva temporalmente en `Bundle`; también se preservan texto, categoría y tipo al recrearse la actividad.

`MainActivity` selecciona el XML adecuado a partir de `NavigationController`; `NavigationRenderer` infla el XML. `CatalogPresenter` carga categorías, aplica filtros y presenta resultados. Las consultas son asíncronas por `BackgroundCatalogOperations`, con una sola cola de lectura y entrega al hilo UI mediante `Handler`. Las respuestas obsoletas no actualizan una pantalla que ya se reemplazó.

Al cerrar la Activity, el worker termina los trabajos pendientes y cierra el helper SQLite en la misma cola. Esto no introduce procesos, tareas periódicas ni notificaciones.

## Vista previa

El archivo `Asterion-System_Fase-07_Previsualizacion_Interactiva.html` es un artefacto externo. Reproduce textos, colores y flujos de las pantallas XML, permite simular la navegación y ofrece laboratorios independientes para fases 05 y 06. **No usa SQLite ni ejecuta el Java real**; no debe confundirse con una APK ni con una prueba Android.

## Decisiones pospuestas

La fase 03 aún no ha fijado el diseño visual final. La fase 08 de planificación no está aprobada. Las fichas de catálogo no se pueden programar ni completar y no contienen contenido multimedia.
