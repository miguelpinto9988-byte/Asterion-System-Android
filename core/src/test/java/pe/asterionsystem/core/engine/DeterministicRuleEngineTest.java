package pe.asterionsystem.core.engine;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import pe.asterionsystem.core.engine.NumericCondition.Operator;
import pe.asterionsystem.core.engine.RuleEvaluation.Status;
import pe.asterionsystem.core.engine.RuleEvaluation.TraceStatus;

public final class DeterministicRuleEngineTest {
    private static DecisionRule rule(String id, int priority, boolean enabled,
                                     Operator operator, long threshold, String outcome) {
        return new DecisionRule(id, priority, enabled,
                new NumericCondition("score", operator, threshold), outcome);
    }

    private static RuleContext score(long value) {
        return new RuleContext(Map.of("score", value));
    }

    @Test public void orderedPriorityBeatsIdAndLowerPriority() {
        List<DecisionRule> rules = List.of(
                rule("a_lower", 1, true, Operator.GREATER_OR_EQUAL, 5, "LOW"),
                rule("z_higher", 10, true, Operator.GREATER_OR_EQUAL, 5, "HIGH"));
        RuleEvaluation result = new DeterministicRuleEngine(() -> rules).evaluate(score(10));
        assertEquals(Status.MATCH, result.status());
        assertEquals("HIGH", result.outcomeCode().orElseThrow());
        assertEquals("z_higher", result.traces().get(0).ruleId());
        assertTrue(result.diagnostics().contains("LOWER_PRIORITY_MATCH_IGNORED"));
    }

    @Test public void equalPriorityDifferentOutcomesProducesConflict() {
        RuleEvaluation result = new DeterministicRuleEngine(() -> List.of(
                rule("a", 5, true, Operator.EQUAL, 4, "YES"),
                rule("b", 5, true, Operator.GREATER_OR_EQUAL, 4, "NO"))).evaluate(score(4));
        assertEquals(Status.CONFLICT, result.status());
        assertTrue(result.selectedRuleId().isEmpty());
        assertTrue(result.diagnostics().contains("CONFLICT_SAME_PRIORITY"));
    }

    @Test public void equalPrioritySameOutcomeBreaksTieById() {
        RuleEvaluation result = new DeterministicRuleEngine(() -> List.of(
                rule("zeta", 5, true, Operator.EQUAL, 4, "YES"),
                rule("alfa", 5, true, Operator.EQUAL, 4, "YES"))).evaluate(score(4));
        assertEquals(Status.MATCH, result.status());
        assertEquals("alfa", result.selectedRuleId().orElseThrow());
        assertTrue(result.diagnostics().contains("TIE_RESOLVED_BY_RULE_ID"));
    }

    @Test public void disabledAndMissingInputsHaveTraceWithoutMatch() {
        RuleEvaluation result = new DeterministicRuleEngine(() -> List.of(
                rule("disabled", 8, false, Operator.EQUAL, 4, "YES"),
                rule("missing", 7, true, Operator.EQUAL, 4, "YES")))
                .evaluate(new RuleContext(Collections.emptyMap()));
        assertEquals(Status.NO_MATCH, result.status());
        assertEquals(TraceStatus.DISABLED, result.traces().get(0).status());
        assertEquals(TraceStatus.MISSING_FACT, result.traces().get(1).status());
        assertTrue(result.diagnostics().contains("MISSING_FACT:missing:score"));
    }

    @Test public void unmatchedRuleReturnsNoMatch() {
        RuleEvaluation result = new DeterministicRuleEngine(() -> List.of(
                rule("x", 2, true, Operator.LESS_THAN, 3, "LOW"))).evaluate(score(3));
        assertEquals(Status.NO_MATCH, result.status());
        assertEquals(TraceStatus.NOT_MATCHED, result.traces().get(0).status());
    }

    @Test public void duplicateIdsAndTooManyRulesAreRejected() {
        DecisionRule r = rule("same", 1, true, Operator.EQUAL, 1, "YES");
        assertEquals(Status.INVALID_RULESET,
                new DeterministicRuleEngine(() -> List.of(r, r)).evaluate(score(1)).status());
        List<DecisionRule> excessive = new ArrayList<>();
        for (int i = 0; i < 65; i++) excessive.add(rule("r" + i, 1, true, Operator.EQUAL, 1, "YES"));
        assertEquals(Status.INVALID_RULESET,
                new DeterministicRuleEngine(() -> excessive).evaluate(score(1)).status());
    }

    @Test public void failingSourceReturnsStructuredError() {
        RuleEvaluation result = new DeterministicRuleEngine(() -> {
            throw new IllegalStateException("unavailable");
        }).evaluate(score(1));
        assertEquals(Status.INVALID_RULESET, result.status());
        assertEquals("RULE_SOURCE_UNAVAILABLE", result.diagnostics().get(0));
    }

    @Test public void contextIsImmutableAndBounded() {
        Map<String, Long> source = new HashMap<>();
        source.put("score", 7L);
        RuleContext context = new RuleContext(source);
        source.put("score", 10L);
        assertEquals(7L, context.find("score").orElseThrow());
        try { context.facts().put("score", 9L); fail(); } catch (UnsupportedOperationException expected) {}
        try { new RuleContext(Map.of("INVALID", 1L)); fail(); } catch (IllegalArgumentException expected) {}
    }

    @Test public void comparatorCoversAllSixNumericComparisons() {
        assertTrue(Operator.EQUAL.matches(3, 3));
        assertTrue(Operator.NOT_EQUAL.matches(3, 4));
        assertTrue(Operator.GREATER_THAN.matches(4, 3));
        assertTrue(Operator.GREATER_OR_EQUAL.matches(3, 3));
        assertTrue(Operator.LESS_THAN.matches(2, 3));
        assertTrue(Operator.LESS_OR_EQUAL.matches(3, 3));
    }
}
