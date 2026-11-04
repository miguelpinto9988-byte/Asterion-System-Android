package pe.asterionsystem.core.engine;

import java.util.List;

/** Read-only rule source port, usable by a future data adapter without depending on SQLite. */
@FunctionalInterface
public interface RuleCatalog {
    List<DecisionRule> loadRules();
}
