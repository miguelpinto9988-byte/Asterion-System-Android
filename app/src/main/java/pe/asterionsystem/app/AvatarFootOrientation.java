package pe.asterionsystem.app;

/**
 * Foot orientation for the *prone toe-contact* phase of a push-up.
 * A toe points TOWARDS the head while the heel points away. The shin
 * direction does not define the shoe's forward direction: ankle dorsiflexion
 * bends the shoe independently. Both shoes share one longitudinal body axis
 * so they do not appear skewed or mirrored in the overhead projection.
 */
public final class AvatarFootOrientation {
    private AvatarFootOrientation(){}

    public static final float TOE_REACH=17f;
    public static final float HEEL_REACH=12f;

    public enum Side { LEFT,RIGHT }
    public enum Contact { TOE,HEEL,AIRBORNE }

    public static final class FootProjection {
        public final Side side;
        public final Contact contact;
        public final AvatarKinematics.Point ankle,toe,heel;
        public final float angleDegrees;
        public final AvatarOrientationRules.ShoeSurface surface;

        private FootProjection(Side side,Contact contact,
                               AvatarKinematics.Point ankle,
                               AvatarKinematics.Point toe,
                               AvatarKinematics.Point heel,
                               float angleDegrees,
                               AvatarOrientationRules.ShoeSurface surface){
            this.side=side;this.contact=contact;
            this.ankle=ankle;this.toe=toe;this.heel=heel;
            this.angleDegrees=angleDegrees;this.surface=surface;
            if(!Float.isFinite(angleDegrees))
                throw new IllegalArgumentException("Foot rotation finite");
        }

        public void requireProneOrientation(){
            if(contact!=Contact.TOE||surface!=AvatarOrientationRules.ShoeSurface.UPPER||
                    !(toe.y<ankle.y&&ankle.y<heel.y)||
                    toe.distance(ankle)<TOE_REACH-.05f||
                    heel.distance(ankle)<HEEL_REACH-.05f)
                throw new IllegalArgumentException("Reversed prone shoe");
        }
    }

    public static FootProjection proneTop(
            AvatarMultiViewKinematics.Pose p,Side side){
        if(p==null||side==null)throw new IllegalArgumentException("Foot pose required");
        AvatarOrientationRules.requireProneTopPose(p);
        AvatarKinematics.Point ankle=side==Side.LEFT?p.leftAnkle:p.rightAnkle;

        // Root/head axis, NOT knee-to-ankle orientation. It keeps the
        // two shoes parallel when the legs shift independently.
        float midHipX=(p.leftHip.x+p.rightHip.x)*.5f;
        float midHipY=(p.leftHip.y+p.rightHip.y)*.5f;
        float dx=p.head.x-midHipX,dy=p.head.y-midHipY;
        float distance=(float)Math.hypot(dx,dy);
        if(!Float.isFinite(distance)||distance<40f||dy>=-20f)
            throw new IllegalArgumentException("Head must face away from toes");
        dx/=distance;dy/=distance;
        AvatarKinematics.Point toe=new AvatarKinematics.Point(
                ankle.x+dx*TOE_REACH,ankle.y+dy*TOE_REACH);
        AvatarKinematics.Point heel=new AvatarKinematics.Point(
                ankle.x-dx*HEEL_REACH,ankle.y-dy*HEEL_REACH);
        AvatarOrientationRules.BodyFrame frame=AvatarOrientationRules.forPosture(
                AvatarOrientationRules.Posture.PRONE_GROUND_FACING);
        if(AvatarOrientationRules.topToeSign(frame)>=0f)
            throw new IllegalStateException("Prone toe should aim toward head");
        FootProjection foot=new FootProjection(
                side,Contact.TOE,ankle,toe,heel,
                (float)Math.toDegrees(Math.atan2(dy,dx)+Math.PI*.5),
                AvatarOrientationRules.visibleShoeSurface(
                        AvatarOrientationRules.Camera.TOP,frame));
        foot.requireProneOrientation();
        return foot;
    }

    /** A single invariant for all upper-camera frames and both feet. */
    public static void requirePair(AvatarMultiViewKinematics.Pose p){
        FootProjection left=proneTop(p,Side.LEFT);
        FootProjection right=proneTop(p,Side.RIGHT);
        if(Math.abs(left.angleDegrees-right.angleDegrees)>.01f||
                Math.abs(left.toe.x-right.toe.x-
                        (left.ankle.x-right.ankle.x))>.01f||
                !(left.toe.y<p.leftAnkle.y&&right.toe.y<p.rightAnkle.y))
            throw new IllegalArgumentException("Top shoes diverged");
    }
}
