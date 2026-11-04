package pe.asterionsystem.app.platform;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Objects;
import pe.asterionsystem.core.settings.AppPreferences;
import pe.asterionsystem.core.settings.PreferencesRepository;

/** Private preferences only. No SQLite, accounts, exports or network permissions. */
public final class AndroidPreferencesRepository implements PreferencesRepository {
    public static final String FILE_NAME = "asterion_preferences_v1";
    private static final String KEY_TEXT = "presentation.text_size";
    private static final String KEY_DENSITY = "presentation.list_density";
    private static final String KEY_START = "navigation.start_section";

    private final SharedPreferences values;

    public AndroidPreferencesRepository(Context context) {
        Context app = Objects.requireNonNull(context, "context").getApplicationContext();
        values = app.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    private String readString(String key, String defaultValue) {
        try {
            String value = values.getString(key, defaultValue);
            return value == null ? defaultValue : value;
        } catch (ClassCastException wrongType) {
            // An unknown previous preference type must not crash app startup.
            return defaultValue;
        }
    }

    @Override public AppPreferences read() {
        return new AppPreferences(
                AppPreferences.TextSize.fromStored(readString(KEY_TEXT, "normal")),
                AppPreferences.ListDensity.fromStored(readString(KEY_DENSITY, "normal")),
                AppPreferences.StartSection.fromStored(readString(KEY_START, "home")));
    }

    @Override public void save(AppPreferences preferences) {
        AppPreferences p = Objects.requireNonNull(preferences, "preferences");
        values.edit()
                .putString(KEY_TEXT, p.textSize().stored())
                .putString(KEY_DENSITY, p.listDensity().stored())
                .putString(KEY_START, p.startSection().stored())
                .apply();
    }

    @Override public void reset() {
        // Clear ONLY the dedicated preferences file, not any SQLite business records.
        values.edit().clear().apply();
    }
}
