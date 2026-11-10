package pe.asterionsystem.core.engine.missions;

import static org.junit.Assert.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalogLoader;
import pe.asterionsystem.core.engine.exercises.ExerciseIntensity;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;

/** Exercises full 817-variant Android asset taxonomy with a real intensity index. */
public class DailyMissionStabilityTest {
    private ExerciseCatalog catalog;
    private MissionPlanner planner;
    private final LocalDate monday=LocalDate.of(2026,11,9);
    private final List<Integer> upperAllWeek=Arrays.asList(0,0,0,0,0,0,0);

    @Before public void loadRealLibrary() throws Exception {
        String prefix="/asterion_exercises/v1/";
        try(InputStreamReader f=reader(prefix+"families.tsv");
            InputStreamReader t=reader(prefix+"types.tsv");
            InputStreamReader v=reader(prefix+"variants.tsv");
            InputStreamReader i=reader(prefix+"intensity.tsv")) {
            catalog=ExerciseCatalogLoader.load(f,t,v,i);
            assertEquals(817,catalog.variants().size());
            planner=new MissionPlanner(catalog);
        }
    }
    private InputStreamReader reader(String path) {
        return new InputStreamReader(getClass().getResourceAsStream(path),
                StandardCharsets.UTF_8);
    }
    private MissionPlanner.Profile profile(String intensity,Set<String> equipment) {
        return new MissionPlanner.Profile("intermedio","bienestar",upperAllWeek,
                equipment,3,intensity,"misiones|series|60|30|");
    }
    private List<String> ids(MissionPlanner.Proposal proposal) {
        ArrayList<String> ids=new ArrayList<>();
        for(ExerciseVariant variant:proposal.candidates())ids.add(variant.id());
        return ids;
    }

    @Test public void sameDaySameProfileNeverReshufflesOnAnotherScreen() {
        MissionPlanner.Profile p=profile("moderada",Set.of("sin_equipo","pared"));
        List<String> first=ids(planner.daily(p,monday));
        assertFalse(first.isEmpty());
        for(int i=0;i<12;i++) {
            assertEquals(first,ids(new MissionPlanner(catalog).daily(p,monday)));
        }
        assertEquals(first,ids(planner.restoreDaily(p,monday,first)));
    }

    @Test public void futureDaysProduceVarietyAcrossWholeEligibleCatalog() {
        MissionPlanner.Profile p=profile("moderada",Set.of("sin_equipo"));
        Set<List<String>> selections=new HashSet<>();
        for(int day=0;day<7;day++) {
            selections.add(ids(planner.daily(p,monday.plusDays(day))));
        }
        assertTrue("Date-seeded shuffle should vary daily missions",
                selections.size()>1);
    }

    @Test public void changedIntensityInvalidatesSignatureAndLimitsToLow() {
        MissionPlanner.Profile moderate=profile("moderada",Set.of("sin_equipo"));
        MissionPlanner.Profile low=profile("baja",Set.of("sin_equipo"));
        assertNotEquals(moderate.missionSignature(),low.missionSignature());
        MissionPlanner.Proposal proposal=planner.daily(low,monday);
        assertFalse(proposal.empty());
        for(ExerciseVariant v:proposal.candidates()) {
            assertEquals(ExerciseIntensity.LOW,catalog.intensity(v.id()));
        }
    }

    @Test public void equipmentCalendarGoalAndRoutineModeChangeFingerprint() {
        MissionPlanner.Profile baseline=profile("moderada",Set.of("sin_equipo"));
        assertNotEquals(baseline.missionSignature(),
                profile("moderada",Set.of("sin_equipo","pared")).missionSignature());
        assertNotEquals(baseline.missionSignature(),
                new MissionPlanner.Profile("intermedio","fuerza",upperAllWeek,
                        Set.of("sin_equipo"),3,"moderada","misiones|series|60|30|")
                        .missionSignature());
        assertNotEquals(baseline.missionSignature(),
                new MissionPlanner.Profile("intermedio","bienestar",
                        Arrays.asList(0,1,0,0,0,0,0),Set.of("sin_equipo"),3,
                        "moderada","misiones|series|60|30|").missionSignature());
        assertNotEquals(baseline.missionSignature(),
                new MissionPlanner.Profile("intermedio","bienestar",upperAllWeek,
                        Set.of("sin_equipo"),3,"moderada",
                        "rutinas|rondas|90|15|").missionSignature());
    }

    @Test public void unorderedEquipmentInputDoesNotReshuffle() {
        Set<String> gearA=new java.util.LinkedHashSet<>(
                Arrays.asList("pared","sin_equipo","mancuerna"));
        Set<String> gearB=new java.util.LinkedHashSet<>(
                Arrays.asList("mancuerna","pared","sin_equipo"));
        assertEquals(profile("moderada",gearA).missionSignature(),
                profile("moderada",gearB).missionSignature());
        assertEquals(ids(planner.daily(profile("moderada",gearA),monday)),
                ids(planner.daily(profile("moderada",gearB),monday)));
    }

    @Test public void storedIdentifiersAreRehydratedOnlyWhenSafe() {
        MissionPlanner.Profile compatible=profile("moderada",Set.of("sin_equipo"));
        List<String> ids=ids(planner.daily(compatible,monday));
        assertEquals(ids,ids(planner.restoreDaily(compatible,monday,ids)));
        MissionPlanner.Profile noGear=profile("moderada",Set.of());
        assertThrows(IllegalArgumentException.class,
                ()->planner.restoreDaily(noGear,monday,ids));
        assertThrows(IllegalArgumentException.class,
                ()->planner.restoreDaily(compatible,monday,
                        Arrays.asList(ids.get(0),ids.get(0))));
    }

    @Test public void restDayHasNoExercisesEvenAfterProfileUpdate() {
        MissionPlanner.Profile p=new MissionPlanner.Profile(
                "principiante","constancia",Arrays.asList(5,0,0,0,0,0,0),
                Set.of("sin_equipo"),3,"moderada","");
        assertTrue(planner.daily(p,monday).empty());
    }

    @Test public void vigorousCatalogItemsAreNotAssignedAutomatically() {
        MissionPlanner.Profile p=profile("moderada",
                Set.of("sin_equipo","mancuerna","pared","silla"));
        for(int day=0;day<5;day++) {
            for(ExerciseVariant v:planner.daily(p,monday.plusDays(day)).candidates()) {
                assertNotEquals(ExerciseIntensity.HIGH,catalog.intensity(v.id()));
                assertNotEquals(ExerciseIntensity.VARIABLE,catalog.intensity(v.id()));
                assertTrue(p.equipment.containsAll(v.equipment()));
            }
        }
    }
}
