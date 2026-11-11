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
        LUNGE("Zancada");
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

        float hx=184f,hy=190f,tilt=.10f;
        AvatarKinematics.Point ankle=new AvatarKinematics.Point(182f,364f);
        AvatarKinematics.Point farAnkle=new AvatarKinematics.Point(170f,363f);
        switch(motion) {
            case HIP_HINGE:
                tilt=.10f+.67f*s;
                hy+=9f*s;
                hx-=5f*s;
                break;
            case ARM_RAISE:
                // Arms rotate in profile while both feet remain grounded.
                break;
            case MARCH:
                // Front foot is intentionally free in this motion; far foot
                // remains a fixed world-space contact.
                ankle=new AvatarKinematics.Point(182f+24f*s,364f-45f*s);
                hy-=4f*s;
                hx+=3f*s;
                break;
            case LUNGE:
                // Wide planted stance, illustrated in a single side view.
                ankle=new AvatarKinematics.Point(217f,364f);
                farAnkle=new AvatarKinematics.Point(143f,363f);
                hx=178f-6f*s;
                hy=188f+35f*s;
                tilt=.12f+.10f*s;
                break;
            default: throw new IllegalArgumentException("Unknown preset");
        }
        AvatarKinematics.Point hip=new AvatarKinematics.Point(hx,hy);
        AvatarKinematics.Point farHip=new AvatarKinematics.Point(hx-10f,hy-3f);

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
                shoulder.x-9f,shoulder.y+3f);
        AvatarKinematics.Point neck=new AvatarKinematics.Point(
                shoulder.x+3f,shoulder.y-9f);
        AvatarKinematics.Point head=new AvatarKinematics.Point(
                neck.x+5f,neck.y-29f);

        float frontDx=4f,frontDy=107f,farDx=-11f,farDy=101f;
        if(motion==Motion.ARM_RAISE) {
            frontDx+=57f*s;
            frontDy-=165f*s;
            farDx+=68f*s;
            farDy-=147f*s;
        } else if(motion==Motion.HIP_HINGE) {
            frontDx+=48f*s;
            frontDy-=28f*s;
            farDx+=44f*s;
            farDy-=30f*s;
        } else if(motion==Motion.MARCH) {
            frontDx+=31f*s;
            frontDy-=28f*s;
            farDx+=25f*s;
            farDy-=26f*s;
        } else if(motion==Motion.LUNGE) {
            frontDx+=55f*s;
            frontDy-=74f*s;
            farDx+=51f*s;
            farDy-=66f*s;
        }
        AvatarKinematics.Point wrist=new AvatarKinematics.Point(
                shoulder.x+frontDx,shoulder.y+frontDy);
        AvatarKinematics.Point farWrist=new AvatarKinematics.Point(
                farShoulder.x+farDx,farShoulder.y+farDy);
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
