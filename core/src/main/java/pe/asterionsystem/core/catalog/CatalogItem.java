package pe.asterionsystem.core.catalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import pe.asterionsystem.core.domain.EntityId;

/** Immutable catalog item with validated, non-executable text metadata. */
public final class CatalogItem {
    private final EntityId id;
    private final String title;
    private final String description;
    private final CatalogItemKind kind;
    private final CatalogCategory category;
    private final List<String> tags;

    public CatalogItem(EntityId id, String title, String description,
                       CatalogItemKind kind, CatalogCategory category, List<String> tags) {
        this.id = Objects.requireNonNull(id, "id");
        if (id.value().length() > 64) {
            throw new IllegalArgumentException("ID de contenido demasiado largo");
        }
        this.title = validated(title, 120, false, "title");
        this.description = validated(description, 2000, true, "description");
        this.kind = Objects.requireNonNull(kind, "kind");
        this.category = Objects.requireNonNull(category, "category");
        Objects.requireNonNull(tags, "tags");
        if (tags.size() > 12) {
            throw new IllegalArgumentException("Demasiadas etiquetas");
        }
        List<String> safe = new ArrayList<>();
        for (String tag : tags) {
            String clean = validated(tag, 50, false, "tag");
            if (clean.indexOf('\u001f') >= 0 || safe.contains(clean)) {
                throw new IllegalArgumentException("Etiqueta duplicada o incompatible");
            }
            safe.add(clean);
        }
        this.tags = Collections.unmodifiableList(safe);
    }

    private static String validated(String text, int max, boolean emptyAllowed, String field) {
        String clean = Objects.requireNonNull(text, field).trim();
        if (clean.length() > max || (!emptyAllowed && clean.isEmpty())) {
            throw new IllegalArgumentException(field + " fuera de los límites permitidos");
        }
        return clean;
    }

    public EntityId id() { return id; }
    public String title() { return title; }
    public String description() { return description; }
    public CatalogItemKind kind() { return kind; }
    public CatalogCategory category() { return category; }
    public List<String> tags() { return tags; }
}
