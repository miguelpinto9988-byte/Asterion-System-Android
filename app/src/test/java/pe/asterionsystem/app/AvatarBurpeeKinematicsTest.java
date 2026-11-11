package pe.asterionsystem.app;

import static org.junit.Assert.*;
import org.junit.Test;

/** A stage-based lateral clip must not stretch bones or lose hand contacts. */
public final class AvatarBurpeeKinematicsTest {
    @Test public void fullBurpeeReturnsToIdenticalStandingPose(){
        AvatarBodySpec spec=AvatarBodySpec.DEFAULT;
        AvatarKinematics.Pose a=AvatarBurpeeKinematics.pose(0f,spec);
        AvatarKinematics.Pose end=AvatarBurpeeKinematics.pose(1f,spec);
        AvatarMotionEngine.assertSkeleton(a,spec,.08f);
        assertEquals(0f,a.hip.distance(end.hip),.001f);
        assertEquals(0f,a.knee.distance(end.knee),.001f);
        assertEquals(0f,a.elbow.distance(end.elbow),.001f);
        assertEquals(0f,a.wrist.distance(end.wrist),.001f);
        assertTrue(a.hip.distance(a.ankle)>189.4f);
        assertTrue(a.shoulder.distance(a.wrist)>120.9f);
        assertTrue(a.farShoulder.distance(a.farWrist)>120.9f);
    }

    @Test public void clipIncludesCrouchPlankAndGroundFacingDip(){
        AvatarKinematics.Pose crouch=AvatarBurpeeKinematics.pose(.25f,
                AvatarBodySpec.DEFAULT);
        AvatarKinematics.Pose plank=AvatarBurpeeKinematics.pose(.40f,
                AvatarBodySpec.DEFAULT);
        AvatarKinematics.Pose lowered=AvatarBurpeeKinematics.pose(.50f,
                AvatarBodySpec.DEFAULT);
        assertTrue(crouch.hip.y>260f);
        assertTrue(plank.ankle.x<50f);
        assertTrue(plank.shoulder.x>plank.hip.x+110f);
        assertTrue(plank.head.x>plank.shoulder.x);
        assertTrue(lowered.shoulder.y>plank.shoulder.y+18f);
        assertTrue(AvatarBurpeeKinematics.handsPlanted(.50f));
        assertFalse(AvatarBurpeeKinematics.handsPlanted(.12f));
        assertTrue(AvatarBurpeeKinematics.feetTravelling(.32f));
        assertFalse(AvatarBurpeeKinematics.feetTravelling(.50f));
    }

    @Test public void handsStayPlantedDuringHorizontalSegment(){
        for(int i=0;i<=100;i++){
            float phase=.25f+i*.005f;
            AvatarKinematics.Pose p=AvatarBurpeeKinematics.pose(
                    phase,AvatarBodySpec.DEFAULT);
            assertEquals(305f,p.wrist.x,.08f);
            assertEquals(364f,p.wrist.y,.08f);
            AvatarMotionEngine.assertSkeleton(p,AvatarBodySpec.DEFAULT,.09f);
        }
    }

    @Test public void denseSampleChecksLengthsAndContinuity(){
        AvatarKinematics.Pose prev=null;
        for(int i=0;i<=600;i++){
            AvatarKinematics.Pose p=AvatarBurpeeKinematics.pose(
                    i/600f,AvatarBodySpec.DEFAULT);
            AvatarMotionEngine.assertSkeleton(p,AvatarBodySpec.DEFAULT,.09f);
            if(prev!=null){
                assertTrue(p.hip.distance(prev.hip)<2f);
                assertTrue(p.elbow.distance(prev.elbow)<5f);
                assertTrue(p.knee.distance(prev.knee)<5f);
                assertTrue(p.ankle.distance(prev.ankle)<4f);
            }
            prev=p;
        }
    }

    @Test public void invalidPhaseAndBodyRejected(){
        assertThrows(IllegalArgumentException.class,()->
                AvatarBurpeeKinematics.pose(Float.NaN,AvatarBodySpec.DEFAULT));
        assertThrows(IllegalArgumentException.class,()->
                AvatarBurpeeKinematics.pose(0f,null));
    }
}
