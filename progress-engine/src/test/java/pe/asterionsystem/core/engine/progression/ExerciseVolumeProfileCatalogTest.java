package pe.asterionsystem.core.engine.progression;

import static org.junit.Assert.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.Before;
import org.junit.Test;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalogLoader;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;

public final class ExerciseVolumeProfileCatalogTest {
    private ExerciseCatalog catalog;
    private ExerciseVolumeProfileCatalog profiles;

    private InputStreamReader input(String name) {
        return new InputStreamReader(getClass().getResourceAsStream(
                "/asterion_exercises/v1/"+name),StandardCharsets.UTF_8);
    }

    @Before public void loadActualCatalog() throws Exception {
        try(InputStreamReader families=input("families.tsv");
            InputStreamReader types=input("types.tsv");
            InputStreamReader variants=input("variants.tsv");
            InputStreamReader volume=input("volume_profiles.tsv")) {
            catalog=ExerciseCatalogLoader.load(families,types,variants);
            profiles=ExerciseVolumeProfileCatalog.load(volume,catalog);
        }
    }

    private ExerciseVolumeProfileCatalog.Profile get(String id) {
        return profiles.require(catalog.variant(id).orElseThrow());
    }

    @Test public void all1257RealVariantsHaveExactlyOneValidatedReference() {
        assertEquals(1257,catalog.variants().size());
        assertEquals(catalog.variants().size(),profiles.size());
        for(ExerciseVariant exercise:catalog.variants()) {
            ExerciseVolumeProfileCatalog.Profile p=profiles.require(exercise);
            assertEquals(exercise.id(),p.variantId);
            assertTrue(p.initialSets>=1 && p.initialSets<=3);
            assertTrue(p.initialValue>=p.minimumValue);
            assertTrue(p.initialValue<=p.maximumValue);
        }
    }

    @Test public void squatsAreCountedByRepetitions() {
        assertEquals(ExerciseVolumeProfileCatalog.Unit.REPETITIONS,
                get("sentadilla_bilateral_v03").unit);
    }

    @Test public void plankAndBalanceUseSeconds() {
        assertEquals(ExerciseVolumeProfileCatalog.Unit.SECONDS,
                get("plancha_frontal_v01").unit);
        assertEquals(ExerciseVolumeProfileCatalog.Unit.SECONDS,
                get("equilibrio_unipodal_v01").unit);
    }

    @Test public void walkingAndCyclingUseSeconds() {
        assertEquals(ExerciseVolumeProfileCatalog.Unit.SECONDS,
                get("marcha_v02").unit);
        assertEquals(ExerciseVolumeProfileCatalog.Unit.SECONDS,
                get("ciclo_estatico_v01").unit);
    }

    @Test public void DynamicUpperBodyMovementIsNotMisreadAsStaticHold() {
        assertEquals(ExerciseVolumeProfileCatalog.Unit.REPETITIONS,
                get("flexiones_v02").unit);
        assertEquals(ExerciseVolumeProfileCatalog.Unit.REPETITIONS,
                get("superman_v01").unit);
    }

    @Test public void manualOnlyCatalogVariantsHaveMetadataButNoAutoProgression() {
        ExerciseVolumeProfileCatalog.Profile p=get("sentadilla_bilateral_v07");
        assertNotNull(p.unit);
        assertFalse(p.automaticEligible);
    }

    @Test public void missingVariantFailsClosed() throws Exception {
        String header="variant_id\tunit\tinitial_sets\tinitial_value\t"
                +"minimum_value\tmaximum_value\tincrement\n";
        try {
            ExerciseVolumeProfileCatalog.load(
                    new java.io.StringReader(header),catalog);
            fail("should reject unclassified catalog");
        }catch(IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Falta referencia"));
        }
    }
}
