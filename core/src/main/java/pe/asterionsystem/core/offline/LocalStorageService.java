package pe.asterionsystem.core.offline;

import java.util.Objects;

/** Explicit diagnostics only: does not delete, migrate or repair records. */
public final class LocalStorageService {
    private final LocalStorageCheck check;

    public LocalStorageService(LocalStorageCheck check) {
        this.check = Objects.requireNonNull(check, "check");
    }

    public LocalStorageReport inspect() {
        return Objects.requireNonNull(check.inspect(), "local storage report");
    }
}
