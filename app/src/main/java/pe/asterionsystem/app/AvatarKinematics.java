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
        final Point hip=new Point(184f-45f*s,190f+69f*s);
        final Point farHip=new Point(hip.x-10f,hip.y-3f);
        Point knee=kneeForward(hip,ankle,spec.upperLeg,spec.lowerLeg);
        Point farKnee=kneeForward(farHip,farAnkle,spec.upperLeg,spec.lowerLeg);

        float trunkTilt=0.10f+0.43f*s;
        Point shoulder=new Point(hip.x+(float)Math.sin(trunkTilt)*spec.torso,
                hip.y-(float)Math.cos(trunkTilt)*spec.torso);
        Point farShoulder=new Point(shoulder.x-9f,shoulder.y+3f);
        Point neck=new Point(shoulder.x+3f,shoulder.y-9f);
        Point head=new Point(neck.x+5f,neck.y-29f);

        // One target per wrist, with analytical two-bone IK for both elbows.
        // Segment lengths stay fixed even when the wrists travel forward.
        Point wrist=lerp(new Point(shoulder.x+4f,shoulder.y+107f),
                new Point(shoulder.x+105f,shoulder.y+23f),s);
        Point farWrist=lerp(new Point(farShoulder.x-11f,farShoulder.y+101f),
                new Point(farShoulder.x+102f,farShoulder.y+19f),s);
        Point elbow=elbowBelow(shoulder,wrist,spec.upperArm,spec.forearm);
        Point farElbow=elbowBelow(farShoulder,farWrist,spec.upperArm,spec.forearm);
        return new Pose(ankle,farAnkle,knee,farKnee,hip,farHip,
                shoulder,farShoulder,neck,head,elbow,wrist,
                farElbow,farWrist);
    }

    /**
     * Planar two-bone IK; select the forward knee. Distances are clamped to
     * prevent NaN if later poses or proportions become unreachable.
     */
    public static Point kneeForward(Point hip,Point ankle,float femur,float tibia) {
        if(!Float.isFinite(femur)||!Float.isFinite(tibia)||femur<=0 || tibia<=0)
            throw new IllegalArgumentException("Longitudes positivas y finitas");
        float dx=ankle.x-hip.x,dy=ankle.y-hip.y;
        float raw=(float)Math.hypot(dx,dy);
        float d=Math.max(Math.abs(femur-tibia)+0.001f,
                Math.min(femur+tibia-0.001f,raw));
        float ux=raw<0.0001f?0f:dx/raw;
        float uy=raw<0.0001f?1f:dy/raw;
        float a=(femur*femur-tibia*tibia+d*d)/(2f*d);
        float side=(float)Math.sqrt(Math.max(0f,femur*femur-a*a));
        float midX=hip.x+ux*a,midY=hip.y+uy*a;
        Point k1=new Point(midX+uy*side,midY-ux*side);
        Point k2=new Point(midX-uy*side,midY+ux*side);
        return k1.x>=k2.x?k1:k2;
    }

    /**
     * Two-segment illustrative arm: the elbow follows the lower branch of
     * the circle intersection continuously, rather than independent keyframes.
     * This preserves lengths but does not represent a clinical movement guide.
     */
    public static Point elbowBelow(Point shoulder,Point wrist,
                                   float upperArm,float forearm) {
        if(!Float.isFinite(upperArm) || !Float.isFinite(forearm)
                || upperArm<=0f || forearm<=0f) {
            throw new IllegalArgumentException("Longitudes positivas y finitas");
        }
        float dx=wrist.x-shoulder.x,dy=wrist.y-shoulder.y;
        float raw=(float)Math.hypot(dx,dy);
        float d=Math.max(Math.abs(upperArm-forearm)+.001f,
                Math.min(upperArm+forearm-.001f,raw));
        float ux=raw<.0001f?0f:dx/raw;
        float uy=raw<.0001f?1f:dy/raw;
        float along=(upperArm*upperArm-forearm*forearm+d*d)/(2f*d);
        float bend=(float)Math.sqrt(Math.max(0f,upperArm*upperArm-along*along));
        return new Point(shoulder.x+ux*along-uy*bend,
                shoulder.y+uy*along+ux*bend);
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
    private static Point lerp(Point a,Point b,float t) {
        return new Point(a.x+(b.x-a.x)*t,a.y+(b.y-a.y)*t);
    }
}
