package pe.asterionsystem.app;

import static org.junit.Assert.*;
import org.junit.Test;

/** Ground contacts and anatomical orientation are checked before painting. */
public final class AvatarFrameSafetyTest {
    private static final AvatarBodySpec SPEC=AvatarBodySpec.DEFAULT;

    @Test public void allSixLateralMotionsStayAttachedDuringFullCycles(){
        for(AvatarMotionEngine.Motion motion:AvatarMotionEngine.Motion.values()){
            for(int i=0;i<=240;i++){
                float phase=i/240f;
                AvatarPose pose=AvatarMotionEngine.pose(motion,phase,SPEC);
                AvatarContactFrame contact=AvatarContactFrame.forSide(motion,phase);
                AvatarFrameSafety.side(pose,contact,SPEC);
            }
        }
    }

    @Test public void everyCameraKeepsFeetAndHandsAtAuthoredContact(){
        for(AvatarMultiViewKinematics.ViewMode mode:
                new AvatarMultiViewKinematics.ViewMode[]{
                AvatarMultiViewKinematics.ViewMode.FRONT,
                AvatarMultiViewKinematics.ViewMode.FRONT_SQUAT,
                AvatarMultiViewKinematics.ViewMode.FRONT_JACKS,
                AvatarMultiViewKinematics.ViewMode.FRONT_ELBOW,
                AvatarMultiViewKinematics.ViewMode.TOP}){
            for(int i=0;i<=240;i++){
                float phase=i/240f;
                AvatarMultiViewKinematics.Pose pose;
                switch(mode){
                    case FRONT:pose=AvatarMultiViewKinematics.frontArms(phase,SPEC);break;
                    case FRONT_SQUAT:pose=AvatarMultiViewKinematics.frontWideSquat(phase,SPEC);break;
                    case FRONT_JACKS:pose=AvatarMultiViewKinematics.frontJumpingJack(phase,SPEC);break;
                    case FRONT_ELBOW:pose=AvatarMultiViewKinematics.frontElbowFlex(phase,SPEC);break;
                    case TOP:pose=AvatarMultiViewKinematics.diamondTop(phase,SPEC);break;
                    default:throw new AssertionError();
                }
                AvatarFrameSafety.camera(pose,mode,
                        AvatarContactFrame.forCamera(mode,phase),SPEC);
            }
        }
    }

    @Test public void aMislabelledFlyingFootCannotBeRenderedAsPlanted(){
        AvatarMultiViewKinematics.Pose airborne=
                AvatarMultiViewKinematics.frontJumpingJack(.25f,SPEC);
        AvatarContactFrame ground=AvatarContactFrame.forCamera(
                AvatarMultiViewKinematics.ViewMode.FRONT_JACKS,0f);
        assertThrows(IllegalStateException.class,()->
                AvatarFrameSafety.camera(airborne,
                        AvatarMultiViewKinematics.ViewMode.FRONT_JACKS,
                        ground,SPEC));
    }

    @Test public void cameraMismatchAndMissingSpecsFailClosed(){
        AvatarMultiViewKinematics.Pose front=
                AvatarMultiViewKinematics.frontArms(0f,SPEC);
        assertThrows(IllegalArgumentException.class,()->
                AvatarFrameSafety.camera(front,
                        AvatarMultiViewKinematics.ViewMode.TOP,
                        AvatarContactFrame.forCamera(AvatarMultiViewKinematics.ViewMode.TOP,0f),
                        SPEC));
        assertThrows(IllegalArgumentException.class,()->
                AvatarFrameSafety.side(null,
                        AvatarContactFrame.forSide(AvatarMotionEngine.Motion.SQUAT,0f),
                        SPEC));
    }
}
