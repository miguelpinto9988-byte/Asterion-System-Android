package pe.asterionsystem.core.engine.progression;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Immutable pattern selection and bounded numeric parameters; never executable code. */
public final class ProgressPatternConfig {
    public static final int MAX_PARAMETERS = 16;
    private final String patternId;
    private final Map<String, Long> parameters;

    public ProgressPatternConfig(String patternId, Map<String, Long> parameters) {
        if (patternId == null || !patternId.matches("[a-z][a-z0-9_]{0,39}")) {
            throw new IllegalArgumentException("ID de patrón inválido");
        }
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.size() > MAX_PARAMETERS) {
            throw new IllegalArgumentException("Demasiados parámetros de patrón");
        }
        LinkedHashMap<String, Long> safe = new LinkedHashMap<>();
        for (Map.Entry<String, Long> entry : parameters.entrySet()) {
            String key = Objects.requireNonNull(entry.getKey(), "parameter key");
            if (!key.matches("[a-z][a-z0-9_]{0,31}")) {
                throw new IllegalArgumentException("Nombre de parámetro inválido");
            }
            safe.put(key, Objects.requireNonNull(entry.getValue(), "parameter value"));
        }
        this.patternId = patternId;
        this.parameters = Collections.unmodifiableMap(safe);
    }

    public static ProgressPatternConfig standard() {
        return new ProgressPatternConfig(StandardProgressPattern.ID, Collections.emptyMap());
    }

    public String patternId() { return patternId; }
    public Map<String, Long> parameters() { return parameters; }
}
