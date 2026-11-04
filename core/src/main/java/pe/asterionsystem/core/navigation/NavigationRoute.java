package pe.asterionsystem.core.navigation;

/** Stable identifiers for provisional phase-04 screens. */
public enum NavigationRoute {
    HOME("home"),
    SECONDARY("secondary");

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
