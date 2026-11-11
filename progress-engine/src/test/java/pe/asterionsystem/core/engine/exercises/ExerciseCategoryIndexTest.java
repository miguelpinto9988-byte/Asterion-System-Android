package pe.asterionsystem.core.engine.exercises;

import static org.junit.Assert.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

/** Real bundled v1 TSV taxonomy: every variant resolves through the five categories. */
public final class ExerciseCategoryIndexTest {
    private ExerciseCatalog load() throws Exception {
        String root="/asterion_exercises/v1/";
        try(InputStreamReader families=reader(root+"families.tsv");
            InputStreamReader types=reader(root+"types.tsv");
            InputStreamReader variants=reader(root+"variants.tsv");
            InputStreamReader intensity=reader(root+"intensity.tsv")) {
            return ExerciseCatalogLoader.load(families,types,variants,intensity);
        }
    }

    private InputStreamReader reader(String path) {
        return new InputStreamReader(
                getClass().getResourceAsStream(path),StandardCharsets.UTF_8);
    }

    @Test public void allRealRowsHaveOneAndOnlyOneOperationalCategory() throws Exception {
        ExerciseCatalog catalog=load();
        ExerciseCategoryIndex index=catalog.categoryIndex();
        assertEquals(5,index.categories().size());
        int families=0,types=0,variants=0;
        Set<String> familyIds=new HashSet<>();
        Set<String> typeIds=new HashSet<>();
        Set<String> variantIds=new HashSet<>();
        for(String category:index.categories()) {
            List<ExerciseFamily> familyList=index.families(category);
            List<ExerciseType> typeList=index.types(category);
            List<ExerciseVariant> variantList=index.variants(category);
            assertFalse(category+" empty families",familyList.isEmpty());
            assertFalse(category+" empty types",typeList.isEmpty());
            assertFalse(category+" empty variants",variantList.isEmpty());
            families+=familyList.size();
            types+=typeList.size();
            variants+=variantList.size();
            for(ExerciseFamily family:familyList) {
                assertTrue("Repeated family "+family.id(),familyIds.add(family.id()));
                assertEquals(category,index.categoryForFamily(family.id()));
                assertEquals(category,
                        ExerciseCategoryMapper.primaryCategory(family.category()));
                assertTrue(ExerciseMuscleFocus.knowsFamily(family.id()));
            }
            for(ExerciseType type:typeList) {
                assertTrue("Repeated type "+type.id(),typeIds.add(type.id()));
                assertEquals(category,index.categoryForType(type.id()));
                assertEquals(category,index.categoryForFamily(
                        index.familyForType(type.id())));
            }
            for(ExerciseVariant variant:variantList) {
                assertTrue("Repeated variant "+variant.id(),
                        variantIds.add(variant.id()));
                assertEquals(category,index.categoryForVariant(variant.id()));
                assertEquals(category,index.categoryForType(
                        index.typeForVariant(variant.id())));
                assertEquals(category,index.categoryForFamily(
                        index.familyForVariant(variant.id())));
                assertFalse(ExerciseMuscleFocus.forVariant(catalog,variant)
                        .primary().equals("no clasificado"));
            }
        }
        assertEquals(125,families);
        assertEquals(354,types);
        assertEquals(1017,variants);
        assertEquals(catalog.variants().size(),variantIds.size());
    }

    @Test public void categoryInventoryMatchesDeclaredProfileOrder() throws Exception {
        ExerciseCategoryIndex index=load().categoryIndex();
        int[] familyCounts={27,23,27,24,24};
        for(int i=0;i<5;i++) {
            String id=ExerciseCategoryMapper.weeklyCode(i);
            assertEquals(familyCounts[i],index.families(id).size());
        }
        assertEquals("movilidad",index.categoryForFamily("equilibrio"));
        assertEquals("tren_central",index.categoryForFamily("transportes"));
        assertEquals("tren_superior",index.categoryForVariant(
                "elevacion_frontal_sentado_v01"));
        assertEquals("tren_central",index.categoryForVariant(
                "inclinacion_pelvis_pie_v01"));
        assertEquals("tren_inferior",index.categoryForVariant(
                "paso_frente_y_retorno_v01"));
        assertEquals("movilidad",index.categoryForVariant(
                "giro_talon_suelo_v01"));
        assertEquals("cardio",index.categoryForVariant(
                "marcha_sentada_recreativa_v01"));
    }

    @Test public void indexViewsAreImmutableAndInvalidCodesFail() throws Exception {
        ExerciseCategoryIndex index=load().categoryIndex();
        assertThrows(UnsupportedOperationException.class,
                ()->index.categories().add("descanso"));
        assertThrows(UnsupportedOperationException.class,
                ()->index.families("tren_inferior").clear());
        assertThrows(UnsupportedOperationException.class,
                ()->index.types("movilidad").clear());
        assertThrows(UnsupportedOperationException.class,
                ()->index.variants("cardio").clear());
        assertThrows(IllegalArgumentException.class,
                ()->index.families("descanso"));
        assertThrows(IllegalArgumentException.class,
                ()->index.categoryForVariant("invalid_missing"));
    }

    @Test public void catalogSearchAcceptsFiveCategoriesAndHistoricalCodes()
            throws Exception {
        ExerciseCatalog catalog=load();
        assertFalse("Coordination must be discoverable under mobility",
                catalog.search("Equilibrio sobre una pierna","movilidad",
                        null,5,100,0).isEmpty());
        assertFalse("Historic coordination filters remain valid",
                catalog.search("Equilibrio sobre una pierna","coordinacion",
                        null,5,100,0).isEmpty());
        assertFalse("Integrated movement must appear under trunk",
                catalog.search("Transportes de cargas","tren_central",
                        null,5,100,0).isEmpty());
        assertFalse("New category families must be searchable",
                catalog.search("Marcha recreativa sentado","cardio",
                        null,2,100,0).isEmpty());
        assertTrue("Rest is never an exercise category",
                catalog.search("","descanso",null,5,100,0).isEmpty());
    }

    @Test public void expandedVariantsKeepLowComplexityAndExplicitIntensity()
            throws Exception {
        ExerciseCatalog catalog=load();
        ExerciseCategoryIndex index=catalog.categoryIndex();
        for(ExerciseVariant variant:catalog.variants()) {
            if(variant.id().startsWith("elevacion_frontal_sentado_")
                    || variant.id().startsWith("marcha_sentada_recreativa_")
                    || variant.id().startsWith("paso_frente_y_retorno_")) {
                assertTrue(variant.difficulty()<=2);
                assertFalse(variant.supervisionRecommended());
                assertTrue(catalog.eligibleForAutomaticSuggestion(variant));
                assertTrue(catalog.intensity(variant.id())!=
                        ExerciseIntensity.UNCLASSIFIED);
                assertFalse(index.categoryForVariant(variant.id()).isBlank());
            }
        }
        assertEquals(ExerciseIntensity.LOW,
                catalog.intensity("marcha_sentada_recreativa_v01"));
        assertEquals(ExerciseIntensity.MODERATE,
                catalog.intensity("marcha_sentada_recreativa_v02"));
    }
}
