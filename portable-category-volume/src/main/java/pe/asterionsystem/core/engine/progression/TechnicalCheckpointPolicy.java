package pe.asterionsystem.core.engine.progression;

import pe.asterionsystem.core.engine.missions.MissionDifficultyMixPolicy;

/**
 * Category-isolated technical review at 7/14/21 elapsed calendar days.
 * Every increase needs positive completed-session evidence in that category.
 * Day 21 reviews the result; technical levels 4/5 remain disallowed.
 */
public final class TechnicalCheckpointPolicy {
    public enum Action {
        WAIT, MAINTAIN, REVIEW_EASIER, ADVANCE_PHASE, ROTATE_EQUIVALENT
    }

    public static final class Decision {
        public final int checkpoint, category;
        public final Action action;
        public final String explanation;

        private Decision(int checkpoint, int category,
                         Action action, String explanation) {
            this.checkpoint = checkpoint;
            this.category = category;
            this.action = action;
            this.explanation = explanation;
        }

        public boolean mayAdvance() {
            return action == Action.ADVANCE_PHASE;
        }
    }

    private TechnicalCheckpointPolicy() { }

    public static Decision evaluate(int category, long daysSinceExperienceStart,
                                    int currentPhase, String experience,
                                    WeeklyCategoryChallengePolicy.Review weekly,
                                    boolean interrupted) {
        if (category < 0 || category > 5 || daysSinceExperienceStart < 0)
            throw new IllegalArgumentException("Calendario o categoría inválida");
        if (category == CategoryEffortPolicy.REST) {
            return new Decision(0, category, Action.WAIT,
                    "Día de descanso: no hay progresión técnica.");
        }
        int checkpoint = daysSinceExperienceStart >= 21 ? 21
                : daysSinceExperienceStart >= 14 ? 14
                : daysSinceExperienceStart >= 7 ? 7 : 0;
        if (checkpoint == 0) {
            return new Decision(0, category, Action.WAIT,
                    "Aún no se alcanza la primera revisión a los siete días.");
        }
        if (interrupted || (weekly != null && weekly.highRatings > 0)) {
            return new Decision(checkpoint, category, Action.REVIEW_EASIER,
                    "Revisión de categoría: esfuerzo alto o interrupción. "
                    + "No se aumenta complejidad; priorizar alternativas sencillas.");
        }
        if (weekly == null || weekly.category != category
                || weekly.status !=
                        WeeklyCategoryChallengePolicy.Status.OFFER_EQUIVALENT_VARIETY
                || weekly.completedRatings < 2 || weekly.lowRatings < 2) {
            return new Decision(checkpoint, category, Action.MAINTAIN,
                    "Revisión de categoría: sin evidencia suficiente de sesiones "
                    + "completadas fáciles; mantener complejidad.");
        }
        if (!MissionDifficultyMixPolicy.mixed(experience)) {
            return new Decision(checkpoint, category, Action.ROTATE_EQUIVALENT,
                    "En nivel básico se buscan equivalentes seguros, "
                    + "sin forzar niveles avanzados.");
        }
        if (MissionDifficultyMixPolicy.clampPhase(currentPhase)
                >= MissionDifficultyMixPolicy.PHASES - 1) {
            return new Decision(checkpoint, category, Action.ROTATE_EQUIVALENT,
                    "Límite técnico automático alcanzado; revisar alternativas "
                    + "del nivel existente. No se habilitan niveles 4 y 5.");
        }
        return new Decision(checkpoint, category, Action.ADVANCE_PHASE,
                "Revisión positiva de la misma categoría: ampliar gradualmente "
                + "las variantes técnicas disponibles, sin aumentar cargas "
                + "ni imponer ejercicios.");
    }
}
