package pe.asterionsystem.core.engine.progression;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;

/**
 * Pure per-category volume calculation, not a training mandate.
 * Recalculates a reference for the NEXT session from recorded completion and
 * effort. A calculated increase never forces the user to perform that work.
 * No load/intensity escalation, no coercion, and no transfer between variants.
 */
public final class VolumeProgressionEngine {
    public enum Status {
        NO_HISTORY, MAINTAIN, CALCULATED_INCREASE, CALCULATED_DECREASE,
        CALCULATED_EXTRA_SERIES, REVIEW_SERIES_WITH_GUIDANCE,
        RECOVERY_PRIORITY, PAUSED, OPTIONAL_ONE_REPETITION
    }

    public static final class Observation {
        public final LocalDate day;
        public final int category;
        public final String variantId, typeId;
        public final int technicalLevel, sets, repetitions, effort;
        public final boolean completed, interrupted;

        public Observation(LocalDate day, int category, String variantId, String typeId,
                           int technicalLevel, int sets, int repetitions, int effort,
                           boolean completed, boolean interrupted) {
            this.day = Objects.requireNonNull(day, "day");
            if (category < 0 || category > 4 || technicalLevel < 1 || technicalLevel > 3
                    || sets < 1 || sets > 12 || repetitions < 1 || repetitions > 60
                    || effort < 1 || effort > 10) {
                throw new IllegalArgumentException("Registro de volumen fuera de rango");
            }
            if (!validId(variantId) || !validId(typeId)) {
                throw new IllegalArgumentException("Identificador de catálogo inválido");
            }
            this.category = category;
            this.variantId = variantId;
            this.typeId = typeId;
            this.technicalLevel = technicalLevel;
            this.sets = sets;
            this.repetitions = repetitions;
            this.effort = effort;
            this.completed = completed;
            this.interrupted = interrupted;
        }
    }

    public static final class Suggestion {
        public final Status status;
        /** Previous actually logged volume; zero when no comparable record. */
        public final int previousSets, previousRepetitions;
        /** Automatically calculated next-session reference, not an obligation. */
        public final int offeredSets, offeredRepetitions;
        public final String explanation;

        private Suggestion(Status status, int previousSets, int previousRepetitions,
                           int offeredSets, int offeredRepetitions, String explanation) {
            this.status = status;
            this.previousSets = previousSets;
            this.previousRepetitions = previousRepetitions;
            this.offeredSets = offeredSets;
            this.offeredRepetitions = offeredRepetitions;
            this.explanation = explanation;
        }

        public boolean requiresConsent() {
            return status == Status.CALCULATED_INCREASE
                    || status == Status.CALCULATED_EXTRA_SERIES
                    || status == Status.OPTIONAL_ONE_REPETITION;
        }
    }

    private VolumeProgressionEngine() { }

    private static boolean validId(String id) {
        return id != null && id.matches("[a-z][a-z0-9_]{1,63}");
    }

    /**
     * Return the greatest effort on the most recent recorded calendar day in
     * THIS category, within seven days. Other categories cannot affect it.
     */
    public static int latestCategoryEffort(int category, LocalDate today,
                                           List<Observation> observations) {
        if (category == CategoryEffortPolicy.REST) return 0;
        if (category < 0 || category > 4) {
            throw new IllegalArgumentException("Categoría inválida");
        }
        Objects.requireNonNull(today, "today");
        Objects.requireNonNull(observations, "observations");
        LocalDate latest = null;
        int effort = 0;
        for (Observation entry : observations) {
            if (entry == null || entry.category != category
                    || !entry.day.isBefore(today)
                    || entry.day.isBefore(today.minusDays(7))) continue;
            if (latest == null || entry.day.isAfter(latest)) {
                latest = entry.day;
                effort = entry.effort;
            } else if (entry.day.equals(latest)) {
                effort = Math.max(effort, entry.effort);
            }
        }
        return effort;
    }

    /** Requested step mapping, kept pure and testable. Positive values are
     * informative references, not permission to require more exercise. */
    public static int repetitionDelta(int effort) {
        switch (effort) {
            case 1: return 5;
            case 2: return 3;
            case 3: return 1;
            case 4: case 5: case 6: case 7: return 0;
            case 8: return -1;
            case 9: return -2;
            case 10: return -3;
            default: throw new IllegalArgumentException("Esfuerzo fuera de 1..10");
        }
    }

    /**
     * Compare the ACTUALLY SELECTED catalog variant with completed history
     * from its own category. On the next eligible date, the user's mapping is
     * reflected automatically in the reference, while the person may always
     * skip, rest or choose an easier amount.
     *
     * Three distinct easy completed sessions can trigger a one-series option,
     * after seven calendar days at minimum and with a three-series ceiling.
     * Never propose increasing sets AND reps at the same time.
     */
    public static Suggestion review(ExerciseVariant selected, int category,
                                    LocalDate nextDay, List<Observation> history,
                                    boolean pausedOrInterrupted) {
        Objects.requireNonNull(selected, "selected");
        Objects.requireNonNull(nextDay, "nextDay");
        Objects.requireNonNull(history, "history");
        if (category < 0 || category > 4)
            throw new IllegalArgumentException("Categoría inválida");
        if (pausedOrInterrupted) {
            return new Suggestion(Status.PAUSED, 0, 0, 0, 0,
                    "Pausa o interrupción: no se generan aumentos ni objetivos obligatorios.");
        }

        ArrayList<Observation> matching = new ArrayList<>();
        boolean recentHighOrInterrupted = false;
        for (Observation entry : history) {
            if (entry == null || entry.category != category
                    || !entry.day.isBefore(nextDay)
                    || entry.day.isBefore(nextDay.minusDays(21))) continue;
            if (!entry.day.isBefore(nextDay.minusDays(7))
                    && (entry.effort >= 8 || entry.interrupted))
                recentHighOrInterrupted = true;
            if (entry.completed && !entry.interrupted
                    && entry.variantId.equals(selected.id())
                    && entry.typeId.equals(selected.typeId())
                    && entry.technicalLevel == selected.difficulty())
                matching.add(entry);
        }
        matching.sort(Comparator.comparing((Observation o) -> o.day).reversed());
        if (matching.isEmpty()) {
            return new Suggestion(recentHighOrInterrupted
                    ? Status.RECOVERY_PRIORITY : Status.NO_HISTORY,
                    0, 0, 0, 0,
                    recentHighOrInterrupted
                    ? "Esfuerzo reciente alto en esta categoría; se prioriza recuperación."
                    : "No hay volumen completado comparable para esta variante.");
        }

        Observation recent = matching.get(0);
        int delta = repetitionDelta(recent.effort);
        if (delta < 0) {
            return new Suggestion(Status.CALCULATED_DECREASE,
                    recent.sets, recent.repetitions,
                    recent.sets, Math.max(1, recent.repetitions + delta),
                    "La dificultad comunicada reduce automáticamente la referencia "
                    + "de la siguiente sesión. Descansar sigue disponible.");
        }
        if (recentHighOrInterrupted) {
            return new Suggestion(Status.RECOVERY_PRIORITY,
                    recent.sets, recent.repetitions,
                    recent.sets, recent.repetitions,
                    "Hubo una sesión reciente difícil: se evita aumentar el volumen.");
        }
        if (delta == 0) {
            return new Suggestion(Status.MAINTAIN,
                    recent.sets, recent.repetitions,
                    recent.sets, recent.repetitions,
                    "Esfuerzo 4–7: conservar la referencia sin cambios.");
        }

        ArrayList<LocalDate> easyDays = new ArrayList<>();
        boolean sameVolume = true;
        LocalDate oldest = recent.day;
        for (Observation obs : matching) {
            if (obs.effort > 3 || obs.sets != recent.sets
                    || obs.repetitions != recent.repetitions)
                sameVolume = false;
            if (obs.effort <= 3 && !easyDays.contains(obs.day))
                easyDays.add(obs.day);
            if (obs.day.isBefore(oldest)) oldest = obs.day;
        }
        boolean eligibleSeries = matching.size() >= 3
                && easyDays.size() >= 3
                && sameVolume
                && ChronoUnit.DAYS.between(oldest, recent.day) >= 7
                && recent.sets < 3;
        if (eligibleSeries) {
            return new Suggestion(Status.CALCULATED_EXTRA_SERIES,
                    recent.sets, recent.repetitions,
                    recent.sets + 1, recent.repetitions,
                    "Tres sesiones fáciles comparables: el motor calcula una serie "
                    + "adicional como opción revisable, sin imponerla.");
        }

        final int maxReferenceReps = 20;
        if (recent.repetitions >= maxReferenceReps) {
            return new Suggestion(Status.REVIEW_SERIES_WITH_GUIDANCE,
                    recent.sets, recent.repetitions,
                    recent.sets, recent.repetitions,
                    "Límite de referencia alcanzado: revisar técnica y variedad "
                    + "sin añadir volumen automáticamente.");
        }
        int reference = Math.min(maxReferenceReps,
                recent.repetitions + delta);
        return new Suggestion(Status.CALCULATED_INCREASE,
                recent.sets, recent.repetitions,
                recent.sets, reference,
                "Cambio calculado a partir del esfuerzo, limitado a una "
                + "referencia opcional; no es una obligación de entrenar.");
    }
}
