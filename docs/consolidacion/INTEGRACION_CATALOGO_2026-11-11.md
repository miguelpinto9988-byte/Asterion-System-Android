# Asterion System: conciliación del catálogo (11-11-2026)

**Estado:** rama experimental, no corresponde a una versión distribuible. No fusionar a `main` sin superar CI, revisión editorial y aprobación explícita.

## Origen
- Base de la rama: PR #14, apilada sobre #13 y #11; conserva el catálogo curado de 56 deportes y sus 125 etiquetas con fuentes.
- PR #12: ampliación independiente con 30 familias, 120 tipos y 240 variantes que no colisionan por ID con la rama base.
- Se conserva el prefijo de registros de #11: 125 familias, 354 tipos, 1.017 variantes y 512 intensidades.

## Catálogo acumulado en esta rama
- 155 familias, 474 tipos, 1.257 variantes y 752 etiquetas de intensidad.
- Exactamente cinco categorías canónicas; se convierten exclusivamente en las 30 familias nuevas `tronco → tren_central` y `piernas → tren_inferior`.
- Se agregan 30 rótulos de foco funcional amplios y no clínicos en `ExerciseMuscleFocus`.

## Restricción durante la revisión
Las 240 variantes traídas de #12 se etiquetan con `supervision=1` **solo en esta rama**. Siguen visibles en el catálogo, pero no pueden proponerse automáticamente por el motor de misiones ni por el generador de rutinas. Esta restricción temporal evita recomendar movimientos específicos antes de revisar individualmente material, nivel, accesibilidad e intensidad. No prescribe cargas, intensidad ni metas.

## Problema previo detectado en #12
La última CI de #12 falló en `AsterionDomainEngineTest.progressEvaluationDoesNotWriteRecordsOrPreferences`: esperaba 130% para un caso que en `main` y #11 espera 100%. La rama de consolidación conserva la expectativa 100%, pendiente de confirmación con CI completo.

## Trabajo restante
1. Reconciliar las 25 etiquetas de disciplina de #12 con los 56 códigos deportivos de #11 sin atribuir referencias de procedencia incorrectas.
2. Revisar uno por uno los movimientos manuales antes de considerar habilitarlos para sugerencias.
3. Actualizar contratos, JUnit y pruebas instrumentadas, comprobar UI/persistencia.
4. Revisar #2/#3 por separado sin sustituir imágenes protegidas ni rediseñar la identidad oficial.
5. Validar en teléfono físico y autorizar explícitamente cualquier integración en `main`.

La aplicación sigue sin usar red, migraciones SQLite ni servicios de vídeo externos.
