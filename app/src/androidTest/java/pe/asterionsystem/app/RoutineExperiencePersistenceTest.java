package pe.asterionsystem.app;

import static org.junit.Assert.*;
import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.time.LocalDate;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;

/**
 * Android emulator tests executed in separate processes.
 * ONLY in a disposable CI emulator: wipe this test's private XP preferences.
 */
@RunWith(AndroidJUnit4.class)
public final class RoutineExperiencePersistenceTest {
    private Context context() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }
    private List<ExerciseVariant> items() {
        return List.of(new ExerciseVariant("test_routine_v01","test_routine",
                "Movimiento sentado",1,"sin_equipo","controlado",false));
    }

    @Test public void seedCompletedRoutineOnce() {
        Context ctx=context();
        ctx.getSharedPreferences("asterion_routine_experience_v1",Context.MODE_PRIVATE)
                .edit().clear().commit();
        LocalDate day=LocalDate.now();
        assertEquals(0,RoutineExperienceStore.total(ctx));
        assertFalse(RoutineExperienceStore.recorded(ctx,day));
        assertEquals(0,RoutineExperienceStore.awardOnce(ctx,day.minusDays(1)));
        assertEquals(10,RoutineExperienceStore.awardOnce(ctx,day));
        assertEquals(0,RoutineExperienceStore.awardOnce(ctx,day));
        assertEquals(10,RoutineExperienceStore.total(ctx));
        String token=RoutineSessionStore.token("custom",day,items());
        RoutineSessionStore.save(ctx,token,1,1,0);
        assertEquals(1,RoutineSessionStore.read(ctx,token,1).index);
    }

    @Test public void verifyAfterProcessRestart() {
        Context ctx=context();
        LocalDate today=LocalDate.now();
        assertTrue("Completion should survive process restart",
                RoutineExperienceStore.recorded(ctx,today));
        assertEquals(10,RoutineExperienceStore.total(ctx));
        assertEquals("Replaying completion never grants extra XP",
                0,RoutineExperienceStore.awardOnce(ctx,today));
        String token=RoutineSessionStore.token("custom",today,items());
        assertEquals("Guided progress should survive process restart",
                1,RoutineSessionStore.read(ctx,token,1).index);
        RoutineSessionStore.clear(ctx,token);
        assertEquals(0,RoutineSessionStore.read(ctx,token,1).index);
    }
}
