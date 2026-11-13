package pe.asterionsystem.app;

import static org.junit.Assert.*;
import org.junit.Test;

public final class RoutineDurationPreferenceTest {
    @Test public void allowsOnlyExistingRoutineDurations() {
        assertArrayEquals(new int[]{10,15,20,30,45,60},
                RoutineDurationPreference.options());
        for(int value:RoutineDurationPreference.options()) {
            assertTrue(RoutineDurationPreference.isSupported(value));
            assertEquals(value,RoutineDurationPreference.parse(
                    String.valueOf(value)));
            assertEquals(value,RoutineDurationPreference.options()[
                    RoutineDurationPreference.indexOf(value)]);
        }
    }

    @Test public void missingOrInvalidSavedDurationDefaultsToThirty() {
        assertEquals(30,RoutineDurationPreference.parse(null));
        assertEquals(30,RoutineDurationPreference.parse(""));
        assertEquals(30,RoutineDurationPreference.parse("20m"));
        assertEquals(30,RoutineDurationPreference.parse("90"));
        assertEquals(30,RoutineDurationPreference.parse("-15"));
        assertEquals(45,RoutineDurationPreference.parse(" 45 "));
    }

    @Test public void profileAndRoutineSelectorsUseMatchingOptions() {
        String[] raw=RoutineDurationPreference.optionStrings();
        String[] labels=RoutineDurationPreference.displayOptions();
        assertEquals(6,raw.length);
        assertEquals(raw.length,labels.length);
        assertEquals("30",raw[3]);
        assertEquals("30 minutos",labels[3]);
        assertEquals(3,RoutineDurationPreference.indexOf(
                RoutineDurationPreference.parse(null)));
    }
}
