package pe.asterionsystem.core.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public final class EntityIdTest {
    @Test
    public void normalizesAndComparesValues() {
        EntityId first = new EntityId("  item-1 ");
        EntityId second = new EntityId("item-1");
        assertEquals("item-1", first.value());
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void rejectsEmptyIdentifier() {
        assertThrows(IllegalArgumentException.class, () -> new EntityId("  "));
        assertThrows(IllegalArgumentException.class, () -> new EntityId(null));
    }
}
