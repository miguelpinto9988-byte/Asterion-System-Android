package pe.asterionsystem.core.engine;

/** Port for the phase-06 rule engine; no rule implementation in phase 02. */
@FunctionalInterface
public interface RuleEvaluator<C, R> {
    R evaluate(C context);
}
