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
    static final String REVIEW_REQUIRED="review_required";

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
        java.util.Objects.requireNonNull(today,"today");
        java.util.Objects.requireNonNull(planner,"planner");
        java.util.Objects.requireNonNull(profile,"profile");
        java.util.Objects.requireNonNull(catalog,"catalog");
        SharedPreferences sp=preferences(context);
        LocalDate preview=parse(sp.getString(START,""));
        LocalDate accepted=parse(sp.getString(ACCEPTED,""));
        String status=sp.getString(STATUS,PREVIEW);
        String profileSignature=signature(profile);
        boolean profileChanged=!profileSignature.equals(
                sp.getString(PROFILE_SIGNATURE,""));
        boolean expiredActive=ACTIVE.equals(status)
                && (accepted==null || !today.isBefore(accepted.plusDays(5)));
        boolean expiredReview=REVIEW_REQUIRED.equals(status)
                && (accepted==null || !today.isBefore(accepted.plusDays(5)));
        boolean invalidPreview=preview==null || today.isBefore(preview)
                || ChronoUnit.DAYS.between(preview,today)>=5;
        String raw=sp.getString(IDS,"");
        List<String> stored=readIds(raw);
        boolean invalidIds=raw!=null && !raw.isEmpty() && stored.isEmpty();
        if(!invalidIds && preview!=null && !stored.isEmpty()) {
            if(stored.size()==5) {
                try { planner.restoreMonarch(profile,preview,stored); }
                catch(IllegalArgumentException invalid) { invalidIds=true; }
            } else {
                // Less than five eligible candidates is a legitimate preview
                // but cannot be accepted. Any catalog deletion invalidates it.
                for(String id:stored) {
                    if(catalog.variant(id).isEmpty())invalidIds=true;
                }
            }
        }

        // Never silently replace an accepted five-day mission. Freeze its
        // execution if changed profile/equipment or catalog makes it unsafe.
        if(ACTIVE.equals(status) && !expiredActive
                && (profileChanged || invalidIds)) {
            if(!sp.edit().putString(STATUS,REVIEW_REQUIRED).commit()) {
                throw new IllegalStateException("No se pudo pausar Monarca");
            }
            status=REVIEW_REQUIRED;
        }

        if(expiredActive || expiredReview
                || (!ACTIVE.equals(status) && !REVIEW_REQUIRED.equals(status)
                    && (profileChanged || invalidPreview || invalidIds))) {
            // Previous cycle is only an input to prefer fresh eligible IDs,
            // never a second historical workout or automatically completed mark.
            List<String> previous=profileChanged || invalidIds
                    ?Collections.emptyList():stored;
            MissionPlanner.Proposal generated=planner.monarch(profile,today,previous);
            List<String> ids=new ArrayList<>();
            for(ExerciseVariant variant:generated.candidates())ids.add(variant.id());
            if(!sp.edit().putString(PROFILE_SIGNATURE,profileSignature)
                    .putString(START,today.toString())
                    .putString(IDS,String.join(",",ids))
                    .putString(STATUS,PREVIEW)
                    .remove(ACCEPTED).commit()) {
                throw new IllegalStateException("No se pudo guardar Monarca");
            }
            preview=today;
            accepted=null;
            status=PREVIEW;
            stored=ids;
        }

        if(REVIEW_REQUIRED.equals(status)) {
            return new Snapshot(preview,accepted,status,Collections.emptyList());
        }
        List<ExerciseVariant> selected=new ArrayList<>();
        for(String id:stored)catalog.variant(id).ifPresent(selected::add);
        return new Snapshot(preview,accepted,status,selected);
    }

    static boolean accept(Context context, LocalDate today, Snapshot reviewed,
                          MissionPlanner.Profile profile) {
        if(reviewed==null || !reviewed.canAccept()
                || reviewed.previewStart==null || profile==null
                || today.isBefore(reviewed.previewStart)
                || !today.isBefore(reviewed.previewStart.plusDays(5)))return false;
        SharedPreferences sp=preferences(context);
        if(!PREVIEW.equals(sp.getString(STATUS,PREVIEW)))return false;
        if(!signature(profile).equals(sp.getString(PROFILE_SIGNATURE,"")))return false;
        if(!reviewed.previewStart.toString().equals(sp.getString(START,"")))return false;
        List<String> reviewedIds=new ArrayList<>();
        for(ExerciseVariant exercise:reviewed.exercises)reviewedIds.add(exercise.id());
        String stored=sp.getString(IDS,"");
        if(!String.join(",",reviewedIds).equals(stored)
                || readIds(stored).size()!=5)return false;
        // The caller separately shows the exact five names for approval.
        return sp.edit().putString(STATUS,ACTIVE)
                .putString(ACCEPTED,today.toString()).commit();
    }

    /** Explicitly discard only this suspended preview, not exercise history. */
    static void restartAfterReview(Context context) {
        SharedPreferences sp=preferences(context);
        if(REVIEW_REQUIRED.equals(sp.getString(STATUS,""))) {
            sp.edit().clear().commit();
        }
    }

    private static List<String> readIds(String raw) {
        if(raw==null || raw.isEmpty())return Collections.emptyList();
        String[] values=raw.split(",",-1);
        if(values.length>5)return Collections.emptyList();
        java.util.LinkedHashSet<String> ids=new java.util.LinkedHashSet<>();
        for(String id:values) {
            if(!id.matches("[a-z][a-z0-9_]{1,63}") || !ids.add(id))
                return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(ids));
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
                +"|"+new java.util.TreeSet<>(profile.equipment)
                +"|"+profile.intensityPreference;
    }

    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS,Context.MODE_PRIVATE);
    }

    private static LocalDate parse(String raw) {
        try { return LocalDate.parse(raw); }
        catch (RuntimeException invalid) { return null; }
    }
}
