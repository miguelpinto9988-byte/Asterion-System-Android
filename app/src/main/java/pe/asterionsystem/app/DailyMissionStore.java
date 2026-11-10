package pe.asterionsystem.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;
import pe.asterionsystem.core.engine.missions.MissionPlanner;

/**
 * ONE-DAY lightweight pin, NOT a saved workout plan or historical mission log.
 *
 * Stores just local date, mission-relevant profile fingerprint, catalog version
 * fingerprint and up to seven variant IDs. Descriptions, exercises, categories,
 * intensity, repetitions and progression history are never copied here.
 * Completion and perceived effort remain in MissionTrackingStore separately.
 */
final class DailyMissionStore {
    private static final String FILE="asterion_daily_mission_pin_v1";
    private static final String DATE="local_date";
    private static final String SIGNATURE="profile_signature";
    private static final String CATALOG="catalog_signature";
    private static final String IDS="variant_ids";

    private DailyMissionStore() {}

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(FILE,Context.MODE_PRIVATE);
    }

    /**
     * Invalid/missing/expired pins are ignored; the motor will generate anew.
     * No former date survives a successful new-day write.
     */
    static List<String> read(Context context, LocalDate today,
                             MissionPlanner.Profile profile, ExerciseCatalog catalog) {
        Objects.requireNonNull(today,"today");
        Objects.requireNonNull(profile,"profile");
        Objects.requireNonNull(catalog,"catalog");
        SharedPreferences p=prefs(context);
        if(!today.toString().equals(p.getString(DATE,""))
                || !profile.missionSignature().equals(p.getString(SIGNATURE,""))
                || !catalogSignature(catalog).equals(p.getString(CATALOG,""))) {
            // Drop yesterday's (or the former profile's) tiny pin on first
            // read; it is not retained as a historical generated mission.
            p.edit().clear().apply();
            return Collections.emptyList();
        }
        String raw=p.getString(IDS,"");
        if(raw==null || raw.isEmpty())return Collections.emptyList();
        String[] pieces=raw.split(",",-1);
        if(pieces.length>profile.dailyLimit || pieces.length>7)return Collections.emptyList();
        ArrayList<String> result=new ArrayList<>();
        for(String id:pieces) {
            if(!id.matches("[a-z][a-z0-9_]{1,63}") || result.contains(id))
                return Collections.emptyList();
            result.add(id);
        }
        return Collections.unmodifiableList(result);
    }

    static void write(Context context,LocalDate today,MissionPlanner.Profile profile,
                      ExerciseCatalog catalog,List<ExerciseVariant> choices) {
        Objects.requireNonNull(today,"today");
        Objects.requireNonNull(profile,"profile");
        Objects.requireNonNull(catalog,"catalog");
        Objects.requireNonNull(choices,"choices");
        if(choices.isEmpty() || choices.size()>profile.dailyLimit) {
            throw new IllegalArgumentException("Propuesta diaria inválida");
        }
        StringBuilder ids=new StringBuilder();
        for(ExerciseVariant exercise:choices) {
            if(ids.length()>0)ids.append(',');
            ids.append(exercise.id());
        }
        boolean success=prefs(context).edit()
                .putString(DATE,today.toString())
                .putString(SIGNATURE,profile.missionSignature())
                .putString(CATALOG,catalogSignature(catalog))
                .putString(IDS,ids.toString())
                .commit();
        if(!success)throw new IllegalStateException("No se pudo estabilizar la misión diaria");
    }

    static void clear(Context context) { prefs(context).edit().clear().apply(); }

    private static String catalogSignature(ExerciseCatalog catalog) {
        // Reproducible across process restarts. Detects APK catalog changes
        // without persisting variant descriptions or any sensitive profile data.
        long hash=0xcbf29ce484222325L;
        for(ExerciseVariant item:catalog.variants()) {
            String text=item.id()+"|"+item.difficulty()+"|"
                    +catalog.intensity(item.id()).code()+";";
            for(int i=0;i<text.length();i++) {
                hash^=text.charAt(i);
                hash*=0x100000001b3L;
            }
        }
        return Integer.toString(catalog.variants().size())+"-"+Long.toHexString(hash);
    }
}
