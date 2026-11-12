package pe.asterionsystem.app;

/**
 * Authored SIDE scenes with distinct support geometry and equipment.
 * The ordinary standing front/side renderer cannot represent a supine back
 * or a chair correctly, so these scenes own separate contact and props.
 * Screen-space motion is illustrative rather than clinically measured.
 */
public final class AvatarDedicatedScene {
    public enum Kind {
        GLUTE_BRIDGE_SHORT,GLUTE_BRIDGE_BASIC,GLUTE_BRIDGE_PAUSE,
        GLUTE_BRIDGE_BAND,
        SEATED_KNEE_EXTENSION,SEATED_KNEE_EXTENSION_PAUSE,
        SEATED_KNEE_EXTENSION_ALTERNATING,SEATED_TOE_RAISE,
        SEATED_TOE_RAISE_SINGLE,SEATED_FORWARD_REACH,
        SEATED_HEEL_RAISE,SEATED_HEEL_RAISE_GENTLE
    }
    public final Kind kind;
    public final AvatarKinematics.Pose pose;
    public final AvatarKinematics.Point supportFoot,nearShoulder,nearHip;
    public final boolean floorBackContact,chairSeatContact;
    /** Foot hinge used only for seated bilateral toe raise; heels remain planted. */
    public final float toeRaiseDegrees;
    public final float heelRaiseDegrees;
    public final AvatarKinematics.Point neutralNearAnkle,neutralFarAnkle;
    public final AvatarKinematics.Point nearToeContact,farToeContact;
    private AvatarDedicatedScene(Kind kind,AvatarKinematics.Pose p,
                                 AvatarKinematics.Point foot,
                                 AvatarKinematics.Point shoulder,
                                 AvatarKinematics.Point hip,float toeRaiseDegrees,
                                 float heelRaiseDegrees,
                                 AvatarKinematics.Point neutralNearAnkle,
                                 AvatarKinematics.Point neutralFarAnkle){
        this.kind=kind;pose=p;supportFoot=foot;
        nearShoulder=shoulder;nearHip=hip;
        floorBackContact=kind==Kind.GLUTE_BRIDGE_SHORT||
                kind==Kind.GLUTE_BRIDGE_BASIC||kind==Kind.GLUTE_BRIDGE_PAUSE||
                kind==Kind.GLUTE_BRIDGE_BAND;
        chairSeatContact=!floorBackContact;
        // A dedicated ankle command is rendered around each shoe heel,
        // not interpreted as hip or knee motion.
        this.toeRaiseDegrees=(kind==Kind.SEATED_TOE_RAISE||
                kind==Kind.SEATED_TOE_RAISE_SINGLE)?toeRaiseDegrees:0f;
        this.heelRaiseDegrees=(kind==Kind.SEATED_HEEL_RAISE||
                kind==Kind.SEATED_HEEL_RAISE_GENTLE)?heelRaiseDegrees:0f;
        this.neutralNearAnkle=neutralNearAnkle;
        this.neutralFarAnkle=neutralFarAnkle;
        this.nearToeContact=neutralNearAnkle==null?null:
                pt(neutralNearAnkle.x+42f,neutralNearAnkle.y+10f);
        this.farToeContact=neutralFarAnkle==null?null:
                pt(neutralFarAnkle.x+42f,neutralFarAnkle.y+10f);
    }
    private static AvatarKinematics.Point pt(float x,float y){
        return new AvatarKinematics.Point(x,y);
    }
    /** Rotates an ankle position about the fixed toe/forefoot contact. */
    private static AvatarKinematics.Point rotateAbout(
            AvatarKinematics.Point origin,AvatarKinematics.Point pivot,
            float degrees){
        float a=(float)Math.toRadians(degrees),cos=(float)Math.cos(a),
                sin=(float)Math.sin(a),x=origin.x-pivot.x,y=origin.y-pivot.y;
        return pt(pivot.x+x*cos-y*sin,pivot.y+x*sin+y*cos);
    }
    public static AvatarDedicatedScene at(Kind kind,float phase,AvatarBodySpec b){
        if(kind==null||b==null||!Float.isFinite(phase)||phase<0f||phase>1f)
            throw new IllegalArgumentException("Escena, cuerpo y fase válidos requeridos");
        float d=AvatarKinematics.depthAtPhase(phase);
        float held=AvatarJointTimeline.heldWeight(phase);
        AvatarKinematics.Point hip,farHip,shoulder,farShoulder,ankle,farAnkle;
        AvatarRigConstraints.TwoBone leg,farLeg,arm,farArm;
        AvatarKinematics.Point neck,head,hand,farHand;
        if(kind==Kind.GLUTE_BRIDGE_SHORT||kind==Kind.GLUTE_BRIDGE_BASIC||
                kind==Kind.GLUTE_BRIDGE_PAUSE||kind==Kind.GLUTE_BRIDGE_BAND){
            // The shoulder and head remain on the mat; the pelvis follows
            // a torso-length circular arc while the soles remain in place.
            float rise=kind==Kind.GLUTE_BRIDGE_SHORT?22f*d:
                    kind==Kind.GLUTE_BRIDGE_PAUSE?42f*held:42f*d;
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
            // A true seated posture: fixed seat, femurs and grounded heels.
            // Reaching flexes the trunk with constant hip-shoulder length.
            hip=pt(151f,225f);farHip=pt(143f,224f);
            float torsoReach=kind==Kind.SEATED_FORWARD_REACH?.36f*d:0f;
            shoulder=pt(hip.x+b.torso*(float)Math.sin(torsoReach),
                    hip.y-b.torso*(float)Math.cos(torsoReach));
            farShoulder=pt(farHip.x+b.torso*(float)Math.sin(torsoReach),
                    farHip.y-b.torso*(float)Math.cos(torsoReach));
            float ux=.64f,uy=(float)Math.sqrt(1f-ux*ux);
            AvatarKinematics.Point knee=pt(hip.x+ux*b.upperLeg,hip.y+uy*b.upperLeg);
            AvatarKinematics.Point farKnee=pt(
                    farHip.x+ux*b.upperLeg,farHip.y+uy*b.upperLeg);
            float nearDepth=kind==Kind.SEATED_KNEE_EXTENSION?d:
                    kind==Kind.SEATED_KNEE_EXTENSION_PAUSE?held:
                    kind==Kind.SEATED_KNEE_EXTENSION_ALTERNATING&&phase<=.5f
                    ?AvatarKinematics.depthAtPhase(phase*2f):0f;
            float farDepth=kind==Kind.SEATED_KNEE_EXTENSION_ALTERNATING&&phase>=.5f
                    ?AvatarKinematics.depthAtPhase((phase-.5f)*2f):0f;
            float nearAngle=(float)(Math.PI*.36d*nearDepth);
            float farAngle=(float)(Math.PI*.36d*farDepth);
            ankle=pt(knee.x+b.lowerLeg*(float)Math.sin(nearAngle),
                    knee.y+b.lowerLeg*(float)Math.cos(nearAngle));
            farAnkle=pt(farKnee.x+b.lowerLeg*(float)Math.sin(farAngle),
                    farKnee.y+b.lowerLeg*(float)Math.cos(farAngle));
            AvatarKinematics.Point neutralNearAnkle=ankle;
            AvatarKinematics.Point neutralFarAnkle=farAnkle;
            float heelAngle=(kind==Kind.SEATED_HEEL_RAISE_GENTLE?12f:
                    kind==Kind.SEATED_HEEL_RAISE?22f:0f)*d;
            if(heelAngle>0f){
                // The shoebox rotates about its fixed toe contact; the
                // ankle follows this arc, and IK adjusts the knee without
                // stretching the femur or tibia.
                ankle=rotateAbout(ankle,pt(ankle.x+42f,ankle.y+10f),heelAngle);
                farAnkle=rotateAbout(farAnkle,pt(farAnkle.x+42f,farAnkle.y+10f),heelAngle);
            }
            leg=AvatarRigConstraints.twoBone(hip,ankle,
                    b.upperLeg,b.lowerLeg,-1,0f,170f);
            // The knee must not drift during extension: we retain the
            // authored seated knee as the hinge below.
            farLeg=AvatarRigConstraints.twoBone(farHip,farAnkle,
                    b.upperLeg,b.lowerLeg,-1,0f,170f);
            neck=pt(shoulder.x+2f,shoulder.y-5f);
            head=pt(neck.x+3f,neck.y-27f);
            float nearArmAngle=kind==Kind.SEATED_FORWARD_REACH
                    ?.15f+1.12f*d:.255f;
            float farArmAngle=kind==Kind.SEATED_FORWARD_REACH
                    ?.12f+1.12f*d:.255f;
            float armReach=b.upperArm+b.forearm-.025f;
            hand=AvatarKinematics.wristOnArc(shoulder,armReach,nearArmAngle);
            farHand=AvatarKinematics.wristOnArc(farShoulder,armReach,farArmAngle);
            arm=AvatarRigConstraints.twoBone(shoulder,hand,
                    b.upperArm,b.forearm,1,0f,170f);
            farArm=AvatarRigConstraints.twoBone(farShoulder,farHand,
                    b.upperArm,b.forearm,1,0f,170f);
            // Use explicit angular knee axes instead of twoBone's solver:
            // hip-knee and knee-ankle are rigid and knee never relocates.
            AvatarKinematics.Point legJoint=heelAngle>0f?leg.joint:knee;
            AvatarKinematics.Point farJoint=heelAngle>0f?farLeg.joint:farKnee;
            AvatarKinematics.Pose p=new AvatarKinematics.Pose(
                    ankle,farAnkle,legJoint,farJoint,hip,farHip,
                    shoulder,farShoulder,neck,head,arm.joint,arm.end,
                    farArm.joint,farArm.end);
            AvatarMotionEngine.assertSkeleton(p,b,.12f);
            // Support side changes in the alternating clip; at any time
            // only one knee extends, the other retains its ground reference.
            AvatarKinematics.Point planted=kind==Kind.SEATED_KNEE_EXTENSION_ALTERNATING
                    &&phase>=.5f?ankle:farAnkle;
            return new AvatarDedicatedScene(kind,p,planted,shoulder,hip,
                    kind==Kind.SEATED_TOE_RAISE||kind==Kind.SEATED_TOE_RAISE_SINGLE
                    ?22f*d:0f,heelAngle,neutralNearAnkle,neutralFarAnkle);
        }
        AvatarKinematics.Pose p=new AvatarKinematics.Pose(
                ankle,farAnkle,leg.joint,farLeg.joint,hip,farHip,
                shoulder,farShoulder,neck,head,arm.joint,arm.end,
                farArm.joint,farArm.end);
        AvatarMotionEngine.assertSkeleton(p,b,.12f);
        return new AvatarDedicatedScene(kind,p,ankle,shoulder,hip,
                0f,0f,ankle,farAnkle);
    }
}
