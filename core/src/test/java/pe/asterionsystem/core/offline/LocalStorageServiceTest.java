package pe.asterionsystem.core.offline;

import static org.junit.Assert.*;
import org.junit.Test;

public final class LocalStorageServiceTest {
    @Test public void reportsHealthWhenBothChecksPass() {
        LocalStorageReport report = new LocalStorageReport(true, true, 4, 3, 2);
        assertEquals(LocalStorageReport.State.HEALTHY, report.state());
        assertEquals(4L, report.catalogCount());
        assertEquals(3L, report.scheduledCount());
        assertEquals(2L, report.outcomesCount());
    }

    @Test public void warnsWhenForeignKeysFailOrIntegrityFails() {
        assertEquals(LocalStorageReport.State.ATTENTION,
                new LocalStorageReport(true, false, 3, 2, 1).state());
        assertEquals(LocalStorageReport.State.ATTENTION,
                new LocalStorageReport(false, true, 3, 2, 1).state());
    }

    @Test public void warnsIfOutcomeCountExceedsScheduledCount() {
        LocalStorageReport report = new LocalStorageReport(true, true, 3, 1, 2);
        assertEquals(LocalStorageReport.State.ATTENTION, report.state());
        assertFalse(report.foreignKeysPassed());
    }

    @Test public void rejectsNegativeCounts() {
        try {
            new LocalStorageReport(true, true, 0, -1, 0);
            fail("negative counts must be rejected");
        } catch (IllegalArgumentException expected) { }
    }

    @Test public void forwardsInspectionWithoutChangingItsData() {
        LocalStorageReport expected = new LocalStorageReport(true, true, 4, 0, 0);
        LocalStorageService service = new LocalStorageService(() -> expected);
        assertSame(expected, service.inspect());
        assertSame(expected, service.inspect());
    }
}
