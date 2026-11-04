package pe.asterionsystem.core.progress;

/**
 * Pending has no persisted tracking row. Only completed/skipped are explicit results.
 */
public enum TrackingStatus {
    PENDING("pending"), COMPLETED("completed"), SKIPPED("skipped");

    private final String value;
    TrackingStatus(String value) { this.value = value; }
    public String value() { return value; }

    public static TrackingStatus fromStored(String raw) {
        if ("completed".equals(raw)) return COMPLETED;
        if ("skipped".equals(raw)) return SKIPPED;
        throw new IllegalArgumentException("Estado de seguimiento desconocido: " + raw);
    }
}
