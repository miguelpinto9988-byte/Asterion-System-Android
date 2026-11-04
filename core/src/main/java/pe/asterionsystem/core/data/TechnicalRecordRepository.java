package pe.asterionsystem.core.data;

import java.util.List;
import pe.asterionsystem.core.domain.EntityId;

/**
 * Contract only: no Android, SQL or business rules in the core module.
 * Implementations must not access SQLite from the Android main thread.
 */
public interface TechnicalRecordRepository extends RecordReader<EntityId, TechnicalRecord> {
    /** Inserts a new ID. Raises StorageException when the operation fails. */
    void create(TechnicalRecord record);

    /** Replaces editable columns; false when the ID/timestamp pair is not found. */
    boolean update(TechnicalRecord record);

    /** Deletes one ID; false when already absent. */
    boolean delete(EntityId id);

    /** Most recently updated first. limit 1..100 and offset >= 0. */
    List<TechnicalRecord> list(int limit, int offset);

    int count();
}
