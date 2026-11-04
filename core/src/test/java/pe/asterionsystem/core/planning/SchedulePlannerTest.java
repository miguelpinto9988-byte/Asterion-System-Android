package pe.asterionsystem.core.planning;

import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.*;
import org.junit.Test;
import pe.asterionsystem.core.catalog.*;
import pe.asterionsystem.core.domain.EntityId;

public final class SchedulePlannerTest {
    private static final LocalDate DAY = LocalDate.of(2026, 11, 4);
    private static EntityId id(String value) { return new EntityId(value); }
    private static ScheduledActivity entry(String key, int start, int duration) {
        return new ScheduledActivity(id(key), id("demo-revision"), DAY, start, duration, 1, 1);
    }

    @Test public void adjacentSlotsAreAllowedAndOverlapsRejected() {
        ScheduledActivity first = entry("first", 540, 60);
        assertTrue(first.overlaps(entry("second", 599, 10)));
        assertFalse(first.overlaps(entry("second", 600, 20)));
        assertFalse(first.overlaps(new ScheduledActivity(
                id("third"), id("demo-revision"), DAY.plusDays(1), 550, 20, 1, 1)));
    }

    @Test public void rejectsOvernightAndInvalidDurations() {
        try {
            entry("a", 1430, 20);
            fail("Must reject overnight time");
        } catch (IllegalArgumentException expected) { }
        try {
            entry("a", 540, 0);
            fail("Must reject zero duration");
        } catch (IllegalArgumentException expected) { }
    }

    @Test public void rejectsOverlapsAndPreservesOriginalTimestampOnEdit() {
        FakeSchedules schedules = new FakeSchedules();
        SchedulePlanner planner = new SchedulePlanner(schedules, fakeCatalog());
        ScheduledActivity existing = entry("existing", 540, 60);
        planner.create(existing);
        try {
            planner.create(entry("overlap", 560, 30));
            fail("Expected conflict");
        } catch (ScheduleConflictException expected) { }
        assertEquals(1, schedules.forDate(DAY).size());
        ScheduledActivity moved = existing.reschedule(id("demo-revision"), DAY, 600, 30, 10);
        assertEquals(1, moved.createdAtMs());
        assertTrue(planner.update(moved));
        planner.create(entry("adjacent", 630, 30));
        assertEquals(2, planner.forDate(DAY).size());
        assertTrue(planner.delete(id("adjacent")));
        assertFalse(planner.delete(id("adjacent")));
    }

    @Test public void rejectsCatalogContent() {
        SchedulePlanner planner = new SchedulePlanner(new FakeSchedules(), fakeCatalog());
        ScheduledActivity content = new ScheduledActivity(
                id("content"), id("demo-intro"), DAY, 600, 30, 1, 1);
        try {
            planner.create(content);
            fail("Expected non-activity rejection");
        } catch (IllegalArgumentException expected) { }
    }

    private static CatalogRepository fakeCatalog() {
        return new CatalogRepository() {
            @Override public Optional<CatalogItem> findById(EntityId id) {
                CatalogItemKind kind = id.value().equals("demo-revision")
                        ? CatalogItemKind.ACTIVITY : CatalogItemKind.CONTENT;
                return Optional.of(new CatalogItem(id, "Ejemplo", "", kind,
                        new CatalogCategory(new EntityId("general"), "General", 0), Collections.emptyList()));
            }
            @Override public List<CatalogCategory> listCategories() { return Collections.emptyList(); }
            @Override public List<CatalogItem> search(CatalogQuery query) { return Collections.emptyList(); }
        };
    }

    private static final class FakeSchedules implements ScheduleRepository {
        private final Map<EntityId, ScheduledActivity> data = new HashMap<>();
        @Override public Optional<ScheduledActivity> find(EntityId id) { return Optional.ofNullable(data.get(id)); }
        @Override public List<ScheduledActivity> forDate(LocalDate date) {
            List<ScheduledActivity> out = new ArrayList<>();
            for (ScheduledActivity value : data.values()) if (value.date().equals(date)) out.add(value);
            return out;
        }
        @Override public void insert(ScheduledActivity entry) { data.put(entry.id(), entry); }
        @Override public boolean update(ScheduledActivity entry) {
            return data.replace(entry.id(), entry) != null;
        }
        @Override public boolean delete(EntityId id) { return data.remove(id) != null; }
    }
}
