package pe.asterionsystem.app;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONException;
import org.json.JSONObject;

/** Private local wizard draft; does not indicate a completed profile. */
final class ProfileRegistrationDraft {
    private static final String PREFS = "asterion_registration_draft_v2";
    private static final String KEY = "draft_json";
    private ProfileRegistrationDraft() { }

    static JSONObject load(Context context) {
        String raw = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY, "{}");
        try { return new JSONObject(raw == null ? "{}" : raw); }
        catch (JSONException invalid) { return new JSONObject(); }
    }

    static void save(Context context, JSONObject draft) {
        context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY, draft.toString()).apply();
    }

    static void clear(Context context) {
        context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().clear().apply();
    }
}
