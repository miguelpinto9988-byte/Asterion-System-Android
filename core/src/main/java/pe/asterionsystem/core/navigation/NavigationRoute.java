package pe.asterionsystem.core.navigation;

/** Stable identifiers for phase-04 and phase-07 native XML screens. */
public enum NavigationRoute {
    HOME("home"),
    SECONDARY("secondary"),
    CATALOG("catalog"),
    CATALOG_DETAIL("catalog_detail"),
    PLAN("plan"),
    PLAN_FORM("plan_form"),
    PROGRESS("progress"),
    PROGRESS_SUMMARY("progress_summary"),
    SETTINGS("settings"),
    LOCAL_STATUS("local_status");

    private final String id;

    NavigationRoute(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static NavigationRoute fromId(String id) {
        for (NavigationRoute route : values()) {
            if (route.id.equals(id)) {
                return route;
            }
        }
        throw new IllegalArgumentException("Ruta no reconocida: " + id);
    }
}
