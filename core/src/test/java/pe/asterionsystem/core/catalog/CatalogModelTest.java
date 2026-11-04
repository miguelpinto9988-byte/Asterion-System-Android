package pe.asterionsystem.core.catalog;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import pe.asterionsystem.core.domain.EntityId;

public final class CatalogModelTest {
    private static CatalogCategory category() {
        return new CatalogCategory(new EntityId("general"), " General ", 0);
    }

    @Test public void kindsHaveStableStorageValues() {
        assertEquals(CatalogItemKind.CONTENT, CatalogItemKind.fromDatabaseValue("content"));
        assertEquals(CatalogItemKind.ACTIVITY, CatalogItemKind.fromDatabaseValue("activity"));
        assertInvalid(() -> CatalogItemKind.fromDatabaseValue("unknown"));
    }

    @Test public void itemIsImmutableAndTrimsVisibleTexts() {
        ArrayList<String> tags = new ArrayList<>(Arrays.asList(" Demo ", "Lectura"));
        CatalogItem item = new CatalogItem(new EntityId("item"), " Título ", " Texto ",
                CatalogItemKind.CONTENT, category(), tags);
        tags.clear();
        assertEquals("Título", item.title());
        assertEquals("Texto", item.description());
        assertEquals("General", item.category().name());
        assertEquals(Arrays.asList("Demo", "Lectura"), item.tags());
        try {
            item.tags().add("Otra");
            fail("Tags must be immutable");
        } catch (UnsupportedOperationException expected) {
            // Defensive copy.
        }
    }

    @Test public void rejectsInvalidFieldsAndDuplicateTags() {
        assertInvalid(() -> new CatalogCategory(new EntityId("x"), "", 0));
        assertInvalid(() -> new CatalogCategory(new EntityId("x"), "ok", -1));
        assertInvalid(() -> new CatalogItem(new EntityId("x"), "", "", CatalogItemKind.CONTENT,
                category(), Collections.emptyList()));
        assertInvalid(() -> new CatalogItem(new EntityId("x"), "ok", "z".repeat(2001),
                CatalogItemKind.ACTIVITY, category(), Collections.emptyList()));
        assertInvalid(() -> new CatalogItem(new EntityId("x"), "ok", "", CatalogItemKind.CONTENT,
                category(), Arrays.asList("duplicada", "duplicada")));
    }

    @Test public void queryRequiresBoundedPagingAndLiteralText() {
        CatalogQuery query = new CatalogQuery(" búsqueda ", new EntityId("general"),
                CatalogItemKind.CONTENT, 25, 0);
        assertEquals("búsqueda", query.text());
        assertEquals(25, query.limit());
        assertEquals(CatalogItemKind.CONTENT, query.kind());
        assertInvalid(() -> new CatalogQuery("", null, null, 0, 0));
        assertInvalid(() -> new CatalogQuery("", null, null, 51, 0));
        assertInvalid(() -> new CatalogQuery("", null, null, 50, -1));
        assertInvalid(() -> new CatalogQuery("x".repeat(121), null, null, 25, 0));
    }

    private static void assertInvalid(Runnable assertion) {
        try {
            assertion.run();
            fail("An invalid model was accepted");
        } catch (IllegalArgumentException expected) {
            // Expected.
        }
    }
}
