package pe.asterionsystem.core.engine.progression;

import java.util.Collections;
import java.util.Objects;
import pe.asterionsystem.core.progress.ProgressSummary;

/** Exact existing week-rate semantics: completed / (completed + skipped). */
public final class StandardProgressPattern implements ProgressPattern {
    public static final String ID = "standard_v1";

    @Override public String id() { return ID; }

    @Override public ProgressOutcome evaluate(ProgressContext context) {
        ProgressSummary summary = Objects.requireNonNull(context, "context").summary();
        if (!summary.hasEvaluated()) {
            return new ProgressOutcome("UNASSESSED", null, Collections.emptyList());
        }
        return new ProgressOutcome("EVALUATED", summary.completionPercent(),
                Collections.emptyList());
    }
}
