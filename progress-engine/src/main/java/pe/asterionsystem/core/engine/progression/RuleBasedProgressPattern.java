package pe.asterionsystem.core.engine.progression;

import java.util.Objects;
import pe.asterionsystem.core.engine.RuleContext;
import pe.asterionsystem.core.engine.RuleEvaluation;
import pe.asterionsystem.core.engine.RuleEvaluator;

/** Optional adapter for phase-06 rules; selection remains deterministic and read-only. */
public final class RuleBasedProgressPattern implements ProgressPattern {
    private final String id;
    private final RuleEvaluator<RuleContext, RuleEvaluation> rules;

    public RuleBasedProgressPattern(String id, RuleEvaluator<RuleContext, RuleEvaluation> rules) {
        if (id == null || !id.matches("[a-z][a-z0-9_]{0,39}")) {
            throw new IllegalArgumentException("ID de patrón inválido");
        }
        this.id = id;
        this.rules = Objects.requireNonNull(rules, "rules");
    }

    @Override public String id() { return id; }

    @Override public ProgressOutcome evaluate(ProgressContext context) {
        Objects.requireNonNull(context, "context");
        RuleEvaluation evaluated = Objects.requireNonNull(
                rules.evaluate(context.ruleFacts()), "rule evaluation");
        String code;
        switch (evaluated.status()) {
            case MATCH:
                code = evaluated.outcomeCode().orElseThrow(IllegalStateException::new);
                break;
            case NO_MATCH: code = "NO_RULE_MATCH"; break;
            case CONFLICT: code = "RULE_CONFLICT"; break;
            case INVALID_RULESET: code = "RULESET_INVALID"; break;
            default: throw new IllegalStateException("Estado de reglas desconocido");
        }
        Integer rate = context.summary().hasEvaluated()
                ? context.summary().completionPercent() : null;
        return new ProgressOutcome(code, rate, evaluated.diagnostics());
    }
}
