package pe.asterionsystem.app.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Looper;
import java.util.Objects;
import pe.asterionsystem.core.data.StorageException;
import pe.asterionsystem.core.offline.LocalStorageCheck;
import pe.asterionsystem.core.offline.LocalStorageReport;

/**
 * Read-only SQLite inspection. No DROP, restore, remote sync, or data mutation.
 * Must run on a background worker, never on the main thread.
 */
public final class SQLiteLocalStorageCheck implements LocalStorageCheck {
    private final AsterionDatabaseHelper helper;

    public SQLiteLocalStorageCheck(AsterionDatabaseHelper helper) {
        this.helper = Objects.requireNonNull(helper, "helper");
    }

    private static void offMain() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("Diagnóstico SQLite en hilo principal");
        }
    }

    private static boolean quickCheck(SQLiteDatabase database) {
        try (Cursor cursor = database.rawQuery("PRAGMA quick_check(1)", null)) {
            return cursor.moveToFirst() && "ok".equalsIgnoreCase(cursor.getString(0));
        }
    }

    private static boolean foreignKeysOk(SQLiteDatabase database) {
        try (Cursor setting = database.rawQuery("PRAGMA foreign_keys", null)) {
            if (!setting.moveToFirst() || setting.getInt(0) != 1) return false;
        }
        try (Cursor cursor = database.rawQuery("PRAGMA foreign_key_check", null)) {
            // A returned row means a broken reference; never repair on this path.
            return !cursor.moveToFirst();
        }
    }

    private static long[] counts(SQLiteDatabase database) {
        // All three counts use a single read statement and one consistent snapshot.
        final String sql = "SELECT (SELECT COUNT(*) FROM catalog_items), "
                + "(SELECT COUNT(*) FROM planned_activities), "
                + "(SELECT COUNT(*) FROM tracking_results)";
        try (Cursor cursor = database.rawQuery(sql, null)) {
            if (!cursor.moveToFirst()) throw new SQLiteException("No se pudieron consultar los contadores");
            return new long[]{cursor.getLong(0), cursor.getLong(1), cursor.getLong(2)};
        }
    }

    @Override public LocalStorageReport inspect() {
        offMain();
        try {
            SQLiteDatabase database = helper.getReadableDatabase();
            boolean integrity = quickCheck(database);
            boolean references = foreignKeysOk(database);
            long[] summary = counts(database);
            return new LocalStorageReport(integrity, references,
                    summary[0], summary[1], summary[2]);
        } catch (SQLiteException failure) {
            // Do not try destructive recovery: caller can retry after resolving cause.
            throw new StorageException("No se pudo inspeccionar el almacenamiento", failure);
        }
    }
}
