package pe.asterionsystem.core.progress;

import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.*;
import org.junit.Test;
import pe.asterionsystem.core.domain.EntityId;
import pe.asterionsystem.core.planning.ScheduledActivity;

public final class ProgressSummaryTest {
    private static ProgressEntry item(String id, TrackingStatus status) {
        ScheduledActivity schedule = new ScheduledActivity(
                new EntityId(id), new EntityId("demo-revision"),
                LocalDate.of(2026, 11, 4), 600, 30, 1, 1);
        return new ProgressEntry(schedule, "Actividad de prueba", status, "");
    }

    @Test public void emptyWeekHasNoRate() {
        ProgressSummary p = ProgressSummary.from(Collections.emptyList());
        assertEquals(0, p.total());
        assertFalse(p.hasEvaluated());
        assertEquals(0, p.completionPercent());
    }

    @Test public void pendingDoesNotCountAsEvaluated() {
        ProgressSummary p = ProgressSummary.from(Arrays.asList(
                item("a", TrackingStatus.COMPLETED),
                item("b", TrackingStatus.SKIPPED),
                item("c", TrackingStatus.PENDING)));
        assertEquals(3, p.total());
        assertEquals(1, p.completed());
        assertEquals(1, p.skipped());
        assertEquals(1, p.pending());
        assertEquals(2, p.evaluated());
        assertEquals(50, p.completionPercent());
    }

    @Test public void roundsPercentage() {
        ProgressSummary p = ProgressSummary.from(Arrays.asList(
                item("a", TrackingStatus.COMPLETED),
                item("b", TrackingStatus.COMPLETED),
                item("c", TrackingStatus.SKIPPED)));
        assertEquals(67, p.completionPercent());
    }

    @Test public void forbidsSavingPendingRecord() {
        try {
            new TrackingRecord(new EntityId("a"), TrackingStatus.PENDING, "", 1);
            fail("Expected invalid persisted status");
        } catch (IllegalArgumentException expected) { }
    }

    @Test public void validatesNoteLength() {
        try {
            new TrackingRecord(new EntityId("a"), TrackingStatus.SKIPPED, "x".repeat(501), 1);
            fail("Expected invalid note");
        } catch (IllegalArgumentException expected) { }
    }
}
