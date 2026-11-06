package pe.asterionsystem.app;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONObject;

/** Minimal, private v1 profile identity. Exercise settings will be defined separately. */
public final class ProfileStore {
    private static final String PREFS = "asterion_profile_local_v1";
    private static final String KEY_NAME = "hunter_name";
    private ProfileStore() { }

    public static boolean hasProfile(Context context) {
        return !getName(context).isEmpty();
    }

    public static String getName(Context context) {
        SharedPreferences preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String name = preferences.getString(KEY_NAME, "");
        return name == null ? "" : name.trim();
    }

    /**
     * Atomic completion of the four-step wizard. A partially completed draft never
     * unlocks the profile gate: the name and full configuration commit together.
     * Legacy v1 identities remain valid and are never reset by this migration.
     */
    public static boolean saveRegistration(Context context, JSONObject profile) {
        if (profile == null) return false;
        String name = profile.optString("name", "").trim();
        int age = profile.optInt("age", 0);
        if (name.isEmpty() || name.length() > 40
                || name.indexOf('\n') >= 0 || age < 1 || age > 120) {
            return false;
        }
        return context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_NAME, name)
                .putString("complete_registration_v2", profile.toString())
                .commit();
    }

    /** Exposes approved configuration without using body metrics as an exercise score. */
    public static JSONObject getRegistration(Context context) {
        String raw = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString("complete_registration_v2", "{}");
        try { return new JSONObject(raw == null ? "{}" : raw); }
        catch (org.json.JSONException badData) { return new JSONObject(); }
    }

    /** Synchronous commit ensures the profile is available on the very next app launch. */
    public static boolean saveName(Context context, String requestedName) {
        String name = requestedName == null ? "" : requestedName.trim();
        if (name.isEmpty() || name.length() > 40 || name.indexOf('\n') >= 0) {
            return false;
        }
        return context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY_NAME, name).commit();
    }
}
