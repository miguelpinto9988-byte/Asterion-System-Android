package pe.asterionsystem.core.engine.progression;

/**
 * Independent automatic per-category adjustments for the NEXT occurrence of
 * that same weekly training category. The policy never changes stored calendar
 * days, repetition targets, weights or exercise counts upward.
 */
public final class CategoryEffortPolicy {
    public static final int UPPER = 0;
    public static final int CORE = 1;
    public static final int LOWER = 2;
    public static final int MOBILITY = 3;
    public static final int CARDIO = 4;
    public static final int REST = 5;
    public static final String[] CATEGORY_NAMES = {
            "Tren superior", "Tren central", "Tren inferior", "Movilidad",
            "Cardio", "Descanso"
    };

    public enum Guidance { NO_DATA, REVIEW_OPTIONAL, MAINTAIN, EASIER }

    public static final class Decision {
        public final int category;
        public final int lastEffort;
        public final Guidance guidance;
        public final int suggestedActivities;
        public final boolean preferSimplerTechnique;
        public final String explanation;

        private Decision(int category, int lastEffort, Guidance guidance,
                         int suggestedActivities, boolean preferSimplerTechnique,
                         String explanation) {
            this.category=category;
            this.lastEffort=lastEffort;
            this.guidance=guidance;
            this.suggestedActivities=suggestedActivities;
            this.preferSimplerTechnique=preferSimplerTechnique;
            this.explanation=explanation;
        }
    }

    private CategoryEffortPolicy() { }

    /**
     * Rating 0 means no category-specific observation. Reports from another
     * body region MUST NOT be passed here. Low effort can trigger a suitable
     * same-level variant swap, NOT an automatic workload increase.
     */
    public static Decision evaluate(int category, int lastEffort, int configuredActivities) {
        if (category < UPPER || category > REST) {
            throw new IllegalArgumentException("Categoría desconocida");
        }
        if (lastEffort < 0 || lastEffort > 10) {
            throw new IllegalArgumentException("Esfuerzo fuera de escala");
        }
        int baseline=Math.max(1,Math.min(7,configuredActivities));
        if(category == REST) {
            return new Decision(category,lastEffort,Guidance.MAINTAIN,0,false,
                    "Descanso configurado. No se generan ejercicios.");
        }
        String categoryName=CATEGORY_NAMES[category];
        if(lastEffort==0) {
            return new Decision(category,0,Guidance.NO_DATA,baseline,false,
                    "Sin valoración previa de "+categoryName+". Se conserva la configuración.");
        }
        if(lastEffort<=3) {
            return new Decision(category,lastEffort,Guidance.REVIEW_OPTIONAL,baseline,false,
                    "En "+categoryName+" declaraste poco esfuerzo ("+lastEffort+
                    "/10). El motor buscará automáticamente una variante del mismo tipo y nivel técnico, "
                    +"si existe. Los ajustes de repeticiones se calculan por separado.");
        }
        if(lastEffort<=7) {
            return new Decision(category,lastEffort,Guidance.MAINTAIN,baseline,false,
                    "Esfuerzo moderado en "+categoryName+" ("+lastEffort+
                    "/10). Se conserva el nivel y la cantidad propuesta.");
        }
        int count=lastEffort>=9 ? Math.min(1,baseline) : Math.min(2,baseline);
        return new Decision(category,lastEffort,Guidance.EASIER,count,true,
                "Esfuerzo alto en "+categoryName+" ("+lastEffort+
                "/10). Se sugieren menos actividades y variantes técnicas sencillas. "
                +"Descansar o no completar la sesión es válido.");
    }
}
