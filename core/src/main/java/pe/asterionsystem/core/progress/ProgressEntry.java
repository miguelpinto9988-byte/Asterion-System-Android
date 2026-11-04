package pe.asterionsystem.core.progress;

import java.util.Objects;
import pe.asterionsystem.core.planning.ScheduledActivity;

/** Scheduled occurrence enriched with current catalog title and optional outcome. */
public final class ProgressEntry {
    private final ScheduledActivity schedule;
    private final String title;
    private final TrackingStatus status;
    private final String note;

    public ProgressEntry(ScheduledActivity schedule, String title, TrackingStatus status, String note) {
        this.schedule = Objects.requireNonNull(schedule, "schedule");
        this.title = Objects.requireNonNull(title, "title");
        if (title.trim().isEmpty()) throw new IllegalArgumentException("Título vacío");
        this.status = Objects.requireNonNull(status, "status");
        this.note = Objects.requireNonNull(note, "note");
        if (note.length() > TrackingRecord.MAX_NOTE_LENGTH || (status == TrackingStatus.PENDING && !note.isEmpty())) {
            throw new IllegalArgumentException("Observación de seguimiento inválida");
        }
    }
    public ScheduledActivity schedule() { return schedule; }
    public String title() { return title; }
    public TrackingStatus status() { return status; }
    public String note() { return note; }
}
