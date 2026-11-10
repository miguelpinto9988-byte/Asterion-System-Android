package pe.asterionsystem.core.engine.missions;

import static org.junit.Assert.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalogLoader;
import pe.asterionsystem.core.engine.exercises.ExerciseCategoryMapper;
import pe.asterionsystem.core.engine.exercises.ExerciseFamily;
import pe.asterionsystem.core.engine.exercises.ExerciseType;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;

public final class MonarchRandomnessTest {
    private static final LocalDate START=LocalDate.of(2026,11,10);
    private ExerciseCatalog catalog;
    private MissionPlanner planner;

    @Before public void setup() throws Exception {
        String root="/asterion_exercises/v1/";
        try(InputStreamReader f=reader(root+"families.tsv");
            InputStreamReader t=reader(root+"types.tsv");
            InputStreamReader v=reader(root+"variants.tsv");
            InputStreamReader i=reader(root+"intensity.tsv")) {
            catalog=ExerciseCatalogLoader.load(f,t,v,i);
            assertEquals(917,catalog.variants().size());
            planner=new MissionPlanner(catalog);
        }
    }

    private InputStreamReader reader(String path) {
        return new InputStreamReader(getClass().getResourceAsStream(path),
                StandardCharsets.UTF_8);
    }

    private MissionPlanner.Profile profile(String experience,String goal,
                                            Set<String> gear) {
        return new MissionPlanner.Profile(experience,goal,
                Arrays.asList(0,1,2,3,4,5,5),gear,3,"moderada","");
    }

    private MissionPlanner.Profile broad(String experience) {
        return profile(experience,"bienestar",Set.of(
                "sin_equipo","pared","colchoneta","silla","banco","mancuerna"));
    }

    private List<String> ids(MissionPlanner.Proposal proposal) {
        List<String> result=new ArrayList<>();
        for(ExerciseVariant v:proposal.candidates())result.add(v.id());
        return result;
    }

    @Test public void sameCycleAndProfileRemainStableAcrossNewPlannerInstances() {
        MissionPlanner.Profile p=broad("intermedio");
        List<String> initial=ids(planner.monarch(p,START));
        assertEquals(5,initial.size());
        for(int i=0;i<10;i++) {
            assertEquals(initial,ids(new MissionPlanner(catalog).monarch(p,START)));
        }
        assertEquals(5,new HashSet<>(initial).size());
        assertEquals(START.plusDays(5),planner.monarch(p,START).untilExclusive());
        assertTrue(planner.monarch(p,START).requiresConsent());
    }

    @Test public void newFiveDayCyclesProduceMultipleSelections() {
        MissionPlanner.Profile p=broad("intermedio");
        Set<List<String>> selections=new HashSet<>();
        for(int index=0;index<8;index++) {
            selections.add(ids(planner.monarch(p,START.plusDays(index*5))));
        }
        assertTrue("Every cycle must have opportunity for varied selection",
                selections.size()>1);
    }

    @Test public void previousCycleGetsNewCompatibleAlternativesWhenAvailable() {
        MissionPlanner.Profile p=broad("intermedio");
        List<String> previous=ids(planner.monarch(p,START));
        MissionPlanner.Proposal next=planner.monarch(p,START.plusDays(5),previous);
        assertEquals(5,next.candidates().size());
        assertEquals(5,new HashSet<>(ids(next)).size());
        assertTrue("Avoid recent Monarch repetition when there are alternatives",
                Collections.disjoint(previous,ids(next)));
        assertEquals(ids(next),ids(planner.monarch(p,START.plusDays(5),
                new ArrayList<>(previous))));
    }

    @Test public void spreadAcrossAvailableScheduledCategories() {
        MissionPlanner.Profile p=broad("intermedio");
        Map<String,String> categoryForFamily=new HashMap<>();
        for(ExerciseFamily family:catalog.families()) {
            categoryForFamily.put(family.id(),
                    ExerciseCategoryMapper.primaryCategory(family.category()));
        }
        Map<String,String> categoryForType=new HashMap<>();
        for(ExerciseType type:catalog.types()) {
            categoryForType.put(type.id(),categoryForFamily.get(type.familyId()));
        }
        Set<String> categories=new HashSet<>();
        for(ExerciseVariant variant:planner.monarch(p,START).candidates()) {
            categories.add(categoryForType.get(variant.typeId()));
        }
        assertEquals("One candidate for each of five compatible categories",5,
                categories.size());
    }

    @Test public void noAdvancedOrSupervisedAutomaticMonarchSelection() {
        for(String experience:List.of("principiante","intermedio","avanzado")) {
            MissionPlanner.Profile p=broad(experience);
            for(int cycle=0;cycle<7;cycle++) {
                MissionPlanner.Proposal proposal=planner.monarch(p,
                        START.plusDays(5*cycle));
                assertEquals(5,proposal.candidates().size());
                for(ExerciseVariant v:proposal.candidates()) {
                    assertTrue(v.difficulty()<=
                            ("principiante".equals(experience)?1:2));
                    assertFalse(v.supervisionRecommended());
                    assertTrue(p.equipment.containsAll(v.equipment()));
                    assertTrue(catalog.eligibleForAutomaticSuggestion(v));
                }
            }
        }
    }

    @Test public void restoredAcceptedListMustRemainCompatible() {
        MissionPlanner.Profile p=broad("intermedio");
        List<String> selected=ids(planner.monarch(p,START));
        assertEquals(selected,ids(planner.restoreMonarch(p,START,selected)));
        assertThrows(IllegalArgumentException.class,()->
                planner.restoreMonarch(p,START,
                        Arrays.asList(selected.get(0),selected.get(0),
                                selected.get(2),selected.get(3),selected.get(4))));
        MissionPlanner.Profile missingGear=profile("intermedio","bienestar",Set.of());
        assertThrows(IllegalArgumentException.class,()->
                planner.restoreMonarch(missingGear,START,selected));
    }

    @Test public void restOnlyProfileStillCannotActivateMonarch() {
        MissionPlanner.Profile rest=new MissionPlanner.Profile(
                "avanzado","bienestar",Arrays.asList(5,5,5,5,5,5,5),
                Set.of("sin_equipo"),3);
        assertTrue(planner.monarch(rest,START).empty());
        assertTrue(planner.monarch(rest,START).requiresConsent());
    }
}
