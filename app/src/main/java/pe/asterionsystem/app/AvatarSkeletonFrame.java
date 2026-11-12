package pe.asterionsystem.app;

/**
 * Canonical *anatomical names* for the side/front/overhead 2D pose snapshots.
 * All cameras pass through this immutable adapter before rendering. The 2D
 * coordinates remain authored projections; this is not a free-rotation 3D rig.
 */
public final class AvatarSkeletonFrame {
    public enum Joint {
        HEAD,NECK,LEFT_SHOULDER,RIGHT_SHOULDER,LEFT_ELBOW,RIGHT_ELBOW,
        LEFT_WRIST,RIGHT_WRIST,LEFT_HIP,RIGHT_HIP,LEFT_KNEE,RIGHT_KNEE,
        LEFT_ANKLE,RIGHT_ANKLE
    }

    private final AvatarKinematics.Point[] joints;
    public final AvatarOrientationRules.Camera camera;
    public final AvatarOrientationRules.BodyFrame orientation;
    public final AvatarBodySpec proportions;

    private AvatarSkeletonFrame(AvatarKinematics.Point[] values,
                                AvatarOrientationRules.Camera camera,
                                AvatarOrientationRules.Posture posture,
                                AvatarBodySpec proportions){
        if(values.length!=Joint.values().length||camera==null||
                posture==null||proportions==null)
            throw new IllegalArgumentException("Incomplete skeleton");
        joints=values.clone();
        this.camera=camera;
        this.orientation=AvatarOrientationRules.forPosture(posture);
        this.proportions=proportions;
        for(Joint j:Joint.values()){
            AvatarKinematics.Point p=joints[j.ordinal()];
            if(p==null||!Float.isFinite(p.x)||!Float.isFinite(p.y))
                throw new IllegalArgumentException("Missing/nonfinite joint "+j);
        }
        requireNonCollapsed();
    }

    public AvatarKinematics.Point joint(Joint j){
        if(j==null)throw new IllegalArgumentException("Joint required");
        return joints[j.ordinal()];
    }

    public static AvatarSkeletonFrame fromSide(AvatarPose p,AvatarBodySpec spec){
        if(p==null)throw new IllegalArgumentException("Side pose required");
        AvatarSkeletonFrame frame=new AvatarSkeletonFrame(new AvatarKinematics.Point[]{
                p.head,p.neck,p.shoulder,p.farShoulder,
                p.elbow,p.farElbow,p.wrist,p.farWrist,
                p.hip,p.farHip,p.knee,p.farKnee,p.ankle,p.farAnkle},
                AvatarOrientationRules.Camera.SIDE,
                AvatarOrientationRules.Posture.STANDING,spec);
        frame.requireArmLengths(1f,.15f);
        frame.requireLegLengths(1f,.15f);
        return frame;
    }

    public static AvatarSkeletonFrame fromCamera(
            AvatarMultiViewKinematics.Pose p,
            AvatarMultiViewKinematics.ViewMode view,
            AvatarBodySpec spec){
        if(p==null||view==null||view==AvatarMultiViewKinematics.ViewMode.SIDE)
            throw new IllegalArgumentException("Multiview pose and camera required");
        AvatarOrientationRules.Camera cam=view==AvatarMultiViewKinematics.ViewMode.TOP
                ?AvatarOrientationRules.Camera.TOP:AvatarOrientationRules.Camera.FRONT;
        AvatarOrientationRules.Posture posture=view==AvatarMultiViewKinematics.ViewMode.TOP
                ?AvatarOrientationRules.Posture.PRONE_GROUND_FACING
                :AvatarOrientationRules.Posture.STANDING;
        AvatarSkeletonFrame f=new AvatarSkeletonFrame(new AvatarKinematics.Point[]{
                p.head,p.neck,p.leftShoulder,p.rightShoulder,
                p.leftElbow,p.rightElbow,p.leftWrist,p.rightWrist,
                p.leftHip,p.rightHip,p.leftKnee,p.rightKnee,
                p.leftAnkle,p.rightAnkle},cam,posture,spec);
        f.requireArmLengths(view==AvatarMultiViewKinematics.ViewMode.TOP?.8f:1f,.12f);
        if(view==AvatarMultiViewKinematics.ViewMode.TOP){
            AvatarOrientationRules.requireProneTopPose(p);
            AvatarFootOrientation.requirePair(p);
        }else{
            AvatarOrientationRules.requireUprightFrontPose(p);
            f.requireLegLengths(1f,.15f);
        }
        return f;
    }

    /**
     * Shared IK contract. A top-camera forearm is projected at .80 scale
     * intentionally; it is not a different anatomical bone length.
     */
    public void requireArmLengths(float projectedScale,float tolerance){
        if(!Float.isFinite(projectedScale)||projectedScale<=0f||
                !Float.isFinite(tolerance)||tolerance<0f)
            throw new IllegalArgumentException("Length settings required");
        bone(Joint.LEFT_SHOULDER,Joint.LEFT_ELBOW,
                proportions.upperArm*projectedScale,tolerance);
        bone(Joint.LEFT_ELBOW,Joint.LEFT_WRIST,
                proportions.forearm*projectedScale,tolerance);
        bone(Joint.RIGHT_SHOULDER,Joint.RIGHT_ELBOW,
                proportions.upperArm*projectedScale,tolerance);
        bone(Joint.RIGHT_ELBOW,Joint.RIGHT_WRIST,
                proportions.forearm*projectedScale,tolerance);
    }

    public void requireLegLengths(float projectedScale,float tolerance){
        bone(Joint.LEFT_HIP,Joint.LEFT_KNEE,proportions.upperLeg*projectedScale,tolerance);
        bone(Joint.LEFT_KNEE,Joint.LEFT_ANKLE,proportions.lowerLeg*projectedScale,tolerance);
        bone(Joint.RIGHT_HIP,Joint.RIGHT_KNEE,proportions.upperLeg*projectedScale,tolerance);
        bone(Joint.RIGHT_KNEE,Joint.RIGHT_ANKLE,proportions.lowerLeg*projectedScale,tolerance);
    }

    private void bone(Joint a,Joint b,float expected,float tolerance){
        AvatarRigConstraints.requireFixedLength(joint(a),joint(b),expected,tolerance);
    }

    private void requireNonCollapsed(){
        for(Joint[] segment:new Joint[][]{
                {Joint.HEAD,Joint.NECK},
                {Joint.LEFT_SHOULDER,Joint.LEFT_HIP},
                {Joint.RIGHT_SHOULDER,Joint.RIGHT_HIP},
                {Joint.LEFT_SHOULDER,Joint.LEFT_ELBOW},
                {Joint.LEFT_ELBOW,Joint.LEFT_WRIST},
                {Joint.RIGHT_SHOULDER,Joint.RIGHT_ELBOW},
                {Joint.RIGHT_ELBOW,Joint.RIGHT_WRIST},
                {Joint.LEFT_HIP,Joint.LEFT_KNEE},
                {Joint.LEFT_KNEE,Joint.LEFT_ANKLE},
                {Joint.RIGHT_HIP,Joint.RIGHT_KNEE},
                {Joint.RIGHT_KNEE,Joint.RIGHT_ANKLE}
        }){
            if(joint(segment[0]).distance(joint(segment[1]))<.001f)
                throw new IllegalArgumentException("Collapsed link "+segment[0]);
        }
    }
}
