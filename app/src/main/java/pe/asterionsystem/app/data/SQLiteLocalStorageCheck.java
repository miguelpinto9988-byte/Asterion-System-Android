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
        try (Cursor cursor = database.rawQuery("PRAGMA foreign_key_check", null)) {
            // Any returned row is a violation. The query does not alter data.
            return !cursor.moveToFirst();
        }
    }

    private static long count(SQLiteDatabase database, String table) {
        // Values are fixed application-owned identifiers, never user input.
        try (Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + table, null)) {
            if (!cursor.moveToFirst()) throw new SQLiteException("No se pudo contar " + table);
            return cursor.getLong(0);
        }
    }

    @Override public LocalStorageReport inspect() {
        offMain();
        try {
            SQLiteDatabase database = helper.getReadableDatabase();
            boolean integrity = quickCheck(database);
            boolean references = foreignKeysOk(database);
            long catalog = count(database, "catalog_items");
            long plan = count(database, "planned_activities");
            long tracking = count(database, "tracking_results");
            return new LocalStorageReport(integrity, references, catalog, plan, tracking);
        } catch (SQLiteException failure) {
            // Do not try destructive recovery: caller can retry after resolving cause.
            throw new StorageException("No se pudo inspeccionar el almacenamiento", failure);
        }
    }
}
