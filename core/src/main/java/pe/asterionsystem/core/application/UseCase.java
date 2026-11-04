package pe.asterionsystem.core.application;

/** Contract only: concrete use cases are introduced in their approved phases. */
@FunctionalInterface
public interface UseCase<I, O> {
    O execute(I request);
}
