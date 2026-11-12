package pe.asterionsystem.core.engine.progression;

import static org.junit.Assert.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalogLoader;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;

public final class TimedVolumeProgressionTest {
    private ExerciseCatalog catalog;
    private ExerciseVolumeProfileCatalog profiles;
    private final LocalDate day=LocalDate.of(2026,11,23);

    private InputStreamReader reader(String name) {
        return new InputStreamReader(getClass().getResourceAsStream(
                "/asterion_exercises/v1/"+name),StandardCharsets.UTF_8);
    }

    @Before public void setup() throws Exception {
        try(InputStreamReader f=reader("families.tsv");
            InputStreamReader t=reader("types.tsv");
            InputStreamReader v=reader("variants.tsv");
            InputStreamReader p=reader("volume_profiles.tsv")) {
            catalog=ExerciseCatalogLoader.load(f,t,v);
            profiles=ExerciseVolumeProfileCatalog.load(p,catalog);
        }
    }

    private ExerciseVariant variant(String id) {
        return catalog.variant(id).orElseThrow();
    }

    private VolumeProgressionEngine.Observation obs(ExerciseVariant v,int days,
             int category,int sets,int quantity,int effort,
             ExerciseVolumeProfileCatalog.Unit unit) {
        return new VolumeProgressionEngine.Observation(
                day.minusDays(days),category,v.id(),v.typeId(),
                v.difficulty(),sets,quantity,effort,true,false,unit);
    }

    @Test public void newPlankStartsAtItsOwnSecondsNotOldSquatRepetitions() {
        ExerciseVariant plank=variant("plancha_frontal_v01");
        ExerciseVolumeProfileCatalog.Profile p=profiles.require(plank);
        VolumeProgressionEngine.Suggestion fresh=VolumeProgressionEngine.review(
                plank,1,day,List.of(),false,p);
        assertEquals(ExerciseVolumeProfileCatalog.Unit.SECONDS,fresh.unit);
        assertEquals(p.initialSets,fresh.offeredSets);
        assertEquals(p.initialValue,fresh.offeredQuantity);
        assertEquals(0,fresh.previousSets);
    }

    @Test public void firstEasyTimedSessionAdjustsSecondsNotRepetitions() {
        ExerciseVariant plank=variant("plancha_frontal_v01");
        ExerciseVolumeProfileCatalog.Profile p=profiles.require(plank);
        VolumeProgressionEngine.Suggestion s=VolumeProgressionEngine.review(
                plank,1,day,List.of(obs(plank,1,1,1,p.initialValue,2,p.unit)),
                false,p);
        assertEquals(ExerciseVolumeProfileCatalog.Unit.SECONDS,s.unit);
        assertEquals(VolumeProgressionEngine.Status.CALCULATED_INCREASE,s.status);
        assertEquals(p.initialValue+3*p.increment,s.offeredQuantity);
    }

    @Test public void walkingUsesItsOwnSecondIncrements() {
        ExerciseVariant walk=variant("marcha_v02");
        ExerciseVolumeProfileCatalog.Profile p=profiles.require(walk);
        VolumeProgressionEngine.Suggestion s=VolumeProgressionEngine.review(
                walk,4,day,List.of(obs(walk,1,4,1,p.initialValue,1,p.unit)),
                false,p);
        assertEquals(ExerciseVolumeProfileCatalog.Unit.SECONDS,s.unit);
        assertEquals(p.initialValue+5*p.increment,s.offeredQuantity);
    }

    @Test public void veryHardTimedActivityReducesSeconds() {
        ExerciseVariant walk=variant("marcha_v02");
        ExerciseVolumeProfileCatalog.Profile p=profiles.require(walk);
        VolumeProgressionEngine.Suggestion s=VolumeProgressionEngine.review(
                walk,4,day,List.of(obs(walk,1,4,1,p.initialValue,9,p.unit)),
                false,p);
        assertEquals(VolumeProgressionEngine.Status.CALCULATED_DECREASE,s.status);
        assertEquals(p.initialValue-2*p.increment,s.offeredQuantity);
    }

    @Test public void oldRepetitionLogCannotBecomeTimedHistory() {
        ExerciseVariant plank=variant("plancha_frontal_v01");
        ExerciseVolumeProfileCatalog.Profile p=profiles.require(plank);
        VolumeProgressionEngine.Observation old=
                new VolumeProgressionEngine.Observation(
                        day.minusDays(1),1,plank.id(),plank.typeId(),
                        plank.difficulty(),2,8,2,true,false);
        VolumeProgressionEngine.Suggestion s=VolumeProgressionEngine.review(
                plank,1,day,List.of(old),false,p);
        assertEquals(VolumeProgressionEngine.Status.NO_HISTORY,s.status);
        assertEquals(p.initialValue,s.offeredQuantity);
    }

    @Test public void sameSecondsReferenceSurvivesModerateEffort() {
        ExerciseVariant plank=variant("plancha_frontal_v01");
        ExerciseVolumeProfileCatalog.Profile p=profiles.require(plank);
        VolumeProgressionEngine.Suggestion s=VolumeProgressionEngine.review(
                plank,1,day,List.of(obs(plank,1,1,1,12,6,p.unit)),
                false,p);
        assertEquals(VolumeProgressionEngine.Status.MAINTAIN,s.status);
        assertEquals(12,s.offeredQuantity);
    }

    @Test public void threeEasyTimedSessionsCanReviewOneAdditionalSeries() {
        ExerciseVariant plank=variant("plancha_frontal_v01");
        ExerciseVolumeProfileCatalog.Profile p=profiles.require(plank);
        VolumeProgressionEngine.Suggestion s=VolumeProgressionEngine.review(
                plank,1,day,List.of(
                        obs(plank,1,1,1,15,1,p.unit),
                        obs(plank,5,1,1,13,2,p.unit),
                        obs(plank,9,1,1,10,3,p.unit)),false,p);
        assertEquals(VolumeProgressionEngine.Status.CALCULATED_EXTRA_SERIES,s.status);
        assertEquals(2,s.offeredSets);
        assertEquals(15,s.offeredQuantity);
    }

    @Test public void hardEffortCannotRaiseAnActuallyLowerTimedEntry() {
        ExerciseVariant walk=variant("marcha_v02");
        ExerciseVolumeProfileCatalog.Profile p=profiles.require(walk);
        // Actual low volume can be below the configured display minimum.
        VolumeProgressionEngine.Suggestion s=VolumeProgressionEngine.review(
                walk,4,day,List.of(obs(walk,1,4,1,5,10,p.unit)),false,p);
        assertEquals(VolumeProgressionEngine.Status.CALCULATED_DECREASE,s.status);
        assertEquals(5,s.offeredQuantity);
    }

    @Test public void limitsApplySeparatelyToSeconds() {
        ExerciseVariant plank=variant("plancha_frontal_v01");
        ExerciseVolumeProfileCatalog.Profile p=profiles.require(plank);
        VolumeProgressionEngine.Suggestion s=VolumeProgressionEngine.review(
                plank,1,day,List.of(obs(plank,1,1,1,p.maximumValue,1,p.unit)),
                false,p);
        assertEquals(p.maximumValue,s.offeredQuantity);
        assertEquals(VolumeProgressionEngine.Status.REVIEW_SERIES_WITH_GUIDANCE,s.status);
    }

    @Test public void manualOnlyTechnicalVariantDoesNotProgressAutomatically() {
        ExerciseVariant exercise=variant("sentadilla_bilateral_v07");
        ExerciseVolumeProfileCatalog.Profile p=profiles.require(exercise);
        assertFalse(p.automaticEligible);
        VolumeProgressionEngine.Suggestion s=VolumeProgressionEngine.review(
                exercise,2,day,List.of(
                        obs(exercise,1,2,1,p.initialValue,1,p.unit)),false,p);
        assertEquals(VolumeProgressionEngine.Status.MAINTAIN,s.status);
    }
}
