package pe.asterionsystem.core.planning;

import java.time.LocalDate;
import java.util.Objects;
import pe.asterionsystem.core.domain.EntityId;

/** Immutable, local-wall-time schedule: single date, no overnight or recurrence. */
public final class ScheduledActivity {
    private final EntityId id;
    private final EntityId activityId;
    private final LocalDate date;
    private final int startMinute;
    private final int durationMinutes;
    private final long createdAtMs;
    private final long updatedAtMs;

    public ScheduledActivity(EntityId id, EntityId activityId, LocalDate date,
                             int startMinute, int durationMinutes, long createdAtMs, long updatedAtMs) {
        this.id = Objects.requireNonNull(id, "id");
        this.activityId = Objects.requireNonNull(activityId, "activityId");
        this.date = Objects.requireNonNull(date, "date");
        if (id.value().length() > 64 || activityId.value().length() > 64) {
            throw new IllegalArgumentException("ID de programación demasiado largo");
        }
        if (date.getYear() < 2000 || date.getYear() > 2100) {
            throw new IllegalArgumentException("Fecha fuera del intervalo permitido");
        }
        if (startMinute < 0 || startMinute >= 1440 || durationMinutes < 1
                || durationMinutes > 720 || startMinute + durationMinutes > 1440) {
            throw new IllegalArgumentException("Horario inválido o cruza la medianoche");
        }
        if (createdAtMs < 0 || updatedAtMs < createdAtMs) {
            throw new IllegalArgumentException("Tiempos de auditoría inválidos");
        }
        this.startMinute = startMinute;
        this.durationMinutes = durationMinutes;
        this.createdAtMs = createdAtMs;
        this.updatedAtMs = updatedAtMs;
    }

    public EntityId id() { return id; }
    public EntityId activityId() { return activityId; }
    public LocalDate date() { return date; }
    public int startMinute() { return startMinute; }
    public int durationMinutes() { return durationMinutes; }
    public int endMinute() { return startMinute + durationMinutes; }
    public long createdAtMs() { return createdAtMs; }
    public long updatedAtMs() { return updatedAtMs; }

    public boolean overlaps(ScheduledActivity other) {
        Objects.requireNonNull(other, "other");
        return date.equals(other.date) && startMinute < other.endMinute()
                && other.startMinute < endMinute();
    }

    /** Retains ID and creation time when modifying an existing entry. */
    public ScheduledActivity reschedule(EntityId catalogActivity, LocalDate onDate, int start,
                                        int duration, long updated) {
        return new ScheduledActivity(id, catalogActivity, onDate, start, duration, createdAtMs, updated);
    }
}
