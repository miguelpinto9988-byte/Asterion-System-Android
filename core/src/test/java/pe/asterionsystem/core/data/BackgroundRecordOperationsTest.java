package pe.asterionsystem.core.data;

import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import pe.asterionsystem.core.domain.EntityId;

public final class BackgroundRecordOperationsTest {
    @Test public void runsSeriallyAwayFromCallingThread() throws Exception {
        Thread caller = Thread.currentThread();
        ArrayList<Thread> calls = new ArrayList<>();
        TechnicalRecordRepository repository = new TechnicalRecordRepository() {
            @Override public void create(TechnicalRecord record) { calls.add(Thread.currentThread()); }
            @Override public Optional<TechnicalRecord> findById(EntityId id) { return Optional.empty(); }
            @Override public boolean update(TechnicalRecord record) { return false; }
            @Override public boolean delete(EntityId id) { return false; }
            @Override public List<TechnicalRecord> list(int limit, int offset) { return Collections.emptyList(); }
            @Override public int count() { return 0; }
        };
        TechnicalRecord item = new TechnicalRecord(new EntityId("test"), "Prueba", "", 1, 1);
        try (BackgroundRecordOperations worker = new BackgroundRecordOperations(repository)) {
            worker.create(item).get(3, TimeUnit.SECONDS);
            worker.create(item).get(3, TimeUnit.SECONDS);
            assertEquals(2, calls.size());
            assertNotSame(caller, calls.get(0));
            assertSame(calls.get(0), calls.get(1));
        }
    }
}
