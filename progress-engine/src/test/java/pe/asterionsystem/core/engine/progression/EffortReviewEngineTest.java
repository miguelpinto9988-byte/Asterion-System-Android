package pe.asterionsystem.core.engine.progression;

import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class EffortReviewEngineTest {
    private final LocalDate dayOne=LocalDate.of(2026,11,1);

    @Test public void checkpointsOccurAtDaysSevenFourteenAndTwentyOne() {
        EffortReviewEngine.Observation r=new EffortReviewEngine.Observation(dayOne,4);
        assertEquals(0,EffortReviewEngine.review(dayOne,dayOne.plusDays(5),Arrays.asList(r)).checkpoint);
        assertEquals(7,EffortReviewEngine.review(dayOne,dayOne.plusDays(6),Arrays.asList(r)).checkpoint);
        assertEquals(14,EffortReviewEngine.review(dayOne,dayOne.plusDays(13),Arrays.asList(r)).checkpoint);
        assertEquals(21,EffortReviewEngine.review(dayOne,dayOne.plusDays(20),Arrays.asList(r)).checkpoint);
        assertEquals(1,EffortReviewEngine.review(dayOne,dayOne.plusDays(21),Arrays.asList(r)).dayInCycle);
    }

    @Test public void noEvidenceNeverInventsPerformance() {
        EffortReviewEngine.Review missing=EffortReviewEngine.review(null,dayOne,Collections.emptyList());
        assertTrue(Double.isNaN(missing.averageEffort));
        assertEquals(0,missing.observationCount);
        assertTrue(missing.recommendation.contains("Sin datos"));
    }

    @Test public void highEffortCanSuggestReducingAtDayTwentyOne() {
        EffortReviewEngine.Review review=EffortReviewEngine.review(dayOne,dayOne.plusDays(20),
                Arrays.asList(new EffortReviewEngine.Observation(dayOne.plusDays(19),9)));
        assertEquals(21,review.checkpoint);
        assertTrue(review.recommendation.contains("descanso"));
        assertTrue(review.recommendation.contains("no se aumentará"));
    }

    @Test public void lowEffortDoesNotTriggerAutomaticIncrease() {
        EffortReviewEngine.Review review=EffortReviewEngine.review(dayOne,dayOne.plusDays(6),
                Arrays.asList(new EffortReviewEngine.Observation(dayOne.plusDays(6),2)));
        assertTrue(review.recommendation.contains("sin aumentar automáticamente"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void effortMustBeWithinScale() {
        new EffortReviewEngine.Observation(dayOne,11);
    }

    @Test public void onlyRecentReportsCountForReview() {
        EffortReviewEngine.Review review=EffortReviewEngine.review(dayOne,dayOne.plusDays(14),
                Arrays.asList(
                        new EffortReviewEngine.Observation(dayOne,10),
                        new EffortReviewEngine.Observation(dayOne.plusDays(14),3)));
        assertEquals(1,review.observationCount);
        assertEquals(3.0,review.averageEffort,0.001);
    }
}
