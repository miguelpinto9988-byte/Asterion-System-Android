package pe.asterionsystem.core.engine;

import java.util.Objects;
import pe.asterionsystem.core.events.EventBus;

/** Publishes one typed event per evaluation; engine itself stays side-effect free. */
public final class RuleEngineService implements RuleEvaluator<RuleContext, RuleEvaluation> {
    private final RuleEvaluator<RuleContext, RuleEvaluation> engine;
    private final EventBus events;

    public RuleEngineService(RuleEvaluator<RuleContext, RuleEvaluation> engine, EventBus events) {
        this.engine = Objects.requireNonNull(engine, "engine");
        this.events = Objects.requireNonNull(events, "events");
    }

    @Override public RuleEvaluation evaluate(RuleContext context) {
        RuleEvaluation result = engine.evaluate(context);
        events.publish(new RuleEvaluationCompletedEvent(result));
        return result;
    }
}
