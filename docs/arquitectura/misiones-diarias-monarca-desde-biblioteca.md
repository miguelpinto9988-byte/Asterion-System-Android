# Motor de misiones desde la biblioteca real — integración v1

## Pantalla de Inicio aprobada

1. Misiones Monarca — acceso a previsualizar y autorizar.
2. Tarjeta de perfil — nombre y preferencias guardadas.
3. Misiones diarias — propuestas basadas en la categoría del día.
4. Progreso y evolución — conserva el módulo de seguimiento existente.
5. Biblioteca de ejercicios — acceso nativo a las 39 familias, 80 tipos y 445 variantes reales.

Los antiguos accesos a planificador, catálogo clásico, configuración y estado local siguen disponibles como herramientas, sin alterar sus rutas ni los datos SQLite.

## Flujo de datos auténtico

```text
Perfil de cuatro pasos (ProfileStore.getRegistration)
  └─ semana, objetivo, experiencia, equipamiento, cantidad opcional 1..7
     + Biblioteca versionada (TSV; 39 familias, 80 tipos, 445 variantes)
       └─ AssetExerciseCatalogRepository (hilo de trabajo; solo lectura)
           └─ ExerciseCatalog → MissionPlanner (Java puro)
               ├─ daily(fecha local): 1 día; respeta Descanso
               └─ monarch(fecha): propone 5 ejercicios para un ciclo opcional
                   └─ MonarchSessionStore (SharedPreferences privado)
                      ├─ preview: no activo
                      ├─ accept: únicamente después de revisar y confirmar
                      ├─ declined: no activo
                      ├─ active: 5 días desde la aceptación
                      └─ abandon: sin penalización
```

El motor nunca genera variantes de nivel técnico 4 o 5, ni variantes con recomendación de supervisión. El nivel elegible se limita además según experiencia. No inventa equipo, ni interpreta estatura, peso o edad como medidas de rendimiento. Si el perfil solo contiene días de descanso, no genera obligaciones; si no hay variantes compatibles, muestra un estado vacío.

### Cadencia

- **Diarias**: se vuelven a calcular cuando se abre o reanuda la pantalla en una fecha local distinta. No se necesitan permisos ni un servicio de fondo.
- **Monarca**: la vista previa se conserva durante cinco días; el usuario puede rechazarla. Una vez aceptada, la misión finaliza cinco días naturales después de la fecha de aceptación. Al vencer, se ofrece otra propuesta sin activarse automáticamente.
- **Cambio de perfil durante Monarca**: modificaciones del calendario, objetivo, experiencia o equipo invalidan la autorización anterior y requieren una nueva revisión.
- **Privacidad**: no hay conexión a servicios externos y los datos se almacenan localmente.

### Limitaciones explícitas de esta entrega

1. Las misiones son **propuestas de actividades**. No prescriben series, repeticiones, pesos, intensidades, retos de resistencia ni castigos por inactividad. No se implementa la cuota de 75 a 100 repeticiones por ejercicio solicitada inicialmente, ya que no es una exigencia segura de aplicar universalmente.
2. La creación de propuestas sí está conectada al catálogo real. Su **finalización** aún no se escribe como `ProgressEntry` en la base de datos antigua, porque los IDs `variant_id` de los recursos TSV no son intercambiables con los IDs del catálogo SQLite `catalog_items`.
3. No se han añadido notificaciones, trabajos en segundo plano, sincronización ni un servicio de IA. Las propuestas se generan localmente mediante reglas verificables.
4. Para que las misiones completadas participen en estadísticas, hay que diseñar una **migración idempotente o tabla de correspondencias de IDs**, validarla con claves foráneas y conservar el historial anterior.
5. No se modifican el icono oficial, los recursos de splash ni el esquema de SQLite v5.

### Seguridad y verificaciones

- `:progress-engine:test`: comprueba cinco candidatos, filtros reales, días de descanso, límite técnico, resultado determinista y ausencia de equipo inventado.
- `tests/missions_from_catalog_contract.py`: valida el acceso desde el Inicio, el uso de recursos reales, la autorización explícita, el abandono y la duración de cinco días.
- CI de `main`: compila `pe.asterionsystem.app` y ejecuta tests, Lint y prueba de inicio en Android.
- Pendiente de ampliar: pruebas instrumentadas de autorización, reinstalación, cambio de zona horaria/fecha y flujo de registro real en el dispositivo.

**MagicPath:** la edición del canvas sigue bloqueada por cuota de llamadas; el prototipo HTML independiente permite examinar el diseño mientras tanto.
