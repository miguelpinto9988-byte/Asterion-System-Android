package pe.asterionsystem.app.data;

import android.content.Context;
import android.os.Looper;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import pe.asterionsystem.app.AvatarExerciseCommandCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;

/** Offline, read-only adapter. Catalog construction must run off the UI thread. */
public final class AssetAvatarExerciseCommandRepository {
    private static final String PATH="asterion_exercises/v1/avatar_motion_commands.tsv";
    private final Context context;
    public AssetAvatarExerciseCommandRepository(Context context){
        this.context=Objects.requireNonNull(context,"context").getApplicationContext();
    }
    public AvatarExerciseCommandCatalog load(ExerciseCatalog catalog){
        if(Looper.myLooper()==Looper.getMainLooper())
            throw new IllegalStateException("Cargar comandos fuera del hilo UI");
        try(Reader input=new InputStreamReader(context.getAssets().open(PATH),
                StandardCharsets.UTF_8)){
            return AvatarExerciseCommandCatalog.load(input,catalog);
        }catch(IOException problem){
            throw new IllegalStateException("Comandos de avatar no disponibles",problem);
        }
    }
}
