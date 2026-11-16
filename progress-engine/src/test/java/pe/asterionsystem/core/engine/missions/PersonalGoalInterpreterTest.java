package pe.asterionsystem.core.engine.missions;

import static org.junit.Assert.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalogLoader;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;

/** Regression tests for optional free-text training goals. */
public final class PersonalGoalInterpreterTest {
    private ExerciseVariant option(String id,String type,String label) {
        return new ExerciseVariant(id,type,label,1,"sin_equipo","estable",false);
    }

    @Test public void spanishAccentsAndActivityWordsAreRecognized() {
        PersonalGoalInterpreter walk=PersonalGoalInterpreter.from(
                "Quiero caminar por la montaña con constancia.");
        assertTrue(walk.recognized());
        assertTrue(walk.score(option("caminar_suave","caminata","Caminata suave"),"cardio")>0);
        assertEquals(0,walk.score(option("press_suave","empuje_horizontal","Empuje suave"),"pecho"));
        PersonalGoalInterpreter mobility=PersonalGoalInterpreter.from(
                "Me interesa la flexibilidad y la movilidad.");
        assertTrue(mobility.score(option("movimiento_suave","movilidad_cadera",
                "Movilidad de cadera"),"movilidad")>0);
    }

    @Test public void negationAndUnrecognizedTextDoNotInventPreferences() {
        assertFalse(PersonalGoalInterpreter.from("No quiero correr.").recognized());
        assertFalse(PersonalGoalInterpreter.from("Mi objetivo es perder peso.").recognized());
        assertFalse(PersonalGoalInterpreter.from("").recognized());
        assertFalse(PersonalGoalInterpreter.from(null).recognized());
    }

    @Test public void storedGoalIsRetainedByDailySafetyAdaptation() {
        MissionPlanner.Profile configured=new MissionPlanner.Profile(
                "principiante","bienestar",Arrays.asList(4,4,4,4,4,4,5),
                Set.of("sin_equipo"),3,"baja","", "Quiero caminar con frecuencia");
        MissionPlanner.Profile softer=configured.adapted("principiante",1);
        assertEquals(configured.personalGoal,softer.personalGoal);
        assertEquals(1,softer.dailyLimit);
        assertNotEquals(configured.missionSignature(),
                new MissionPlanner.Profile("principiante","bienestar",
                configured.weekly,configured.equipment,3,"baja","",
                "Quiero hacer ejercicios de movilidad").missionSignature());
    }

    @Test public void freeTextNeverReplacesAnExplicitRestDayOrEquipmentFilter()
            throws Exception {
        String root="/asterion_exercises/v1/";
        try(InputStreamReader family=reader(root+"families.tsv");
            InputStreamReader types=reader(root+"types.tsv");
            InputStreamReader variants=reader(root+"variants.tsv")) {
            ExerciseCatalog catalog=ExerciseCatalogLoader.load(family,types,variants);
            MissionPlanner planner=new MissionPlanner(catalog);
            MissionPlanner.Profile rest=new MissionPlanner.Profile(
                    "principiante","bienestar",Arrays.asList(5,4,4,4,4,4,5),
                    Set.of("sin_equipo"),3,"baja","",
                    "Quiero caminar a diario");
            assertTrue(planner.daily(rest,LocalDate.of(2026,11,9)).empty());
            MissionPlanner.Profile noGear=new MissionPlanner.Profile(
                    "principiante","bienestar",Arrays.asList(4,4,4,4,4,4,4),
                    Set.of(),3,"baja","",
                    "Quiero caminar a diario");
            assertTrue(planner.daily(noGear,LocalDate.of(2026,11,9)).empty());
        }
    }

    @Test public void dailySelectionWithFreeTextIsStableAndRespectsLimits()
            throws Exception {
        String root="/asterion_exercises/v1/";
        try(InputStreamReader family=reader(root+"families.tsv");
            InputStreamReader types=reader(root+"types.tsv");
            InputStreamReader variants=reader(root+"variants.tsv")) {
            ExerciseCatalog catalog=ExerciseCatalogLoader.load(family,types,variants);
            MissionPlanner planner=new MissionPlanner(catalog);
            MissionPlanner.Profile profile=new MissionPlanner.Profile(
                    "principiante","resistencia",Arrays.asList(4,4,4,4,4,4,4),
                    Set.of("sin_equipo","pared","silla"),3,"baja","",
                    "Me gustaría caminar con frecuencia");
            LocalDate monday=LocalDate.of(2026,11,9);
            List<ExerciseVariant> first=planner.daily(profile,monday).candidates();
            List<ExerciseVariant> again=planner.daily(profile,monday).candidates();
            assertEquals(first.stream().map(ExerciseVariant::id).toList(),
                    again.stream().map(ExerciseVariant::id).toList());
            assertTrue(first.size()<=profile.dailyLimit);
            for(ExerciseVariant exercise:first) {
                assertTrue(catalog.eligibleForAutomaticSuggestion(exercise));
                assertTrue(profile.equipment.containsAll(exercise.equipment()));
            }
        }
    }

    private InputStreamReader reader(String path) {
        return new InputStreamReader(getClass().getResourceAsStream(path),
                StandardCharsets.UTF_8);
    }
}
