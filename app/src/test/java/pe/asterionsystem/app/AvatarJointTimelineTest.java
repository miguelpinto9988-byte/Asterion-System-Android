package pe.asterionsystem.app;

import static org.junit.Assert.*;
import org.junit.Test;

/** Screen-space timeline continuity; not a biomechanical validation. */
public final class AvatarJointTimelineTest {
    @Test public void all49CommandsHaveFiniteJointTargetsAndCloseTheirLoop(){
        assertEquals(51,AvatarJointTimeline.Command.values().length);
        assertEquals(24,AvatarJointTimeline.Joint.values().length);
        for(AvatarJointTimeline.Command command:AvatarJointTimeline.Command.values()){
            AvatarJointTimeline line=new AvatarJointTimeline(command);
            assertSame(command,line.command());
            for(AvatarJointTimeline.Joint joint:AvatarJointTimeline.Joint.values()){
                assertEquals("Initial "+command+" "+joint,0f,line.offset(joint,0f),.0001f);
                assertEquals("Final "+command+" "+joint,0f,line.offset(joint,1f),.0001f);
                assertTrue(Float.isFinite(line.offset(joint,.5f)));
                assertEquals(line.peakOffsets().getOrDefault(joint,0f),
                        line.offset(joint,.5f),.0001f);
            }
            assertEquals(24,line.describe(.5f).split("\n").length);
        }
    }

    @Test public void transitionsAreContinuousAndDoNotJumpAcrossFrames(){
        for(AvatarJointTimeline.Command command:AvatarJointTimeline.Command.values()){
            AvatarJointTimeline line=new AvatarJointTimeline(command);
            float before=0f;
            for(int step=0;step<=1000;step++){
                float weight=line.weight(step/1000f);
                assertTrue(weight>=0f && weight<=1f);
                assertTrue("Abrupt transition "+command,Math.abs(weight-before)<.016f);
                before=weight;
            }
            assertEquals(line.weight(0f),line.weight(1f),0f);
            for(float boundary:new float[]{.08f,.42f,.58f,.94f}){
                float pre=line.weight(boundary-.0001f);
                float after=line.weight(boundary+.0001f);
                assertTrue(Math.abs(pre-after)<.001f);
            }
        }
    }

    @Test public void distinctNewCommandsMoveTheExpectedHinges(){
        AvatarJointTimeline curl=new AvatarJointTimeline(
                AvatarJointTimeline.Command.ELBOW_FLEXION);
        assertTrue(curl.offset(AvatarJointTimeline.Joint.LEFT_ELBOW,.5f)>60f);
        assertEquals(0f,curl.offset(AvatarJointTimeline.Joint.LEFT_KNEE,.5f),0f);
        AvatarJointTimeline knee=new AvatarJointTimeline(
                AvatarJointTimeline.Command.KNEE_FLEXION);
        assertTrue(knee.offset(AvatarJointTimeline.Joint.RIGHT_KNEE,.5f)>40f);
        assertEquals(0f,knee.offset(AvatarJointTimeline.Joint.RIGHT_ELBOW,.5f),0f);
        AvatarJointTimeline neck=new AvatarJointTimeline(
                AvatarJointTimeline.Command.NECK_MOBILITY);
        assertTrue(neck.offset(AvatarJointTimeline.Joint.NECK_TURN,.5f)>0);
    }

    @Test public void newUnilateralCommandsUseTheCorrectSideAndAxis(){
        AvatarJointTimeline step=new AvatarJointTimeline(
                AvatarJointTimeline.Command.LATERAL_STEP);
        assertTrue(step.offset(AvatarJointTimeline.Joint.RIGHT_HIP_SIDE,.5f)>0);
        assertEquals(0f,step.offset(AvatarJointTimeline.Joint.LEFT_HIP_SIDE,.5f),0);
        AvatarJointTimeline neck=new AvatarJointTimeline(
                AvatarJointTimeline.Command.NECK_SIDE_TILT);
        assertEquals(12f,neck.offset(AvatarJointTimeline.Joint.NECK_SIDE,.5f),.001f);
        assertEquals(0f,neck.offset(AvatarJointTimeline.Joint.NECK_TURN,.5f),0);
    }

    @Test public void invalidPhaseAndNullJointFailClosed(){
        AvatarJointTimeline line=new AvatarJointTimeline(
                AvatarJointTimeline.Command.BALANCE);
        for(float bad:new float[]{-1f,1.1f,Float.NaN,
                                  Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY}){
            assertThrows(IllegalArgumentException.class,()->line.weight(bad));
        }
        assertThrows(IllegalArgumentException.class,()->
                line.offset(null,.5f));
        assertThrows(IllegalArgumentException.class,()->
                new AvatarJointTimeline(null));
    }
}
