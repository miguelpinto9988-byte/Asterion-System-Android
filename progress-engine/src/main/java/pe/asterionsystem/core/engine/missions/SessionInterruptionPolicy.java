package pe.asterionsystem.core.engine.missions;

/**
 * Closed-answer session interruption policy. Does not diagnose medical issues.
 * Unexpected serious signs suspend all exercise suggestions until medical
 * evaluation, not merely one calendar day. Other reports affect only the
 * category where the interruption occurred.
 */
public final class SessionInterruptionPolicy {
    public static final int EXHAUSTION=0;
    public static final int MINOR_DISCOMFORT=1;
    public static final int SERIOUS_JOINT_PAIN=2;
    public static final int INTENSE_DIZZINESS_VOMITING=3;
    public static final int STOP_WITHOUT_REASON=4;

    public enum Action {
        CATEGORY_LIGHTER_NEXT_SESSION,
        CATEGORY_PAUSE_UNTIL_REVIEW,
        GLOBAL_PAUSE_AND_SEEK_MEDICAL_CARE,
        STOP_ONLY
    }

    public static final class Assessment {
        public final int choice;
        public final Action action;
        public final String explanation;
        public final boolean urgentMedicalAdvice;
        private Assessment(int choice, Action action, String explanation, boolean urgent) {
            this.choice=choice;
            this.action=action;
            this.explanation=explanation;
            this.urgentMedicalAdvice=urgent;
        }
    }

    private SessionInterruptionPolicy() { }

    public static Assessment assess(int choice) {
        switch(choice) {
            case EXHAUSTION:
                return new Assessment(choice,Action.CATEGORY_LIGHTER_NEXT_SESSION,
                        "Sesión detenida. En la siguiente sesión de esta categoría "
                                +"se propondrán menos actividades y opciones sencillas.",false);
            case MINOR_DISCOMFORT:
                return new Assessment(choice,Action.CATEGORY_PAUSE_UNTIL_REVIEW,
                        "Sesión detenida. La categoría quedará en pausa; no retomes "
                                +"actividades que provoquen dolor. Solicita orientación "
                                +"si persiste o empeora.",false);
            case SERIOUS_JOINT_PAIN:
                return new Assessment(choice,Action.GLOBAL_PAUSE_AND_SEEK_MEDICAL_CARE,
                        "Detén el ejercicio y solicita valoración médica urgente "
                                +"si sospechas una lesión grave, esguince o luxación. "
                                +"No se reanudará automáticamente mañana.",true);
            case INTENSE_DIZZINESS_VOMITING:
                return new Assessment(choice,Action.GLOBAL_PAUSE_AND_SEEK_MEDICAL_CARE,
                        "Detén el ejercicio y pide ayuda médica urgente ante mareo "
                                +"intenso, vómitos o desmayo. No reanudes automáticamente.",true);
            case STOP_WITHOUT_REASON:
                return new Assessment(choice,Action.STOP_ONLY,
                        "Sesión detenida. Puedes descansar sin explicar el motivo "
                                +"y sin penalizaciones.",false);
            default:
                throw new IllegalArgumentException("Respuesta desconocida");
        }
    }
}
