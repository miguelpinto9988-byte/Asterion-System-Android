# Regla obligatoria — Pantallas de carga permanentes

**Decisión del propietario:** las dos ilustraciones aprobadas de Asterion System son oficiales, definitivas e inmutables ante todas las actualizaciones futuras. La decisión complementa, sin alterar, la política del icono oficial `ICONO_OFICIAL_INMUTABLE.md`.

## Ilustraciones bloqueadas

| Horario local en el teléfono | Imagen obligatoria | SHA-256 inmutable |
|---|---|---|
| 00:00:00 a 11:59:59 | `app/src/main/res/drawable-nodpi/asterion_loading_manana.png` | `67c495b89b0acd631e1a64c113c6803df31e488cef559237b936b1ed6a88ff27` |
| 12:00:00 a 23:59:59 | `app/src/main/res/drawable-nodpi/asterion_loading_tarde_noche.png` | `d1af6660b3c1afb9db0e6b5bdca2d7c4e457e302e1f363f3d03226c3461b3cba` |

Los archivos incluyen ilustración, decoración, título, barra dibujada y texto de estado; **no está permitido retocar, regenerar, sustituir, recolorear, alterar textos, recortar, aplicar filtros, reemplazar personajes ni agregar otros emblemas**. Las imágenes finales están almacenadas como PNG 1080 × 1920 dentro del código fuente del repositorio y empaquetadas en la APK debug. Se obtuvieron ampliando las previsualizaciones aprobadas de 941 × 1672 px mediante remuestreo; la ampliación técnica conserva la composición pero no añade detalle real.

## Comportamiento obligatorio

- Al arrancar desde el lanzador, `SplashActivity` muestra **exactamente una** de estas dos imágenes.
- Para la selección usa `Calendar.getInstance().get(Calendar.HOUR_OF_DAY)`, es decir, la hora local del dispositivo, formato 24 horas.
- De medianoche hasta antes de mediodía, muestra la versión clara; desde mediodía hasta medianoche, la oscura.
- Muestra la ilustración en `fitCenter` para no recortar el texto ni el personaje en teléfonos con otras relaciones de aspecto; las franjas adicionales usarán el fondo oscuro.
- La imagen de carga **no contiene el icono oficial**. El icono de lanzador de Android queda intacto.
- Se muestra aproximadamente 1,6 segundos cuando la actividad está visible y después abre `MainActivity`. El indicador gráfico de progreso es parte fija de la imagen aprobada, **no representa progreso de descarga o inicialización**.
- No necesita internet ni añade permisos. No modifica preferencias, catálogo, planificación, seguimiento ni base de datos.
- No se implementa creación de perfil en esta entrega; se aborda después, separadamente.

## Protección técnica para futuras versiones

- `tests/branding_splash_lock.py` valida byte por byte ambos recursos con sus SHA-256, dimensiones, horario y declaración del lanzador.
- `SplashTimeWindowTest` cubre todas las 24 horas y dos entradas inválidas.
- `.github/workflows/android-ci.yml` ejecuta la validación de integridad en cada push a `main`, antes de compilar `debug`. Fallará ante cambios no aprobados de imágenes o configuración horaria.
- Queda prohibida toda modificación al arte, a la política o al control de integridad. La actualización de otras funciones **no debe modificar estos recursos**.

**Límite:** los controles automáticos detectan cambios bajo el proceso de CI actual; no pueden impedir que alguien con privilegios administrativos quite los controles o reescriba el historial. Para máxima protección, configurar la rama `main` para requerir que pase CI y revisión de archivos de identidad.
