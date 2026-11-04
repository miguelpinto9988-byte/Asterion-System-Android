package pe.asterionsystem.app.presentation;

import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteFullException;
import pe.asterionsystem.app.R;

/**
 * Safe messages for storage failures; never exposes SQL, filesystem paths or
 * sensitive record values. No automatic retry of write operations.
 */
public final class OfflineErrorMessages {
    private OfflineErrorMessages() { }

    public static int forError(Throwable error) {
        Throwable current = error;
        for (int i = 0; current != null && i < 12; i++, current = current.getCause()) {
            if (current instanceof SQLiteDatabaseCorruptException) {
                return R.string.offline_error_integrity;
            }
            if (current instanceof SQLiteFullException) {
                return R.string.offline_error_full;
            }
            if (current instanceof SQLiteCantOpenDatabaseException
                    || current instanceof SQLiteDiskIOException) {
                return R.string.offline_error_unavailable;
            }
        }
        return R.string.offline_error_generic;
    }
}
