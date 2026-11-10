package pe.asterionsystem.app;

import static org.junit.Assert.*;
import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalogLoader;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;
import pe.asterionsystem.core.engine.missions.MissionPlanner;

/**
 * Real Android SharedPreferences persistence smoke. CI runs seedPin and
 * verifyAfterRestartAndDayChange as two SEPARATE instrument invocations,
 * with adb force-stop between them. The clock itself is not changed:
 * LocalDate is injected as a test input to simulate the next calendar day.
 * This test is only for a clean ephemeral emulator, never a personal device.
 */
@RunWith(AndroidJUnit4.class)
public final class DailyMissionPersistenceTest {
    private static final LocalDate DAY=LocalDate.of(2026,11,10);
    private static final String ASSETS="asterion_exercises/v1/";

    private Context context() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    private ExerciseCatalog catalog() throws Exception {
        Context context=context();
        try(InputStreamReader f=new InputStreamReader(
                     context.getAssets().open(ASSETS+"families.tsv"),StandardCharsets.UTF_8);
            InputStreamReader t=new InputStreamReader(
                     context.getAssets().open(ASSETS+"types.tsv"),StandardCharsets.UTF_8);
            InputStreamReader v=new InputStreamReader(
                     context.getAssets().open(ASSETS+"variants.tsv"),StandardCharsets.UTF_8);
            InputStreamReader i=new InputStreamReader(
                     context.getAssets().open(ASSETS+"intensity.tsv"),StandardCharsets.UTF_8)) {
            return ExerciseCatalogLoader.load(f,t,v,i);
        }
    }

    private MissionPlanner.Profile profile(String goal) {
        return new MissionPlanner.Profile("intermedio",goal,
                Arrays.asList(0,0,0,0,0,0,0),
                Set.of("sin_equipo","pared"),3,"moderada","");
    }

    private List<String> ids(MissionPlanner.Proposal proposal) {
        List<String> ids=new ArrayList<>();
        for(ExerciseVariant item:proposal.candidates())ids.add(item.id());
        return ids;
    }

    @Test public void seedPin() throws Exception {
        Context context=context();
        DailyMissionStore.clear(context); // fresh CI emulator only
        ExerciseCatalog library=catalog();
        assertEquals(817,library.variants().size());
        MissionPlanner.Profile profile=profile("bienestar");
        List<ExerciseVariant> variants=new MissionPlanner(library).daily(
                profile,DAY).candidates();
        assertEquals(3,variants.size());
        DailyMissionStore.write(context,DAY,profile,library,variants);
        assertEquals(ids(new MissionPlanner(library).daily(profile,DAY)),
                DailyMissionStore.read(context,DAY,profile,library));
    }

    @Test public void verifyAfterRestartAndDayChange() throws Exception {
        Context context=context();
        ExerciseCatalog library=catalog();
        MissionPlanner.Profile profile=profile("bienestar");
        List<String> expected=ids(new MissionPlanner(library).daily(profile,DAY));
        // A separate instrumentation run now reads persisted data.
        assertFalse(expected.isEmpty());
        assertEquals("Pin should survive process shutdown/restart",expected,
                DailyMissionStore.read(context,DAY,profile,library));
        assertEquals("Next local day invalidates yesterday's pin",List.of(),
                DailyMissionStore.read(context,DAY.plusDays(1),profile,library));
        List<ExerciseVariant> tomorrow=new MissionPlanner(library).daily(
                profile,DAY.plusDays(1)).candidates();
        DailyMissionStore.write(context,DAY.plusDays(1),profile,library,tomorrow);
        assertEquals(ids(new MissionPlanner(library).daily(profile,DAY.plusDays(1))),
                DailyMissionStore.read(context,DAY.plusDays(1),profile,library));
        assertTrue("Profile edits invalidate old IDs",
                DailyMissionStore.read(context,DAY.plusDays(1),
                        profile("fuerza"),library).isEmpty());
    }
}
