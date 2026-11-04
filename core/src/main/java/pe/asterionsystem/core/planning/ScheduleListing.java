package pe.asterionsystem.core.planning;

import java.util.Objects;

/** Presentation-neutral schedule with the catalog activity's current title. */
public final class ScheduleListing {
    private final ScheduledActivity entry;
    private final String title;

    public ScheduleListing(ScheduledActivity entry, String title) {
        this.entry = Objects.requireNonNull(entry, "entry");
        this.title = Objects.requireNonNull(title, "title");
    }

    public ScheduledActivity entry() { return entry; }
    public String title() { return title; }
}
