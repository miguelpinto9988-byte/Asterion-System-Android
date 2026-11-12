package pe.asterionsystem.app;

/**
 * Authored SIDE scenes with distinct support geometry and equipment.
 * The ordinary standing front/side renderer cannot represent a supine back
 * or a chair correctly, so these scenes own separate contact and props.
 * Screen-space motion is illustrative rather than clinically measured.
 */
public final class AvatarDedicatedScene {
    public enum Kind {
        GLUTE_BRIDGE_SHORT,GLUTE_BRIDGE_BASIC,SEATED_KNEE_EXTENSION
    }
    public final Kind kind;
    public final AvatarKinematics.Pose pose;
    public final AvatarKinematics.Point supportFoot,nearShoulder,nearHip;
    public final boolean floorBackContact,chairSeatContact;
    private AvatarDedicatedScene(Kind kind,AvatarKinematics.Pose p,
                                 AvatarKinematics.Point foot,
                                 AvatarKinematics.Point shoulder,
                                 AvatarKinematics.Point hip){
        this.kind=kind;pose=p;supportFoot=foot;
        nearShoulder=shoulder;nearHip=hip;
        floorBackContact=kind!=Kind.SEATED_KNEE_EXTENSION;
        chairSeatContact=kind==Kind.SEATED_KNEE_EXTENSION;
    }
    private static AvatarKinematics.Point pt(float x,float y){
        return new AvatarKinematics.Point(x,y);
    }
    public static AvatarDedicatedScene at(Kind kind,float phase,AvatarBodySpec b){
        if(kind==null||b==null)throw new IllegalArgumentException("Escena y cuerpo necesarios");
        float d=AvatarKinematics.depthAtPhase(phase);
        AvatarKinematics.Point hip,farHip,shoulder,farShoulder,ankle,farAnkle;
        AvatarRigConstraints.TwoBone leg,farLeg,arm,farArm;
        AvatarKinematics.Point neck,head,hand,farHand;
        if(kind==Kind.GLUTE_BRIDGE_SHORT||kind==Kind.GLUTE_BRIDGE_BASIC){
            // The shoulder and head remain on the mat; the pelvis follows
            // a torso-length circular arc while the soles remain in place.
            float rise=kind==Kind.GLUTE_BRIDGE_SHORT?22f*d:42f*d;
            shoulder=pt(125f,377f);
            farShoulder=pt(119f,387f);
            hip=pt(shoulder.x+(float)Math.sqrt(b.torso*b.torso-rise*rise),
                    shoulder.y-rise);
            farHip=pt(farShoulder.x+(float)Math.sqrt(
                    b.torso*b.torso-rise*rise),farShoulder.y-rise);
            ankle=pt(307f,391f);farAnkle=pt(298f,391f);
            leg=AvatarRigConstraints.twoBone(
                    hip,ankle,b.upperLeg,b.lowerLeg,-1,0f,170f);
            farLeg=AvatarRigConstraints.twoBone(
                    farHip,farAnkle,b.upperLeg,b.lowerLeg,-1,0f,170f);
            if(leg.targetLimited||farLeg.targetLimited)
                throw new IllegalStateException("Bridge foot anchor out of reach");
            neck=pt(shoulder.x-27f,shoulder.y+2f);
            head=pt(neck.x-27f,neck.y);
            float reach=b.upperArm+b.forearm-.025f;
            hand=pt(shoulder.x+reach,shoulder.y+1f);
            farHand=pt(farShoulder.x+reach,farShoulder.y+1f);
            arm=AvatarRigConstraints.twoBone(shoulder,hand,
                    b.upperArm,b.forearm,1,0f,170f);
            farArm=AvatarRigConstraints.twoBone(farShoulder,farHand,
                    b.upperArm,b.forearm,1,0f,170f);
        }else{
            // A true seated posture: pelvis/seat and knees remain fixed.
            // The one unloaded shin pivots from a flexed position toward
            // forward extension. The far leg retains its foot contact.
            hip=pt(151f,225f);farHip=pt(143f,224f);
            shoulder=pt(151f,225f-b.torso);
            farShoulder=pt(145f,235f-b.torso);
            float ux=.64f,uy=(float)Math.sqrt(1f-ux*ux);
            AvatarKinematics.Point knee=pt(hip.x+ux*b.upperLeg,hip.y+uy*b.upperLeg);
            AvatarKinematics.Point farKnee=pt(
                    farHip.x+ux*b.upperLeg,farHip.y+uy*b.upperLeg);
            float angle=(float)(Math.PI*.36d*d);
            ankle=pt(knee.x+b.lowerLeg*(float)Math.sin(angle),
                    knee.y+b.lowerLeg*(float)Math.cos(angle));
            farAnkle=pt(farKnee.x,farKnee.y+b.lowerLeg);
            leg=AvatarRigConstraints.twoBone(hip,ankle,
                    b.upperLeg,b.lowerLeg,-1,0f,170f);
            // The knee must not drift during extension: we retain the
            // authored seated knee as the hinge below.
            farLeg=AvatarRigConstraints.twoBone(farHip,farAnkle,
                    b.upperLeg,b.lowerLeg,-1,0f,170f);
            neck=pt(shoulder.x+2f,shoulder.y-5f);
            head=pt(neck.x+3f,neck.y-27f);
            hand=pt(shoulder.x+29f,shoulder.y+111f);
            farHand=pt(farShoulder.x+29f,farShoulder.y+111f);
            arm=AvatarRigConstraints.twoBone(shoulder,hand,
                    b.upperArm,b.forearm,1,0f,170f);
            farArm=AvatarRigConstraints.twoBone(farShoulder,farHand,
                    b.upperArm,b.forearm,1,0f,170f);
            // Use explicit angular knee axes instead of twoBone's solver:
            // hip-knee and knee-ankle are rigid and knee never relocates.
            AvatarKinematics.Point legJoint=knee,farJoint=farKnee;
            AvatarKinematics.Pose p=new AvatarKinematics.Pose(
                    ankle,farAnkle,legJoint,farJoint,hip,farHip,
                    shoulder,farShoulder,neck,head,arm.joint,arm.end,
                    farArm.joint,farArm.end);
            AvatarMotionEngine.assertSkeleton(p,b,.12f);
            return new AvatarDedicatedScene(kind,p,farAnkle,shoulder,hip);
        }
        AvatarKinematics.Pose p=new AvatarKinematics.Pose(
                ankle,farAnkle,leg.joint,farLeg.joint,hip,farHip,
                shoulder,farShoulder,neck,head,arm.joint,arm.end,
                farArm.joint,farArm.end);
        AvatarMotionEngine.assertSkeleton(p,b,.12f);
        return new AvatarDedicatedScene(kind,p,ankle,shoulder,hip);
    }
}
