package pe.asterionsystem.app;

/**
 * Immutable proportions for a stylized, educational 2D avatar.
 * Virtual Canvas units, not real-world body measurements.
 */
public final class AvatarBodySpec {
    public static final AvatarBodySpec DEFAULT=new AvatarBodySpec(
            94f,96f,118f,58f,63f,21f,31f,23f,33f,24f,26f,17f,1f);

    public final float upperLeg,lowerLeg,torso,upperArm,forearm;
    public final float waistHalfWidth,chestHalfWidth,shoulderHalfWidth;
    public final float thighWidth,calfWidth,upperArmWidth,forearmWidth,headScale;

    public AvatarBodySpec(float upperLeg,float lowerLeg,float torso,
                          float upperArm,float forearm,float waistHalfWidth,
                          float chestHalfWidth,float shoulderHalfWidth,
                          float thighWidth,float calfWidth,float upperArmWidth,
                          float forearmWidth,float headScale) {
        this.upperLeg=valid(upperLeg);
        this.lowerLeg=valid(lowerLeg);
        this.torso=valid(torso);
        this.upperArm=valid(upperArm);
        this.forearm=valid(forearm);
        this.waistHalfWidth=valid(waistHalfWidth);
        this.chestHalfWidth=valid(chestHalfWidth);
        this.shoulderHalfWidth=valid(shoulderHalfWidth);
        this.thighWidth=valid(thighWidth);
        this.calfWidth=valid(calfWidth);
        this.upperArmWidth=valid(upperArmWidth);
        this.forearmWidth=valid(forearmWidth);
        this.headScale=valid(headScale);
    }
    private static float valid(float value) {
        if(!Float.isFinite(value)||value<=0f||value>1000f)
            throw new IllegalArgumentException("Proporción finita y positiva");
        return value;
    }
}
