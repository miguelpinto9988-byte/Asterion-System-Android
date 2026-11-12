package pe.asterionsystem.app;

/**
 * Renderer-time contracts. Rejects non-finite/collapsed joint configurations,
 * mismatched surface contacts and detached silhouette pivots BEFORE painting.
 *
 * The 2D scenes are illustrative and not a measurement of human biomechanics.
 * Contact positions are authored in the same scene-space coordinates as the
 * animations, so they must not be deduced from perspective alone.
 */
public final class AvatarFrameSafety {
    private static final float SIDE_FLOOR_Y=364f;
    private static final float FRONT_FLOOR_Y=428f;
    private AvatarFrameSafety(){}

    public static void side(AvatarPose pose,AvatarContactFrame contact,
                            AvatarBodySpec body){
        if(pose==null||contact==null||body==null)
            throw new IllegalArgumentException("Side pose and contact required");
        AvatarSkeletonFrame frame=AvatarSkeletonFrame.fromSide(pose,body);
        // Neck must not have detached from the proximal torso or head.
        float headNeck=frame.joint(AvatarSkeletonFrame.Joint.HEAD).distance(
                frame.joint(AvatarSkeletonFrame.Joint.NECK));
        float neckShoulder=frame.joint(AvatarSkeletonFrame.Joint.NECK).distance(
                frame.joint(AvatarSkeletonFrame.Joint.LEFT_SHOULDER));
        if(headNeck>50f||headNeck<3f||neckShoulder>40f)
            throw new IllegalStateException("Disconnected head or neck");
        requireGround(contact.leftFoot,pose.ankle.y,SIDE_FLOOR_Y,1.8f);
        requireGround(contact.rightFoot,pose.farAnkle.y,SIDE_FLOOR_Y,2.2f);
        if(contact.leftHand==AvatarContactFrame.Ground.PALM)
            requireY(pose.wrist.y,376f,1.6f,"Near hand detached from floor");
        if(contact.rightHand==AvatarContactFrame.Ground.PALM)
            requireY(pose.farWrist.y,371f,1.6f,"Far hand detached from floor");
    }

    public static void camera(AvatarMultiViewKinematics.Pose pose,
                              AvatarMultiViewKinematics.ViewMode view,
                              AvatarContactFrame contact,AvatarBodySpec body){
        if(pose==null||view==null||contact==null||body==null)
            throw new IllegalArgumentException("Camera pose and contact required");
        AvatarSkeletonFrame frame=AvatarSkeletonFrame.fromCamera(pose,view,body);
        if(view==AvatarMultiViewKinematics.ViewMode.TOP){
            contact.assertProneUpperCamera();
            AvatarFootOrientation.requirePair(pose);
            requireY(pose.leftWrist.y,226f,1f,"Left palm detached");
            requireY(pose.rightWrist.y,226f,1f,"Right palm detached");
            requireY(pose.leftAnkle.y,406f,1f,"Left toe detached");
            requireY(pose.rightAnkle.y,406f,1f,"Right toe detached");
            return;
        }
        if(frame.camera!=AvatarOrientationRules.Camera.FRONT)
            throw new IllegalArgumentException("Unexpected camera");
        requireGround(contact.leftFoot,pose.leftAnkle.y,FRONT_FLOOR_Y,1.25f);
        requireGround(contact.rightFoot,pose.rightAnkle.y,FRONT_FLOOR_Y,1.25f);
        if(contact.airborne){
            if(pose.leftAnkle.y>=FRONT_FLOOR_Y ||
                    pose.rightAnkle.y>=FRONT_FLOOR_Y)
                throw new IllegalStateException("Aerial pose has planted feet");
        }
        if(Math.abs(pose.leftShoulder.y-pose.rightShoulder.y)>.6f)
            throw new IllegalStateException("Front shoulders are not level");
    }

    private static void requireGround(AvatarContactFrame.Ground contact,
                                      float actual,float ground,float tolerance){
        if(contact==AvatarContactFrame.Ground.SOLE||
                contact==AvatarContactFrame.Ground.TOE)
            requireY(actual,ground,tolerance,"Grounded foot detached");
    }

    private static void requireY(float actual,float expected,float tolerance,
                                 String error){
        if(!Float.isFinite(actual)||Math.abs(actual-expected)>tolerance)
            throw new IllegalStateException(error);
    }
}
