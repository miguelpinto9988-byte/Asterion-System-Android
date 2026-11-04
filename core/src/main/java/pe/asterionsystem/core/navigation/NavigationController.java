package pe.asterionsystem.core.navigation;

import java.util.Objects;
import pe.asterionsystem.core.events.EventBus;
import pe.asterionsystem.core.state.StateStore;

/**
 * Navigation transitions, independent of Android.
 * Call from one UI thread; event delivery is synchronous and reentrant.
 */
public final class NavigationController {
    private final StateStore<NavigationState> store;
    private final EventBus events;

    public NavigationController(StateStore<NavigationState> store, EventBus events) {
        this.store = Objects.requireNonNull(store, "store");
        this.events = Objects.requireNonNull(events, "events");
        Objects.requireNonNull(store.current(), "initial navigation state");
    }

    public NavigationState state() {
        return store.current();
    }

    public boolean navigateTo(NavigationRoute route) {
        return commit(state().navigateTo(route));
    }

    /** Returns false at HOME so Android can handle system Back (leave the app). */
    public boolean goBack() {
        return commit(state().goBack());
    }

    public boolean restore(NavigationState snapshot) {
        return commit(Objects.requireNonNull(snapshot, "snapshot"));
    }

    private boolean commit(NavigationState proposed) {
        NavigationState previous = state();
        if (previous.equals(proposed)) {
            return false;
        }
        store.replace(proposed);
        events.publish(new NavigationChangedEvent(previous, proposed));
        return true;
    }
}
