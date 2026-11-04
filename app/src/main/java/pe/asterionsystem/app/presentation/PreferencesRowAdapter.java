package pe.asterionsystem.app.presentation;

import android.app.Activity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import pe.asterionsystem.app.platform.AndroidPreferencesRepository;
import pe.asterionsystem.core.settings.AppPreferences;

/** Consistent text size and vertical density across catalog, planning and tracking rows. */
public final class PreferencesRowAdapter extends ArrayAdapter<String> {
    private final AppPreferences preferences;
    private final float density;

    public PreferencesRowAdapter(Activity activity) {
        super(activity, android.R.layout.simple_list_item_1, new ArrayList<String>());
        preferences = new AndroidPreferencesRepository(activity).read();
        density = activity.getResources().getDisplayMetrics().density;
    }

    private int dp(int value) { return Math.round(value * density); }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        TextView row = (TextView) super.getView(position, convertView, parent);
        boolean large = preferences.textSize() == AppPreferences.TextSize.LARGE;
        boolean compact = preferences.listDensity() == AppPreferences.ListDensity.COMPACT;
        row.setTextSize(TypedValue.COMPLEX_UNIT_SP, large ? 19f : 16f);
        row.setPadding(dp(16), dp(compact ? 6 : 14), dp(16), dp(compact ? 6 : 14));
        row.setMinHeight(dp(compact ? 44 : 56));
        return row;
    }
}
