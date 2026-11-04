package pe.asterionsystem.core.state;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/** Stores one non-null state in memory; no disk or Android dependencies. */
public final class InMemoryStateStore<S> implements StateStore<S> {
    private final AtomicReference<S> current;

    public InMemoryStateStore(S initialState) {
        current = new AtomicReference<>(Objects.requireNonNull(initialState, "initialState"));
    }

    @Override
    public S current() {
        return current.get();
    }

    @Override
    public void replace(S nextState) {
        current.set(Objects.requireNonNull(nextState, "nextState"));
    }
}
