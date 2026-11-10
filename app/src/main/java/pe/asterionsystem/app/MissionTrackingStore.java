package pe.asterionsystem.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import pe.asterionsystem.core.engine.progression.EffortReviewEngine;

/**
 * Locally retained voluntary activity check-offs and 1–10 effort ratings.
 * No quota of repetitions, exertion threshold, streak or automatic increase.
 * Kept separate from historical SQLite progress to avoid data loss.
 */
final class MissionTrackingStore {
    private static final String PREFS="asterion_mission_tracking_v1";
    private static final String FIRST="first_effort_date";
    private static final String DAY="day_done_";
    private static final String RATE="day_effort_";
    private static final String MONARCH="monarch_done_";

    private MissionTrackingStore() { }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS,Context.MODE_PRIVATE);
    }

    private static String id(String variantId) {
        if(variantId==null || !variantId.matches("[A-Za-z0-9_-]{1,96}")) {
            throw new IllegalArgumentException("Identificador de ejercicio inválido");
        }
        return variantId;
    }

    private static String key(String prefix,LocalDate date) {
        if(date==null) throw new IllegalArgumentException("Fecha requerida");
        return prefix+date.toString();
    }

    static Set<String> completedToday(Context context,LocalDate day) {
        return Collections.unmodifiableSet(new HashSet<>(
                prefs(context).getStringSet(key(DAY,day),Collections.emptySet())));
    }

    static void setDailyCompleted(Context context,LocalDate day,String variantId,
                                  boolean completed) {
        SharedPreferences sp=prefs(context);
        String key=key(DAY,day);
        Set<String> ids=new HashSet<>(sp.getStringSet(key,Collections.emptySet()));
        if(completed)ids.add(id(variantId)); else ids.remove(id(variantId));
        sp.edit().putStringSet(key,ids).commit();
    }

    static int rating(Context context,LocalDate date) {
        int result=prefs(context).getInt(key(RATE,date),0);
        return result>=1&&result<=10?result:0;
    }

    static void rate(Context context,LocalDate date,int effort) {
        if(effort<1||effort>10)throw new IllegalArgumentException("Escala 1 a 10");
        SharedPreferences sp=prefs(context);
        String first=sp.getString(FIRST,"");
        SharedPreferences.Editor edit=sp.edit().putInt(key(RATE,date),effort);
        if(first.isEmpty() || LocalDate.parse(first).isAfter(date)) {
            edit.putString(FIRST,date.toString());
        }
        edit.commit();
    }

    static LocalDate firstReport(Context context) {
        try {return LocalDate.parse(prefs(context).getString(FIRST,""));}
        catch(RuntimeException badDate){return null;}
    }

    static List<EffortReviewEngine.Observation> ratings(Context context,LocalDate today) {
        ArrayList<EffortReviewEngine.Observation> list=new ArrayList<>();
        for(int days=0;days<21;days++) {
            LocalDate date=today.minusDays(days);
            int value=rating(context,date);
            if(value!=0)list.add(new EffortReviewEngine.Observation(date,value));
        }
        return Collections.unmodifiableList(list);
    }

    static Set<String> monarchCompleted(Context context,LocalDate accepted) {
        return Collections.unmodifiableSet(new HashSet<>(
                prefs(context).getStringSet(key(MONARCH,accepted),Collections.emptySet())));
    }

    static void markMonarch(Context context,LocalDate accepted,String variantId) {
        String key=key(MONARCH,accepted);
        SharedPreferences sp=prefs(context);
        Set<String> completed=new HashSet<>(sp.getStringSet(key,Collections.emptySet()));
        completed.add(id(variantId));
        sp.edit().putStringSet(key,completed).commit();
    }

    static void clear(Context context) {
        prefs(context).edit().clear().commit();
    }
}
