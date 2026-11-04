package pe.asterionsystem.core.progress;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import pe.asterionsystem.core.domain.EntityId;

/** Serial worker for database I/O. UI callbacks must post to Android main thread. */
public final class BackgroundProgressOperations implements AutoCloseable {
    private final ProgressService service;
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "asterion-progress-worker");
        thread.setDaemon(true);
        return thread;
    });

    public BackgroundProgressOperations(ProgressService service) {
        this.service = Objects.requireNonNull(service, "service");
    }

    private <T> CompletableFuture<T> work(Supplier<T> body) {
        return CompletableFuture.supplyAsync(body, worker);
    }

    public CompletableFuture<List<ProgressEntry>> forDate(LocalDate day) {
        return work(() -> service.forDate(day));
    }

    public CompletableFuture<ProgressSummary> summaryForWeek(LocalDate monday) {
        return work(() -> service.summaryForWeek(monday));
    }

    public CompletableFuture<Void> mark(EntityId id, TrackingStatus status, String note, long timeMs) {
        return work(() -> { service.mark(id, status, note, timeMs); return null; });
    }

    public void closeAfter(Runnable finishOnWorker) {
        if (finishOnWorker != null) worker.execute(finishOnWorker);
        worker.shutdown();
    }

    @Override public void close() { closeAfter(null); }
}
