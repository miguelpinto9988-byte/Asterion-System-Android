package pe.asterionsystem.core.planning;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import pe.asterionsystem.core.catalog.CatalogItem;
import pe.asterionsystem.core.catalog.CatalogItemKind;
import pe.asterionsystem.core.catalog.CatalogQuery;
import pe.asterionsystem.core.catalog.CatalogRepository;
import pe.asterionsystem.core.domain.EntityId;

/**
 * Validates planning against the existing catalog and detects interval conflicts.
 * SQLite triggers are the authoritative atomic backstop against concurrent writes.
 * Does not execute activities, send reminders or compute progress.
 */
public final class SchedulePlanner {
    private final ScheduleRepository entries;
    private final CatalogRepository catalog;

    public SchedulePlanner(ScheduleRepository entries, CatalogRepository catalog) {
        this.entries = Objects.requireNonNull(entries, "entries");
        this.catalog = Objects.requireNonNull(catalog, "catalog");
    }

    public List<CatalogItem> activities() {
        return catalog.search(new CatalogQuery("", null, CatalogItemKind.ACTIVITY, 50, 0));
    }

    public Optional<ScheduledActivity> find(EntityId id) {
        return entries.find(Objects.requireNonNull(id, "id"));
    }

    public List<ScheduledActivity> forDate(LocalDate date) {
        return entries.forDate(Objects.requireNonNull(date, "date"));
    }

    public List<ScheduleListing> listWithTitles(LocalDate day) {
        List<ScheduleListing> rows = new ArrayList<>();
        for (ScheduledActivity entry : forDate(day)) {
            rows.add(new ScheduleListing(entry, titleFor(entry.activityId())));
        }
        return rows;
    }

    public String titleFor(EntityId id) {
        return catalog.findById(Objects.requireNonNull(id, "id"))
                .map(CatalogItem::title).orElse("Actividad no disponible");
    }

    public void create(ScheduledActivity proposed) {
        validate(proposed);
        entries.insert(proposed);
    }

    public boolean update(ScheduledActivity proposed) {
        validate(proposed);
        return entries.update(proposed);
    }

    public boolean delete(EntityId id) {
        return entries.delete(Objects.requireNonNull(id, "id"));
    }

    private void validate(ScheduledActivity proposed) {
        Objects.requireNonNull(proposed, "proposed");
        CatalogItem item = catalog.findById(proposed.activityId())
                .orElseThrow(() -> new IllegalArgumentException("Actividad no encontrada en catálogo"));
        if (item.kind() != CatalogItemKind.ACTIVITY) {
            throw new IllegalArgumentException("Solo se pueden programar actividades");
        }
        for (ScheduledActivity existing : entries.forDate(proposed.date())) {
            if (!existing.id().equals(proposed.id()) && existing.overlaps(proposed)) {
                throw new ScheduleConflictException("El horario coincide con otra actividad");
            }
        }
    }
}
