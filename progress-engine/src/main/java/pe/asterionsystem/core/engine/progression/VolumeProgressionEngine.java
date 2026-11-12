package pe.asterionsystem.core.engine.progression;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;

/**
 * Pure, optional volume review. It NEVER edits a workout or creates mandatory
 * exercise prescriptions. Only completed, personally logged volume is evidence.
 * Numeric suggestions are small, require repeated observations, and need consent.
 */
public final class VolumeProgressionEngine {
    public enum Status {
        NO_HISTORY, MAINTAIN, OPTIONAL_ONE_REPETITION,
        REVIEW_SERIES_WITH_GUIDANCE, RECOVERY_PRIORITY, PAUSED
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
        /** Optional, NEVER automatically applied to a session. */
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
            return status == Status.OPTIONAL_ONE_REPETITION;
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

    /**
     * Check one ACTUALLY SELECTED catalog variant. Volume from a different
     * variant, even of the same movement type, is never transferred blindly.
     */
    public static Suggestion review(ExerciseVariant selected, int category,
                                    LocalDate nextDay, List<Observation> history,
                                    boolean pausedOrInterrupted) {
        Objects.requireNonNull(selected, "selected");
        Objects.requireNonNull(nextDay, "nextDay");
        Objects.requireNonNull(history, "history");
        if (category < 0 || category > 4) {
            throw new IllegalArgumentException("Categoría inválida");
        }
        if (pausedOrInterrupted) {
            return new Suggestion(Status.PAUSED, 0, 0, 0, 0,
                    "Categoría en pausa o con interrupción reciente: no hay progresión de volumen.");
        }
        ArrayList<Observation> matching = new ArrayList<>();
        boolean recoveryAlert = false;
        for (Observation entry : history) {
            if (entry == null || entry.category != category
                    || !entry.day.isBefore(nextDay)
                    || entry.day.isBefore(nextDay.minusDays(21))) continue;
            if (!entry.day.isBefore(nextDay.minusDays(7))
                    && (entry.effort >= 7 || entry.interrupted)) recoveryAlert = true;
            if (entry.variantId.equals(selected.id())
                    && entry.typeId.equals(selected.typeId())
                    && entry.technicalLevel == selected.difficulty()) {
                matching.add(entry);
            }
        }
        matching.sort(Comparator.comparing((Observation o) -> o.day).reversed());
        if (matching.isEmpty()) {
            return new Suggestion(recoveryAlert ? Status.RECOVERY_PRIORITY : Status.NO_HISTORY,
                    0, 0, 0, 0,
                    recoveryAlert ? "Esfuerzo reciente alto en esta categoría: priorizar recuperación."
                            : "Variante sin volumen comparable registrado: no se inventa un objetivo.");
        }
        Observation recent = matching.get(0);
        if (recoveryAlert || recent.effort >= 7 || recent.interrupted) {
            return new Suggestion(Status.RECOVERY_PRIORITY, recent.sets,
                    recent.repetitions, recent.sets, recent.repetitions,
                    "Esfuerzo alto o interrupción reciente: no se propone aumentar el volumen.");
        }
        if (matching.size() >= 2) {
            Observation prior = matching.get(1);
            boolean repeatedAndSeparated = recent.completed && prior.completed
                    && !recent.interrupted && !prior.interrupted
                    && recent.effort <= 3 && prior.effort <= 3
                    && recent.sets == prior.sets
                    && recent.repetitions == prior.repetitions
                    && ChronoUnit.DAYS.between(prior.day, recent.day) >= 2
                    && ChronoUnit.DAYS.between(prior.day, nextDay) >= 7;
            if (repeatedAndSeparated && recent.sets <= 3 && recent.repetitions < 20) {
                return new Suggestion(Status.OPTIONAL_ONE_REPETITION,
                        recent.sets, recent.repetitions,
                        recent.sets, recent.repetitions + 1,
                        "Dos sesiones completadas y fáciles, separadas en el tiempo: "
                        + "se puede revisar una repetición adicional por serie. "
                        + "Solo si se confirma voluntariamente y se mantiene buena técnica.");
            }
            if (repeatedAndSeparated && recent.repetitions >= 20) {
                return new Suggestion(Status.REVIEW_SERIES_WITH_GUIDANCE,
                        recent.sets, recent.repetitions,
                        recent.sets, recent.repetitions,
                        "No se añaden más repeticiones ni series automáticamente. "
                        + "Revisar alternativas y técnica con orientación adecuada.");
            }
        }
        return new Suggestion(Status.MAINTAIN,
                recent.sets, recent.repetitions,
                recent.sets, recent.repetitions,
                "Mantener el volumen registrado. Se requieren sesiones comparables "
                + "y recuperación suficiente antes de ofrecer cambios opcionales.");
    }
}
