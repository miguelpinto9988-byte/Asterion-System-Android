package pe.asterionsystem.core.catalog;

/** Stable persisted values for the two phase-07 catalog entry types. */
public enum CatalogItemKind {
    CONTENT("content"),
    ACTIVITY("activity");

    private final String databaseValue;

    CatalogItemKind(String databaseValue) {
        this.databaseValue = databaseValue;
    }

    public String databaseValue() {
        return databaseValue;
    }

    public static CatalogItemKind fromDatabaseValue(String raw) {
        for (CatalogItemKind kind : values()) {
            if (kind.databaseValue.equals(raw)) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Tipo de catálogo desconocido: " + raw);
    }
}
