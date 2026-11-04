package pe.asterionsystem.app.platform;

import android.os.Bundle;
import java.util.ArrayList;
import pe.asterionsystem.core.navigation.NavigationState;

/** Serializes only temporary navigation state to Android's saved instance bundle. */
public final class NavigationBundleAdapter {
    private static final String VERSION_KEY = "pe.asterionsystem.nav.version";
    private static final String HISTORY_KEY = "pe.asterionsystem.nav.history";
    private static final int FORMAT_VERSION = 1;

    private NavigationBundleAdapter() { }

    public static void save(Bundle output, NavigationState state) {
        output.putInt(VERSION_KEY, FORMAT_VERSION);
        output.putStringArrayList(HISTORY_KEY, new ArrayList<>(state.routeIds()));
    }

    public static NavigationState read(Bundle saved) {
        if (saved == null) {
            return NavigationState.initial();
        }
        try {
            if (saved.getInt(VERSION_KEY, -1) != FORMAT_VERSION) {
                return NavigationState.initial();
            }
            ArrayList<String> routes = saved.getStringArrayList(HISTORY_KEY);
            return NavigationState.fromIds(routes);
        } catch (RuntimeException malformedState) {
            // Invalid or obsolete snapshots must never crash Activity creation.
            return NavigationState.initial();
        }
    }
}
