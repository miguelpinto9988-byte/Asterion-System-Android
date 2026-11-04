package pe.asterionsystem.core.settings;

import static org.junit.Assert.*;
import org.junit.Test;

public final class PreferencesServiceTest {
    private static final class MemoryRepository implements PreferencesRepository {
        AppPreferences saved = AppPreferences.defaults();
        int writes;
        int resets;
        @Override public AppPreferences read() { return saved; }
        @Override public void save(AppPreferences preferences) {
            saved = preferences;
            writes++;
        }
        @Override public void reset() { saved = AppPreferences.defaults(); resets++; }
    }

    @Test public void defaultConfigurationIsSafe() {
        AppPreferences values = AppPreferences.defaults();
        assertEquals(AppPreferences.TextSize.NORMAL, values.textSize());
        assertEquals(AppPreferences.ListDensity.NORMAL, values.listDensity());
        assertEquals(AppPreferences.StartSection.HOME, values.startSection());
    }

    @Test public void rejectsUnknownStoredValuesWithDefaults() {
        assertEquals(AppPreferences.TextSize.NORMAL,
                AppPreferences.TextSize.fromStored("huge"));
        assertEquals(AppPreferences.ListDensity.NORMAL,
                AppPreferences.ListDensity.fromStored("dense-aggressive"));
        assertEquals(AppPreferences.StartSection.HOME,
                AppPreferences.StartSection.fromStored("detail"));
        assertEquals(AppPreferences.StartSection.PLAN,
                AppPreferences.StartSection.fromStored("plan"));
    }

    @Test public void valueObjectIsImmutable() {
        AppPreferences original = AppPreferences.defaults();
        AppPreferences changed = original.withTextSize(AppPreferences.TextSize.LARGE)
                .withListDensity(AppPreferences.ListDensity.COMPACT)
                .withStartSection(AppPreferences.StartSection.CATALOG);
        assertEquals(AppPreferences.TextSize.NORMAL, original.textSize());
        assertEquals(AppPreferences.TextSize.LARGE, changed.textSize());
        assertEquals(AppPreferences.ListDensity.COMPACT, changed.listDensity());
        assertEquals(AppPreferences.StartSection.CATALOG, changed.startSection());
        assertNotEquals(original, changed);
    }

    @Test public void serviceDoesNotWriteUnchangedValues() {
        MemoryRepository storage = new MemoryRepository();
        PreferencesService service = new PreferencesService(storage);
        assertFalse(service.update(AppPreferences.defaults()));
        assertEquals(0, storage.writes);
        assertTrue(service.update(AppPreferences.defaults()
                .withTextSize(AppPreferences.TextSize.LARGE)));
        assertEquals(1, storage.writes);
        assertFalse(service.update(service.current()));
        assertEquals(1, storage.writes);
    }

    @Test public void resetOnlyResetsPreferencesRepository() {
        MemoryRepository storage = new MemoryRepository();
        PreferencesService service = new PreferencesService(storage);
        service.update(service.current().withStartSection(AppPreferences.StartSection.PROGRESS));
        service.reset();
        assertEquals(AppPreferences.defaults(), service.current());
        assertEquals(1, storage.resets);
    }

    @Test public void nullPreferencesAreRejected() {
        try {
            new AppPreferences(null, AppPreferences.ListDensity.NORMAL,
                    AppPreferences.StartSection.HOME);
            fail("expected null rejected");
        } catch (NullPointerException expected) { }
    }
}
