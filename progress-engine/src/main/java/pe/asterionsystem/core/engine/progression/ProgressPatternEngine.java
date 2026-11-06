package pe.asterionsystem.core.engine.progression;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Bounded, immutable strategy registry; patterns are supplied explicitly via DI. */
public final class ProgressPatternEngine {
    public static final int MAX_PATTERNS = 16;
    private final Map<String, ProgressPattern> patterns;

    public ProgressPatternEngine(Collection<? extends ProgressPattern> registered) {
        Objects.requireNonNull(registered, "registered");
        if (registered.isEmpty() || registered.size() > MAX_PATTERNS) {
            throw new IllegalArgumentException("Cantidad de patrones inválida");
        }
        Map<String, ProgressPattern> index = new LinkedHashMap<>();
        for (ProgressPattern pattern : registered) {
            Objects.requireNonNull(pattern, "pattern");
            String id = pattern.id();
            if (id == null || !id.matches("[a-z][a-z0-9_]{0,39}")
                    || index.putIfAbsent(id, pattern) != null) {
                throw new IllegalArgumentException("Patrón duplicado o inválido");
            }
        }
        patterns = Collections.unmodifiableMap(index);
    }

    public static ProgressPatternEngine standard() {
        return new ProgressPatternEngine(Collections.singletonList(new StandardProgressPattern()));
    }

    public Set<String> registeredPatternIds() { return patterns.keySet(); }

    public ProgressAssessment assess(ProgressContext context) {
        Objects.requireNonNull(context, "context");
        ProgressPattern pattern = patterns.get(context.configuration().patternId());
        if (pattern == null) throw new IllegalArgumentException("Patrón no registrado");
        ProgressOutcome result = Objects.requireNonNull(pattern.evaluate(context), "pattern result");
        return new ProgressAssessment(context, result);
    }
}
