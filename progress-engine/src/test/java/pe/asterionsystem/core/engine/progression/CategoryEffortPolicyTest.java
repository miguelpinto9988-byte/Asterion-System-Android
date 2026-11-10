package pe.asterionsystem.core.engine.progression;

import static org.junit.Assert.*;
import org.junit.Test;

public class CategoryEffortPolicyTest {
    @Test public void lowEffortSuggestsReviewWithoutAutomaticIncrease() {
        CategoryEffortPolicy.Decision d=CategoryEffortPolicy.evaluate(
                CategoryEffortPolicy.UPPER,3,3);
        assertEquals(CategoryEffortPolicy.Guidance.REVIEW_OPTIONAL,d.guidance);
        assertEquals(3,d.suggestedActivities);
        assertFalse(d.preferSimplerTechnique);
        assertTrue(d.explanation.contains("no se aumentan"));
    }

    @Test public void moderateEffortMaintainsLowerCategory() {
        CategoryEffortPolicy.Decision d=CategoryEffortPolicy.evaluate(
                CategoryEffortPolicy.LOWER,5,4);
        assertEquals(CategoryEffortPolicy.Guidance.MAINTAIN,d.guidance);
        assertEquals(4,d.suggestedActivities);
        assertFalse(d.preferSimplerTechnique);
    }

    @Test public void extremeEffortReducesSameCategory() {
        CategoryEffortPolicy.Decision d=CategoryEffortPolicy.evaluate(
                CategoryEffortPolicy.UPPER,10,5);
        assertEquals(CategoryEffortPolicy.Guidance.EASIER,d.guidance);
        assertEquals(1,d.suggestedActivities);
        assertTrue(d.preferSimplerTechnique);
    }

    @Test public void noDataNeverInventsCategoryEffort() {
        CategoryEffortPolicy.Decision d=CategoryEffortPolicy.evaluate(
                CategoryEffortPolicy.CORE,0,3);
        assertEquals(CategoryEffortPolicy.Guidance.NO_DATA,d.guidance);
        assertEquals(3,d.suggestedActivities);
    }

    @Test public void restIsAlwaysRest() {
        assertEquals(0,CategoryEffortPolicy.evaluate(
                CategoryEffortPolicy.REST,3,7).suggestedActivities);
    }

    @Test(expected=IllegalArgumentException.class)
    public void invalidCategoryRejected() {
        CategoryEffortPolicy.evaluate(8,3,3);
    }

    @Test(expected=IllegalArgumentException.class)
    public void invalidRatingRejected() {
        CategoryEffortPolicy.evaluate(0,11,3);
    }

    @Test public void tooFewActivitiesNeverReducedBelowOne() {
        assertEquals(1,CategoryEffortPolicy.evaluate(2,10,1).suggestedActivities);
    }
}
