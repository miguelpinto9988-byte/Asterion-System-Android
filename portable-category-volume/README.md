# Motor portable: organización por categorías y progresión de volumen

Proyecto **Java 17 / Gradle independiente**, extraído de Asterion System para
adaptarlo a otra aplicación. Los originales permanecen intactos. Ningún archivo
importado requiere Android, Canvas, SQLite, SharedPreferences o servicios web.

## Composición

- **Categorías:** `ExerciseCategoryMapper` normaliza etiquetas históricas en
  cinco códigos: `tren_superior`, `tren_central`, `tren_inferior`,
  `movilidad`, `cardio`. `ExerciseCategoryIndex` mantiene el árbol
  categoría → familia → tipo → variante y búsquedas inversas por ID.
- **Catálogo:** `ExerciseCatalogLoader` lee y valida los TSV; incluye
  155 familias, 474 tipos y 1.257 variantes, con intensidad y deportes.
- **Volumen:** `ExerciseVolumeProfileCatalog` tiene una fila por variante,
  su unidad REPETITIONS/SECONDS, series y cantidad inicial, límites y
  multiplicador. `VolumeProgressionEngine` ofrece referencias opcionales
  basadas en registros completados y esfuerzo 1..10. Una variante no hereda
  cantidades de otra.
- **Revisión:** `VolumeResetPolicy` decide reinicio en hitos 7/14/21 al
  cambiar la selección; `CategoryEffortPolicy`,
  `WeeklyCategoryChallengePolicy` y `TechnicalCheckpointPolicy` analizan
  cada categoría sin mezclar historiales.
- **Adaptadores:** `PortableTrainingSettings` valida preferencias iniciales;
  `PortableTrainingModule` carga los datos y presenta métodos de consulta.

## Compilar

Desde la raíz del repositorio (si Gradle está instalado):

```bash
gradle -p portable-category-volume clean test jar
```

O copia esta carpeta a un repositorio nuevo. Para consumirla desde Android,
registra el proyecto Java en el `settings.gradle` de la app receptora o añade
el JAR resultante a sus dependencias. No añadir simultáneamente este módulo
y el `progress-engine` completo de Asterion al mismo classpath: sus clases
conservan nombres de paquete idénticos.

## Configuración inicial

Editar `src/main/resources/configuracion-inicial.properties` (también es
posible pasar `Properties` a `PortableTrainingSettings.from(...)`).

| Clave | Predeterminado | Uso |
|---|---|---|
| `categorias_habilitadas` | cinco categorías | grupos mostrados |
| `nivel_experiencia` | `principiante` | preferencia para nuevo planificador |
| `actividades_diarias` | 3 | preferencia de UI, aún no crea rutinas |
| `sugerencias_volumen` | `false` | requiere activación explícita |
| `zona_horaria` | `system` | fechas de la nueva app |
| `idioma` | `es` | idioma de UI, no traducción automática del catálogo |

El motor conserva los **códigos** de categoría y las reglas originales, que no
son parámetros libres. Personalizar nuevas categorías o reglas de progresión
requiere alterar el modelo y sus pruebas.

Uso rápido:

```java
PortableTrainingModule kit = PortableTrainingModule.loadDefaults();
var families = kit.families("movilidad");
var variants = kit.variants("tren_inferior");
var initial = kit.initialReference("curl_biceps_v01");
// review(...) solo devuelve Optional<Suggestion> si la app activa el módulo
// y le proporciona historial real; nunca registra ni ejecuta una sesión.
```

El traslado NO incluye perfiles personales, historiales, datos SQLite ni
pantallas de Asterion. La app de destino debe crear onboarding, permisos,
guardado de sesiones, calendario, selección de ejercicios y controles de
consentimiento. Ver `MIGRACION.md` para dependencia/retirada segura.

Los valores de volumen son referencias de interfaz, no recomendaciones
médicas ni cantidades exigidas; se deben revisar por variante.
