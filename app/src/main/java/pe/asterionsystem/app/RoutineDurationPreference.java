package pe.asterionsystem.app;

/**
 * Shared duration options for profile registration, profile editing and
 * routine generation. This is a UI preference, never a required workout time.
 */
public final class RoutineDurationPreference {
    public static final int DEFAULT_MINUTES=30;
    private static final int[] MINUTES={10,15,20,30,45,60};

    private RoutineDurationPreference(){ }

    public static int[] options() {return MINUTES.clone();}

    public static String[] optionStrings() {
        String[] result=new String[MINUTES.length];
        for(int i=0;i<MINUTES.length;i++)result[i]=String.valueOf(MINUTES[i]);
        return result;
    }

    public static String[] displayOptions() {
        String[] result=new String[MINUTES.length];
        for(int i=0;i<MINUTES.length;i++)result[i]=MINUTES[i]+" minutos";
        return result;
    }

    public static boolean isSupported(int minutes) {
        for(int option:MINUTES)if(option==minutes)return true;
        return false;
    }

    public static int parse(String value) {
        if(value==null)return DEFAULT_MINUTES;
        try {
            int minutes=Integer.parseInt(value.trim());
            return isSupported(minutes)?minutes:DEFAULT_MINUTES;
        } catch(NumberFormatException invalid) {
            return DEFAULT_MINUTES;
        }
    }

    public static int indexOf(int minutes) {
        for(int i=0;i<MINUTES.length;i++)if(MINUTES[i]==minutes)return i;
        return indexOf(DEFAULT_MINUTES);
    }
}
