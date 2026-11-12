package pe.asterionsystem.core.engine.progression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;

/**
 * Pure checkpoint transition contract for the exercise selector and volume engine.
 * A variant changed at the category's 7/14/21 checkpoint must start a fresh
 * volume epoch, even if that variant was completed much earlier in history.
 * This is not a command to perform any amount of exercise.
 */
public final class VolumeResetPolicy {
    public static final class Decision {
        public final int checkpoint;
        public final Set<String> restartVariantIds;

        private Decision(int checkpoint, Set<String> restartVariantIds) {
            this.checkpoint = checkpoint;
            this.restartVariantIds = Collections.unmodifiableSet(restartVariantIds);
        }

        public boolean restarted(String variantId) {
            return restartVariantIds.contains(variantId);
        }
    }

    private VolumeResetPolicy() { }

    public static String selectionKey(ExerciseVariant variant) {
        Objects.requireNonNull(variant, "variant");
        return variant.id() + "@" + variant.difficulty();
    }

    /**
     * previousSelection is the last selection shown in THIS category.
     * previousCheckpoint is the last checkpoint already observed (0/7/14/21).
     * New session selections between milestones are recorded without a reset.
     */
    public static Decision evaluate(int previousCheckpoint, int currentCheckpoint,
                                    List<String> previousSelection,
                                    List<ExerciseVariant> currentSelection) {
        if (!validCheckpoint(previousCheckpoint) || !validCheckpoint(currentCheckpoint))
            throw new IllegalArgumentException("Hito de revisión desconocido");
        Objects.requireNonNull(previousSelection, "previousSelection");
        Objects.requireNonNull(currentSelection, "currentSelection");
        LinkedHashSet<String> restarted = new LinkedHashSet<>();
        if (currentCheckpoint > previousCheckpoint && currentCheckpoint > 0) {
            Set<String> prior = new HashSet<>(previousSelection);
            for (ExerciseVariant selected : currentSelection) {
                if (selected == null) throw new IllegalArgumentException("Variante nula");
                if (!prior.contains(selectionKey(selected))) {
                    restarted.add(selected.id());
                }
            }
        }
        return new Decision(currentCheckpoint, restarted);
    }

    private static boolean validCheckpoint(int checkpoint) {
        return checkpoint == 0 || checkpoint == 7
                || checkpoint == 14 || checkpoint == 21;
    }

    public static List<String> snapshot(List<ExerciseVariant> selected) {
        Objects.requireNonNull(selected, "selected");
        ArrayList<String> keys = new ArrayList<>();
        for (ExerciseVariant variant : selected) keys.add(selectionKey(variant));
        return Collections.unmodifiableList(keys);
    }
}
