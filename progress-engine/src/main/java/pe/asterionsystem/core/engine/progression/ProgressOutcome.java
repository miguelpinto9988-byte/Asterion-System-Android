package pe.asterionsystem.core.engine.progression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

/** Pattern interpretation only. Never writes or executes a planned activity. */
public final class ProgressOutcome {
    private final String code;
    private final Integer scorePercent;
    private final List<String> diagnostics;

    public ProgressOutcome(String code, Integer scorePercent, List<String> diagnostics) {
        if (code == null || !code.matches("[A-Z][A-Z0-9_]{0,39}")) {
            throw new IllegalArgumentException("Código de resultado inválido");
        }
        if (scorePercent != null && (scorePercent < 0 || scorePercent > 100)) {
            throw new IllegalArgumentException("Porcentaje fuera de 0..100");
        }
        this.code = code;
        this.scorePercent = scorePercent;
        Objects.requireNonNull(diagnostics, "diagnostics");
        if (diagnostics.size() > 64) throw new IllegalArgumentException("Demasiados diagnósticos");
        ArrayList<String> safe = new ArrayList<>();
        for (String diagnostic : diagnostics) {
            String value = Objects.requireNonNull(diagnostic, "diagnostic");
            if (value.length() > 160) throw new IllegalArgumentException("Diagnóstico demasiado largo");
            safe.add(value);
        }
        this.diagnostics = Collections.unmodifiableList(safe);
    }

    public String code() { return code; }
    /** Absence means no meaningful score, not zero percent. */
    public OptionalInt scorePercent() {
        return scorePercent == null ? OptionalInt.empty() : OptionalInt.of(scorePercent);
    }
    public List<String> diagnostics() { return diagnostics; }
}
