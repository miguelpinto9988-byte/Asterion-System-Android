package pe.asterionsystem.core.engine;

import java.util.Objects;
import pe.asterionsystem.core.events.AppEvent;

/** Technical in-process event; not an Android user notification. */
public final class RuleEvaluationCompletedEvent implements AppEvent {
    private final RuleEvaluation evaluation;

    public RuleEvaluationCompletedEvent(RuleEvaluation evaluation) {
        this.evaluation = Objects.requireNonNull(evaluation, "evaluation");
    }

    public RuleEvaluation evaluation() { return evaluation; }
}
