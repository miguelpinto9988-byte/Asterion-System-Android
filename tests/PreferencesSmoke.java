import pe.asterionsystem.core.settings.*;
public final class PreferencesSmoke {
  private static int checks;
  private static void ok(boolean condition) {checks++; if (!condition) throw new AssertionError("failed check " + checks);}
  private static final class Memory implements PreferencesRepository {
    AppPreferences current=AppPreferences.defaults(); int saves=0, resets=0;
    public AppPreferences read(){return current;}
    public void save(AppPreferences next){current=next;saves++;}
    public void reset(){current=AppPreferences.defaults();resets++;}
  }
  public static void main(String[] args) {
    Memory storage=new Memory();PreferencesService service=new PreferencesService(storage);
    ok(service.current().startSection()==AppPreferences.StartSection.HOME);
    ok(AppPreferences.TextSize.fromStored("unknown")==AppPreferences.TextSize.NORMAL);
    ok(AppPreferences.ListDensity.fromStored("unknown")==AppPreferences.ListDensity.NORMAL);
    ok(AppPreferences.StartSection.fromStored("detail")==AppPreferences.StartSection.HOME);
    ok(AppPreferences.StartSection.fromStored("plan")==AppPreferences.StartSection.PLAN);
    ok(!service.update(AppPreferences.defaults()));
    ok(storage.saves==0);
    ok(service.update(service.current().withTextSize(AppPreferences.TextSize.LARGE)));
    ok(service.current().textSize()==AppPreferences.TextSize.LARGE);
    ok(!service.update(service.current()));
    ok(storage.saves==1);
    ok(service.update(service.current().withListDensity(AppPreferences.ListDensity.COMPACT)));
    ok(service.update(service.current().withStartSection(AppPreferences.StartSection.CATALOG)));
    ok(service.current().startSection()==AppPreferences.StartSection.CATALOG);
    ok(storage.saves==3);
    service.reset();ok(storage.resets==1);
    ok(service.current().equals(AppPreferences.defaults()));
    ok(service.current().hashCode()==AppPreferences.defaults().hashCode());
    System.out.println("PASS Java 17 core settings: "+checks+" checks");
  }
}
