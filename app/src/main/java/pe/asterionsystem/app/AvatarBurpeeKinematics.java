package pe.asterionsystem.app;

/**
 * Original multi-pose lateral burpee *animation*, not instructions or form
 * assessment. Body and limb lengths remain constant. A multi-stage clip
 * illustrates standing, crouching, reaching the floor, a horizontal position,
 * a shallow body dip, and returning to the starting pose.
 */
public final class AvatarBurpeeKinematics {
    private AvatarBurpeeKinematics(){}

    // t, hipX, hipY, nearFootX, farFootX, trunkTilt,
    // nearWristTargetX, nearWristTargetY.
    private static final float[][] KEYS={
        {0f,   184f,174.4f,182f,170f,.10f,200f,176f},
        {.17f, 178f,238f, 182f,170f,1.12f,300f,303f},
        {.25f, 178f,272f, 182f,170f,1.34f,305f,364f},
        {.40f, 203f,283f, 34f, 36f, 1.34f,305f,364f},
        {.50f, 203f,304f, 34f, 36f, 1.34f,305f,364f},
        {.60f, 203f,283f, 34f, 36f, 1.34f,305f,364f},
        {.75f, 178f,272f, 182f,170f,1.34f,305f,364f},
        {.85f, 178f,238f, 182f,170f,1.12f,300f,303f},
        {1f,   184f,174.4f,182f,170f,.10f,200f,176f}
    };

    public static AvatarKinematics.Pose pose(float phase,AvatarBodySpec spec){
        if(spec==null||!Float.isFinite(phase))
            throw new IllegalArgumentException("Fase finita y proporciones requeridas");
        float t=Math.max(0f,Math.min(1f,phase));
        int seg=KEYS.length-2;
        for(int i=0;i<KEYS.length-1;i++){
            if(t<=KEYS[i+1][0]) {seg=i;break;}
        }
        float[] before=KEYS[seg],after=KEYS[seg+1];
        float local=(t-before[0])/(after[0]-before[0]);
        local=local*local*(3f-2f*local);
        float hx=mix(before[1],after[1],local);
        float hy=mix(before[2],after[2],local);
        float nearAx=mix(before[3],after[3],local);
        float farAx=mix(before[4],after[4],local);
        float angle=mix(before[5],after[5],local);
        float wx=mix(before[6],after[6],local);
        float wy=mix(before[7],after[7],local);

        // In the feet-travel segment the shoes are detached from the floor.
        float hop=0f;
        if(t>.25f&&t<.40f)hop=10f*(float)Math.sin(
                Math.PI*(t-.25f)/.15f);
        if(t>.60f&&t<.75f)hop=10f*(float)Math.sin(
                Math.PI*(t-.60f)/.15f);
        AvatarKinematics.Point hip=point(hx,hy);
        AvatarKinematics.Point farHip=point(hx-10f,hy-1f);
        AvatarKinematics.Point ankle=point(nearAx,364f-hop);
        AvatarKinematics.Point farAnkle=point(farAx,363f-hop);
        AvatarRigConstraints.TwoBone leg=AvatarRigConstraints.twoBone(
                hip,ankle,spec.upperLeg,spec.lowerLeg,-1,0f,170f);
        AvatarRigConstraints.TwoBone backLeg=AvatarRigConstraints.twoBone(
                farHip,farAnkle,spec.upperLeg,spec.lowerLeg,-1,0f,170f);
        if(leg.targetLimited||backLeg.targetLimited)
            throw new IllegalStateException("El pie objetivo excedió su alcance");

        AvatarKinematics.Point shoulder=point(
                hx+(float)Math.sin(angle)*spec.torso,
                hy-(float)Math.cos(angle)*spec.torso);
        AvatarKinematics.Point farShoulder=point(
                shoulder.x-6f,shoulder.y+10f);
        AvatarKinematics.Point neck=point(
                shoulder.x+(float)Math.sin(angle)*11f,
                shoulder.y-(float)Math.cos(angle)*11f);
        AvatarKinematics.Point head=point(
                shoulder.x+(float)Math.sin(angle)*33f,
                shoulder.y-(float)Math.cos(angle)*33f);
        AvatarRigConstraints.TwoBone arm=AvatarRigConstraints.twoBone(
                shoulder,point(wx,wy),spec.upperArm,spec.forearm,1,0f,170f);
        AvatarRigConstraints.TwoBone backArm=AvatarRigConstraints.twoBone(
                farShoulder,point(wx-14f,wy-5f),
                spec.upperArm,spec.forearm,1,0f,170f);
        return new AvatarKinematics.Pose(
                ankle,farAnkle,leg.joint,backLeg.joint,hip,farHip,
                shoulder,farShoulder,neck,head,
                arm.joint,arm.end,backArm.joint,backArm.end);
    }

    public static boolean handsPlanted(float phase){
        return Float.isFinite(phase)&&phase>=.25f&&phase<=.75f;
    }
    public static boolean feetTravelling(float phase){
        return Float.isFinite(phase)&&
                (phase>.25f&&phase<.40f||phase>.60f&&phase<.75f);
    }
    private static float mix(float a,float b,float t){
        return a+(b-a)*t;
    }
    private static AvatarKinematics.Point point(float x,float y){
        return new AvatarKinematics.Point(x,y);
    }
}
