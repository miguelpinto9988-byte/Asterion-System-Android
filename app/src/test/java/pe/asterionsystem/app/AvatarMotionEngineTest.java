package pe.asterionsystem.app;

import static org.junit.Assert.*;
import org.junit.Test;

/** Generic illustrated motion presets can share one constrained skeleton. */
public final class AvatarMotionEngineTest {
    private static final float EPS=.07f;

    @Test public void fixedBoneLengthsAcrossEverySampledMotion(){
        for(AvatarMotionEngine.Motion m:AvatarMotionEngine.Motion.values()){
            AvatarKinematics.Pose last=null;
            for(int i=0;i<=300;i++){
                AvatarKinematics.Pose p=AvatarMotionEngine.pose(
                        m,i/300f,AvatarBodySpec.DEFAULT);
                AvatarMotionEngine.assertSkeleton(p,AvatarBodySpec.DEFAULT,EPS);
                if(last!=null){
                    float limit=m==AvatarMotionEngine.Motion.BURPEE?7f:4f;
                    assertTrue("hip jump in "+m,p.hip.distance(last.hip)<limit);
                    assertTrue("knee jump in "+m,p.knee.distance(last.knee)<limit);
                    assertTrue("elbow jump in "+m,p.elbow.distance(last.elbow)<limit);
                }
                last=p;
            }
        }
    }

    @Test public void plantedFeetStayLockedUnlessMotionExplicitlyLiftsFoot(){
        for(AvatarMotionEngine.Motion m:AvatarMotionEngine.Motion.values()){
            AvatarKinematics.Pose start=AvatarMotionEngine.pose(
                    m,0f,AvatarBodySpec.DEFAULT);
            for(int i=1;i<=100;i++){
                AvatarKinematics.Pose p=AvatarMotionEngine.pose(
                        m,i/100f,AvatarBodySpec.DEFAULT);
                if(m!=AvatarMotionEngine.Motion.BURPEE)
                    assertEquals(0f,p.farAnkle.distance(start.farAnkle),EPS);
                if(m!=AvatarMotionEngine.Motion.MARCH
                        &&m!=AvatarMotionEngine.Motion.KNEE_CURL
                        &&m!=AvatarMotionEngine.Motion.HIP_EXTENSION_STAND
                        &&m!=AvatarMotionEngine.Motion.BURPEE)
                    assertEquals(0f,p.ankle.distance(start.ankle),EPS);
            }
        }
        AvatarKinematics.Pose raised=AvatarMotionEngine.pose(
                AvatarMotionEngine.Motion.MARCH,.47f,AvatarBodySpec.DEFAULT);
        assertTrue(raised.ankle.y<360f);
    }

    @Test public void standingHipExtensionHasFreeRearFootAndPinnedSupport(){
        AvatarPose first=AvatarMotionEngine.pose(
                AvatarMotionEngine.Motion.HIP_EXTENSION_STAND,0f,AvatarBodySpec.DEFAULT);
        AvatarPose center=AvatarMotionEngine.pose(
                AvatarMotionEngine.Motion.HIP_EXTENSION_STAND,.49f,AvatarBodySpec.DEFAULT);
        assertTrue(center.ankle.x<first.ankle.x-23f);
        assertTrue(center.ankle.y<first.ankle.y-16f);
        assertEquals(0f,first.farAnkle.distance(center.farAnkle),EPS);
        assertEquals(AvatarContactFrame.Ground.NONE,AvatarContactFrame.forSide(
                AvatarMotionEngine.Motion.HIP_EXTENSION_STAND,.49f).leftFoot);
        assertEquals(AvatarContactFrame.Ground.SOLE,AvatarContactFrame.forSide(
                AvatarMotionEngine.Motion.HIP_EXTENSION_STAND,.49f).rightFoot);
    }

    @Test public void eachMotionStartsAndFinishesWithoutPoseJump(){
        for(AvatarMotionEngine.Motion m:AvatarMotionEngine.Motion.values()){
            AvatarKinematics.Pose start=AvatarMotionEngine.pose(
                    m,0f,AvatarBodySpec.DEFAULT);
            AvatarKinematics.Pose end=AvatarMotionEngine.pose(
                    m,1f,AvatarBodySpec.DEFAULT);
            assertEquals(0f,start.hip.distance(end.hip),EPS);
            assertEquals(0f,start.knee.distance(end.knee),EPS);
            assertEquals(0f,start.elbow.distance(end.elbow),EPS);
        }
    }
    @Test public void everyMotionStartsAndFinishesWithExtendedLimbs(){
        AvatarBodySpec spec=AvatarBodySpec.DEFAULT;
        float arm=spec.upperArm+spec.forearm;
        float leg=spec.upperLeg+spec.lowerLeg;
        for(AvatarMotionEngine.Motion m:AvatarMotionEngine.Motion.values()){
            float[] endpoints=m==AvatarMotionEngine.Motion.BURPEE
                    ?new float[]{0f,1f}:new float[]{0f,.03f,.94f,1f};
            for(float phase:endpoints){
                AvatarPose p=AvatarMotionEngine.pose(m,phase,spec);
                assertEquals("near elbow end "+m,arm,
                        p.shoulder.distance(p.wrist),.07f);
                assertEquals("far elbow end "+m,arm,
                        p.farShoulder.distance(p.farWrist),.07f);
                if(m!=AvatarMotionEngine.Motion.LUNGE){
                    assertTrue("near knee neutral "+m,
                            p.hip.distance(p.ankle)>leg-.55f);
                    assertTrue("far knee neutral "+m,
                            p.farHip.distance(p.farAnkle)>leg-.55f);
                }
                assertTrue("rear shoulder must be lower in profile "+m,
                        p.farShoulder.y>p.shoulder.y+8f);
            }
        }
    }

    @Test public void armRaiseHasCompleteOverheadArcAtPeak(){
        AvatarBodySpec spec=AvatarBodySpec.DEFAULT;
        AvatarPose start=AvatarMotionEngine.pose(
                AvatarMotionEngine.Motion.ARM_RAISE,0f,spec);
        AvatarPose peak=AvatarMotionEngine.pose(
                AvatarMotionEngine.Motion.ARM_RAISE,.49f,spec);
        assertTrue(start.wrist.y>start.shoulder.y+115f);
        assertTrue(peak.wrist.y<peak.shoulder.y-115f);
        assertTrue(peak.farWrist.y<peak.farShoulder.y-115f);
        assertTrue(peak.wrist.distance(start.wrist)>220f);
        AvatarMotionEngine.assertSkeleton(peak,spec,.07f);
    }

    @Test public void allExerciseLabelsExistAndNullIsRejected(){
        assertEquals(9,AvatarMotionEngine.Motion.values().length);
        for(AvatarMotionEngine.Motion m:AvatarMotionEngine.Motion.values())
            assertTrue(m.label.length()>4);
        assertThrows(IllegalArgumentException.class,()->
                AvatarMotionEngine.pose(null,0f,AvatarBodySpec.DEFAULT));
    }
}
