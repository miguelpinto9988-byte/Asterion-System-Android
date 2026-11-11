package pe.asterionsystem.app;

/**
 * Authored orthographic projections for exercises that cannot be explained by
 * rotating the existing side-view illustration. These are separate 2D rigs
 * sharing a palette and fixed-length IK; they are not a reconstructed 3D body.
 */
public final class AvatarMultiViewKinematics {
    private AvatarMultiViewKinematics(){}

    public enum ViewMode {
        SIDE("Lateral · ejercicios"),
        FRONT("Frontal · elevación de brazos"),
        FRONT_SQUAT("Frontal · sentadilla abierta"),
        TOP("Superior · flexión diamante");
        public final String title;
        ViewMode(String title){this.title=title;}
    }

    public static final class Pose {
        public final AvatarKinematics.Point head,neck;
        public final AvatarKinematics.Point leftShoulder,rightShoulder;
        public final AvatarKinematics.Point leftElbow,rightElbow;
        public final AvatarKinematics.Point leftWrist,rightWrist;
        public final AvatarKinematics.Point leftHip,rightHip;
        public final AvatarKinematics.Point leftKnee,rightKnee;
        public final AvatarKinematics.Point leftAnkle,rightAnkle;
        public final float depth;
        private final float upperArm,forearm;
        Pose(AvatarKinematics.Point head,AvatarKinematics.Point neck,
             AvatarKinematics.Point ls,AvatarKinematics.Point rs,
             AvatarRigConstraints.TwoBone leftArm,
             AvatarRigConstraints.TwoBone rightArm,
             AvatarKinematics.Point lh,AvatarKinematics.Point rh,
             AvatarKinematics.Point lk,AvatarKinematics.Point rk,
             AvatarKinematics.Point la,AvatarKinematics.Point ra,
             float depth,float upperArm,float forearm) {
            this.head=head;this.neck=neck;
            this.leftShoulder=ls;this.rightShoulder=rs;
            this.leftElbow=leftArm.joint;this.rightElbow=rightArm.joint;
            this.leftWrist=leftArm.end;this.rightWrist=rightArm.end;
            this.leftHip=lh;this.rightHip=rh;
            this.leftKnee=lk;this.rightKnee=rk;
            this.leftAnkle=la;this.rightAnkle=ra;
            this.depth=depth;this.upperArm=upperArm;this.forearm=forearm;
        }
        /** Test both projected arm segments and pinned feet. */
        public void assertArms(float tolerance) {
            AvatarRigConstraints.requireFixedLength(
                    leftShoulder,leftElbow,upperArm,tolerance);
            AvatarRigConstraints.requireFixedLength(
                    leftElbow,leftWrist,forearm,tolerance);
            AvatarRigConstraints.requireFixedLength(
                    rightShoulder,rightElbow,upperArm,tolerance);
            AvatarRigConstraints.requireFixedLength(
                    rightElbow,rightWrist,forearm,tolerance);
        }
    }

    private static AvatarKinematics.Point at(float x,float y) {
        return new AvatarKinematics.Point(x,y);
    }

    /**
     * Frontal shoulders and hips share a horizontal pair, unlike a side-view
     * pose. Hands swing through a full raised-arm arc about each shoulder.
     * The whole scene is scaled to its camera viewport by the renderer.
     */
    public static Pose frontArms(float phase,AvatarBodySpec body) {
        if(body==null)throw new IllegalArgumentException("BodySpec requerido");
        float d=AvatarKinematics.depthAtPhase(phase);
        float angle=.055f+(float)Math.PI*.94f*d;
        float reach=body.upperArm+body.forearm-.025f;
        AvatarKinematics.Point ls=at(153f,150f),rs=at(247f,150f);
        AvatarKinematics.Point lw=at(
                ls.x-(float)Math.sin(angle)*reach,
                ls.y+(float)Math.cos(angle)*reach);
        AvatarKinematics.Point rw=at(
                rs.x+(float)Math.sin(angle)*reach,
                rs.y+(float)Math.cos(angle)*reach);
        AvatarRigConstraints.TwoBone left=AvatarRigConstraints.twoBone(
                ls,lw,body.upperArm,body.forearm,-1,0f,170f);
        AvatarRigConstraints.TwoBone right=AvatarRigConstraints.twoBone(
                rs,rw,body.upperArm,body.forearm,1,0f,170f);
        return new Pose(at(200f,103f),at(200f,130f),ls,rs,left,right,
                at(174f,238f),at(226f,238f),
                at(174f,332f),at(226f,332f),
                at(173f,428f),at(227f,428f),
                d,body.upperArm,body.forearm);
    }

    /**
     * True overhead composition: face points toward the floor, so the camera
     * sees the back of the head and torso, not a standing front-view figure.
     * Hands and toes are fixed screen-space contact landmarks. The up/down
     * movement is only subtly legible from above, hence a side mini-view.
     */
    /**
     * An actual wide-stance front-camera animation, not the side squat viewed
     * head-on. The planted ankles are fixed and both knees are projected by
     * a two-bone solution, outward to opposite sides without stretching.
     */
    public static Pose frontWideSquat(float phase,AvatarBodySpec body) {
        if(body==null)throw new IllegalArgumentException("BodySpec requerido");
        float d=AvatarKinematics.depthAtPhase(phase);
        float shift=46f*d;
        AvatarKinematics.Point ls=at(153f,150f+shift),rs=at(247f,150f+shift);
        AvatarKinematics.Point lh=at(174f,238f+shift),rh=at(226f,238f+shift);
        AvatarKinematics.Point la=at(127f,411f),ra=at(273f,411f);
        AvatarRigConstraints.TwoBone leftLeg=AvatarRigConstraints.twoBone(
                lh,la,body.upperLeg,body.lowerLeg,1,0f,170f);
        AvatarRigConstraints.TwoBone rightLeg=AvatarRigConstraints.twoBone(
                rh,ra,body.upperLeg,body.lowerLeg,-1,0f,170f);
        if(leftLeg.targetLimited||rightLeg.targetLimited)
            throw new IllegalArgumentException("Los pies plantados deben ser alcanzables");
        AvatarKinematics.Point lw=at(184f,212f+shift);
        AvatarKinematics.Point rw=at(216f,212f+shift);
        AvatarRigConstraints.TwoBone leftArm=AvatarRigConstraints.twoBone(
                ls,lw,body.upperArm,body.forearm,1,0f,170f);
        AvatarRigConstraints.TwoBone rightArm=AvatarRigConstraints.twoBone(
                rs,rw,body.upperArm,body.forearm,-1,0f,170f);
        return new Pose(at(200f,103f+shift),at(200f,130f+shift),
                ls,rs,leftArm,rightArm,lh,rh,leftLeg.joint,
                rightLeg.joint,la,ra,d,body.upperArm,body.forearm);
    }

    public static Pose diamondTop(float phase,AvatarBodySpec body) {
        if(body==null)throw new IllegalArgumentException("BodySpec requerido");
        float d=AvatarKinematics.depthAtPhase(phase);
        AvatarKinematics.Point ls=at(132f,158f+5f*d);
        AvatarKinematics.Point rs=at(238f,158f+5f*d);
        AvatarKinematics.Point lw=at(178f,226f),rw=at(192f,226f);
        float upper=body.upperArm*.80f,lower=body.forearm*.80f;
        AvatarRigConstraints.TwoBone left=AvatarRigConstraints.twoBone(
                ls,lw,upper,lower,1,7f,165f);
        AvatarRigConstraints.TwoBone right=AvatarRigConstraints.twoBone(
                rs,rw,upper,lower,-1,7f,165f);
        return new Pose(at(185f,103f+2f*d),at(185f,129f+3f*d),
                ls,rs,left,right,
                at(161f,282f),at(209f,282f),
                at(168f,341f),at(202f,341f),
                at(170f,406f),at(200f,406f),
                d,upper,lower);
    }
}
