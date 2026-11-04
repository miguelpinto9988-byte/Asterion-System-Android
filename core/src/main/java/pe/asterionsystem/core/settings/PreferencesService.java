package pe.asterionsystem.core.settings;

import java.util.Objects;

/** Explicit changes only: avoids touching planning, catalog or tracking data. */
public final class PreferencesService {
    private final PreferencesRepository repository;

    public PreferencesService(PreferencesRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public AppPreferences current() {
        return Objects.requireNonNull(repository.read(), "persisted preferences");
    }

    /** Returns true only when settings were changed. */
    public boolean update(AppPreferences selected) {
        Objects.requireNonNull(selected, "selected");
        if (selected.equals(current())) return false;
        repository.save(selected);
        return true;
    }

    public void reset() {
        repository.reset();
    }
}
