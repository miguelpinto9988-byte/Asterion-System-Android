package pe.asterionsystem.core.settings;

import java.util.Objects;

/** Immutable, presentation-neutral settings. No account, business data or Android SDK. */
public final class AppPreferences {
    public enum TextSize {
        NORMAL("normal"), LARGE("large");
        private final String stored;
        TextSize(String stored) { this.stored = stored; }
        public String stored() { return stored; }
        public static TextSize fromStored(String value) {
            return LARGE.stored.equals(value) ? LARGE : NORMAL;
        }
    }

    public enum ListDensity {
        NORMAL("normal"), COMPACT("compact");
        private final String stored;
        ListDensity(String stored) { this.stored = stored; }
        public String stored() { return stored; }
        public static ListDensity fromStored(String value) {
            return COMPACT.stored.equals(value) ? COMPACT : NORMAL;
        }
    }

    public enum StartSection {
        HOME("home"), CATALOG("catalog"), PLAN("plan"), PROGRESS("progress");
        private final String stored;
        StartSection(String stored) { this.stored = stored; }
        public String stored() { return stored; }
        public static StartSection fromStored(String value) {
            for (StartSection section : values()) {
                if (section.stored.equals(value)) return section;
            }
            return HOME;
        }
    }

    private final TextSize textSize;
    private final ListDensity listDensity;
    private final StartSection startSection;

    public AppPreferences(TextSize textSize, ListDensity listDensity, StartSection startSection) {
        this.textSize = Objects.requireNonNull(textSize, "textSize");
        this.listDensity = Objects.requireNonNull(listDensity, "listDensity");
        this.startSection = Objects.requireNonNull(startSection, "startSection");
    }

    public static AppPreferences defaults() {
        return new AppPreferences(TextSize.NORMAL, ListDensity.NORMAL, StartSection.HOME);
    }

    public TextSize textSize() { return textSize; }
    public ListDensity listDensity() { return listDensity; }
    public StartSection startSection() { return startSection; }

    public AppPreferences withTextSize(TextSize next) {
        return new AppPreferences(next, listDensity, startSection);
    }
    public AppPreferences withListDensity(ListDensity next) {
        return new AppPreferences(textSize, next, startSection);
    }
    public AppPreferences withStartSection(StartSection next) {
        return new AppPreferences(textSize, listDensity, next);
    }

    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof AppPreferences)) return false;
        AppPreferences that = (AppPreferences) other;
        return textSize == that.textSize && listDensity == that.listDensity
                && startSection == that.startSection;
    }

    @Override public int hashCode() { return Objects.hash(textSize, listDensity, startSection); }

    @Override public String toString() {
        return "AppPreferences{" + textSize + "," + listDensity + "," + startSection + "}";
    }
}
