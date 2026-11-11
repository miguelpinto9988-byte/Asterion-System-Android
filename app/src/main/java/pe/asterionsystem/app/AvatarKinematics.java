package pe.asterionsystem.app;

/**
 * Geometry for a restrained 2D educational squat illustration.
 *
 * Joint coordinates are calculated from fixed segment lengths and a continuous
 * pelvis path, with both planted ankles immobile. This is an authored animation,
 * not motion-capture data, medical coaching or an exercise-form assessment.
 * Independent of Android so geometry can be tested in JVM tests.
 */
public final class AvatarKinematics {
    public static final float UPPER_LEG=94f;
    public static final float LOWER_LEG=96f;
    public static final float TORSO=118f;
    public static final float UPPER_ARM=58f;
    public static final float FOREARM=63f;
    private AvatarKinematics() {}

    public static final class Point {
        public final float x,y;
        public Point(float x,float y) {this.x=x;this.y=y;}
        public float distance(Point p) {
            return (float)Math.hypot(x-p.x,y-p.y);
        }
    }

    /** Compatibility alias; new renderer consumes AvatarPose. */
    public static final class Pose extends AvatarPose {
        Pose(Point ankle,Point farAnkle,Point knee,Point farKnee,
             Point hip,Point farHip,Point shoulder,Point farShoulder,
             Point neck,Point head,Point elbow,Point wrist,
             Point farElbow,Point farWrist) {
            super(ankle,farAnkle,knee,farKnee,hip,farHip,shoulder,
                    farShoulder,neck,head,elbow,wrist,farElbow,farWrist);
        }
    }

    public static Pose squat(float depth) {
        return squat(depth,AvatarBodySpec.DEFAULT);
    }

    /** Testable pose calculation with explicit virtual body geometry. */
    public static Pose squat(float depth,AvatarBodySpec spec) {
        if(spec==null)throw new IllegalArgumentException("BodySpec obligatorio");
        final float s=smooth(clamp(depth));
        final Point ankle=new Point(182f,364f);
        final Point farAnkle=new Point(170f,363f);
        final Point hip=new Point(184f-45f*s,174.4f+82f*s);
        final Point farHip=new Point(hip.x-10f,hip.y-1f);
        AvatarRigConstraints.TwoBone nearLeg=AvatarRigConstraints.twoBone(
                hip,ankle,spec.upperLeg,spec.lowerLeg,-1,0f,170f);
        AvatarRigConstraints.TwoBone farLeg=AvatarRigConstraints.twoBone(
                farHip,farAnkle,spec.upperLeg,spec.lowerLeg,-1,0f,170f);
        if(nearLeg.targetLimited||farLeg.targetLimited)
            throw new IllegalArgumentException("Proporciones incompatibles con apoyo de pies");
        Point knee=nearLeg.joint,farKnee=farLeg.joint;

        float trunkTilt=0.10f+0.43f*s;
        Point shoulder=new Point(hip.x+(float)Math.sin(trunkTilt)*spec.torso,
                hip.y-(float)Math.cos(trunkTilt)*spec.torso);
        Point farShoulder=new Point(shoulder.x-6f,shoulder.y+10f);
        Point neck=new Point(shoulder.x+4f,shoulder.y-6f);
        Point head=new Point(neck.x+4f,neck.y-27f);

        // One target per wrist, with analytical two-bone IK for both elbows.
        // Segment lengths stay fixed even when the wrists travel forward.
        float fullArmReach=spec.upperArm+spec.forearm-.015f;
        // Fully extended arms at the beginning and end of the cycle; rotate
        // the wrist target about the shoulder rather than shortening the chord.
        Point desiredWrist=wristOnArc(shoulder,fullArmReach,
                .055f+1.28f*s);
        Point desiredFarWrist=wristOnArc(farShoulder,fullArmReach,
                .015f+1.18f*s);
        AvatarRigConstraints.TwoBone nearArm=AvatarRigConstraints.twoBone(
                shoulder,desiredWrist,spec.upperArm,spec.forearm,1,0f,170f);
        AvatarRigConstraints.TwoBone farArm=AvatarRigConstraints.twoBone(
                farShoulder,desiredFarWrist,spec.upperArm,spec.forearm,1,0f,170f);
        Point wrist=nearArm.end,farWrist=farArm.end;
        Point elbow=nearArm.joint,farElbow=farArm.joint;
        return new Pose(ankle,farAnkle,knee,farKnee,hip,farHip,
                shoulder,farShoulder,neck,head,elbow,wrist,
                farElbow,farWrist);
    }

    /**
     * Planar two-bone IK; select the forward knee. Distances are clamped to
     * prevent NaN if later poses or proportions become unreachable.
     */
    /**
     * Compatibility API, now backed by the shared fixed-length 2-link solver.
     * Bend-side -1 selects the same forward-facing knee as before.
     */
    public static Point kneeForward(Point hip,Point ankle,float femur,float tibia) {
        if(!Float.isFinite(femur)||!Float.isFinite(tibia)||femur<=0||tibia<=0)
            throw new IllegalArgumentException("Longitudes positivas y finitas");
        float dx=ankle.x-hip.x,dy=ankle.y-hip.y;
        float raw=(float)Math.hypot(dx,dy);
        if(!Float.isFinite(raw))throw new IllegalArgumentException("Anclaje inválido");
        return AvatarRigConstraints.twoBone(hip,ankle,femur,tibia,-1,0f,170f).joint;
    }

    /** Compatible name for original arm IK, now using shared fixed lengths. */
    public static Point elbowBelow(Point shoulder,Point wrist,
                                   float upperArm,float forearm) {
        return AvatarRigConstraints.twoBone(shoulder,wrist,upperArm,forearm,
                1,0f,170f).joint;
    }

    public static float depthAtPhase(float phase) {
        float t=clamp(phase);
        if(t<0.08f)return 0f;
        if(t<0.42f)return smooth((t-0.08f)/0.34f);
        if(t<0.55f)return 1f;
        if(t<0.92f)return 1f-smooth((t-0.55f)/0.37f);
        return 0f;
    }

    private static float smooth(float t) {
        t=clamp(t);
        return t*t*(3f-2f*t);
    }
    private static float clamp(float t) {
        if(Float.isNaN(t))return 0f;
        return Math.max(0f,Math.min(1f,t));
    }
    /** A circular wrist target keeps a straight-arm silhouette in profile. */
    static Point wristOnArc(Point shoulder,float reach,float angle) {
        if(shoulder==null||!Float.isFinite(reach)||reach<=0f
                ||!Float.isFinite(angle))
            throw new IllegalArgumentException("Arm trajectory must be finite");
        return new Point(shoulder.x+(float)Math.sin(angle)*reach,
                shoulder.y+(float)Math.cos(angle)*reach);
    }

    private static Point lerp(Point a,Point b,float t) {
        return new Point(a.x+(b.x-a.x)*t,a.y+(b.y-a.y)*t);
    }
}
