package pe.asterionsystem.core.engine.progression;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;

/**
 * Explicit, versioned per-VARIANT volume metadata, validated against every
 * catalog exercise. Repetitions and durations are never interchanged.
 * Starting values are editable REFERENCE defaults, not instructions to
 * complete any activity. Manual/supervised variants never auto-progress.
 */
public final class ExerciseVolumeProfileCatalog {
    public enum Unit {
        REPETITIONS("repeticiones"), SECONDS("segundos");
        public final String label;
        Unit(String label) { this.label = label; }
    }

    public static final class Profile {
        public final String variantId;
        public final Unit unit;
        public final int initialSets, initialValue, minimumValue, maximumValue;
        /** Multiplies the RPE delta; repetitions use 1, time uses 1 or 3 s. */
        public final int increment;
        public final boolean automaticEligible;

        public Profile(String variantId, Unit unit, int initialSets,
                       int initialValue, int minimumValue, int maximumValue,
                       int increment, boolean automaticEligible) {
            if (variantId == null || !variantId.matches("[a-z][a-z0-9_]{1,127}")
                    || unit == null
                    || initialSets < 1 || initialSets > 3
                    || minimumValue < 1 || maximumValue < minimumValue
                    || initialValue < minimumValue || initialValue > maximumValue
                    || increment < 1 || increment > 5
                    || (unit == Unit.REPETITIONS && maximumValue > 20)
                    || (unit == Unit.SECONDS && maximumValue > 180)) {
                throw new IllegalArgumentException("Referencia de volumen inválida");
            }
            this.variantId = variantId;
            this.unit = unit;
            this.initialSets = initialSets;
            this.initialValue = initialValue;
            this.minimumValue = minimumValue;
            this.maximumValue = maximumValue;
            this.increment = increment;
            this.automaticEligible = automaticEligible;
        }

        public String display(int sets,int value) {
            return sets + " serie" + (sets==1?"":"s") + " × "
                    + value + " " + unit.label;
        }
    }

    private final Map<String, Profile> byVariant;

    private ExerciseVolumeProfileCatalog(Map<String, Profile> profiles) {
        byVariant = Collections.unmodifiableMap(new LinkedHashMap<>(profiles));
    }

    public Profile require(ExerciseVariant exercise) {
        Objects.requireNonNull(exercise,"exercise");
        Profile entry=byVariant.get(exercise.id());
        if (entry==null) throw new IllegalArgumentException(
                "Falta referencia: " + exercise.id());
        return entry;
    }

    public int size() { return byVariant.size(); }

    /**
     * Strict TSV. Every real catalog variation (including manual-only
     * variations) must be represented exactly once; never infer quantities
     * from a generic category or silently fall back on invalid metadata.
     */
    public static ExerciseVolumeProfileCatalog load(
            Reader input, ExerciseCatalog catalog) throws IOException {
        Objects.requireNonNull(input,"input");
        Objects.requireNonNull(catalog,"catalog");
        BufferedReader reader = input instanceof BufferedReader
                ? (BufferedReader) input : new BufferedReader(input);
        String expected = "variant_id\tunit\tinitial_sets\tinitial_value\t"
                + "minimum_value\tmaximum_value\tincrement";
        if (!expected.equals(reader.readLine()))
            throw new IllegalArgumentException("Cabecera de volumen desconocida");
        Map<String,Profile> entries = new LinkedHashMap<>();
        String line;
        while ((line=reader.readLine())!=null) {
            if (line.isBlank() || line.startsWith("#")) continue;
            if (line.length()>320)throw new IllegalArgumentException("Fila excesiva");
            String[] p=line.split("\t",-1);
            if(p.length!=7)throw new IllegalArgumentException("Columnas de volumen inválidas");
            ExerciseVariant variant=catalog.variant(p[0]).orElseThrow(()->
                    new IllegalArgumentException("Variante sin catálogo: "+p[0]));
            Unit unit;
            try { unit=Unit.valueOf(p[1]); }
            catch(RuntimeException invalid) {
                throw new IllegalArgumentException("Unidad no reconocida: "+p[1],invalid);
            }
            Profile profile=new Profile(p[0],unit,
                    Integer.parseInt(p[2]),Integer.parseInt(p[3]),
                    Integer.parseInt(p[4]),Integer.parseInt(p[5]),
                    Integer.parseInt(p[6]),
                    catalog.eligibleForAutomaticSuggestion(variant));
            if(entries.putIfAbsent(p[0],profile)!=null)
                throw new IllegalArgumentException("Variante repetida: "+p[0]);
        }
        if(entries.size()!=catalog.variants().size()) {
            for(ExerciseVariant variant:catalog.variants()) {
                if(!entries.containsKey(variant.id()))
                    throw new IllegalArgumentException(
                            "Falta referencia: "+variant.id());
            }
            throw new IllegalArgumentException("Cobertura de catálogo incorrecta");
        }
        return new ExerciseVolumeProfileCatalog(entries);
    }
}
