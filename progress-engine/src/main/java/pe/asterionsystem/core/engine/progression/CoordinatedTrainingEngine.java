package pe.asterionsystem.core.engine.progression;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;
import pe.asterionsystem.core.engine.missions.MissionPlanner;

/**
 * Bidirectional, side-effect-free coordination.
 *
 * Volume -> selector: recent category-specific volume effort participates in
 * CategoryEffortPolicy, potentially reducing count / technical complexity.
 * Selector -> volume: only the actual selected variant and its technical
 * level can receive a comparable, optional numeric volume review.
 *
 * No automatic series/reps change, no mutation of the user's weekly calendar.
 */
public final class CoordinatedTrainingEngine {
    private final MissionPlanner selector;

    public CoordinatedTrainingEngine(MissionPlanner selector) {
        this.selector = Objects.requireNonNull(selector, "selector");
    }

    public static final class Plan {
        public final MissionPlanner.Proposal mission;
        public final CategoryEffortPolicy.Decision effortDecision;
        public final Map<String, VolumeProgressionEngine.Suggestion> volumeByVariant;

        private Plan(MissionPlanner.Proposal mission,
                     CategoryEffortPolicy.Decision effortDecision,
                     Map<String, VolumeProgressionEngine.Suggestion> volumeByVariant) {
            this.mission = mission;
            this.effortDecision = effortDecision;
            this.volumeByVariant = Collections.unmodifiableMap(volumeByVariant);
        }
    }

    public Plan daily(MissionPlanner.Profile profile, LocalDate today,
                      int categoryEffort, List<String> previousCompleted,
                      List<String> recentCompleted, boolean allowWeeklyRotation,
                      int technicalPhase, List<VolumeProgressionEngine.Observation> volumeHistory,
                      boolean recentInterruption) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(today, "today");
        Objects.requireNonNull(volumeHistory, "volumeHistory");
        int category = profile.weekly.get(today.getDayOfWeek().getValue() - 1);
        int volumeEffort = VolumeProgressionEngine.latestCategoryEffort(
                category, today, volumeHistory);
        int combinedEffort = Math.max(categoryEffort, volumeEffort);
        if (recentInterruption && category != CategoryEffortPolicy.REST) {
            combinedEffort = Math.max(combinedEffort, 9);
        }
        CategoryEffortPolicy.Decision decision = CategoryEffortPolicy.evaluate(
                category, combinedEffort, profile.dailyLimit);
        MissionPlanner.Profile adapted = decision.preferSimplerTechnique
                || decision.suggestedActivities < profile.dailyLimit
                ? profile.adapted(
                        decision.preferSimplerTechnique ? "principiante" : profile.experience,
                        Math.max(1, decision.suggestedActivities))
                : profile;
        MissionPlanner.Proposal mission = selector.daily(
                adapted, today, decision,
                Objects.requireNonNull(previousCompleted, "previousCompleted"),
                allowWeeklyRotation && !recentInterruption,
                Objects.requireNonNull(recentCompleted, "recentCompleted"),
                technicalPhase);
        Map<String, VolumeProgressionEngine.Suggestion> proposed = new LinkedHashMap<>();
        for (ExerciseVariant variant : mission.candidates()) {
            proposed.put(variant.id(), VolumeProgressionEngine.review(
                    variant, category, today, volumeHistory,
                    recentInterruption || decision.preferSimplerTechnique));
        }
        return new Plan(mission, decision, proposed);
    }
}
