package pe.asterionsystem.core.planning;

import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.*;
import org.junit.Test;
import pe.asterionsystem.core.catalog.*;
import pe.asterionsystem.core.domain.EntityId;

/** Verifies planner uses the catalog's bulk contract, preserving schedule order. */
public final class SchedulePlannerBulkTitlesTest {
    private static EntityId id(String value) { return new EntityId(value); }
    private static final LocalDate DATE = LocalDate.of(2026, 11, 4);

    private static final class FakeSchedules implements ScheduleRepository {
        final List<ScheduledActivity> entries = Arrays.asList(
                new ScheduledActivity(id("first"), id("activity-a"), DATE, 540, 30, 1, 1),
                new ScheduledActivity(id("second"), id("activity-b"), DATE, 600, 30, 1, 1),
                new ScheduledActivity(id("third"), id("activity-a"), DATE, 660, 30, 1, 1));
        public Optional<ScheduledActivity> find(EntityId key) { return Optional.empty(); }
        public List<ScheduledActivity> forDate(LocalDate day) {
            return DATE.equals(day) ? entries : Collections.emptyList();
        }
        public void insert(ScheduledActivity entry) { throw new UnsupportedOperationException(); }
        public boolean update(ScheduledActivity entry) { throw new UnsupportedOperationException(); }
        public boolean delete(EntityId key) { throw new UnsupportedOperationException(); }
    }

    private static final class FakeCatalog implements CatalogRepository {
        int batchCalls;
        int singleCalls;
        public Optional<CatalogItem> findById(EntityId id) {
            singleCalls++;
            return Optional.empty();
        }
        public List<CatalogCategory> listCategories() { return Collections.emptyList(); }
        public List<CatalogItem> search(CatalogQuery query) { return Collections.emptyList(); }
        @Override public Map<EntityId,String> titlesById(List<EntityId> ids) {
            batchCalls++;
            assertEquals(3, ids.size());
            Map<EntityId,String> titles = new HashMap<>();
            titles.put(id("activity-a"), "Actividad A");
            return titles;
        }
    }

    @Test public void resolvesRepeatedAndMissingTitlesInOneCall() {
        FakeCatalog catalog = new FakeCatalog();
        SchedulePlanner planner = new SchedulePlanner(new FakeSchedules(), catalog);
        List<ScheduleListing> listings = planner.listWithTitles(DATE);
        assertEquals(1, catalog.batchCalls);
        assertEquals(0, catalog.singleCalls);
        assertEquals(3, listings.size());
        assertEquals("Actividad A", listings.get(0).title());
        assertEquals("Actividad no disponible", listings.get(1).title());
        assertEquals("Actividad A", listings.get(2).title());
        assertEquals("first", listings.get(0).entry().id().value());
    }

    @Test public void emptyDayProducesNoListings() {
        FakeCatalog catalog = new FakeCatalog();
        List<ScheduleListing> result = new SchedulePlanner(new FakeSchedules(), catalog)
                .listWithTitles(DATE.plusDays(1));
        assertTrue(result.isEmpty());
        assertEquals(1, catalog.batchCalls);
    }
}
