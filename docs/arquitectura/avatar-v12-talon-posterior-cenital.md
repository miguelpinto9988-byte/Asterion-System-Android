# Asterion V12 — zapatilla posterior para cámara cenital

## Motivo
La vista superior de flexión diamante mostraba demasiado **empeine,
lengüeta y agujetas**, y el diseño parecía una zapatilla frontal aunque el
cuerpo estaba visto desde arriba. El usuario pide mostrar esencialmente
**talón, contrafuerte y pieza posterior** del calzado, sin cordones.

## Cambios
- Solamente la rutina Android `AvatarArtDetails.drawProneTopSneaker`
  utiliza el nuevo calzado posterior. No modifica la vista lateral,
  frontal ni otras escenas.
- Contorno del talón compacto con volumen orgánico y blanco en la base;
  paneles laterales, contrafuerte acolchado, costura posterior
  `backstay` y pequeña tira/tirador de talón, con paletas azul/morado.
- **Eliminados:** lengua visible, agujetas, ojales y tres franjas
  horizontales frontales que provocaban una lectura errónea de la figura.
- Conservado: `AvatarFootOrientation.FootProjection`, ambos pies
  alineados con el eje corporal, contacto en punteras dirigido hacia la
  cabeza y suelas orientadas al suelo.

## Control de calidad
- Regresión de contrato `avatar_v12_rear_heel_contract.py`: rechaza
  los antiguos elementos de la zapatilla frontal en esta cámara.
- Instrumentación `proneTopShoesUseRearHeelCounterWithoutLaceBands`:
  comprueba continuidad del contrafuerte sin bandas blancas de
  agujetas, ambos perfiles y dos paletas.
- Instrumentación `exportRealAndroidMultiviewFramesForReview`:
  conserva el archivo de 42 fotogramas del renderer Android real.
- Compilación Gradle, pruebas JUnit/AndroidTest, Lint y emulador CI.
- La nueva PR sigue apilada sobre V11 y **no modifica `main`**.

## Referencias de diseño
Las partes posteriores de un zapato se describen como `heel counter`
(contrafuerte), `heel tab` (tirador) y `backstay` (pieza/costura de
la parte trasera); son diferentes de la lengüeta y el sistema de cordones.
Ver [FX Footwear, Parts of a Shoe](https://fxfootwear.com/parts-of-a-shoe/).
El dibujo utiliza `Canvas.save`, `translate`, `rotate`,
`Path.cubicTo`, `drawPath` y `restore` de
[Android Canvas](https://developer.android.com/reference/android/graphics/Canvas)
y [Android Path](https://developer.android.com/reference/android/graphics/Path).

## Límite
Se trata de una interpretación gráfica 2D posterior para esta cámara,
no una geometría 3D físicamente reconstruida. El pivote del tobillo,
la orientación de puntera y los apoyos no han cambiado.
