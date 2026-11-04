package pe.asterionsystem.core.planning;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import pe.asterionsystem.core.domain.EntityId;

/** Synchronous port, backed by SQLite only in :app. Call off Android's UI thread. */
public interface ScheduleRepository {
    Optional<ScheduledActivity> find(EntityId id);
    List<ScheduledActivity> forDate(LocalDate date);
    void insert(ScheduledActivity entry);
    boolean update(ScheduledActivity entry);
    boolean delete(EntityId id);
}
