package pe.asterionsystem.core.engine.exercises;

import static org.junit.Assert.*;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

/** Sport taxonomy is optional for legacy code and complete in Android assets. */
public final class ExerciseDisciplineIndexTest {
    private Reader resource(String filename) {
        return new InputStreamReader(getClass().getResourceAsStream(
                "/asterion_exercises/v1/"+filename),StandardCharsets.UTF_8);
    }

    private ExerciseCatalog bundledWithSports() throws Exception {
        try(Reader families=resource("families.tsv");Reader types=resource("types.tsv");
            Reader variants=resource("variants.tsv");Reader intensity=resource("intensity.tsv");
            Reader sports=resource("disciplines.tsv")) {
            return ExerciseCatalogLoader.load(families,types,variants,intensity,sports);
        }
    }

    @Test public void allMovementTypesHaveSportLabelsButOneCanonicalCategory()
            throws Exception {
        ExerciseCatalog catalog=bundledWithSports();
        ExerciseDisciplineIndex index=catalog.disciplineIndex();
        assertEquals(130,catalog.families().size());
        assertEquals(374,catalog.types().size());
        assertEquals(1157,catalog.variants().size());
        assertEquals(25,index.disciplines().size());
        assertEquals(130,ExerciseMuscleFocus.classifiedFamilies());
        Set<String> names=new HashSet<>();
        for(ExerciseType type:catalog.types()) {
            List<String> disciplines=index.disciplinesForType(type.id());
            assertFalse("Missing sport tags: "+type.id(),disciplines.isEmpty());
            assertTrue(disciplines.contains("general"));
            assertEquals(disciplines.size(),new HashSet<>(disciplines).size());
            assertFalse(index.roleForType(type.id()).isEmpty());
            assertNotNull(catalog.categoryIndex().categoryForType(type.id()));
            names.add(type.id());
        }
        assertEquals(374,names.size());
        for(String discipline:index.disciplines()) {
            assertFalse(index.types(discipline).isEmpty());
            assertFalse(index.variants(discipline).isEmpty());
            for(ExerciseVariant variant:index.variants(discipline))
                assertTrue(index.disciplinesForType(variant.typeId())
                        .contains(discipline));
        }
    }

    @Test public void sportSearchFindsLegacyCalisthenicsAndNewSpecificActivities()
            throws Exception {
        ExerciseCatalog catalog=bundledWithSports();
        assertFalse(catalog.searchByDiscipline("calistenia","dominada",null,null,null,
                5,50,0).isEmpty());
        assertFalse(catalog.searchByDiscipline("gimnasio","prensa", "piernas",null,null,
                5,50,0).isEmpty());
        assertFalse(catalog.searchByDiscipline("futbol","Pase con interior","piernas",
                "balon_futbol",null,5,50,0).isEmpty());
        assertFalse(catalog.searchByDiscipline("baloncesto","bote","cardio","balon",null,
                5,50,0).isEmpty());
        assertFalse(catalog.searchByDiscipline("hockey","conducción", "tren_superior",
                "palo_hockey",null,5,50,0).isEmpty());
        assertFalse(catalog.searchByDiscipline("voleibol","antebrazos","tren_superior",
                "balon_voleibol",null,5,50,0).isEmpty());
        assertTrue(catalog.searchByDiscipline("natacion","dominada",null,null,null,
                5,50,0).isEmpty());
        assertTrue(catalog.searchByDiscipline("futbol","", "descanso",null,null,
                5,50,0).isEmpty());
    }

    @Test public void sportsViewsAreReadOnlyAndPaginationLimitsApply() throws Exception {
        ExerciseCatalog catalog=bundledWithSports();
        ExerciseDisciplineIndex index=catalog.disciplineIndex();
        assertThrows(UnsupportedOperationException.class,
                ()->index.disciplines().add("inventado"));
        assertThrows(UnsupportedOperationException.class,
                ()->index.types("general").clear());
        assertThrows(UnsupportedOperationException.class,
                ()->index.variants("baloncesto").clear());
        assertThrows(UnsupportedOperationException.class,
                ()->index.disciplinesForType("dominadas").clear());
        assertThrows(IllegalArgumentException.class,
                ()->catalog.searchByDiscipline("gimnasio","",null,null,null,5,101,0));
        assertEquals(25,catalog.searchByDiscipline("general","",null,null,null,
                5,25,0).size());
        assertEquals(25,catalog.searchByDiscipline("general","",null,null,null,
                5,25,25).size());
    }

    @Test public void demandingActivityLabelsCannotBeAutomaticallyPrescribed()
            throws Exception {
        ExerciseCatalog catalog=bundledWithSports();
        for(String id:List.of("burpee_adaptado_v01","burpee_adaptado_v02",
                "carreras_ida_vuelta_controladas_v02")) {
            ExerciseVariant variant=catalog.variant(id).orElseThrow();
            assertEquals(ExerciseIntensity.VARIABLE,catalog.intensity(id));
            assertFalse(catalog.eligibleForAutomaticSuggestion(variant));
        }
        assertEquals("cardio",catalog.categoryIndex().categoryForVariant(
                "carreras_ida_vuelta_controladas_v02"));
    }

    @Test public void unknownSportOrDuplicateRowsFailWithoutSideEffects()
            throws Exception {
        String families="family_id\tname\tcategory\tpattern\n"
                +"test_family\tFamilia\tcardio\tpasos\n";
        String types="type_id\tfamily_id\tname\n"
                +"test_type\ttest_family\tTipo\n";
        String variants="variant_id\ttype_id\tname\tlevel\tequipment\texecution\tsupervision\n"
                +"test_v01\ttest_type\tPasos fáciles\t1\tsin_equipo\tcontrolado\t0\n";
        String intensity="variant_id\tintensity\ntest_v01\tbaja\n";
        String header="type_id\tdisciplines\trole\n";
        ExerciseCatalog legacy=ExerciseCatalogLoader.load(new StringReader(families),
                new StringReader(types),new StringReader(variants));
        assertTrue(legacy.disciplineIndex().disciplines().isEmpty());
        assertThrows(IllegalArgumentException.class,()->ExerciseCatalogLoader.load(
                new StringReader(families),new StringReader(types),new StringReader(variants),
                new StringReader(intensity),
                new StringReader(header+"other_type\tgeneral\thabilidad\n")));
        assertThrows(IllegalArgumentException.class,()->ExerciseCatalogLoader.load(
                new StringReader(families),new StringReader(types),new StringReader(variants),
                new StringReader(intensity),
                new StringReader(header+"test_type\tgeneral+general\thabilidad\n")));
    }
}
