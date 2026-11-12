package pe.asterionsystem.app;

/**
 * World-space orientation contract shared by camera-specific 2D rigs.
 *
 * Coordinate convention: +X character right, +Y away from the floor,
 * +Z from head toward feet for the prone body; the top camera looks
 * down from +Y. These vectors are virtual model axes, not measured human
 * 3D motion and do not replace an actual biomechanics solver.
 */
public final class AvatarOrientationRules {
    private AvatarOrientationRules(){}

    public static final class Vector3 {
        public final float x,y,z;
        public Vector3(float x,float y,float z) {
            if(!Float.isFinite(x)||!Float.isFinite(y)||!Float.isFinite(z))
                throw new IllegalArgumentException("Vector must be finite");
            this.x=x;this.y=y;this.z=z;
        }
        public float dot(Vector3 other) {
            if(other==null)throw new IllegalArgumentException("Other required");
            return x*other.x+y*other.y+z*other.z;
        }
        public Vector3 negate(){return new Vector3(-x,-y,-z);}
        public float length(){return (float)Math.sqrt(dot(this));}
        public Vector3 normalized(){
            float len=length();
            if(len<.0001f)throw new IllegalArgumentException("Zero axis");
            return new Vector3(x/len,y/len,z/len);
        }
    }

    public enum Camera {
        FRONT(new Vector3(0f,0f,-1f)),
        SIDE(new Vector3(1f,0f,0f)),
        TOP(new Vector3(0f,1f,0f));
        /** Direction from character to viewing camera, not the camera ray. */
        public final Vector3 towardViewer;
        Camera(Vector3 direction){towardViewer=direction;}
    }

    public enum Posture { STANDING, PRONE_GROUND_FACING }
    public enum ShoeSurface { UPPER, SOLE }

    public static final class BodyFrame {
        public final Posture posture;
        public final Vector3 headAxis,faceNormal,torsoAnteriorNormal;
        public final Vector3 palmNormal,soleNormal,toeAxis;
        BodyFrame(Posture posture,Vector3 headAxis,Vector3 faceNormal,
                  Vector3 torsoAnteriorNormal,Vector3 palmNormal,
                  Vector3 soleNormal,Vector3 toeAxis){
            this.posture=posture;
            this.headAxis=headAxis.normalized();
            this.faceNormal=faceNormal.normalized();
            this.torsoAnteriorNormal=torsoAnteriorNormal.normalized();
            this.palmNormal=palmNormal.normalized();
            this.soleNormal=soleNormal.normalized();
            this.toeAxis=toeAxis.normalized();
            if(Math.abs(this.headAxis.dot(this.faceNormal))>.001f)
                throw new IllegalArgumentException("Head and face axes conflict");
            if(Math.abs(this.soleNormal.dot(this.toeAxis))>.001f)
                throw new IllegalArgumentException("Sole and toe axes conflict");
        }
    }

    // Sole normals stay directed toward the floor in these illustrative
    // contact poses. The toe is NOT constrained to share the face direction.
    private static final Vector3 DOWN=new Vector3(0f,-1f,0f);
    private static final BodyFrame STANDING_FRAME=new BodyFrame(
            Posture.STANDING,new Vector3(0f,1f,0f),
            new Vector3(0f,0f,-1f),
            new Vector3(0f,0f,-1f),new Vector3(0f,0f,-1f),
            DOWN,new Vector3(0f,0f,-1f));
    private static final BodyFrame PRONE_FRAME=new BodyFrame(
            Posture.PRONE_GROUND_FACING,new Vector3(0f,0f,-1f),
            DOWN,DOWN,DOWN,DOWN,new Vector3(0f,0f,-1f));

    public static BodyFrame forPosture(Posture posture) {
        if(posture==null)throw new IllegalArgumentException("Posture required");
        return posture==Posture.STANDING?STANDING_FRAME:PRONE_FRAME;
    }

    /** If a surface points away from the camera it cannot be shown as a
     * face-on visible decal. A sole directed downward is not a top graphic. */
    public static boolean visibleSurface(Vector3 outward,Camera camera) {
        if(outward==null||camera==null)
            throw new IllegalArgumentException("Surface and camera required");
        return outward.normalized().dot(camera.towardViewer)>.01f;
    }
    public static ShoeSurface visibleShoeSurface(Camera camera,BodyFrame frame){
        if(camera==null||frame==null)
            throw new IllegalArgumentException("Camera and frame required");
        // This simplified rule expresses the facing-up/back-of-shoe
        // contact scene, not general 3D footwear geometry.
        return visibleSurface(frame.soleNormal,camera)
                ?ShoeSurface.SOLE:ShoeSurface.UPPER;
    }

    /**
     * Screen x/y are a camera projection, NOT world coordinates.
     * In TOP camera the virtual +Z axis is screen +Y (towards the feet).
     */
    public static AvatarKinematics.Point project(Vector3 v,Camera camera) {
        if(v==null||camera==null)
            throw new IllegalArgumentException("Project input required");
        switch(camera){
            case FRONT:return new AvatarKinematics.Point(v.x,-v.y);
            case SIDE:return new AvatarKinematics.Point(v.z,-v.y);
            case TOP:return new AvatarKinematics.Point(v.x,v.z);
            default:throw new IllegalStateException("Missing camera");
        }
    }

    /** Sign for the shoe tip in the overhead projection. In a prone
     * toe-supported pose the toes bend TOWARDS the head (screen -Y),
     * independently of the knee-to-ankle shin direction. */
    public static float topToeSign(BodyFrame frame){
        if(frame==null)throw new IllegalArgumentException("Frame required");
        float y=project(frame.toeAxis,Camera.TOP).y;
        if(Math.abs(y)<.001f)throw new IllegalStateException("Toe undefined");
        return Math.signum(y);
    }

    /** Lateral face points +X when standing, and toward the screen floor
     * during a floor-facing pose. Canvas.rotate(...,head.x,head.y)
     * applies this continuous forward-kinematics orientation to the face. */
    public static float sideFacingDegrees(AvatarKinematics.Point hip,
                                          AvatarKinematics.Point shoulder){
        if(hip==null||shoulder==null)
            throw new IllegalArgumentException("Spine points required");
        float dx=shoulder.x-hip.x,dy=shoulder.y-hip.y;
        if(!Float.isFinite(dx)||!Float.isFinite(dy)||
                Math.hypot(dx,dy)<.001)
            throw new IllegalArgumentException("Nondegenerate spine required");
        return (float)Math.toDegrees(Math.atan2(dy,dx)+Math.PI*.5);
    }

    /**
     * Shared front-camera contract for upright scenes. This is a shape
     * orientation guard, not an assessment of exercise technique; both
     * heels may temporarily leave the ground in a jumping scene.
     */
    public static void requireUprightFrontPose(AvatarMultiViewKinematics.Pose p){
        if(p==null)throw new IllegalArgumentException("Pose required");
        BodyFrame standing=forPosture(Posture.STANDING);
        if(!visibleSurface(standing.faceNormal,Camera.FRONT)||
                visibleSurface(standing.soleNormal,Camera.TOP)||
                !(p.head.y<p.neck.y&&p.neck.y<p.leftShoulder.y&&
                  p.neck.y<p.rightShoulder.y&&
                  p.leftShoulder.y<p.leftHip.y&&
                  p.rightShoulder.y<p.rightHip.y&&
                  p.leftHip.y<p.leftAnkle.y&&
                  p.rightHip.y<p.rightAnkle.y&&
                  Math.abs(p.leftShoulder.y-p.rightShoulder.y)<.01f))
            throw new IllegalArgumentException("Inverted upright projection");
    }

    /** Head above shoulders and toes beyond knees in a TOP prone camera.
     * Catches mirrored / reversed projections before painting shoes. */
    public static void requireProneTopPose(AvatarMultiViewKinematics.Pose p) {
        if(p==null)throw new IllegalArgumentException("Pose required");
        BodyFrame frame=forPosture(Posture.PRONE_GROUND_FACING);
        if(frame.faceNormal.dot(DOWN)<.999f||
                frame.torsoAnteriorNormal.dot(DOWN)<.999f||
                frame.palmNormal.dot(DOWN)<.999f||
                visibleSurface(frame.palmNormal,Camera.TOP)||
                visibleShoeSurface(Camera.TOP,frame)!=ShoeSurface.UPPER||
                topToeSign(frame)>0f||
                !(p.head.y<p.neck.y&&p.neck.y<p.leftHip.y&&
                  p.neck.y<p.rightHip.y&&p.leftKnee.y<p.leftAnkle.y&&
                  p.rightKnee.y<p.rightAnkle.y&&
                  p.leftHip.y<p.leftAnkle.y&&p.rightHip.y<p.rightAnkle.y))
            throw new IllegalArgumentException("Inverted prone projection");
    }
}
