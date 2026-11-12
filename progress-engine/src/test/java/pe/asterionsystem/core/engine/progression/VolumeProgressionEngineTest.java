package pe.asterionsystem.core.engine.progression;

import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.List;
import org.junit.Test;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;

public class VolumeProgressionEngineTest {
    private final ExerciseVariant squat = new ExerciseVariant(
            "sentadilla_bilateral_v03", "sentadilla_bilateral", "Sentadilla libre",
            2, "sin_equipo", "bilateral", false);
    private final ExerciseVariant other = new ExerciseVariant(
            "sentadilla_bilateral_v02", "sentadilla_bilateral", "Sentadilla con apoyo",
            1, "pared", "asistido", false);
    private final LocalDate today = LocalDate.of(2026, 11, 23);

    private VolumeProgressionEngine.Observation entry(int daysBefore, int category,
                                                       int sets, int reps, int effort,
                                                       boolean completed) {
        return new VolumeProgressionEngine.Observation(
                today.minusDays(daysBefore), category, squat.id(), squat.typeId(),
                squat.difficulty(), sets, reps, effort, completed, false);
    }

    @Test public void noHistoryNeverInventsTargets() {
        VolumeProgressionEngine.Suggestion s = VolumeProgressionEngine.review(
                squat, 2, today, List.of(), false);
        assertEquals(VolumeProgressionEngine.Status.NO_HISTORY, s.status);
        assertEquals(0, s.offeredRepetitions);
    }

    @Test public void oneEasyDayDoesNotIncreaseAnything() {
        VolumeProgressionEngine.Suggestion s = VolumeProgressionEngine.review(
                squat, 2, today, List.of(entry(1,2,2,8,2,true)), false);
        assertEquals(VolumeProgressionEngine.Status.MAINTAIN, s.status);
        assertEquals(2, s.offeredSets);
        assertEquals(8, s.offeredRepetitions);
    }

    @Test public void twoEasyCompletedAndSeparatedSessionsOfferOneRepOnly() {
        VolumeProgressionEngine.Suggestion s = VolumeProgressionEngine.review(
                squat, 2, today, List.of(
                        entry(1,2,2,8,2,true),
                        entry(9,2,2,8,3,true)), false);
        assertEquals(VolumeProgressionEngine.Status.OPTIONAL_ONE_REPETITION, s.status);
        assertTrue(s.requiresConsent());
        assertEquals(2, s.offeredSets);
        assertEquals(9, s.offeredRepetitions);
    }

    @Test public void sessionsOneDayApartDoNotQualify() {
        VolumeProgressionEngine.Suggestion s = VolumeProgressionEngine.review(
                squat,2,today,List.of(entry(1,2,2,8,2,true),entry(2,2,2,8,2,true)),false);
        assertEquals(VolumeProgressionEngine.Status.MAINTAIN,s.status);
    }

    @Test public void incompleteWorkCannotProveReadiness() {
        VolumeProgressionEngine.Suggestion s = VolumeProgressionEngine.review(
                squat,2,today,List.of(entry(1,2,2,8,2,false),entry(9,2,2,8,2,true)),false);
        assertEquals(VolumeProgressionEngine.Status.MAINTAIN,s.status);
    }

    @Test public void highEffortPreventsIncreasingVolume() {
        VolumeProgressionEngine.Suggestion s = VolumeProgressionEngine.review(
                squat,2,today,List.of(entry(1,2,2,8,9,true),entry(9,2,2,8,2,true)),false);
        assertEquals(VolumeProgressionEngine.Status.RECOVERY_PRIORITY,s.status);
        assertFalse(s.requiresConsent());
    }

    @Test public void noCrossCategoryLeakage() {
        assertEquals(0, VolumeProgressionEngine.latestCategoryEffort(
                0,today,List.of(entry(1,2,2,8,9,true))));
        assertEquals(9, VolumeProgressionEngine.latestCategoryEffort(
                2,today,List.of(entry(1,2,2,8,9,true))));
    }

    @Test public void noCrossVariantVolumeTransferEvenSameType() {
        VolumeProgressionEngine.Suggestion s = VolumeProgressionEngine.review(
                other,2,today,List.of(entry(1,2,2,8,2,true),entry(9,2,2,8,2,true)),false);
        assertEquals(VolumeProgressionEngine.Status.NO_HISTORY,s.status);
        assertEquals(0,s.offeredSets);
    }

    @Test public void recentInterruptionForcesPause() {
        VolumeProgressionEngine.Suggestion s = VolumeProgressionEngine.review(
                squat,2,today,List.of(entry(1,2,2,8,2,true),entry(9,2,2,8,2,true)),true);
        assertEquals(VolumeProgressionEngine.Status.PAUSED,s.status);
    }

    @Test public void highRepetitionsDoNotTriggerMoreSetsAutomatically() {
        VolumeProgressionEngine.Suggestion s = VolumeProgressionEngine.review(
                squat,2,today,List.of(entry(1,2,2,20,2,true),entry(9,2,2,20,2,true)),false);
        assertEquals(VolumeProgressionEngine.Status.REVIEW_SERIES_WITH_GUIDANCE,s.status);
        assertEquals(2,s.offeredSets);
        assertEquals(20,s.offeredRepetitions);
    }

    @Test(expected=IllegalArgumentException.class)
    public void rejectImplausibleVolume() {
        new VolumeProgressionEngine.Observation(
                today,2,squat.id(),squat.typeId(),2,0,8,2,true,false);
    }
}
