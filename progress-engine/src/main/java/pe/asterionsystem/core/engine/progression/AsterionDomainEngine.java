package pe.asterionsystem.core.engine.progression;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import pe.asterionsystem.core.catalog.CatalogCategory;
import pe.asterionsystem.core.catalog.CatalogItem;
import pe.asterionsystem.core.catalog.CatalogItemKind;
import pe.asterionsystem.core.catalog.CatalogQuery;
import pe.asterionsystem.core.catalog.CatalogRepository;
import pe.asterionsystem.core.domain.EntityId;
import pe.asterionsystem.core.planning.ScheduleListing;
import pe.asterionsystem.core.planning.SchedulePlanner;
import pe.asterionsystem.core.planning.ScheduledActivity;
import pe.asterionsystem.core.progress.ProgressEntry;
import pe.asterionsystem.core.progress.ProgressRepository;
import pe.asterionsystem.core.progress.ProgressService;
import pe.asterionsystem.core.settings.AppPreferences;
import pe.asterionsystem.core.settings.PreferencesService;

/**
 * Independent core facade for exercises, planning, settings and progress patterns.
 * Synchronous repository calls MUST be dispatched off the Android main thread.
 * Pattern evaluation is pure: only explicit command methods may persist data.
 */
public final class AsterionDomainEngine {
    private final CatalogRepository catalog;
    private final SchedulePlanner planning;
    private final PreferencesService settings;
    private final ProgressRepository progressRepository;
    private final ProgressService tracking;
    private final ProgressPatternEngine patterns;

    public AsterionDomainEngine(CatalogRepository catalog, SchedulePlanner planning,
                               PreferencesService settings, ProgressRepository progressRepository,
                               ProgressPatternEngine patterns) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.planning = Objects.requireNonNull(planning, "planning");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.progressRepository = Objects.requireNonNull(progressRepository, "progressRepository");
        this.tracking = new ProgressService(progressRepository);
        this.patterns = Objects.requireNonNull(patterns, "patterns");
    }

    // Catalog: exercise candidates are the existing ACTIVITY kind; no new DB table.
    public List<CatalogCategory> categories() { return catalog.listCategories(); }

    public List<CatalogItem> exerciseCatalog(String query, EntityId categoryId, int limit, int offset) {
        return catalog.search(new CatalogQuery(query, categoryId, CatalogItemKind.ACTIVITY, limit, offset));
    }

    // Planning: preserve the existing conflict checks and SQLite transactional backstop.
    public List<ScheduleListing> plannedFor(LocalDate date) {
        return planning.listWithTitles(Objects.requireNonNull(date, "date"));
    }

    public void createPlan(ScheduledActivity entry) { planning.create(entry); }
    public boolean updatePlan(ScheduledActivity entry) { return planning.update(entry); }
    public boolean deletePlan(EntityId id) { return planning.delete(id); }

    // Settings: future pattern selection remains separate from the three current UI preferences.
    public AppPreferences currentSettings() { return settings.current(); }
    public boolean updateSettings(AppPreferences selected) { return settings.update(selected); }
    public void resetSettings() { settings.reset(); }

    // Tracking writes are explicit. A pattern NEVER marks an exercise as completed.
    public List<ProgressEntry> progressFor(LocalDate date) {
        return tracking.forDate(Objects.requireNonNull(date, "date"));
    }

    public void recordOutcome(EntityId scheduledId, pe.asterionsystem.core.progress.TrackingStatus status,
                              String note, long recordedAtMs) {
        tracking.mark(scheduledId, status, note, recordedAtMs);
    }

    /** One weekly snapshot; the same records serve the standard and future strategies. */
    public ProgressAssessment assessWeek(LocalDate monday, ProgressPatternConfig configuration) {
        Objects.requireNonNull(monday, "monday");
        Objects.requireNonNull(configuration, "configuration");
        if (monday.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new IllegalArgumentException("El período debe comenzar un lunes");
        }
        if (!patterns.registeredPatternIds().contains(configuration.patternId())) {
            throw new IllegalArgumentException("Patrón no registrado");
        }
        List<ProgressEntry> rows = progressRepository.forWeek(monday);
        ProgressContext context = new ProgressContext(
                monday, rows, settings.current(), configuration);
        return patterns.assess(context);
    }
}
