package pe.asterionsystem.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONObject;
import pe.asterionsystem.core.engine.missions.MissionPlanner;

/**
 * Reads the existing completed four-step registration, NOT a temporary draft.
 * Never treats body measurements as a performance score or intensity target.
 */
final class MissionProfileAdapter {
    private MissionProfileAdapter() { }

    static MissionPlanner.Profile fromRegistration(JSONObject saved) {
        JSONObject profile = saved == null ? new JSONObject() : saved;
        List<Integer> week = new ArrayList<>(7);
        String[] values = profile.optString("week", "5,5,5,5,5,5,5").split(",");
        for (int i=0; i<7; i++) {
            int category = 5;
            if (values.length == 7) {
                try {
                    category = Integer.parseInt(values[i].trim());
                } catch (NumberFormatException ignored) { }
            }
            week.add(category < 0 || category > 5 ? 5 : category);
        }
        Set<String> equipment = new HashSet<>();
        if (isEnabled(profile, "bodyweight", true)) equipment.add("sin_equipo");
        if (isEnabled(profile, "dumbbells", false)) equipment.add("mancuerna");
        if (isEnabled(profile, "gym", false)) {
            String[] keys = {"polea","maquina","banco","barra","bicicleta","remo","eliptica"};
            for (String key : keys) if (isEnabled(profile, "gym_"+key, false)) {
                equipment.add(key);
            }
        }
        int dailyCount = 3;
        try { dailyCount = Integer.parseInt(profile.optString("missions", "3")); }
        catch (NumberFormatException ignored) { }
        return new MissionPlanner.Profile(
                profile.optString("experience","principiante"),
                profile.optString("goal","bienestar"),
                week, equipment, dailyCount);
    }

    private static boolean isEnabled(JSONObject source, String key, boolean fallback) {
        if (!source.has(key)) return fallback;
        return Boolean.parseBoolean(source.optString(key, String.valueOf(fallback)));
    }
}
