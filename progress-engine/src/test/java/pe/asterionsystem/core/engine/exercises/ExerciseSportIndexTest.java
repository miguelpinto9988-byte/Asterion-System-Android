package pe.asterionsystem.core.engine.exercises;

import static org.junit.Assert.*;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import org.junit.Test;

/** Real bundled assets: cross-sport tags remain secondary to the five categories. */
public final class ExerciseSportIndexTest {
    private Reader read(String name) {
        return new InputStreamReader(
                getClass().getResourceAsStream("/asterion_exercises/v1/"+name),
                StandardCharsets.UTF_8);
    }
    private ExerciseCatalog catalog() throws Exception {
        try(Reader f=read("families.tsv");Reader t=read("types.tsv");
            Reader v=read("variants.tsv");Reader i=read("intensity.tsv");
            Reader names=read("sport_registry.tsv");Reader tags=read("sport_tags.tsv")) {
            return ExerciseCatalogLoader.load(f,t,v,i,names,tags);
        }
    }

    @Test public void exactlyFivePrimaryCategoriesForAllSourcedSports() throws Exception {
        ExerciseCatalog c=catalog();
        assertEquals(125,c.families().size());
        assertEquals(354,c.types().size());
        assertEquals(1017,c.variants().size());
        assertEquals(5,c.categoryIndex().categories().size());
        assertEquals(56,c.sportIndex().sports().size());
        assertEquals(56,new HashSet<>(c.sportIndex().sports()).size());
        for(String code:c.sportIndex().sports())assertFalse(c.sportIndex().sportName(code).isBlank());
        for(ExerciseType type:c.types())
            assertTrue(c.categoryIndex().categories().contains(
                    c.categoryIndex().categoryForType(type.id())));
    }

    @Test public void sportsAreOptionalTagsNotNewPrimaryCategories() throws Exception {
        ExerciseCatalog c=catalog();
        assertTrue(c.sportIndex().includes("dep_burpee_tecnico","calistenia"));
        assertTrue(c.sportIndex().includes("dep_burpee_tecnico","funcional"));
        assertEquals("cardio",c.categoryIndex().categoryForType("dep_burpee_tecnico"));
        assertTrue(c.sportIndex().includes("dep_carrera_ida_vuelta","baloncesto"));
        assertEquals("cardio",c.categoryIndex().categoryForType("dep_carrera_ida_vuelta"));
        assertTrue(c.sportIndex().includes("dep_carrera_ida_vuelta","hockey_cesped"));
        assertFalse(c.search("","cardio",null,null,"baloncesto",5,100,0).isEmpty());
        assertFalse(c.search("","tren_superior",null,null,"calistenia",5,100,0).isEmpty());
        assertTrue(c.search("","movilidad",null,null,"baloncesto",5,100,0).stream()
                .allMatch(v->c.categoryIndex().categoryForVariant(v.id()).equals("movilidad")));
        assertTrue(c.sportIndex().codesForType("sentadilla_bilateral").isEmpty());
        assertFalse(c.search("","tronco",null,5,100,0).isEmpty());
        assertFalse(c.search("","piernas",null,5,100,0).isEmpty());
    }

    @Test public void manualOnlyAndAdvancedRecordsNeverAutoSuggested() throws Exception {
        ExerciseCatalog c=catalog();
        int sourced=0,manual=0;
        for(ExerciseVariant v:c.variants()) {
            if(!v.id().startsWith("dep_"))continue;
            sourced++;
            assertFalse(c.sportIndex().codesForType(v.typeId()).isEmpty());
            assertTrue(c.sportIndex().sourceUrl(v.typeId()).startsWith("https://"));
            assertNotEquals(ExerciseIntensity.UNCLASSIFIED,c.intensity(v.id()));
            if(c.sportIndex().manualOnly(v.typeId())) {
                manual++;
                assertFalse("Manual-only movement was auto-suggestable: "+v.id(),
                        c.eligibleForAutomaticSuggestion(v));
                assertTrue(v.supervisionRecommended());
            }
        }
        assertEquals(100,sourced);
        assertTrue(manual>=25);
        assertFalse(c.eligibleForAutomaticSuggestion(
                c.variant("dep_burpee_tecnico_v01").orElseThrow()));
        assertFalse(c.eligibleForAutomaticSuggestion(
                c.variant("dep_carrera_ida_vuelta_v01").orElseThrow()));
    }

    @Test public void sourceAndSportFiltersAreImmutableAndValidated() throws Exception {
        ExerciseCatalog c=catalog();
        assertThrows(UnsupportedOperationException.class,
                ()->c.sportIndex().sports().add("invalido"));
        assertThrows(UnsupportedOperationException.class,
                ()->c.sportIndex().codesForType("dep_burpee_tecnico").clear());
        assertThrows(IllegalArgumentException.class,
                ()->c.sportIndex().sportName("inexistente"));
        assertThrows(IllegalArgumentException.class,
                ()->c.search("","cardio",null,null,"inexistente",5,20,0));
        assertEquals(56,c.sportIndex().sports().size());
    }
}
