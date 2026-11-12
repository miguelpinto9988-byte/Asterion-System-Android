package pe.asterionsystem.app;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Pure-Java regression: the 53 catalog commands drive a rigid 2D skeletal
 * reference, NOT biomechanically validated exercise demonstrations.
 */
public final class AvatarCommandFigurePoseTest {
    private static final float EPS=.045f;
    @Test public void allCommandsKeepRigidSegmentsAndHaveSmoothClosedCycles(){
        for(AvatarJointTimeline.Command command:AvatarJointTimeline.Command.values()){
            AvatarJointTimeline program=new AvatarJointTimeline(command);
            AvatarCommandFigurePose first=AvatarCommandFigurePose.from(program,0f);
            AvatarCommandFigurePose last=null;
            for(int i=0;i<=360;i++){
                AvatarCommandFigurePose p=AvatarCommandFigurePose.from(program,i/360f);
                p.requireFixedLengths(EPS);
                if(last!=null){
                    assertTrue("Wrist skipped "+command,
                            p.leftWrist.distance(last.leftWrist)<5f);
                    assertTrue("Right knee skipped "+command,
                            p.rightKnee.distance(last.rightKnee)<5f);
                    assertTrue("Neck skipped "+command,
                            p.head.distance(last.head)<5f);
                }
                last=p;
            }
            assertNotNull(last);
            assertEquals("L wrist seam "+command,0f,
                    first.leftWrist.distance(last.leftWrist),EPS);
            assertEquals("R ankle seam "+command,0f,
                    first.rightAnkle.distance(last.rightAnkle),EPS);
            assertEquals("Neck seam "+command,0f,
                    first.head.distance(last.head),EPS);
            assertEquals("Hip seam "+command,0f,
                    first.leftHip.distance(last.leftHip),EPS);
        }
    }
    @Test public void muscleFamiliesAffectDistinctVisualSegments(){
        AvatarCommandFigurePose arms0=AvatarCommandFigurePose.from(
                new AvatarJointTimeline(AvatarJointTimeline.Command.SHOULDER_ABDUCTION),0f);
        AvatarCommandFigurePose arms1=AvatarCommandFigurePose.from(
                new AvatarJointTimeline(AvatarJointTimeline.Command.SHOULDER_ABDUCTION),.49f);
        assertTrue(arms0.leftWrist.distance(arms1.leftWrist)>65f);
        AvatarCommandFigurePose knee0=AvatarCommandFigurePose.from(
                new AvatarJointTimeline(AvatarJointTimeline.Command.KNEE_FLEXION),0f);
        AvatarCommandFigurePose knee1=AvatarCommandFigurePose.from(
                new AvatarJointTimeline(AvatarJointTimeline.Command.KNEE_FLEXION),.49f);
        assertTrue(knee0.leftAnkle.distance(knee1.leftAnkle)>30f);
        assertEquals(0f,knee0.leftWrist.distance(knee1.leftWrist),EPS);
        AvatarCommandFigurePose neck0=AvatarCommandFigurePose.from(
                new AvatarJointTimeline(AvatarJointTimeline.Command.NECK_SIDE_TILT),0f);
        AvatarCommandFigurePose neck1=AvatarCommandFigurePose.from(
                new AvatarJointTimeline(AvatarJointTimeline.Command.NECK_SIDE_TILT),.49f);
        assertTrue(neck1.head.x>neck0.head.x+4f);
        assertEquals(0f,neck0.leftAnkle.distance(neck1.leftAnkle),EPS);
    }
    @Test public void rejectsMissingCommandsAndInvalidPhases(){
        assertThrows(IllegalArgumentException.class,()->
                AvatarCommandFigurePose.from(null,.5f));
        AvatarJointTimeline known=new AvatarJointTimeline(
                AvatarJointTimeline.Command.WALK);
        for(float bad:new float[]{-1f,1.01f,Float.NaN,Float.POSITIVE_INFINITY}){
            assertThrows(IllegalArgumentException.class,()->
                    AvatarCommandFigurePose.from(known,bad));
        }
    }
}
