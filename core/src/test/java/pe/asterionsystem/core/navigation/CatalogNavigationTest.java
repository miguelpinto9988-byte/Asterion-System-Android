package pe.asterionsystem.core.navigation;

import static org.junit.Assert.*;
import java.util.Arrays;
import org.junit.Test;
import pe.asterionsystem.core.events.InMemoryEventBus;
import pe.asterionsystem.core.state.InMemoryStateStore;

public final class CatalogNavigationTest {
    @Test public void catalogAndDetailAreRoundTripRoutes() {
        NavigationController controller = new NavigationController(
                new InMemoryStateStore<>(NavigationState.initial()), new InMemoryEventBus());
        assertTrue(controller.navigateTo(NavigationRoute.CATALOG));
        assertTrue(controller.navigateTo(NavigationRoute.CATALOG_DETAIL));
        assertEquals(Arrays.asList("home", "catalog", "catalog_detail"),
                controller.state().routeIds());
        assertEquals(controller.state(),
                NavigationState.fromIds(controller.state().routeIds()));
        assertTrue(controller.goBack());
        assertEquals(NavigationRoute.CATALOG, controller.state().current());
        assertTrue(controller.goBack());
        assertEquals(NavigationRoute.HOME, controller.state().current());
        assertFalse(controller.goBack());
    }
}
