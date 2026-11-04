package pe.asterionsystem.core.engine;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import pe.asterionsystem.core.events.InMemoryEventBus;
import pe.asterionsystem.core.events.Subscription;

public final class RuleEngineServiceTest {
    @Test public void emitsOnlyOneTypedEventPerEvaluation() {
        InMemoryEventBus bus = new InMemoryEventBus();
        List<RuleEvaluation> events = new ArrayList<>();
        Subscription sub = bus.subscribe(RuleEvaluationCompletedEvent.class,
                event -> events.add(event.evaluation()));
        DecisionRule r = new DecisionRule("sample", 1, true,
                new NumericCondition("score", NumericCondition.Operator.GREATER_OR_EQUAL, 50),
                "PASSED");
        RuleEngineService service = new RuleEngineService(new DeterministicRuleEngine(() -> List.of(r)), bus);
        RuleEvaluation result = service.evaluate(new RuleContext(Map.of("score", 70L)));
        assertEquals(1, events.size());
        assertSame(result, events.get(0));
        assertEquals(RuleEvaluation.Status.MATCH, result.status());
        sub.close();
        service.evaluate(new RuleContext(Map.of("score", 10L)));
        assertEquals(1, events.size());
    }
}
