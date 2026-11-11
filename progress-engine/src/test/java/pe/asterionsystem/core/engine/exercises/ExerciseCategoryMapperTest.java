package pe.asterionsystem.core.engine.exercises;

import static org.junit.Assert.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

/** All 1257 real catalog variants must belong to exactly one of five weekly bins. */
public class ExerciseCategoryMapperTest {
    @Test public void originalSevenSectionsMapToFiveProfileCategories() {
        assertEquals("movilidad",ExerciseCategoryMapper.primaryCategory("coordinacion"));
        assertEquals("tren_central",ExerciseCategoryMapper.primaryCategory("integrado"));
        assertEquals(0,ExerciseCategoryMapper.weeklyIndex("tren_superior"));
        assertEquals(1,ExerciseCategoryMapper.weeklyIndex("tronco"));
        assertEquals(1,ExerciseCategoryMapper.weeklyIndex("tren_central"));
        assertEquals(2,ExerciseCategoryMapper.weeklyIndex("piernas"));
        assertEquals(2,ExerciseCategoryMapper.weeklyIndex("tren_inferior"));
        assertEquals(3,ExerciseCategoryMapper.weeklyIndex("movilidad"));
        assertEquals(4,ExerciseCategoryMapper.weeklyIndex("cardio"));
        assertEquals(3,ExerciseCategoryMapper.weeklyIndex("coordinacion"));
        assertEquals(1,ExerciseCategoryMapper.weeklyIndex("integrado"));
        assertEquals(-1,ExerciseCategoryMapper.weeklyIndex("desconocido"));
        assertEquals(5,ExerciseCategoryMapper.weeklyCodes().size());
        assertEquals(java.util.List.of("tren_superior","tren_central","tren_inferior","movilidad","cardio"),ExerciseCategoryMapper.weeklyCodes());
    }

    @Test public void everySourceFamilyAndVariantIsAccessibleByWeeklyCategory() throws Exception {
        String base="/asterion_exercises/v1/";
        try (InputStreamReader f=read(base+"families.tsv");
             InputStreamReader t=read(base+"types.tsv");
             InputStreamReader v=read(base+"variants.tsv")) {
            ExerciseCatalog catalog=ExerciseCatalogLoader.load(f,t,v);
            assertEquals(155,catalog.families().size());
            assertEquals(474,catalog.types().size());
            assertEquals(1257,catalog.variants().size());
            Set<Integer> categories=new HashSet<>();
            for(ExerciseFamily family:catalog.families()) {
                int index=ExerciseCategoryMapper.weeklyIndex(family.category());
                assertTrue("Unmapped: "+family.id(),index>=0 && index<5);
                categories.add(index);
            }
            assertEquals(5,categories.size());
            Set<String> existing=new HashSet<>();
            for(ExerciseVariant variant:catalog.variants()) {
                assertTrue("duplicate ID "+variant.id(),existing.add(variant.id()));
                assertFalse(variant.name().isBlank());
            }
            assertTrue(catalog.variant("prension_suave_v01").isPresent());
            assertTrue(catalog.variant("elevaciones_rodillas_cardio_v03").isPresent());
        }
    }

    private InputStreamReader read(String location) {
        return new InputStreamReader(
                getClass().getResourceAsStream(location),StandardCharsets.UTF_8);
    }

    @Test(expected=IllegalArgumentException.class)
    public void restIsNotAnExerciseCategory() {
        ExerciseCategoryMapper.weeklyCode(5);
    }
}
