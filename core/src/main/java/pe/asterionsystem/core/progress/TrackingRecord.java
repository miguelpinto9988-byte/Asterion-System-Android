package pe.asterionsystem.core.progress;

import java.util.Objects;
import pe.asterionsystem.core.domain.EntityId;

/** One explicit outcome for one scheduled activity. Pending is represented by no row. */
public final class TrackingRecord {
    public static final int MAX_NOTE_LENGTH = 500;
    private final EntityId scheduleId;
    private final TrackingStatus status;
    private final String note;
    private final long recordedAtMs;

    public TrackingRecord(EntityId scheduleId, TrackingStatus status, String note, long recordedAtMs) {
        this.scheduleId = Objects.requireNonNull(scheduleId, "scheduleId");
        this.status = Objects.requireNonNull(status, "status");
        if (status == TrackingStatus.PENDING || scheduleId.value().length() > 64) {
            throw new IllegalArgumentException("Estado o identificador inválido para guardar");
        }
        String cleaned = Objects.requireNonNull(note, "note").trim();
        if (cleaned.length() > MAX_NOTE_LENGTH || recordedAtMs < 0) {
            throw new IllegalArgumentException("Nota o fecha de registro inválidas");
        }
        this.note = cleaned;
        this.recordedAtMs = recordedAtMs;
    }
    public EntityId scheduleId() { return scheduleId; }
    public TrackingStatus status() { return status; }
    public String note() { return note; }
    public long recordedAtMs() { return recordedAtMs; }
}
