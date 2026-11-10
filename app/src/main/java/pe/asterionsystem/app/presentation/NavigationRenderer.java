package pe.asterionsystem.app.presentation;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import java.util.Objects;
import pe.asterionsystem.app.R;
import pe.asterionsystem.core.navigation.NavigationRoute;
import pe.asterionsystem.core.settings.PreferencesService;

/** Native XML screen selection. Domain state and SQL are not handled here. */
public final class NavigationRenderer {
    private final Activity activity;
    private final FrameLayout host;
    private final PreferencesService preferences;

    public NavigationRenderer(Activity activity, FrameLayout host,
                              PreferencesService preferences) {
        this.activity = Objects.requireNonNull(activity, "activity");
        this.host = Objects.requireNonNull(host, "host");
        this.preferences = Objects.requireNonNull(preferences, "preferences");
    }

    public View render(NavigationRoute route, Runnable openSecondary,
                       Runnable back, Runnable openCatalog, Runnable openPlan,
                       Runnable openProgress, Runnable openSettings,
                       Runnable openLocalStatus) {
        Objects.requireNonNull(route, "route");
        final int layout;
        switch (route) {
            case HOME: layout = R.layout.screen_home; break;
            case SECONDARY: layout = R.layout.screen_secondary; break;
            case CATALOG: layout = R.layout.screen_catalog; break;
            case CATALOG_DETAIL: layout = R.layout.screen_catalog_detail; break;
            case PLAN: layout = R.layout.screen_plan; break;
            case PLAN_FORM: layout = R.layout.screen_plan_form; break;
            case PROGRESS: layout = R.layout.screen_progress; break;
            case PROGRESS_SUMMARY: layout = R.layout.screen_progress_summary; break;
            case SETTINGS: layout = R.layout.screen_settings; break;
            case LOCAL_STATUS: layout = R.layout.screen_local_status; break;
            default: throw new IllegalArgumentException("Ruta no soportada: " + route);
        }
        View screen = LayoutInflater.from(activity).inflate(layout, host, false);
        if (route == NavigationRoute.HOME) {
            Button openSecond = screen.findViewById(R.id.open_secondary);
            openSecond.setOnClickListener(view -> openSecondary.run());
            Button openCatalogButton = screen.findViewById(R.id.open_catalog);
            openCatalogButton.setOnClickListener(view -> openCatalog.run());
            Button openPlanButton = screen.findViewById(R.id.open_plan);
            openPlanButton.setOnClickListener(view -> openPlan.run());
            Button openProgressButton = screen.findViewById(R.id.open_progress);
            openProgressButton.setOnClickListener(view -> openProgress.run());
            Button openSettingsButton = screen.findViewById(R.id.open_settings);
            openSettingsButton.setOnClickListener(view -> openSettings.run());
            Button openLocalStatusButton = screen.findViewById(R.id.open_local_status);
            openLocalStatusButton.setOnClickListener(view -> openLocalStatus.run());
            HomeMissionBinder.bind(activity,screen,openProgress);
        } else if (route == NavigationRoute.SECONDARY) {
            Button backButton = screen.findViewById(R.id.back_to_home);
            backButton.setOnClickListener(view -> back.run());
        }
        NativePreferencesApplier.apply(screen, preferences.current());
        host.removeAllViews();
        host.addView(screen);
        return screen;
    }
}
