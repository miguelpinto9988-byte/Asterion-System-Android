import pe.asterionsystem.core.offline.*;

/** Standalone JVM verification: no Android SDK or Gradle required. */
public final class LocalStorageSmoke {
    private static int checks;
    private static void yes(boolean value) {
        checks++;
        if (!value) throw new AssertionError("Fallo en comprobación " + checks);
    }
    public static void main(String[] args) throws Exception {
        LocalStorageReport healthy = new LocalStorageReport(true, true, 4, 2, 1);
        yes(healthy.state() == LocalStorageReport.State.HEALTHY);
        yes(healthy.quickCheckPassed());
        yes(healthy.foreignKeysPassed());
        yes(healthy.catalogCount() == 4);
        yes(healthy.scheduledCount() == 2);
        yes(healthy.outcomesCount() == 1);
        yes(new LocalStorageReport(false,true,4,2,1).state()
                == LocalStorageReport.State.ATTENTION);
        yes(new LocalStorageReport(true,false,4,2,1).state()
                == LocalStorageReport.State.ATTENTION);
        LocalStorageReport impossible = new LocalStorageReport(true,true,4,1,2);
        yes(impossible.state() == LocalStorageReport.State.ATTENTION);
        yes(!impossible.foreignKeysPassed());
        try {
            new LocalStorageReport(true,true,-1,0,0);
            throw new AssertionError("El contador negativo debe rechazarse");
        } catch (IllegalArgumentException expected) { checks++; }
        LocalStorageService service = new LocalStorageService(() -> healthy);
        yes(service.inspect() == healthy);
        try (BackgroundLocalStorageChecks work = new BackgroundLocalStorageChecks(service)) {
            yes(work.inspect().get() == healthy);
        }
        System.out.println("PASS local storage checks: " + checks);
    }
}
