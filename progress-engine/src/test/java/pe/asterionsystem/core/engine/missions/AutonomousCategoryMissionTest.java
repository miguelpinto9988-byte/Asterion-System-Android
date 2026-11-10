package pe.asterionsystem.core.engine.missions;

import static org.junit.Assert.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalogLoader;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;
import pe.asterionsystem.core.engine.progression.CategoryEffortPolicy;

/** Uses actual saved catalog records, not invented exercise fixtures. */
public class AutonomousCategoryMissionTest {
    private final LocalDate monday=LocalDate.of(2026,11,9);
    private MissionPlanner planner;
    private MissionPlanner.Profile profile;

    @Before public void loadActualTaxonomy() throws Exception {
        String prefix="/asterion_exercises/v1/";
        try (InputStreamReader family=resource(prefix+"families.tsv");
             InputStreamReader types=resource(prefix+"types.tsv");
             InputStreamReader variants=resource(prefix+"variants.tsv")) {
            ExerciseCatalog catalog=ExerciseCatalogLoader.load(family,types,variants);
            assertEquals(445,catalog.variants().size());
            planner=new MissionPlanner(catalog);
        }
        profile=new MissionPlanner.Profile("principiante","bienestar",
                Arrays.asList(2,0,2,1,3,4,5),
                new HashSet<>(Arrays.asList("sin_equipo","silla","pared","colchoneta")),
                3);
    }

    private InputStreamReader resource(String path) {
        return new InputStreamReader(getClass().getResourceAsStream(path),
                StandardCharsets.UTF_8);
    }

    @Test public void firstWeekLowEffortDoesNotForceAutomaticRotation() {
        CategoryEffortPolicy.Decision low=CategoryEffortPolicy.evaluate(2,3,3);
        MissionPlanner.Proposal first=planner.daily(profile,monday,low,
                Collections.singletonList("sentadilla_bilateral_v01"),false);
        assertEquals("sentadilla_bilateral_v01",first.candidates().get(0).id());
        MissionPlanner.Proposal afterReview=planner.daily(profile,monday,low,
                Collections.singletonList("sentadilla_bilateral_v01"),true);
        assertEquals("sentadilla_bilateral_v02",afterReview.candidates().get(0).id());
    }

    @Test public void lowEffortChangesVariantAutomaticallyWithinSameTypeAndLevel() {
        CategoryEffortPolicy.Decision low=CategoryEffortPolicy.evaluate(2,3,3);
        MissionPlanner.Proposal proposal=planner.daily(profile,monday,low,
                Collections.singletonList("sentadilla_bilateral_v01"));
        assertFalse(proposal.empty());
        assertEquals("sentadilla_bilateral_v02",proposal.candidates().get(0).id());
        assertEquals(1,proposal.candidates().get(0).difficulty());
        assertTrue(profile.equipment.containsAll(proposal.candidates().get(0).equipment()));
        assertFalse(proposal.requiresConsent());
    }

    @Test public void moderateEffortKeepsSameVariantWithoutUserSelection() {
        CategoryEffortPolicy.Decision moderate=CategoryEffortPolicy.evaluate(2,5,3);
        MissionPlanner.Proposal proposal=planner.daily(profile,monday,moderate,
                Collections.singletonList("sentadilla_bilateral_v01"));
        assertEquals("sentadilla_bilateral_v01",proposal.candidates().get(0).id());
    }

    @Test public void highEffortAutomaticallyReducesActivitiesAndTechnicalLevel() {
        CategoryEffortPolicy.Decision high=CategoryEffortPolicy.evaluate(2,10,3);
        MissionPlanner.Proposal proposal=planner.daily(profile,monday,high,
                Collections.singletonList("sentadilla_bilateral_v01"));
        assertTrue(proposal.candidates().size()<=1);
        for(ExerciseVariant v:proposal.candidates()) {
            assertTrue(v.difficulty()<=1);
            assertFalse(v.supervisionRecommended());
        }
    }

    @Test public void cannotApplyLowerBodyEffortToUpperBodyDay() {
        CategoryEffortPolicy.Decision lower=CategoryEffortPolicy.evaluate(2,3,3);
        try {
            planner.daily(profile,monday.plusDays(1),lower,
                    Collections.singletonList("sentadilla_bilateral_v01"));
            fail("A report from another category must be rejected");
        } catch(IllegalArgumentException correct) {
            assertTrue(correct.getMessage().contains("categoría"));
        }
    }

    @Test public void restDayIsNotConvertedIntoAnExercise() {
        CategoryEffortPolicy.Decision rest=CategoryEffortPolicy.evaluate(5,0,3);
        assertTrue(planner.daily(profile,monday.plusDays(6),rest,
                Collections.emptyList()).empty());
    }

    @Test public void recommendationsAreDeterministicWithSameHistory() {
        CategoryEffortPolicy.Decision low=CategoryEffortPolicy.evaluate(2,3,3);
        List<String> previous=Collections.singletonList("sentadilla_bilateral_v01");
        MissionPlanner.Proposal a=planner.daily(profile,monday,low,previous);
        MissionPlanner.Proposal b=planner.daily(profile,monday,low,previous);
        assertEquals(a.candidates().size(),b.candidates().size());
        for(int i=0;i<a.candidates().size();i++) {
            assertEquals(a.candidates().get(i).id(),b.candidates().get(i).id());
        }
    }
}
