package pe.asterionsystem.app.platform;

import java.util.Objects;
import pe.asterionsystem.core.lifecycle.LifecycleObserver;
import pe.asterionsystem.core.lifecycle.LifecyclePhase;

/** Translates Android Activity lifecycle callbacks to a platform-neutral port. */
public final class AndroidLifecycleAdapter {
    private final LifecycleObserver observer;

    public AndroidLifecycleAdapter(LifecycleObserver observer) {
        this.observer = Objects.requireNonNull(observer, "observer");
    }

    public void onCreated() { observer.onLifecycleChanged(LifecyclePhase.CREATED); }
    public void onStarted() { observer.onLifecycleChanged(LifecyclePhase.STARTED); }
    public void onResumed() { observer.onLifecycleChanged(LifecyclePhase.RESUMED); }
    public void onPaused() { observer.onLifecycleChanged(LifecyclePhase.PAUSED); }
    public void onStopped() { observer.onLifecycleChanged(LifecyclePhase.STOPPED); }
    public void onDestroyed() { observer.onLifecycleChanged(LifecyclePhase.DESTROYED); }
}
