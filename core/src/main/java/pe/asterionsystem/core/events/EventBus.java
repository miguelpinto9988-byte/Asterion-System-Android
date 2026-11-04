package pe.asterionsystem.core.events;

/**
 * Synchronous, in-process event delivery. Events are matched by exact runtime
 * class; listeners are responsible for unsubscribing when their owner ends.
 */
public interface EventBus {
    <E extends AppEvent> Subscription subscribe(Class<E> eventType, EventListener<? super E> listener);

    void publish(AppEvent event);
}
