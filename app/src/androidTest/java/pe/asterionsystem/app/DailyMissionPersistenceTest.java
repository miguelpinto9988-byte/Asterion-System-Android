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
import pe.asterionsystem.core.engine.progression.WeeklyCategoryChallengePolicy;

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
        context.getSharedPreferences("asterion_mission_level_progress_v1",
                Context.MODE_PRIVATE).edit().clear().commit();
        assertEquals(0,MissionLevelProgressStore.observe(
                context,"intermedio",0,DAY,null,false).phase);
        ExerciseCatalog library=catalog();
        assertEquals(817,library.variants().size());
        MissionPlanner.Profile profile=profile("bienestar");
        List<ExerciseVariant> variants=new MissionPlanner(library).daily(
                profile,DAY).candidates();
        assertEquals(3,variants.size());
        DailyMissionStore.write(context,DAY,profile,library,variants);
        // Seed a real check-off from the preceding day; it is not a proposal.
        MissionTrackingStore.setDailyCompleted(
                context,DAY.minusDays(1),variants.get(0).id(),true);
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
        assertEquals("Experience phase should survive process restart",0,
                MissionLevelProgressStore.observe(
                        context,"intermedio",0,DAY,null,false).phase);
        assertTrue(MissionTrackingStore.recentCompletedForCategory(
                context,library,0,DAY).contains(expected.get(0)));
        assertTrue("Completed exercise from upper body must not appear in legs",
                MissionTrackingStore.recentCompletedForCategory(
                        context,library,2,DAY).isEmpty());
        MissionTrackingStore.DiversitySummary observed=
                MissionTrackingStore.recentDiversityForCategory(
                        context,library,0,DAY);
        assertEquals(1,observed.activeDays);
        assertEquals(1,observed.uniqueVariants);
        WeeklyCategoryChallengePolicy.Review easilyCompleted=
                WeeklyCategoryChallengePolicy.evaluate(0,DAY,DAY.plusDays(7),
                        List.of(
                                new WeeklyCategoryChallengePolicy.Observation(
                                        DAY.plusDays(2),0,2,true),
                                new WeeklyCategoryChallengePolicy.Observation(
                                        DAY.plusDays(4),0,3,true)));
        assertEquals(WeeklyCategoryChallengePolicy.Status.OFFER_EQUIVALENT_VARIETY,
                easilyCompleted.status);
        assertEquals("First week must stay mostly basic",0,
                MissionLevelProgressStore.observe(
                        context,"intermedio",0,DAY.plusDays(6),
                        easilyCompleted,false).phase);
        assertEquals("Interrupted training cannot progress",0,
                MissionLevelProgressStore.observe(
                        context,"intermedio",0,DAY.plusDays(7),
                        easilyCompleted,true).phase);
        assertEquals("Another category cannot inherit an easy review",0,
                MissionLevelProgressStore.observe(
                        context,"intermedio",1,DAY.plusDays(7),
                        easilyCompleted,false).phase);
        assertEquals(1,MissionLevelProgressStore.observe(
                context,"intermedio",0,DAY.plusDays(8),
                easilyCompleted,false).phase);
        assertEquals("Repeated app opens must not promote again",1,
                MissionLevelProgressStore.observe(
                        context,"intermedio",0,DAY.plusDays(8),
                        easilyCompleted,false).phase);
        WeeklyCategoryChallengePolicy.Review nextEasyWeek=
                WeeklyCategoryChallengePolicy.evaluate(0,DAY,DAY.plusDays(15),
                        List.of(
                                new WeeklyCategoryChallengePolicy.Observation(
                                        DAY.plusDays(10),0,2,true),
                                new WeeklyCategoryChallengePolicy.Observation(
                                        DAY.plusDays(12),0,2,true)));
        assertEquals(2,MissionLevelProgressStore.observe(
                context,"intermedio",0,DAY.plusDays(15),
                nextEasyWeek,false).phase);
        assertEquals(0,MissionLevelProgressStore.observe(
                context,"avanzado",0,DAY.plusDays(16),
                nextEasyWeek,false).phase); // changing experience resets phase
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
    @Test public void seedMonarchPreviewAndAccept() throws Exception {
        Context ctx=context();
        // The runner uses an ephemeral, clean emulator. Never erase a user's
        // personal Monarca mission from a production device.
        MonarchSessionStore.abandon(ctx);
        ExerciseCatalog library=catalog();
        MissionPlanner planner=new MissionPlanner(library);
        MissionPlanner.Profile configured=profile("bienestar");
        MonarchSessionStore.Snapshot preview=MonarchSessionStore.current(
                ctx,DAY,planner,configured,library);
        assertEquals(MonarchSessionStore.PREVIEW,preview.status);
        assertTrue("Five safe candidates are available",preview.canAccept());
        assertEquals(preview.exercises.size(),5);
        List<String> initialIds=new ArrayList<>();
        for(ExerciseVariant item:preview.exercises)initialIds.add(item.id());
        MonarchSessionStore.Snapshot reopened=MonarchSessionStore.current(
                ctx,DAY,planner,configured,library);
        List<String> reread=new ArrayList<>();
        for(ExerciseVariant item:reopened.exercises)reread.add(item.id());
        assertEquals(initialIds,reread);
        assertTrue(MonarchSessionStore.accept(ctx,DAY,preview,configured));
        assertEquals(MonarchSessionStore.ACTIVE,MonarchSessionStore.current(
                ctx,DAY,planner,configured,library).status);
        assertFalse("Once active, cannot accept again",
                MonarchSessionStore.accept(ctx,DAY,preview,configured));
    }

    @Test public void verifyMonarchAfterRestartAndProfileReview() throws Exception {
        Context ctx=context();
        ExerciseCatalog library=catalog();
        MissionPlanner planner=new MissionPlanner(library);
        MissionPlanner.Profile original=profile("bienestar");
        MonarchSessionStore.Snapshot active=MonarchSessionStore.current(
                ctx,DAY.plusDays(2),planner,original,library);
        assertEquals("Acceptance and IDs must survive a process restart",
                MonarchSessionStore.ACTIVE,active.status);
        assertEquals(5,active.exercises.size());
        List<String> initial=new ArrayList<>();
        for(ExerciseVariant item:active.exercises)initial.add(item.id());
        MonarchSessionStore.Snapshot repeated=MonarchSessionStore.current(
                ctx,DAY.plusDays(3),planner,original,library);
        List<String> repeatedIds=new ArrayList<>();
        for(ExerciseVariant item:repeated.exercises)repeatedIds.add(item.id());
        assertEquals(initial,repeatedIds);
        assertEquals(DAY.plusDays(5),active.activeUntilExclusive());

        MissionPlanner.Profile changed=profile("fuerza");
        MonarchSessionStore.Snapshot paused=MonarchSessionStore.current(
                ctx,DAY.plusDays(3),planner,changed,library);
        assertEquals("Do not silently overwrite an accepted cycle",
                MonarchSessionStore.REVIEW_REQUIRED,paused.status);
        assertTrue("Pausing must disable the old suggested activities",
                paused.exercises.isEmpty());
        assertFalse("Changing profiles does not reauthorise the old cycle",
                MonarchSessionStore.accept(ctx,DAY.plusDays(3),
                        repeated,changed));
        assertEquals(MonarchSessionStore.REVIEW_REQUIRED,
                MonarchSessionStore.current(ctx,DAY.plusDays(3),
                        planner,changed,library).status);

        MonarchSessionStore.restartAfterReview(ctx);
        MonarchSessionStore.Snapshot next=MonarchSessionStore.current(
                ctx,DAY.plusDays(3),planner,changed,library);
        assertEquals("New profile must require a fresh acceptance",
                MonarchSessionStore.PREVIEW,next.status);
        assertTrue(next.canAccept());
        assertTrue(MonarchSessionStore.accept(ctx,DAY.plusDays(3),next,changed));
        assertEquals(MonarchSessionStore.ACTIVE,MonarchSessionStore.current(
                ctx,DAY.plusDays(4),planner,changed,library).status);

        // A cycle expires five days from ACCEPTANCE, not preview creation.
        MonarchSessionStore.Snapshot nextCycle=MonarchSessionStore.current(
                ctx,DAY.plusDays(8),planner,changed,library);
        assertEquals(MonarchSessionStore.PREVIEW,nextCycle.status);
        assertEquals(DAY.plusDays(8),nextCycle.previewStart);
        MonarchSessionStore.decline(ctx);
        assertEquals(MonarchSessionStore.DECLINED,
                MonarchSessionStore.current(ctx,DAY.plusDays(8),
                        planner,changed,library).status);
    }

}
