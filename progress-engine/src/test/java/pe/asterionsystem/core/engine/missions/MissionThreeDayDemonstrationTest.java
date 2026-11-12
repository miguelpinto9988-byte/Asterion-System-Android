package pe.asterionsystem.core.engine.missions;

import static org.junit.Assert.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalogLoader;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;
import pe.asterionsystem.core.engine.progression.CategoryEffortPolicy;
import pe.asterionsystem.core.engine.progression.WeeklyCategoryChallengePolicy;

/**
 * A read-only, reproducible demo. Uses the ACTUAL 1,257-row catalog and
 * MissionPlanner; never writes device preferences or exercise outcomes.
 * Calendar deliberately repeats the SAME category for three hypothetical
 * dates to isolate feedback. Not a prescription to exercise daily.
 */
public final class MissionThreeDayDemonstrationTest {
    private static final int CATEGORY=CategoryEffortPolicy.LOWER;
    private static final LocalDate FIRST=LocalDate.of(2026,11,12);
    private ExerciseCatalog catalog;
    private MissionPlanner planner;
    private MissionPlanner.Profile profile;

    @Before public void load() throws Exception {
        String p="/asterion_exercises/v1/";
        try(InputStreamReader f=resource(p+"families.tsv");
            InputStreamReader t=resource(p+"types.tsv");
            InputStreamReader v=resource(p+"variants.tsv");
            InputStreamReader i=resource(p+"intensity.tsv")) {
            catalog=ExerciseCatalogLoader.load(f,t,v,i);
        }
        assertEquals(1257,catalog.variants().size());
        planner=new MissionPlanner(catalog);
        profile=new MissionPlanner.Profile("principiante","bienestar",
            Arrays.asList(2,2,2,2,2,2,2),
            new HashSet<>(Arrays.asList("sin_equipo","silla","pared","colchoneta")),
            3,"moderada","misiones|rondas|60|30||");
    }

    private InputStreamReader resource(String path){
        return new InputStreamReader(
            getClass().getResourceAsStream(path),StandardCharsets.UTF_8);
    }

    private static String jsonString(String text) {
        StringBuilder b=new StringBuilder("\"");
        for(char c:text.toCharArray()){
            switch(c){
                case '"': b.append("\\\"");break;
                case '\\':b.append("\\\\");break;
                case '\n':b.append("\\n");break;
                case '\r':b.append("\\r");break;
                default:b.append(c);
            }
        }
        return b.append('"').toString();
    }

    private static List<String> ids(MissionPlanner.Proposal p){
        ArrayList<String> out=new ArrayList<>();
        for(ExerciseVariant v:p.candidates())out.add(v.id());
        return out;
    }

    private static String render(String scenario,int day,int previousEffort,
                                 int reportedAfter,CategoryEffortPolicy.Decision decision,
                                 MissionPlanner.Proposal p) {
        StringBuilder b=new StringBuilder("{\"type\":\"day\"");
        b.append(",\"scenario\":").append(jsonString(scenario));
        b.append(",\"day\":").append(day);
        b.append(",\"date\":").append(jsonString(p.start().toString()));
        b.append(",\"category\":").append(jsonString("Tren inferior"));
        b.append(",\"rpe_before\":").append(previousEffort);
        b.append(",\"rpe_after\":").append(reportedAfter);
        b.append(",\"guidance\":").append(jsonString(decision.guidance.name()));
        b.append(",\"requested_max\":").append(decision.suggestedActivities);
        b.append(",\"prefer_simple\":").append(decision.preferSimplerTechnique);
        b.append(",\"explanation\":").append(jsonString(decision.explanation));
        b.append(",\"choices\":[");
        for(int i=0;i<p.candidates().size();i++){
            ExerciseVariant v=p.candidates().get(i);
            if(i>0)b.append(',');
            b.append("{\"id\":").append(jsonString(v.id()));
            b.append(",\"name\":").append(jsonString(v.name()));
            b.append(",\"level\":").append(v.difficulty());
            b.append(",\"equipment\":[");
            for(int j=0;j<v.equipment().size();j++){
                if(j>0)b.append(',');
                b.append(jsonString(v.equipment().get(j)));
            }
            b.append("]}");
        }
        return b.append("]}").toString();
    }

    private static void output(String json){
        System.out.println("ASTERION_DEMO_JSON:"+json);
    }

    @Test public void reproducibleRandomDaysWithoutUserFeedback(){
        CategoryEffortPolicy.Decision none=CategoryEffortPolicy.evaluate(CATEGORY,0,3);
        List<String> firstIds=null;
        for(int day=1;day<=3;day++){
            LocalDate date=FIRST.plusDays(day-1);
            MissionPlanner.Proposal proposal=planner.daily(profile,date);
            assertFalse("Real catalog must contain suggestions",proposal.empty());
            assertEquals(ids(proposal),ids(planner.daily(profile,date)));
            if(firstIds==null)firstIds=ids(proposal);
            output(render("aleatoria_sin_historial",day,0,0,none,proposal));
        }
        assertNotNull(firstIds);
    }

    /** Each simulated session is completed and rated. No real user records. */
    private void threeDays(String scenario,int day1Effort,int day2Effort){
        int[] after={day1Effort,day2Effort,0};
        int lastEffort=0;
        List<String> lastIds=Collections.emptyList();
        ArrayList<String> recentCompleted=new ArrayList<>();
        for(int day=1;day<=3;day++){
            LocalDate date=FIRST.plusDays(day-1);
            CategoryEffortPolicy.Decision decision=
                    CategoryEffortPolicy.evaluate(CATEGORY,lastEffort,profile.dailyLimit);
            MissionPlanner.Profile todayProfile=
                    decision.preferSimplerTechnique||
                    decision.suggestedActivities<profile.dailyLimit
                    ?profile.adapted(
                        decision.preferSimplerTechnique?"principiante":profile.experience,
                        Math.max(1,decision.suggestedActivities))
                    :profile;
            // The real UI disables equivalent rotation before day-eight review.
            MissionPlanner.Proposal proposal=planner.daily(
                    todayProfile,date,decision,lastIds,false,recentCompleted,0);
            assertEquals(proposal.candidates().size(),
                    planner.daily(todayProfile,date,decision,lastIds,false,
                            recentCompleted,0).candidates().size());
            assertTrue(proposal.candidates().size()<=decision.suggestedActivities);
            for(ExerciseVariant v:proposal.candidates()){
                assertTrue(v.eligibleForAutomaticSuggestion());
                assertTrue(profile.equipment.containsAll(v.equipment()));
                assertTrue(v.difficulty()<=2);
                if(decision.preferSimplerTechnique)assertTrue(v.difficulty()<=1);
            }
            output(render(scenario,day,lastEffort,after[day-1],decision,proposal));
            lastIds=ids(proposal);
            recentCompleted.addAll(lastIds);
            lastEffort=after[day-1];
        }
    }

    @Test public void actualThreeDayFeedbackScenarios(){
        threeDays("facil_2",2,2);
        threeDays("moderado_5",5,5);
        threeDays("dificil_8",8,8);
        threeDays("muy_dificil_9",9,9);
        threeDays("facil_luego_muy_dificil",2,9);
    }

    @Test public void genuineWeeklyGateAndTechnicalPhaseTargets(){
        List<WeeklyCategoryChallengePolicy.Observation> low=Arrays.asList(
            new WeeklyCategoryChallengePolicy.Observation(
                FIRST.plusDays(1),CATEGORY,2,true),
            new WeeklyCategoryChallengePolicy.Observation(
                FIRST.plusDays(2),CATEGORY,3,true));
        WeeklyCategoryChallengePolicy.Review early=
                WeeklyCategoryChallengePolicy.evaluate(
                        CATEGORY,FIRST,FIRST.plusDays(2),low);
        WeeklyCategoryChallengePolicy.Review weekly=
                WeeklyCategoryChallengePolicy.evaluate(
                        CATEGORY,FIRST,FIRST.plusDays(7),low);
        assertEquals(WeeklyCategoryChallengePolicy.Status.WAITING_FOR_CHECKPOINT,early.status);
        assertEquals(WeeklyCategoryChallengePolicy.Status.OFFER_EQUIVALENT_VARIETY,weekly.status);
        assertFalse(early.rotateEquivalentVariant());
        assertTrue(weekly.rotateEquivalentVariant());
        int n=5;
        int phase0=MissionDifficultyMixPolicy.higherTarget("intermedio",0,n);
        int phase1=MissionDifficultyMixPolicy.higherTarget("intermedio",1,n);
        int phase2=MissionDifficultyMixPolicy.higherTarget("intermedio",2,n);
        assertTrue(phase0<phase1&&phase1<phase2);
        output("{\"type\":\"weekly\",\"status_day3\":"+jsonString(early.status.name())
                +",\"status_day8\":"+jsonString(weekly.status.name())
                +",\"completed_ratings\":"+weekly.completedRatings
                +",\"low_ratings\":"+weekly.lowRatings
                +",\"technical_mix_intermediate_five\":["
                +phase0+","+phase1+","+phase2+"]}");
        List<WeeklyCategoryChallengePolicy.Observation> difficult=Arrays.asList(
            low.get(0),new WeeklyCategoryChallengePolicy.Observation(
                FIRST.plusDays(2),CATEGORY,8,true));
        WeeklyCategoryChallengePolicy.Review hard=
                WeeklyCategoryChallengePolicy.evaluate(
                        CATEGORY,FIRST,FIRST.plusDays(7),difficult);
        assertEquals(WeeklyCategoryChallengePolicy.Status.RECOVERY_PRIORITY,hard.status);
        output("{\"type\":\"weekly_hard\",\"status_day8\":"
                +jsonString(hard.status.name())+",\"high_ratings\":"
                +hard.highRatings+"}");
    }
}
