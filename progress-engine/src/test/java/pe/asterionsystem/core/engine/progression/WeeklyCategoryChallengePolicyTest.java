package pe.asterionsystem.core.engine.progression;

import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class WeeklyCategoryChallengePolicyTest {
    private final LocalDate monday=LocalDate.of(2026,11,2);
    private WeeklyCategoryChallengePolicy.Observation report(int day,int category,
                                                          int effort,boolean finished) {
        return new WeeklyCategoryChallengePolicy.Observation(
                monday.plusDays(day-1),category,effort,finished);
    }

    @Test public void reviewDoesNotRunUntilSevenDaysElapsed() {
        List<WeeklyCategoryChallengePolicy.Observation> reports=Arrays.asList(
                report(1,0,3,true),report(3,0,2,true));
        assertEquals(WeeklyCategoryChallengePolicy.Status.WAITING_FOR_CHECKPOINT,
                WeeklyCategoryChallengePolicy.evaluate(0,monday,monday.plusDays(6),reports).status);
    }

    @Test public void twoCompletedEasyUpperSessionsTriggerAutomaticVarietyAfterWeek() {
        List<WeeklyCategoryChallengePolicy.Observation> reports=Arrays.asList(
                report(1,0,3,true),report(3,0,2,true),report(2,2,10,true));
        WeeklyCategoryChallengePolicy.Review result=WeeklyCategoryChallengePolicy.evaluate(
                0,monday,monday.plusDays(7),reports);
        assertTrue(result.rotateEquivalentVariant());
        assertEquals(2,result.completedRatings);
        assertEquals(0,result.highRatings);
    }

    @Test public void lowerBodyFeedbackDoesNotInfluenceUpperBody() {
        List<WeeklyCategoryChallengePolicy.Observation> reports=Arrays.asList(
                report(1,0,3,true),report(3,0,3,true),report(5,0,10,true),
                report(2,2,2,true),report(4,2,2,true));
        assertEquals(WeeklyCategoryChallengePolicy.Status.RECOVERY_PRIORITY,
                WeeklyCategoryChallengePolicy.evaluate(0,monday,monday.plusDays(7),reports).status);
        assertTrue(WeeklyCategoryChallengePolicy.evaluate(
                2,monday,monday.plusDays(7),reports).rotateEquivalentVariant());
    }

    @Test public void severeUnfinishedReportAlwaysBlocksNovelty() {
        List<WeeklyCategoryChallengePolicy.Observation> reports=Arrays.asList(
                report(1,0,2,true),report(3,0,3,true),report(5,0,10,false));
        assertEquals(WeeklyCategoryChallengePolicy.Status.RECOVERY_PRIORITY,
                WeeklyCategoryChallengePolicy.evaluate(0,monday,monday.plusDays(7),reports).status);
    }

    @Test public void incompleteActivitiesCannotTriggerProgression() {
        List<WeeklyCategoryChallengePolicy.Observation> reports=Arrays.asList(
                report(1,0,1,false),report(3,0,2,true));
        assertEquals(WeeklyCategoryChallengePolicy.Status.INSUFFICIENT_EVIDENCE,
                WeeklyCategoryChallengePolicy.evaluate(0,monday,monday.plusDays(7),reports).status);
    }

    @Test public void mediumRatingsKeepPlanStable() {
        List<WeeklyCategoryChallengePolicy.Observation> reports=Arrays.asList(
                report(1,0,3,true),report(3,0,5,true));
        assertEquals(WeeklyCategoryChallengePolicy.Status.KEEP_STABLE,
                WeeklyCategoryChallengePolicy.evaluate(0,monday,monday.plusDays(7),reports).status);
    }

    @Test public void missingDataKeepsExistingPlan() {
        assertEquals(WeeklyCategoryChallengePolicy.Status.INSUFFICIENT_EVIDENCE,
                WeeklyCategoryChallengePolicy.evaluate(
                        0,monday,monday.plusDays(7),Collections.emptyList()).status);
    }

    @Test public void oldObservationsOutsideSevenDayWindowAreIgnored() {
        assertEquals(WeeklyCategoryChallengePolicy.Status.INSUFFICIENT_EVIDENCE,
                WeeklyCategoryChallengePolicy.evaluate(0,monday,monday.plusDays(21),
                    Arrays.asList(report(1,0,2,true),report(3,0,2,true))).status);
    }

    @Test(expected=IllegalArgumentException.class)
    public void invalidRatingIsRejected() {report(1,0,11,true);}
}
