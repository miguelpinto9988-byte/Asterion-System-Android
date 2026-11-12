package pe.asterionsystem.app;

import static org.junit.Assert.*;
import org.junit.Test;

/** Screen-space timeline continuity; not a biomechanical validation. */
public final class AvatarJointTimelineTest {
    @Test public void all57CommandsHaveFiniteJointTargetsAndCloseTheirLoop(){
        assertEquals(63,AvatarJointTimeline.Command.values().length);
        assertEquals(24,AvatarJointTimeline.Joint.values().length);
        for(AvatarJointTimeline.Command command:AvatarJointTimeline.Command.values()){
            AvatarJointTimeline line=new AvatarJointTimeline(command);
            assertSame(command,line.command());
            for(AvatarJointTimeline.Joint joint:AvatarJointTimeline.Joint.values()){
                assertEquals("Initial "+command+" "+joint,0f,line.offset(joint,0f),.0001f);
                assertEquals("Final "+command+" "+joint,0f,line.offset(joint,1f),.0001f);
                assertTrue(Float.isFinite(line.offset(joint,.5f)));
                if(command==AvatarJointTimeline.Command.SEATED_HEEL_TOE_ALTERNATION){
                    assertEquals("Neutral half-cycle",0f,line.offset(joint,.5f),.0001f);
                    if(joint==AvatarJointTimeline.Joint.LEFT_ANKLE||
                            joint==AvatarJointTimeline.Joint.RIGHT_ANKLE){
                        assertEquals(-16f,line.offset(joint,.25f),.001f);
                        assertEquals(16f,line.offset(joint,.75f),.001f);
                    }
                }else if(command==AvatarJointTimeline.Command.SEATED_KNEE_EXTENSION_ALTERNATING){
                    // In a truly alternating sequence both knees are neutral
                    // at 50%, rather than reaching peak together.
                    float expected=joint==AvatarJointTimeline.Joint.LEFT_KNEE
                            ?line.peakOffsets().getOrDefault(joint,0f):0f;
                    assertEquals("first knee at quarter-cycle",expected,
                            line.offset(joint,.25f),.0001f);
                    expected=joint==AvatarJointTimeline.Joint.RIGHT_KNEE
                            ?line.peakOffsets().getOrDefault(joint,0f):0f;
                    assertEquals("second knee at three-quarter-cycle",expected,
                            line.offset(joint,.75f),.0001f);
                    assertEquals("neutral midpoint",0f,
                            line.offset(joint,.5f),.0001f);
                }else{
                    assertEquals(line.peakOffsets().getOrDefault(joint,0f),
                            line.offset(joint,.5f),.0001f);
                }
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

    @Test public void seatedProgramsMatchArticulatedCanvasAngles(){
        AvatarJointTimeline toes=new AvatarJointTimeline(
                AvatarJointTimeline.Command.SEATED_TOE_RAISE);
        assertEquals(22f,toes.offset(AvatarJointTimeline.Joint.LEFT_ANKLE,.49f),.001f);
        assertEquals(22f,toes.offset(AvatarJointTimeline.Joint.RIGHT_ANKLE,.49f),.001f);
        assertEquals(0f,toes.offset(AvatarJointTimeline.Joint.LEFT_KNEE,.49f),0f);
        AvatarJointTimeline reach=new AvatarJointTimeline(
                AvatarJointTimeline.Command.SEATED_FORWARD_REACH);
        assertEquals(21f,reach.offset(AvatarJointTimeline.Joint.SPINE_BEND,.49f),.001f);
        assertEquals(64f,reach.offset(AvatarJointTimeline.Joint.LEFT_SHOULDER,.49f),.001f);
        assertEquals(64f,reach.offset(AvatarJointTimeline.Joint.RIGHT_SHOULDER,.49f),.001f);
        assertEquals(0f,reach.offset(AvatarJointTimeline.Joint.LEFT_HIP,.49f),0f);
        for(float t:new float[]{0f,1f}){
            assertEquals(0f,toes.offset(AvatarJointTimeline.Joint.LEFT_ANKLE,t),.001f);
            assertEquals(0f,reach.offset(AvatarJointTimeline.Joint.SPINE_BEND,t),.001f);
        }
    }

    @Test public void dedicatedSeatedAndBridgeProgramsMatchTheirTwoStageOrHeldScenes(){
        AvatarJointTimeline alternate=new AvatarJointTimeline(
                AvatarJointTimeline.Command.SEATED_KNEE_EXTENSION_ALTERNATING);
        assertEquals(65f,alternate.offset(AvatarJointTimeline.Joint.LEFT_KNEE,.25f),.001f);
        assertEquals(0f,alternate.offset(AvatarJointTimeline.Joint.RIGHT_KNEE,.25f),.001f);
        assertEquals(0f,alternate.offset(AvatarJointTimeline.Joint.LEFT_KNEE,.75f),.001f);
        assertEquals(65f,alternate.offset(AvatarJointTimeline.Joint.RIGHT_KNEE,.75f),.001f);
        AvatarJointTimeline pausedKnee=new AvatarJointTimeline(
                AvatarJointTimeline.Command.SEATED_KNEE_EXTENSION_PAUSE);
        AvatarJointTimeline pausedBridge=new AvatarJointTimeline(
                AvatarJointTimeline.Command.GLUTE_BRIDGE_PAUSE);
        AvatarJointTimeline single=new AvatarJointTimeline(
                AvatarJointTimeline.Command.SEATED_KNEE_EXTENSION_SINGLE);
        assertEquals(65f,pausedKnee.offset(AvatarJointTimeline.Joint.LEFT_KNEE,.35f),.001f);
        assertEquals(65f,pausedKnee.offset(AvatarJointTimeline.Joint.LEFT_KNEE,.65f),.001f);
        assertTrue(pausedBridge.offset(AvatarJointTimeline.Joint.LEFT_HIP,.65f)<-18f);
        assertTrue(single.offset(AvatarJointTimeline.Joint.LEFT_KNEE,.5f)>60f);
        assertEquals(0f,single.offset(AvatarJointTimeline.Joint.RIGHT_KNEE,.5f),0f);
    }

    @Test public void unilateralToeAndTwoHeelRaiseProgramsAreDistinct(){
        AvatarJointTimeline single=new AvatarJointTimeline(
                AvatarJointTimeline.Command.SEATED_TOE_RAISE_SINGLE);
        assertEquals(22f,single.offset(AvatarJointTimeline.Joint.LEFT_ANKLE,.49f),.1f);
        assertEquals(0f,single.offset(AvatarJointTimeline.Joint.RIGHT_ANKLE,.49f),0f);
        AvatarJointTimeline heel=new AvatarJointTimeline(
                AvatarJointTimeline.Command.SEATED_HEEL_RAISE);
        AvatarJointTimeline gentle=new AvatarJointTimeline(
                AvatarJointTimeline.Command.SEATED_HEEL_RAISE_GENTLE);
        assertEquals(-22f,heel.offset(AvatarJointTimeline.Joint.LEFT_ANKLE,.49f),.1f);
        assertEquals(-22f,heel.offset(AvatarJointTimeline.Joint.RIGHT_ANKLE,.49f),.1f);
        assertEquals(-12f,gentle.offset(AvatarJointTimeline.Joint.LEFT_ANKLE,.49f),.1f);
        assertEquals(0f,heel.offset(AvatarJointTimeline.Joint.LEFT_KNEE,.49f),0f);
        assertEquals(0f,heel.offset(AvatarJointTimeline.Joint.RIGHT_HIP,.49f),0f);
    }

    @Test public void heelAndToeHaveSeparateFixedContactPhases(){
        AvatarJointTimeline t=new AvatarJointTimeline(
                AvatarJointTimeline.Command.SEATED_HEEL_TOE_ALTERNATION);
        for(AvatarJointTimeline.Joint j:new AvatarJointTimeline.Joint[]{
                AvatarJointTimeline.Joint.LEFT_ANKLE,
                AvatarJointTimeline.Joint.RIGHT_ANKLE}){
            assertEquals(0f,t.offset(j,0f),0f);
            assertEquals(-16f,t.offset(j,.25f),.001f);
            assertEquals(0f,t.offset(j,.5f),0f);
            assertEquals(16f,t.offset(j,.75f),.001f);
            assertEquals(0f,t.offset(j,1f),0f);
        }
        assertEquals(0f,t.offset(AvatarJointTimeline.Joint.LEFT_KNEE,.25f),0f);
        assertEquals(0f,t.offset(AvatarJointTimeline.Joint.LEFT_HIP,.75f),0f);
    }

    @Test public void standingShoeCommandsKeepTheOppositeSupportAndNoHipMovement(){
        AvatarJointTimeline heel=new AvatarJointTimeline(
                AvatarJointTimeline.Command.STANDING_HEEL_RAISE);
        AvatarJointTimeline toes=new AvatarJointTimeline(
                AvatarJointTimeline.Command.STANDING_TOE_RAISE);
        for(AvatarJointTimeline.Joint joint:new AvatarJointTimeline.Joint[]{
                AvatarJointTimeline.Joint.LEFT_ANKLE,
                AvatarJointTimeline.Joint.RIGHT_ANKLE}){
            assertEquals(-18f,heel.offset(joint,.49f),.001f);
            assertEquals(16f,toes.offset(joint,.49f),.001f);
            assertEquals(0f,heel.offset(joint,1f),0f);
            assertEquals(0f,toes.offset(joint,1f),0f);
        }
        assertEquals(0f,heel.offset(AvatarJointTimeline.Joint.LEFT_KNEE,.49f),0f);
        assertEquals(0f,heel.offset(AvatarJointTimeline.Joint.SPINE_BEND,.49f),0f);
        assertEquals(0f,toes.offset(AvatarJointTimeline.Joint.RIGHT_HIP,.49f),0f);
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
