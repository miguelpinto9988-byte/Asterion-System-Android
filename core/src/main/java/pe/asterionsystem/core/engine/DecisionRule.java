package pe.asterionsystem.core.engine;

import java.util.Objects;

/** Immutable rule with fixed condition and outcome; no executable user expressions. */
public final class DecisionRule {
    private final String id;
    private final int priority;
    private final boolean enabled;
    private final NumericCondition condition;
    private final String outcomeCode;

    public DecisionRule(String id, int priority, boolean enabled,
                        NumericCondition condition, String outcomeCode) {
        if (id == null || !id.matches("[a-z][a-z0-9_]{0,39}")) {
            throw new IllegalArgumentException("ID de regla inválido");
        }
        if (priority < -1000 || priority > 1000) {
            throw new IllegalArgumentException("Prioridad fuera de límites");
        }
        if (outcomeCode == null || !outcomeCode.matches("[A-Z][A-Z0-9_]{0,39}")) {
            throw new IllegalArgumentException("Código de resultado inválido");
        }
        this.id = id;
        this.priority = priority;
        this.enabled = enabled;
        this.condition = Objects.requireNonNull(condition, "condition");
        this.outcomeCode = outcomeCode;
    }

    public String id() { return id; }
    public int priority() { return priority; }
    public boolean enabled() { return enabled; }
    public NumericCondition condition() { return condition; }
    public String outcomeCode() { return outcomeCode; }
}
