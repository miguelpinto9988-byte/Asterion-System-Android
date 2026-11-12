package pe.asterionsystem.core.engine.progression;

import static org.junit.Assert.*;
import java.util.List;
import org.junit.Test;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;

public class VolumeResetPolicyTest {
    private final ExerciseVariant old = new ExerciseVariant(
            "sentadilla_bilateral_v01", "sentadilla_bilateral", "Sentadilla con silla",
            1, "silla", "controlada", false);
    private final ExerciseVariant newTechnique = new ExerciseVariant(
            "sentadilla_bilateral_v02", "sentadilla_bilateral", "Sentadilla sin silla",
            2, "sin_equipo", "controlada", false);
    private final ExerciseVariant otherExercise = new ExerciseVariant(
            "elevacion_puntas_v01", "elevacion_puntas", "Elevación de puntas",
            1, "sin_equipo", "controlada", false);

    @Test public void daySevenChangedExerciseResetsOnlyNewVariant() {
        VolumeResetPolicy.Decision reset = VolumeResetPolicy.evaluate(
                0, 7, List.of(VolumeResetPolicy.selectionKey(old)),
                List.of(old, otherExercise));
        assertFalse(reset.restarted(old.id()));
        assertTrue(reset.restarted(otherExercise.id()));
        assertEquals(1, reset.restartVariantIds.size());
    }

    @Test public void dayFourteenChangedTechniqueStartsFreshVolumeEpoch() {
        VolumeResetPolicy.Decision reset = VolumeResetPolicy.evaluate(
                7,14,List.of(VolumeResetPolicy.selectionKey(old)),
                List.of(newTechnique));
        assertTrue(reset.restarted(newTechnique.id()));
        assertFalse(reset.restarted(old.id()));
    }

    @Test public void dayTwentyOneAlsoResetsAPreviouslyUsedVariantWhenReintroduced() {
        VolumeResetPolicy.Decision reset = VolumeResetPolicy.evaluate(
                14,21,List.of(VolumeResetPolicy.selectionKey(otherExercise)),
                List.of(old));
        // Old logs for "old" may exist: the transition still invalidates them.
        assertTrue(reset.restarted(old.id()));
    }

    @Test public void unchangedTechniqueDoesNotLoseItsVolumeHistory() {
        for(int day:new int[]{7,14,21}) {
            int previous=day==7?0:day-7;
            VolumeResetPolicy.Decision result=VolumeResetPolicy.evaluate(
                    previous,day,List.of(VolumeResetPolicy.selectionKey(old)),
                    List.of(old));
            assertTrue(result.restartVariantIds.isEmpty());
        }
    }

    @Test public void betweenMilestonesNewVariantDoesNotFakeCheckpoint() {
        VolumeResetPolicy.Decision result=VolumeResetPolicy.evaluate(
                7,7,List.of(VolumeResetPolicy.selectionKey(old)),
                List.of(newTechnique));
        assertTrue(result.restartVariantIds.isEmpty());
    }

    @Test public void screenReopenCannotRestartVolumeTwice() {
        VolumeResetPolicy.Decision result=VolumeResetPolicy.evaluate(
                14,14,List.of(VolumeResetPolicy.selectionKey(old)),
                List.of(old,otherExercise));
        assertTrue(result.restartVariantIds.isEmpty());
    }

    @Test public void firstObservedCheckpointWithoutPreviousSelectionResetsRatherThanCarries() {
        VolumeResetPolicy.Decision result=VolumeResetPolicy.evaluate(
                0,7,List.of(),List.of(old));
        assertTrue(result.restarted(old.id()));
    }

    @Test public void snapshotUsesBothExerciseIdentityAndTechnicalLevel() {
        assertNotEquals(VolumeResetPolicy.selectionKey(old),
                VolumeResetPolicy.selectionKey(newTechnique));
        assertEquals(List.of(VolumeResetPolicy.selectionKey(old)),
                VolumeResetPolicy.snapshot(List.of(old)));
    }

    @Test(expected=IllegalArgumentException.class)
    public void arbitraryDayIsNotAReviewCheckpoint() {
        VolumeResetPolicy.evaluate(0,8,List.of(),List.of(old));
    }
}
