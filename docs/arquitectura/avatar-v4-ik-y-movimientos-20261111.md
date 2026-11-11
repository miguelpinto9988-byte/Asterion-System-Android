# Arquitectura de movimiento Asterion V4 (Android Java nativo)
Fecha de trabajo: 2026-11-11. Rama experimental sobre PR #19.
No modifica la rama main, no publica un APK de producción.

## Fuentes investigadas, verificación y licencia
1. **Caliko**: https://github.com/FedUni/caliko — repositorio Java de FABRIK 2D/3D, licencia MIT verificada leyendo `LICENSE.txt`; archivo consultado `caliko/src/main/java/au/edu/federation/caliko/FabrikChain2D.java`. El código no se ha copiado ni se distribuye Caliko en la APK: el solver ligero implementado en `AvatarRigConstraints` es original y mantiene sólo los principios matemáticos.
2. **FABRIK, Aristidou y Lasenby (2011)**: https://doi.org/10.1016/j.gmod.2011.05.003 — concepto publicado de iteraciones hacia delante/atrás con longitudes fijas; paper no redistribuido.
3. **libGDX Box2D**: https://libgdx.com/wiki/extensions/physics/box2d — pivotes y límites de bisagra. Es una referencia conceptual: Box2D requiere física y no se ha añadido al programa.
4. **gdx-box2d**: https://github.com/libgdx/gdx-box2d — Apache-2.0; no añadido como dependencia. La documentación actual distingue revisiones nuevas frente a las API antiguas.
5. **Android Canvas**: https://developer.android.com/develop/ui/views/layout/custom-views/custom-drawing ; https://developer.android.com/reference/android/view/View#postInvalidateOnAnimation() — render por Canvas y fotogramas sincronizados.

## Restricciones incorporadas
- IK analítica 2D para huesos superior/inferior (`twoBone`), longitudes inmutables, elección de rama de giro y límites configurables de flexión.
- Objetivos inalcanzables: el extremo calculado se proyecta en un radio admisible; no se alargan huesos. Las poses con apoyo fijo incompatible con la geometría se rechazan en lugar de liberar silenciosamente el pie.
- `rootForPinnedEnd`: alternativa para reubicar la raíz móvil cuando el extremo debe permanecer plantado.
- FABRIK genérico para 2 a 32 enlaces con raíz fija, máximo de iteraciones y tolerancia finita; diseñado para cadenas futuras, aún no conectado a movimientos del catálogo.
- `requireFixedLength` y `AvatarMotionEngine.assertSkeleton` comprueban invariantes de longitud en ambos lados, codos, rodillas y tronco.
- Cinco perfiles **ilustrativos**: sentadilla, bisagra de cadera, elevación de brazos, marcha y zancada. Sólo estas cinco curvas tienen objetivos explícitos en vista lateral.
- Controles Android incluyen un selector de demostraciones; la selección y el estado se guardan al recrear la actividad.

## Límites científicos y funcionales
No es captura de movimiento ni simulación biomecánica validada. Un límite de giro que evita una deformación gráfica no indica que el movimiento humano sea correcto ni seguro. Las cifras de flexión son parámetros de producción, no recomendaciones médicas. No es correcto asignar la misma animación a ejercicios sin un perfil y una proyección revisados; el catálogo completo queda pendiente de diseñar y verificar, ejercicio por ejercicio. Una vista lateral tampoco expresa bien todos los movimientos frontales, rotacionales o asimétricos: puede requerirse una segunda vista o 3D.

## Rendimiento y controles
Se mantiene Canvas Java, `postInvalidateOnAnimation`, Paint/Path/gradientes reutilizados y la lógica de pausa/accesibilidad. Se excluye Box2D, Unity y JNI; no se añaden permisos ni SDK externos.

## Matriz de validación
- JUnit: segmentos de longitud constante, objetivo inalcanzable, pie plantado, intervalo de giro, convergencia FABRIK y ausencia de saltos en 301 muestras por movimiento.
- AndroidTest: los cinco movimientos dibujan en pantalla y los nombres se exponen a accesibilidad.
- CI: contratos estáticos, compilación, lint, JUnit y pruebas en emulador Android 16.
- Revisión visual posterior: lateral móvil, uniones hombro/codo/rodilla/pelvis/calzado, tamaños de pantalla y perfiles de movimiento. Los perfiles no sustituyen instrucciones ni evaluación de técnica.
