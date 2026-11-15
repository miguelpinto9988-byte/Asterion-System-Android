package pe.asterionsystem.portable;

import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Properties;
import org.junit.Test;
import pe.asterionsystem.core.engine.progression.VolumeProgressionEngine;

public final class PortableTrainingModuleTest {
    @Test public void corpusAndInitialSettingsAreComplete() throws Exception {
        PortableTrainingModule kit=PortableTrainingModule.loadDefaults();
        assertEquals(5,kit.categories().size());
        assertEquals(5,kit.settings().dailyActivities);
        assertEquals(5,PortableTrainingSettings.defaults().dailyActivities);
        assertEquals(155,kit.catalog().families().size());
        assertEquals(474,kit.catalog().types().size());
        assertEquals(1257,kit.catalog().variants().size());
        assertEquals(1257,kit.profiles().size());
        assertEquals("tren_superior",kit.categoryForVariant("curl_biceps_v01"));
        assertFalse(kit.settings().volumeSuggestionsEnabled);
        assertFalse(kit.review("curl_biceps_v01",LocalDate.of(2026,11,12),
                Collections.emptyList(),false).isPresent());
        assertTrue(kit.initialReference("curl_biceps_v01").initialValue>0);
    }

    @Test public void optionalReferenceNeedsExplicitActivation() throws Exception {
        Properties p=new Properties();
        p.setProperty("sugerencias_volumen","true");
        PortableTrainingModule kit=PortableTrainingModule.load(
                PortableTrainingSettings.from(p));
        VolumeProgressionEngine.Suggestion suggestion=kit.review("curl_biceps_v01",
                LocalDate.of(2026,11,12),Collections.emptyList(),false)
                .orElseThrow();
        assertEquals(VolumeProgressionEngine.Status.NO_HISTORY,suggestion.status);
    }

    @Test public void rejectsUnknownCategoriesAndSettings() {
        Properties p=new Properties();
        p.setProperty("categorias_habilitadas","cardio,inexistente");
        assertThrows(IllegalArgumentException.class,
                ()->PortableTrainingSettings.from(p));
        p.clear();
        p.setProperty("sugerencias_volumen","yes");
        assertThrows(IllegalArgumentException.class,
                ()->PortableTrainingSettings.from(p));
    }
}
