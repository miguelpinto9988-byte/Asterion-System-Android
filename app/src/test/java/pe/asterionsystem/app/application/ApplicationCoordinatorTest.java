package pe.asterionsystem.app.application;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import pe.asterionsystem.core.events.InMemoryEventBus;
import pe.asterionsystem.core.lifecycle.LifecyclePhase;
import pe.asterionsystem.core.state.InMemoryStateStore;

public final class ApplicationCoordinatorTest {
    @Test
    public void updatesOnlyStructuralStateAndEmitsEvent() {
        InMemoryEventBus bus = new InMemoryEventBus();
        List<LifecyclePhase> observed = new ArrayList<>();
        bus.subscribe(LifecycleChangedEvent.class, event -> observed.add(event.phase()));
        ApplicationCoordinator coordinator = new ApplicationCoordinator(
                bus, new InMemoryStateStore<>(LifecyclePhase.INITIALIZED));
        coordinator.onLifecycleChanged(LifecyclePhase.CREATED);
        coordinator.onLifecycleChanged(LifecyclePhase.RESUMED);
        assertEquals(LifecyclePhase.RESUMED, coordinator.currentPhase());
        assertEquals(List.of(LifecyclePhase.CREATED, LifecyclePhase.RESUMED), observed);
    }
}
