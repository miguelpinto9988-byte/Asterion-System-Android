package pe.asterionsystem.core.catalog;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import pe.asterionsystem.core.data.RecordReader;
import pe.asterionsystem.core.domain.EntityId;

/** Read-only phase-07 catalog contract. Future authoring is not implemented. */
public interface CatalogRepository extends RecordReader<EntityId, CatalogItem> {
    List<CatalogCategory> listCategories();
    List<CatalogItem> search(CatalogQuery query);

    @Override Optional<CatalogItem> findById(EntityId id);

    /**
     * Lookup titles in one request. Adapters may batch reads; default preserves
     * compatibility with existing in-memory and test repositories.
     */
    default Map<EntityId, String> titlesById(List<EntityId> ids) {
        Objects.requireNonNull(ids, "ids");
        Map<EntityId, String> result = new LinkedHashMap<>();
        for (EntityId id : ids) {
            Objects.requireNonNull(id, "item id");
            if (!result.containsKey(id)) {
                findById(id).ifPresent(item -> result.put(id, item.title()));
            }
        }
        return Collections.unmodifiableMap(result);
    }
}
