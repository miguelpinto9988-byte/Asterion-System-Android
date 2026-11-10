package pe.asterionsystem.core.engine.progression;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Seven-day category-specific review based on completed, rated activities.
 * The engine may trigger automatic SAME-LEVEL exercise variety, not mandatory
 * extra reps/sets, intensity escalation, or removal of rest days.
 */
public final class WeeklyCategoryChallengePolicy {
    public enum Status {
        WAITING_FOR_CHECKPOINT, INSUFFICIENT_EVIDENCE,
        KEEP_STABLE, OFFER_EQUIVALENT_VARIETY, RECOVERY_PRIORITY
    }

    public static final class Observation {
        public final LocalDate date;
        public final int category;
        public final int effort;
        public final boolean activityCompleted;

        public Observation(LocalDate date, int category, int effort, boolean activityCompleted) {
            this.date=Objects.requireNonNull(date,"date");
            if(category<0 || category>4) throw new IllegalArgumentException("Categoría inválida");
            if(effort<1 || effort>10) throw new IllegalArgumentException("Escala 1..10");
            this.category=category;
            this.effort=effort;
            this.activityCompleted=activityCompleted;
        }
    }

    public static final class Review {
        public final Status status;
        public final int category;
        public final int completedRatings;
        public final int lowRatings;
        public final int highRatings;
        public final int completedDaysSinceFirstRating;
        public final String explanation;

        Review(Status status, int category, int completedRatings,
               int lowRatings, int highRatings, int elapsed, String explanation) {
            this.status=status;
            this.category=category;
            this.completedRatings=completedRatings;
            this.lowRatings=lowRatings;
            this.highRatings=highRatings;
            this.completedDaysSinceFirstRating=elapsed;
            this.explanation=explanation;
        }

        public boolean rotateEquivalentVariant() {
            return status==Status.OFFER_EQUIVALENT_VARIETY;
        }
    }

    private WeeklyCategoryChallengePolicy() { }

    /**
     * firstRecordedDay is the actual first RPE entry, not the creation date
     * of a profile. On day 8 or later, the preceding full seven calendar
     * days can be reviewed. No inference is made from unreported sessions.
     * A high-effort observation prevents novelty being prioritized.
     */
    public static Review evaluate(int category, LocalDate firstRecordedDay,
                                  LocalDate today, List<Observation> observations) {
        if(category<0 || category>4)throw new IllegalArgumentException("Categoría inválida");
        Objects.requireNonNull(today,"today");
        Objects.requireNonNull(observations,"observations");
        int elapsed=firstRecordedDay==null || today.isBefore(firstRecordedDay)
                ?0:(int)Math.min(Integer.MAX_VALUE,
                    ChronoUnit.DAYS.between(firstRecordedDay,today));
        if(elapsed<7) {
            return new Review(Status.WAITING_FOR_CHECKPOINT,category,0,0,0,elapsed,
                    "Primera revisión semanal pendiente; se conserva la sesión del perfil.");
        }
        LocalDate since=today.minusDays(7);
        int completed=0,low=0,high=0;
        ArrayList<LocalDate> counted=new ArrayList<>();
        for(Observation observation:observations) {
            if(observation==null || observation.category!=category ||
                    !observation.activityCompleted ||
                    observation.date.isBefore(since) ||
                    !observation.date.isBefore(today) ||
                    counted.contains(observation.date))continue;
            counted.add(observation.date);
            completed++;
            if(observation.effort<=3)low++;
            if(observation.effort>=7)high++;
        }
        if(high>0) {
            return new Review(Status.RECOVERY_PRIORITY,category,completed,low,high,elapsed,
                    "Hubo esfuerzo elevado en esta categoría: priorizar descanso o simplificación.");
        }
        if(completed<2) {
            return new Review(Status.INSUFFICIENT_EVIDENCE,category,completed,low,high,elapsed,
                    "Se necesitan varias sesiones completadas y valoradas para detectar una tendencia.");
        }
        if(low>=2 && low==completed) {
            return new Review(Status.OFFER_EQUIVALENT_VARIETY,category,completed,low,high,elapsed,
                    "Esfuerzo bajo repetido: el motor puede renovar el desafío con una variante "
                    +"equivalente y material compatible, sin añadir volumen de entrenamiento.");
        }
        return new Review(Status.KEEP_STABLE,category,completed,low,high,elapsed,
                "Valoraciones mixtas o moderadas: conservar el plan y los descansos actuales.");
    }
}
