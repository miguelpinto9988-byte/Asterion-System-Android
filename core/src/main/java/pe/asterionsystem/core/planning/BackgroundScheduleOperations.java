package pe.asterionsystem.core.planning;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import pe.asterionsystem.core.catalog.CatalogItem;
import pe.asterionsystem.core.domain.EntityId;

/** Single-worker gateway. Callbacks must explicitly marshal to Android's UI thread. */
public final class BackgroundScheduleOperations implements AutoCloseable {
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "asterion-planning-worker");
        t.setDaemon(true);
        return t;
    });
    private final SchedulePlanner planner;

    public BackgroundScheduleOperations(SchedulePlanner planner) {
        this.planner = Objects.requireNonNull(planner, "planner");
    }

    private <T> CompletableFuture<T> run(Supplier<T> action) {
        return CompletableFuture.supplyAsync(action, worker);
    }

    public CompletableFuture<List<CatalogItem>> activities() {
        return run(planner::activities);
    }

    public CompletableFuture<List<ScheduledActivity>> forDate(LocalDate day) {
        return run(() -> planner.forDate(day));
    }

    public CompletableFuture<List<ScheduleListing>> listWithTitles(LocalDate day) {
        return run(() -> planner.listWithTitles(day));
    }

    public CompletableFuture<Optional<ScheduledActivity>> find(EntityId id) {
        return run(() -> planner.find(id));
    }

    public CompletableFuture<String> titleFor(EntityId id) {
        return run(() -> planner.titleFor(id));
    }

    public CompletableFuture<Void> create(ScheduledActivity entry) {
        return run(() -> { planner.create(entry); return null; });
    }

    public CompletableFuture<Boolean> update(ScheduledActivity entry) {
        return run(() -> planner.update(entry));
    }

    public CompletableFuture<Boolean> delete(EntityId id) {
        return run(() -> planner.delete(id));
    }

    public void closeAfter(Runnable onWorkerExit) {
        if (onWorkerExit != null) {
            worker.execute(onWorkerExit);
        }
        worker.shutdown();
    }

    @Override public void close() { closeAfter(null); }
}
