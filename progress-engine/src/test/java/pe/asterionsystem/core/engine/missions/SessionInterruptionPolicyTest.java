package pe.asterionsystem.core.engine.missions;

import static org.junit.Assert.*;
import org.junit.Test;

public class SessionInterruptionPolicyTest {
    @Test public void exhaustionOnlyRequestsSofterNextCategorySession() {
        SessionInterruptionPolicy.Assessment result=SessionInterruptionPolicy.assess(
                SessionInterruptionPolicy.EXHAUSTION);
        assertEquals(SessionInterruptionPolicy.Action.CATEGORY_LIGHTER_NEXT_SESSION,
                result.action);
        assertFalse(result.urgentMedicalAdvice);
    }

    @Test public void mildPainPausesOnlyAffectedCategory() {
        SessionInterruptionPolicy.Assessment result=SessionInterruptionPolicy.assess(
                SessionInterruptionPolicy.MINOR_DISCOMFORT);
        assertEquals(SessionInterruptionPolicy.Action.CATEGORY_PAUSE_UNTIL_REVIEW,
                result.action);
        assertFalse(result.urgentMedicalAdvice);
    }

    @Test public void suspectedDislocationRequiresMedicalAssessmentWithoutOneDayExpiry() {
        SessionInterruptionPolicy.Assessment result=SessionInterruptionPolicy.assess(
                SessionInterruptionPolicy.SERIOUS_JOINT_PAIN);
        assertEquals(SessionInterruptionPolicy.Action.GLOBAL_PAUSE_AND_SEEK_MEDICAL_CARE,
                result.action);
        assertTrue(result.urgentMedicalAdvice);
        assertTrue(result.explanation.contains("No se reanudará automáticamente"));
    }

    @Test public void severeDizzinessAndVomitingPauseAllExercise() {
        SessionInterruptionPolicy.Assessment result=SessionInterruptionPolicy.assess(
                SessionInterruptionPolicy.INTENSE_DIZZINESS_VOMITING);
        assertEquals(SessionInterruptionPolicy.Action.GLOBAL_PAUSE_AND_SEEK_MEDICAL_CARE,
                result.action);
        assertTrue(result.urgentMedicalAdvice);
    }

    @Test public void stoppingWithoutDisclosingReasonIsAllowed() {
        assertEquals(SessionInterruptionPolicy.Action.STOP_ONLY,
                SessionInterruptionPolicy.assess(
                        SessionInterruptionPolicy.STOP_WITHOUT_REASON).action);
    }

    @Test(expected=IllegalArgumentException.class)
    public void unknownAnswersAreRejected() {
        SessionInterruptionPolicy.assess(99);
    }
}
