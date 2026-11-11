package pe.asterionsystem.core.engine.exercises;

import static org.junit.Assert.*;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

/** All known intensity tags are explicitly sourced; legacy IDs remain stable. */
public final class ExerciseIntensityCatalogTest {
    @Test public void realMetadataLoadsAndEveryTaggedVariantExists() throws Exception {
        String folder="/asterion_exercises/v1/";
        try(Reader f=resource(folder+"families.tsv");
            Reader t=resource(folder+"types.tsv");
            Reader v=resource(folder+"variants.tsv");
            Reader intensities=resource(folder+"intensity.tsv")) {
            ExerciseCatalog catalog=ExerciseCatalogLoader.load(f,t,v,intensities);
            assertEquals(1257,catalog.variants().size());
            assertEquals(ExerciseIntensity.LOW,catalog.intensity("marcha_acuatica_v01"));
            assertEquals(ExerciseIntensity.HIGH,catalog.intensity("nado_crol_recreativo_v03"));
            assertEquals(ExerciseIntensity.VARIABLE,
                    catalog.intensity("press_suelo_unilateral_v02"));
            assertEquals(ExerciseIntensity.UNCLASSIFIED,
                    catalog.intensity("sentadilla_bilateral_v01"));
            assertFalse(catalog.eligibleForAutomaticSuggestion(
                    catalog.variant("nado_crol_recreativo_v03").get()));
            assertTrue(catalog.eligibleForAutomaticSuggestion(
                    catalog.variant("empuje_escapular_pared_v01").get()));
        }
    }

    private Reader resource(String path) {
        return new InputStreamReader(
                getClass().getResourceAsStream(path),StandardCharsets.UTF_8);
    }

    private static final String F="family_id\tname\tcategory\tpattern\n"
            +"test_family\tFamilia de prueba\tcardio\ttest\n";
    private static final String T="type_id\tfamily_id\tname\n"
            +"test_type\ttest_family\tTipo de prueba\n";
    private static final String V="variant_id\ttype_id\tname\tlevel\tequipment\texecution\tsupervision\n"
            +"test_variant\ttest_type\tMovimiento de prueba\t1\tsin_equipo\taerobico\t0\n";

    @Test public void highIntensityCannotBeAssignedAutomaticallyAtAnyTechnicalLevel()
            throws Exception {
        ExerciseCatalog catalog=ExerciseCatalogLoader.load(
                new StringReader(F),new StringReader(T),new StringReader(V),
                new StringReader("variant_id\tintensity\n"
                        +"test_variant\talta\n"));
        ExerciseVariant item=catalog.variants().get(0);
        assertEquals(1,item.difficulty());
        assertTrue(item.eligibleForAutomaticSuggestion());
        assertFalse(catalog.eligibleForAutomaticSuggestion(item));
    }

    @Test public void variableIntensityRequiresMoreInformation() throws Exception {
        ExerciseCatalog catalog=ExerciseCatalogLoader.load(
                new StringReader(F),new StringReader(T),new StringReader(V),
                new StringReader("variant_id\tintensity\n"
                        +"test_variant\tvariable\n"));
        assertFalse(catalog.eligibleForAutomaticSuggestion(catalog.variants().get(0)));
    }

    @Test public void originalThreeReaderApiRemainsBackwardsCompatible() throws Exception {
        ExerciseCatalog catalog=ExerciseCatalogLoader.load(
                new StringReader(F),new StringReader(T),new StringReader(V));
        assertEquals(ExerciseIntensity.UNCLASSIFIED,
                catalog.intensity("test_variant"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void unexpectedIntensityValuesAreRejected() throws Exception {
        ExerciseCatalogLoader.load(new StringReader(F),new StringReader(T),
                new StringReader(V),
                new StringReader("variant_id\tintensity\ntest_variant\textrema\n"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void missingVariantReferencesAreRejected() throws Exception {
        ExerciseCatalogLoader.load(new StringReader(F),new StringReader(T),
                new StringReader(V),
                new StringReader("variant_id\tintensity\ninvalid_variant\tbaja\n"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void duplicateIntensityRowsAreRejected() throws Exception {
        ExerciseCatalogLoader.load(new StringReader(F),new StringReader(T),
                new StringReader(V),
                new StringReader("variant_id\tintensity\n"
                        +"test_variant\tbaja\ntest_variant\tmoderada\n"));
    }
}
