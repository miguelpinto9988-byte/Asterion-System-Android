package pe.asterionsystem.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseCategoryMapper;
import pe.asterionsystem.core.engine.exercises.ExerciseFamily;
import pe.asterionsystem.core.engine.exercises.ExerciseType;
import java.util.List;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;
import pe.asterionsystem.core.engine.exercises.ExerciseMuscleFocus;
import java.util.Set;
import pe.asterionsystem.core.engine.progression.EffortReviewEngine;
import pe.asterionsystem.core.engine.progression.WeeklyCategoryChallengePolicy;

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
     * Seven-day completed-session observations, tagged with their original
     * category. A rating without a logged activity is not evidence that a
     * workout was completed.
     */
    static List<WeeklyCategoryChallengePolicy.Observation> recentCategoryObservations(
            Context context,ExerciseCatalog catalog,int category,LocalDate today) {
        if(catalog==null || today==null || category<0 || category>4)
            throw new IllegalArgumentException("Datos de categoría requeridos");
        Map<String,Integer> exerciseCategory=variantCategories(catalog);
        ArrayList<WeeklyCategoryChallengePolicy.Observation> observations=new ArrayList<>();
        for(int days=1;days<=7;days++) {
            LocalDate prior=today.minusDays(days);
            int effort=ratingForCategory(context,prior,category);
            if(effort==0)continue;
            boolean completed=false;
            for(String id:completedToday(context,prior)) {
                if(exerciseCategory.getOrDefault(id,-1)==category) {
                    completed=true;
                    break;
                }
            }
            observations.add(new WeeklyCategoryChallengePolicy.Observation(
                    prior,category,effort,completed));
        }
        return Collections.unmodifiableList(observations);
    }

    /**
     * Uses actual past check-offs, not stored/generated daily proposals.
     * Finds the latest completed session for THIS catalog category only.
     * Today is intentionally excluded so checking off an exercise cannot
     * unexpectedly change the remaining mission when switching panels.
     */
    static List<String> latestCompletedForCategory(Context context,ExerciseCatalog catalog,
                                                    int category,LocalDate today) {
        if(catalog==null || today==null || category<0 || category>4)
            throw new IllegalArgumentException("Parámetros de historial inválidos");
        Map<String,Integer> byVariant=variantCategories(catalog);
        for(int days=1;days<=21;days++) {
            LocalDate prior=today.minusDays(days);
            ArrayList<String> hits=new ArrayList<>();
            for(String id:completedToday(context,prior)) {
                if(byVariant.getOrDefault(id,-1)==category)hits.add(id);
            }
            if(!hits.isEmpty()) {
                Collections.sort(hits);
                return Collections.unmodifiableList(hits);
            }
        }
        return Collections.emptyList();
    }

    /**
     * The last 14 days of ACTUALLY completed variant IDs in this category.
     * Current-day check-offs are ignored to avoid reshuffling a pinned day.
     * Derived from existing private records; no second history is persisted.
     */
    static List<String> recentCompletedForCategory(Context context,
            ExerciseCatalog catalog,int category,LocalDate today) {
        if(catalog==null || today==null || category<0 || category>4)
            throw new IllegalArgumentException("Categoría/fecha requerida");
        Map<String,Integer> categoryForVariant=variantCategories(catalog);
        LinkedHashSet<String> ids=new LinkedHashSet<>();
        for(int day=1;day<=14;day++) {
            ArrayList<String> sorted=new ArrayList<>(completedToday(
                    context,today.minusDays(day)));
            Collections.sort(sorted);
            for(String id:sorted) {
                if(categoryForVariant.getOrDefault(id,-1)==category)ids.add(id);
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(ids));
    }

    /** Read-only summary; no new profile keys, DB schema or proposal snapshots. */
    static final class DiversitySummary {
        final int activeDays;
        final int completedMarks;
        final int uniqueVariants;
        final int focusGroups;

        DiversitySummary(int activeDays,int completedMarks,int uniqueVariants,
                         int focusGroups) {
            this.activeDays=activeDays;
            this.completedMarks=completedMarks;
            this.uniqueVariants=uniqueVariants;
            this.focusGroups=focusGroups;
        }
    }

    static DiversitySummary recentDiversityForCategory(Context context,
            ExerciseCatalog catalog,int category,LocalDate today) {
        if(catalog==null || today==null || category<0 || category>4)
            throw new IllegalArgumentException("Categoría/fecha requerida");
        Map<String,Integer> byVariant=variantCategories(catalog);
        Set<String> distinctIds=new HashSet<>();
        Set<String> focusIds=new HashSet<>();
        int activeDays=0;
        int total=0;
        // Include today's check-offs in the displayed summary, but they never
        // affect the already pinned exercise proposal.
        for(int days=0;days<14;days++) {
            boolean dayHasCompletions=false;
            for(String id:completedToday(context,today.minusDays(days))) {
                if(byVariant.getOrDefault(id,-1)!=category)continue;
                dayHasCompletions=true;
                total++;
                distinctIds.add(id);
                catalog.variant(id).ifPresent(variant->focusIds.add(
                        ExerciseMuscleFocus.forVariant(catalog,variant).key()));
            }
            if(dayHasCompletions)activeDays++;
        }
        return new DiversitySummary(activeDays,total,distinctIds.size(),
                focusIds.size());
    }

    private static Map<String,Integer> variantCategories(ExerciseCatalog catalog) {
        Map<String,Integer> familyCategories=new HashMap<>();
        for(ExerciseFamily family:catalog.families()) {
            familyCategories.put(family.id(),
                    ExerciseCategoryMapper.weeklyIndex(family.category()));
        }
        Map<String,Integer> typeCategories=new HashMap<>();
        for(ExerciseType type:catalog.types()) {
            typeCategories.put(type.id(),
                    familyCategories.getOrDefault(type.familyId(),-1));
        }
        Map<String,Integer> byVariant=new HashMap<>();
        for(ExerciseVariant variant:catalog.variants()) {
            byVariant.put(variant.id(),
                    typeCategories.getOrDefault(variant.typeId(),-1));
        }
        return byVariant;
    }

    /** Removes obsolete cached full daily proposals, preserves effort/completion. */
    static void clearLegacyProposalSnapshots(Context context) {
        SharedPreferences shared=prefs(context);
        if(shared.getBoolean("legacy_proposal_purged_v2",false))return;
        SharedPreferences.Editor edit=shared.edit();
        for(int category=0;category<5;category++) {
            edit.remove(CATEGORY_PLAN+category);
            edit.remove(CATEGORY_PLAN_PREVIOUS+category);
        }
        edit.putBoolean("legacy_proposal_purged_v2",true).apply();
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
