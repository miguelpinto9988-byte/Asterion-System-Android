package pe.asterionsystem.app;

/** Permanent local-time schedule for the two approved Asterion splash illustrations. */
public final class SplashTimeWindow {
    private SplashTimeWindow() { }

    /** 00:00-11:59 uses daytime; 12:00-23:59 uses the night illustration. */
    public static boolean isMorning(int hourOfDay) {
        if (hourOfDay < 0 || hourOfDay > 23) {
            throw new IllegalArgumentException("Hour must be in the local 0-23 range");
        }
        return hourOfDay < 12;
    }
}
