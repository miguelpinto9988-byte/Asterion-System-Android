package pe.asterionsystem.core.navigation;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import pe.asterionsystem.core.events.InMemoryEventBus;
import pe.asterionsystem.core.events.Subscription;
import pe.asterionsystem.core.state.InMemoryStateStore;

public final class NavigationControllerTest {
    @Test public void navigationEmitsOnlyWhenStateChanges() {
        InMemoryEventBus bus = new InMemoryEventBus();
        List<NavigationChangedEvent> changes = new ArrayList<>();
        Subscription subscription = bus.subscribe(NavigationChangedEvent.class, changes::add);
        NavigationController controller = new NavigationController(
                new InMemoryStateStore<>(NavigationState.initial()), bus);
        assertFalse(controller.goBack());
        assertTrue(controller.navigateTo(NavigationRoute.SECONDARY));
        assertFalse(controller.navigateTo(NavigationRoute.SECONDARY));
        assertTrue(controller.goBack());
        assertEquals(2, changes.size());
        assertEquals(NavigationRoute.HOME, changes.get(0).previous().current());
        assertEquals(NavigationRoute.SECONDARY, changes.get(0).current().current());
        subscription.close();
        assertTrue(controller.navigateTo(NavigationRoute.SECONDARY));
        assertEquals(2, changes.size());
    }

    @Test public void restoreAndResetWorkWithoutAndroid() {
        NavigationController controller = new NavigationController(
                new InMemoryStateStore<>(NavigationState.initial()), new InMemoryEventBus());
        NavigationState saved = NavigationState.initial().navigateTo(NavigationRoute.SECONDARY);
        assertTrue(controller.restore(saved));
        assertFalse(controller.restore(saved));
        assertEquals(saved, controller.state());
        assertTrue(controller.navigateTo(NavigationRoute.HOME));
        assertFalse(controller.state().canGoBack());
    }
}
