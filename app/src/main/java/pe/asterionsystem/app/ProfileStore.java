package pe.asterionsystem.app;

import android.content.Context;
import android.content.SharedPreferences;

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
