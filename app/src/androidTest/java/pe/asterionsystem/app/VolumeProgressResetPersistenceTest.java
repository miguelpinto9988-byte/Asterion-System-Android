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
import pe.asterionsystem.core.engine.progression.VolumeProgressionEngine;
import pe.asterionsystem.core.engine.progression.VolumeResetPolicy;
import pe.asterionsystem.core.engine.progression.ExerciseVolumeProfileCatalog;

/**
 * On an ephemeral CI emulator, verify that checkpoint selection is persisted,
 * old volume stays in history, and numeric references do not inherit it.
 */
@RunWith(AndroidJUnit4.class)
public final class VolumeProgressResetPersistenceTest {
    private Context context() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    private ExerciseVariant variant(String id,int level) {
        return new ExerciseVariant(id,"sentadilla_bilateral",
                "Variante de sentadilla",level,"sin_equipo","controlada",false);
    }

    @Test public void checkpointReintroducingOldVariantResetsItsNumericHistory() {
        Context ctx=context();
        assertTrue(ctx.getSharedPreferences("asterion_volume_progress_v1",
                Context.MODE_PRIVATE).edit().clear().commit());
        LocalDate first=LocalDate.now();
        ExerciseVariant old=variant("sentadilla_bilateral_v01",1);
        ExerciseVariant newer=variant("sentadilla_bilateral_v02",2);
        VolumeProgressStore.recordCompleted(ctx,
                new VolumeProgressionEngine.Observation(
                        first,2,old.id(),old.typeId(),old.difficulty(),
                        2,8,2,true,false));
        assertEquals(1,VolumeProgressStore.previousForCategory(
                ctx,2,first.plusDays(1)).size());

        assertTrue(VolumeProgressStore.observeCheckpointSelection(
                ctx,2,first,first,0,List.of(old))
                .restartVariantIds.isEmpty());
        VolumeResetPolicy.Decision day7=VolumeProgressStore.observeCheckpointSelection(
                ctx,2,first.plusDays(7),first,7,List.of(newer));
        assertTrue(day7.restarted(newer.id()));
        assertTrue(VolumeProgressStore.restartedOn(
                ctx,2,newer.id(),first.plusDays(7)));

        // At day 14 an old variant returns. It must NOT inherit the earlier 2x8.
        VolumeResetPolicy.Decision day14=VolumeProgressStore.observeCheckpointSelection(
                ctx,2,first.plusDays(14),first,14,List.of(old));
        assertTrue(day14.restarted(old.id()));
        assertEquals(1,VolumeProgressStore.previousForCategory(
                ctx,2,first.plusDays(15)).size());
        List<VolumeProgressionEngine.Observation> filtered=
                VolumeProgressStore.progressionHistoryForCategory(
                        ctx,2,first.plusDays(15));
        assertTrue(filtered.isEmpty());
        assertEquals(VolumeProgressionEngine.Status.NO_HISTORY,
                VolumeProgressionEngine.review(old,2,first.plusDays(15),
                        filtered,false).status);
        assertTrue(VolumeProgressStore.observeCheckpointSelection(
                ctx,2,first.plusDays(14),first,14,List.of(old))
                .restartVariantIds.isEmpty());
    }

    @Test public void unchangedExerciseAtMilestonesKeepsOwnBaseline() {
        Context ctx=context();
        assertTrue(ctx.getSharedPreferences("asterion_volume_progress_v1",
                Context.MODE_PRIVATE).edit().clear().commit());
        LocalDate first=LocalDate.now();
        ExerciseVariant same=variant("elevacion_puntas_v01",1);
        VolumeProgressStore.recordCompleted(ctx,
                new VolumeProgressionEngine.Observation(
                        first,0,same.id(),same.typeId(),same.difficulty(),
                        2,8,4,true,false));
        VolumeProgressStore.observeCheckpointSelection(
                ctx,0,first,first,0,List.of(same));
        for(int checkpoint:new int[]{7,14,21}) {
            assertTrue(VolumeProgressStore.observeCheckpointSelection(
                    ctx,0,first.plusDays(checkpoint),first,
                    checkpoint,List.of(same)).restartVariantIds.isEmpty());
        }
        assertEquals(1,VolumeProgressStore.progressionHistoryForCategory(
                ctx,0,first.plusDays(21)).size());
        assertTrue(VolumeProgressStore.previousForCategory(
                ctx,2,first.plusDays(21)).isEmpty());
    }

    @Test public void timedSecondsPersistAndLegacyRepetitionsRemainDistinct() {
        Context ctx=context();
        assertTrue(ctx.getSharedPreferences("asterion_volume_progress_v1",
                Context.MODE_PRIVATE).edit().clear().commit());
        LocalDate now=LocalDate.now();
        ExerciseVariant timed=new ExerciseVariant("marcha_v02","marcha",
                "Caminata cómoda en terreno plano",1,"sin_equipo","aerobico",false);
        ExerciseVolumeProfileCatalog.Profile profile=
                new ExerciseVolumeProfileCatalog.Profile(
                        timed.id(),ExerciseVolumeProfileCatalog.Unit.SECONDS,
                        1,60,10,180,3,true);
        VolumeProgressStore.recordCompleted(ctx,
                new VolumeProgressionEngine.Observation(
                        now,4,timed.id(),timed.typeId(),1,1,60,1,true,false,
                        ExerciseVolumeProfileCatalog.Unit.SECONDS));
        List<VolumeProgressionEngine.Observation> saved=
                VolumeProgressStore.previousForCategory(ctx,4,now.plusDays(1));
        assertEquals(1,saved.size());
        assertEquals(ExerciseVolumeProfileCatalog.Unit.SECONDS,saved.get(0).unit);
        assertEquals(60,saved.get(0).quantity);
        assertEquals(75,VolumeProgressionEngine.review(
                timed,4,now.plusDays(1),saved,false,profile).offeredQuantity);

        // An older ten-field record has no unit: it must decode as repetitions.
        String priorDate=now.minusDays(1).toString();
        String legacyKey="vol_"+priorDate+"_"+timed.id();
        String legacyValue=priorDate+"|4|"+timed.id()+"|"+timed.typeId()
                +"|1|1|8|2|1|0";
        assertTrue(ctx.getSharedPreferences("asterion_volume_progress_v1",
                Context.MODE_PRIVATE).edit().putString(
                        legacyKey,legacyValue).commit());
        List<VolumeProgressionEngine.Observation> mixed=
                VolumeProgressStore.previousForCategory(ctx,4,now.plusDays(1));
        assertEquals(2,mixed.size());
        for(VolumeProgressionEngine.Observation entry:mixed) {
            if(entry.day.equals(now.minusDays(1))) {
                assertEquals(ExerciseVolumeProfileCatalog.Unit.REPETITIONS,entry.unit);
            }
        }
        // Latest seconds entry stays the only comparable volume evidence.
        assertEquals(75,VolumeProgressionEngine.review(
                timed,4,now.plusDays(1),mixed,false,profile).offeredQuantity);
    }

}
