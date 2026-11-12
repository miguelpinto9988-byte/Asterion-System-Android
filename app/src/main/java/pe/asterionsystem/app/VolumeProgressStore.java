package pe.asterionsystem.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;
import pe.asterionsystem.core.engine.progression.VolumeProgressionEngine;
import pe.asterionsystem.core.engine.progression.VolumeResetPolicy;

/**
 * Opt-in, device-local, per-day exercise volume. Separate from legacy SQLite,
 * completed marks, and mission pinning. An absent log is never inferred.
 * No body measurements or personal identifiers are stored.
 */
final class VolumeProgressStore {
    private static final String FILE = "asterion_volume_progress_v1";
    private static final String PREFIX = "vol_";
    // Metadata only; neither the original volume log nor category effort is deleted.
    private static final String LAST_SELECTION = "selection_category_";
    private static final String LAST_CHECKPOINT = "checkpoint_category_";
    private static final String EXPERIENCE_ENTRY = "level_entry_category_";
    private static final String VOLUME_RESTART = "volume_restart_";

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


    /** 
     * Called after daily mission pinning, before publishing new volume references.
     * The prior selection is category-local. At the first 7/14/21 review after
     * that category was seen, changed variants begin a new numeric history.
     * The same-day call is idempotent, including when reopening the screen.
     * The old observations remain stored for non-numeric historical analysis.
     */
    static VolumeResetPolicy.Decision observeCheckpointSelection(
            Context context, int category, LocalDate today,
            LocalDate experienceEntered, int checkpoint,
            List<ExerciseVariant> selected) {
        if (category < 0 || category > 4 || today == null
                || experienceEntered == null || selected == null) {
            throw new IllegalArgumentException("Selección o categoría inválida");
        }
        SharedPreferences sp = prefs(context);
        String priorText = sp.getString(LAST_SELECTION + category, "");
        List<String> prior = priorText.isEmpty()
                ? Collections.emptyList() : Arrays.asList(priorText.split(",", -1));
        String initialEntry = sp.getString(EXPERIENCE_ENTRY + category, "");
        boolean experienceChanged = !initialEntry.isEmpty()
                && !experienceEntered.toString().equals(initialEntry);
        int previousCheckpoint = experienceChanged ? 0
                : sp.getInt(LAST_CHECKPOINT + category, 0);
        VolumeResetPolicy.Decision result = VolumeResetPolicy.evaluate(
                previousCheckpoint, checkpoint, prior, selected);
        SharedPreferences.Editor editor = sp.edit()
                .putString(LAST_SELECTION + category,
                        String.join(",", VolumeResetPolicy.snapshot(selected)))
                .putString(EXPERIENCE_ENTRY + category, experienceEntered.toString())
                .putInt(LAST_CHECKPOINT + category,
                        Math.max(previousCheckpoint, checkpoint));
        if (experienceChanged) {
            // A new technical-experience profile cannot silently reuse an old
            // higher numeric target even before the first weekly checkpoint.
            for (ExerciseVariant entry : selected) {
                editor.putString(restartKey(category, entry.id()), today.toString());
            }
        } else {
            for (String id : result.restartVariantIds) {
                editor.putString(restartKey(category, id), today.toString());
            }
        }
        if (!editor.commit()) {
            throw new IllegalStateException("No se pudo registrar el nuevo bloque de volumen");
        }
        return result;
    }

    private static String restartKey(int category, String variantId) {
        return VOLUME_RESTART + category + "_" + variantId;
    }

    private static LocalDate parseDate(String raw) {
        try { return LocalDate.parse(raw); }
        catch (RuntimeException invalid) { return null; }
    }

    static boolean restartedOn(Context context, int category,
                               String variantId, LocalDate today) {
        if (today == null || category < 0 || category > 4) return false;
        return today.equals(parseDate(prefs(context).getString(
                restartKey(category, variantId), "")));
    }

    /**
     * Only the NUMERIC progression is restarted. The selector's earlier
     * category effort reports continue to work as safety signals.
     */
    static List<VolumeProgressionEngine.Observation> progressionHistoryForCategory(
            Context context, int category, LocalDate before) {
        List<VolumeProgressionEngine.Observation> full =
                previousForCategory(context, category, before);
        SharedPreferences sp = prefs(context);
        ArrayList<VolumeProgressionEngine.Observation> recent = new ArrayList<>();
        for (VolumeProgressionEngine.Observation item : full) {
            LocalDate restart = parseDate(sp.getString(
                    restartKey(category, item.variantId), ""));
            if (restart == null || !item.day.isBefore(restart)) recent.add(item);
        }
        return Collections.unmodifiableList(recent);
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
