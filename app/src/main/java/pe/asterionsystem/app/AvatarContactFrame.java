package pe.asterionsystem.app;

/**
 * Explicit ground-contact phases by scene. Camera projection cannot tell
 * whether a sole/palm is grounded: contact is an authored state, not
 * reconstructed from two projected joint coordinates.
 */
public final class AvatarContactFrame {
    public enum Ground { NONE,SOLE,TOE,PALM }
    public final Ground leftFoot,rightFoot,leftHand,rightHand;
    public final AvatarOrientationRules.Posture posture;
    public final boolean airborne;

    private AvatarContactFrame(Ground leftFoot,Ground rightFoot,
                               Ground leftHand,Ground rightHand,
                               AvatarOrientationRules.Posture posture,
                               boolean airborne){
        this.leftFoot=leftFoot;this.rightFoot=rightFoot;
        this.leftHand=leftHand;this.rightHand=rightHand;
        this.posture=posture;this.airborne=airborne;
        if(airborne&&(leftFoot!=Ground.NONE||rightFoot!=Ground.NONE))
            throw new IllegalArgumentException("An airborne foot cannot be planted");
        if(posture==AvatarOrientationRules.Posture.PRONE_GROUND_FACING&&
                (leftHand==Ground.PALM||rightHand==Ground.PALM)&&
                AvatarOrientationRules.visibleSurface(
                    AvatarOrientationRules.forPosture(posture).palmNormal,
                    AvatarOrientationRules.Camera.TOP))
            throw new IllegalArgumentException("Palms facing top camera");
    }

    public static AvatarContactFrame forCamera(
            AvatarMultiViewKinematics.ViewMode camera,float phase){
        checkPhase(phase);
        if(camera==null)throw new IllegalArgumentException("Camera required");
        if(camera==AvatarMultiViewKinematics.ViewMode.SIDE)
            throw new IllegalArgumentException("Use forSide with motion");
        if(camera==AvatarMultiViewKinematics.ViewMode.TOP)
            return new AvatarContactFrame(Ground.TOE,Ground.TOE,
                    Ground.PALM,Ground.PALM,
                    AvatarOrientationRules.Posture.PRONE_GROUND_FACING,false);
        boolean jumping=camera==AvatarMultiViewKinematics.ViewMode.FRONT_JACKS;
        boolean inFlight=jumping&&
                Math.abs(Math.sin(phase*Math.PI*2d))>.1d;
        Ground feet=inFlight?Ground.NONE:Ground.SOLE;
        return new AvatarContactFrame(feet,feet,Ground.NONE,Ground.NONE,
                AvatarOrientationRules.Posture.STANDING,inFlight);
    }

    public static AvatarContactFrame forSide(
            AvatarMotionEngine.Motion motion,float phase){
        checkPhase(phase);
        if(motion==null)throw new IllegalArgumentException("Motion required");
        switch(motion){
            case MARCH:
                return new AvatarContactFrame(Ground.NONE,Ground.SOLE,
                        Ground.NONE,Ground.NONE,
                        AvatarOrientationRules.Posture.STANDING,false);
            case BURPEE:
                boolean hands=AvatarBurpeeKinematics.handsPlanted(phase);
                boolean travel=AvatarBurpeeKinematics.feetTravelling(phase);
                Ground feet=travel?Ground.NONE:
                        hands?Ground.TOE:Ground.SOLE;
                return new AvatarContactFrame(feet,feet,
                        hands?Ground.PALM:Ground.NONE,
                        hands?Ground.PALM:Ground.NONE,
                        hands?AvatarOrientationRules.Posture.PRONE_GROUND_FACING
                             :AvatarOrientationRules.Posture.STANDING,false);
            default:
                return new AvatarContactFrame(Ground.SOLE,Ground.SOLE,
                        Ground.NONE,Ground.NONE,
                        AvatarOrientationRules.Posture.STANDING,false);
        }
    }

    public void assertProneUpperCamera(){
        if(posture!=AvatarOrientationRules.Posture.PRONE_GROUND_FACING||
                leftFoot!=Ground.TOE||rightFoot!=Ground.TOE||
                leftHand!=Ground.PALM||rightHand!=Ground.PALM||
                airborne)
            throw new IllegalStateException("Overhead prone contact mismatch");
    }

    private static void checkPhase(float phase){
        if(!Float.isFinite(phase)||phase<0f||phase>1f)
            throw new IllegalArgumentException("Phase must be normalized");
    }
}
