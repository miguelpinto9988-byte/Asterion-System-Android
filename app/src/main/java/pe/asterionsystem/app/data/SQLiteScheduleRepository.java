package pe.asterionsystem.app.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Looper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import pe.asterionsystem.core.data.StorageException;
import pe.asterionsystem.core.domain.EntityId;
import pe.asterionsystem.core.planning.ScheduleConflictException;
import pe.asterionsystem.core.planning.ScheduleRepository;
import pe.asterionsystem.core.planning.ScheduledActivity;

/** Synchronous, transactional SQLite adapter. Never invoke on Android main thread. */
public final class SQLiteScheduleRepository implements ScheduleRepository {
    private static final String TABLE = "planned_activities";
    private static final String[] COLUMNS = {
            "id", "catalog_item_id", "local_date", "start_minute", "duration_minutes",
            "created_at_ms", "updated_at_ms"
    };
    private final AsterionDatabaseHelper database;

    public SQLiteScheduleRepository(AsterionDatabaseHelper database) {
        this.database = Objects.requireNonNull(database, "database");
    }

    private static void offMain() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("SQLite planificación en hilo principal");
        }
    }

    private static String id(EntityId identifier) {
        String raw = Objects.requireNonNull(identifier, "identifier").value();
        if (raw.length() > 64) throw new IllegalArgumentException("ID demasiado largo");
        return raw;
    }

    private static ContentValues values(ScheduledActivity item) {
        ContentValues v = new ContentValues();
        v.put("id", item.id().value());
        v.put("catalog_item_id", item.activityId().value());
        v.put("local_date", item.date().toString());
        v.put("start_minute", item.startMinute());
        v.put("duration_minutes", item.durationMinutes());
        v.put("created_at_ms", item.createdAtMs());
        v.put("updated_at_ms", item.updatedAtMs());
        return v;
    }

    private static ScheduledActivity fromCursor(Cursor c) {
        return new ScheduledActivity(
                new EntityId(c.getString(c.getColumnIndexOrThrow("id"))),
                new EntityId(c.getString(c.getColumnIndexOrThrow("catalog_item_id"))),
                LocalDate.parse(c.getString(c.getColumnIndexOrThrow("local_date"))),
                c.getInt(c.getColumnIndexOrThrow("start_minute")),
                c.getInt(c.getColumnIndexOrThrow("duration_minutes")),
                c.getLong(c.getColumnIndexOrThrow("created_at_ms")),
                c.getLong(c.getColumnIndexOrThrow("updated_at_ms")));
    }

    private static RuntimeException failure(String operation, SQLiteException error) {
        String message = error.getMessage();
        if (message != null && message.toLowerCase(Locale.ROOT).contains("schedule_overlap")) {
            return new ScheduleConflictException("Horario ocupado por otra actividad", error);
        }
        return new StorageException("Error al " + operation + " la programación", error);
    }

    @Override public Optional<ScheduledActivity> find(EntityId entryId) {
        offMain();
        try (Cursor c = database.getReadableDatabase().query(
                TABLE, COLUMNS, "id = ?", new String[] {id(entryId)},
                null, null, null, "1")) {
            return c.moveToFirst() ? Optional.of(fromCursor(c)) : Optional.empty();
        } catch (SQLiteException e) {
            throw failure("consultar", e);
        }
    }

    @Override public List<ScheduledActivity> forDate(LocalDate date) {
        offMain();
        String local = Objects.requireNonNull(date, "date").toString();
        List<ScheduledActivity> rows = new ArrayList<>();
        try (Cursor c = database.getReadableDatabase().query(
                TABLE, COLUMNS, "local_date = ?", new String[]{local}, null, null,
                "start_minute ASC, id ASC")) {
            while (c.moveToNext()) rows.add(fromCursor(c));
            return rows;
        } catch (SQLiteException e) {
            throw failure("listar", e);
        }
    }

    @Override public void insert(ScheduledActivity entry) {
        offMain();
        Objects.requireNonNull(entry, "entry");
        SQLiteDatabase db = database.getWritableDatabase();
        try {
            db.beginTransaction();
            try {
                db.insertOrThrow(TABLE, null, values(entry));
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (SQLiteException e) {
            throw failure("crear", e);
        }
    }

    @Override public boolean update(ScheduledActivity entry) {
        offMain();
        Objects.requireNonNull(entry, "entry");
        ContentValues changed = values(entry);
        changed.remove("id");
        changed.remove("created_at_ms");
        SQLiteDatabase db = database.getWritableDatabase();
        try {
            db.beginTransaction();
            try {
                int updated = db.update(TABLE, changed, "id = ? AND created_at_ms = ?",
                        new String[] {entry.id().value(), Long.toString(entry.createdAtMs())});
                db.setTransactionSuccessful();
                return updated > 0;
            } finally {
                db.endTransaction();
            }
        } catch (SQLiteException e) {
            throw failure("editar", e);
        }
    }

    @Override public boolean delete(EntityId entryId) {
        offMain();
        SQLiteDatabase db = database.getWritableDatabase();
        try {
            db.beginTransaction();
            try {
                int removed = db.delete(TABLE, "id = ?", new String[]{id(entryId)});
                db.setTransactionSuccessful();
                return removed > 0;
            } finally {
                db.endTransaction();
            }
        } catch (SQLiteException e) {
            throw failure("eliminar", e);
        }
    }
}
