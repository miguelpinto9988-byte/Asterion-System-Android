package pe.asterionsystem.app.presentation;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import pe.asterionsystem.app.R;
import pe.asterionsystem.app.data.AsterionDatabaseHelper;
import pe.asterionsystem.core.catalog.BackgroundCatalogOperations;
import pe.asterionsystem.core.catalog.CatalogCategory;
import pe.asterionsystem.core.catalog.CatalogItem;
import pe.asterionsystem.core.catalog.CatalogItemKind;
import pe.asterionsystem.core.catalog.CatalogQuery;
import pe.asterionsystem.core.domain.EntityId;

/**
 * Binds provisional XML views to catalog read ports.
 * All DB work is off-main-thread; only this presenter updates Android Views.
 */
public final class CatalogPresenter implements AutoCloseable {
    private static final String KEY_SEARCH = "asterion.catalog.search.v1";
    private static final long SEARCH_DEBOUNCE_MS = 280L;
    private static final String KEY_CATEGORY = "asterion.catalog.category.v1";
    private static final String KEY_KIND = "asterion.catalog.kind.v1";

    private final Activity activity;
    private final AsterionDatabaseHelper database;
    private final BackgroundCatalogOperations operations;
    private final Handler main = new Handler(Looper.getMainLooper());
    private View activeScreen;
    private long generation;
    private long listBindingGeneration;
    private boolean closed;
    private Runnable pendingSearch;
    private EditText watchedSearch;
    private TextWatcher searchWatcher;
    private String searchText = "";
    private String categoryId = "";
    private CatalogItemKind kind;

    public CatalogPresenter(Activity activity, AsterionDatabaseHelper database,
                            BackgroundCatalogOperations operations) {
        this.activity = activity;
        this.database = database;
        this.operations = operations;
    }

    public void restore(Bundle input) {
        if (input == null) return;
        String savedSearch = input.getString(KEY_SEARCH, "");
        if (savedSearch != null && savedSearch.length() <= 120) {
            searchText = savedSearch;
        }
        String savedCategory = input.getString(KEY_CATEGORY, "");
        if (savedCategory != null && savedCategory.length() <= 64) {
            categoryId = savedCategory;
        }
        String savedKind = input.getString(KEY_KIND, "");
        if (savedKind != null && !savedKind.isEmpty()) {
            try {
                kind = CatalogItemKind.fromDatabaseValue(savedKind);
            } catch (IllegalArgumentException ignored) {
                kind = null;
            }
        }
    }

    public void save(Bundle output) {
        output.putString(KEY_SEARCH, searchText);
        output.putString(KEY_CATEGORY, categoryId);
        output.putString(KEY_KIND, kind == null ? "" : kind.databaseValue());
    }

    private boolean isCurrent(View screen, long requestGeneration) {
        return !closed && activeScreen == screen && generation == requestGeneration;
    }

    private void cancelPendingSearch() {
        if (pendingSearch != null) {
            main.removeCallbacks(pendingSearch);
            pendingSearch = null;
        }
    }

    private void detachWatcher() {
        if (watchedSearch != null && searchWatcher != null) {
            watchedSearch.removeTextChangedListener(searchWatcher);
        }
        watchedSearch = null;
        searchWatcher = null;
    }

    public void bindList(View screen, Consumer<EntityId> openDetail, Runnable goBack) {
        cancelPendingSearch();
        detachWatcher();
        activeScreen = screen;
        final long listBinding = ++listBindingGeneration;
        ++generation;
        final EditText search = screen.findViewById(R.id.catalog_search);
        final Spinner categories = screen.findViewById(R.id.catalog_category_filter);
        final Spinner kinds = screen.findViewById(R.id.catalog_type_filter);
        final ListView results = screen.findViewById(R.id.catalog_results);
        final TextView status = screen.findViewById(R.id.catalog_status);
        Button back = screen.findViewById(R.id.catalog_back_home);
        back.setOnClickListener(view -> goBack.run());
        screen.<Button>findViewById(R.id.catalog_retry)
                .setOnClickListener(view -> bindList(screen, openDetail, goBack));

        final List<CatalogCategory> availableCategories = new ArrayList<>();
        final List<CatalogItem> visibleItems = new ArrayList<>();
        final ArrayAdapter<String> itemAdapter = new PreferencesRowAdapter(activity);
        results.setAdapter(itemAdapter);
        results.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < visibleItems.size() && !closed && activeScreen == screen) {
                openDetail.accept(visibleItems.get(position).id());
            }
        });

        final Runnable refresh = () -> {
            if (closed || activeScreen != screen || listBindingGeneration != listBinding) return;
            final CatalogQuery query;
            try {
                query = new CatalogQuery(searchText,
                        categoryId.isEmpty() ? null : new EntityId(categoryId),
                        kind, 50, 0);
            } catch (IllegalArgumentException invalid) {
                status.setText(R.string.catalog_error);
                return;
            }
            final long request = ++generation;
            status.setText(R.string.catalog_loading);
            operations.search(query).whenComplete((items, error) -> main.post(() -> {
                if (closed || activeScreen != screen || generation != request
                        || listBindingGeneration != listBinding) return;
                if (error != null) {
                    status.setText(OfflineErrorMessages.forError(error));
                    visibleItems.clear();
                    itemAdapter.clear();
                    return;
                }
                visibleItems.clear();
                visibleItems.addAll(items);
                itemAdapter.clear();
                for (CatalogItem item : items) {
                    String type = activity.getString(item.kind() == CatalogItemKind.CONTENT
                            ? R.string.catalog_type_content : R.string.catalog_type_activity);
                    itemAdapter.add(item.title() + " · " + type + "\n" + item.category().name());
                }
                status.setText(items.isEmpty()
                        ? activity.getString(R.string.catalog_empty)
                        : activity.getString(R.string.catalog_results_count, items.size()));
            }));
        };

        // A fixed session token guards callbacks after a screen replacement.
        final Runnable safeRefresh = () -> {
            if (closed || activeScreen != screen || listBindingGeneration != listBinding) return;
            cancelPendingSearch();
            refresh.run();
        };

        search.setText(searchText);
        watchedSearch = search;
        searchWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchText = s.toString();
                if (closed || activeScreen != screen
                        || listBindingGeneration != listBinding) return;
                // Invalidate an outstanding query immediately, then wait for a typing pause.
                ++generation;
                cancelPendingSearch();
                Runnable task = () -> {
                    pendingSearch = null;
                    safeRefresh.run();
                };
                pendingSearch = task;
                status.setText(R.string.catalog_loading);
                main.postDelayed(task, SEARCH_DEBOUNCE_MS);
            }
            @Override public void afterTextChanged(Editable s) { }
        };
        search.addTextChangedListener(searchWatcher);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(activity,
                R.array.catalog_type_options, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        kinds.setAdapter(typeAdapter);
        kinds.setSelection(kind == null ? 0 : kind == CatalogItemKind.CONTENT ? 1 : 2);
        kinds.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View selected,
                                                  int index, long id) {
                kind = index == 1 ? CatalogItemKind.CONTENT :
                        index == 2 ? CatalogItemKind.ACTIVITY : null;
                safeRefresh.run();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        operations.categories().whenComplete((categoriesList, error) -> main.post(() -> {
            if (closed || activeScreen != screen || listBindingGeneration != listBinding) return;
            if (error != null) {
                status.setText(OfflineErrorMessages.forError(error));
                return;
            }
            availableCategories.clear();
            availableCategories.addAll(categoriesList);
            List<String> labels = new ArrayList<>();
            labels.add(activity.getString(R.string.catalog_all_categories));
            int selected = 0;
            for (int i = 0; i < availableCategories.size(); i++) {
                CatalogCategory item = availableCategories.get(i);
                labels.add(item.name());
                if (item.id().value().equals(categoryId)) selected = i + 1;
            }
            if (selected == 0) categoryId = "";
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    activity, android.R.layout.simple_spinner_item, labels);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categories.setAdapter(adapter);
            categories.setSelection(selected);
            categories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onItemSelected(AdapterView<?> parent, View view,
                                                      int position, long id) {
                    categoryId = position == 0 ? "" : availableCategories.get(position - 1).id().value();
                    safeRefresh.run();
                }
                @Override public void onNothingSelected(AdapterView<?> parent) { }
            });
            safeRefresh.run();
        }));
    }

    public void bindDetail(View screen, EntityId id, Runnable goBack) {
        cancelPendingSearch();
        detachWatcher();
        activeScreen = screen;
        ++listBindingGeneration;
        final long session = ++generation;
        Button back = screen.findViewById(R.id.catalog_detail_back);
        back.setOnClickListener(view -> goBack.run());
        screen.<Button>findViewById(R.id.catalog_detail_retry)
                .setOnClickListener(view -> bindDetail(screen, id, goBack));
        TextView title = screen.findViewById(R.id.catalog_detail_title);
        TextView type = screen.findViewById(R.id.catalog_detail_type);
        TextView category = screen.findViewById(R.id.catalog_detail_category);
        TextView description = screen.findViewById(R.id.catalog_detail_description);
        TextView tags = screen.findViewById(R.id.catalog_detail_tags);
        operations.findById(id).whenComplete((optional, error) -> main.post(() -> {
            if (!isCurrent(screen, session)) return;
            if (error != null) {
                title.setText(OfflineErrorMessages.forError(error));
                return;
            }
            if (!optional.isPresent()) {
                title.setText(R.string.catalog_missing);
                return;
            }
            CatalogItem item = optional.get();
            title.setText(item.title());
            type.setText(item.kind() == CatalogItemKind.CONTENT
                    ? R.string.catalog_type_content : R.string.catalog_type_activity);
            category.setText(activity.getString(R.string.catalog_category_label,
                    item.category().name()));
            description.setText(item.description());
            tags.setText(activity.getString(R.string.catalog_tags_label,
                    item.tags().isEmpty() ? "—" : String.join(", ", item.tags())));
        }));
    }

    @Override public void close() {
        if (closed) return;
        closed = true;
        cancelPendingSearch();
        detachWatcher();
        ++listBindingGeneration;
        activeScreen = null;
        ++generation;
        operations.closeAfter(database::close);
    }
}
