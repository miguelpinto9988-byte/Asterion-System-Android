package pe.asterionsystem.app;

import static org.junit.Assert.*;
import org.junit.Test;

/** Screen-space IK, world contact, camera identity and loop continuity.
 * Not an assessment of exercise form or human anatomical joint ranges. */
public final class AvatarNewFrontalMotionsTest {
    private static final AvatarBodySpec SPEC=AvatarBodySpec.DEFAULT;
    private static final float EPS=.17f;

    private AvatarMultiViewKinematics.Pose pose(
            AvatarMultiViewKinematics.ViewMode camera,float phase){
        switch(camera){
            case FRONT_LATERAL:return AvatarMultiViewKinematics.frontLateralRaise(phase,SPEC);
            case FRONT_BALANCE:return AvatarMultiViewKinematics.frontSingleLegBalance(phase,SPEC);
            case FRONT_SIDE_STEP:return AvatarMultiViewKinematics.frontSideStep(phase,SPEC);
            case FRONT_NECK_TILT:return AvatarMultiViewKinematics.frontNeckSideTilt(phase,SPEC);
            default:throw new AssertionError("Not a new frontal motion");
        }
    }

    @Test public void everyFramePreservesAllFourFixedLengthChainsAndClosesLoop(){
        for(AvatarMultiViewKinematics.ViewMode scene:new AvatarMultiViewKinematics.ViewMode[]{
                AvatarMultiViewKinematics.ViewMode.FRONT_LATERAL,
                AvatarMultiViewKinematics.ViewMode.FRONT_BALANCE,
                AvatarMultiViewKinematics.ViewMode.FRONT_SIDE_STEP,
                AvatarMultiViewKinematics.ViewMode.FRONT_NECK_TILT}){
            AvatarMultiViewKinematics.Pose start=pose(scene,0f);
            AvatarMultiViewKinematics.Pose last=null;
            for(int i=0;i<=360;i++){
                AvatarMultiViewKinematics.Pose p=pose(scene,i/360f);
                p.assertArms(EPS);
                AvatarSkeletonFrame frame=AvatarSkeletonFrame.fromCamera(p,scene,SPEC);
                AvatarPoseGuard.requireRig(frame);
                AvatarRigConstraints.requireFixedLength(p.leftHip,p.leftKnee,SPEC.upperLeg,EPS);
                AvatarRigConstraints.requireFixedLength(p.leftKnee,p.leftAnkle,SPEC.lowerLeg,EPS);
                AvatarRigConstraints.requireFixedLength(p.rightHip,p.rightKnee,SPEC.upperLeg,EPS);
                AvatarRigConstraints.requireFixedLength(p.rightKnee,p.rightAnkle,SPEC.lowerLeg,EPS);
                if(last!=null){
                    assertTrue("Left knee discontinuity "+scene,
                            p.leftKnee.distance(last.leftKnee)<5f);
                    assertTrue("Right knee discontinuity "+scene,
                            p.rightKnee.distance(last.rightKnee)<5f);
                    assertTrue("Left wrist discontinuity "+scene,
                            p.leftWrist.distance(last.leftWrist)<5f);
                }
                last=p;
            }
            assertNotNull(last);
            assertEquals(0f,start.leftWrist.distance(last.leftWrist),EPS);
            assertEquals(0f,start.leftAnkle.distance(last.leftAnkle),EPS);
            assertEquals(0f,start.rightAnkle.distance(last.rightAnkle),EPS);
        }
    }

    @Test public void lateralRaiseStopsAtShoulderHeightAndKeepsSolesFixed(){
        AvatarMultiViewKinematics.Pose a=pose(
                AvatarMultiViewKinematics.ViewMode.FRONT_LATERAL,0f);
        AvatarMultiViewKinematics.Pose peak=pose(
                AvatarMultiViewKinematics.ViewMode.FRONT_LATERAL,.49f);
        assertTrue(peak.leftWrist.x<a.leftWrist.x-105f);
        assertTrue(peak.rightWrist.x>a.rightWrist.x+105f);
        assertEquals(peak.leftShoulder.y,peak.leftWrist.y,1.5f);
        assertEquals(peak.rightShoulder.y,peak.rightWrist.y,1.5f);
        assertEquals(0f,a.leftAnkle.distance(peak.leftAnkle),.001f);
        assertEquals(0f,a.rightAnkle.distance(peak.rightAnkle),.001f);
    }

    @Test public void singleLegBalanceOnlyReleasesTheLeftSole(){
        AvatarMultiViewKinematics.Pose a=pose(
                AvatarMultiViewKinematics.ViewMode.FRONT_BALANCE,0f);
        AvatarMultiViewKinematics.Pose peak=pose(
                AvatarMultiViewKinematics.ViewMode.FRONT_BALANCE,.49f);
        assertTrue(peak.leftAnkle.y<a.leftAnkle.y-50f);
        assertEquals(0f,a.rightAnkle.distance(peak.rightAnkle),.001f);
        AvatarContactFrame c=AvatarContactFrame.forCamera(
                AvatarMultiViewKinematics.ViewMode.FRONT_BALANCE,.49f);
        assertEquals(AvatarContactFrame.Ground.NONE,c.leftFoot);
        assertEquals(AvatarContactFrame.Ground.SOLE,c.rightFoot);
        AvatarContactFrame end=AvatarContactFrame.forCamera(
                AvatarMultiViewKinematics.ViewMode.FRONT_BALANCE,1f);
        assertEquals(AvatarContactFrame.Ground.SOLE,end.leftFoot);
    }

    @Test public void unloadedLateralStepReleasesOnlyMovingFoot(){
        AvatarMultiViewKinematics.Pose a=pose(
                AvatarMultiViewKinematics.ViewMode.FRONT_SIDE_STEP,0f);
        AvatarMultiViewKinematics.Pose peak=pose(
                AvatarMultiViewKinematics.ViewMode.FRONT_SIDE_STEP,.49f);
        assertTrue(peak.rightAnkle.x>a.rightAnkle.x+40f);
        assertTrue(peak.rightAnkle.y<a.rightAnkle.y-10f);
        assertEquals(0f,a.leftAnkle.distance(peak.leftAnkle),.001f);
        AvatarContactFrame c=AvatarContactFrame.forCamera(
                AvatarMultiViewKinematics.ViewMode.FRONT_SIDE_STEP,.49f);
        assertEquals(AvatarContactFrame.Ground.SOLE,c.leftFoot);
        assertEquals(AvatarContactFrame.Ground.NONE,c.rightFoot);
    }

    @Test public void cervicalTiltKeepsNeckLengthAndBothSolesFixed(){
        AvatarMultiViewKinematics.Pose a=pose(
                AvatarMultiViewKinematics.ViewMode.FRONT_NECK_TILT,0f);
        AvatarMultiViewKinematics.Pose peak=pose(
                AvatarMultiViewKinematics.ViewMode.FRONT_NECK_TILT,.49f);
        assertTrue(peak.head.x>a.head.x+5f);
        float degrees=(float)Math.toDegrees(Math.atan2(peak.head.x-peak.neck.x,
                peak.neck.y-peak.head.y));
        assertEquals(12f,degrees,.1f);
        assertEquals(27f,peak.head.distance(peak.neck),.05f);
        assertEquals(0f,a.leftAnkle.distance(peak.leftAnkle),.001f);
        assertEquals(0f,a.rightAnkle.distance(peak.rightAnkle),.001f);
        AvatarContactFrame frame=AvatarContactFrame.forCamera(
                AvatarMultiViewKinematics.ViewMode.FRONT_NECK_TILT,.49f);
        assertEquals(AvatarContactFrame.Ground.SOLE,frame.leftFoot);
        assertEquals(AvatarContactFrame.Ground.SOLE,frame.rightFoot);
    }

    @Test public void invalidInputsAreRejected(){
        assertThrows(IllegalArgumentException.class,()->
                AvatarMultiViewKinematics.frontLateralRaise(.5f,null));
        assertThrows(IllegalArgumentException.class,()->
                AvatarMultiViewKinematics.frontSingleLegBalance(.5f,null));
        assertThrows(IllegalArgumentException.class,()->
                AvatarMultiViewKinematics.frontSideStep(.5f,null));
        assertThrows(IllegalArgumentException.class,()->
                AvatarMultiViewKinematics.frontNeckSideTilt(.5f,null));
    }
}
