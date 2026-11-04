package pe.asterionsystem.core.catalog;

import java.util.Objects;
import pe.asterionsystem.core.domain.EntityId;

/** Filter input with strictly bounded pagination; null filters mean 'all'. */
public final class CatalogQuery {
    private final String text;
    private final EntityId categoryId;
    private final CatalogItemKind kind;
    private final int limit;
    private final int offset;

    public CatalogQuery(String text, EntityId categoryId, CatalogItemKind kind,
                        int limit, int offset) {
        String clean = Objects.requireNonNull(text, "text").trim();
        if (clean.length() > 120 || limit < 1 || limit > 50 || offset < 0) {
            throw new IllegalArgumentException("Filtro o paginación inválidos");
        }
        if (categoryId != null && categoryId.value().length() > 64) {
            throw new IllegalArgumentException("Categoría inválida");
        }
        this.text = clean;
        this.categoryId = categoryId;
        this.kind = kind;
        this.limit = limit;
        this.offset = offset;
    }

    public String text() { return text; }
    public EntityId categoryId() { return categoryId; }
    public CatalogItemKind kind() { return kind; }
    public int limit() { return limit; }
    public int offset() { return offset; }

    public static CatalogQuery initial() {
        return new CatalogQuery("", null, null, 50, 0);
    }
}
