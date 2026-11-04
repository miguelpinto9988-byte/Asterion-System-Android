package pe.asterionsystem.app.presentation;

import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import java.util.Objects;
import pe.asterionsystem.app.R;
import pe.asterionsystem.core.settings.AppPreferences;
import pe.asterionsystem.core.settings.PreferencesService;

/** Binds the native settings XML to a platform-neutral service. */
public final class SettingsPresenter {
    private final PreferencesService service;

    public SettingsPresenter(PreferencesService service) {
        this.service = Objects.requireNonNull(service, "service");
    }

    public void bind(View screen, Runnable goBack, Runnable onChange) {
        Objects.requireNonNull(screen, "screen");
        Objects.requireNonNull(goBack, "goBack");
        Objects.requireNonNull(onChange, "onChange");
        AppPreferences settings = service.current();
        RadioGroup text = screen.findViewById(R.id.settings_text_group);
        RadioGroup density = screen.findViewById(R.id.settings_density_group);
        RadioGroup start = screen.findViewById(R.id.settings_start_group);

        // Check first, subscribe afterwards: initial selection is NOT a user change.
        text.check(settings.textSize() == AppPreferences.TextSize.LARGE
                ? R.id.settings_text_large : R.id.settings_text_normal);
        density.check(settings.listDensity() == AppPreferences.ListDensity.COMPACT
                ? R.id.settings_density_compact : R.id.settings_density_normal);
        int startId;
        switch (settings.startSection()) {
            case CATALOG: startId = R.id.settings_start_catalog; break;
            case PLAN: startId = R.id.settings_start_plan; break;
            case PROGRESS: startId = R.id.settings_start_progress; break;
            default: startId = R.id.settings_start_home; break;
        }
        start.check(startId);

        text.setOnCheckedChangeListener((group, checked) -> {
            if (checked != R.id.settings_text_normal && checked != R.id.settings_text_large) return;
            AppPreferences.TextSize selected = checked == R.id.settings_text_large
                    ? AppPreferences.TextSize.LARGE : AppPreferences.TextSize.NORMAL;
            if (service.update(service.current().withTextSize(selected))) onChange.run();
        });
        density.setOnCheckedChangeListener((group, checked) -> {
            if (checked != R.id.settings_density_normal && checked != R.id.settings_density_compact) return;
            AppPreferences.ListDensity selected = checked == R.id.settings_density_compact
                    ? AppPreferences.ListDensity.COMPACT : AppPreferences.ListDensity.NORMAL;
            if (service.update(service.current().withListDensity(selected))) onChange.run();
        });
        start.setOnCheckedChangeListener((group, checked) -> {
            AppPreferences.StartSection selected;
            if (checked == R.id.settings_start_home) selected = AppPreferences.StartSection.HOME;
            else if (checked == R.id.settings_start_catalog) selected = AppPreferences.StartSection.CATALOG;
            else if (checked == R.id.settings_start_plan) selected = AppPreferences.StartSection.PLAN;
            else if (checked == R.id.settings_start_progress) selected = AppPreferences.StartSection.PROGRESS;
            else return;
            if (service.update(service.current().withStartSection(selected))) onChange.run();
        });

        Button reset = screen.findViewById(R.id.settings_reset);
        reset.setOnClickListener(v -> {
            service.reset();
            onChange.run();
        });
        screen.<Button>findViewById(R.id.settings_back).setOnClickListener(v -> goBack.run());
    }
}
