package pe.asterionsystem.core.state;

/** Nonpersistent, typed state contract. Durable state belongs to phase 05. */
public interface StateStore<S> {
    S current();

    void replace(S nextState);
}
