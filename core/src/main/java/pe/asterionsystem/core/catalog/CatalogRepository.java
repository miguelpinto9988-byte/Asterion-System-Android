package pe.asterionsystem.core.catalog;

import java.util.List;
import java.util.Optional;
import pe.asterionsystem.core.data.RecordReader;
import pe.asterionsystem.core.domain.EntityId;

/** Read-only phase-07 catalog contract. Future authoring is not implemented. */
public interface CatalogRepository extends RecordReader<EntityId, CatalogItem> {
    List<CatalogCategory> listCategories();
    List<CatalogItem> search(CatalogQuery query);

    @Override Optional<CatalogItem> findById(EntityId id);
}
