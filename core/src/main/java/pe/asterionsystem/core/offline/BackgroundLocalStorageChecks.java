package pe.asterionsystem.core.offline;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Serial, user-initiated background checks; does not monitor network state. */
public final class BackgroundLocalStorageChecks implements AutoCloseable {
    private final LocalStorageService service;
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "asterion-offline-check");
        thread.setDaemon(true);
        return thread;
    });

    public BackgroundLocalStorageChecks(LocalStorageService service) {
        this.service = Objects.requireNonNull(service, "service");
    }

    public CompletableFuture<LocalStorageReport> inspect() {
        return CompletableFuture.supplyAsync(service::inspect, worker);
    }

    public void closeAfter(Runnable cleanup) {
        if (cleanup != null) worker.execute(cleanup);
        worker.shutdown();
    }

    @Override public void close() { closeAfter(null); }
}
