package pe.asterionsystem.core.engine.progression;

import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.Arrays;
import org.junit.Test;

public class TechnicalCheckpointPolicyTest {
    private final LocalDate first=LocalDate.of(2026,11,1);

    private WeeklyCategoryChallengePolicy.Review review(int category,
            int effort1,int effort2) {
        return WeeklyCategoryChallengePolicy.evaluate(category,first,
                first.plusDays(7),Arrays.asList(
                        new WeeklyCategoryChallengePolicy.Observation(
                                first.plusDays(1),category,effort1,true),
                        new WeeklyCategoryChallengePolicy.Observation(
                                first.plusDays(5),category,effort2,true)));
    }

    @Test public void firstCheckpointNeedsSevenElapsedDays() {
        assertEquals(0,TechnicalCheckpointPolicy.evaluate(2,6,0,
                "intermedio",review(2,2,3),false).checkpoint);
        assertEquals(TechnicalCheckpointPolicy.Action.WAIT,
                TechnicalCheckpointPolicy.evaluate(2,6,0,
                        "intermedio",review(2,2,3),false).action);
    }

    @Test public void sevenDaysCanAdvanceOnlyWithCategoryEvidence() {
        assertTrue(TechnicalCheckpointPolicy.evaluate(2,7,0,
                "intermedio",review(2,2,3),false).mayAdvance());
        assertFalse(TechnicalCheckpointPolicy.evaluate(0,7,0,
                "intermedio",review(2,2,3),false).mayAdvance());
    }

    @Test public void fourteenDaysAdvanceSecondPhaseOnlyIfEligible() {
        TechnicalCheckpointPolicy.Decision d=TechnicalCheckpointPolicy.evaluate(
                2,14,1,"avanzado",review(2,2,3),false);
        assertEquals(14,d.checkpoint);
        assertTrue(d.mayAdvance());
    }

    @Test public void twentyOneDaysRespectSafeTechnicalCeiling() {
        TechnicalCheckpointPolicy.Decision d=TechnicalCheckpointPolicy.evaluate(
                2,21,2,"avanzado",review(2,1,2),false);
        assertEquals(21,d.checkpoint);
        assertEquals(TechnicalCheckpointPolicy.Action.ROTATE_EQUIVALENT,d.action);
        assertFalse(d.mayAdvance());
    }

    @Test public void effortEightPrioritizesEasierReview() {
        TechnicalCheckpointPolicy.Decision d=TechnicalCheckpointPolicy.evaluate(
                2,7,0,"avanzado",review(2,2,8),false);
        assertEquals(TechnicalCheckpointPolicy.Action.REVIEW_EASIER,d.action);
        assertFalse(d.mayAdvance());
    }

    @Test public void beginnerCannotAutomaticallyUnlockAdvancedTechnicalLevel() {
        TechnicalCheckpointPolicy.Decision d=TechnicalCheckpointPolicy.evaluate(
                2,21,0,"principiante",review(2,1,2),false);
        assertEquals(TechnicalCheckpointPolicy.Action.ROTATE_EQUIVALENT,d.action);
    }

    @Test public void pausedCategoryNeverAdvances() {
        assertEquals(TechnicalCheckpointPolicy.Action.REVIEW_EASIER,
                TechnicalCheckpointPolicy.evaluate(
                        2,14,0,"avanzado",review(2,2,3),true).action);
    }

    @Test public void restDayNeverAdvances() {
        assertEquals(TechnicalCheckpointPolicy.Action.WAIT,
                TechnicalCheckpointPolicy.evaluate(
                        5,21,0,"avanzado",null,false).action);
    }
}
