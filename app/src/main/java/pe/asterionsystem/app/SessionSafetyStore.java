package pe.asterionsystem.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.time.LocalDate;
import pe.asterionsystem.core.engine.missions.SessionInterruptionPolicy;

/**
 * Local interruption state. Serious red flags hold all exercise proposals
 * until the user explicitly confirms professional evaluation; there is no
 * automatic one-day expiration. Category reports never spill across categories.
 */
final class SessionSafetyStore {
    private static final String FILE="asterion_session_safety_v1";
    private static final String GLOBAL="global_medical_pause";
    private static final String GLOBAL_REASON="global_reason";
    private static final String NEXT_PREFIX="lighter_after_";
    private static final String APPLIED_PREFIX="lighter_applied_";
    private static final String PAUSE_PREFIX="category_paused_";

    private SessionSafetyStore() {}

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(FILE,Context.MODE_PRIVATE);
    }

    static boolean globallyPaused(Context c) {
        return prefs(c).getBoolean(GLOBAL,false);
    }
    static String globalReason(Context c) {
        return prefs(c).getString(GLOBAL_REASON,"Síntomas graves reportados durante una sesión.");
    }
    static boolean categoryPaused(Context c,int category) {
        checkCategory(category);
        return prefs(c).getBoolean(PAUSE_PREFIX+category,false);
    }

    static void report(Context c,int category,LocalDate date,
                       SessionInterruptionPolicy.Assessment result) {
        if(date==null||result==null)throw new IllegalArgumentException("Datos requeridos");
        if(category<0 || category>4) {
            if(category!=-1 ||
                    (result.action!=SessionInterruptionPolicy.Action.GLOBAL_PAUSE_AND_SEEK_MEDICAL_CARE
                    && result.action!=SessionInterruptionPolicy.Action.STOP_ONLY)) {
                throw new IllegalArgumentException("Categoría inválida");
            }
        }
        SharedPreferences.Editor edit=prefs(c).edit();
        switch(result.action) {
            case GLOBAL_PAUSE_AND_SEEK_MEDICAL_CARE:
                edit.putBoolean(GLOBAL,true).putString(GLOBAL_REASON,result.explanation);
                if(category>=0) {
                    edit.putString(NEXT_PREFIX+category,date.toString())
                            .remove(APPLIED_PREFIX+category);
                }
                break;
            case CATEGORY_PAUSE_UNTIL_REVIEW:
                edit.putBoolean(PAUSE_PREFIX+category,true)
                        .putString(NEXT_PREFIX+category,date.toString())
                        .remove(APPLIED_PREFIX+category);
                break;
            case CATEGORY_LIGHTER_NEXT_SESSION:
                edit.putString(NEXT_PREFIX+category,date.toString())
                        .remove(APPLIED_PREFIX+category);
                break;
            case STOP_ONLY:
                break;
        }
        if(!edit.commit())throw new IllegalStateException("No se pudo guardar la pausa");
    }

    /** At most one later session of the same category is reduced. */
    static boolean lighterToday(Context c,int category,LocalDate today) {
        checkCategory(category);
        if(today==null || globallyPaused(c) || categoryPaused(c,category))return false;
        SharedPreferences sp=prefs(c);
        LocalDate reported=parse(sp.getString(NEXT_PREFIX+category,""));
        LocalDate applied=parse(sp.getString(APPLIED_PREFIX+category,""));
        if(reported==null || !reported.isBefore(today))return false;
        if(applied!=null) {
            if(applied.equals(today))return true;
            if(applied.isBefore(today)) {
                sp.edit().remove(NEXT_PREFIX+category)
                        .remove(APPLIED_PREFIX+category).apply();
            }
            return false;
        }
        return sp.edit().putString(APPLIED_PREFIX+category,today.toString()).commit();
    }

    /**
     * For minor complaints the user may mark the symptom resolved. This
     * does not override the separate serious medical-evaluation hold.
     */
    static void clearMinorCategoryPause(Context c,int category) {
        checkCategory(category);
        prefs(c).edit().remove(PAUSE_PREFIX+category).apply();
    }

    /**
     * Does not claim to verify medical clearance. UI must explicitly request
     * acknowledgement of an actual clinical evaluation first.
     */
    static void acknowledgeProfessionalEvaluation(Context c) {
        prefs(c).edit().remove(GLOBAL).remove(GLOBAL_REASON).apply();
    }

    private static void checkCategory(int category) {
        if(category<0||category>4)throw new IllegalArgumentException("Categoría inválida");
    }
    private static LocalDate parse(String raw) {
        try{return LocalDate.parse(raw);}
        catch(RuntimeException bad){return null;}
    }
}
