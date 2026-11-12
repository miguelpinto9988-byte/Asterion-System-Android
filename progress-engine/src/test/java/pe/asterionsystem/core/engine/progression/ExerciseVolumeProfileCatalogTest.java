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

    @Test public void raisedStartingQuantityCoversAll1257VariantsWithoutChangingSets() {
        int repetitions=0,seconds=0;
        for(ExerciseVariant exercise:catalog.variants()) {
            ExerciseVolumeProfileCatalog.Profile p=profiles.require(exercise);
            assertEquals("Conservar el inicio en una serie por seguridad",1,p.initialSets);
            assertTrue("Referencia nueva debe permanecer bajo su máximo",
                    p.initialValue<=p.maximumValue);
            if(p.unit==ExerciseVolumeProfileCatalog.Unit.REPETITIONS) {
                repetitions++;
                assertTrue("Referencia de repeticiones ya recalibrada",
                        p.initialValue>=4);
            } else {
                seconds++;
                assertTrue("Referencia en segundos ya recalibrada",
                        p.initialValue>=8);
            }
        }
        assertEquals(924,repetitions);
        assertEquals(333,seconds);
    }

    @Test public void raisedBaselineExamplesRemainExerciseSpecific() {
        assertEquals(17,get("sentadilla_bilateral_v01").initialValue);
        assertEquals(15,get("sentadilla_bilateral_v03").initialValue);
        assertEquals(13,get("sentadilla_bilateral_v04").initialValue);
        assertEquals(12,get("sentadilla_bilateral_v07").initialValue);
        assertEquals(15,get("flexiones_v02").initialValue);
        assertEquals(25,get("plancha_frontal_v01").initialValue);
        assertEquals(30,get("equilibrio_unipodal_v01").initialValue);
        assertEquals(95,get("marcha_v02").initialValue);
        assertEquals(95,get("ciclo_estatico_v01").initialValue);
    }

    @Test public void secondIncreasePreservesLimitsAndOneInitialSetForAllVariants() {
        int repetitions=0;
        int seconds=0;
        for(ExerciseVariant variant:catalog.variants()) {
            ExerciseVolumeProfileCatalog.Profile p=profiles.require(variant);
            assertEquals(1,p.initialSets);
            assertTrue(p.initialValue<=p.maximumValue);
            if(p.unit==ExerciseVolumeProfileCatalog.Unit.REPETITIONS) {
                repetitions++;
                assertTrue("Repetitions after second recalibration",p.initialValue>=5);
            } else {
                seconds++;
                assertTrue("Seconds after second recalibration",p.initialValue>=11);
            }
        }
        assertEquals(924,repetitions);
        assertEquals(333,seconds);
    }

    @Test public void coreMobilityAndCardioRepetitionsHaveOwnInitialReferences() {
        assertEquals(14,get("superman_v01").initialValue);
        assertEquals(12,get("alcance_unipodal_v01").initialValue);
        assertEquals(13,get("talones_alternos_v02").initialValue);
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
