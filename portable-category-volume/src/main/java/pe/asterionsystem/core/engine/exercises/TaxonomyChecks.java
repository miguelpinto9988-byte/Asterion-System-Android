package pe.asterionsystem.core.engine.exercises;

/** Strict, non-executable field validation for versioned catalog data. */
final class TaxonomyChecks {
    private TaxonomyChecks() { }
    static String id(String raw) {
        if (raw == null || !raw.matches("[a-z][a-z0-9_]{1,63}")) {
            throw new IllegalArgumentException("ID de catálogo inválido");
        }
        return raw;
    }
    static String token(String raw) {
        if (raw == null || !raw.matches("[a-z][a-z0-9_]{1,39}")) {
            throw new IllegalArgumentException("Código de catálogo inválido");
        }
        return raw;
    }
    static String label(String raw) {
        if (raw == null || raw.trim().isEmpty() || raw.length() > 120 ||
                raw.indexOf('\t') >= 0 || raw.indexOf('\n') >= 0) {
            throw new IllegalArgumentException("Nombre de catálogo inválido");
        }
        return raw.trim();
    }
}
