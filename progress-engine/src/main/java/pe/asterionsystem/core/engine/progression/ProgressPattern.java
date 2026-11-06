package pe.asterionsystem.core.engine.progression;

import pe.asterionsystem.core.engine.RuleEvaluator;

/** Strategy port. Register a Java implementation; never run scripts from configuration. */
public interface ProgressPattern extends RuleEvaluator<ProgressContext, ProgressOutcome> {
    String id();
}
