package pe.asterionsystem.app.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Looper;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import pe.asterionsystem.core.data.StorageException;
import pe.asterionsystem.core.domain.EntityId;
import pe.asterionsystem.core.planning.ScheduledActivity;
import pe.asterionsystem.core.progress.ProgressEntry;
import pe.asterionsystem.core.progress.ProgressRepository;
import pe.asterionsystem.core.progress.TrackingRecord;
import pe.asterionsystem.core.progress.TrackingStatus;

/**
 * SQLite phase-09 implementation, independent from planning edits.
 * All calls are synchronous and disallowed on the Android UI thread.
 */
public final class SQLiteProgressRepository implements ProgressRepository {
    private static final String TABLE = "tracking_results";
    private static final String BASE_QUERY =
            "SELECT p.id, p.catalog_item_id, p.local_date, p.start_minute, "
            + "p.duration_minutes, p.created_at_ms, p.updated_at_ms, c.title, "
            + "r.status, r.note "
            + "FROM planned_activities p "
            + "JOIN catalog_items c ON c.id = p.catalog_item_id "
            + "LEFT JOIN tracking_results r ON r.schedule_id = p.id ";
    private final AsterionDatabaseHelper helper;

    public SQLiteProgressRepository(AsterionDatabaseHelper helper) {
        this.helper = Objects.requireNonNull(helper, "helper");
    }

    private static void offMainThread() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("Seguimiento SQLite no permitido en el hilo principal");
        }
    }

    private static ProgressEntry row(Cursor c) {
        ScheduledActivity scheduled = new ScheduledActivity(
                new EntityId(c.getString(0)),
                new EntityId(c.getString(1)),
                LocalDate.parse(c.getString(2)),
                c.getInt(3), c.getInt(4),
                c.getLong(5), c.getLong(6));
        String persisted = c.getString(8);
        TrackingStatus status = persisted == null
                ? TrackingStatus.PENDING : TrackingStatus.fromStored(persisted);
        String note = status == TrackingStatus.PENDING ? "" : c.getString(9);
        return new ProgressEntry(scheduled, c.getString(7), status, note);
    }

    private List<ProgressEntry> query(String where, String[] args) {
        List<ProgressEntry> rows = new ArrayList<>();
        try (Cursor cursor = helper.getReadableDatabase().rawQuery(
                BASE_QUERY + where + " ORDER BY p.local_date ASC, p.start_minute ASC, p.id ASC",
                args)) {
            while (cursor.moveToNext()) rows.add(row(cursor));
            return rows;
        } catch (SQLiteException failure) {
            throw new StorageException("No se pudieron consultar los resultados", failure);
        }
    }

    @Override public List<ProgressEntry> forDate(LocalDate date) {
        offMainThread();
        return query("WHERE p.local_date = ?",
                new String[] {Objects.requireNonNull(date, "date").toString()});
    }

    @Override public List<ProgressEntry> forWeek(LocalDate monday) {
        offMainThread();
        Objects.requireNonNull(monday, "monday");
        if (monday.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new IllegalArgumentException("Semana debe comenzar el lunes");
        }
        return query("WHERE p.local_date >= ? AND p.local_date < ?",
                new String[]{monday.toString(), monday.plusDays(7).toString()});
    }

    @Override public void save(TrackingRecord record) {
        offMainThread();
        Objects.requireNonNull(record, "record");
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            db.beginTransaction();
            try {
                ContentValues values = new ContentValues();
                values.put("status", record.status().value());
                values.put("note", record.note());
                values.put("recorded_at_ms", record.recordedAtMs());
                int modified = db.update(TABLE, values, "schedule_id = ?",
                        new String[]{record.scheduleId().value()});
                if (modified == 0) {
                    values.put("schedule_id", record.scheduleId().value());
                    db.insertOrThrow(TABLE, null, values);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (SQLiteException failure) {
            throw new StorageException("No se pudo guardar el seguimiento", failure);
        }
    }

    @Override public boolean clear(EntityId scheduleId) {
        offMainThread();
        String raw = Objects.requireNonNull(scheduleId, "scheduleId").value();
        if (raw.length() > 64) throw new IllegalArgumentException("ID inválido");
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            db.beginTransaction();
            try {
                int removed = db.delete(TABLE, "schedule_id = ?", new String[]{raw});
                db.setTransactionSuccessful();
                return removed > 0;
            } finally {
                db.endTransaction();
            }
        } catch (SQLiteException failure) {
            throw new StorageException("No se pudo limpiar el seguimiento", failure);
        }
    }
}
