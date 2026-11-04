package pe.asterionsystem.core.data;

import static org.junit.Assert.*;
import org.junit.Test;
import pe.asterionsystem.core.domain.EntityId;

public final class TechnicalRecordTest {
    @Test public void trimsFieldsAndPreservesIdentityWhenEdited() {
        TechnicalRecord original = new TechnicalRecord(new EntityId(" demo "), " Título ",
                " Nota ", 10L, 11L);
        assertEquals("demo", original.id().value());
        assertEquals("Título", original.title());
        assertEquals("Nota", original.note());
        TechnicalRecord edited = original.withContent("Nuevo", "", 15L);
        assertEquals(original.id(), edited.id());
        assertEquals(original.createdAtMs(), edited.createdAtMs());
        assertEquals("Nuevo", edited.title());
    }

    @Test public void rejectsInvalidTitlesAndDates() {
        assertInvalid(() -> new TechnicalRecord(new EntityId("x"), " ", "", 0, 0));
        assertInvalid(() -> new TechnicalRecord(new EntityId("x"), "ok", "", -1, 0));
        assertInvalid(() -> new TechnicalRecord(new EntityId("x"), "ok", "", 10, 9));
        assertInvalid(() -> new TechnicalRecord(new EntityId("x"), "a".repeat(121), "", 0, 0));
        assertInvalid(() -> new TechnicalRecord(new EntityId("x"), "ok", "z".repeat(1001), 0, 0));
        assertInvalid(() -> new TechnicalRecord(new EntityId("x".repeat(65)), "ok", "", 0, 0));
    }

    private static void assertInvalid(Runnable block) {
        try {
            block.run();
            fail("Expected invalid record");
        } catch (IllegalArgumentException expected) {
            // Rejected by domain validation.
        }
    }
}
