package pe.asterionsystem.core.engine.missions;

import static org.junit.Assert.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalogLoader;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;

public class MissionPlannerTest {
    private MissionPlanner planner;
    private final LocalDate monday=LocalDate.of(2026,11,9);

    @Before public void setup() throws Exception {
        String root="/asterion_exercises/v1/";
        try (InputStreamReader f=reader(root+"families.tsv");
             InputStreamReader t=reader(root+"types.tsv");
             InputStreamReader v=reader(root+"variants.tsv")) {
            ExerciseCatalog catalog=ExerciseCatalogLoader.load(f,t,v);
            assertEquals(505,catalog.variants().size());
            planner=new MissionPlanner(catalog);
        }
    }
    private InputStreamReader reader(String resource) {
        return new InputStreamReader(
                getClass().getResourceAsStream(resource), StandardCharsets.UTF_8);
    }
    private MissionPlanner.Profile profile(List<Integer> week) {
        Set<String> equipment=new HashSet<>(Arrays.asList(
                "sin_equipo","silla","pared","colchoneta","escalon"));
        return new MissionPlanner.Profile("principiante","bienestar",week,equipment,3);
    }

    @Test public void restDayIsNotAutomaticallyReplacedWithActivity() {
        MissionPlanner.Proposal p=planner.daily(
                profile(Arrays.asList(5,0,1,2,3,4,5)),monday);
        assertTrue(p.empty());
        assertEquals(monday.plusDays(1),p.untilExclusive());
        assertTrue(p.explanation().contains("descanso"));
    }

    @Test public void dailyCandidatesComeFromActualCatalogAndAreConservative() {
        MissionPlanner.Profile p=profile(Arrays.asList(0,1,2,3,4,5,5));
        MissionPlanner.Proposal one=planner.daily(p,monday);
        MissionPlanner.Proposal again=planner.daily(p,monday);
        assertFalse(one.empty());
        assertEquals(one.candidates().size(),again.candidates().size());
        assertEquals(one.candidates().get(0).id(),again.candidates().get(0).id());
        assertTrue(one.candidates().size()<=3);
        for(ExerciseVariant variant:one.candidates()) {
            assertTrue(variant.difficulty()<=1);
            assertFalse(variant.supervisionRecommended());
            assertTrue(p.equipment.containsAll(variant.equipment()));
        }
        assertFalse(one.requiresConsent());
    }

    @Test public void monarchFiveDayPreviewHasExactlyFiveSafeCandidates() {
        MissionPlanner.Proposal p=planner.monarch(
                profile(Arrays.asList(0,1,2,3,4,5,5)),monday);
        assertEquals(MissionPlanner.MONARCH,p.kind());
        assertEquals(monday.plusDays(5),p.untilExclusive());
        assertTrue(p.requiresConsent());
        assertEquals(5,p.candidates().size());
        for(ExerciseVariant v:p.candidates()) {
            assertTrue(v.difficulty()<=1);
            assertFalse(v.supervisionRecommended());
        }
    }

    @Test public void monarchDoesNotOverrideRestOnlyCalendar() {
        MissionPlanner.Proposal p=planner.monarch(
                profile(Arrays.asList(5,5,5,5,5,5,5)),monday);
        assertTrue(p.empty());
        assertTrue(p.requiresConsent());
    }

    @Test public void missingEquipmentPreventsUnsafeAssumptions() {
        MissionPlanner.Profile p=new MissionPlanner.Profile(
                "principiante","movilidad",Arrays.asList(0,0,0,0,0,0,0),
                Set.of(),2);
        assertTrue(planner.daily(p,monday).empty());
        assertTrue(planner.monarch(p,monday).empty());
    }

    @Test public void inputIsImmutableAndDailyLimitBounded() {
        MissionPlanner.Profile p=new MissionPlanner.Profile(
                "avanzado","bienestar",Arrays.asList(0,1,2,3,4,5,5),
                Set.of("sin_equipo"),100);
        assertEquals(7,p.dailyLimit);
        assertThrows(UnsupportedOperationException.class,()->p.weekly.set(0,5));
    }
}
