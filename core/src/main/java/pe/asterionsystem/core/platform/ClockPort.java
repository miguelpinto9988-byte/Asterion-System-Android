package pe.asterionsystem.core.platform;

import java.time.Instant;

/** Injectible time source for deterministic future unit tests. */
@FunctionalInterface
public interface ClockPort {
    Instant now();
}
