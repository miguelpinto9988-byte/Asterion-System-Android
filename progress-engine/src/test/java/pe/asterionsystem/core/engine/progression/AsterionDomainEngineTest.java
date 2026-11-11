package pe.asterionsystem.core.engine.progression;

import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.*;
import org.junit.Test;
import pe.asterionsystem.core.catalog.*;
import pe.asterionsystem.core.domain.EntityId;
import pe.asterionsystem.core.planning.*;
import pe.asterionsystem.core.progress.*;
import pe.asterionsystem.core.settings.*;

public final class AsterionDomainEngineTest {
    private static final LocalDate MONDAY = LocalDate.of(2026, 11, 2);
    private static EntityId id(String text) { return new EntityId(text); }

    private static final class Catalog implements CatalogRepository {
        final CatalogCategory category = new CatalogCategory(id("activity_category"), "Actividades", 0);
        final CatalogItem activity = new CatalogItem(id("exercise"), "Ejercicio de prueba", "",
                CatalogItemKind.ACTIVITY, category, Collections.emptyList());
        CatalogQuery lastQuery;
        public List<CatalogCategory> listCategories() { return Collections.singletonList(category); }
        public List<CatalogItem> search(CatalogQuery query) {
            lastQuery = query;
            return query.kind() == CatalogItemKind.ACTIVITY
                    ? Collections.singletonList(activity) : Collections.emptyList();
        }
        public Optional<CatalogItem> findById(EntityId key) {
            return activity.id().equals(key) ? Optional.of(activity) : Optional.empty();
        }
    }

    private static final class Plans implements ScheduleRepository {
        final List<ScheduledActivity> rows = new ArrayList<>();
        int insertCalls;
        public Optional<ScheduledActivity> find(EntityId key) {
            return rows.stream().filter(x -> x.id().equals(key)).findFirst();
        }
        public List<ScheduledActivity> forDate(LocalDate date) {
            List<ScheduledActivity> result = new ArrayList<>();
            for (ScheduledActivity entry : rows) if (entry.date().equals(date)) result.add(entry);
            return result;
        }
        public void insert(ScheduledActivity item) { insertCalls++; rows.add(item); }
        public boolean update(ScheduledActivity item) { return false; }
        public boolean delete(EntityId key) { return rows.removeIf(x -> x.id().equals(key)); }
    }

    private static final class Settings implements PreferencesRepository {
        AppPreferences current = AppPreferences.defaults();
        int saves;
        public AppPreferences read() { return current; }
        public void save(AppPreferences value) { current = value; saves++; }
        public void reset() { current = AppPreferences.defaults(); }
    }

    private static final class Tracking implements ProgressRepository {
        final List<ProgressEntry> entries = new ArrayList<>();
        int weekReads;
        int saves;
        int clears;
        public List<ProgressEntry> forDate(LocalDate day) { return Collections.unmodifiableList(entries); }
        public List<ProgressEntry> forWeek(LocalDate monday) { weekReads++; return entries; }
        public void save(TrackingRecord record) { saves++; }
        public boolean clear(EntityId key) { clears++; return true; }
    }

    private static final class Fixture {
        final Catalog catalog = new Catalog();
        final Plans plans = new Plans();
        final Settings settings = new Settings();
        final Tracking tracking = new Tracking();
        final AsterionDomainEngine engine = new AsterionDomainEngine(
                catalog, new SchedulePlanner(plans, catalog),
                new PreferencesService(settings), tracking, ProgressPatternEngine.standard());
    }

    @Test public void catalogueAndPlansDelegateToExistingServices() {
        Fixture fixture = new Fixture();
        assertEquals(1, fixture.engine.categories().size());
        assertEquals(1, fixture.engine.exerciseCatalog("Ejercicio", null, 10, 0).size());
        assertEquals(CatalogItemKind.ACTIVITY, fixture.catalog.lastQuery.kind());
        ScheduledActivity plan = new ScheduledActivity(id("plan"), id("exercise"),
                MONDAY.plusDays(2), 600, 30, 1, 1);
        fixture.engine.createPlan(plan);
        assertEquals(1, fixture.plans.insertCalls);
        assertEquals("Ejercicio de prueba",
                fixture.engine.plannedFor(MONDAY.plusDays(2)).get(0).title());
        assertTrue(fixture.engine.deletePlan(id("plan")));
    }

    @Test public void progressEvaluationDoesNotWriteRecordsOrPreferences() {
        Fixture fixture = new Fixture();
        ScheduledActivity plan = new ScheduledActivity(id("plan"), id("exercise"),
                MONDAY.plusDays(2), 600, 30, 1, 1);
        fixture.tracking.entries.add(new ProgressEntry(plan, "Ejercicio de prueba",
                TrackingStatus.COMPLETED, ""));
        ProgressAssessment result = fixture.engine.assessWeek(MONDAY, ProgressPatternConfig.standard());
        assertEquals(100, result.scorePercent().getAsInt());
        assertEquals(1, fixture.tracking.weekReads);
        assertEquals(0, fixture.tracking.saves);
        assertEquals(0, fixture.tracking.clears);
        assertEquals(0, fixture.settings.saves);
        assertEquals(0, fixture.plans.insertCalls);
    }

    @Test public void explicitOutcomeAndSettingsCommandsRemainSeparate() {
        Fixture fixture = new Fixture();
        assertEquals(AppPreferences.defaults(), fixture.engine.currentSettings());
        AppPreferences large = AppPreferences.defaults()
                .withTextSize(AppPreferences.TextSize.LARGE);
        assertTrue(fixture.engine.updateSettings(large));
        assertEquals(1, fixture.settings.saves);
        fixture.engine.recordOutcome(id("plan"), TrackingStatus.COMPLETED, "", 10L);
        assertEquals(1, fixture.tracking.saves);
        fixture.engine.recordOutcome(id("plan"), TrackingStatus.PENDING, "", 11L);
        assertEquals(1, fixture.tracking.clears);
    }

    @Test public void taxonomyIsReadOnlyAndCanBeInjectedAlongsideLegacyCatalog() throws Exception {
        pe.asterionsystem.core.engine.exercises.ExerciseCatalog taxonomy;
        try (java.io.Reader families = new java.io.InputStreamReader(
                     getClass().getResourceAsStream("/asterion_exercises/v1/families.tsv"),
                     java.nio.charset.StandardCharsets.UTF_8);
             java.io.Reader types = new java.io.InputStreamReader(
                     getClass().getResourceAsStream("/asterion_exercises/v1/types.tsv"),
                     java.nio.charset.StandardCharsets.UTF_8);
             java.io.Reader variants = new java.io.InputStreamReader(
                     getClass().getResourceAsStream("/asterion_exercises/v1/variants.tsv"),
                     java.nio.charset.StandardCharsets.UTF_8)) {
            taxonomy = pe.asterionsystem.core.engine.exercises.ExerciseCatalogLoader
                    .load(families, types, variants);
        }
        Fixture fixture = new Fixture();
        AsterionDomainEngine withTaxonomy = new AsterionDomainEngine(
                fixture.catalog, new SchedulePlanner(fixture.plans, fixture.catalog),
                new PreferencesService(fixture.settings), fixture.tracking,
                ProgressPatternEngine.standard(), taxonomy);
        assertEquals(125, withTaxonomy.exerciseFamilies().size());
        assertEquals(3, withTaxonomy.exerciseTypes("sentadillas").size());
        assertEquals(7, withTaxonomy.exerciseVariants("sentadilla_bilateral").size());
        assertFalse(withTaxonomy.searchExerciseVariants(
                "sentadilla", "piernas", "silla", null, 2, 50, 0).isEmpty());
        assertEquals(0, fixture.tracking.saves);
        assertEquals(0, fixture.tracking.clears);
        assertEquals(0, fixture.settings.saves);
        assertEquals(0, fixture.plans.insertCalls);
    }

    @Test public void rejectsUnknownPatternBeforeStorageRead() {
        Fixture fixture = new Fixture();
        try {
            fixture.engine.assessWeek(MONDAY,
                    new ProgressPatternConfig("not_installed", Collections.emptyMap()));
            fail("Unknown strategy must fail");
        } catch (IllegalArgumentException expected) { }
        assertEquals(0, fixture.tracking.weekReads);
    }
}
