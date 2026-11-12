package pe.asterionsystem.app;

import static org.junit.Assert.*;
import org.junit.Test;

/** Regression gates: no declared support point drifts off its ground anchor. */
public final class AvatarFrameSafetyTest {
    private static final AvatarBodySpec BODY=AvatarBodySpec.DEFAULT;

    @Test public void everySideExerciseKeepsPlantedContactsOnFloor(){
        for(AvatarMotionEngine.Motion motion:AvatarMotionEngine.Motion.values()){
            for(int i=0;i<=240;i++){
                float t=i/240f;
                AvatarPose pose=AvatarMotionEngine.pose(motion,t,BODY);
                AvatarContactFrame c=AvatarContactFrame.forSide(motion,t);
                AvatarFrameSafety.requireSide(pose,c);
                AvatarSkeletonFrame skeleton=AvatarSkeletonFrame.fromSide(pose,BODY);
                assertEquals(pose.elbow.x,
                        skeleton.joint(AvatarSkeletonFrame.Joint.LEFT_ELBOW).x,.0001f);
                assertEquals(pose.wrist.y,
                        skeleton.joint(AvatarSkeletonFrame.Joint.LEFT_WRIST).y,.0001f);
                assertEquals(pose.knee.x,
                        skeleton.joint(AvatarSkeletonFrame.Joint.LEFT_KNEE).x,.0001f);
            }
        }
    }

    @Test public void everyMultiviewExerciseKeepsFeetAndPalmsBounded(){
        for(int i=0;i<=240;i++){
            float t=i/240f;
            for(AvatarMultiViewKinematics.ViewMode view:
                    new AvatarMultiViewKinematics.ViewMode[]{
                    AvatarMultiViewKinematics.ViewMode.FRONT,
                    AvatarMultiViewKinematics.ViewMode.FRONT_SQUAT,
                    AvatarMultiViewKinematics.ViewMode.FRONT_JACKS,
                    AvatarMultiViewKinematics.ViewMode.FRONT_ELBOW,
                    AvatarMultiViewKinematics.ViewMode.TOP}){
                AvatarMultiViewKinematics.Pose p;
                switch(view){
                    case FRONT:p=AvatarMultiViewKinematics.frontArms(t,BODY);break;
                    case FRONT_SQUAT:p=AvatarMultiViewKinematics.frontWideSquat(t,BODY);break;
                    case FRONT_JACKS:p=AvatarMultiViewKinematics.frontJumpingJack(t,BODY);break;
                    case FRONT_ELBOW:p=AvatarMultiViewKinematics.frontElbowFlex(t,BODY);break;
                    case TOP:p=AvatarMultiViewKinematics.diamondTop(t,BODY);break;
                    default:throw new AssertionError("Missing view");
                }
                AvatarContactFrame c=AvatarContactFrame.forCamera(view,t);
                AvatarFrameSafety.requireCamera(p,view,c);
                AvatarSkeletonFrame sk=AvatarSkeletonFrame.fromCamera(p,view,BODY);
                assertEquals(p.leftElbow.y,
                        sk.joint(AvatarSkeletonFrame.Joint.LEFT_ELBOW).y,.0001f);
                assertEquals(p.rightElbow.x,
                        sk.joint(AvatarSkeletonFrame.Joint.RIGHT_ELBOW).x,.0001f);
            }
        }
    }

    @Test public void detachedSideFootIsRejectedBeforeCanvasDraw(){
        AvatarKinematics.Pose p=AvatarKinematics.squat(.34f,BODY);
        AvatarKinematics.Pose broken=new AvatarKinematics.Pose(
                new AvatarKinematics.Point(p.ankle.x,p.ankle.y-30f),
                p.farAnkle,p.knee,p.farKnee,p.hip,p.farHip,
                p.shoulder,p.farShoulder,p.neck,p.head,p.elbow,p.wrist,
                p.farElbow,p.farWrist);
        AvatarContactFrame contact=AvatarContactFrame.forSide(
                AvatarMotionEngine.Motion.SQUAT,.34f);
        assertThrows(IllegalStateException.class,()->
                AvatarFrameSafety.requireSide(broken,contact));
        assertThrows(IllegalArgumentException.class,()->
                AvatarFrameSafety.requireSide(null,contact));
    }

    @Test public void invalidCameraAndContactMustFailFast(){
        AvatarMultiViewKinematics.Pose top=
                AvatarMultiViewKinematics.diamondTop(.49f,BODY);
        assertThrows(IllegalArgumentException.class,()->
                AvatarFrameSafety.requireCamera(top,
                        AvatarMultiViewKinematics.ViewMode.SIDE,
                        AvatarContactFrame.forCamera(
                                AvatarMultiViewKinematics.ViewMode.TOP,.49f)));
        assertThrows(IllegalArgumentException.class,()->
                AvatarFrameSafety.requireCamera(top,
                        AvatarMultiViewKinematics.ViewMode.TOP,null));
    }
}
