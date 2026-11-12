package pe.asterionsystem.app;

import static org.junit.Assert.*;
import org.junit.Test;

/** Geometry and stability regression for every camera and authored exercise. */
public final class AvatarV13PoseGuardTest {
    private static final AvatarBodySpec SPEC=AvatarBodySpec.DEFAULT;

    @Test public void allSideMotionsKeepEveryHingeConnected(){
        for(AvatarMotionEngine.Motion motion:AvatarMotionEngine.Motion.values()){
            for(int i=0;i<=180;i++){
                float phase=i/180f;
                AvatarPose pose=AvatarMotionEngine.pose(motion,phase,SPEC);
                AvatarSkeletonFrame frame=AvatarSkeletonFrame.fromSide(pose,SPEC);
                AvatarPoseGuard.requireRig(frame);
                assertEquals(SPEC,frame.proportions);
                assertTrue(AvatarPoseGuard.hingeAngle(pose.shoulder,pose.elbow,pose.wrist)>=0f);
            }
        }
    }

    @Test public void allCameraMotionsKeepBothElbowsAndKneesAttached(){
        for(AvatarMultiViewKinematics.ViewMode mode:AvatarMultiViewKinematics.ViewMode.values()){
            if(mode==AvatarMultiViewKinematics.ViewMode.SIDE)continue;
            for(int i=0;i<=180;i++){
                float t=i/180f;
                AvatarMultiViewKinematics.Pose p;
                switch(mode){
                    case FRONT:p=AvatarMultiViewKinematics.frontArms(t,SPEC);break;
                    case FRONT_SQUAT:p=AvatarMultiViewKinematics.frontWideSquat(t,SPEC);break;
                    case FRONT_JACKS:p=AvatarMultiViewKinematics.frontJumpingJack(t,SPEC);break;
                    case FRONT_ELBOW:p=AvatarMultiViewKinematics.frontElbowFlex(t,SPEC);break;
                    case TOP:p=AvatarMultiViewKinematics.diamondTop(t,SPEC);break;
                    case FRONT_LATERAL:p=AvatarMultiViewKinematics.frontLateralRaise(t,SPEC);break;
                    case FRONT_BALANCE:p=AvatarMultiViewKinematics.frontSingleLegBalance(t,SPEC);break;
                    case FRONT_SIDE_STEP:p=AvatarMultiViewKinematics.frontSideStep(t,SPEC);break;
                    default:throw new AssertionError();
                }
                AvatarSkeletonFrame frame=AvatarSkeletonFrame.fromCamera(p,mode,SPEC);
                AvatarPoseGuard.requireRig(frame);
                assertEquals(SPEC,frame.proportions);
            }
        }
    }

    @Test public void organicJointInputsCannotCollapseOrOverflow(){
        AvatarKinematics.Point origin=new AvatarKinematics.Point(0f,0f);
        AvatarKinematics.Point hinge=new AvatarKinematics.Point(0f,40f);
        AvatarKinematics.Point tip=new AvatarKinematics.Point(15f,65f);
        assertTrue(AvatarPoseGuard.hingeAngle(origin,hinge,tip)>0f);
        assertThrows(IllegalArgumentException.class,()->
                AvatarPoseGuard.requireLink(origin,origin));
        assertThrows(IllegalArgumentException.class,()->
                AvatarPoseGuard.requireLink(origin,
                        new AvatarKinematics.Point(Float.POSITIVE_INFINITY,1f)));
        assertThrows(IllegalArgumentException.class,()->
                AvatarPoseGuard.requireWidth(Float.NaN));
        assertThrows(IllegalArgumentException.class,()->
                AvatarPoseGuard.requireWidth(0f));
        assertThrows(IllegalArgumentException.class,()->
                AvatarPoseGuard.requireWidth(999f));
    }

    @Test public void plantedContactsRemainExplicitForDiamondAndFront(){
        for(int i=0;i<=180;i++){
            float phase=i/180f;
            AvatarContactFrame top=AvatarContactFrame.forCamera(
                    AvatarMultiViewKinematics.ViewMode.TOP,phase);
            top.assertProneUpperCamera();
            AvatarFootOrientation.requirePair(
                    AvatarMultiViewKinematics.diamondTop(phase,SPEC));
            AvatarContactFrame front=AvatarContactFrame.forCamera(
                    AvatarMultiViewKinematics.ViewMode.FRONT_SQUAT,phase);
            assertEquals(AvatarContactFrame.Ground.SOLE,front.leftFoot);
            assertEquals(AvatarContactFrame.Ground.SOLE,front.rightFoot);
        }
    }
}
