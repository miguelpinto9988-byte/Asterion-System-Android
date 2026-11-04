package pe.asterionsystem.core.data;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import pe.asterionsystem.core.domain.EntityId;

/**
 * Serializes storage requests on one worker thread.
 * Future continuations do NOT automatically run on Android's UI thread.
 * Close the gateway with its owning lifecycle scope.
 */
public final class BackgroundRecordOperations implements AutoCloseable {
    private static final AtomicInteger NEXT_THREAD = new AtomicInteger();
    private final TechnicalRecordRepository repository;
    private final ExecutorService executor;

    public BackgroundRecordOperations(TechnicalRecordRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
        ThreadFactory factory = task -> {
            Thread worker = new Thread(task, "asterion-records-" + NEXT_THREAD.incrementAndGet());
            worker.setDaemon(true);
            return worker;
        };
        this.executor = Executors.newSingleThreadExecutor(factory);
    }

    private <T> CompletableFuture<T> execute(Supplier<T> action) {
        return CompletableFuture.supplyAsync(action, executor);
    }

    public CompletableFuture<Void> create(TechnicalRecord record) {
        Objects.requireNonNull(record, "record");
        return execute(() -> {
            repository.create(record);
            return null;
        });
    }

    public CompletableFuture<Optional<TechnicalRecord>> find(EntityId id) {
        Objects.requireNonNull(id, "id");
        return execute(() -> repository.findById(id));
    }

    public CompletableFuture<Boolean> update(TechnicalRecord record) {
        Objects.requireNonNull(record, "record");
        return execute(() -> repository.update(record));
    }

    public CompletableFuture<Boolean> delete(EntityId id) {
        Objects.requireNonNull(id, "id");
        return execute(() -> repository.delete(id));
    }

    public CompletableFuture<List<TechnicalRecord>> list(int limit, int offset) {
        return execute(() -> repository.list(limit, offset));
    }

    public CompletableFuture<Integer> count() {
        return execute(repository::count);
    }

    @Override public void close() {
        executor.shutdown();
    }
}
