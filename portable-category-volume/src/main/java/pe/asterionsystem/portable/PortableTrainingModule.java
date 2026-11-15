package pe.asterionsystem.portable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalogLoader;
import pe.asterionsystem.core.engine.exercises.ExerciseCategoryMapper;
import pe.asterionsystem.core.engine.exercises.ExerciseFamily;
import pe.asterionsystem.core.engine.exercises.ExerciseType;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;
import pe.asterionsystem.core.engine.progression.ExerciseVolumeProfileCatalog;
import pe.asterionsystem.core.engine.progression.VolumeProgressionEngine;

/** Standalone taxonomy and read-only volume suggestion facade. */
public final class PortableTrainingModule {
    private static final String ASSETS="asterion_exercises/v1/";
    private final ExerciseCatalog catalog;
    private final ExerciseVolumeProfileCatalog profiles;
    private final PortableTrainingSettings settings;

    private PortableTrainingModule(ExerciseCatalog catalog,
            ExerciseVolumeProfileCatalog profiles, PortableTrainingSettings settings) {
        this.catalog=catalog;
        this.profiles=profiles;
        this.settings=settings;
    }

    private static Reader open(String path) {
        InputStream stream=PortableTrainingModule.class.getClassLoader()
                .getResourceAsStream(path);
        if(stream==null)throw new IllegalStateException("No está empaquetado: "+path);
        return new InputStreamReader(stream,StandardCharsets.UTF_8);
    }

    public static PortableTrainingModule loadDefaults() throws IOException {
        Properties props=new Properties();
        try(Reader reader=open("configuracion-inicial.properties")){props.load(reader);}
        return load(PortableTrainingSettings.from(props));
    }

    public static PortableTrainingModule load(PortableTrainingSettings settings)
            throws IOException {
        try(Reader families=open(ASSETS+"families.tsv");
            Reader types=open(ASSETS+"types.tsv");
            Reader variants=open(ASSETS+"variants.tsv");
            Reader intensities=open(ASSETS+"intensity.tsv");
            Reader registry=open(ASSETS+"sport_registry.tsv");
            Reader tags=open(ASSETS+"sport_tags.tsv");
            Reader volumes=open(ASSETS+"volume_profiles.tsv")) {
            return fromReaders(families,types,variants,intensities,
                    registry,tags,volumes,settings);
        }
    }

    /** Inject custom datasets; caller must close the Readers. */
    public static PortableTrainingModule fromReaders(
            Reader families, Reader types, Reader variants, Reader intensity,
            Reader sportRegistry, Reader sportTags, Reader volume,
            PortableTrainingSettings settings) throws IOException {
        Objects.requireNonNull(settings,"settings");
        ExerciseCatalog catalog=ExerciseCatalogLoader.load(
                families,types,variants,intensity,sportRegistry,sportTags);
        ExerciseVolumeProfileCatalog references=ExerciseVolumeProfileCatalog.load(
                volume,catalog);
        return new PortableTrainingModule(catalog,references,settings);
    }

    public PortableTrainingSettings settings(){return settings;}
    public ExerciseCatalog catalog(){return catalog;}
    public ExerciseVolumeProfileCatalog profiles(){return profiles;}
    public List<String> categories(){return settings.categories;}

    private void requireEnabled(String code) {
        if(!settings.categories.contains(code))
            throw new IllegalArgumentException("Categoría deshabilitada: "+code);
    }
    public List<ExerciseFamily> families(String code) {
        requireEnabled(code);
        return catalog.categoryIndex().families(code);
    }
    public List<ExerciseType> types(String code) {
        requireEnabled(code);
        return catalog.categoryIndex().types(code);
    }
    public List<ExerciseVariant> variants(String code) {
        requireEnabled(code);
        return catalog.categoryIndex().variants(code);
    }
    public ExerciseVariant variant(String id) {
        return catalog.variant(id).orElseThrow(
                ()->new IllegalArgumentException("ID desconocido: "+id));
    }
    public String categoryForVariant(String variantId) {
        return catalog.categoryIndex().categoryForVariant(variantId);
    }
    public ExerciseVolumeProfileCatalog.Profile initialReference(String variantId) {
        return profiles.require(variant(variantId));
    }

    /** This method neither stores nor applies the returned optional reference. */
    public Optional<VolumeProgressionEngine.Suggestion> review(
            String variantId, LocalDate nextDay,
            List<VolumeProgressionEngine.Observation> history,
            boolean pausedOrInterrupted) {
        if(!settings.volumeSuggestionsEnabled)return Optional.empty();
        String category=categoryForVariant(variantId);
        requireEnabled(category);
        ExerciseVariant selected=variant(variantId);
        return Optional.of(VolumeProgressionEngine.review(selected,
                ExerciseCategoryMapper.weeklyIndex(category),
                Objects.requireNonNull(nextDay,"nextDay"),
                Objects.requireNonNull(history,"history"),
                pausedOrInterrupted,profiles.require(selected)));
    }
}
