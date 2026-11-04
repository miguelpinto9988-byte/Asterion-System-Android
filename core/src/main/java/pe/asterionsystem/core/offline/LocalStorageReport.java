package pe.asterionsystem.core.offline;

/**
 * Read-only diagnostics for local SQLite. No network tests or automatic repair.
 * Counts are snapshots, not a transactional business report.
 */
public final class LocalStorageReport {
    public enum State { HEALTHY, ATTENTION }

    private final boolean quickCheckPassed;
    private final boolean foreignKeysPassed;
    private final long catalogCount;
    private final long scheduledCount;
    private final long outcomesCount;

    public LocalStorageReport(boolean quickCheckPassed, boolean foreignKeysPassed,
                              long catalogCount, long scheduledCount, long outcomesCount) {
        if (catalogCount < 0 || scheduledCount < 0 || outcomesCount < 0) {
            throw new IllegalArgumentException("Los contadores locales no pueden ser negativos");
        }
        if (outcomesCount > scheduledCount) {
            // A foreign-key violation or concurrent snapshot may require a manual recheck.
            foreignKeysPassed = false;
        }
        this.quickCheckPassed = quickCheckPassed;
        this.foreignKeysPassed = foreignKeysPassed;
        this.catalogCount = catalogCount;
        this.scheduledCount = scheduledCount;
        this.outcomesCount = outcomesCount;
    }

    public State state() {
        return quickCheckPassed && foreignKeysPassed ? State.HEALTHY : State.ATTENTION;
    }
    public boolean quickCheckPassed() { return quickCheckPassed; }
    public boolean foreignKeysPassed() { return foreignKeysPassed; }
    public long catalogCount() { return catalogCount; }
    public long scheduledCount() { return scheduledCount; }
    public long outcomesCount() { return outcomesCount; }
}
