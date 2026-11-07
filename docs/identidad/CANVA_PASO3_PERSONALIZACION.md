# Paso 3 — Personalización: integración de Canva

**Diseño aprobado:** [Asterion System — Paso 3 de 4: Personalización](https://www.canva.com/d/gMU31Bp6ag4VAXD)

La captura aprobada es la referencia visual, pero NO debe emplearse como interfaz rasterizada completa en Android: los textos quedarían ilegibles en pantallas pequeñas.

## Implementación nativa

La nueva vista está en:
app/src/main/java/pe/asterionsystem/app/CanvaProfileStepThreeView.java

Se conecta a ProfileRegistrationSteps.step3() y conserva el guardado local:
- theme: dark o light.
- accent: azul, morado, rosa, verde, rojo y blanco; exclusión mutua.
- companion: igris, beru, tank, tusk, iron.
- card_silhouette: sung_jinwoo, cha_haein, kim_dokja, yoo_joonghyuk, yoo_sangah.
- card_background: ruinas, niebla, obsidiana, aurora, bosque.

Los valores antiguos de compañero y silueta se migran por el orden en que
aparecían, sin borrar las otras respuestas.

## Auditoría de legibilidad

- Tipografías nativas escalables de 12–17 sp para etiquetas y títulos.
- Títulos con buen contraste; descripciones secundarias no inferiores a 13 sp.
- Controles de tema de 52 dp y colores de 76 dp de altura mínima.
- Listas horizontales desplazables para sombras, cazadores y paisajes.
- Vista previa horizontal de 176 dp, sin insignia de rango S.
- Estado seleccionado y descripción accesible en cada control.
- No se usa descarga de imágenes ni generación de imágenes en ejecución.

## Nombres definitivos de los recursos de ilustración

Los PNG/WebP separados del diseño se prepararon en la conversación original.
**Los binarios aprobados aún deben transferirse al repositorio**. Ponerlos en
app/src/main/res/drawable-nodpi/ con estos nombres:

- asterion_step3_background.webp: escenario de ruinas y portal azul.
- asterion_step3_cha_haein.webp: personaje lateral transparente.
- asterion_companion_igris.webp, asterion_companion_beru.webp,
  asterion_companion_tank.webp, asterion_companion_tusk.webp,
  asterion_companion_iron.webp.
- asterion_hunter_sung_jinwoo.webp, asterion_hunter_cha_haein.webp,
  asterion_hunter_kim_dokja.webp, asterion_hunter_yoo_joonghyuk.webp,
  asterion_hunter_yoo_sangah.webp.
- asterion_card_silhouette_sung_jinwoo.webp,
  asterion_card_silhouette_cha_haein.webp,
  asterion_card_silhouette_kim_dokja.webp,
  asterion_card_silhouette_yoo_joonghyuk.webp,
  asterion_card_silhouette_yoo_sangah.webp.
- asterion_card_background_ruinas.webp,
  asterion_card_background_niebla.webp,
  asterion_card_background_obsidiana.webp,
  asterion_card_background_aurora.webp,
  asterion_card_background_bosque.webp.

Mantener las figuras con transparencia y separar fondos, retratos, marcos y
etiquetas; así el texto sigue siendo legible y la elección actualiza la credencial.
Se usan recursos locales de respaldo mientras no estén los definitivos.

## Control de calidad

Ejecutar python3 tests/profile_wizard_visual_contract.py, compilar la variante
preview de Android y revisar en pantallas de 320, 360 y 412 dp, incluyendo
fuente al 130 % y TalkBack. No modificar los recursos oficiales de icono y splash.
