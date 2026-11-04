package pe.asterionsystem.core.progress;

import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.*;
import org.junit.Test;
import pe.asterionsystem.core.domain.EntityId;

public final class ProgressServiceTest {
    private static final class FakeRepository implements ProgressRepository {
        private final Map<EntityId, TrackingRecord> records = new HashMap<>();
        @Override public List<ProgressEntry> forDate(LocalDate date) { return Collections.emptyList(); }
        @Override public List<ProgressEntry> forWeek(LocalDate monday) { return Collections.emptyList(); }
        @Override public void save(TrackingRecord r) { records.put(r.scheduleId(), r); }
        @Override public boolean clear(EntityId id) { return records.remove(id) != null; }
    }

    @Test public void markAndClearOutcomes() {
        FakeRepository repo = new FakeRepository();
        ProgressService service = new ProgressService(repo);
        EntityId id = new EntityId("plan-1");
        service.mark(id, TrackingStatus.COMPLETED, "Realizada", 10);
        assertEquals(TrackingStatus.COMPLETED, repo.records.get(id).status());
        service.mark(id, TrackingStatus.SKIPPED, "Omitida", 11);
        assertEquals(TrackingStatus.SKIPPED, repo.records.get(id).status());
        service.mark(id, TrackingStatus.PENDING, "", 12);
        assertFalse(repo.records.containsKey(id));
    }

    @Test public void requiresMondayForWeeklySummary() {
        ProgressService service = new ProgressService(new FakeRepository());
        try {
            service.summaryForWeek(LocalDate.of(2026, 11, 4));
            fail("Expected Monday validation");
        } catch (IllegalArgumentException expected) { }
        assertEquals(0, service.summaryForWeek(LocalDate.of(2026, 11, 2)).total());
    }
}
