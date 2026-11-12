package pe.asterionsystem.app;

/**
 * Fail-fast geometric invariants. These are stylized rig safety limits,
 * not diagnostic human joint-angle limits.
 */
public final class AvatarPoseGuard {
    private AvatarPoseGuard(){}
    public static void requireLink(AvatarKinematics.Point a,AvatarKinematics.Point b){
        if(a==null||b==null||!Float.isFinite(a.x)||!Float.isFinite(a.y)||
                !Float.isFinite(b.x)||!Float.isFinite(b.y)||
                a.distance(b)<.001f)
            throw new IllegalArgumentException("Invalid or disconnected bone");
    }
    public static void requireWidth(float width){
        if(!Float.isFinite(width)||width<1f||width>150f)
            throw new IllegalArgumentException("Unsafe joint width");
    }
    /** Reject degenerate, hyper-extended projected hinges. */
    public static float hingeAngle(AvatarKinematics.Point root,
                                   AvatarKinematics.Point hinge,
                                   AvatarKinematics.Point tip){
        requireLink(root,hinge);requireLink(hinge,tip);
        float ax=root.x-hinge.x,ay=root.y-hinge.y;
        float bx=tip.x-hinge.x,by=tip.y-hinge.y;
        double cos=(ax*bx+ay*by)/
                (root.distance(hinge)*hinge.distance(tip));
        float degrees=(float)Math.toDegrees(Math.acos(
                Math.max(-1d,Math.min(1d,cos))));
        if(!Float.isFinite(degrees)||degrees<0f||degrees>180f)
            throw new IllegalArgumentException("Invalid projected hinge angle");
        return degrees;
    }
    public static void requireRig(AvatarSkeletonFrame frame){
        if(frame==null)throw new IllegalArgumentException("Frame required");
        for(AvatarSkeletonFrame.Joint[] triple:new AvatarSkeletonFrame.Joint[][]{
            {AvatarSkeletonFrame.Joint.LEFT_SHOULDER,AvatarSkeletonFrame.Joint.LEFT_ELBOW,AvatarSkeletonFrame.Joint.LEFT_WRIST},
            {AvatarSkeletonFrame.Joint.RIGHT_SHOULDER,AvatarSkeletonFrame.Joint.RIGHT_ELBOW,AvatarSkeletonFrame.Joint.RIGHT_WRIST},
            {AvatarSkeletonFrame.Joint.LEFT_HIP,AvatarSkeletonFrame.Joint.LEFT_KNEE,AvatarSkeletonFrame.Joint.LEFT_ANKLE},
            {AvatarSkeletonFrame.Joint.RIGHT_HIP,AvatarSkeletonFrame.Joint.RIGHT_KNEE,AvatarSkeletonFrame.Joint.RIGHT_ANKLE}
        })hingeAngle(frame.joint(triple[0]),frame.joint(triple[1]),frame.joint(triple[2]));
        requireLink(frame.joint(AvatarSkeletonFrame.Joint.NECK),
                    frame.joint(AvatarSkeletonFrame.Joint.HEAD));
    }
}
