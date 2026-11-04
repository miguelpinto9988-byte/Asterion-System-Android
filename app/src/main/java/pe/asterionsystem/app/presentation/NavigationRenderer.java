package pe.asterionsystem.app.presentation;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import java.util.Objects;
import pe.asterionsystem.app.R;
import pe.asterionsystem.core.navigation.NavigationRoute;

/** Inflates native Android XML screens; no HTML, WebView or Compose. */
public final class NavigationRenderer {
    private final Activity activity;
    private final FrameLayout host;

    public NavigationRenderer(Activity activity, FrameLayout host) {
        this.activity = Objects.requireNonNull(activity, "activity");
        this.host = Objects.requireNonNull(host, "host");
    }

    public void render(NavigationRoute route, Runnable openSecondary, Runnable backToHome) {
        Objects.requireNonNull(route, "route");
        final int layout = route == NavigationRoute.HOME
                ? R.layout.screen_home : R.layout.screen_secondary;
        View screen = LayoutInflater.from(activity).inflate(layout, host, false);
        if (route == NavigationRoute.HOME) {
            Button openButton = screen.findViewById(R.id.open_secondary);
            openButton.setOnClickListener(view -> openSecondary.run());
        } else {
            Button backButton = screen.findViewById(R.id.back_to_home);
            backButton.setOnClickListener(view -> backToHome.run());
        }
        host.removeAllViews();
        host.addView(screen);
    }
}
