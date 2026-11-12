package pe.asterionsystem.app;

import static org.junit.Assert.*;
import org.junit.Test;

/** Both non-lateral cameras use independent scene poses and constant-length IK. */
public final class AvatarMultiViewKinematicsTest {
    private static final float EPS=.08f;
    @Test public void frontRaisesBothArmsWhileShouldersStayLevel() {
        AvatarBodySpec spec=AvatarBodySpec.DEFAULT;
        AvatarMultiViewKinematics.Pose first=
                AvatarMultiViewKinematics.frontArms(0f,spec);
        AvatarMultiViewKinematics.Pose peak=
                AvatarMultiViewKinematics.frontArms(.49f,spec);
        assertEquals(first.leftShoulder.y,first.rightShoulder.y,EPS);
        assertEquals(peak.leftShoulder.y,peak.rightShoulder.y,EPS);
        assertTrue(first.leftWrist.y>first.leftShoulder.y+110f);
        assertTrue(peak.leftWrist.y<peak.leftShoulder.y-110f);
        assertTrue(peak.rightWrist.y<peak.rightShoulder.y-110f);
        assertEquals(first.leftAnkle.x,peak.leftAnkle.x,EPS);
        assertEquals(first.rightAnkle.y,peak.rightAnkle.y,EPS);
    }
    @Test public void allFramesPreserveProjectedArmsAndContinuousMotion() {
        AvatarBodySpec spec=AvatarBodySpec.DEFAULT;
        for(AvatarMultiViewKinematics.ViewMode camera:
                new AvatarMultiViewKinematics.ViewMode[]{
                    AvatarMultiViewKinematics.ViewMode.FRONT,
                    AvatarMultiViewKinematics.ViewMode.TOP}) {
            AvatarMultiViewKinematics.Pose previous=null;
            for(int i=0;i<=240;i++) {
                AvatarMultiViewKinematics.Pose pose=
                        camera==AvatarMultiViewKinematics.ViewMode.FRONT
                        ?AvatarMultiViewKinematics.frontArms(i/240f,spec)
                        :AvatarMultiViewKinematics.diamondTop(i/240f,spec);
                pose.assertArms(EPS);
                if(previous!=null) {
                    assertTrue(pose.leftElbow.distance(previous.leftElbow)<7f);
                    assertTrue(pose.rightElbow.distance(previous.rightElbow)<7f);
                }
                previous=pose;
            }
        }
    }
    @Test public void topCameraShowsGroundFacingPersonAndPairedFixedHands(){
        AvatarBodySpec spec=AvatarBodySpec.DEFAULT;
        AvatarMultiViewKinematics.Pose start=
                AvatarMultiViewKinematics.diamondTop(0f,spec);
        AvatarMultiViewKinematics.Pose depth=
                AvatarMultiViewKinematics.diamondTop(.48f,spec);
        assertTrue(start.head.y<start.leftShoulder.y);
        assertTrue(start.leftShoulder.x<start.rightShoulder.x);
        assertTrue(start.leftWrist.x<start.rightWrist.x);
        assertTrue(start.rightWrist.x-start.leftWrist.x<30f);
        assertEquals(start.leftWrist.x,depth.leftWrist.x,EPS);
        assertEquals(start.rightWrist.y,depth.rightWrist.y,EPS);
        assertEquals(start.leftAnkle.y,depth.leftAnkle.y,EPS);
        assertEquals(start.rightAnkle.x,depth.rightAnkle.x,EPS);
    }
    @Test public void wideSquatIsGroundedInFrontalPlaneWithOpposedKnees(){
        AvatarBodySpec spec=AvatarBodySpec.DEFAULT;
        AvatarMultiViewKinematics.Pose neutral=
                AvatarMultiViewKinematics.frontWideSquat(0f,spec);
        AvatarMultiViewKinematics.Pose deepest=
                AvatarMultiViewKinematics.frontWideSquat(.49f,spec);
        for(int i=0;i<=240;i++) {
            AvatarMultiViewKinematics.Pose p=
                    AvatarMultiViewKinematics.frontWideSquat(i/240f,spec);
            p.assertArms(.08f);
            AvatarRigConstraints.requireFixedLength(
                    p.leftHip,p.leftKnee,spec.upperLeg,.08f);
            AvatarRigConstraints.requireFixedLength(
                    p.leftKnee,p.leftAnkle,spec.lowerLeg,.08f);
            AvatarRigConstraints.requireFixedLength(
                    p.rightHip,p.rightKnee,spec.upperLeg,.08f);
            AvatarRigConstraints.requireFixedLength(
                    p.rightKnee,p.rightAnkle,spec.lowerLeg,.08f);
            assertEquals(neutral.leftAnkle.x,p.leftAnkle.x,.001f);
            assertEquals(neutral.rightAnkle.y,p.rightAnkle.y,.001f);
            assertEquals(p.leftShoulder.y,p.rightShoulder.y,.001f);
            assertTrue(p.leftKnee.x<p.leftHip.x);
            assertTrue(p.rightKnee.x>p.rightHip.x);
        }
        assertTrue(deepest.leftHip.y>neutral.leftHip.y+40f);
        assertTrue(deepest.rightKnee.x>neutral.rightKnee.x+10f);
        assertEquals(10,AvatarMultiViewKinematics.ViewMode.values().length);
    }

    @Test public void headAndNeckAreSeparateFromShouldersInEveryCamera(){
        for(float phase:new float[]{0f,.2f,.49f,.7f,1f}){
            AvatarMultiViewKinematics.Pose a=
                    AvatarMultiViewKinematics.frontArms(phase,AvatarBodySpec.DEFAULT);
            AvatarMultiViewKinematics.Pose b=
                    AvatarMultiViewKinematics.frontWideSquat(phase,AvatarBodySpec.DEFAULT);
            AvatarMultiViewKinematics.Pose c=
                    AvatarMultiViewKinematics.diamondTop(phase,AvatarBodySpec.DEFAULT);
            for(AvatarMultiViewKinematics.Pose p:new AvatarMultiViewKinematics.Pose[]{a,b,c}) {
                assertTrue(p.head.y<p.neck.y);
                assertTrue(p.neck.y<p.leftShoulder.y);
                assertEquals(p.leftShoulder.y,p.rightShoulder.y,.001f);
            }
        }
    }

    @Test public void wideSquatBeginsAndEndsFullyExtendedWithFixedShoes(){
        AvatarBodySpec spec=AvatarBodySpec.DEFAULT;
        AvatarMultiViewKinematics.Pose a=
                AvatarMultiViewKinematics.frontWideSquat(0f,spec);
        AvatarMultiViewKinematics.Pose b=
                AvatarMultiViewKinematics.frontWideSquat(1f,spec);
        AvatarMultiViewKinematics.Pose low=
                AvatarMultiViewKinematics.frontWideSquat(.49f,spec);
        float leg=spec.upperLeg+spec.lowerLeg;
        assertTrue(a.leftHip.distance(a.leftAnkle)>leg-.15f);
        assertTrue(a.rightHip.distance(a.rightAnkle)>leg-.15f);
        assertEquals(0f,a.leftKnee.distance(b.leftKnee),.06f);
        assertEquals(0f,a.rightKnee.distance(b.rightKnee),.06f);
        assertEquals(0f,a.leftAnkle.distance(low.leftAnkle),.001f);
        assertEquals(0f,a.rightAnkle.distance(low.rightAnkle),.001f);
        assertTrue(low.leftHip.y>a.leftHip.y+48f);
        for(int i=0;i<=300;i++){
            AvatarMultiViewKinematics.Pose p=
                    AvatarMultiViewKinematics.frontWideSquat(i/300f,spec);
            assertTrue(p.leftHip.distance(p.leftAnkle)<=leg+.07f);
            AvatarRigConstraints.requireFixedLength(
                    p.leftHip,p.leftKnee,spec.upperLeg,.09f);
            AvatarRigConstraints.requireFixedLength(
                    p.leftKnee,p.leftAnkle,spec.lowerLeg,.09f);
        }
    }

    @Test public void jumpingJacksOpenAndCloseWithIKAndVisibleFlight(){
        AvatarBodySpec spec=AvatarBodySpec.DEFAULT;
        AvatarMultiViewKinematics.Pose a=
                AvatarMultiViewKinematics.frontJumpingJack(0f,spec);
        AvatarMultiViewKinematics.Pose peak=
                AvatarMultiViewKinematics.frontJumpingJack(.5f,spec);
        AvatarMultiViewKinematics.Pose end=
                AvatarMultiViewKinematics.frontJumpingJack(1f,spec);
        AvatarMultiViewKinematics.Pose flight=
                AvatarMultiViewKinematics.frontJumpingJack(.25f,spec);
        assertTrue(a.leftWrist.y>a.leftShoulder.y+110f);
        assertTrue(peak.leftWrist.y<peak.leftShoulder.y-110f);
        assertTrue(peak.rightWrist.y<peak.rightShoulder.y-110f);
        assertTrue(peak.leftAnkle.x<a.leftAnkle.x-60f);
        assertTrue(peak.rightAnkle.x>a.rightAnkle.x+60f);
        assertTrue(flight.leftAnkle.y<a.leftAnkle.y-8f);
        assertEquals(0f,a.leftAnkle.distance(end.leftAnkle),.06f);
        assertEquals(0f,a.leftWrist.distance(end.leftWrist),.06f);
        for(int i=0;i<=300;i++){
            AvatarMultiViewKinematics.Pose p=
                    AvatarMultiViewKinematics.frontJumpingJack(i/300f,spec);
            p.assertArms(.09f);
            AvatarRigConstraints.requireFixedLength(
                    p.leftHip,p.leftKnee,spec.upperLeg,.09f);
            AvatarRigConstraints.requireFixedLength(
                    p.leftKnee,p.leftAnkle,spec.lowerLeg,.09f);
            AvatarRigConstraints.requireFixedLength(
                    p.rightHip,p.rightKnee,spec.upperLeg,.09f);
            AvatarRigConstraints.requireFixedLength(
                    p.rightKnee,p.rightAnkle,spec.lowerLeg,.09f);
        }
    }

    @Test public void visibleElbowFlexionChangesReachWithoutStretch(){
        AvatarBodySpec spec=AvatarBodySpec.DEFAULT;
        AvatarMultiViewKinematics.Pose a=
                AvatarMultiViewKinematics.frontElbowFlex(0f,spec);
        AvatarMultiViewKinematics.Pose peak=
                AvatarMultiViewKinematics.frontElbowFlex(.49f,spec);
        AvatarMultiViewKinematics.Pose end=
                AvatarMultiViewKinematics.frontElbowFlex(1f,spec);
        assertTrue(a.leftShoulder.distance(a.leftWrist)>120.8f);
        assertTrue(peak.leftShoulder.distance(peak.leftWrist)<55f);
        assertTrue(peak.rightShoulder.distance(peak.rightWrist)<55f);
        assertTrue(peak.leftElbow.x<a.leftElbow.x-10f);
        assertEquals(0f,a.leftWrist.distance(end.leftWrist),.09f);
        for(int i=0;i<=300;i++){
            AvatarMultiViewKinematics.Pose p=
                    AvatarMultiViewKinematics.frontElbowFlex(i/300f,spec);
            p.assertArms(.09f);
        }
    }

    @Test public void invalidViewParamsAreRejected(){
        assertThrows(IllegalArgumentException.class,()->
                AvatarMultiViewKinematics.frontArms(0f,null));
        assertThrows(IllegalArgumentException.class,()->
                AvatarMultiViewKinematics.diamondTop(.4f,null));
    }
}
