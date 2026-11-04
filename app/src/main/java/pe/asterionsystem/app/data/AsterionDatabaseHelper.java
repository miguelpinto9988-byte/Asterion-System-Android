package pe.asterionsystem.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Private SQLite database for technical test records only.
 * Schema v1 -> v2 is additive, transactional (SQLiteOpenHelper), and non-destructive.
 */
public final class AsterionDatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "asterion_internal.db";
    public static final int DATABASE_VERSION = 2;
    public static final String TABLE_RECORDS = "technical_records";

    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_NOTE = "note";
    public static final String COL_CREATED = "created_at_ms";
    public static final String COL_UPDATED = "updated_at_ms";

    /** Public to enable source-level verification of the exact schema statements. */
    public static final String CREATE_V1 =
            "CREATE TABLE technical_records ("
            + "id TEXT NOT NULL PRIMARY KEY CHECK(length(id) BETWEEN 1 AND 64), "
            + "title TEXT NOT NULL CHECK(length(title) BETWEEN 1 AND 120), "
            + "note TEXT NOT NULL CHECK(length(note) <= 1000), "
            + "created_at_ms INTEGER NOT NULL CHECK(created_at_ms >= 0)"
            + ")";

    public static final String ALTER_V1_TO_V2 =
            "ALTER TABLE technical_records ADD COLUMN "
            + "updated_at_ms INTEGER NOT NULL DEFAULT 0 CHECK(updated_at_ms >= 0)";

    public static final String BACKFILL_V2 =
            "UPDATE technical_records SET updated_at_ms = created_at_ms";

    public static final String CREATE_UPDATED_INDEX =
            "CREATE INDEX idx_technical_records_updated "
            + "ON technical_records (updated_at_ms DESC, id ASC)";

    public static final String CREATE_INSERT_TRIGGER =
            "CREATE TRIGGER trg_records_timestamp_insert "
            + "BEFORE INSERT ON technical_records "
            + "WHEN NEW.updated_at_ms < NEW.created_at_ms "
            + "BEGIN SELECT RAISE(ABORT, 'updated_at_ms precedes created_at_ms'); END";

    public static final String CREATE_UPDATE_TRIGGER =
            "CREATE TRIGGER trg_records_timestamp_update "
            + "BEFORE UPDATE ON technical_records "
            + "WHEN NEW.updated_at_ms < NEW.created_at_ms "
            + "BEGIN SELECT RAISE(ABORT, 'updated_at_ms precedes created_at_ms'); END";

    public AsterionDatabaseHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
        setWriteAheadLoggingEnabled(true);
    }

    @Override public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_V1);
        migrateFromV1ToV2(db);
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int version = oldVersion;
        if (version == 1 && newVersion >= 2) {
            migrateFromV1ToV2(db);
            version = 2;
        }
        if (version != newVersion) {
            // Never silently drop or replace existing user data.
            throw new SQLiteException("Migración no disponible: " + oldVersion + " → " + newVersion);
        }
    }

    @Override public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new SQLiteException("No se permite rebajar el esquema sin migración explícita");
    }

    private static void migrateFromV1ToV2(SQLiteDatabase db) {
        db.execSQL(ALTER_V1_TO_V2);
        db.execSQL(BACKFILL_V2);
        db.execSQL(CREATE_UPDATED_INDEX);
        db.execSQL(CREATE_INSERT_TRIGGER);
        db.execSQL(CREATE_UPDATE_TRIGGER);
    }
}
