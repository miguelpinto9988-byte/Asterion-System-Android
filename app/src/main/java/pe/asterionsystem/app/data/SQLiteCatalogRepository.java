package pe.asterionsystem.app.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Looper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import pe.asterionsystem.core.catalog.CatalogCategory;
import pe.asterionsystem.core.catalog.CatalogItem;
import pe.asterionsystem.core.catalog.CatalogItemKind;
import pe.asterionsystem.core.catalog.CatalogQuery;
import pe.asterionsystem.core.catalog.CatalogRepository;
import pe.asterionsystem.core.data.StorageException;
import pe.asterionsystem.core.domain.EntityId;

/**
 * Read-only, parameterized catalog adapter. All calls must be made on a worker.
 * Does not reuse technical-record tables or expose content authoring.
 */
public final class SQLiteCatalogRepository implements CatalogRepository {
    private static final String SELECT_ITEMS =
            "SELECT i.id AS item_id, i.title AS item_title, "
            + "i.description AS item_description, i.kind AS item_kind, "
            + "c.id AS category_id, c.name AS category_name, "
            + "c.sort_order AS category_order, "
            + "COALESCE((SELECT group_concat(t.label, char(31)) "
            + "FROM catalog_item_tags it JOIN catalog_tags t ON t.id = it.tag_id "
            + "WHERE it.item_id = i.id), '') AS tag_list "
            + "FROM catalog_items i JOIN catalog_categories c ON c.id = i.category_id";
    private final AsterionDatabaseHelper helper;

    public SQLiteCatalogRepository(AsterionDatabaseHelper helper) {
        this.helper = Objects.requireNonNull(helper, "helper");
    }

    private static void requireWorker() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("La consulta SQLite debe ejecutarse fuera del hilo UI");
        }
    }

    private static List<String> tagsFrom(Cursor cursor) {
        String text = cursor.getString(cursor.getColumnIndexOrThrow("tag_list"));
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(text.split(String.valueOf((char)31), -1));
    }

    private static CatalogItem itemFrom(Cursor cursor) {
        CatalogCategory category = new CatalogCategory(
                new EntityId(cursor.getString(cursor.getColumnIndexOrThrow("category_id"))),
                cursor.getString(cursor.getColumnIndexOrThrow("category_name")),
                cursor.getInt(cursor.getColumnIndexOrThrow("category_order")));
        return new CatalogItem(
                new EntityId(cursor.getString(cursor.getColumnIndexOrThrow("item_id"))),
                cursor.getString(cursor.getColumnIndexOrThrow("item_title")),
                cursor.getString(cursor.getColumnIndexOrThrow("item_description")),
                CatalogItemKind.fromDatabaseValue(cursor.getString(
                        cursor.getColumnIndexOrThrow("item_kind"))),
                category, tagsFrom(cursor));
    }

    @Override public List<CatalogCategory> listCategories() {
        requireWorker();
        ArrayList<CatalogCategory> categories = new ArrayList<>();
        try (Cursor cursor = helper.getReadableDatabase().rawQuery(
                "SELECT id,name,sort_order FROM catalog_categories "
                        + "ORDER BY sort_order ASC, id ASC", new String[0])) {
            while (cursor.moveToNext()) {
                categories.add(new CatalogCategory(
                        new EntityId(cursor.getString(0)),
                        cursor.getString(1), cursor.getInt(2)));
            }
            return categories;
        } catch (SQLiteException failure) {
            throw new StorageException("No se pudieron consultar las categorías", failure);
        }
    }

    @Override public Optional<CatalogItem> findById(EntityId id) {
        requireWorker();
        String key = Objects.requireNonNull(id, "id").value();
        if (key.length() > 64) {
            throw new IllegalArgumentException("ID de catálogo demasiado largo");
        }
        try (Cursor cursor = helper.getReadableDatabase().rawQuery(
                SELECT_ITEMS + " WHERE i.id = ? LIMIT 1", new String[]{key})) {
            return cursor.moveToFirst() ? Optional.of(itemFrom(cursor)) : Optional.empty();
        } catch (SQLiteException failure) {
            throw new StorageException("No se pudo consultar el elemento", failure);
        }
    }

    private static String literalLike(String input) {
        // Escape wildcard characters so the search always treats input as text.
        return "%" + input.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    }

    @Override public List<CatalogItem> search(CatalogQuery query) {
        requireWorker();
        Objects.requireNonNull(query, "query");
        StringBuilder sql = new StringBuilder(SELECT_ITEMS).append(" WHERE 1=1");
        ArrayList<String> args = new ArrayList<>();
        if (!query.text().isEmpty()) {
            sql.append(" AND (i.title LIKE ? ESCAPE '!' "
                    + "OR i.description LIKE ? ESCAPE '!' "
                    + "OR EXISTS (SELECT 1 FROM catalog_item_tags jt "
                    + "JOIN catalog_tags t ON t.id = jt.tag_id "
                    + "WHERE jt.item_id = i.id AND t.label LIKE ? ESCAPE '!'))");
            String pattern = literalLike(query.text());
            args.add(pattern);
            args.add(pattern);
            args.add(pattern);
        }
        if (query.categoryId() != null) {
            sql.append(" AND i.category_id = ?");
            args.add(query.categoryId().value());
        }
        if (query.kind() != null) {
            sql.append(" AND i.kind = ?");
            args.add(query.kind().databaseValue());
        }
        sql.append(" ORDER BY i.title COLLATE NOCASE ASC, i.id ASC LIMIT ? OFFSET ?");
        args.add(Integer.toString(query.limit()));
        args.add(Integer.toString(query.offset()));
        ArrayList<CatalogItem> result = new ArrayList<>();
        try (Cursor cursor = helper.getReadableDatabase().rawQuery(
                sql.toString(), args.toArray(new String[0]))) {
            while (cursor.moveToNext()) {
                result.add(itemFrom(cursor));
            }
            return result;
        } catch (SQLiteException failure) {
            throw new StorageException("No se pudo filtrar el catálogo", failure);
        }
    }
}
