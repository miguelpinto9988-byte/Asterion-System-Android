package pe.asterionsystem.core.data;

import java.util.Objects;
import pe.asterionsystem.core.domain.EntityId;

/**
 * Immutable technical test record, not a task, workout or other business entity.
 * Java-side limits are applied before any SQLite operation.
 */
public final class TechnicalRecord {
    public static final int MAX_ID_LENGTH = 64;
    public static final int MAX_TITLE_LENGTH = 120;
    public static final int MAX_NOTE_LENGTH = 1000;

    private final EntityId id;
    private final String title;
    private final String note;
    private final long createdAtMs;
    private final long updatedAtMs;

    public TechnicalRecord(EntityId id, String title, String note,
                           long createdAtMs, long updatedAtMs) {
        this.id = Objects.requireNonNull(id, "id");
        if (id.value().length() > MAX_ID_LENGTH) {
            throw new IllegalArgumentException("Identificador demasiado largo");
        }
        String cleanTitle = Objects.requireNonNull(title, "title").trim();
        if (cleanTitle.isEmpty() || cleanTitle.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("El título debe tener entre 1 y 120 caracteres");
        }
        String cleanNote = Objects.requireNonNull(note, "note").trim();
        if (cleanNote.length() > MAX_NOTE_LENGTH) {
            throw new IllegalArgumentException("Nota demasiado larga");
        }
        if (createdAtMs < 0 || updatedAtMs < createdAtMs) {
            throw new IllegalArgumentException("Tiempos de registro inconsistentes");
        }
        this.title = cleanTitle;
        this.note = cleanNote;
        this.createdAtMs = createdAtMs;
        this.updatedAtMs = updatedAtMs;
    }

    public EntityId id() { return id; }
    public String title() { return title; }
    public String note() { return note; }
    public long createdAtMs() { return createdAtMs; }
    public long updatedAtMs() { return updatedAtMs; }

    /** Editing must preserve identity and original creation timestamp. */
    public TechnicalRecord withContent(String newTitle, String newNote, long updatedAtMs) {
        return new TechnicalRecord(id, newTitle, newNote, createdAtMs, updatedAtMs);
    }

    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof TechnicalRecord)) return false;
        TechnicalRecord that = (TechnicalRecord) other;
        return id.equals(that.id) && title.equals(that.title) && note.equals(that.note)
                && createdAtMs == that.createdAtMs && updatedAtMs == that.updatedAtMs;
    }

    @Override public int hashCode() {
        return Objects.hash(id, title, note, createdAtMs, updatedAtMs);
    }
}
