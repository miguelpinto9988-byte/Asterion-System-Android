package pe.asterionsystem.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.time.LocalDate;

/**
 * Optional participation XP for self-reported completed custom routines.
 * Fixed small amount; once per local day. No rewards for more sets, higher
 * technical levels, longer durations, skipped steps or repeat sessions.
 * Never modifies legacy progress tracking or the SQLite schema.
 */
final class RoutineExperienceStore {
    private static final String FILE="asterion_routine_experience_v1";
    private static final int ONCE_PER_DAY=10;
    private RoutineExperienceStore() {}

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(FILE,Context.MODE_PRIVATE);
    }

    static int total(Context c) { return Math.max(0,prefs(c).getInt("total",0)); }

    static boolean recorded(Context c,LocalDate date) {
        return prefs(c).getBoolean("completed_"+date,false);
    }

    static int awardOnce(Context c,LocalDate date) {
        if(date==null || !date.equals(LocalDate.now()))return 0;
        SharedPreferences p=prefs(c);
        String key="completed_"+date;
        if(p.getBoolean(key,false))return 0;
        int points=Math.min(1_000_000,total(c)+ONCE_PER_DAY);
        return p.edit().putBoolean(key,true).putInt("total",points).commit()
                ?ONCE_PER_DAY:0;
    }
}
