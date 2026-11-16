# Objetivo personal escrito: estado de integración

El campo **Objetivo personalizado** existe en el paso 2 del registro y usa la clave persistente `custom_goal`.

- `ProfileRegistrationSteps.step2()` muestra una entrada multilinea que guarda los cambios en el borrador.
- `ProfileRegistrationDraft` preserva `custom_goal` dentro del JSON del borrador.
- `ProfileStore.saveRegistration()` conserva el JSON íntegro al activar el perfil; `getRegistration()` permite recuperarlo.
- `ProfileRegistrationSteps.step4()` muestra el objetivo en el resumen de entrenamiento.
- El HTML local actualizado presenta el campo y lo exporta como `custom_goal`.

**Precaución:** no se ha confirmado una implementación en el generador de misiones que lea `custom_goal` y la convierta en selección concreta de actividades. Mostrar y guardar el objetivo NO equivale a adaptar las misiones. Debe verificarse y probarse el consumidor real cuando se conecte el motor.

Comportamiento requerido en el generador: leer el texto como preferencia *orientativa* junto con nivel de experiencia, objetivo principal, categoría semanal, equipamiento e intensidad. No deducir condiciones médicas ni prescribir cargas extremas a partir de texto libre; las opciones seguras, controles existentes y descansos tienen prioridad. El campo es opcional: texto vacío conserva el comportamiento actual. Mantener claves previas y compatibilidad de perfiles guardados.

Estado: actualización informativa en la rama de diseño; **no certificar integración del motor ni compilación APK**.
