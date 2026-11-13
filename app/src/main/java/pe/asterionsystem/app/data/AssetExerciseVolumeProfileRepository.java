package pe.asterionsystem.app.data;

import android.content.Context;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.progression.ExerciseVolumeProfileCatalog;

/**
 * Local-only versioned numeric references. Fails closed if any variant is
 * missing: a bad catalogue must not fall back to an unsafe generic workout.
 */
public final class AssetExerciseVolumeProfileRepository {
    private final Context context;

    public AssetExerciseVolumeProfileRepository(Context context) {
        this.context=Objects.requireNonNull(context,"context").getApplicationContext();
    }

    public ExerciseVolumeProfileCatalog load(ExerciseCatalog catalog) {
        try(InputStreamReader input=new InputStreamReader(
                context.getAssets().open("asterion_exercises/v2/volume_profiles.tsv"),
                StandardCharsets.UTF_8)) {
            return ExerciseVolumeProfileCatalog.load(input,catalog);
        }catch(IOException|IllegalArgumentException error) {
            throw new IllegalStateException(
                    "No se pudo verificar las unidades y referencias de los ejercicios",error);
        }
    }
}
