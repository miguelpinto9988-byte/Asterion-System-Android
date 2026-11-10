package pe.asterionsystem.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;
import pe.asterionsystem.core.engine.missions.MissionPlanner;

/**
 * Private local five-day opt-in state.
 * Preview is never treated as an activation. The user can refuse or abandon.
 * The five-day active period starts on the date the user accepts; preview
 * refreshes every five days when no active mission exists.
 */
final class MonarchSessionStore {
    private static final String PREFS="asterion_monarch_mission_v1";
    private static final String START="preview_start";
    private static final String IDS="preview_variant_ids";
    private static final String STATUS="state";
    private static final String ACCEPTED="accepted_date";
    private static final String PROFILE_SIGNATURE="profile_signature";

    static final String PREVIEW="preview";
    static final String ACTIVE="active";
    static final String DECLINED="declined";

    static final class Snapshot {
        final LocalDate previewStart;
        final LocalDate acceptedDate;
        final String status;
        final List<ExerciseVariant> exercises;

        Snapshot(LocalDate previewStart, LocalDate acceptedDate, String status,
                 List<ExerciseVariant> exercises) {
            this.previewStart=previewStart;
            this.acceptedDate=acceptedDate;
            this.status=status;
            this.exercises=Collections.unmodifiableList(new ArrayList<>(exercises));
        }

        LocalDate activeUntilExclusive() {
            return acceptedDate == null ? null : acceptedDate.plusDays(5);
        }
        boolean canAccept() { return PREVIEW.equals(status) && exercises.size()==5; }
    }

    private MonarchSessionStore() { }

    static Snapshot current(Context context, LocalDate today, MissionPlanner planner,
                            MissionPlanner.Profile profile, ExerciseCatalog catalog) {
        SharedPreferences sp=preferences(context);
        LocalDate preview=parse(sp.getString(START,""));
        LocalDate accepted=parse(sp.getString(ACCEPTED,""));
        String status=sp.getString(STATUS,PREVIEW);
        String signature=signature(profile);
        boolean profileChanged=!signature.equals(sp.getString(PROFILE_SIGNATURE,""));
        boolean expiredActive=ACTIVE.equals(status)
                && (accepted==null || !today.isBefore(accepted.plusDays(5)));
        boolean invalidPreview=preview==null || today.isBefore(preview)
                || ChronoUnit.DAYS.between(preview,today)>=5;
        if (expiredActive || profileChanged || (!ACTIVE.equals(status) && invalidPreview)) {
            MissionPlanner.Proposal generated=planner.monarch(profile,today);
            StringBuilder ids=new StringBuilder();
            for (ExerciseVariant variant:generated.candidates()) {
                if(ids.length()>0)ids.append(",");
                ids.append(variant.id());
            }
            sp.edit().putString(PROFILE_SIGNATURE,signature)
                    .putString(START,today.toString())
                    .putString(IDS,ids.toString())
                    .putString(STATUS,PREVIEW)
                    .remove(ACCEPTED).apply();
            preview=today;
            accepted=null;
            status=PREVIEW;
        }
        List<ExerciseVariant> selected=new ArrayList<>();
        for (String id:sp.getString(IDS,"").split(",")) {
            if(id.isEmpty())continue;
            catalog.variant(id).ifPresent(selected::add);
        }
        return new Snapshot(preview,accepted,status,selected);
    }

    static boolean accept(Context context, LocalDate today, Snapshot reviewed) {
        if(!reviewed.canAccept())return false;
        SharedPreferences sp=preferences(context);
        if(!PREVIEW.equals(sp.getString(STATUS,PREVIEW)))return false;
        if(!reviewed.previewStart.toString().equals(sp.getString(START,"")))return false;
        String stored=sp.getString(IDS,"");
        if(stored.isEmpty()||stored.split(",").length!=5)return false;
        // The caller shows a confirmation dialog that explicitly names
        // all five choices before invoking this method.
        return sp.edit().putString(STATUS,ACTIVE)
                .putString(ACCEPTED,today.toString()).commit();
    }

    static void decline(Context context) {
        SharedPreferences sp=preferences(context);
        if(PREVIEW.equals(sp.getString(STATUS,PREVIEW))) {
            sp.edit().putString(STATUS,DECLINED).apply();
        }
    }

    static void abandon(Context context) {
        preferences(context).edit().clear().apply();
    }

    private static String signature(MissionPlanner.Profile profile) {
        return profile.experience+"|"+profile.goal+"|"+profile.weekly
                +"|"+new java.util.TreeSet<>(profile.equipment);
    }

    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS,Context.MODE_PRIVATE);
    }

    private static LocalDate parse(String raw) {
        try { return LocalDate.parse(raw); }
        catch (RuntimeException invalid) { return null; }
    }
}
