package pe.asterionsystem.core.engine.exercises;

/** One type of movement belonging to one family. */
public final class ExerciseType {
    private final String id, familyId, name;
    public ExerciseType(String id, String familyId, String name) {
        this.id = TaxonomyChecks.id(id);
        this.familyId = TaxonomyChecks.id(familyId);
        this.name = TaxonomyChecks.label(name);
    }
    public String id() { return id; }
    public String familyId() { return familyId; }
    public String name() { return name; }
}
