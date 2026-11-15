package pe.asterionsystem.core.engine.missions;

/**
 * Conservative difficulty mix for optional daily proposals. The catalog's
 * 1..5 values measure TECHNICAL complexity, not exertion or age.
 *
 * Beginner: technical levels 1-2 (e.g. supported variations).
 * Intermediate: mostly 1-2 at entry, some level 3; a repeated weekly
 * low-effort/completed review may gradually increase the level 3 share.
 * Advanced: levels 2 and 3 with progressively more level 3 after evidence.
 *
 * Level 4-5 and supervised-only catalog entries are NEVER scheduled
 * automatically. This policy cannot raise exercise count, load or intensity.
 */
public final class MissionDifficultyMixPolicy {
    public static final int MAX_AUTOMATIC_LEVEL=3;
    public static final int PHASES=3;

    private MissionDifficultyMixPolicy() { }

    public static int clampPhase(int phase) {
        return Math.max(0,Math.min(PHASES-1,phase));
    }

    public static boolean mixed(String experience) {
        return "intermedio".equals(experience) || "avanzado".equals(experience);
    }

    public static int maxTechnicalLevel(String experience) {
        return mixed(experience)?MAX_AUTOMATIC_LEVEL:2;
    }

    /** Target only: it may be reduced when compatible alternatives are scarce. */
    public static int higherTarget(String experience,int phase,int activities) {
        if(activities<0 || activities>7)
            throw new IllegalArgumentException("Número de actividades inválido");
        if(activities==0 || !mixed(experience))return 0;
        int[] percentages="avanzado".equals(experience)
                ?new int[]{33,55,75}:new int[]{25,40,60};
        int share=percentages[clampPhase(phase)];
        int target=Math.round(activities*share/100f);
        // When three or more proposals are requested, retain both levels
        // from the first week if compatible exercises are available.
        if(activities>=3 && target==0)target=1;
        return Math.min(activities,Math.max(0,target));
    }

    public static String explanation(String experience,int phase) {
        if("principiante".equals(experience))
            return "Nivel básico: variantes técnicas sencillas (1 y 2), "
                    +"sin actividades avanzadas automáticas.";
        if("intermedio".equals(experience))
            return "Nivel intermedio: combinación de variantes básicas (1-2) "
                    +"e intermedias (3), ajustada según sesiones realizadas "
                    +"y esfuerzo comunicado. Etapa "+(clampPhase(phase)+1)+"/3.";
        if("avanzado".equals(experience))
            return "Nivel avanzado: combinación de niveles técnicos 2 y 3. "
                    +"Las variantes 4-5 requieren supervisión y no se proponen "
                    +"automáticamente. Etapa "+(clampPhase(phase)+1)+"/3.";
        return "Nivel no reconocido: se conservan solo variantes sencillas.";
    }
}
