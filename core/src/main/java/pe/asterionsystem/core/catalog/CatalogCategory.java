package pe.asterionsystem.core.catalog;

import java.util.Objects;
import pe.asterionsystem.core.domain.EntityId;

/** Immutable category; separate from technical records and scheduling concepts. */
public final class CatalogCategory {
    private final EntityId id;
    private final String name;
    private final int order;

    public CatalogCategory(EntityId id, String name, int order) {
        this.id = Objects.requireNonNull(id, "id");
        if (id.value().length() > 64) {
            throw new IllegalArgumentException("Identificador de categoría demasiado largo");
        }
        String clean = Objects.requireNonNull(name, "name").trim();
        if (clean.isEmpty() || clean.length() > 80 || order < 0) {
            throw new IllegalArgumentException("Categoría inválida");
        }
        this.name = clean;
        this.order = order;
    }

    public EntityId id() { return id; }
    public String name() { return name; }
    public int order() { return order; }

    @Override public String toString() { return name; }
}
