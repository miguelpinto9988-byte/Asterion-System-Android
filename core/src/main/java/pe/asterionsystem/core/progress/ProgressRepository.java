package pe.asterionsystem.core.progress;

import java.time.LocalDate;
import java.util.List;
import pe.asterionsystem.core.domain.EntityId;

/** Storage boundary. Date/week reads join planning and tracking without modifying a plan. */
public interface ProgressRepository {
    List<ProgressEntry> forDate(LocalDate date);
    /** Inclusive Monday, exclusive following Monday. */
    List<ProgressEntry> forWeek(LocalDate monday);
    /** Atomic insert or update; the referenced plan must exist. */
    void save(TrackingRecord record);
    /** Removing the explicit outcome restores implicit PENDING. */
    boolean clear(EntityId scheduleId);
}
