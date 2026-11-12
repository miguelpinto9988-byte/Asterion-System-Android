package pe.asterionsystem.core.engine.progression;

import static org.junit.Assert.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalogLoader;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;
import pe.asterionsystem.core.engine.missions.MissionPlanner;

public class CoordinatedTrainingEngineTest {
    private final LocalDate monday = LocalDate.of(2026, 11, 23);
    private MissionPlanner.Profile profile;
    private MissionPlanner planner;

    private InputStreamReader reader(String file) {
        return new InputStreamReader(getClass().getResourceAsStream(
                "/asterion_exercises/v1/"+file),StandardCharsets.UTF_8);
    }

    @Before public void setup() throws Exception {
        try (InputStreamReader f=reader("families.tsv");
             InputStreamReader t=reader("types.tsv");
             InputStreamReader v=reader("variants.tsv")) {
            ExerciseCatalog catalog = ExerciseCatalogLoader.load(f,t,v);
            assertEquals(1257,catalog.variants().size());
            planner = new MissionPlanner(catalog);
        }
        profile = new MissionPlanner.Profile("principiante","bienestar",
                Arrays.asList(2,2,2,2,2,5,5),
                Set.of("sin_equipo","silla","pared","colchoneta","escalon"),3);
    }

    @Test public void highVolumeEffortFeedsBackIntoSelection() {
        VolumeProgressionEngine.Observation past = new VolumeProgressionEngine.Observation(
                monday.minusDays(1),2,"sentadilla_bilateral_v03",
                "sentadilla_bilateral",2,2,8,9,true,false);
        CoordinatedTrainingEngine.Plan joined = new CoordinatedTrainingEngine(planner)
                .daily(profile,monday,2,List.of(),List.of(),false,0,List.of(past),false);
        assertEquals(CategoryEffortPolicy.Guidance.EASIER,joined.effortDecision.guidance);
        assertEquals(1,joined.mission.candidates().size());
        assertTrue(joined.mission.candidates().get(0).difficulty()<=1);
        assertEquals(1,joined.volumeByVariant.size());
    }

    @Test public void selectedVariantFeedsBackIntoVolumeComparison() {
        CoordinatedTrainingEngine engine=new CoordinatedTrainingEngine(planner);
        CoordinatedTrainingEngine.Plan first=engine.daily(
                profile,monday,0,List.of(),List.of(),false,0,List.of(),false);
        assertEquals(3,first.mission.candidates().size());
        for (ExerciseVariant exercise:first.mission.candidates()) {
            VolumeProgressionEngine.Suggestion s=first.volumeByVariant.get(exercise.id());
            assertNotNull(s);
            assertEquals(VolumeProgressionEngine.Status.NO_HISTORY,s.status);
        }
    }

    @Test public void interruptionDisallowsProgressionAndSimplifiesProposal() {
        CoordinatedTrainingEngine.Plan joined=new CoordinatedTrainingEngine(planner)
                .daily(profile,monday,0,List.of(),List.of(),true,0,List.of(),true);
        assertEquals(1,joined.mission.candidates().size());
        for (VolumeProgressionEngine.Suggestion s:joined.volumeByVariant.values()) {
            assertEquals(VolumeProgressionEngine.Status.PAUSED,s.status);
        }
    }


    @Test public void completedVolumeFeedbackCanKeepAComparableVariant() {
        ExerciseVariant previouslySelected=planner.daily(profile,monday)
                .candidates().get(0);
        VolumeProgressionEngine.Observation logged=new VolumeProgressionEngine.Observation(
                monday.minusDays(3),2,previouslySelected.id(),
                previouslySelected.typeId(),previouslySelected.difficulty(),
                2,8,5,true,false);
        CoordinatedTrainingEngine.Plan joined=new CoordinatedTrainingEngine(planner)
                .daily(profile,monday,5,List.of(previouslySelected.id()),
                        List.of(previouslySelected.id()),false,0,List.of(logged),false);
        assertTrue(joined.mission.candidates().stream().anyMatch(
                v->v.id().equals(previouslySelected.id())));
    }


    @Test public void postCheckpointRecalculationDoesNotInheritOldVariantVolume() {
        CoordinatedTrainingEngine engine=new CoordinatedTrainingEngine(planner);
        CoordinatedTrainingEngine.Plan selected=engine.daily(
                profile,monday,0,List.of(),List.of(),false,0,List.of(),false);
        ExerciseVariant same=selected.mission.candidates().get(0);
        VolumeProgressionEngine.Observation old=new VolumeProgressionEngine.Observation(
                monday.minusDays(3),2,same.id(),same.typeId(),
                same.difficulty(),2,8,2,true,false);
        assertEquals(VolumeProgressionEngine.Status.CALCULATED_INCREASE,
                CoordinatedTrainingEngine.reviewSelected(
                        selected.mission,2,monday,List.of(old),false)
                        .get(same.id()).status);
        // The checkpoint store exposes NO old numeric observations after reset.
        assertEquals(VolumeProgressionEngine.Status.NO_HISTORY,
                CoordinatedTrainingEngine.reviewSelected(
                        selected.mission,2,monday,List.of(),false)
                        .get(same.id()).status);
    }


    @Test public void coordinatorUsesUnitAndStartingReferenceOfEachRealSelectedVariant() throws Exception {
        ExerciseCatalog library;
        ExerciseVolumeProfileCatalog profiles;
        try(InputStreamReader families=reader("families.tsv");
            InputStreamReader types=reader("types.tsv");
            InputStreamReader variants=reader("variants.tsv");
            InputStreamReader volume=reader("volume_profiles.tsv")) {
            library=ExerciseCatalogLoader.load(families,types,variants);
            profiles=ExerciseVolumeProfileCatalog.load(volume,library);
        }
        MissionPlanner.Profile cardio=new MissionPlanner.Profile(
                "principiante","bienestar",
                Arrays.asList(4,4,4,4,4,5,5),Set.of("sin_equipo","silla","pared"),3);
        CoordinatedTrainingEngine.Plan plan=new CoordinatedTrainingEngine(
                new MissionPlanner(library)).daily(
                        cardio,monday,0,List.of(),List.of(),false,0,
                        List.of(),false,profiles);
        assertFalse(plan.mission.empty());
        for(ExerciseVariant variant:plan.mission.candidates()) {
            ExerciseVolumeProfileCatalog.Profile own=profiles.require(variant);
            VolumeProgressionEngine.Suggestion s=
                    plan.volumeByVariant.get(variant.id());
            assertEquals("No generic unit may override real catalog metadata",
                    own.unit,s.unit);
            assertEquals(own.initialSets,s.offeredSets);
            assertEquals(own.initialValue,s.offeredQuantity);
        }
    }

    @Test public void restDayCannotAcquireExercisesFromVolumeHistory() {
        MissionPlanner.Profile rest = new MissionPlanner.Profile("avanzado","bienestar",
                Arrays.asList(5,5,5,5,5,5,5),Set.of("sin_equipo"),3);
        CoordinatedTrainingEngine.Plan joined=new CoordinatedTrainingEngine(planner)
                .daily(rest,monday,0,List.of(),List.of(),true,0,List.of(),false);
        assertTrue(joined.mission.empty());
        assertTrue(joined.volumeByVariant.isEmpty());
    }
}
