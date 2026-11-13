package pe.asterionsystem.core.engine.exercises;

import java.util.List;

/**
 * Stable bridge between historic seven taxonomy sections and the five
 * workout categories configured in the profile. No exercise ID is changed.
 * The source family's descriptive category remains available for reference.
 */
public final class ExerciseCategoryMapper {
    private static final List<String> WEEKLY_CODES=List.of(
            "tren_superior","tren_central","tren_inferior","movilidad","cardio");
    private static final List<String> DISPLAY_NAMES=List.of(
            "Tren superior","Tren central","Tren inferior","Movilidad","Cardio");

    private ExerciseCategoryMapper() {}

    public static String primaryCategory(String originalCategory) {
        if(originalCategory==null)return "";
        switch(originalCategory) {
            case "tren_superior":
            case "tren_central":
            case "tren_inferior":
            case "movilidad":
            case "cardio":
                return originalCategory;
            case "tronco":
            case "integrado":
                return "tren_central";
            case "piernas":
                return "tren_inferior";
            case "coordinacion":
                // Neuromotor balance and coordination = movement control.
                return "movilidad";

            default:
                return "";
        }
    }

    public static int weeklyIndex(String originalCategory) {
        return WEEKLY_CODES.indexOf(primaryCategory(originalCategory));
    }

    public static String weeklyCode(int index) {
        if(index<0 || index>=WEEKLY_CODES.size()) {
            throw new IllegalArgumentException("Categoría fuera de 0..4");
        }
        return WEEKLY_CODES.get(index);
    }

    public static String displayName(int index) {
        if(index<0 || index>=DISPLAY_NAMES.size()) {
            throw new IllegalArgumentException("Categoría fuera de 0..4");
        }
        return DISPLAY_NAMES.get(index);
    }

    public static List<String> weeklyCodes() { return WEEKLY_CODES; }
}
