package pe.asterionsystem.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;
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
    private static final String CATEGORY_RATE="category_effort_";
    private static final String MONARCH="monarch_done_";
    private static final String CATEGORY_PLAN="category_plan_current_";
    private static final String CATEGORY_PLAN_PREVIOUS="category_plan_previous_";

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

    private static String categoryKey(LocalDate day,int category) {
        if(category<0 || category>4)throw new IllegalArgumentException(
                "Solo cinco categorías de actividad pueden recibir valoraciones");
        return CATEGORY_RATE+day.toString()+"_"+category;
    }

    /**
     * Records both category-local feedback and the legacy per-day RPE, so
     * existing 7/14/21-day charts remain readable. Old day-only ratings are
     * deliberately NOT inferred as upper/lower/core category observations.
     */
    static void rateCategory(Context context,LocalDate day,int category,int effort) {
        if(effort<1||effort>10)throw new IllegalArgumentException("Escala 1 a 10");
        String key=categoryKey(day,category);
        SharedPreferences sp=prefs(context);
        if(!sp.edit().putInt(key,effort).commit()) {
            throw new IllegalStateException("No se pudo guardar la valoración de la categoría");
        }
        rate(context,day,effort);
    }

    static int ratingForCategory(Context context,LocalDate day,int category) {
        int value=prefs(context).getInt(categoryKey(day,category),0);
        return value>=1&&value<=10?value:0;
    }

    /** Returns zero if this category was never rated in the preceding 21 days. */
    static int latestCategoryRating(Context context,int category,LocalDate before) {
        if(before==null)throw new IllegalArgumentException("Fecha requerida");
        for(int days=1;days<=21;days++) {
            int rating=ratingForCategory(context,before.minusDays(days),category);
            if(rating!=0)return rating;
        }
        return 0;
    }

    /**
     * Returns previously displayed exercise IDs from the same category only.
     * Values from a different category can never leak into this decision.
     * A current-day proposal is not treated as a previous session.
     */
    static List<String> previousCategoryVariants(Context context,int category,
                                                  LocalDate today) {
        if(category<0||category>4)throw new IllegalArgumentException("Categoría inválida");
        SharedPreferences store=prefs(context);
        String current=store.getString(CATEGORY_PLAN+category,"");
        LocalDate currentDate=planDate(current);
        if(currentDate!=null && currentDate.isBefore(today)) {
            return decodePlan(current,today);
        }
        return decodePlan(store.getString(CATEGORY_PLAN_PREVIOUS+category,""),today);
    }

    /**
     * Saves what the machine actually proposed, for reproducible contextual
     * selections on subsequent occurrences of the same category. Does not
     * modify workout completion, saved exercise targets or historical SQLite.
     */
    static void rememberCategoryProposal(Context context,int category,LocalDate today,
                                         List<ExerciseVariant> proposed) {
        if(category<0||category>4)throw new IllegalArgumentException("Categoría inválida");
        if(today==null || proposed==null)throw new IllegalArgumentException("Datos requeridos");
        StringBuilder record=new StringBuilder(today.toString()).append('|');
        for(ExerciseVariant exercise:proposed) {
            if(record.charAt(record.length()-1)!='|')record.append(',');
            record.append(id(exercise.id()));
        }
        SharedPreferences sp=prefs(context);
        String key=CATEGORY_PLAN+category;
        String old=sp.getString(key,"");
        LocalDate oldDay=planDate(old);
        if(record.toString().equals(old))return;
        SharedPreferences.Editor edit=sp.edit();
        if(oldDay!=null && oldDay.isBefore(today)) {
            edit.putString(CATEGORY_PLAN_PREVIOUS+category,old);
        }
        edit.putString(key,record.toString()).apply();
    }

    private static LocalDate planDate(String record) {
        try {
            int separator=record.indexOf('|');
            return separator<0 ? null : LocalDate.parse(record.substring(0,separator));
        } catch(RuntimeException invalid) {return null;}
    }

    private static List<String> decodePlan(String record,LocalDate today) {
        LocalDate date=planDate(record);
        if(date==null || !date.isBefore(today)
                || date.isBefore(today.minusDays(21)))return Collections.emptyList();
        int separator=record.indexOf('|');
        String ids=record.substring(separator+1);
        if(ids.isEmpty())return Collections.emptyList();
        ArrayList<String> result=new ArrayList<>();
        for(String candidate:ids.split(",")) {
            if(candidate.matches("[A-Za-z0-9_-]{1,96}") && !result.contains(candidate)) {
                result.add(candidate);
            }
        }
        return Collections.unmodifiableList(result);
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
