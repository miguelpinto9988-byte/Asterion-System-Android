package pe.asterionsystem.core.engine.exercises;

import java.util.Locale;

/**
 * Descriptive activity-intensity class, not a personal effort prescription.
 * Intensity varies with execution, conditions and the individual.
 */
public enum ExerciseIntensity {
    LOW("baja", "Baja"),
    MODERATE("moderada", "Moderada"),
    HIGH("alta", "Alta"),
    VARIABLE("variable", "Variable"),
    UNCLASSIFIED("sin_clasificar", "Sin clasificar");

    private final String code;
    private final String label;
    ExerciseIntensity(String code, String label) {
        this.code=code;
        this.label=label;
    }
    public String code() {return code;}
    public String label() {return label;}

    public static ExerciseIntensity parse(String value) {
        if(value==null)throw new IllegalArgumentException("Intensidad desconocida");
        for(ExerciseIntensity item:values()) {
            if(item!=UNCLASSIFIED && item.code.equals(value))return item;
        }
        throw new IllegalArgumentException("Intensidad no admitida: "+value);
    }
}
