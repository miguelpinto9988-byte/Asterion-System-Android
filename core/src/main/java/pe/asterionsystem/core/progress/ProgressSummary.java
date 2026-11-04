package pe.asterionsystem.core.progress;

import java.util.List;
import java.util.Objects;

/**
 * Evaluated = completed + skipped. Pending remains outside completion denominator,
 * regardless of its scheduled date. Zero evaluated => no rate, not 0% progress.
 */
public final class ProgressSummary {
    private final int total;
    private final int completed;
    private final int skipped;
    private final int pending;

    private ProgressSummary(int total, int completed, int skipped, int pending) {
        this.total = total;
        this.completed = completed;
        this.skipped = skipped;
        this.pending = pending;
    }

    public static ProgressSummary from(List<ProgressEntry> entries) {
        Objects.requireNonNull(entries, "entries");
        int done = 0, missed = 0, waiting = 0;
        for (ProgressEntry entry : entries) {
            switch (Objects.requireNonNull(entry, "entry").status()) {
                case COMPLETED: done++; break;
                case SKIPPED: missed++; break;
                case PENDING: waiting++; break;
                default: throw new IllegalStateException("Estado no soportado");
            }
        }
        return new ProgressSummary(entries.size(), done, missed, waiting);
    }

    public int total() { return total; }
    public int completed() { return completed; }
    public int skipped() { return skipped; }
    public int pending() { return pending; }
    public int evaluated() { return completed + skipped; }
    public boolean hasEvaluated() { return evaluated() > 0; }
    /** Rounded integer percent among explicitly evaluated activities. */
    public int completionPercent() {
        return hasEvaluated() ? (completed * 100 + evaluated() / 2) / evaluated() : 0;
    }
}
