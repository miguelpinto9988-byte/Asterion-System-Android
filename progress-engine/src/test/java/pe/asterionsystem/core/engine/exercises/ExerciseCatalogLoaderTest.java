package pe.asterionsystem.core.engine.exercises;

import static org.junit.Assert.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import org.junit.Test;

/** Verifies the full packaged taxonomy, not a simulated catalog. */
public final class ExerciseCatalogLoaderTest {
    private static final String DIRECTORY = "/asterion_exercises/v1/";

    private static Reader bundled(String file) {
        InputStream stream = ExerciseCatalogLoaderTest.class.getResourceAsStream(DIRECTORY + file);
        assertNotNull("Missing versioned catalog resource: " + file, stream);
        return new InputStreamReader(stream, StandardCharsets.UTF_8);
    }

    private static ExerciseCatalog loadBundled() throws IOException {
        try (Reader f = bundled("families.tsv");
             Reader t = bundled("types.tsv");
             Reader v = bundled("variants.tsv")) {
            return ExerciseCatalogLoader.load(f, t, v);
        }
    }

    @Test public void packagedInventoryCountsAndHierarchyAreValid() throws Exception {
        ExerciseCatalog index = loadBundled();
        assertEquals(39, index.families().size());
        assertEquals(80, index.types().size());
        assertEquals(445, index.variants().size());
        for (ExerciseFamily family : index.families()) {
            assertFalse("Family without types: " + family.id(),
                    index.typesInFamily(family.id()).isEmpty());
        }
        for (ExerciseType type : index.types()) {
            List<ExerciseVariant> variants = index.variantsInType(type.id());
            assertFalse("Type without variants: " + type.id(), variants.isEmpty());
            int previous = 0;
            for (ExerciseVariant variation : variants) {
                assertTrue(variation.difficulty() >= previous);
                previous = variation.difficulty();
            }
        }
    }

    @Test public void stableIdentifiersRemainUniqueAndResolvable() throws Exception {
        ExerciseCatalog index = loadBundled();
        Set<String> keys = new HashSet<>();
        for (ExerciseVariant variation : index.variants()) {
            assertTrue("Duplicate variant: " + variation.id(), keys.add(variation.id()));
            assertSame(variation, index.variant(variation.id()).orElse(null));
        }
        assertEquals("Sentadilla a silla",
                index.variant("sentadilla_bilateral_v01").get().name());
        assertEquals("Flexión de brazos estándar",
                index.variant("flexiones_v04").get().name());
    }

    @Test public void implementationAndDifficultyFiltersWorkTogether() throws Exception {
        ExerciseCatalog catalog = loadBundled();
        List<ExerciseVariant> matches = catalog.search(
                "", "piernas", "mancuerna", "carga_externa", 3, 100, 0);
        assertFalse(matches.isEmpty());
        for (ExerciseVariant item : matches) {
            assertTrue(item.difficulty() <= 3);
            assertTrue(item.equipment().contains("mancuerna"));
            assertEquals("carga_externa", item.execution());
        }
        assertFalse(catalog.search("biceps", "tren_superior", null,
                null, 5, 100, 0).isEmpty());
        assertFalse(catalog.search("sentadilla", "piernas", null,
                3, 50, 0).isEmpty());
        assertTrue(catalog.search("noexiste_palabra_aleatoria", null, null,
                5, 10, 0).isEmpty());
    }

    @Test public void advancedSkillsAreNotAutoRecommended() throws Exception {
        ExerciseCatalog catalog = loadBundled();
        boolean foundAdvanced = false;
        boolean foundIntro = false;
        for (ExerciseVariant entry : catalog.variants()) {
            if (entry.difficulty() >= 4) {
                foundAdvanced = true;
                assertTrue(entry.supervisionRecommended());
                assertFalse(entry.eligibleForAutomaticSuggestion());
            } else if (entry.eligibleForAutomaticSuggestion()) {
                foundIntro = true;
            }
        }
        assertTrue(foundAdvanced);
        assertTrue(foundIntro);
    }

    @Test public void queryResultsAndEquipmentAreImmutable() throws Exception {
        ExerciseCatalog catalog = loadBundled();
        try {
            catalog.families().clear();
            fail("Families must be read-only");
        } catch (UnsupportedOperationException expected) { }
        try {
            catalog.variants().get(0).equipment().add("unknown");
            fail("Equipment must be read-only");
        } catch (UnsupportedOperationException expected) { }
        try {
            catalog.variantsInType("sentadilla_bilateral").clear();
            fail("Variation lists must be read-only");
        } catch (UnsupportedOperationException expected) { }
    }

    @Test public void duplicateIdsAndDanglingParentsAreRejected() throws Exception {
        String family = "family_id\tname\tcategory\tpattern\nsquat\tSentadilla\tpiernas\tsentadilla\n";
        String type = "type_id\tfamily_id\tname\nsquat_type\tsquat\tSentadilla libre\n";
        String variants = "variant_id\ttype_id\tname\tlevel\tequipment\texecution\tsupervision\n";
        try {
            ExerciseCatalogLoader.load(new StringReader(family + "squat\tDuplicado\tpiernas\tsentadilla\n"),
                    new StringReader(type),
                    new StringReader(variants + "test_v01\tsquat_type\tBase\t1\tsin_equipo\tlibre\t0\n"));
            fail("Duplicate families must be rejected");
        } catch (IllegalArgumentException expected) { }
        try {
            ExerciseCatalogLoader.load(new StringReader(family),
                    new StringReader(type.replace("\tsquat\t", "\tmissing\t")),
                    new StringReader(variants + "test_v01\tsquat_type\tBase\t1\tsin_equipo\tlibre\t0\n"));
            fail("Dangling family must be rejected");
        } catch (IllegalArgumentException expected) { }
    }

    @Test public void outOfOrderLevelsAndMissingSupervisionAreRejected() throws Exception {
        String family = "family_id\tname\tcategory\tpattern\nsquat\tSentadilla\tpiernas\tsentadilla\n";
        String type = "type_id\tfamily_id\tname\nsquat_type\tsquat\tSentadilla libre\n";
        String v = "variant_id\ttype_id\tname\tlevel\tequipment\texecution\tsupervision\n";
        try {
            ExerciseCatalogLoader.load(new StringReader(family), new StringReader(type),
                    new StringReader(v + "test_v01\tsquat_type\tCompleja\t4\tbarra\tlibre\t1\n"
                            + "test_v02\tsquat_type\tSimple\t2\tsin_equipo\tlibre\t0\n"));
            fail("Variations must be ordered");
        } catch (IllegalArgumentException expected) { }
        try {
            ExerciseCatalogLoader.load(new StringReader(family), new StringReader(type),
                    new StringReader(v + "test_v01\tsquat_type\tAvanzada\t5\tbarra\tlibre\t0\n"));
            fail("Higher complexity requires supervision");
        } catch (IllegalArgumentException expected) { }
    }

    @Test public void invalidHeaderAndUnknownEquipmentFieldsFail() throws Exception {
        try {
            ExerciseCatalogLoader.load(new StringReader("wrong\n"),
                    new StringReader("type_id\tfamily_id\tname\n"),
                    new StringReader("variant_id\ttype_id\tname\tlevel\tequipment\texecution\tsupervision\n"));
            fail("Unexpected header must be rejected");
        } catch (IllegalArgumentException expected) { }
        try {
            new ExerciseVariant("test_id", "test_type", "Ejercicio", 2, "sin equipo", "libre", false);
            fail("Free-form, ambiguous implements must be rejected");
        } catch (IllegalArgumentException expected) { }
    }
}
