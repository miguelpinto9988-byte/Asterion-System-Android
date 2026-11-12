package pe.asterionsystem.core.engine.progression;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * Explains voluntary perceived effort reports at days 7, 14 and 21 of each
 * 21-day observation cycle. Ratings do NOT authorize exercise escalation.
 * No medical inference or automatic adjustment of repetitions/duration.
 */
public final class EffortReviewEngine {
    private EffortReviewEngine() { }

    public static final class Observation {
        public final LocalDate date;
        public final int perceivedEffort;

        public Observation(LocalDate date, int perceivedEffort) {
            this.date = Objects.requireNonNull(date, "date");
            if (perceivedEffort < 1 || perceivedEffort > 10) {
                throw new IllegalArgumentException("La valoración debe estar entre 1 y 10");
            }
            this.perceivedEffort = perceivedEffort;
        }
    }

    public static final class Review {
        public final int dayInCycle;
        public final int checkpoint;
        public final int nextCheckpoint;
        public final int observationCount;
        public final double averageEffort;
        public final String recommendation;

        Review(int dayInCycle, int checkpoint, int nextCheckpoint,
               int observationCount, double averageEffort, String recommendation) {
            this.dayInCycle=dayInCycle;
            this.checkpoint=checkpoint;
            this.nextCheckpoint=nextCheckpoint;
            this.observationCount=observationCount;
            this.averageEffort=averageEffort;
            this.recommendation=recommendation;
        }
    }

    public static Review review(LocalDate firstRecordedDay, LocalDate today,
                                List<Observation> reports) {
        Objects.requireNonNull(today, "today");
        Objects.requireNonNull(reports, "reports");
        if (firstRecordedDay == null || today.isBefore(firstRecordedDay)) {
            return new Review(0,0,7,0,Double.NaN,
                    "Sin datos suficientes: registra una valoración voluntaria cuando termines.");
        }
        long elapsed=ChronoUnit.DAYS.between(firstRecordedDay,today);
        int day=(int)(elapsed % 21L)+1;
        int checkpoint=day >=21 ?21 : day>=14 ?14 : day>=7 ?7 :0;
        int next=day<7?7:day<14?14:21;
        LocalDate cycleStart=today.minusDays(day-1);
        LocalDate windowStart=today.minusDays(6);
        if (windowStart.isBefore(cycleStart))windowStart=cycleStart;
        int sum=0;
        int count=0;
        for (Observation report: reports) {
            if (report!=null && !report.date.isBefore(windowStart)
                    && !report.date.isAfter(today)) {
                sum+=report.perceivedEffort;
                count++;
            }
        }
        if (count==0) {
            return new Review(day,checkpoint,next,0,Double.NaN,
                    "Sin valoraciones recientes: no se puede determinar una tendencia.");
        }
        double average=(double)sum/count;
        String advice;
        if (average>=8.0) {
            advice="Esfuerzo percibido elevado. Considera descanso o una alternativa más suave; "
                    +"no se aumentará la exigencia automáticamente.";
        } else if (average>=4.0) {
            advice="Esfuerzo moderado. Mantén opciones flexibles y revisa cómo te sientes; "
                    +"no se aumentará la exigencia automáticamente.";
        } else {
            advice="Esfuerzo declarado bajo. Puedes revisar variedad y preferencias, "
                    +"sin aumentar automáticamente repeticiones o intensidad.";
        }
        return new Review(day,checkpoint,next,count,average,advice);
    }
}
