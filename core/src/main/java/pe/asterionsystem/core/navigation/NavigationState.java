package pe.asterionsystem.core.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Immutable navigation history: HOME first, never empty, never repeated at the top. */
public final class NavigationState {
    public static final int MAX_DEPTH = 16;
    private final List<NavigationRoute> stack;

    private NavigationState(List<NavigationRoute> routes) {
        if (routes == null || routes.isEmpty() || routes.size() > MAX_DEPTH
                || routes.get(0) != NavigationRoute.HOME) {
            throw new IllegalArgumentException("Historial de navegación inválido");
        }
        NavigationRoute previous = null;
        for (int index = 0; index < routes.size(); index++) {
            NavigationRoute route = Objects.requireNonNull(routes.get(index), "route");
            if (index > 0 && (route == NavigationRoute.HOME || route == previous)) {
                throw new IllegalArgumentException("Ruta repetida o inicio fuera de lugar");
            }
            previous = route;
        }
        stack = Collections.unmodifiableList(new ArrayList<>(routes));
    }

    public static NavigationState initial() {
        return new NavigationState(Collections.singletonList(NavigationRoute.HOME));
    }

    /** Rebuild only a strictly valid stack; callers should handle malformed input. */
    public static NavigationState fromIds(List<String> ids) {
        Objects.requireNonNull(ids, "ids");
        if (ids.isEmpty() || ids.size() > MAX_DEPTH) {
            throw new IllegalArgumentException("Número de rutas inválido");
        }
        List<NavigationRoute> routes = new ArrayList<>(ids.size());
        for (String id : ids) {
            routes.add(NavigationRoute.fromId(id));
        }
        return new NavigationState(routes);
    }

    public List<String> routeIds() {
        List<String> ids = new ArrayList<>(stack.size());
        for (NavigationRoute route : stack) {
            ids.add(route.id());
        }
        return Collections.unmodifiableList(ids);
    }

    public NavigationRoute current() {
        return stack.get(stack.size() - 1);
    }

    public List<NavigationRoute> history() {
        return stack;
    }

    public boolean canGoBack() {
        return stack.size() > 1;
    }

    public NavigationState navigateTo(NavigationRoute destination) {
        Objects.requireNonNull(destination, "destination");
        if (destination == NavigationRoute.HOME) {
            return initial();
        }
        if (current() == destination) {
            return this;
        }
        if (stack.size() == MAX_DEPTH) {
            throw new IllegalStateException("Límite de navegación alcanzado");
        }
        List<NavigationRoute> next = new ArrayList<>(stack);
        next.add(destination);
        return new NavigationState(next);
    }

    public NavigationState goBack() {
        if (!canGoBack()) {
            return this;
        }
        return new NavigationState(stack.subList(0, stack.size() - 1));
    }

    @Override
    public boolean equals(Object value) {
        return value instanceof NavigationState && stack.equals(((NavigationState) value).stack);
    }

    @Override
    public int hashCode() {
        return stack.hashCode();
    }

    @Override
    public String toString() {
        return "NavigationState" + stack;
    }
}
