package pe.asterionsystem.core.engine.progression;

import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.*;
import org.junit.Test;
import pe.asterionsystem.core.domain.EntityId;
import pe.asterionsystem.core.engine.DecisionRule;
import pe.asterionsystem.core.engine.DeterministicRuleEngine;
import pe.asterionsystem.core.engine.NumericCondition;
import pe.asterionsystem.core.planning.ScheduledActivity;
import pe.asterionsystem.core.progress.ProgressEntry;
import pe.asterionsystem.core.progress.TrackingStatus;
import pe.asterionsystem.core.settings.AppPreferences;

public final class ProgressPatternEngineTest {
    private static final LocalDate MONDAY = LocalDate.of(2026, 11, 2);

    private static ProgressEntry entry(String id, TrackingStatus status, int duration) {
        ScheduledActivity plan = new ScheduledActivity(
                new EntityId(id), new EntityId("exercise"), MONDAY.plusDays(2),
                600, duration, 1, 1);
        return new ProgressEntry(plan, "Actividad de prueba", status, "");
    }

    private static ProgressContext context(List<ProgressEntry> rows, ProgressPatternConfig config) {
        return new ProgressContext(MONDAY, rows, AppPreferences.defaults(), config);
    }

    @Test public void standardHasNoScoreWhenNoActivityWasEvaluated() {
        ProgressContext input = context(Collections.singletonList(
                entry("pending", TrackingStatus.PENDING, 30)), ProgressPatternConfig.standard());
        ProgressAssessment result = ProgressPatternEngine.standard().assess(input);
        assertEquals("standard_v1", result.patternId());
        assertEquals("UNASSESSED", result.resultCode());
        assertEquals(1, result.summary().pending());
        assertFalse(result.scorePercent().isPresent());
        assertFalse(input.ruleFacts().find("completion_percent").isPresent());
        assertEquals(0, input.ruleFacts().find("has_evaluated").getAsLong());
    }

    @Test public void standardMatchesExistingExplicitCompletionSemantics() {
        List<ProgressEntry> rows = Arrays.asList(
                entry("one", TrackingStatus.COMPLETED, 30),
                entry("two", TrackingStatus.COMPLETED, 15),
                entry("three", TrackingStatus.SKIPPED, 20),
                entry("four", TrackingStatus.PENDING, 25));
        ProgressContext input = context(rows, ProgressPatternConfig.standard());
        ProgressAssessment result = ProgressPatternEngine.standard().assess(input);
        assertEquals("EVALUATED", result.resultCode());
        assertEquals(67, result.scorePercent().getAsInt());
        assertEquals(3, result.summary().evaluated());
        assertEquals(4, input.ruleFacts().find("total").getAsLong());
        assertEquals(67, input.ruleFacts().find("completion_percent").getAsLong());
    }

    @Test public void allowsNewStrategiesAndExplicitPatternSelection() {
        ProgressPattern weighted = new ProgressPattern() {
            @Override public String id() { return "duration_v1"; }
            @Override public ProgressOutcome evaluate(ProgressContext ctx) {
                int complete = 0;
                int evaluated = 0;
                for (ProgressEntry row : ctx.entries()) {
                    if (row.status() == TrackingStatus.PENDING) continue;
                    evaluated += row.schedule().durationMinutes();
                    if (row.status() == TrackingStatus.COMPLETED) {
                        complete += row.schedule().durationMinutes();
                    }
                }
                Integer percent = evaluated == 0 ? null : (complete * 100 / evaluated);
                return new ProgressOutcome("DURATION_TRACKED", percent, Collections.emptyList());
            }
        };
        ProgressPatternEngine engine = new ProgressPatternEngine(
                Arrays.asList(new StandardProgressPattern(), weighted));
        List<ProgressEntry> rows = Arrays.asList(
                entry("a", TrackingStatus.COMPLETED, 30),
                entry("b", TrackingStatus.SKIPPED, 10));
        ProgressContext standard = context(rows, ProgressPatternConfig.standard());
        ProgressContext custom = context(rows,
                new ProgressPatternConfig("duration_v1", Collections.emptyMap()));
        assertEquals(50, engine.assess(standard).scorePercent().getAsInt());
        assertEquals(75, engine.assess(custom).scorePercent().getAsInt());
        assertEquals(2, engine.registeredPatternIds().size());
    }

    @Test public void reusesExistingDeterministicRuleEngine() {
        DecisionRule rule = new DecisionRule("at_least_half", 100, true,
                new NumericCondition("completion_percent",
                        NumericCondition.Operator.GREATER_OR_EQUAL, 50), "GOAL_MET");
        RuleBasedProgressPattern rules = new RuleBasedProgressPattern("rules_v1",
                new DeterministicRuleEngine(() -> Collections.singletonList(rule)));
        ProgressPatternEngine engine = new ProgressPatternEngine(
                Arrays.asList(new StandardProgressPattern(), rules));
        ProgressPatternConfig config = new ProgressPatternConfig(
                "rules_v1", Collections.singletonMap("weekly_goal", 50L));
        ProgressContext input = context(Arrays.asList(
                entry("a", TrackingStatus.COMPLETED, 30),
                entry("b", TrackingStatus.SKIPPED, 30)), config);
        assertEquals(50, input.ruleFacts().find("param_weekly_goal").getAsLong());
        ProgressAssessment result = engine.assess(input);
        assertEquals("GOAL_MET", result.resultCode());
        assertEquals(50, result.scorePercent().getAsInt());
    }

    @Test public void rulesHandleMissingPercentageWithoutAssigningZero() {
        DecisionRule rule = new DecisionRule("needs_rate", 1, true,
                new NumericCondition("completion_percent",
                        NumericCondition.Operator.EQUAL, 0), "ZERO_RATE");
        RuleBasedProgressPattern pattern = new RuleBasedProgressPattern("rules_v1",
                new DeterministicRuleEngine(() -> Collections.singletonList(rule)));
        ProgressAssessment result = new ProgressPatternEngine(Collections.singletonList(pattern))
                .assess(context(Collections.emptyList(),
                        new ProgressPatternConfig("rules_v1", Collections.emptyMap())));
        assertEquals("NO_RULE_MATCH", result.resultCode());
        assertFalse(result.scorePercent().isPresent());
        assertTrue(result.diagnostics().stream().anyMatch(x -> x.startsWith("MISSING_FACT:")));
    }

    @Test public void unknownOrDuplicatePatternsAreRejected() {
        try {
            ProgressPatternEngine.standard().assess(context(Collections.emptyList(),
                    new ProgressPatternConfig("future_v1", Collections.emptyMap())));
            fail("Unknown pattern should fail explicitly");
        } catch (IllegalArgumentException expected) { }
        try {
            new ProgressPatternEngine(Arrays.asList(
                    new StandardProgressPattern(), new StandardProgressPattern()));
            fail("Duplicate pattern IDs should fail");
        } catch (IllegalArgumentException expected) { }
    }

    @Test public void defensiveSnapshotAndConfigurationValidation() {
        ArrayList<ProgressEntry> source = new ArrayList<>();
        source.add(entry("a", TrackingStatus.COMPLETED, 30));
        Map<String, Long> settings = new HashMap<>();
        settings.put("weekly_goal", 3L);
        ProgressPatternConfig config = new ProgressPatternConfig("standard_v1", settings);
        ProgressContext ctx = context(source, config);
        source.clear();
        settings.clear();
        assertEquals(1, ctx.entries().size());
        assertEquals(3, ctx.configuration().parameters().get("weekly_goal").longValue());
        try {
            ctx.entries().clear();
            fail("Snapshot must be immutable");
        } catch (UnsupportedOperationException expected) { }
        try {
            new ProgressPatternConfig("standard_v1", Collections.singletonMap("invalid-key", 1L));
            fail("Invalid names must be rejected");
        } catch (IllegalArgumentException expected) { }
    }

    @Test public void rejectsRecordsOutsideTheSelectedWeek() {
        ScheduledActivity nextWeek = new ScheduledActivity(
                new EntityId("next_week"), new EntityId("exercise"),
                MONDAY.plusWeeks(1), 600, 30, 1, 1);
        ProgressEntry record = new ProgressEntry(
                nextWeek, "Otra semana", TrackingStatus.COMPLETED, "");
        try {
            context(Collections.singletonList(record), ProgressPatternConfig.standard());
            fail("Cross-week record should be rejected");
        } catch (IllegalArgumentException expected) { }
    }

    @Test public void samePriorityRuleConflictsRemainVisible() {
        DecisionRule first = new DecisionRule("first", 10, true,
                new NumericCondition("evaluated",
                        NumericCondition.Operator.GREATER_OR_EQUAL, 1), "RULE_A");
        DecisionRule second = new DecisionRule("second", 10, true,
                new NumericCondition("evaluated",
                        NumericCondition.Operator.GREATER_OR_EQUAL, 1), "RULE_B");
        RuleBasedProgressPattern strategy = new RuleBasedProgressPattern("rules_v1",
                new DeterministicRuleEngine(() -> Arrays.asList(first, second)));
        ProgressAssessment assessment = new ProgressPatternEngine(
                Collections.singletonList(strategy)).assess(
                        context(Collections.singletonList(
                                entry("a", TrackingStatus.COMPLETED, 30)),
                                new ProgressPatternConfig("rules_v1", Collections.emptyMap())));
        assertEquals("RULE_CONFLICT", assessment.resultCode());
        assertTrue(assessment.diagnostics().contains("CONFLICT_SAME_PRIORITY"));
    }

    @Test public void percentIsValidatedAndPeriodMustStartOnMonday() {
        try {
            new ProgressOutcome("BAD_SCORE", 101, Collections.emptyList());
            fail("Score must be bounded");
        } catch (IllegalArgumentException expected) { }
        try {
            new ProgressContext(MONDAY.plusDays(1), Collections.emptyList(),
                    AppPreferences.defaults(), ProgressPatternConfig.standard());
            fail("Period must start on Monday");
        } catch (IllegalArgumentException expected) { }
    }
}
