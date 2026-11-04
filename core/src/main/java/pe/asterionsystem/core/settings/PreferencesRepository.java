package pe.asterionsystem.core.settings;

/** Settings persistence port. Android implements it using private SharedPreferences. */
public interface PreferencesRepository {
    AppPreferences read();
    void save(AppPreferences preferences);
    void reset();
}
