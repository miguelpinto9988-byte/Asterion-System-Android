package pe.asterionsystem.app.application;

import java.util.Objects;
import pe.asterionsystem.core.events.AppEvent;
import pe.asterionsystem.core.lifecycle.LifecyclePhase;

/** Immutable structural event; not a user-facing notification. */
public final class LifecycleChangedEvent implements AppEvent {
    private final LifecyclePhase phase;

    public LifecycleChangedEvent(LifecyclePhase phase) {
        this.phase = Objects.requireNonNull(phase, "phase");
    }

    public LifecyclePhase phase() {
        return phase;
    }
}
