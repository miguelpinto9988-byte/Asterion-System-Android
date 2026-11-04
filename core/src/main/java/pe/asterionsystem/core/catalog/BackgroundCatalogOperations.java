package pe.asterionsystem.core.catalog;

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

/** Single background worker for catalog reads, independent of Android. */
public final class BackgroundCatalogOperations implements AutoCloseable {
    private static final AtomicInteger COUNTER = new AtomicInteger();
    private final CatalogRepository repository;
    private final ExecutorService worker;

    public BackgroundCatalogOperations(CatalogRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
        ThreadFactory factory = runnable -> {
            Thread thread = new Thread(runnable, "asterion-catalog-" + COUNTER.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
        worker = Executors.newSingleThreadExecutor(factory);
    }

    private <T> CompletableFuture<T> schedule(Supplier<T> task) {
        return CompletableFuture.supplyAsync(task, worker);
    }

    public CompletableFuture<List<CatalogCategory>> categories() {
        return schedule(repository::listCategories);
    }

    public CompletableFuture<List<CatalogItem>> search(CatalogQuery query) {
        Objects.requireNonNull(query, "query");
        return schedule(() -> repository.search(query));
    }

    public CompletableFuture<Optional<CatalogItem>> findById(EntityId id) {
        Objects.requireNonNull(id, "id");
        return schedule(() -> repository.findById(id));
    }

    /** Enqueue resource cleanup behind all pending reads, then shut the worker down. */
    public void closeAfter(Runnable cleanup) {
        worker.execute(Objects.requireNonNull(cleanup, "cleanup"));
        worker.shutdown();
    }

    @Override public void close() {
        worker.shutdown();
    }
}
