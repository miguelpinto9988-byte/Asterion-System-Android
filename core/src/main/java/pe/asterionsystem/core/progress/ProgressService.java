package pe.asterionsystem.core.progress;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import pe.asterionsystem.core.domain.EntityId;

/** Explicit tracking only; no automated completion, rewards, reminders or execution. */
public final class ProgressService {
    private final ProgressRepository repository;

    public ProgressService(ProgressRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public List<ProgressEntry> forDate(LocalDate date) {
        return repository.forDate(Objects.requireNonNull(date, "date"));
    }

    public ProgressSummary summaryForWeek(LocalDate monday) {
        Objects.requireNonNull(monday, "monday");
        if (monday.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new IllegalArgumentException("La fecha debe ser lunes");
        }
        return ProgressSummary.from(repository.forWeek(monday));
    }

    public void mark(EntityId planId, TrackingStatus outcome, String note, long recordedAtMs) {
        Objects.requireNonNull(planId, "planId");
        if (outcome == TrackingStatus.PENDING) {
            if (note != null && !note.trim().isEmpty()) {
                throw new IllegalArgumentException("Pendiente no admite observación guardada");
            }
            repository.clear(planId);
        } else {
            repository.save(new TrackingRecord(planId, outcome, note, recordedAtMs));
        }
    }
}
