package pe.asterionsystem.app.presentation;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import java.util.Objects;
import pe.asterionsystem.core.settings.AppPreferences;

/**
 * Adjusts native XML-inflated views. Called exactly once per new inflated root
 * to prevent cumulative font scaling. Row text is handled by PreferencesRowAdapter.
 */
public final class NativePreferencesApplier {
    private NativePreferencesApplier() { }

    public static void apply(View root, AppPreferences preferences) {
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(preferences, "preferences");
        scale(root, preferences);
    }

    private static void scale(View view, AppPreferences p) {
        if (view instanceof TextView && p.textSize() == AppPreferences.TextSize.LARGE) {
            TextView label = (TextView) view;
            label.setTextSize(TypedValue.COMPLEX_UNIT_PX, label.getTextSize() * 1.18f);
        }
        if (view instanceof ListView) {
            ListView list = (ListView) view;
            float density = view.getResources().getDisplayMetrics().density;
            list.setDividerHeight(Math.max(1, Math.round(
                    (p.listDensity() == AppPreferences.ListDensity.COMPACT ? 6 : 10) * density)));
        }
        if (view instanceof ViewGroup) {
            ViewGroup container = (ViewGroup) view;
            for (int i = 0; i < container.getChildCount(); i++) {
                scale(container.getChildAt(i), p);
            }
        }
    }
}
