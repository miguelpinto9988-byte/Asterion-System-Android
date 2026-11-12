package pe.asterionsystem.app;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Regression against the V10 reversed-shoe defect. The toe-contacting
 * sneaker is not a rigid continuation of the shin in a prone projection.
 */
public final class AvatarV11OrientationTest {
    private static final AvatarBodySpec SPEC=AvatarBodySpec.DEFAULT;

    @Test public void toesPointTowardsHeadNotAwayFromItFor361Frames(){
        for(int i=0;i<=360;i++){
            AvatarMultiViewKinematics.Pose p=
                    AvatarMultiViewKinematics.diamondTop(i/360f,SPEC);
            AvatarFootOrientation.FootProjection left=
                    AvatarFootOrientation.proneTop(p,AvatarFootOrientation.Side.LEFT);
            AvatarFootOrientation.FootProjection right=
                    AvatarFootOrientation.proneTop(p,AvatarFootOrientation.Side.RIGHT);
            assertTrue("left toe faces head",left.toe.y<left.ankle.y);
            assertTrue("right toe faces head",right.toe.y<right.ankle.y);
            assertTrue("heel is opposite end",left.heel.y>left.ankle.y);
            assertTrue("heel is opposite end",right.heel.y>right.ankle.y);
            assertEquals(AvatarFootOrientation.Contact.TOE,left.contact);
            assertEquals(AvatarOrientationRules.ShoeSurface.UPPER,left.surface);
            assertEquals(AvatarOrientationRules.ShoeSurface.UPPER,right.surface);
            assertEquals("shoe axes parallel",left.angleDegrees,
                    right.angleDegrees,.005f);
            assertEquals("no divergent X displacement",
                    left.toe.x-left.ankle.x,
                    right.toe.x-right.ankle.x,.005f);
            assertEquals(AvatarFootOrientation.TOE_REACH,
                    left.toe.distance(left.ankle),.005f);
            assertEquals(AvatarFootOrientation.HEEL_REACH,
                    right.heel.distance(right.ankle),.005f);
        }
    }

    @Test public void sharedFramesKeepLengthsAndPositionsForAllFrontCameras(){
        for(int i=0;i<=120;i++){
            float t=i/120f;
            for(AvatarMultiViewKinematics.ViewMode v:new
                    AvatarMultiViewKinematics.ViewMode[]{
                    AvatarMultiViewKinematics.ViewMode.FRONT,
                    AvatarMultiViewKinematics.ViewMode.FRONT_SQUAT,
                    AvatarMultiViewKinematics.ViewMode.FRONT_JACKS,
                    AvatarMultiViewKinematics.ViewMode.FRONT_ELBOW,
                    AvatarMultiViewKinematics.ViewMode.TOP}){
                AvatarMultiViewKinematics.Pose p;
                switch(v){
                    case FRONT:p=AvatarMultiViewKinematics.frontArms(t,SPEC);break;
                    case FRONT_SQUAT:p=AvatarMultiViewKinematics.frontWideSquat(t,SPEC);break;
                    case FRONT_JACKS:p=AvatarMultiViewKinematics.frontJumpingJack(t,SPEC);break;
                    case FRONT_ELBOW:p=AvatarMultiViewKinematics.frontElbowFlex(t,SPEC);break;
                    case TOP:p=AvatarMultiViewKinematics.diamondTop(t,SPEC);break;
                    default:throw new AssertionError();
                }
                AvatarSkeletonFrame frame=AvatarSkeletonFrame.fromCamera(p,v,SPEC);
                assertEquals(p.head.x,frame.joint(AvatarSkeletonFrame.Joint.HEAD).x,.001f);
                assertEquals(p.leftAnkle.y,
                        frame.joint(AvatarSkeletonFrame.Joint.LEFT_ANKLE).y,.001f);
                assertEquals(v==AvatarMultiViewKinematics.ViewMode.TOP
                                ?AvatarOrientationRules.Camera.TOP
                                :AvatarOrientationRules.Camera.FRONT,frame.camera);
            }
        }
    }

    @Test public void sideAllMovementsUseTheSameAnatomicalNames(){
        for(AvatarMotionEngine.Motion m:AvatarMotionEngine.Motion.values()){
            for(int i=0;i<=60;i++){
                float t=i/60f;
                AvatarPose p=AvatarMotionEngine.pose(m,t,SPEC);
                AvatarSkeletonFrame f=AvatarSkeletonFrame.fromSide(p,SPEC);
                assertEquals(AvatarOrientationRules.Camera.SIDE,f.camera);
                assertEquals(p.neck.x,
                        f.joint(AvatarSkeletonFrame.Joint.NECK).x,.001f);
                assertEquals(p.farKnee.y,
                        f.joint(AvatarSkeletonFrame.Joint.RIGHT_KNEE).y,.001f);
            }
        }
    }

    @Test public void frontalAndTopUseTheSameProportionsObject(){
        AvatarMultiViewKinematics.Pose front=
                AvatarMultiViewKinematics.frontArms(.48f,SPEC);
        AvatarMultiViewKinematics.Pose top=
                AvatarMultiViewKinematics.diamondTop(.48f,SPEC);
        assertSame(SPEC,AvatarSkeletonFrame.fromCamera(
                front,AvatarMultiViewKinematics.ViewMode.FRONT,SPEC).proportions);
        assertSame(SPEC,AvatarSkeletonFrame.fromCamera(
                top,AvatarMultiViewKinematics.ViewMode.TOP,SPEC).proportions);
    }

    @Test public void negativeAndDegenerateAxesRejected(){
        assertThrows(IllegalArgumentException.class,()->
                AvatarFootOrientation.proneTop(null,AvatarFootOrientation.Side.LEFT));
        assertThrows(IllegalArgumentException.class,()->
                AvatarFootOrientation.proneTop(
                        AvatarMultiViewKinematics.diamondTop(0f,SPEC),null));
        assertThrows(IllegalArgumentException.class,()->
                AvatarSkeletonFrame.fromSide(null,SPEC));
        assertThrows(IllegalArgumentException.class,()->
                AvatarSkeletonFrame.fromCamera(
                        AvatarMultiViewKinematics.diamondTop(0f,SPEC),
                        AvatarMultiViewKinematics.ViewMode.SIDE,SPEC));
    }
}
