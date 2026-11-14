package pe.asterionsystem.core.engine.exercises;

import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public final class ExternalExerciseSelectionPolicyTest {
    private static ExternalExerciseSelectionPolicy.Candidate row(
            int id,String category,int level,boolean reviewed,boolean supervised,
            String gear) {
        return new ExternalExerciseSelectionPolicy.Candidate(
                String.format("ext_%04d",id),category,Set.of(gear),level,
                reviewed,supervised);
    }

    @Test public void neverSchedulesUnreviewedSourceEntries() {
        List<ExternalExerciseSelectionPolicy.Candidate> rows=List.of(
                row(1,"tren_inferior",0,false,false,"sin_equipo"),
                row(2,"tren_inferior",0,false,false,"mancuerna"));
        assertTrue(ExternalExerciseSelectionPolicy.select(rows,"tren_inferior",
                Set.of("sin_equipo","mancuerna"),3,5,
                LocalDate.of(2026,11,13),0).isEmpty());
    }

    @Test public void honorsCategorySafetyLevelsAndAvailableEquipment() {
        List<ExternalExerciseSelectionPolicy.Candidate> rows=List.of(
                row(1,"tren_inferior",1,true,false,"sin_equipo"),
                row(2,"tren_inferior",2,true,false,"mancuerna"),
                row(3,"tren_inferior",4,true,true,"sin_equipo"),
                row(4,"cardio",1,true,false,"sin_equipo"),
                row(5,"tren_inferior",1,true,true,"sin_equipo"),
                row(6,"tren_inferior",0,false,false,"sin_equipo"));
        var found=ExternalExerciseSelectionPolicy.select(rows,"tren_inferior",
                Set.of("sin_equipo"),1,5,LocalDate.of(2026,11,13),0);
        assertEquals(1,found.size());
        assertEquals("ext_0001",found.get(0).id);
    }

    @Test public void stableRandomizationRegardlessOfInputOrder() {
        var a=row(1,"movilidad",1,true,false,"sin_equipo");
        var b=row(2,"movilidad",1,true,false,"sin_equipo");
        var c=row(3,"movilidad",2,true,false,"sin_equipo");
        var today=LocalDate.of(2026,11,13);
        var left=ExternalExerciseSelectionPolicy.select(
                Arrays.asList(a,b,c),"movilidad",Set.of("sin_equipo"),
                2,2,today,5);
        var right=ExternalExerciseSelectionPolicy.select(
                Arrays.asList(c,a,b),"movilidad",Set.of("sin_equipo"),
                2,2,today,5);
        assertEquals(left.stream().map(x->x.id).toList(),
                right.stream().map(x->x.id).toList());
        assertEquals(2,left.size());
        assertNotEquals(left.get(0).id,left.get(1).id);
    }

    @Test public void rejectsUnvalidatedOrUnboundedRequests() {
        assertThrows(IllegalArgumentException.class,()->row(1,"cardio",0,true,
                false,"sin_equipo"));
        assertThrows(IllegalArgumentException.class,()->
                ExternalExerciseSelectionPolicy.select(Collections.emptyList(),
                        "inventada",Set.of("sin_equipo"),1,5,
                        LocalDate.now(),0));
        assertThrows(IllegalArgumentException.class,()->
                ExternalExerciseSelectionPolicy.select(Collections.emptyList(),
                        "cardio",Set.of("sin_equipo"),4,5,
                        LocalDate.now(),0));
    }
}
