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

    @Test public void oneEasyDayAutomaticallyCalculatesNextReference() {
        VolumeProgressionEngine.Suggestion s = VolumeProgressionEngine.review(
                squat, 2, today, List.of(entry(1,2,2,8,2,true)), false);
        assertEquals(VolumeProgressionEngine.Status.CALCULATED_INCREASE, s.status);
        assertEquals(2, s.offeredSets);
        assertEquals(11, s.offeredRepetitions);
    }

    @Test public void twoEasyCompletedSessionsStillOnlyAdjustRepetitions() {
        VolumeProgressionEngine.Suggestion s = VolumeProgressionEngine.review(
                squat, 2, today, List.of(
                        entry(1,2,2,8,2,true),
                        entry(9,2,2,8,3,true)), false);
        assertEquals(VolumeProgressionEngine.Status.CALCULATED_INCREASE, s.status);
        assertTrue(s.requiresConsent());
        assertEquals(2, s.offeredSets);
        assertEquals(11, s.offeredRepetitions);
    }

    @Test public void sessionsOneDayApartDoNotQualify() {
        VolumeProgressionEngine.Suggestion s = VolumeProgressionEngine.review(
                squat,2,today,List.of(entry(1,2,2,8,2,true),entry(2,2,2,8,2,true)),false);
        assertEquals(VolumeProgressionEngine.Status.CALCULATED_INCREASE,s.status);
        assertEquals(2,s.offeredSets);
    }

    @Test public void incompleteWorkCannotProveReadiness() {
        VolumeProgressionEngine.Suggestion s = VolumeProgressionEngine.review(
                squat,2,today,List.of(entry(1,2,2,8,2,false),entry(9,2,2,8,2,true)),false);
        assertEquals(VolumeProgressionEngine.Status.CALCULATED_INCREASE,s.status);
        assertEquals(11,s.offeredRepetitions);
    }

    @Test public void highEffortPreventsIncreasingVolume() {
        VolumeProgressionEngine.Suggestion s = VolumeProgressionEngine.review(
                squat,2,today,List.of(entry(1,2,2,8,9,true),entry(9,2,2,8,2,true)),false);
        assertEquals(VolumeProgressionEngine.Status.CALCULATED_DECREASE,s.status);
        assertEquals(6,s.offeredRepetitions);
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


    @Test public void exactRequestedRepetitionBandsAreDeterministic() {
        assertEquals(5,VolumeProgressionEngine.repetitionDelta(1));
        assertEquals(3,VolumeProgressionEngine.repetitionDelta(2));
        assertEquals(1,VolumeProgressionEngine.repetitionDelta(3));
        for(int e=4;e<=7;e++) assertEquals(0,VolumeProgressionEngine.repetitionDelta(e));
        assertEquals(-1,VolumeProgressionEngine.repetitionDelta(8));
        assertEquals(-2,VolumeProgressionEngine.repetitionDelta(9));
        assertEquals(-3,VolumeProgressionEngine.repetitionDelta(10));
    }

    @Test public void moderateSevenRetainsExactlyTheLastReportedVolume() {
        VolumeProgressionEngine.Suggestion s=VolumeProgressionEngine.review(
                squat,2,today,List.of(entry(1,2,2,8,7,true)),false);
        assertEquals(VolumeProgressionEngine.Status.MAINTAIN,s.status);
        assertEquals(8,s.offeredRepetitions);
    }

    @Test public void eightEffortLowersReferenceByOneWithoutIncreasingSets() {
        VolumeProgressionEngine.Suggestion s=VolumeProgressionEngine.review(
                squat,2,today,List.of(entry(1,2,2,8,8,true)),false);
        assertEquals(VolumeProgressionEngine.Status.CALCULATED_DECREASE,s.status);
        assertEquals(2,s.offeredSets);
        assertEquals(7,s.offeredRepetitions);
    }

    @Test public void threeEasySessionsSeparatedAcrossWeekAllowOneExtraSeries() {
        VolumeProgressionEngine.Suggestion s=VolumeProgressionEngine.review(
                squat,2,today,List.of(entry(1,2,2,10,1,true),
                        entry(5,2,2,9,2,true),entry(9,2,2,8,3,true)),false);
        assertEquals(VolumeProgressionEngine.Status.CALCULATED_EXTRA_SERIES,s.status);
        assertEquals(3,s.offeredSets);
        assertEquals(10,s.offeredRepetitions);
    }

    @Test public void threeConsecutiveDaysCannotEscalateSeriesImmediately() {
        VolumeProgressionEngine.Suggestion s=VolumeProgressionEngine.review(
                squat,2,today,List.of(entry(1,2,2,10,1,true),
                        entry(2,2,2,9,2,true),entry(3,2,2,8,3,true)),false);
        assertEquals(VolumeProgressionEngine.Status.CALCULATED_INCREASE,s.status);
        assertEquals(2,s.offeredSets);
    }

    @Test public void upperLimitsDoNotRequireEndlessEscalation() {
        VolumeProgressionEngine.Suggestion s=VolumeProgressionEngine.review(
                squat,2,today,List.of(entry(1,2,3,19,1,true)),false);
        assertEquals(20,s.offeredRepetitions);
        assertEquals(3,s.offeredSets);
    }

    @Test public void veryHardAtOneRepCannotDecreaseBelowOne() {
        VolumeProgressionEngine.Suggestion s=VolumeProgressionEngine.review(
                squat,2,today,List.of(entry(1,2,2,1,10,true)),false);
        assertEquals(1,s.offeredRepetitions);
    }

    @Test(expected=IllegalArgumentException.class)
    public void rejectImplausibleVolume() {
        new VolumeProgressionEngine.Observation(
                today,2,squat.id(),squat.typeId(),2,0,8,2,true,false);
    }
}
