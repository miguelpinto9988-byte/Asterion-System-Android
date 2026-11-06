package pe.asterionsystem.core.engine.progression;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import pe.asterionsystem.core.engine.RuleContext;
import pe.asterionsystem.core.progress.ProgressEntry;
import pe.asterionsystem.core.progress.ProgressSummary;
import pe.asterionsystem.core.settings.AppPreferences;

/** Read-only weekly input: existing schedule/tracking rows plus future pattern parameters. */
public final class ProgressContext {
    private final LocalDate monday;
    private final List<ProgressEntry> entries;
    private final ProgressSummary summary;
    private final AppPreferences preferences;
    private final ProgressPatternConfig configuration;

    public ProgressContext(LocalDate monday, List<ProgressEntry> entries,
                           AppPreferences preferences, ProgressPatternConfig configuration) {
        this.monday = Objects.requireNonNull(monday, "monday");
        if (monday.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new IllegalArgumentException("El período debe iniciar un lunes");
        }
        Objects.requireNonNull(entries, "entries");
        ArrayList<ProgressEntry> copy = new ArrayList<>(entries.size());
        LocalDate nextMonday = monday.plusWeeks(1);
        for (ProgressEntry row : entries) {
            ProgressEntry entry = Objects.requireNonNull(row, "entry");
            LocalDate day = entry.schedule().date();
            if (day.isBefore(monday) || !day.isBefore(nextMonday)) {
                throw new IllegalArgumentException("Actividad fuera de la semana seleccionada");
            }
            copy.add(entry);
        }
        this.entries = Collections.unmodifiableList(copy);
        this.summary = ProgressSummary.from(this.entries);
        this.preferences = Objects.requireNonNull(preferences, "preferences");
        this.configuration = Objects.requireNonNull(configuration, "configuration");
    }

    public LocalDate monday() { return monday; }
    public List<ProgressEntry> entries() { return entries; }
    public ProgressSummary summary() { return summary; }
    public AppPreferences preferences() { return preferences; }
    public ProgressPatternConfig configuration() { return configuration; }

    /** Supplies the already-existing bounded phase-06 numeric rule engine. */
    public RuleContext ruleFacts() {
        Map<String, Long> facts = new LinkedHashMap<>();
        facts.put("total", (long) summary.total());
        facts.put("completed", (long) summary.completed());
        facts.put("skipped", (long) summary.skipped());
        facts.put("pending", (long) summary.pending());
        facts.put("evaluated", (long) summary.evaluated());
        facts.put("has_evaluated", summary.hasEvaluated() ? 1L : 0L);
        // Do not encode an unevaluated period as a misleading zero-percent result.
        if (summary.hasEvaluated()) {
            facts.put("completion_percent", (long) summary.completionPercent());
        }
        for (Map.Entry<String, Long> entry : configuration.parameters().entrySet()) {
            facts.put("param_" + entry.getKey(), entry.getValue());
        }
        return new RuleContext(facts);
    }
}
