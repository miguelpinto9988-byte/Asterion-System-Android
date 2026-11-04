package pe.asterionsystem.core.engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import pe.asterionsystem.core.engine.RuleEvaluation.Status;
import pe.asterionsystem.core.engine.RuleEvaluation.Trace;
import pe.asterionsystem.core.engine.RuleEvaluation.TraceStatus;

/** Bounded, single-pass, side-effect-free evaluator. Stable priority and ID ordering. */
public final class DeterministicRuleEngine implements RuleEvaluator<RuleContext, RuleEvaluation> {
    public static final int MAX_RULES = 64;
    private static final Comparator<DecisionRule> ORDER = Comparator
            .comparingInt(DecisionRule::priority).reversed()
            .thenComparing(DecisionRule::id);

    private final RuleCatalog source;

    public DeterministicRuleEngine(RuleCatalog source) {
        this.source = Objects.requireNonNull(source, "source");
    }

    @Override public RuleEvaluation evaluate(RuleContext context) {
        Objects.requireNonNull(context, "context");
        List<DecisionRule> sorted;
        try {
            List<DecisionRule> raw = Objects.requireNonNull(source.loadRules(), "rules");
            if (raw.size() > MAX_RULES) {
                return invalid("RULE_LIMIT_EXCEEDED");
            }
            Set<String> ids = new HashSet<>();
            sorted = new ArrayList<>(raw.size());
            for (DecisionRule rule : raw) {
                if (rule == null || !ids.add(rule.id())) {
                    return invalid("INVALID_OR_DUPLICATE_RULE_ID");
                }
                sorted.add(rule);
            }
            sorted.sort(ORDER);
        } catch (RuntimeException failure) {
            // A failing catalog is an input/source error, not executable rule logic.
            return invalid("RULE_SOURCE_UNAVAILABLE");
        }

        List<Trace> traces = new ArrayList<>(sorted.size());
        List<String> diagnostics = new ArrayList<>();
        List<DecisionRule> matched = new ArrayList<>();
        for (DecisionRule rule : sorted) {
            TraceStatus traceStatus;
            if (!rule.enabled()) {
                traceStatus = TraceStatus.DISABLED;
            } else {
                NumericCondition.Verdict verdict = rule.condition().test(context);
                switch (verdict) {
                    case MATCH:
                        traceStatus = TraceStatus.MATCHED;
                        matched.add(rule);
                        break;
                    case NO_MATCH:
                        traceStatus = TraceStatus.NOT_MATCHED;
                        break;
                    case MISSING_FACT:
                        traceStatus = TraceStatus.MISSING_FACT;
                        diagnostics.add("MISSING_FACT:" + rule.id() + ":" + rule.condition().field());
                        break;
                    default: throw new AssertionError(verdict);
                }
            }
            traces.add(new Trace(rule.id(), rule.priority(), traceStatus, rule.outcomeCode()));
        }
        if (matched.isEmpty()) {
            diagnostics.add("NO_MATCH");
            return new RuleEvaluation(Status.NO_MATCH, null, null, traces, diagnostics);
        }
        DecisionRule winner = matched.get(0); // Sorted by descending priority, then ID.
        Set<String> topOutputs = new HashSet<>();
        int topCount = 0;
        for (DecisionRule rule : matched) {
            if (rule.priority() == winner.priority()) {
                topOutputs.add(rule.outcomeCode());
                topCount++;
            }
        }
        if (topOutputs.size() > 1) {
            diagnostics.add("CONFLICT_SAME_PRIORITY");
            return new RuleEvaluation(Status.CONFLICT, null, null, traces, diagnostics);
        }
        if (topCount > 1) diagnostics.add("TIE_RESOLVED_BY_RULE_ID");
        if (matched.size() > topCount) diagnostics.add("LOWER_PRIORITY_MATCH_IGNORED");
        return new RuleEvaluation(Status.MATCH, winner.id(), winner.outcomeCode(), traces, diagnostics);
    }

    private static RuleEvaluation invalid(String code) {
        return new RuleEvaluation(Status.INVALID_RULESET, null, null, Collections.emptyList(), Collections.singletonList(code));
    }
}
