package pe.asterionsystem.core.engine.exercises;

import java.util.Objects;

/** Broad, stable movement family, independent of training programmes. */
public final class ExerciseFamily {
    private final String id, name, category, movementPattern;
    public ExerciseFamily(String id, String name, String category, String movementPattern) {
        this.id = TaxonomyChecks.id(id);
        this.name = TaxonomyChecks.label(name);
        this.category = TaxonomyChecks.token(category);
        this.movementPattern = TaxonomyChecks.token(movementPattern);
    }
    public String id() { return id; }
    public String name() { return name; }
    public String category() { return category; }
    public String movementPattern() { return movementPattern; }
}
