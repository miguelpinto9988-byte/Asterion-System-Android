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
    @Test public void invalidViewParamsAreRejected(){
        assertThrows(IllegalArgumentException.class,()->
                AvatarMultiViewKinematics.frontArms(0f,null));
        assertThrows(IllegalArgumentException.class,()->
                AvatarMultiViewKinematics.diamondTop(.4f,null));
    }
}
