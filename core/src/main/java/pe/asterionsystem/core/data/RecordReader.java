package pe.asterionsystem.core.data;

import java.util.Optional;

/** Read-side contract; schema, storage, transactions and writes belong to phase 05. */
@FunctionalInterface
public interface RecordReader<ID, T> {
    Optional<T> findById(ID id);
}
