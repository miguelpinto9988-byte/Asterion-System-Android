package pe.asterionsystem.core.engine.progression;

import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.Collections;
import org.junit.Test;

public class EffortAdaptationPolicyTest {
    private final LocalDate start=LocalDate.of(2026,11,1);

    private EffortReviewEngine.Review report(int rating,int day) {
        return EffortReviewEngine.review(start,start.plusDays(day-1),
                Collections.singletonList(new EffortReviewEngine.Observation(
                        start.plusDays(day-1),rating)));
    }

    @Test public void emptyReportsPreserveOriginalLimit() {
        assertEquals(4,EffortAdaptationPolicy.dailySuggestionCount(4,
                EffortReviewEngine.review(null,start,Collections.emptyList())));
    }
    @Test public void highEffortReducesActivityCount() {
        assertEquals(2,EffortAdaptationPolicy.dailySuggestionCount(5,report(7,7)));
        assertEquals(1,EffortAdaptationPolicy.dailySuggestionCount(5,report(9,14)));
    }
    @Test public void thirdReviewMayStillReduceActivities() {
        assertEquals(1,EffortAdaptationPolicy.dailySuggestionCount(5,report(10,21)));
    }
    @Test public void lowEffortNeverIncreasesActivityCount() {
        assertEquals(3,EffortAdaptationPolicy.dailySuggestionCount(3,report(2,7)));
    }
    @Test public void alreadyMinimalCountDoesNotFallBelowOne() {
        assertEquals(1,EffortAdaptationPolicy.dailySuggestionCount(1,report(10,21)));
    }
}
