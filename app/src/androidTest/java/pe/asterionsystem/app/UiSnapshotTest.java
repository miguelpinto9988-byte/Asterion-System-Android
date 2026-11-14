package pe.asterionsystem.app;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import pe.asterionsystem.app.data.AssetExerciseCatalogRepository;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseCategoryMapper;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;
import pe.asterionsystem.core.engine.missions.RoutineGenerator;

/** Captures the actual rendered Android windows on a disposable CI emulator. */
@RunWith(AndroidJUnit4.class)
public final class UiSnapshotTest {
    private final Instrumentation instrument = InstrumentationRegistry.getInstrumentation();
    private final Context context = instrument.getTargetContext();
    private final File output = new File(context.getExternalFilesDir(null), "ui-screens");

    @Test public void captureScreens() throws Exception {
        assertTrue(output.isDirectory() || output.mkdirs());
        context.getSharedPreferences("asterion_profile_local_v1", Context.MODE_PRIVATE)
                .edit().clear().commit();
        ProfileRegistrationDraft.clear(context);

        start(SplashActivity.class, null, null, 100);
        capture("01_inicio_splash");
        start(ProfileGateActivity.class, null, null, 350);
        capture("02_acceso_perfil");

        JSONObject draft = new JSONObject();
        draft.put("name", "Cazador demo");
        draft.put("age", "28");
        draft.put("gender", "unspecified");
        draft.put("theme", "light");
        draft.put("experience", "intermedio");
        draft.put("goal", "bienestar");
        draft.put("intensity", "moderada");
        draft.put("mode", "misiones");
        draft.put("missions", "3");
        draft.put("bodyweight", true);
        draft.put("wall", true);
        draft.put("mat", true);
        String[] week = {"0", "1", "2", "3", "4", "5", "5"};
        week[LocalDate.now().getDayOfWeek().getValue() - 1] = "0";
        draft.put("week", String.join(",", week));
        ProfileRegistrationDraft.save(context, draft);

        Activity wizard = start(ProfileCreateActivity.class, null, null, 250);
        capture("03_registro_identidad");
        click(wizard, R.id.profile_create_submit);
        capture("04_registro_entrenamiento");
        click(wizard, R.id.profile_create_submit);
        capture("05_registro_personalizacion");
        click(wizard, R.id.profile_create_submit);
        capture("06_registro_confirmacion");
        CheckBox confirm = findType(wizard.getWindow().getDecorView(), CheckBox.class);
        assertNotNull("Falta confirmación del perfil", confirm);
        instrument.runOnMainSync(confirm::performClick);
        click(wizard, R.id.profile_create_submit);
        assertTrue("El perfil no se guardó", ProfileStore.hasProfile(context));
        capture("07_registro_activado");

        start(MainActivity.class, "asterion_open_section", "home", 450);
        capture("08_inicio");
        start(MainActivity.class, "asterion_open_section", "secondary", 350);
        capture("09_menu_secundario");
        Activity main = start(MainActivity.class, "asterion_open_section", "catalog", 1200);
        capture("10_catalogo_local");
        ListView results = main.findViewById(R.id.catalog_results);
        assertNotNull(results);
        for (int tries = 0; tries < 12 && results.getCount() == 0; tries++) pause(500);
        assertTrue("El catálogo local no cargó", results.getCount() > 0);
        instrument.runOnMainSync(() -> results.performItemClick(
                results.getChildAt(0), 0, results.getAdapter().getItemId(0)));
        pause(350);
        capture("11_ficha_catalogo_local");

        main = start(MainActivity.class, "asterion_open_section", "plan", 600);
        capture("12_plan_semanal");
        click(main, R.id.plan_create);
        capture("13_formulario_plan");
        main = start(MainActivity.class, "asterion_open_section", "progress", 600);
        capture("14_progreso");
        click(main, R.id.progress_show_summary);
        capture("15_resumen_progreso");
        start(MainActivity.class, "asterion_open_section", "settings", 350);
        capture("16_ajustes");
        start(MainActivity.class, "asterion_open_section", "local_status", 900);
        capture("17_estado_local");

        start(ProfileDetailsActivity.class, null, null, 500);
        capture("18_detalle_perfil");
        start(CalendarActivity.class, null, null, 800);
        capture("19_calendario");
        for (String section : new String[]{"daily", "monarch", "library", "progress"}) {
            start(MissionsActivity.class, "section", section, 1300);
            capture("20_misiones_" + section);
        }

        Activity routines = start(RoutinesActivity.class, null, null, 1300);
        capture("21_rutinas");
        Button review = findButton(routines.getWindow().getDecorView(), "Revisar rutina 1");
        if (review != null) {
            instrument.runOnMainSync(review::performClick);
            capture("22_vista_previa_rutina");
        }
        captureRoutineStep();

        Activity imported = start(ImportedExerciseCatalogActivity.class, null, null, 2000);
        capture("24_catalogo_gifs");
        Button first = findButton(imported.getWindow().getDecorView(), "Asterion · ");
        assertNotNull("Falta una variante del catálogo importado", first);
        instrument.runOnMainSync(first::performClick);
        pause(1200);
        capture("25_ficha_con_gif");
    }

    private void captureRoutineStep() throws Exception {
        ExerciseCatalog catalog = new AssetExerciseCatalogRepository(context).load();
        RoutineGenerator.Request request = new RoutineGenerator.Request(
                ExerciseCategoryMapper.weeklyCode(0), 20, "basico", LocalDate.now(), 0);
        List<RoutineGenerator.Offer> offers = new RoutineGenerator(catalog).generate(
                request, MissionProfileAdapter.fromRegistration(ProfileStore.getRegistration(context)));
        assertFalse("No hay rutinas para el perfil de ejemplo", offers.isEmpty());
        RoutineGenerator.Offer offer = offers.get(0);
        ArrayList<String> ids = new ArrayList<>();
        for (ExerciseVariant variant : offer.exercises()) ids.add(variant.id());
        Intent intent = new Intent(context, RoutineStepActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .putExtra("kind", "custom")
                .putExtra("date", LocalDate.now().toString())
                .putExtra("category", 0).putExtra("minutes", 20)
                .putExtra("level", "basico").putExtra("variation", 0)
                .putExtra("offer_id", offer.id()).putStringArrayListExtra("variants", ids);
        instrument.startActivitySync(intent);
        instrument.waitForIdleSync();
        pause(1400);
        capture("23_sesion_guiada");
    }

    private Activity start(Class<? extends Activity> type, String key, String value,
                           long delayMillis) throws Exception {
        Intent intent = new Intent(context, type)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (key != null) intent.putExtra(key, value);
        Activity activity = instrument.startActivitySync(intent);
        instrument.waitForIdleSync();
        pause(delayMillis);
        return activity;
    }

    private void click(Activity activity, int id) throws Exception {
        View target = activity.findViewById(id);
        assertNotNull("Falta botón " + id, target);
        instrument.runOnMainSync(target::performClick);
        instrument.waitForIdleSync();
        pause(250);
    }

    private void capture(String name) throws Exception {
        Bitmap screenshot = null;
        for (int tries = 0; tries < 4 && screenshot == null; tries++) {
            screenshot = instrument.getUiAutomation().takeScreenshot();
            if (screenshot == null) pause(250);
        }
        assertNotNull("No se pudo capturar " + name, screenshot);
        File file = new File(output, name + ".png");
        try (FileOutputStream stream = new FileOutputStream(file)) {
            assertTrue(screenshot.compress(Bitmap.CompressFormat.PNG, 100, stream));
        }
        screenshot.recycle();
    }

    private static <T extends View> T findType(View node, Class<T> type) {
        if (type.isInstance(node)) return type.cast(node);
        if (node instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) node;
            for (int i = 0; i < group.getChildCount(); i++) {
                T child = findType(group.getChildAt(i), type);
                if (child != null) return child;
            }
        }
        return null;
    }

    private static Button findButton(View node, String prefix) {
        if (node instanceof Button && ((Button) node).getText().toString().startsWith(prefix))
            return (Button) node;
        if (node instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) node;
            for (int i = 0; i < group.getChildCount(); i++) {
                Button child = findButton(group.getChildAt(i), prefix);
                if (child != null) return child;
            }
        }
        return null;
    }

    private static void pause(long millis) throws InterruptedException { Thread.sleep(millis); }
}
