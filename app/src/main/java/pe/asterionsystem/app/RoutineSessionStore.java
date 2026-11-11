package pe.asterionsystem.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.time.LocalDate;
import java.util.List;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;

/** Locally preserves the step-by-step screen across app restarts. */
final class RoutineSessionStore {
    private static final String FILE="asterion_routine_flow_v1";
    private RoutineSessionStore() {}

    static final class State {
        final int index;
        final int round;
        final int extraSeries;
        State(int index,int round,int extraSeries) {
            this.index=index;
            this.round=round;
            this.extraSeries=extraSeries;
        }
    }
    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(FILE,Context.MODE_PRIVATE);
    }

    static String token(String kind,LocalDate date,List<ExerciseVariant> exercises) {
        if(!"daily".equals(kind) && !"monarch".equals(kind) && !"custom".equals(kind))
            throw new IllegalArgumentException("Tipo inválido");
        if(date==null||exercises==null||exercises.isEmpty())
            throw new IllegalArgumentException("Sesión vacía");
        StringBuilder fingerprint=new StringBuilder(kind).append('|').append(date);
        for(ExerciseVariant v:exercises)fingerprint.append('|').append(v.id());
        return kind+"_"+date+"_"+Integer.toHexString(fingerprint.toString().hashCode());
    }

    static State read(Context c,String token,int size) {
        SharedPreferences p=prefs(c);
        int position=p.getInt(token+"_index",0);
        int round=p.getInt(token+"_round",1);
        int series=p.getInt(token+"_series",0);
        return new State(Math.max(0,Math.min(Math.max(0,size),position)),
                Math.max(1,Math.min(999,round)),Math.max(0,Math.min(999,series)));
    }

    static void save(Context c,String token,int index,int round,int extraSeries) {
        if(index<0||round<1||extraSeries<0)
            throw new IllegalArgumentException("Progreso inválido");
        prefs(c).edit().putInt(token+"_index",index)
                .putInt(token+"_round",round).putInt(token+"_series",extraSeries).apply();
    }

    static void clear(Context c,String token) {
        prefs(c).edit().remove(token+"_index").remove(token+"_round")
                .remove(token+"_series").apply();
    }
}
