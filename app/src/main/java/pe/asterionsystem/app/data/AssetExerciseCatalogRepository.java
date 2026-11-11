package pe.asterionsystem.app.data;

import android.content.Context;
import android.os.Looper;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalogLoader;

/**
 * Read-only Android adapter for versioned, packaged exercise taxonomy assets.
 * Call from a worker; creating this index does not write to SQLite v5.
 */
public final class AssetExerciseCatalogRepository {
    private static final String DIRECTORY = "asterion_exercises/v1/";
    private final Context context;

    public AssetExerciseCatalogRepository(Context context) {
        this.context = Objects.requireNonNull(context, "context").getApplicationContext();
    }

    public ExerciseCatalog load() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("El catálogo debe cargarse fuera del hilo UI");
        }
        try (Reader families = new InputStreamReader(
                     context.getAssets().open(DIRECTORY + "families.tsv"), StandardCharsets.UTF_8);
             Reader types = new InputStreamReader(
                     context.getAssets().open(DIRECTORY + "types.tsv"), StandardCharsets.UTF_8);
             Reader variants = new InputStreamReader(
                     context.getAssets().open(DIRECTORY + "variants.tsv"), StandardCharsets.UTF_8);
             Reader intensity = new InputStreamReader(
                     context.getAssets().open(DIRECTORY + "intensity.tsv"), StandardCharsets.UTF_8);
             Reader sportRegistry = new InputStreamReader(
                     context.getAssets().open(DIRECTORY + "sport_registry.tsv"), StandardCharsets.UTF_8);
             Reader sportTags = new InputStreamReader(
                     context.getAssets().open(DIRECTORY + "sport_tags.tsv"), StandardCharsets.UTF_8)) {
            return ExerciseCatalogLoader.load(families, types, variants,
                    intensity, sportRegistry, sportTags);
        } catch (IOException invalid) {
            throw new IllegalStateException("No se pudo cargar el catálogo de ejercicios", invalid);
        }
    }
}
