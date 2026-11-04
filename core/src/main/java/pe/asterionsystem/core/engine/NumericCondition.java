package pe.asterionsystem.core.engine;

import java.util.Objects;
import java.util.OptionalLong;

/** A single typed comparison, evaluated exactly once without recursion. */
public final class NumericCondition {
    public enum Operator {
        GREATER_THAN, GREATER_OR_EQUAL, LESS_THAN, LESS_OR_EQUAL, EQUAL, NOT_EQUAL;

        public boolean matches(long actual, long expected) {
            switch (this) {
                case GREATER_THAN: return actual > expected;
                case GREATER_OR_EQUAL: return actual >= expected;
                case LESS_THAN: return actual < expected;
                case LESS_OR_EQUAL: return actual <= expected;
                case EQUAL: return actual == expected;
                case NOT_EQUAL: return actual != expected;
                default: throw new AssertionError(this);
            }
        }
    }

    public enum Verdict { MATCH, NO_MATCH, MISSING_FACT }

    private final String field;
    private final Operator operator;
    private final long expected;

    public NumericCondition(String field, Operator operator, long expected) {
        if (field == null || !field.matches("[a-z][a-z0-9_]{0,47}")) {
            throw new IllegalArgumentException("Nombre de campo inválido");
        }
        this.field = field;
        this.operator = Objects.requireNonNull(operator, "operator");
        this.expected = expected;
    }

    public String field() { return field; }
    public Operator operator() { return operator; }
    public long expected() { return expected; }

    public Verdict test(RuleContext context) {
        OptionalLong found = Objects.requireNonNull(context, "context").find(field);
        if (!found.isPresent()) return Verdict.MISSING_FACT;
        return operator.matches(found.getAsLong(), expected) ? Verdict.MATCH : Verdict.NO_MATCH;
    }
}
