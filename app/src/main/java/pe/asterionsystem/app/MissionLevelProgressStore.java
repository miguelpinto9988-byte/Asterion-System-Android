package pe.asterionsystem.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import pe.asterionsystem.core.engine.missions.MissionDifficultyMixPolicy;
import pe.asterionsystem.core.engine.progression.WeeklyCategoryChallengePolicy;

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

        Snapshot(String experience,LocalDate entered,int phase,boolean promoted) {
            this.experience=experience;
            this.entered=entered;
            this.phase=phase;
            this.promoted=promoted;
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
        if(category==5 || !MissionDifficultyMixPolicy.mixed(selected))
            return new Snapshot(selected,entered,0,false);

        int phase=MissionDifficultyMixPolicy.clampPhase(
                sp.getInt(PHASE+category,0));
        LocalDate last=parse(sp.getString(SHIFT+category,""));
        // Never promote from calendar time or another category's report.
        boolean eligible=!recentInterruption
                && phase<MissionDifficultyMixPolicy.PHASES-1
                && !today.isBefore(entered.plusDays(7))
                && (last==null || !today.isBefore(last.plusDays(7)))
                && weekly!=null
                && weekly.category==category
                && weekly.status==WeeklyCategoryChallengePolicy.Status.OFFER_EQUIVALENT_VARIETY
                && weekly.completedRatings>=2
                && weekly.lowRatings>=2
                && weekly.highRatings==0;
        if(eligible) {
            if(!sp.edit().putInt(PHASE+category,phase+1)
                    .putString(SHIFT+category,today.toString()).commit()) {
                throw new IllegalStateException("No se pudo guardar la progresión");
            }
            phase++;
        }
        return new Snapshot(selected,entered,phase,eligible);
    }

    private static LocalDate parse(String value) {
        try { return LocalDate.parse(value); }
        catch(RuntimeException invalid) { return null; }
    }
}
