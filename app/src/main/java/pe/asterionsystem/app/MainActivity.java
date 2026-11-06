package pe.asterionsystem.app;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import pe.asterionsystem.app.application.ApplicationCoordinator;
import pe.asterionsystem.app.data.AsterionDatabaseHelper;
import pe.asterionsystem.app.data.SQLiteCatalogRepository;
import pe.asterionsystem.app.data.SQLiteScheduleRepository;
import pe.asterionsystem.app.data.SQLiteProgressRepository;
import pe.asterionsystem.app.data.SQLiteLocalStorageCheck;
import pe.asterionsystem.app.platform.AndroidLifecycleAdapter;
import pe.asterionsystem.app.platform.NavigationBundleAdapter;
import pe.asterionsystem.app.platform.AndroidPreferencesRepository;
import pe.asterionsystem.app.presentation.CatalogPresenter;
import pe.asterionsystem.app.presentation.NavigationRenderer;
import pe.asterionsystem.app.presentation.SchedulePresenter;
import pe.asterionsystem.app.presentation.ProgressPresenter;
import pe.asterionsystem.app.presentation.LocalStatusPresenter;
import pe.asterionsystem.app.presentation.SettingsPresenter;
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
import pe.asterionsystem.core.offline.LocalStorageService;
import pe.asterionsystem.core.offline.BackgroundLocalStorageChecks;
import pe.asterionsystem.core.state.InMemoryStateStore;
import pe.asterionsystem.core.settings.AppPreferences;
import pe.asterionsystem.core.settings.PreferencesService;

/** Native Android shell with XML screens and private preferences; no reminders or auto-execution. */
public final class MainActivity extends ComponentActivity {
    private static final String KEY_CATALOG_SELECTED_ID = "asterion.catalog.selected.id.v1";

    private AndroidLifecycleAdapter lifecycleAdapter;
    private NavigationController navigation;
    private NavigationRenderer renderer;
    private CatalogPresenter catalogPresenter;
    private SchedulePresenter schedulePresenter;
    private ProgressPresenter progressPresenter;
    private LocalStatusPresenter localStatusPresenter;
    private PreferencesService preferencesService;
    private SettingsPresenter settingsPresenter;
    private EntityId selectedCatalogItem;
    private Subscription navigationSubscription;
    private OnBackPressedCallback navigationBackCallback;

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
        configureSafeSystemInsets();
        preferencesService = new PreferencesService(
                new AndroidPreferencesRepository(getApplicationContext()));
        settingsPresenter = new SettingsPresenter(preferencesService);
        renderer = new NavigationRenderer(this, (FrameLayout) findViewById(R.id.navigation_host),
                preferencesService);

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

        AsterionDatabaseHelper healthDb = new AsterionDatabaseHelper(getApplicationContext());
        localStatusPresenter = new LocalStatusPresenter(this, healthDb,
                new BackgroundLocalStorageChecks(new LocalStorageService(
                        new SQLiteLocalStorageCheck(healthDb))));

        if (savedInstanceState != null) {
            String rawId = savedInstanceState.getString(KEY_CATALOG_SELECTED_ID, "");
            if (rawId != null && !rawId.trim().isEmpty() && rawId.length() <= 64) {
                selectedCatalogItem = new EntityId(rawId);
            }
        }
        // Backward-compatible dispatcher: enabled only while navigation has history.
        // AndroidX delegates to system Back when the app is already on its root screen.
        navigationBackCallback = new OnBackPressedCallback(false) {
            @Override public void handleOnBackPressed() {
                navigation.goBack();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, navigationBackCallback);
        navigationSubscription = events.subscribe(NavigationChangedEvent.class,
                event -> renderNavigation());
        if (!navigation.restore(initialNavigation(savedInstanceState))) {
            renderNavigation();
        }
    }

    /**
     * Target 36 is edge-to-edge on Android 15+ / Android 16. Apply the current
     * system bars and cutout insets to the full-screen navigation container.
     * Older OS versions keep their framework-managed window fitting.
     * This replaces fitsSystemWindows to avoid applying insets twice.
     */
    private void configureSafeSystemInsets() {
        if (Build.VERSION.SDK_INT < 35) return;
        View root = findViewById(R.id.bootstrap_root);
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            android.graphics.Insets safe = insets.getInsets(
                    WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
            view.setPadding(safe.left, safe.top, safe.right, safe.bottom);
            return insets;
        });
        root.requestApplyInsets();
    }

    /**
     * Cold launches honor the preferred section. A real saved Bundle always wins:
     * it restores the route being viewed instead of applying a new start route.
     */
    private NavigationState initialNavigation(Bundle saved) {
        if (saved != null) return NavigationBundleAdapter.read(saved);
        AppPreferences.StartSection preferred = preferencesService.current().startSection();
        NavigationRoute route;
        switch (preferred) {
            case CATALOG: route = NavigationRoute.CATALOG; break;
            case PLAN: route = NavigationRoute.PLAN; break;
            case PROGRESS: route = NavigationRoute.PROGRESS; break;
            default: route = NavigationRoute.HOME; break;
        }
        return NavigationState.initial().navigateTo(route);
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
                () -> navigation.navigateTo(NavigationRoute.PROGRESS),
                () -> navigation.navigateTo(NavigationRoute.SETTINGS),
                () -> navigation.navigateTo(NavigationRoute.LOCAL_STATUS));

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
        } else if (route == NavigationRoute.SETTINGS) {
            settingsPresenter.bind(screen, () -> navigation.goBack(), this::renderNavigation);
            selectedCatalogItem = null;
        } else if (route == NavigationRoute.LOCAL_STATUS) {
            localStatusPresenter.bind(screen, () -> navigation.goBack());
            selectedCatalogItem = null;
        } else {
            selectedCatalogItem = null;
        }
        updateBackHandling();
    }

    private void updateBackHandling() {
        navigationBackCallback.setEnabled(navigation.state().canGoBack());
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
        localStatusPresenter.close();
        progressPresenter.close();
        schedulePresenter.close();
        catalogPresenter.close();
        navigationSubscription.close();
        lifecycleAdapter.onDestroyed();
        super.onDestroy();
    }
}
