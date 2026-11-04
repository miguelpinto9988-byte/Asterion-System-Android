package pe.asterionsystem.app;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;
import android.widget.FrameLayout;
import pe.asterionsystem.app.application.ApplicationCoordinator;
import pe.asterionsystem.app.data.AsterionDatabaseHelper;
import pe.asterionsystem.app.data.SQLiteCatalogRepository;
import pe.asterionsystem.app.data.SQLiteScheduleRepository;
import pe.asterionsystem.app.data.SQLiteProgressRepository;
import pe.asterionsystem.app.platform.AndroidLifecycleAdapter;
import pe.asterionsystem.app.platform.NavigationBundleAdapter;
import pe.asterionsystem.app.presentation.CatalogPresenter;
import pe.asterionsystem.app.presentation.NavigationRenderer;
import pe.asterionsystem.app.presentation.SchedulePresenter;
import pe.asterionsystem.app.presentation.ProgressPresenter;
import pe.asterionsystem.core.catalog.BackgroundCatalogOperations;
import pe.asterionsystem.core.domain.EntityId;
import pe.asterionsystem.core.events.EventBus;
import pe.asterionsystem.core.events.InMemoryEventBus;
import pe.asterionsystem.core.events.Subscription;
import pe.asterionsystem.core.lifecycle.LifecyclePhase;
import pe.asterionsystem.core.navigation.NavigationChangedEvent;
import pe.asterionsystem.core.navigation.NavigationController;
import pe.asterionsystem.core.navigation.NavigationRoute;
import pe.asterionsystem.core.navigation.NavigationState;
import pe.asterionsystem.core.planning.BackgroundScheduleOperations;
import pe.asterionsystem.core.planning.SchedulePlanner;
import pe.asterionsystem.core.progress.BackgroundProgressOperations;
import pe.asterionsystem.core.progress.ProgressService;
import pe.asterionsystem.core.state.InMemoryStateStore;

/** Native Android shell. Catalog, planning and explicit tracking enabled; reminders and execution excluded. */
public final class MainActivity extends Activity {
    private static final String KEY_CATALOG_SELECTED_ID = "asterion.catalog.selected.id.v1";

    private AndroidLifecycleAdapter lifecycleAdapter;
    private NavigationController navigation;
    private NavigationRenderer renderer;
    private CatalogPresenter catalogPresenter;
    private SchedulePresenter schedulePresenter;
    private ProgressPresenter progressPresenter;
    private EntityId selectedCatalogItem;
    private Subscription navigationSubscription;
    private OnBackInvokedCallback systemBackCallback;
    private boolean backCallbackRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus events = new InMemoryEventBus();
        ApplicationCoordinator coordinator = new ApplicationCoordinator(
                events, new InMemoryStateStore<>(LifecyclePhase.INITIALIZED));
        lifecycleAdapter = new AndroidLifecycleAdapter(coordinator);
        lifecycleAdapter.onCreated();

        navigation = new NavigationController(
                new InMemoryStateStore<>(NavigationState.initial()), events);
        setContentView(R.layout.activity_main);
        renderer = new NavigationRenderer(this, (FrameLayout) findViewById(R.id.navigation_host));

        AsterionDatabaseHelper database = new AsterionDatabaseHelper(getApplicationContext());
        catalogPresenter = new CatalogPresenter(this, database,
                new BackgroundCatalogOperations(new SQLiteCatalogRepository(database)));
        catalogPresenter.restore(savedInstanceState);

        AsterionDatabaseHelper plannerDb = new AsterionDatabaseHelper(getApplicationContext());
        schedulePresenter = new SchedulePresenter(this, plannerDb,
                new BackgroundScheduleOperations(new SchedulePlanner(
                        new SQLiteScheduleRepository(plannerDb),
                        new SQLiteCatalogRepository(plannerDb))));
        schedulePresenter.restore(savedInstanceState);

        AsterionDatabaseHelper progressDb = new AsterionDatabaseHelper(getApplicationContext());
        progressPresenter = new ProgressPresenter(this, progressDb,
                new BackgroundProgressOperations(new ProgressService(
                        new SQLiteProgressRepository(progressDb))));
        progressPresenter.restore(savedInstanceState);

        if (savedInstanceState != null) {
            String rawId = savedInstanceState.getString(KEY_CATALOG_SELECTED_ID, "");
            if (rawId != null && !rawId.trim().isEmpty() && rawId.length() <= 64) {
                selectedCatalogItem = new EntityId(rawId);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            systemBackCallback = () -> navigation.goBack();
        }
        navigationSubscription = events.subscribe(NavigationChangedEvent.class,
                event -> renderNavigation());
        if (!navigation.restore(NavigationBundleAdapter.read(savedInstanceState))) {
            renderNavigation();
        }
    }

    private void renderNavigation() {
        NavigationRoute route = navigation.state().current();
        if (route == NavigationRoute.CATALOG_DETAIL && selectedCatalogItem == null) {
            // A malformed or obsolete snapshot must never display an orphan detail.
            navigation.goBack();
            return;
        }
        View screen = renderer.render(route,
                () -> navigation.navigateTo(NavigationRoute.SECONDARY),
                () -> navigation.goBack(),
                () -> navigation.navigateTo(NavigationRoute.CATALOG),
                () -> navigation.navigateTo(NavigationRoute.PLAN),
                () -> navigation.navigateTo(NavigationRoute.PROGRESS));

        if (route == NavigationRoute.CATALOG) {
            catalogPresenter.bindList(screen, id -> {
                selectedCatalogItem = id;
                navigation.navigateTo(NavigationRoute.CATALOG_DETAIL);
            }, () -> navigation.goBack());
        } else if (route == NavigationRoute.CATALOG_DETAIL) {
            catalogPresenter.bindDetail(screen, selectedCatalogItem, () -> navigation.goBack());
        } else if (route == NavigationRoute.PLAN) {
            schedulePresenter.bindCalendar(screen,
                    () -> navigation.goBack(),
                    () -> {
                        schedulePresenter.newEntry();
                        navigation.navigateTo(NavigationRoute.PLAN_FORM);
                    },
                    id -> {
                        schedulePresenter.editEntry(id);
                        navigation.navigateTo(NavigationRoute.PLAN_FORM);
                    });
            selectedCatalogItem = null;
        } else if (route == NavigationRoute.PLAN_FORM) {
            schedulePresenter.bindForm(screen, () -> navigation.goBack());
            selectedCatalogItem = null;
        } else if (route == NavigationRoute.PROGRESS) {
            progressPresenter.bindTracking(screen, () -> navigation.goBack(),
                    () -> navigation.navigateTo(NavigationRoute.PROGRESS_SUMMARY));
            selectedCatalogItem = null;
        } else if (route == NavigationRoute.PROGRESS_SUMMARY) {
            progressPresenter.bindSummary(screen, () -> navigation.goBack());
            selectedCatalogItem = null;
        } else {
            selectedCatalogItem = null;
        }
        updateSystemBackRegistration();
    }

    private void updateSystemBackRegistration() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return;
        boolean needed = navigation.state().canGoBack();
        if (needed && !backCallbackRegistered) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT, systemBackCallback);
            backCallbackRegistered = true;
        } else if (!needed && backCallbackRegistered) {
            getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(systemBackCallback);
            backCallbackRegistered = false;
        }
    }

    @Override public void onBackPressed() {
        if (!navigation.goBack()) super.onBackPressed();
    }

    @Override protected void onSaveInstanceState(Bundle output) {
        NavigationBundleAdapter.save(output, navigation.state());
        catalogPresenter.save(output);
        schedulePresenter.save(output);
        progressPresenter.save(output);
        if (selectedCatalogItem != null) {
            output.putString(KEY_CATALOG_SELECTED_ID, selectedCatalogItem.value());
        }
        super.onSaveInstanceState(output);
    }

    @Override protected void onStart() {
        super.onStart();
        lifecycleAdapter.onStarted();
    }

    @Override protected void onResume() {
        super.onResume();
        lifecycleAdapter.onResumed();
    }

    @Override protected void onPause() {
        lifecycleAdapter.onPaused();
        super.onPause();
    }

    @Override protected void onStop() {
        lifecycleAdapter.onStopped();
        super.onStop();
    }

    @Override protected void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && backCallbackRegistered) {
            getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(systemBackCallback);
            backCallbackRegistered = false;
        }
        progressPresenter.close();
        schedulePresenter.close();
        catalogPresenter.close();
        navigationSubscription.close();
        lifecycleAdapter.onDestroyed();
        super.onDestroy();
    }
}
