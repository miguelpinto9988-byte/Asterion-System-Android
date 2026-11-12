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
        FRONT_JACKS("Frontal · saltos de tijera"),
        FRONT_ELBOW("Frontal · articulación de codos"),
        TOP("Superior · flexión diamante"),
        FRONT_LATERAL("Frontal · separación de brazos"),
        FRONT_BALANCE("Frontal · equilibrio unilateral"),
        FRONT_SIDE_STEP("Frontal · paso lateral con retorno"),
        FRONT_NECK_TILT("Frontal · inclinación lateral cervical");
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
        float shift=49f*d;
        AvatarKinematics.Point ls=at(153f,156f+shift),rs=at(247f,156f+shift);
        AvatarKinematics.Point lh=at(174f,244f+shift),rh=at(226f,244f+shift);
        AvatarKinematics.Point la=at(127f,428f),ra=at(273f,428f);
        AvatarRigConstraints.TwoBone leftLeg=AvatarRigConstraints.twoBone(
                lh,la,body.upperLeg,body.lowerLeg,1,0f,170f);
        AvatarRigConstraints.TwoBone rightLeg=AvatarRigConstraints.twoBone(
                rh,ra,body.upperLeg,body.lowerLeg,-1,0f,170f);
        if(leftLeg.targetLimited||rightLeg.targetLimited)
            throw new IllegalArgumentException("Los pies plantados deben ser alcanzables");
        AvatarKinematics.Point lw=at(184f,218f+shift);
        AvatarKinematics.Point rw=at(216f,218f+shift);
        AvatarRigConstraints.TwoBone leftArm=AvatarRigConstraints.twoBone(
                ls,lw,body.upperArm,body.forearm,1,0f,170f);
        AvatarRigConstraints.TwoBone rightArm=AvatarRigConstraints.twoBone(
                rs,rw,body.upperArm,body.forearm,-1,0f,170f);
        return new Pose(at(200f,109f+shift),at(200f,136f+shift),
                ls,rs,leftArm,rightArm,lh,rh,leftLeg.joint,
                rightLeg.joint,la,ra,d,body.upperArm,body.forearm);
    }

    /**
     * Frontal cardio demonstration: one complete closed/open/closed cycle.
     * Knee and elbow positions are recalculated via fixed-length IK. During
     * the two travel intervals the feet are allowed to be airborne; there
     * is no claim that the 2D timing validates human movement technique.
     */
    public static Pose frontJumpingJack(float phase,AvatarBodySpec body) {
        if(body==null)throw new IllegalArgumentException("BodySpec requerido");
        if(!Float.isFinite(phase))
            throw new IllegalArgumentException("Fase finita requerida");
        float t=Math.max(0f,Math.min(1f,phase));
        float d=.5f-.5f*(float)Math.cos(Math.PI*2d*t);
        float hop=9f*Math.abs((float)Math.sin(Math.PI*2d*t));
        float lift=16f*d-hop;
        AvatarKinematics.Point ls=at(153f,150f+lift),rs=at(247f,150f+lift);
        AvatarKinematics.Point lh=at(174f,238f+lift),rh=at(226f,238f+lift);
        AvatarKinematics.Point la=at(174f-69f*d,428f-hop);
        AvatarKinematics.Point ra=at(226f+69f*d,428f-hop);
        AvatarRigConstraints.TwoBone legL=AvatarRigConstraints.twoBone(
                lh,la,body.upperLeg,body.lowerLeg,1,0f,170f);
        AvatarRigConstraints.TwoBone legR=AvatarRigConstraints.twoBone(
                rh,ra,body.upperLeg,body.lowerLeg,-1,0f,170f);
        if(legL.targetLimited||legR.targetLimited)
            throw new IllegalStateException("Los pies deben permanecer al alcance");
        float angle=.025f+((float)Math.PI-.05f)*d;
        float reach=body.upperArm+body.forearm-.03f;
        AvatarKinematics.Point lw=at(ls.x-(float)Math.sin(angle)*reach,
                ls.y+(float)Math.cos(angle)*reach);
        AvatarKinematics.Point rw=at(rs.x+(float)Math.sin(angle)*reach,
                rs.y+(float)Math.cos(angle)*reach);
        AvatarRigConstraints.TwoBone armL=AvatarRigConstraints.twoBone(
                ls,lw,body.upperArm,body.forearm,-1,0f,170f);
        AvatarRigConstraints.TwoBone armR=AvatarRigConstraints.twoBone(
                rs,rw,body.upperArm,body.forearm,1,0f,170f);
        return new Pose(at(200f,103f+lift),at(200f,130f+lift),
                ls,rs,armL,armR,lh,rh,legL.joint,legR.joint,
                la,ra,d,body.upperArm,body.forearm);
    }

    /** Deliberate elbow-flexion demonstration distinct from straight arm
     * raises, to make the hinge and its constant link lengths inspectable. */
    public static Pose frontElbowFlex(float phase,AvatarBodySpec body) {
        if(body==null)throw new IllegalArgumentException("BodySpec requerido");
        float d=AvatarKinematics.depthAtPhase(phase);
        AvatarKinematics.Point ls=at(153f,150f),rs=at(247f,150f);
        float reach=body.upperArm+body.forearm-.03f;
        AvatarKinematics.Point lw=at(
                ls.x+(30f*d),ls.y+reach*(1f-d)+39f*d);
        AvatarKinematics.Point rw=at(
                rs.x-(30f*d),rs.y+reach*(1f-d)+39f*d);
        AvatarRigConstraints.TwoBone armL=AvatarRigConstraints.twoBone(
                ls,lw,body.upperArm,body.forearm,1,0f,170f);
        AvatarRigConstraints.TwoBone armR=AvatarRigConstraints.twoBone(
                rs,rw,body.upperArm,body.forearm,-1,0f,170f);
        return new Pose(at(200f,103f),at(200f,130f),ls,rs,
                armL,armR,at(174f,238f),at(226f,238f),
                at(174f,332f),at(226f,332f),
                at(174f,428f),at(226f,428f),
                d,body.upperArm,body.forearm);
    }


    /** True frontal shoulder abduction: arms stop at shoulder height rather
     * than copying the existing overhead frontal flexion. Feet are pinned. */
    public static Pose frontLateralRaise(float phase,AvatarBodySpec body) {
        if(body==null)throw new IllegalArgumentException("BodySpec requerido");
        float d=AvatarKinematics.depthAtPhase(phase);
        float angle=.055f+((float)Math.PI/2f-.055f)*d;
        float reach=body.upperArm+body.forearm-.03f;
        AvatarKinematics.Point ls=at(153f,150f),rs=at(247f,150f);
        AvatarKinematics.Point lw=at(ls.x-(float)Math.sin(angle)*reach,
                ls.y+(float)Math.cos(angle)*reach);
        AvatarKinematics.Point rw=at(rs.x+(float)Math.sin(angle)*reach,
                rs.y+(float)Math.cos(angle)*reach);
        AvatarRigConstraints.TwoBone la=AvatarRigConstraints.twoBone(
                ls,lw,body.upperArm,body.forearm,-1,0f,170f);
        AvatarRigConstraints.TwoBone ra=AvatarRigConstraints.twoBone(
                rs,rw,body.upperArm,body.forearm,1,0f,170f);
        AvatarKinematics.Point lh=at(174f,238f),rh=at(226f,238f);
        AvatarKinematics.Point lf=at(174f,428f),rf=at(226f,428f);
        AvatarRigConstraints.TwoBone ll=AvatarRigConstraints.twoBone(
                lh,lf,body.upperLeg,body.lowerLeg,-1,0f,170f);
        AvatarRigConstraints.TwoBone rl=AvatarRigConstraints.twoBone(
                rh,rf,body.upperLeg,body.lowerLeg,1,0f,170f);
        return new Pose(at(200f,103f),at(200f,130f),ls,rs,la,ra,
                lh,rh,ll.joint,rl.joint,ll.end,rl.end,d,body.upperArm,body.forearm);
    }

    /** Single-leg balance: left free ankle travels upward while the right
     * sole and both shoulders retain fixed world-space landmarks. */
    public static Pose frontSingleLegBalance(float phase,AvatarBodySpec body) {
        if(body==null)throw new IllegalArgumentException("BodySpec requerido");
        float d=AvatarKinematics.depthAtPhase(phase);
        AvatarKinematics.Point ls=at(153f,150f),rs=at(247f,150f);
        float reach=body.upperArm+body.forearm-.03f;
        AvatarKinematics.Point lw=at(ls.x-6f,ls.y+reach-.15f);
        AvatarKinematics.Point rw=at(rs.x+6f,rs.y+reach-.15f);
        AvatarRigConstraints.TwoBone la=AvatarRigConstraints.twoBone(
                ls,lw,body.upperArm,body.forearm,-1,0f,170f);
        AvatarRigConstraints.TwoBone ra=AvatarRigConstraints.twoBone(
                rs,rw,body.upperArm,body.forearm,1,0f,170f);
        AvatarKinematics.Point lh=at(174f,238f),rh=at(226f,238f);
        AvatarKinematics.Point lf=at(174f+22f*d,428f-57f*d);
        AvatarKinematics.Point rf=at(226f,428f);
        AvatarRigConstraints.TwoBone ll=AvatarRigConstraints.twoBone(
                lh,lf,body.upperLeg,body.lowerLeg,-1,0f,170f);
        AvatarRigConstraints.TwoBone rl=AvatarRigConstraints.twoBone(
                rh,rf,body.upperLeg,body.lowerLeg,1,0f,170f);
        if(ll.targetLimited||rl.targetLimited)
            throw new IllegalArgumentException("Apoyo unilateral fuera de alcance");
        return new Pose(at(200f,103f),at(200f,130f),ls,rs,la,ra,
                lh,rh,ll.joint,rl.joint,ll.end,rl.end,d,body.upperArm,body.forearm);
    }

    /** One unloaded lateral step and return, never a band-resistance demo.
     * Left sole is planted; right foot is free until neutral. */
    public static Pose frontSideStep(float phase,AvatarBodySpec body) {
        if(body==null)throw new IllegalArgumentException("BodySpec requerido");
        float d=AvatarKinematics.depthAtPhase(phase);
        AvatarKinematics.Point ls=at(153f,150f),rs=at(247f,150f);
        float reach=body.upperArm+body.forearm-.03f;
        AvatarKinematics.Point lw=at(ls.x-6f,ls.y+reach-.15f);
        AvatarKinematics.Point rw=at(rs.x+6f,rs.y+reach-.15f);
        AvatarRigConstraints.TwoBone la=AvatarRigConstraints.twoBone(
                ls,lw,body.upperArm,body.forearm,-1,0f,170f);
        AvatarRigConstraints.TwoBone ra=AvatarRigConstraints.twoBone(
                rs,rw,body.upperArm,body.forearm,1,0f,170f);
        AvatarKinematics.Point lh=at(174f,238f),rh=at(226f,238f);
        AvatarKinematics.Point lf=at(174f,428f);
        AvatarKinematics.Point rf=at(226f+43f*d,428f-13f*d);
        AvatarRigConstraints.TwoBone ll=AvatarRigConstraints.twoBone(
                lh,lf,body.upperLeg,body.lowerLeg,-1,0f,170f);
        AvatarRigConstraints.TwoBone rl=AvatarRigConstraints.twoBone(
                rh,rf,body.upperLeg,body.lowerLeg,1,0f,170f);
        if(ll.targetLimited||rl.targetLimited)
            throw new IllegalArgumentException("Paso lateral fuera de alcance");
        return new Pose(at(200f,103f),at(200f,130f),ls,rs,la,ra,
                lh,rh,ll.joint,rl.joint,ll.end,rl.end,d,body.upperArm,body.forearm);
    }


    /** Gentle frontal head tilt; the neck-to-head reference link stays
     * constant. This is not an axial head rotation or a clinical ROM target. */
    public static Pose frontNeckSideTilt(float phase,AvatarBodySpec body){
        if(body==null)throw new IllegalArgumentException("BodySpec requerido");
        float d=AvatarKinematics.depthAtPhase(phase);
        AvatarKinematics.Point ls=at(153f,150f),rs=at(247f,150f);
        float reach=body.upperArm+body.forearm-.03f;
        AvatarKinematics.Point lw=at(ls.x-6f,ls.y+reach-.15f);
        AvatarKinematics.Point rw=at(rs.x+6f,rs.y+reach-.15f);
        AvatarRigConstraints.TwoBone la=AvatarRigConstraints.twoBone(
                ls,lw,body.upperArm,body.forearm,-1,0f,170f);
        AvatarRigConstraints.TwoBone ra=AvatarRigConstraints.twoBone(
                rs,rw,body.upperArm,body.forearm,1,0f,170f);
        AvatarKinematics.Point lh=at(174f,238f),rh=at(226f,238f);
        AvatarKinematics.Point lf=at(174f,428f),rf=at(226f,428f);
        AvatarRigConstraints.TwoBone ll=AvatarRigConstraints.twoBone(
                lh,lf,body.upperLeg,body.lowerLeg,-1,0f,170f);
        AvatarRigConstraints.TwoBone rl=AvatarRigConstraints.twoBone(
                rh,rf,body.upperLeg,body.lowerLeg,1,0f,170f);
        // Match the authored NECK_SIDE_TILT reference (12 degrees at peak).
        float x=27f*(float)Math.sin(Math.toRadians(12d))*d;
        float y=130f-(float)Math.sqrt(27f*27f-x*x);
        return new Pose(at(200f+x,y),at(200f,130f),
                ls,rs,la,ra,lh,rh,ll.joint,rl.joint,
                ll.end,rl.end,d,body.upperArm,body.forearm);
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
