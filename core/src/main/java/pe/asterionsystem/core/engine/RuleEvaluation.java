package pe.asterionsystem.core.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Structured, immutable result with an ordered trace for every inspected rule. */
public final class RuleEvaluation {
    public enum Status { MATCH, NO_MATCH, CONFLICT, INVALID_RULESET }
    public enum TraceStatus { MATCHED, NOT_MATCHED, DISABLED, MISSING_FACT }

    public static final class Trace {
        private final String ruleId;
        private final int priority;
        private final TraceStatus status;
        private final String outcomeCode;

        public Trace(String ruleId, int priority, TraceStatus status, String outcomeCode) {
            this.ruleId = Objects.requireNonNull(ruleId, "ruleId");
            this.priority = priority;
            this.status = Objects.requireNonNull(status, "status");
            this.outcomeCode = Objects.requireNonNull(outcomeCode, "outcomeCode");
        }

        public String ruleId() { return ruleId; }
        public int priority() { return priority; }
        public TraceStatus status() { return status; }
        public String outcomeCode() { return outcomeCode; }
    }

    private final Status status;
    private final String selectedRuleId;
    private final String outcomeCode;
    private final List<Trace> traces;
    private final List<String> diagnostics;

    public RuleEvaluation(Status status, String selectedRuleId, String outcomeCode,
                          List<Trace> traces, List<String> diagnostics) {
        this.status = Objects.requireNonNull(status, "status");
        if ((status == Status.MATCH) != (selectedRuleId != null && outcomeCode != null)) {
            throw new IllegalArgumentException("Selección incoherente con el estado");
        }
        this.selectedRuleId = selectedRuleId;
        this.outcomeCode = outcomeCode;
        this.traces = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(traces, "traces")));
        this.diagnostics = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(diagnostics, "diagnostics")));
    }

    public Status status() { return status; }
    public Optional<String> selectedRuleId() { return Optional.ofNullable(selectedRuleId); }
    public Optional<String> outcomeCode() { return Optional.ofNullable(outcomeCode); }
    public List<Trace> traces() { return traces; }
    public List<String> diagnostics() { return diagnostics; }
}
