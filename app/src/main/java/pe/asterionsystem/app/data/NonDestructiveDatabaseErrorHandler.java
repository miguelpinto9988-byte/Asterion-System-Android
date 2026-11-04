package pe.asterionsystem.app.data;

import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Android's stock corruption handler may delete a damaged database.
 * Phase 14 deliberately preserves the file for authorized inspection/recovery.
 * No deletion, restoration, export, or repair takes place automatically.
 */
public final class NonDestructiveDatabaseErrorHandler implements DatabaseErrorHandler {
    @Override public void onCorruption(SQLiteDatabase database) {
        // No database path or record values are logged.
        Log.e("AsterionLocalStorage", "Se detectó una incidencia de integridad SQLite; sin borrado automático");
    }
}
