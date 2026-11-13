package pe.asterionsystem.core.engine.exercises;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Descriptive variation. Difficulty measures technical complexity, NOT a
 * person's ability or a recommendation to attempt it.
 */
public final class ExerciseVariant {
    private final String id, typeId, name, execution;
    private final int difficulty;
    private final List<String> equipment;
    private final boolean supervisionRecommended;

    public ExerciseVariant(String id, String typeId, String name, int difficulty,
                           String equipmentCodes, String execution,
                           boolean supervisionRecommended) {
        this.id = TaxonomyChecks.id(id);
        this.typeId = TaxonomyChecks.id(typeId);
        this.name = TaxonomyChecks.label(name);
        if (difficulty < 1 || difficulty > 5) {
            throw new IllegalArgumentException("Nivel técnico fuera de 1..5");
        }
        this.difficulty = difficulty;
        if (equipmentCodes == null || equipmentCodes.isEmpty()) {
            throw new IllegalArgumentException("Equipamiento sin especificar");
        }
        ArrayList<String> items = new ArrayList<>();
        for (String equipmentCode : equipmentCodes.split("\\+", -1)) {
            String token = TaxonomyChecks.token(equipmentCode);
            if (items.contains(token)) throw new IllegalArgumentException("Material duplicado");
            items.add(token);
        }
        if (items.size() > 4) throw new IllegalArgumentException("Demasiados materiales");
        this.equipment = Collections.unmodifiableList(items);
        this.execution = TaxonomyChecks.token(execution);
        if (difficulty >= 4 && !supervisionRecommended) {
            throw new IllegalArgumentException("Variantes técnicas avanzadas requieren orientación");
        }
        this.supervisionRecommended = supervisionRecommended;
    }

    public String id() { return id; }
    public String typeId() { return typeId; }
    public String name() { return name; }
    public int difficulty() { return difficulty; }
    public List<String> equipment() { return equipment; }
    public String execution() { return execution; }
    public boolean supervisionRecommended() { return supervisionRecommended; }
    /** No automated recommendations for high-complexity movements. */
    public boolean eligibleForAutomaticSuggestion() {
        return difficulty <= 3 && !supervisionRecommended;
    }
}
