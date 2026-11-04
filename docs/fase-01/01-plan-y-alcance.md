# Fase 01 — Plan y alcance

**Proyecto:** Asterion System Android
**Nombre de repositorio aprobado:** `Asterion-System-Android`
**Visibilidad aprobada:** público
**Estado:** estructura preparada; CI pendiente.

La documentación técnica de fase 00 es referencia de diseño, pero no se sube automáticamente al repositorio público.

## Objetivo

Proporcionar una base Android nativa en Java con estructura importable, pantalla mínima, pruebas iniciales, automatización CI y documentación reproducible.

## Etapas

| Etapa | Resultado |
|---|---|
| 01.1 | Repositorio público inicializado; renombrar para quitar punto final |
| 01.2 | Configuración Gradle, Android Gradle Plugin, SDK y JDK |
| 01.3 | Directorios Java, recursos, pruebas y documentación |
| 01.4 | `MainActivity` y pantalla XML mínima, sin navegación avanzada |
| 01.5 | GitHub Actions para pruebas JVM, lint y compilación de APK debug |
| 01.6 | Controles estáticos; ejecutar CI y registrar resultado final |

## Fuera del alcance

- Fase 02: implementación de arquitectura de dominio y contratos de motores.
- Fase 03: identidad visual definitiva (pendiente).
- Fase 04: navegación y estado completos.
- Fase 05: modelos de persistencia y base de datos.
- Fase 06: motor central de reglas.
- Fases 07, 08, 09, 11 y 14: pendientes de decisión.
- Fases 10, 12 y 13: excluidas.
- Fase 15: análisis integral de seguridad y rendimiento; solo hay controles básicos de CI en esta etapa.

## Entregables

Repositorio con proyecto Android Java, pantalla de arranque neutra, documentación, prueba JVM mínima y workflow. APK únicamente cuando se confirme compilación exitosa.
