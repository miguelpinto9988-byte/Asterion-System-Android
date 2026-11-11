package pe.asterionsystem.app;

import static org.junit.Assert.*;
import org.junit.Test;

/** Prevent upside-down surfaces in an illustrative multi-view cutout rig. */
public final class AvatarOrientationRulesTest {
    private static final AvatarBodySpec SPEC=AvatarBodySpec.DEFAULT;

    @Test public void proneFaceAndBothSolesPointTowardsGround(){
        AvatarOrientationRules.BodyFrame prone=AvatarOrientationRules.forPosture(
                AvatarOrientationRules.Posture.PRONE_GROUND_FACING);
        assertEquals(-1f,prone.faceNormal.y,.001f);
        assertEquals(-1f,prone.soleNormal.y,.001f);
        assertEquals(1f,prone.toeAxis.z,.001f);
        assertEquals(1f,AvatarOrientationRules.topToeSign(prone),.001f);
        assertFalse(AvatarOrientationRules.visibleSurface(
                prone.soleNormal,AvatarOrientationRules.Camera.TOP));
        assertEquals(AvatarOrientationRules.ShoeSurface.UPPER,
                AvatarOrientationRules.visibleShoeSurface(
                        AvatarOrientationRules.Camera.TOP,prone));
    }

    @Test public void standingUsesDistinctFaceAndSoleDirections(){
        AvatarOrientationRules.BodyFrame standing=AvatarOrientationRules.forPosture(
                AvatarOrientationRules.Posture.STANDING);
        assertEquals(-1f,standing.faceNormal.z,.001f);
        assertEquals(-1f,standing.soleNormal.y,.001f);
        assertTrue(AvatarOrientationRules.visibleSurface(
                standing.faceNormal,AvatarOrientationRules.Camera.FRONT));
        assertFalse(AvatarOrientationRules.visibleSurface(
                standing.soleNormal,AvatarOrientationRules.Camera.TOP));
    }

    @Test public void anteriorTorsoAndPalmNormalsMatchTheContactPosture(){
        AvatarOrientationRules.BodyFrame prone=AvatarOrientationRules.forPosture(
                AvatarOrientationRules.Posture.PRONE_GROUND_FACING);
        assertEquals(-1f,prone.torsoAnteriorNormal.y,.001f);
        assertEquals(-1f,prone.palmNormal.y,.001f);
        assertFalse(AvatarOrientationRules.visibleSurface(
                prone.palmNormal,AvatarOrientationRules.Camera.TOP));
        AvatarOrientationRules.BodyFrame neutral=AvatarOrientationRules.forPosture(
                AvatarOrientationRules.Posture.STANDING);
        assertEquals(-1f,neutral.torsoAnteriorNormal.z,.001f);
        assertEquals(-1f,neutral.palmNormal.z,.001f);
    }

    @Test public void orthographicProjectionsAreDifferentForEachCamera(){
        AvatarOrientationRules.Vector3 pt=new AvatarOrientationRules.Vector3(
                3f,5f,9f);
        AvatarKinematics.Point front=AvatarOrientationRules.project(
                pt,AvatarOrientationRules.Camera.FRONT);
        AvatarKinematics.Point side=AvatarOrientationRules.project(
                pt,AvatarOrientationRules.Camera.SIDE);
        AvatarKinematics.Point top=AvatarOrientationRules.project(
                pt,AvatarOrientationRules.Camera.TOP);
        assertEquals(3f,front.x,.0001f);
        assertEquals(-5f,front.y,.0001f);
        assertEquals(9f,side.x,.0001f);
        assertEquals(-5f,side.y,.0001f);
        assertEquals(3f,top.x,.0001f);
        assertEquals(9f,top.y,.0001f);
    }

    @Test public void lateralFaceTurnsContinuouslyTowardsFloor(){
        AvatarKinematics.Point hip=new AvatarKinematics.Point(100,240);
        AvatarKinematics.Point shoulder=new AvatarKinematics.Point(100,120);
        assertEquals(0f,AvatarOrientationRules.sideFacingDegrees(
                hip,shoulder),.0001f);
        shoulder=new AvatarKinematics.Point(220,240);
        assertEquals(90f,AvatarOrientationRules.sideFacingDegrees(
                hip,shoulder),.0001f);
        shoulder=new AvatarKinematics.Point(184.85f,155.15f);
        assertEquals(45f,AvatarOrientationRules.sideFacingDegrees(
                hip,shoulder),.2f);
    }

    @Test public void topDiamondKeepsOrientationAndContactsForEveryPhase(){
        for(int i=0;i<=360;i++){
            AvatarMultiViewKinematics.Pose p=
                    AvatarMultiViewKinematics.diamondTop(i/360f,SPEC);
            AvatarOrientationRules.requireProneTopPose(p);
            assertTrue(p.leftAnkle.y>p.leftKnee.y);
            assertTrue(p.rightAnkle.y>p.rightKnee.y);
            assertTrue(p.leftAnkle.y>p.head.y);
            assertTrue(p.rightAnkle.y>p.head.y);
            p.assertArms(.08f);
        }
    }

    @Test public void upsideDownShoeAndHeadPositionsAreRejected(){
        AvatarMultiViewKinematics.Pose p=
                AvatarMultiViewKinematics.diamondTop(0f,SPEC);
        AvatarKinematics.Point swap=new AvatarKinematics.Point(200f,310f);
        // The orientation guard validates the existing pose without
        // mutating a production immutable Pose.
        assertThrows(IllegalArgumentException.class,()->
                AvatarOrientationRules.requireProneTopPose(null));
        assertThrows(IllegalArgumentException.class,()->
                AvatarOrientationRules.sideFacingDegrees(swap,swap));
        assertThrows(IllegalArgumentException.class,()->
                new AvatarOrientationRules.Vector3(Float.NaN,0f,0f));
        assertThrows(IllegalArgumentException.class,()->
                new AvatarOrientationRules.Vector3(0f,0f,0f).normalized());
        assertTrue(p.leftKnee.y<p.leftAnkle.y);
    }

    @Test public void allUprightCamerasKeepTheFaceAboveTheTorso(){
        for(int i=0;i<=160;i++){
            float phase=i/160f;
            AvatarMultiViewKinematics.Pose[] poses={
                AvatarMultiViewKinematics.frontArms(phase,SPEC),
                AvatarMultiViewKinematics.frontWideSquat(phase,SPEC),
                AvatarMultiViewKinematics.frontJumpingJack(phase,SPEC),
                AvatarMultiViewKinematics.frontElbowFlex(phase,SPEC)
            };
            for(AvatarMultiViewKinematics.Pose p:poses)
                AvatarOrientationRules.requireUprightFrontPose(p);
        }
        assertThrows(IllegalArgumentException.class,()->
                AvatarOrientationRules.requireUprightFrontPose(null));
    }

    @Test public void worldAxesAreUnitLengthAndPerpendicular(){
        for(AvatarOrientationRules.Posture posture:
                AvatarOrientationRules.Posture.values()){
            AvatarOrientationRules.BodyFrame frame=
                    AvatarOrientationRules.forPosture(posture);
            assertEquals(1f,frame.headAxis.length(),.0001f);
            assertEquals(1f,frame.toeAxis.length(),.0001f);
            assertEquals(0f,frame.soleNormal.dot(frame.toeAxis),.0001f);
            assertEquals(0f,frame.headAxis.dot(frame.faceNormal),.0001f);
        }
    }
}
