package pe.asterionsystem.app;

/**
 * Reusable authored exercise motion definitions for the 2D side-view avatar.
 *
 * These are animation samples, not scientific biomechanics or instructions on
 * how to exercise. Every exercise uses the same fixed-length articulated rig;
 * new catalog entries need their own reviewed targets and contact constraints.
 */
public final class AvatarMotionEngine {
    public enum Motion {
        SQUAT("Sentadilla"),
        HIP_HINGE("Bisagra de cadera"),
        ARM_RAISE("Elevación de brazos"),
        MARCH("Marcha"),
        LUNGE("Zancada"),
        BURPEE("Burpee · perfil"),
        ARM_CURL("Flexión de codos sin implemento"),
        KNEE_CURL("Flexión de rodilla de pie");
        public final String label;
        Motion(String label){this.label=label;}
    }
    private AvatarMotionEngine(){}

    public static AvatarKinematics.Pose pose(Motion motion,float phase,
                                             AvatarBodySpec spec) {
        if(motion==null||spec==null)
            throw new IllegalArgumentException("Movimiento y proporciones obligatorios");
        float s=AvatarKinematics.depthAtPhase(phase);
        if(motion==Motion.SQUAT)return AvatarKinematics.squat(s,spec);
        if(motion==Motion.BURPEE)return AvatarBurpeeKinematics.pose(phase,spec);

        float hx=184f,hy=174.4f,tilt=.10f;
        AvatarKinematics.Point ankle=new AvatarKinematics.Point(182f,364f);
        AvatarKinematics.Point farAnkle=new AvatarKinematics.Point(170f,363f);
        switch(motion) {
            case HIP_HINGE:
                tilt=.10f+.67f*s;
                hy+=9f*s;
                hx-=5f*s;
                break;
            case ARM_CURL:
                // Neutral stance: only the elbow command moves the hands.
                break;
            case ARM_RAISE:
                // Arms rotate in profile while both feet remain grounded.
                break;
            case MARCH:
                // Front foot is intentionally free in this motion; far foot
                // remains a fixed world-space contact.
                ankle=new AvatarKinematics.Point(182f+24f*s,364f-45f*s);
                hy+=2f*s;
                hx+=3f*s;
                break;
            case KNEE_CURL:
                // Only the near foot moves; the far sole stays fixed.
                // Analytic two-link IK keeps both knee segments constant.
                ankle=new AvatarKinematics.Point(182f-30f*s,364f-47f*s);
                break;
            case LUNGE:
                // Wide planted stance, illustrated in a single side view.
                ankle=new AvatarKinematics.Point(217f,364f);
                farAnkle=new AvatarKinematics.Point(143f,363f);
                hx=178f-6f*s;
                hy=181f+42f*s;
                tilt=.12f+.10f*s;
                break;
            default: throw new IllegalArgumentException("Unknown preset");
        }
        AvatarKinematics.Point hip=new AvatarKinematics.Point(hx,hy);
        AvatarKinematics.Point farHip=new AvatarKinematics.Point(hx-10f,hy-1f);

        AvatarRigConstraints.TwoBone leg=AvatarRigConstraints.twoBone(
                hip,ankle,spec.upperLeg,spec.lowerLeg,-1,0f,170f);
        AvatarRigConstraints.TwoBone farLeg=AvatarRigConstraints.twoBone(
                farHip,farAnkle,spec.upperLeg,spec.lowerLeg,-1,0f,170f);
        // Contact is not silently broken by moving a planted foot. An
        // impossible combination of proportions and contact positions fails.
        if(leg.targetLimited||farLeg.targetLimited)
            throw new IllegalArgumentException(
                    "The requested body proportions cannot reach foot contact");
        AvatarKinematics.Point shoulder=new AvatarKinematics.Point(
                hx+(float)Math.sin(tilt)*spec.torso,
                hy-(float)Math.cos(tilt)*spec.torso);
        AvatarKinematics.Point farShoulder=new AvatarKinematics.Point(
                shoulder.x-6f,shoulder.y+10f);
        AvatarKinematics.Point neck=new AvatarKinematics.Point(
                shoulder.x+4f,shoulder.y-6f);
        AvatarKinematics.Point head=new AvatarKinematics.Point(
                neck.x+4f,neck.y-27f);

        // Full reach at neutral and at both endpoints. The authored angle
        // changes smoothly without pulling the wrists towards the shoulders.
        float nearAngle=.055f,farAngle=.015f;
        switch(motion) {
            case ARM_CURL:
                // Elbow flexion shortens shoulder-to-wrist distance; the
                // elbow joint remains solved at fixed anatomical link lengths.
                nearAngle+=.35f*s;
                farAngle+=.30f*s;
                break;
            case ARM_RAISE:
                nearAngle+=(float)Math.PI*s;
                farAngle+=(float)Math.PI*s;
                break;
            case HIP_HINGE:
                nearAngle+=.73f*s;
                farAngle+=.66f*s;
                break;
            case MARCH:
                nearAngle+=.39f*s;
                farAngle+=.33f*s;
                break;
            case LUNGE:
                nearAngle+=1.03f*s;
                farAngle+=.94f*s;
                break;
            default:
                break;
        }
        float reach=spec.upperArm+spec.forearm-.015f;
        if(motion==Motion.ARM_CURL)reach-=46f*s;
        AvatarKinematics.Point wrist=AvatarKinematics.wristOnArc(
                shoulder,reach,nearAngle);
        AvatarKinematics.Point farWrist=AvatarKinematics.wristOnArc(
                farShoulder,reach,farAngle);
        AvatarRigConstraints.TwoBone arm=AvatarRigConstraints.twoBone(
                shoulder,wrist,spec.upperArm,spec.forearm,1,0f,170f);
        AvatarRigConstraints.TwoBone farArm=AvatarRigConstraints.twoBone(
                farShoulder,farWrist,spec.upperArm,spec.forearm,1,0f,170f);
        return new AvatarKinematics.Pose(
                ankle,farAnkle,leg.joint,farLeg.joint,hip,farHip,
                shoulder,farShoulder,neck,head,arm.joint,arm.end,
                farArm.joint,farArm.end);
    }

    public static void assertSkeleton(AvatarPose p,AvatarBodySpec spec,float tolerance){
        AvatarRigConstraints.requireFixedLength(p.hip,p.knee,spec.upperLeg,tolerance);
        AvatarRigConstraints.requireFixedLength(p.knee,p.ankle,spec.lowerLeg,tolerance);
        AvatarRigConstraints.requireFixedLength(p.farHip,p.farKnee,spec.upperLeg,tolerance);
        AvatarRigConstraints.requireFixedLength(p.farKnee,p.farAnkle,spec.lowerLeg,tolerance);
        AvatarRigConstraints.requireFixedLength(p.hip,p.shoulder,spec.torso,tolerance);
        AvatarRigConstraints.requireFixedLength(p.shoulder,p.elbow,spec.upperArm,tolerance);
        AvatarRigConstraints.requireFixedLength(p.elbow,p.wrist,spec.forearm,tolerance);
        AvatarRigConstraints.requireFixedLength(p.farShoulder,p.farElbow,spec.upperArm,tolerance);
        AvatarRigConstraints.requireFixedLength(p.farElbow,p.farWrist,spec.forearm,tolerance);
    }
}
