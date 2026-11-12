package pe.asterionsystem.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import pe.asterionsystem.core.engine.missions.MissionDifficultyMixPolicy;
import pe.asterionsystem.core.engine.progression.WeeklyCategoryChallengePolicy;
import pe.asterionsystem.core.engine.progression.TechnicalCheckpointPolicy;

/**
 * Private, minimal, category-isolated phase state. This does not modify
 * workout progress, profiles, completion marks or SQLite records.
 * A new selected experience starts at phase 0. Time alone NEVER promotes;
 * there must be at least two recently completed, low-effort rated sessions
 * in the very same category and a minimum seven-day gap between changes.
 */
final class MissionLevelProgressStore {
    private static final String FILE="asterion_mission_level_progress_v1";
    private static final String EXPERIENCE="selected_experience";
    private static final String ENTRY_DATE="experience_first_seen";
    private static final String PHASE="phase_category_";
    private static final String SHIFT="last_phase_change_";

    private MissionLevelProgressStore() { }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(
                FILE,Context.MODE_PRIVATE);
    }

    static final class Snapshot {
        final String experience;
        final LocalDate entered;
        final int phase;
        final boolean promoted;
        final TechnicalCheckpointPolicy.Decision technicalReview;

        Snapshot(String experience,LocalDate entered,int phase,boolean promoted,
                 TechnicalCheckpointPolicy.Decision technicalReview) {
            this.experience=experience;
            this.entered=entered;
            this.phase=phase;
            this.promoted=promoted;
            this.technicalReview=technicalReview;
        }

        long daysSinceEntry(LocalDate today) {
            return Math.max(0,ChronoUnit.DAYS.between(entered,today));
        }
    }

    static Snapshot observe(Context context,String experience,int category,
            LocalDate today,WeeklyCategoryChallengePolicy.Review weekly,
            boolean recentInterruption) {
        Objects.requireNonNull(context,"context");
        Objects.requireNonNull(today,"today");
        if(category<0 || category>5)
            throw new IllegalArgumentException("Categoría inválida");
        String selected="intermedio".equals(experience) || "avanzado".equals(experience)
                ?experience:"principiante";
        SharedPreferences sp=prefs(context);
        LocalDate entered=parse(sp.getString(ENTRY_DATE,""));
        String previous=sp.getString(EXPERIENCE,"");
        if(!selected.equals(previous) || entered==null || today.isBefore(entered)) {
            // Only these dedicated progression preferences are replaced,
            // never saved mission completion, profile or workout data.
            if(!sp.edit().clear()
                    .putString(EXPERIENCE,selected)
                    .putString(ENTRY_DATE,today.toString()).commit()) {
                throw new IllegalStateException("No se pudo guardar el nivel");
            }
            entered=today;
        }
        if(category==5) {
            TechnicalCheckpointPolicy.Decision review=
                    TechnicalCheckpointPolicy.evaluate(
                            category,0,0,selected,null,recentInterruption);
            return new Snapshot(selected,entered,0,false,review);
        }
        int phase=MissionDifficultyMixPolicy.clampPhase(
                sp.getInt(PHASE+category,0));
        TechnicalCheckpointPolicy.Decision review=
                TechnicalCheckpointPolicy.evaluate(
                        category,Math.max(0,ChronoUnit.DAYS.between(entered,today)),
                        phase,selected,weekly,recentInterruption);
        if(!MissionDifficultyMixPolicy.mixed(selected))
            return new Snapshot(selected,entered,0,false,review);

        LocalDate last=parse(sp.getString(SHIFT+category,""));
        // No calendar-only promotion; use completed evidence for this category.
        boolean eligible=review.mayAdvance()
                && (last==null || !today.isBefore(last.plusDays(7)));
        if(eligible) {
            if(!sp.edit().putInt(PHASE+category,phase+1)
                    .putString(SHIFT+category,today.toString()).commit()) {
                throw new IllegalStateException("No se pudo guardar la progresión");
            }
            phase++;
        }
        return new Snapshot(selected,entered,phase,eligible,review);
    }

    private static LocalDate parse(String value) {
        try { return LocalDate.parse(value); }
        catch(RuntimeException invalid) { return null; }
    }
}
