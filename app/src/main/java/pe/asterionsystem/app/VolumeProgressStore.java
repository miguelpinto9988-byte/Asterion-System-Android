package pe.asterionsystem.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import pe.asterionsystem.core.engine.progression.VolumeProgressionEngine;

/**
 * Opt-in, device-local, per-day exercise volume. Separate from legacy SQLite,
 * completed marks, and mission pinning. An absent log is never inferred.
 * No body measurements or personal identifiers are stored.
 */
final class VolumeProgressStore {
    private static final String FILE = "asterion_volume_progress_v1";
    private static final String PREFIX = "vol_";

    private VolumeProgressStore() { }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(
                FILE, Context.MODE_PRIVATE);
    }

    private static String key(VolumeProgressionEngine.Observation entry) {
        return PREFIX + entry.day + "_" + entry.variantId;
    }

    private static String encode(VolumeProgressionEngine.Observation entry) {
        return entry.day + "|" + entry.category + "|" + entry.variantId
                + "|" + entry.typeId + "|" + entry.technicalLevel
                + "|" + entry.sets + "|" + entry.repetitions + "|"
                + entry.effort + "|" + (entry.completed ? 1 : 0) + "|"
                + (entry.interrupted ? 1 : 0);
    }

    private static VolumeProgressionEngine.Observation decode(String raw) {
        if (raw == null) return null;
        try {
            String[] p = raw.split("\\|", -1);
            if (p.length != 10) return null;
            return new VolumeProgressionEngine.Observation(
                    LocalDate.parse(p[0]), Integer.parseInt(p[1]),
                    p[2], p[3], Integer.parseInt(p[4]),
                    Integer.parseInt(p[5]), Integer.parseInt(p[6]),
                    Integer.parseInt(p[7]), "1".equals(p[8]), "1".equals(p[9]));
        } catch (RuntimeException invalid) {
            return null;
        }
    }

    /**
     * Persist only after the user has explicitly completed the exercise.
     * Repeated rounds on the same day accumulate sets; the smaller per-set
     * repetitions and larger effort are retained conservatively.
     */
    static void recordCompleted(Context context, VolumeProgressionEngine.Observation entry) {
        if (!entry.completed || entry.interrupted || !entry.day.equals(LocalDate.now())) {
            throw new IllegalArgumentException("Solo se registra una sesión completada hoy");
        }
        SharedPreferences sp = prefs(context);
        String entryKey = key(entry);
        VolumeProgressionEngine.Observation old = decode(sp.getString(entryKey, ""));
        VolumeProgressionEngine.Observation toStore = entry;
        if (old != null) {
            if (!old.day.equals(entry.day) || old.category != entry.category
                    || !old.variantId.equals(entry.variantId)
                    || !old.typeId.equals(entry.typeId)
                    || old.technicalLevel != entry.technicalLevel) {
                throw new IllegalArgumentException("Datos de sesión incompatibles");
            }
            toStore = new VolumeProgressionEngine.Observation(
                    entry.day, entry.category, entry.variantId, entry.typeId,
                    entry.technicalLevel, Math.min(12, old.sets + entry.sets),
                    Math.min(old.repetitions, entry.repetitions),
                    Math.max(old.effort, entry.effort), true, false);
        }
        if (!sp.edit().putString(entryKey, encode(toStore)).commit()) {
            throw new IllegalStateException("No se pudo guardar el volumen");
        }
        // The engine reviews at most 21 days. Expiration is deliberately longer.
        SharedPreferences.Editor cleanup = sp.edit();
        boolean changed = false;
        for (Map.Entry<String, ?> item : sp.getAll().entrySet()) {
            if (!item.getKey().startsWith(PREFIX)) continue;
            VolumeProgressionEngine.Observation value =
                    decode(item.getValue() instanceof String ? (String) item.getValue() : null);
            if (value == null || value.day.isBefore(LocalDate.now().minusDays(90))) {
                cleanup.remove(item.getKey());
                changed = true;
            }
        }
        if (changed) cleanup.apply();
    }

    /** Read only prior days of this same category; today's display stays pinned. */
    static List<VolumeProgressionEngine.Observation> previousForCategory(
            Context context, int category, LocalDate before) {
        if (category < 0 || category > 4 || before == null) {
            throw new IllegalArgumentException("Categoría o fecha inválida");
        }
        ArrayList<VolumeProgressionEngine.Observation> result = new ArrayList<>();
        for (Map.Entry<String, ?> item : prefs(context).getAll().entrySet()) {
            if (!item.getKey().startsWith(PREFIX)) continue;
            VolumeProgressionEngine.Observation value =
                    decode(item.getValue() instanceof String ? (String) item.getValue() : null);
            if (value != null && value.category == category
                    && value.day.isBefore(before)
                    && !value.day.isBefore(before.minusDays(21))) result.add(value);
        }
        result.sort((a,b) -> b.day.compareTo(a.day));
        return Collections.unmodifiableList(result);
    }
}
