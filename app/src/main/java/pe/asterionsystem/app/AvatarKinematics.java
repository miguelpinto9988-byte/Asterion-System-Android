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
    private AvatarKinematics() {}

    public static final class Point {
        public final float x,y;
        public Point(float x,float y) {this.x=x;this.y=y;}
        public float distance(Point p) {
            return (float)Math.hypot(x-p.x,y-p.y);
        }
    }

    public static final class Pose {
        public final Point ankle,farAnkle,knee,farKnee,hip,farHip;
        public final Point shoulder,farShoulder,neck,head,elbow,wrist;
        public final Point farElbow,farWrist;
        Pose(Point ankle,Point farAnkle,Point knee,Point farKnee,
             Point hip,Point farHip,Point shoulder,Point farShoulder,
             Point neck,Point head,Point elbow,Point wrist,
             Point farElbow,Point farWrist) {
            this.ankle=ankle;this.farAnkle=farAnkle;
            this.knee=knee;this.farKnee=farKnee;
            this.hip=hip;this.farHip=farHip;
            this.shoulder=shoulder;this.farShoulder=farShoulder;
            this.neck=neck;this.head=head;
            this.elbow=elbow;this.wrist=wrist;
            this.farElbow=farElbow;this.farWrist=farWrist;
        }
    }

    public static Pose squat(float depth) {
        final float s=smooth(clamp(depth));
        final Point ankle=new Point(182f,364f);
        final Point farAnkle=new Point(170f,363f);
        final Point hip=new Point(184f-45f*s,190f+69f*s);
        final Point farHip=new Point(hip.x-10f,hip.y-3f);
        Point knee=kneeForward(hip,ankle,UPPER_LEG,LOWER_LEG);
        Point farKnee=kneeForward(farHip,farAnkle,UPPER_LEG,LOWER_LEG);

        float trunkTilt=0.10f+0.43f*s;
        Point shoulder=new Point(hip.x+(float)Math.sin(trunkTilt)*TORSO,
                hip.y-(float)Math.cos(trunkTilt)*TORSO);
        Point farShoulder=new Point(shoulder.x-9f,shoulder.y+3f);
        Point neck=new Point(shoulder.x+3f,shoulder.y-9f);
        Point head=new Point(neck.x+5f,neck.y-29f);

        // During the squat the forearms travel forward without snapping.
        Point elbow=lerp(new Point(shoulder.x+7f,shoulder.y+59f),
                new Point(shoulder.x+51f,shoulder.y+10f),s);
        Point wrist=lerp(new Point(shoulder.x+2f,shoulder.y+105f),
                new Point(shoulder.x+112f,shoulder.y+18f),s);
        Point farElbow=lerp(new Point(farShoulder.x-9f,farShoulder.y+58f),
                new Point(farShoulder.x+47f,farShoulder.y+7f),s);
        Point farWrist=lerp(new Point(farShoulder.x-12f,farShoulder.y+102f),
                new Point(farShoulder.x+105f,farShoulder.y+14f),s);
        return new Pose(ankle,farAnkle,knee,farKnee,hip,farHip,
                shoulder,farShoulder,neck,head,elbow,wrist,
                farElbow,farWrist);
    }

    /**
     * Planar two-bone IK; select the forward knee. Distances are clamped to
     * prevent NaN if later poses or proportions become unreachable.
     */
    public static Point kneeForward(Point hip,Point ankle,float femur,float tibia) {
        if(femur<=0 || tibia<=0)throw new IllegalArgumentException("Longitudes positivas");
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
