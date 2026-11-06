package pe.asterionsystem.core.engine.progression;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import pe.asterionsystem.core.progress.ProgressSummary;

/** Immutable pattern result; raw totals remain available for auditing. */
public final class ProgressAssessment {
    private final LocalDate weekStart;
    private final String patternId;
    private final ProgressSummary summary;
    private final ProgressOutcome outcome;

    ProgressAssessment(ProgressContext context, ProgressOutcome outcome) {
        Objects.requireNonNull(context, "context");
        this.weekStart = context.monday();
        this.patternId = context.configuration().patternId();
        this.summary = context.summary();
        this.outcome = Objects.requireNonNull(outcome, "outcome");
    }

    public LocalDate weekStart() { return weekStart; }
    public String patternId() { return patternId; }
    public ProgressSummary summary() { return summary; }
    public String resultCode() { return outcome.code(); }
    public OptionalInt scorePercent() { return outcome.scorePercent(); }
    public List<String> diagnostics() { return outcome.diagnostics(); }
}
