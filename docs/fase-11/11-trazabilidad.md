# Fase 11 — Trazabilidad de las siete etapas

| Etapa autorizada | Archivos y evidencia |
|---|---|
| 11.1 Modelo y contratos | `AppPreferences.java`, `PreferencesRepository.java`, `PreferencesService.java` en `:core` |
| 11.2 Persistencia local | `AndroidPreferencesRepository.java` — SharedPreferences privado, sin SQLite nuevo |
| 11.3 Presentación | `NativePreferencesApplier.java`, `PreferencesRowAdapter.java`; adaptadores de listas existentes |
| 11.4 Preferencias de navegación | `MainActivity.initialNavigation`, `NavigationRoute.SETTINGS`, restauración por `Bundle` |
| 11.5 Pantalla Android XML | `screen_settings.xml`, `SettingsPresenter.java`, recursos `strings.xml` |
| 11.6 Integración y protección | `PreferencesService.update/reset`, validación de valores desconocidos, reinflado seguro |
| 11.7 Pruebas y publicación | `PreferencesServiceTest`, `tests/PreferencesSmoke.java`, `tests/phase11_jvm_smoke.sh`, `tests/phase11_preferences_static_smoke.py`, documentación, simulador HTML externo y commit GitHub |

Ningún archivo HTML se integra en el proyecto Android, no se modifica el esquema SQLite, no se implementan funcionalidades de fases 12, 13 o 14 y no se autoriza compilar APK.
