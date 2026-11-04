package pe.asterionsystem.core.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public final class InMemoryEventBusTest {
    private static final class SampleEvent implements AppEvent { }
    private static final class OtherEvent implements AppEvent { }

    @Test
    public void deliversOnlyMatchingTypeInRegistrationOrder() {
        InMemoryEventBus bus = new InMemoryEventBus();
        StringBuilder order = new StringBuilder();
        bus.subscribe(SampleEvent.class, event -> order.append('A'));
        bus.subscribe(OtherEvent.class, event -> order.append('X'));
        bus.subscribe(SampleEvent.class, event -> order.append('B'));
        bus.publish(new SampleEvent());
        assertEquals("AB", order.toString());
    }

    @Test
    public void closingSubscriptionIsIdempotent() {
        InMemoryEventBus bus = new InMemoryEventBus();
        AtomicInteger received = new AtomicInteger();
        Subscription subscription = bus.subscribe(SampleEvent.class, event -> received.incrementAndGet());
        bus.publish(new SampleEvent());
        subscription.close();
        subscription.close();
        bus.publish(new SampleEvent());
        assertEquals(1, received.get());
    }

    @Test
    public void listenerFailureIsVisibleToPublisher() {
        InMemoryEventBus bus = new InMemoryEventBus();
        AtomicInteger reached = new AtomicInteger();
        bus.subscribe(SampleEvent.class, event -> { throw new IllegalStateException("test"); });
        bus.subscribe(SampleEvent.class, event -> reached.incrementAndGet());
        assertThrows(IllegalStateException.class, () -> bus.publish(new SampleEvent()));
        assertEquals(0, reached.get());
    }

    @Test
    public void rejectsNullEventAndNullSubscriptionType() {
        InMemoryEventBus bus = new InMemoryEventBus();
        assertThrows(NullPointerException.class, () -> bus.publish(null));
        assertThrows(NullPointerException.class, () -> bus.subscribe(null, event -> {}));
    }
}
