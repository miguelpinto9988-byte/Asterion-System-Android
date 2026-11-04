package pe.asterionsystem.app.application;

import java.util.Objects;
import pe.asterionsystem.core.events.EventBus;
import pe.asterionsystem.core.lifecycle.LifecycleObserver;
import pe.asterionsystem.core.lifecycle.LifecyclePhase;
import pe.asterionsystem.core.state.StateStore;

/**
 * Phase-02 composition boundary. Coordinates only structural lifecycle state;
 * no navigation, domain rules, network access or persistence is implemented.
 */
public final class ApplicationCoordinator implements LifecycleObserver {
    private final EventBus events;
    private final StateStore<LifecyclePhase> lifecycle;

    public ApplicationCoordinator(EventBus events, StateStore<LifecyclePhase> lifecycle) {
        this.events = Objects.requireNonNull(events, "events");
        this.lifecycle = Objects.requireNonNull(lifecycle, "lifecycle");
    }

    public LifecyclePhase currentPhase() {
        return lifecycle.current();
    }

    @Override
    public void onLifecycleChanged(LifecyclePhase phase) {
        lifecycle.replace(Objects.requireNonNull(phase, "phase"));
        events.publish(new LifecycleChangedEvent(phase));
    }
}
