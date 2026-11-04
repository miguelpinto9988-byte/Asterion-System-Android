package pe.asterionsystem.core.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public final class InMemoryStateStoreTest {
    @Test
    public void replacesStateWithoutPersistence() {
        InMemoryStateStore<String> store = new InMemoryStateStore<>("initial");
        assertEquals("initial", store.current());
        store.replace("updated");
        assertEquals("updated", store.current());
    }

    @Test
    public void refusesNullStates() {
        assertThrows(NullPointerException.class, () -> new InMemoryStateStore<>(null));
        InMemoryStateStore<String> store = new InMemoryStateStore<>("initial");
        assertThrows(NullPointerException.class, () -> store.replace(null));
        assertEquals("initial", store.current());
    }
}
