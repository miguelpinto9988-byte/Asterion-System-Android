# Fase 14 — Contratos conceptuales para una sincronización futura

**Documento de especificación, no implementación.** El alcance aprobado de fase 14 excluye servidor, autenticación, red, envío de registros, colas de subida, exportación/importación y sincronización remota automática o manual.

## Puertos que una fase futura tendría que diseñar y aprobar

Si se decide incorporar sincronización, habrá que definir un contrato separado de los repositorios locales que contenga:

- **Identidad estable:** tipo de entidad, identificador local, identificador remoto asignado de forma segura y ámbito del usuario.
- **Versión y causalidad:** versión o secuencia de revisión por registro y control de concurrencia optimista; las marcas de tiempo locales no garantizan por sí solas orden global.
- **Operaciones y eliminación:** semántica de crear, actualizar y eliminar, incluyendo decisiones explícitas sobre marcadores de eliminación (*tombstones*) y privacidad.
- **Resolución de conflictos:** regla de precedencia transparente, intervención del usuario para resultados incompatibles y prevención de duplicados de actividades programadas.
- **Compatibilidad:** versión de esquema/contrato, migraciones compatibles, parámetros de zona horaria y límites de carga.
- **Seguridad:** autorización por identidad, cifrado en tránsito, política de almacenamiento local/remoto, revocación y borrado de datos.
- **Control del usuario:** consentimiento, información de qué datos salen del dispositivo, opción de desconexión y estado claro de sincronización.
- **Pruebas:** repetición idempotente, caída entre operaciones, conectividad intermitente, reordenación de solicitudes, revocación y dos dispositivos simultáneos.

## Entidades candidatas a evaluar

`catalog_items` (solo si existiera autoría remota), `planned_activities`, `tracking_results` y preferencias seleccionadas; la base técnica `technical_records` no se debe sincronizar por defecto. Antes de enviar cualquier dato será necesaria una política de privacidad y un contrato de seguridad aprobados.

**Esta fase no crea clases, tablas, servicios, tareas programadas ni endpoints de sincronización.** No se han añadido permisos Android de acceso a internet.
