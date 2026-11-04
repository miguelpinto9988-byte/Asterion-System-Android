package pe.asterionsystem.core.catalog;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import pe.asterionsystem.core.domain.EntityId;

public final class BackgroundCatalogOperationsTest {
    @Test public void queriesRunSeriallyOnBackgroundThread() throws Exception {
        final Thread callingThread = Thread.currentThread();
        final List<Thread> threads = new ArrayList<>();
        CatalogRepository fake = new CatalogRepository() {
            @Override public List<CatalogCategory> listCategories() {
                threads.add(Thread.currentThread());
                return Collections.emptyList();
            }
            @Override public List<CatalogItem> search(CatalogQuery query) {
                threads.add(Thread.currentThread());
                return Collections.emptyList();
            }
            @Override public Optional<CatalogItem> findById(EntityId id) {
                threads.add(Thread.currentThread());
                return Optional.empty();
            }
        };
        try (BackgroundCatalogOperations background = new BackgroundCatalogOperations(fake)) {
            background.categories().get(3, TimeUnit.SECONDS);
            background.search(CatalogQuery.initial()).get(3, TimeUnit.SECONDS);
            background.findById(new EntityId("demo")).get(3, TimeUnit.SECONDS);
        }
        assertEquals(3, threads.size());
        assertNotSame(callingThread, threads.get(0));
        assertSame(threads.get(0), threads.get(1));
        assertSame(threads.get(1), threads.get(2));
    }
}
