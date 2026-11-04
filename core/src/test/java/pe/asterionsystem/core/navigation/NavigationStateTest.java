package pe.asterionsystem.core.navigation;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public final class NavigationStateTest {
    @Test public void rootHasNoPreviousScreen() {
        NavigationState state = NavigationState.initial();
        assertEquals(NavigationRoute.HOME, state.current());
        assertFalse(state.canGoBack());
        assertSame(state, state.goBack());
    }

    @Test public void forwardBackAndNoDuplicateDestination() {
        NavigationState root = NavigationState.initial();
        NavigationState secondary = root.navigateTo(NavigationRoute.SECONDARY);
        assertEquals(Arrays.asList("home", "secondary"), secondary.routeIds());
        assertSame(secondary, secondary.navigateTo(NavigationRoute.SECONDARY));
        assertEquals(root, secondary.goBack());
        assertEquals(root, secondary.navigateTo(NavigationRoute.HOME));
    }

    @Test public void restorationRoundTripIsIndependentOfInput() {
        ArrayList<String> original = new ArrayList<>(Arrays.asList("home", "secondary"));
        NavigationState restored = NavigationState.fromIds(original);
        original.clear();
        assertEquals(Arrays.asList("home", "secondary"), restored.routeIds());
        assertEquals(NavigationRoute.SECONDARY, restored.current());
    }

    @Test public void invalidStateCannotBeRestored() {
        for (java.util.List<String> ids : Arrays.asList(
                Collections.<String>emptyList(),
                Arrays.asList("secondary"),
                Arrays.asList("home", "home"),
                Arrays.asList("home", "secondary", "secondary"),
                Arrays.asList("home", "unknown"))) {
            try {
                NavigationState.fromIds(ids);
                fail("Expected invalid snapshot: " + ids);
            } catch (IllegalArgumentException expected) {
                // Correct: malformed navigation histories are rejected.
            }
        }
    }

    @Test public void routesHaveStableIdentifiers() {
        assertEquals(NavigationRoute.HOME, NavigationRoute.fromId("home"));
        assertEquals(NavigationRoute.SECONDARY, NavigationRoute.fromId("secondary"));
    }
}
