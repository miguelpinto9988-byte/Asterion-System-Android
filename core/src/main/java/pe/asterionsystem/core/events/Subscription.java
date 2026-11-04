package pe.asterionsystem.core.events;

/** An idempotent handle to unregister an event listener. */
@FunctionalInterface
public interface Subscription extends AutoCloseable {
    @Override
    void close();
}
