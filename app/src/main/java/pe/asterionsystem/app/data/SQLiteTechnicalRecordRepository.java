package pe.asterionsystem.app.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Looper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import pe.asterionsystem.core.data.StorageException;
import pe.asterionsystem.core.data.TechnicalRecord;
import pe.asterionsystem.core.data.TechnicalRecordRepository;
import pe.asterionsystem.core.domain.EntityId;

/**
 * Native SQLite adapter. All calls are synchronous and FORBIDDEN on the UI thread.
 * Use BackgroundRecordOperations; no Activity calls this repository yet.
 */
public final class SQLiteTechnicalRecordRepository implements TechnicalRecordRepository {
    private static final String[] COLUMNS = {
            AsterionDatabaseHelper.COL_ID,
            AsterionDatabaseHelper.COL_TITLE,
            AsterionDatabaseHelper.COL_NOTE,
            AsterionDatabaseHelper.COL_CREATED,
            AsterionDatabaseHelper.COL_UPDATED
    };

    private final AsterionDatabaseHelper helper;

    public SQLiteTechnicalRecordRepository(AsterionDatabaseHelper helper) {
        this.helper = Objects.requireNonNull(helper, "helper");
    }

    private static void checkWorkerThread() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("SQLite no debe ejecutarse en el hilo principal");
        }
    }

    private static String idOf(EntityId id) {
        String raw = Objects.requireNonNull(id, "id").value();
        if (raw.length() > TechnicalRecord.MAX_ID_LENGTH) {
            throw new IllegalArgumentException("Identificador demasiado largo");
        }
        return raw;
    }

    private static ContentValues forCreate(TechnicalRecord record) {
        ContentValues values = new ContentValues();
        values.put(AsterionDatabaseHelper.COL_ID, record.id().value());
        values.put(AsterionDatabaseHelper.COL_TITLE, record.title());
        values.put(AsterionDatabaseHelper.COL_NOTE, record.note());
        values.put(AsterionDatabaseHelper.COL_CREATED, record.createdAtMs());
        values.put(AsterionDatabaseHelper.COL_UPDATED, record.updatedAtMs());
        return values;
    }

    private static TechnicalRecord fromCursor(Cursor cursor) {
        return new TechnicalRecord(
                new EntityId(cursor.getString(cursor.getColumnIndexOrThrow(AsterionDatabaseHelper.COL_ID))),
                cursor.getString(cursor.getColumnIndexOrThrow(AsterionDatabaseHelper.COL_TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(AsterionDatabaseHelper.COL_NOTE)),
                cursor.getLong(cursor.getColumnIndexOrThrow(AsterionDatabaseHelper.COL_CREATED)),
                cursor.getLong(cursor.getColumnIndexOrThrow(AsterionDatabaseHelper.COL_UPDATED)));
    }

    @Override public void create(TechnicalRecord record) {
        checkWorkerThread();
        Objects.requireNonNull(record, "record");
        try {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.beginTransaction();
            try {
                db.insertOrThrow(AsterionDatabaseHelper.TABLE_RECORDS, null, forCreate(record));
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (SQLiteException failure) {
            throw new StorageException("No se pudo crear el registro; revise el identificador y las restricciones", failure);
        }
    }

    @Override public Optional<TechnicalRecord> findById(EntityId id) {
        checkWorkerThread();
        String recordId = idOf(id);
        try (Cursor cursor = helper.getReadableDatabase().query(
                AsterionDatabaseHelper.TABLE_RECORDS, COLUMNS,
                AsterionDatabaseHelper.COL_ID + " = ?",
                new String[] {recordId}, null, null, null, "1")) {
            return cursor.moveToFirst() ? Optional.of(fromCursor(cursor)) : Optional.empty();
        } catch (SQLiteException failure) {
            throw new StorageException("No se pudo consultar el registro", failure);
        }
    }

    @Override public boolean update(TechnicalRecord record) {
        checkWorkerThread();
        Objects.requireNonNull(record, "record");
        ContentValues values = new ContentValues();
        values.put(AsterionDatabaseHelper.COL_TITLE, record.title());
        values.put(AsterionDatabaseHelper.COL_NOTE, record.note());
        values.put(AsterionDatabaseHelper.COL_UPDATED, record.updatedAtMs());
        try {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.beginTransaction();
            try {
                // Matching the original creation timestamp prevents accidental identity overwrite.
                int rows = db.update(AsterionDatabaseHelper.TABLE_RECORDS, values,
                        AsterionDatabaseHelper.COL_ID + " = ? AND "
                                + AsterionDatabaseHelper.COL_CREATED + " = ?",
                        new String[] {record.id().value(), Long.toString(record.createdAtMs())});
                db.setTransactionSuccessful();
                return rows > 0;
            } finally {
                db.endTransaction();
            }
        } catch (SQLiteException failure) {
            throw new StorageException("No se pudo actualizar el registro", failure);
        }
    }

    @Override public boolean delete(EntityId id) {
        checkWorkerThread();
        String recordId = idOf(id);
        try {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.beginTransaction();
            try {
                int rows = db.delete(AsterionDatabaseHelper.TABLE_RECORDS,
                        AsterionDatabaseHelper.COL_ID + " = ?", new String[] {recordId});
                db.setTransactionSuccessful();
                return rows > 0;
            } finally {
                db.endTransaction();
            }
        } catch (SQLiteException failure) {
            throw new StorageException("No se pudo eliminar el registro", failure);
        }
    }

    @Override public List<TechnicalRecord> list(int limit, int offset) {
        checkWorkerThread();
        if (limit < 1 || limit > 100 || offset < 0) {
            throw new IllegalArgumentException("Paginación inválida (limit 1..100, offset >= 0)");
        }
        ArrayList<TechnicalRecord> result = new ArrayList<>();
        try (Cursor cursor = helper.getReadableDatabase().query(
                AsterionDatabaseHelper.TABLE_RECORDS, COLUMNS, null, null, null, null,
                AsterionDatabaseHelper.COL_UPDATED + " DESC, " + AsterionDatabaseHelper.COL_ID + " ASC",
                limit + " OFFSET " + offset)) {
            while (cursor.moveToNext()) {
                result.add(fromCursor(cursor));
            }
            return result;
        } catch (SQLiteException failure) {
            throw new StorageException("No se pudo listar los registros", failure);
        }
    }

    @Override public int count() {
        checkWorkerThread();
        try {
            long amount = DatabaseUtils.longForQuery(helper.getReadableDatabase(),
                    "SELECT COUNT(*) FROM " + AsterionDatabaseHelper.TABLE_RECORDS, null);
            return Math.toIntExact(amount);
        } catch (SQLiteException failure) {
            throw new StorageException("No se pudo contar los registros", failure);
        }
    }
}
