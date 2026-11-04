package pe.asterionsystem.core.events;

/** Handles one concrete event type in the publishing thread. */
@FunctionalInterface
public interface EventListener<E extends AppEvent> {
    void onEvent(E event);
}
