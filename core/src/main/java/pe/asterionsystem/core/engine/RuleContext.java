package pe.asterionsystem.core.engine;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalLong;

/** Immutable, bounded numeric facts. Not an expression or script interpreter. */
public final class RuleContext {
    public static final int MAX_FACTS = 32;
    private final Map<String, Long> facts;

    public RuleContext(Map<String, Long> values) {
        Objects.requireNonNull(values, "values");
        if (values.size() > MAX_FACTS) {
            throw new IllegalArgumentException("Demasiados datos en el contexto");
        }
        LinkedHashMap<String, Long> validated = new LinkedHashMap<>();
        for (Map.Entry<String, Long> entry : values.entrySet()) {
            String key = Objects.requireNonNull(entry.getKey(), "key");
            if (!key.matches("[a-z][a-z0-9_]{0,47}")) {
                throw new IllegalArgumentException("Nombre de campo no válido: " + key);
            }
            validated.put(key, Objects.requireNonNull(entry.getValue(), "value"));
        }
        facts = Collections.unmodifiableMap(validated);
    }

    public OptionalLong find(String field) {
        Objects.requireNonNull(field, "field");
        Long value = facts.get(field);
        return value == null ? OptionalLong.empty() : OptionalLong.of(value);
    }

    public Map<String, Long> facts() {
        return facts;
    }
}
