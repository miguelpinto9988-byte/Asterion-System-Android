package pe.asterionsystem.app;

/**
 * Runtime contact/frame validation for all authored 2D scenes. It checks
 * whether a declared ground contact is still at the renderer's fixed
 * virtual landmark; it never pretends to infer real 3D floor forces from
 * a flattened drawing. No permissions, network, disk or sensors required.
 */
public final class AvatarFrameSafety {
    private AvatarFrameSafety(){}

    private static final float SIDE_FOOT_Y=364f;
    private static final float SIDE_FAR_FOOT_Y=363f;
    private static final float FRONT_FOOT_Y=428f;
    private static final float TOP_FOOT_Y=406f;
    private static final float TOP_HAND_Y=226f;

    public static void requireSide(AvatarPose pose,
                                   AvatarContactFrame contact) {
        if(pose==null||contact==null)
            throw new IllegalArgumentException("Side pose and contact required");
        if(contact.leftFoot!=AvatarContactFrame.Ground.NONE)
            near(pose.ankle.y,SIDE_FOOT_Y,1.3f,"Side near foot floats");
        if(contact.rightFoot!=AvatarContactFrame.Ground.NONE)
            near(pose.farAnkle.y,SIDE_FAR_FOOT_Y,1.3f,"Side far foot floats");
        if(contact.leftHand==AvatarContactFrame.Ground.PALM)
            near(pose.wrist.y,376f,11f,"Side hand left detached");
        if(contact.rightHand==AvatarContactFrame.Ground.PALM)
            near(pose.farWrist.y,371f,12f,"Side hand right detached");
        if(contact.airborne&&
                (contact.leftFoot!=AvatarContactFrame.Ground.NONE||
                 contact.rightFoot!=AvatarContactFrame.Ground.NONE))
            throw new IllegalStateException("Airborne foot simultaneously grounded");
    }

    public static void requireCamera(AvatarMultiViewKinematics.Pose pose,
                                     AvatarMultiViewKinematics.ViewMode view,
                                     AvatarContactFrame contact) {
        if(pose==null||view==null||contact==null)
            throw new IllegalArgumentException("Camera, pose and contacts required");
        if(view==AvatarMultiViewKinematics.ViewMode.SIDE)
            throw new IllegalArgumentException("Side uses requireSide");
        if(view==AvatarMultiViewKinematics.ViewMode.TOP){
            contact.assertProneUpperCamera();
            near(pose.leftAnkle.y,TOP_FOOT_Y,.5f,"Prone left toe floats");
            near(pose.rightAnkle.y,TOP_FOOT_Y,.5f,"Prone right toe floats");
            near(pose.leftWrist.y,TOP_HAND_Y,.5f,"Prone left palm floats");
            near(pose.rightWrist.y,TOP_HAND_Y,.5f,"Prone right palm floats");
            AvatarFootOrientation.requirePair(pose);
            return;
        }
        if(contact.leftFoot!=AvatarContactFrame.Ground.NONE)
            near(pose.leftAnkle.y,FRONT_FOOT_Y,1.3f,"Front left sole floats");
        if(contact.rightFoot!=AvatarContactFrame.Ground.NONE)
            near(pose.rightAnkle.y,FRONT_FOOT_Y,1.3f,"Front right sole floats");
        if(contact.airborne&&
                (contact.leftFoot!=AvatarContactFrame.Ground.NONE||
                 contact.rightFoot!=AvatarContactFrame.Ground.NONE))
            throw new IllegalStateException("Unstable contact state");
    }

    private static void near(float actual,float expected,float tolerance,
                             String message){
        if(!Float.isFinite(actual)||Math.abs(actual-expected)>tolerance)
            throw new IllegalStateException(message);
    }
}
