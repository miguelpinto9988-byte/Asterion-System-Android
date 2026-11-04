package pe.asterionsystem.core.lifecycle;

/** Neutral representation of lifecycle callbacks; not an Android dependency. */
public enum LifecyclePhase {
    INITIALIZED,
    CREATED,
    STARTED,
    RESUMED,
    PAUSED,
    STOPPED,
    DESTROYED
}
