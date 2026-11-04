package pe.asterionsystem.app.platform;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import pe.asterionsystem.core.lifecycle.LifecyclePhase;

public final class AndroidLifecycleAdapterTest {
    @Test
    public void forwardsAllActivityCallbacksInOrder() {
        List<LifecyclePhase> observed = new ArrayList<>();
        AndroidLifecycleAdapter adapter = new AndroidLifecycleAdapter(observed::add);
        adapter.onCreated();
        adapter.onStarted();
        adapter.onResumed();
        adapter.onPaused();
        adapter.onStopped();
        adapter.onDestroyed();
        assertEquals(List.of(LifecyclePhase.CREATED, LifecyclePhase.STARTED,
                LifecyclePhase.RESUMED, LifecyclePhase.PAUSED,
                LifecyclePhase.STOPPED, LifecyclePhase.DESTROYED), observed);
    }
}
