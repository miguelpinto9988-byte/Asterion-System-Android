package pe.asterionsystem.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Private Android SQLite database. Migrations 1->2->3->4->5 are additive and
 * run transactionally inside SQLiteOpenHelper. Never drop existing records.
 */
public final class AsterionDatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "asterion_internal.db";
    public static final int DATABASE_VERSION = 5;
    public static final String TABLE_RECORDS = "technical_records";

    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_NOTE = "note";
    public static final String COL_CREATED = "created_at_ms";
    public static final String COL_UPDATED = "updated_at_ms";

    // Phase-05 SQL kept unchanged for backwards compatibility.
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

    // Phase 07: catalog-only tables. No planning, progress or media.
    public static final String CREATE_CATEGORIES =
            "CREATE TABLE catalog_categories ("
            + "id TEXT NOT NULL PRIMARY KEY CHECK(length(id) BETWEEN 1 AND 64), "
            + "name TEXT NOT NULL CHECK(length(name) BETWEEN 1 AND 80), "
            + "sort_order INTEGER NOT NULL DEFAULT 0 CHECK(sort_order >= 0)"
            + ")";

    public static final String CREATE_ITEMS =
            "CREATE TABLE catalog_items ("
            + "id TEXT NOT NULL PRIMARY KEY CHECK(length(id) BETWEEN 1 AND 64), "
            + "title TEXT NOT NULL CHECK(length(title) BETWEEN 1 AND 120), "
            + "description TEXT NOT NULL CHECK(length(description) <= 2000), "
            + "kind TEXT NOT NULL CHECK(kind IN ('content','activity')), "
            + "category_id TEXT NOT NULL REFERENCES catalog_categories(id) ON DELETE RESTRICT"
            + ")";

    public static final String CREATE_TAGS =
            "CREATE TABLE catalog_tags ("
            + "id TEXT NOT NULL PRIMARY KEY CHECK(length(id) BETWEEN 1 AND 64), "
            + "label TEXT NOT NULL UNIQUE CHECK(length(label) BETWEEN 1 AND 50)"
            + ")";

    public static final String CREATE_ITEM_TAGS =
            "CREATE TABLE catalog_item_tags ("
            + "item_id TEXT NOT NULL REFERENCES catalog_items(id) ON DELETE CASCADE, "
            + "tag_id TEXT NOT NULL REFERENCES catalog_tags(id) ON DELETE CASCADE, "
            + "PRIMARY KEY(item_id, tag_id)"
            + ")";

    public static final String CREATE_CATEGORY_INDEX =
            "CREATE INDEX idx_catalog_category_sort "
            + "ON catalog_categories (sort_order, id)";

    public static final String CREATE_ITEM_INDEX =
            "CREATE INDEX idx_catalog_item_filters "
            + "ON catalog_items (category_id, kind, title COLLATE NOCASE, id)";


    // Phase 08: one-day local wall-time plan; half-open intervals [start, end).
    public static final String CREATE_PLAN_TABLE =
            "CREATE TABLE planned_activities ("
            + "id TEXT NOT NULL PRIMARY KEY CHECK(length(id) BETWEEN 1 AND 64), "
            + "catalog_item_id TEXT NOT NULL REFERENCES catalog_items(id) ON DELETE RESTRICT, "
            + "local_date TEXT NOT NULL CHECK(length(local_date) = 10), "
            + "start_minute INTEGER NOT NULL CHECK(start_minute BETWEEN 0 AND 1439), "
            + "duration_minutes INTEGER NOT NULL CHECK(duration_minutes BETWEEN 1 AND 720 "
            + "AND start_minute + duration_minutes <= 1440), "
            + "created_at_ms INTEGER NOT NULL CHECK(created_at_ms >= 0), "
            + "updated_at_ms INTEGER NOT NULL CHECK(updated_at_ms >= created_at_ms)"
            + ")";

    public static final String CREATE_PLAN_INDEX =
            "CREATE INDEX idx_plan_date_start ON planned_activities "
            + "(local_date, start_minute, id)";

    public static final String CREATE_PLAN_OVERLAP_INSERT =
            "CREATE TRIGGER trg_plan_overlap_insert BEFORE INSERT ON planned_activities "
            + "WHEN EXISTS (SELECT 1 FROM planned_activities existing "
            + "WHERE existing.local_date = NEW.local_date "
            + "AND existing.start_minute < NEW.start_minute + NEW.duration_minutes "
            + "AND NEW.start_minute < existing.start_minute + existing.duration_minutes) "
            + "BEGIN SELECT RAISE(ABORT, 'schedule_overlap'); END";

    public static final String CREATE_PLAN_OVERLAP_UPDATE =
            "CREATE TRIGGER trg_plan_overlap_update BEFORE UPDATE ON planned_activities "
            + "WHEN EXISTS (SELECT 1 FROM planned_activities existing "
            + "WHERE existing.id <> NEW.id AND existing.local_date = NEW.local_date "
            + "AND existing.start_minute < NEW.start_minute + NEW.duration_minutes "
            + "AND NEW.start_minute < existing.start_minute + existing.duration_minutes) "
            + "BEGIN SELECT RAISE(ABORT, 'schedule_overlap'); END";

    public static final String CREATE_PLAN_ACTIVITY_INSERT =
            "CREATE TRIGGER trg_plan_activity_insert BEFORE INSERT ON planned_activities "
            + "WHEN NOT EXISTS (SELECT 1 FROM catalog_items "
            + "WHERE id = NEW.catalog_item_id AND kind = 'activity') "
            + "BEGIN SELECT RAISE(ABORT, 'invalid_catalog_activity'); END";

    public static final String CREATE_PLAN_ACTIVITY_UPDATE =
            "CREATE TRIGGER trg_plan_activity_update BEFORE UPDATE ON planned_activities "
            + "WHEN NOT EXISTS (SELECT 1 FROM catalog_items "
            + "WHERE id = NEW.catalog_item_id AND kind = 'activity') "
            + "BEGIN SELECT RAISE(ABORT, 'invalid_catalog_activity'); END";


    // Phase 09. Absence of a row = PENDING. One outcome per scheduled occurrence.
    public static final String CREATE_TRACKING_RESULTS =
            "CREATE TABLE tracking_results ("
            + "schedule_id TEXT NOT NULL PRIMARY KEY "
            + "REFERENCES planned_activities(id) ON DELETE CASCADE, "
            + "status TEXT NOT NULL CHECK(status IN ('completed','skipped')), "
            + "note TEXT NOT NULL CHECK(length(note) <= 500), "
            + "recorded_at_ms INTEGER NOT NULL CHECK(recorded_at_ms >= 0)"
            + ")";

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
        migrateFromV2ToV3(db);
        migrateFromV3ToV4(db);
        migrateFromV4ToV5(db);
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int version = oldVersion;
        if (version == 1 && newVersion >= 2) {
            migrateFromV1ToV2(db);
            version = 2;
        }
        if (version == 2 && newVersion >= 3) {
            migrateFromV2ToV3(db);
            version = 3;
        }
        if (version == 3 && newVersion >= 4) {
            migrateFromV3ToV4(db);
            version = 4;
        }
        if (version == 4 && newVersion >= 5) {
            migrateFromV4ToV5(db);
            version = 5;
        }
        if (version != newVersion) {
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

    private static void migrateFromV2ToV3(SQLiteDatabase db) {
        db.execSQL(CREATE_CATEGORIES);
        db.execSQL(CREATE_ITEMS);
        db.execSQL(CREATE_TAGS);
        db.execSQL(CREATE_ITEM_TAGS);
        db.execSQL(CREATE_CATEGORY_INDEX);
        db.execSQL(CREATE_ITEM_INDEX);
        seedDemonstrationCatalog(db);
    }

    private static void migrateFromV4ToV5(SQLiteDatabase db) {
        db.execSQL(CREATE_TRACKING_RESULTS);
    }

    private static void migrateFromV3ToV4(SQLiteDatabase db) {
        db.execSQL(CREATE_PLAN_TABLE);
        db.execSQL(CREATE_PLAN_INDEX);
        db.execSQL(CREATE_PLAN_OVERLAP_INSERT);
        db.execSQL(CREATE_PLAN_OVERLAP_UPDATE);
        db.execSQL(CREATE_PLAN_ACTIVITY_INSERT);
        db.execSQL(CREATE_PLAN_ACTIVITY_UPDATE);
    }

    private static void seedDemonstrationCatalog(SQLiteDatabase db) {
        // Fixed technical examples, not content extracted from other applications.
        final String categorySql =
                "INSERT INTO catalog_categories(id,name,sort_order) VALUES (?,?,?)";
        db.execSQL(categorySql, new Object[]{"general", "General", 0});
        db.execSQL(categorySql, new Object[]{"conocimiento", "Conocimiento", 1});
        db.execSQL(categorySql, new Object[]{"practica", "Práctica", 2});

        final String itemSql =
                "INSERT INTO catalog_items(id,title,description,kind,category_id) VALUES (?,?,?,?,?)";
        db.execSQL(itemSql, new Object[]{
                "demo-intro", "Introducción al catálogo",
                "Contenido de demostración para verificar la consulta de información.",
                "content", "general"});
        db.execSQL(itemSql, new Object[]{
                "demo-notas", "Notas de ejemplo",
                "Ficha ilustrativa para revisar la búsqueda y las categorías.",
                "content", "conocimiento"});
        db.execSQL(itemSql, new Object[]{
                "demo-revision", "Actividad de comprobación",
                "Actividad técnica de ejemplo; no ejecuta planificación ni seguimiento.",
                "activity", "practica"});
        db.execSQL(itemSql, new Object[]{
                "demo-navegacion", "Revisión de navegación",
                "Actividad de demostración para abrir y cerrar pantallas.",
                "activity", "general"});

        final String tagSql = "INSERT INTO catalog_tags(id,label) VALUES (?,?)";
        db.execSQL(tagSql, new Object[]{"demo", "Demostración"});
        db.execSQL(tagSql, new Object[]{"lectura", "Lectura"});
        db.execSQL(tagSql, new Object[]{"consulta", "Consulta"});
        db.execSQL(tagSql, new Object[]{"prueba", "Prueba"});

        final String relation =
                "INSERT INTO catalog_item_tags(item_id,tag_id) VALUES (?,?)";
        db.execSQL(relation, new Object[]{"demo-intro", "demo"});
        db.execSQL(relation, new Object[]{"demo-intro", "lectura"});
        db.execSQL(relation, new Object[]{"demo-notas", "demo"});
        db.execSQL(relation, new Object[]{"demo-notas", "consulta"});
        db.execSQL(relation, new Object[]{"demo-revision", "demo"});
        db.execSQL(relation, new Object[]{"demo-revision", "prueba"});
        db.execSQL(relation, new Object[]{"demo-navegacion", "demo"});
        db.execSQL(relation, new Object[]{"demo-navegacion", "prueba"});
    }
}
