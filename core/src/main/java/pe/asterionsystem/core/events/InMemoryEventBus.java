package pe.asterionsystem.core.events;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Typed, in-memory event delivery. Synchronous and exact-class only.
 * An exception thrown by a listener propagates to the publisher; subsequent
 * listeners are not invoked. No persistence, replay, or background threads.
 */
public final class InMemoryEventBus implements EventBus {
    private final ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<Consumer<AppEvent>>> handlers =
            new ConcurrentHashMap<>();

    @Override
    public <E extends AppEvent> Subscription subscribe(Class<E> eventType,
                                                        EventListener<? super E> listener) {
        Objects.requireNonNull(eventType, "eventType");
        Objects.requireNonNull(listener, "listener");
        Consumer<AppEvent> delivery = event -> listener.onEvent(eventType.cast(event));
        handlers.compute(eventType, (type, existing) -> {
            CopyOnWriteArrayList<Consumer<AppEvent>> entries =
                    existing == null ? new CopyOnWriteArrayList<>() : existing;
            entries.add(delivery);
            return entries;
        });
        AtomicBoolean active = new AtomicBoolean(true);
        return () -> {
            if (active.compareAndSet(true, false)) {
                handlers.computeIfPresent(eventType, (type, entries) -> {
                    entries.remove(delivery);
                    return entries.isEmpty() ? null : entries;
                });
            }
        };
    }

    @Override
    public void publish(AppEvent event) {
        Objects.requireNonNull(event, "event");
        CopyOnWriteArrayList<Consumer<AppEvent>> listeners = handlers.get(event.getClass());
        if (listeners != null) {
            for (Consumer<AppEvent> listener : listeners) {
                listener.accept(event);
            }
        }
    }
}
