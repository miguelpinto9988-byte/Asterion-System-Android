package pe.asterionsystem.core.engine.progression;

/**
 * Conservative use of voluntary perceived effort feedback in daily proposals.
 * The policy may reduce the number of candidate activities, never increase it.
 * Monarch proposals retain their independent five-day consent workflow.
 */
public final class EffortAdaptationPolicy {
    private EffortAdaptationPolicy() { }

    public static int dailySuggestionCount(int configured, EffortReviewEngine.Review review) {
        int original=Math.max(1,Math.min(7,configured));
        if(review == null || review.observationCount == 0
                || Double.isNaN(review.averageEffort)) {
            return original;
        }
        if(review.averageEffort >= 8.0) return Math.min(original,1);
        if(review.averageEffort >= 7.0) return Math.min(original,2);
        return original;
    }
}
