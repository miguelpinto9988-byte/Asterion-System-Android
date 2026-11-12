package pe.asterionsystem.app;

/**
 * A low-fidelity articulated COMMAND diagram, not an exercise pose.
 * Every segment uses a rigid 2D rotation about its parent joint.
 * No contact, apparatus, clinical range of motion or 3D yaw is inferred.
 */
public final class AvatarCommandFigurePose {
    public final AvatarKinematics.Point head,neck;
    public final AvatarKinematics.Point leftShoulder,rightShoulder;
    public final AvatarKinematics.Point leftElbow,rightElbow;
    public final AvatarKinematics.Point leftWrist,rightWrist;
    public final AvatarKinematics.Point leftHip,rightHip;
    public final AvatarKinematics.Point leftKnee,rightKnee;
    public final AvatarKinematics.Point leftAnkle,rightAnkle;
    public final AvatarKinematics.Point leftHand,rightHand,leftToe,rightToe;
    private static final float UPPER_ARM=61f,FOREARM=55f;
    private static final float THIGH=72f,SHIN=68f,NECK=27f;
    private static final float SHOULDER_SPAN=98f,HIP_SPAN=46f,TORSO=117f;
    private AvatarCommandFigurePose(
            AvatarKinematics.Point head,AvatarKinematics.Point neck,
            AvatarKinematics.Point ls,AvatarKinematics.Point rs,
            AvatarKinematics.Point le,AvatarKinematics.Point re,
            AvatarKinematics.Point lw,AvatarKinematics.Point rw,
            AvatarKinematics.Point lh,AvatarKinematics.Point rh,
            AvatarKinematics.Point lk,AvatarKinematics.Point rk,
            AvatarKinematics.Point la,AvatarKinematics.Point ra,
            AvatarKinematics.Point lHand,AvatarKinematics.Point rHand,
            AvatarKinematics.Point lToe,AvatarKinematics.Point rToe){
        this.head=head;this.neck=neck;
        leftShoulder=ls;rightShoulder=rs;leftElbow=le;rightElbow=re;
        leftWrist=lw;rightWrist=rw;leftHip=lh;rightHip=rh;
        leftKnee=lk;rightKnee=rk;leftAnkle=la;rightAnkle=ra;
        leftHand=lHand;rightHand=rHand;leftToe=lToe;rightToe=rToe;
    }
    private static AvatarKinematics.Point at(float x,float y){
        return new AvatarKinematics.Point(x,y);
    }
    private static float rad(float degrees){
        return (float)Math.toRadians(degrees);
    }
    /** A 2D rigid limb from a pivot, 0 deg = downwards. */
    private static AvatarKinematics.Point limb(
            AvatarKinematics.Point root,float length,float angleDegrees,int sign){
        float a=rad(angleDegrees);
        return at(root.x+sign*length*(float)Math.sin(a),
                root.y+length*(float)Math.cos(a));
    }
    private static float value(AvatarJointTimeline t,
                               AvatarJointTimeline.Joint j,float phase){
        return t.offset(j,phase);
    }
    public static AvatarCommandFigurePose from(
            AvatarJointTimeline timeline,float phase){
        if(timeline==null||!Float.isFinite(phase)||phase<0||phase>1)
            throw new IllegalArgumentException("Comandos y fase requeridos");
        // Keep this as diagrammatic 2D movement. Reduce lateral projection
        // of sagittal trunk commands rather than claim a real camera transform.
        float pelvisShift=value(timeline,AvatarJointTimeline.Joint.PELVIS,phase)*.25f;
        AvatarKinematics.Point centerHip=at(200f+pelvisShift,253f);
        float torsoAngle=rad(value(timeline,
                AvatarJointTimeline.Joint.SPINE_BEND,phase)*.40f);
        AvatarKinematics.Point centerShoulder=at(
                centerHip.x+TORSO*(float)Math.sin(torsoAngle),
                centerHip.y-TORSO*(float)Math.cos(torsoAngle));
        float lScap=value(timeline,AvatarJointTimeline.Joint.LEFT_SCAPULA,phase)*.35f;
        float rScap=value(timeline,AvatarJointTimeline.Joint.RIGHT_SCAPULA,phase)*.35f;
        AvatarKinematics.Point ls=at(centerShoulder.x-SHOULDER_SPAN/2-lScap,
                centerShoulder.y-lScap*.25f);
        AvatarKinematics.Point rs=at(centerShoulder.x+SHOULDER_SPAN/2+rScap,
                centerShoulder.y-rScap*.25f);
        AvatarKinematics.Point lh=at(centerHip.x-HIP_SPAN/2,centerHip.y);
        AvatarKinematics.Point rh=at(centerHip.x+HIP_SPAN/2,centerHip.y);
        float neckTurn=value(timeline,AvatarJointTimeline.Joint.NECK_TURN,phase);
        float neckSide=value(timeline,AvatarJointTimeline.Joint.NECK_SIDE,phase);
        float neckPitch=value(timeline,AvatarJointTimeline.Joint.NECK_PITCH,phase);
        AvatarKinematics.Point neck=at(centerShoulder.x,centerShoulder.y-11f);
        float neckAngle=rad((neckSide+neckTurn*.35f+neckPitch*.25f));
        AvatarKinematics.Point head=at(
                neck.x+NECK*(float)Math.sin(neckAngle),
                neck.y-NECK*(float)Math.cos(neckAngle));
        float lSh=value(timeline,AvatarJointTimeline.Joint.LEFT_SHOULDER,phase)
                +value(timeline,AvatarJointTimeline.Joint.LEFT_SHOULDER_SIDE,phase);
        float rSh=value(timeline,AvatarJointTimeline.Joint.RIGHT_SHOULDER,phase)
                +value(timeline,AvatarJointTimeline.Joint.RIGHT_SHOULDER_SIDE,phase);
        float lEl=value(timeline,AvatarJointTimeline.Joint.LEFT_ELBOW,phase);
        float rEl=value(timeline,AvatarJointTimeline.Joint.RIGHT_ELBOW,phase);
        float lWr=value(timeline,AvatarJointTimeline.Joint.LEFT_WRIST,phase);
        float rWr=value(timeline,AvatarJointTimeline.Joint.RIGHT_WRIST,phase);
        float lArm=11f+Math.min(130f,lSh);
        float rArm=11f+Math.min(130f,rSh);
        AvatarKinematics.Point le=limb(ls,UPPER_ARM,lArm,-1);
        AvatarKinematics.Point re=limb(rs,UPPER_ARM,rArm,1);
        AvatarKinematics.Point lw=limb(le,FOREARM,lArm-lEl,-1);
        AvatarKinematics.Point rw=limb(re,FOREARM,rArm-rEl,1);
        AvatarKinematics.Point lHand=limb(lw,12f,lArm-lEl+lWr,-1);
        AvatarKinematics.Point rHand=limb(rw,12f,rArm-rEl+rWr,1);
        float lHip=value(timeline,AvatarJointTimeline.Joint.LEFT_HIP,phase)
                +value(timeline,AvatarJointTimeline.Joint.LEFT_HIP_SIDE,phase);
        float rHip=value(timeline,AvatarJointTimeline.Joint.RIGHT_HIP,phase)
                +value(timeline,AvatarJointTimeline.Joint.RIGHT_HIP_SIDE,phase);
        float lKnee=value(timeline,AvatarJointTimeline.Joint.LEFT_KNEE,phase);
        float rKnee=value(timeline,AvatarJointTimeline.Joint.RIGHT_KNEE,phase);
        float lAnkle=value(timeline,AvatarJointTimeline.Joint.LEFT_ANKLE,phase);
        float rAnkle=value(timeline,AvatarJointTimeline.Joint.RIGHT_ANKLE,phase);
        AvatarKinematics.Point lk=limb(lh,THIGH,lHip,-1);
        AvatarKinematics.Point rk=limb(rh,THIGH,rHip,1);
        AvatarKinematics.Point la=limb(lk,SHIN,lHip-lKnee,-1);
        AvatarKinematics.Point ra=limb(rk,SHIN,rHip-rKnee,1);
        AvatarKinematics.Point lToe=limb(la,11f,lHip-lKnee+lAnkle,-1);
        AvatarKinematics.Point rToe=limb(ra,11f,rHip-rKnee+rAnkle,1);
        return new AvatarCommandFigurePose(head,neck,ls,rs,le,re,lw,rw,
                lh,rh,lk,rk,la,ra,lHand,rHand,lToe,rToe);
    }
    public void requireFixedLengths(float tolerance){
        AvatarRigConstraints.requireFixedLength(neck,head,NECK,tolerance);
        AvatarRigConstraints.requireFixedLength(leftShoulder,leftElbow,UPPER_ARM,tolerance);
        AvatarRigConstraints.requireFixedLength(rightShoulder,rightElbow,UPPER_ARM,tolerance);
        AvatarRigConstraints.requireFixedLength(leftElbow,leftWrist,FOREARM,tolerance);
        AvatarRigConstraints.requireFixedLength(rightElbow,rightWrist,FOREARM,tolerance);
        AvatarRigConstraints.requireFixedLength(leftHip,leftKnee,THIGH,tolerance);
        AvatarRigConstraints.requireFixedLength(rightHip,rightKnee,THIGH,tolerance);
        AvatarRigConstraints.requireFixedLength(leftKnee,leftAnkle,SHIN,tolerance);
        AvatarRigConstraints.requireFixedLength(rightKnee,rightAnkle,SHIN,tolerance);
        AvatarRigConstraints.requireFixedLength(leftWrist,leftHand,12f,tolerance);
        AvatarRigConstraints.requireFixedLength(rightWrist,rightHand,12f,tolerance);
        AvatarRigConstraints.requireFixedLength(leftAnkle,leftToe,11f,tolerance);
        AvatarRigConstraints.requireFixedLength(rightAnkle,rightToe,11f,tolerance);
        AvatarRigConstraints.requireFixedLength(leftHip,rightHip,HIP_SPAN,tolerance);
    }
}
