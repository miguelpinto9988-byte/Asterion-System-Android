package pe.asterionsystem.app;

import static org.junit.Assert.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Arrays;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import pe.asterionsystem.core.engine.missions.MissionPlanner;

/** The actual four-step registration JSON reaches the on-device mission planner. */
@RunWith(AndroidJUnit4.class)
public final class PersonalGoalRegistrationTest {
    @Test public void savedWrittenGoalIsPassedToMissionEngine() throws Exception {
        JSONObject profile=new JSONObject();
        profile.put("experience","principiante");
        profile.put("goal","bienestar");
        profile.put("custom_goal","Quiero caminar por la montaña");
        profile.put("week","4,4,4,4,4,5,5");
        profile.put("bodyweight",true);
        profile.put("missions","3");
        profile.put("intensity","baja");
        MissionPlanner.Profile result=MissionProfileAdapter.fromRegistration(profile);
        assertEquals("Quiero caminar por la montaña",result.personalGoal);
        assertEquals(3,result.dailyLimit);
        assertEquals(Arrays.asList(4,4,4,4,4,5,5),result.weekly);
        assertEquals("baja",result.intensityPreference);
        assertTrue(result.equipment.contains("sin_equipo"));
        assertEquals(result.personalGoal,
                result.adapted("principiante",1).personalGoal);
    }

    @Test public void updatingWrittenGoalInvalidatesDailyPinFingerprint() throws Exception {
        JSONObject profile=new JSONObject();
        profile.put("custom_goal","Quiero caminar");
        MissionPlanner.Profile before=MissionProfileAdapter.fromRegistration(profile);
        profile.put("custom_goal","Quiero mejorar mi equilibrio");
        MissionPlanner.Profile after=MissionProfileAdapter.fromRegistration(profile);
        assertNotEquals(before.missionSignature(),after.missionSignature());
        assertEquals(before.weekly,after.weekly);
        assertEquals(before.equipment,after.equipment);
    }
}
