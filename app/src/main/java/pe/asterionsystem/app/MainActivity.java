package pe.asterionsystem.app;

import android.app.Activity;
import android.os.Bundle;
import pe.asterionsystem.app.application.ApplicationCoordinator;
import pe.asterionsystem.app.platform.AndroidLifecycleAdapter;
import pe.asterionsystem.core.events.InMemoryEventBus;
import pe.asterionsystem.core.lifecycle.LifecyclePhase;
import pe.asterionsystem.core.state.InMemoryStateStore;

/** Android shell; keeps the phase-01 XML untouched. No feature UI is enabled. */
public final class MainActivity extends Activity {
    private AndroidLifecycleAdapter lifecycleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApplicationCoordinator coordinator = new ApplicationCoordinator(
                new InMemoryEventBus(),
                new InMemoryStateStore<>(LifecyclePhase.INITIALIZED));
        lifecycleAdapter = new AndroidLifecycleAdapter(coordinator);
        lifecycleAdapter.onCreated();
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        lifecycleAdapter.onStarted();
    }

    @Override
    protected void onResume() {
        super.onResume();
        lifecycleAdapter.onResumed();
    }

    @Override
    protected void onPause() {
        lifecycleAdapter.onPaused();
        super.onPause();
    }

    @Override
    protected void onStop() {
        lifecycleAdapter.onStopped();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        lifecycleAdapter.onDestroyed();
        super.onDestroy();
    }
}
