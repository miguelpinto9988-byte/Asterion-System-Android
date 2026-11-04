package pe.asterionsystem.core.navigation;

import java.util.Objects;
import pe.asterionsystem.core.events.AppEvent;

/** Synchronous event after an actual navigation state transition. */
public final class NavigationChangedEvent implements AppEvent {
    private final NavigationState previous;
    private final NavigationState current;

    public NavigationChangedEvent(NavigationState previous, NavigationState current) {
        this.previous = Objects.requireNonNull(previous, "previous");
        this.current = Objects.requireNonNull(current, "current");
    }

    public NavigationState previous() {
        return previous;
    }

    public NavigationState current() {
        return current;
    }
}
