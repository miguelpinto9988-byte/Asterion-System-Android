package pe.asterionsystem.core.engine.missions;

import static org.junit.Assert.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalogLoader;
import pe.asterionsystem.core.engine.exercises.ExerciseCategoryMapper;
import pe.asterionsystem.core.engine.exercises.ExerciseIntensity;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;

public final class RoutineGeneratorTest {
    private ExerciseCatalog catalog;
    private RoutineGenerator generator;
    private final LocalDate date=LocalDate.of(2026,11,11);

    @Before public void setup() throws Exception {
        String path="/asterion_exercises/v1/";
        try(InputStreamReader a=reader(path+"families.tsv");
            InputStreamReader b=reader(path+"types.tsv");
            InputStreamReader c=reader(path+"variants.tsv");
            InputStreamReader d=reader(path+"intensity.tsv");
            InputStreamReader e=reader(path+"sport_registry.tsv");
            InputStreamReader f=reader(path+"sport_tags.tsv")) {
            catalog=ExerciseCatalogLoader.load(a,b,c,d,e,f);
        }
        generator=new RoutineGenerator(catalog);
    }
    private InputStreamReader reader(String path) {
        return new InputStreamReader(getClass().getResourceAsStream(path),StandardCharsets.UTF_8);
    }
    private MissionPlanner.Profile profile() {
        return new MissionPlanner.Profile("intermedio","bienestar",
                Arrays.asList(0,1,2,3,4,5,5),
                Set.of("sin_equipo","silla","pared","colchoneta","mancuerna","banco"),
                4,"moderada");
    }
    private RoutineGenerator.Request request(String category,int minutes,
                                              String level,int variation) {
        return new RoutineGenerator.Request(category,minutes,level,date,variation);
    }
    private List<String> signature(List<RoutineGenerator.Offer> offers) {
        ArrayList<String> result=new ArrayList<>();
        for(RoutineGenerator.Offer offer:offers) {
            StringBuilder ids=new StringBuilder(offer.id());
            for(ExerciseVariant v:offer.exercises())ids.append('/').append(v.id());
            result.add(ids.toString());
        }
        return result;
    }

    @Test public void sixOffersAreReproducibleUsingEntireNewCatalog() {
        RoutineGenerator.Request req=request("tren_superior",30,"intermedio",0);
        List<RoutineGenerator.Offer> one=generator.generate(req,profile());
        assertEquals(6,one.size());
        assertEquals(signature(one),signature(new RoutineGenerator(catalog)
                .generate(req,profile())));
        Set<String> ids=new HashSet<>(signature(one));
        assertEquals(6,ids.size());
    }

    @Test public void allFiveCategoriesProduceSafeDistinctOptions() {
        for(String code:ExerciseCategoryMapper.weeklyCodes()) {
            List<RoutineGenerator.Offer> offers=generator.generate(
                    request(code,20,"basico",0),profile());
            assertTrue("category "+code,offers.size()>=5);
            for(RoutineGenerator.Offer offer:offers) {
                assertEquals(code,offer.category());
                assertEquals(4,offer.exercises().size());
                Set<String> ids=new HashSet<>();
                for(ExerciseVariant v:offer.exercises()) {
                    assertTrue(ids.add(v.id()));
                    assertEquals(code,catalog.categoryIndex().categoryForVariant(v.id()));
                    assertEquals(1,v.difficulty());
                    assertFalse(v.supervisionRecommended());
                    assertTrue(profile().equipment.containsAll(v.equipment()));
                    assertTrue(catalog.eligibleForAutomaticSuggestion(v));
                    assertTrue(catalog.intensity(v.id())==ExerciseIntensity.LOW
                            || catalog.intensity(v.id())==ExerciseIntensity.MODERATE);
                }
            }
        }
    }

    @Test public void varietyChangesWithExplicitRefreshOrDate() {
        RoutineGenerator.Request first=request("cardio",30,"intermedio",0);
        Set<List<String>> samples=new HashSet<>();
        for(int i=0;i<6;i++)samples.add(signature(generator.generate(
                request("cardio",30,"intermedio",i),profile())));
        assertTrue(samples.size()>1);
        assertNotEquals(signature(generator.generate(first,profile())),
                signature(generator.generate(
                        new RoutineGenerator.Request("cardio",30,"intermedio",
                                date.plusDays(1),0),profile())));
    }

    @Test public void advancedStillExcludesSupervisedHighImpactAndDisallowedEquipment() {
        for(RoutineGenerator.Offer offer:generator.generate(
                request("tren_inferior",45,"avanzado",0),profile())) {
            for(ExerciseVariant v:offer.exercises()) {
                assertTrue(v.difficulty()<=3);
                assertFalse(v.supervisionRecommended());
                assertTrue(profile().equipment.containsAll(v.equipment()));
                assertTrue(catalog.eligibleForAutomaticSuggestion(v));
                assertNotEquals(ExerciseIntensity.HIGH,catalog.intensity(v.id()));
                assertNotEquals(ExerciseIntensity.VARIABLE,catalog.intensity(v.id()));
            }
        }
    }

    @Test public void onlyApprovedDurationsLevelsAndCategories() {
        assertThrows(IllegalArgumentException.class,()->request("piernas",20,"basico",0));
        assertThrows(IllegalArgumentException.class,()->request("cardio",1,"basico",0));
        assertThrows(IllegalArgumentException.class,()->request("cardio",20,"elite",0));
        assertThrows(IllegalArgumentException.class,()->request("cardio",20,"basico",-1));
        assertEquals(6,request("cardio",60,"basico",0).stepCount());
        assertEquals(3,request("cardio",10,"basico",0).stepCount());
    }

    @Test public void monthGridHandlesLeapYearsAndMonthRollover() {
        YearMonth feb=YearMonth.of(2028,2);
        assertEquals(42,MonthCalendar.days(feb).size());
        assertEquals(29,MonthCalendar.days(feb).stream().filter(x->x!=null).count());
        assertEquals(28,MonthCalendar.days(YearMonth.of(2027,2)).stream()
                .filter(x->x!=null).count());
        assertEquals(YearMonth.of(2027,1),MonthCalendar.shift(
                YearMonth.of(2026,12),1));
        assertEquals(YearMonth.of(2025,12),MonthCalendar.shift(
                YearMonth.of(2026,1),-1));
        assertEquals(YearMonth.of(2037,2),MonthCalendar.chooseYear(feb,2037));
        assertEquals(YearMonth.of(5000,11),MonthCalendar.shift(
                YearMonth.of(2026,11),(5000-2026)*12));
    }
}
