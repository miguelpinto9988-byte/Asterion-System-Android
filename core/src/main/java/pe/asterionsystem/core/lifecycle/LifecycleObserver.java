package pe.asterionsystem.core.lifecycle;

@FunctionalInterface
public interface LifecycleObserver {
    void onLifecycleChanged(LifecyclePhase phase);
}
