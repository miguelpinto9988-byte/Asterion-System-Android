package pe.asterionsystem.app;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** Finite 2D illustrative offsets, not measured medical joint angles. */
public final class AvatarJointTimeline {
    public enum Joint {
        NECK_PITCH("cuello, inclinación"),NECK_TURN("cuello, giro"),
        LEFT_SHOULDER("hombro izquierdo, flexión"),RIGHT_SHOULDER("hombro derecho, flexión"),
        LEFT_SHOULDER_SIDE("hombro izquierdo, abducción"),RIGHT_SHOULDER_SIDE("hombro derecho, abducción"),
        LEFT_ELBOW("codo izquierdo"),RIGHT_ELBOW("codo derecho"),
        LEFT_WRIST("muñeca izquierda"),RIGHT_WRIST("muñeca derecha"),
        LEFT_SCAPULA("escápula izquierda"),RIGHT_SCAPULA("escápula derecha"),
        SPINE_BEND("tronco, inclinación"),SPINE_TURN("tronco, giro"),PELVIS("pelvis"),
        LEFT_HIP("cadera izquierda, flexión"),RIGHT_HIP("cadera derecha, flexión"),
        LEFT_HIP_SIDE("cadera izquierda, abducción"),RIGHT_HIP_SIDE("cadera derecha, abducción"),
        LEFT_KNEE("rodilla izquierda"),RIGHT_KNEE("rodilla derecha"),
        LEFT_ANKLE("tobillo izquierdo"),RIGHT_ANKLE("tobillo derecho"),
        NECK_SIDE("cuello, inclinación lateral");
        public final String label;
        Joint(String label){this.label=label;}
    }
    public enum Command {
        SQUAT,LUNGE,HIP_HINGE,HIP_EXTENSION,STEP_UP,ANKLE_CONTROL,KNEE_FLEXION,
        KNEE_EXTENSION,HIP_ABDUCTION,FOOT_CONTROL,LOWER_HOLD,JUMP,SEAT_TRANSITION,FOOTWORK,
        PUSH,PULL,ELBOW_FLEXION,ELBOW_EXTENSION,SHOULDER_RAISE,SHOULDER_ABDUCTION,
        SHOULDER_ROTATION,SCAPULAR_CONTROL,WRIST_HAND,SPORT_UPPER,
        TRUNK_FLEXION,TRUNK_EXTENSION,TRUNK_ROTATION,CORE_BRACE,QUADRUPED,
        TRUNK_REACH,BREATHING,LATERAL_BRACE,BALANCE,COORDINATION,HIP_MOBILITY,
        SPINE_MOBILITY,SHOULDER_MOBILITY,ANKLE_MOBILITY,NECK_MOBILITY,STRETCH,
        WALK,RUN,BIKE,ROW,ELLIPTICAL,AEROBIC_STEP,DANCE,SWIM,CARDIO_SPORT,
        LATERAL_STEP,NECK_SIDE_TILT,SEATED_TOE_RAISE,SEATED_FORWARD_REACH,
        GLUTE_BRIDGE_PAUSE,SEATED_KNEE_EXTENSION_SINGLE,
        SEATED_KNEE_EXTENSION_PAUSE,SEATED_KNEE_EXTENSION_ALTERNATING
    }
    private final Command command;
    private final EnumMap<Joint,Float> targets=new EnumMap<>(Joint.class);
    public AvatarJointTimeline(Command command){
        if(command==null)throw new IllegalArgumentException("Comando requerido");
        this.command=command;assign();
        for(float v:targets.values())if(!Float.isFinite(v)||Math.abs(v)>85f)
            throw new IllegalStateException("Desplazamiento ilustrativo excesivo");
    }
    private void one(Joint joint,float value){targets.put(joint,value);}
    private void pair(Joint a,Joint b,float value){one(a,value);one(b,value);}
    private void legs(float hip,float knee,float ankle){
        pair(Joint.LEFT_HIP,Joint.RIGHT_HIP,hip);
        pair(Joint.LEFT_KNEE,Joint.RIGHT_KNEE,knee);
        pair(Joint.LEFT_ANKLE,Joint.RIGHT_ANKLE,ankle);
    }
    private void arms(float shoulder,float elbow,float wrist){
        pair(Joint.LEFT_SHOULDER,Joint.RIGHT_SHOULDER,shoulder);
        pair(Joint.LEFT_ELBOW,Joint.RIGHT_ELBOW,elbow);
        pair(Joint.LEFT_WRIST,Joint.RIGHT_WRIST,wrist);
    }
    private void assign(){
        switch(command){
            case SQUAT:legs(48,65,12);one(Joint.SPINE_BEND,18);break;
            case LUNGE:legs(39,58,9);one(Joint.PELVIS,6);break;
            case HIP_HINGE:legs(24,8,0);one(Joint.SPINE_BEND,42);break;
            case HIP_EXTENSION:legs(-18,18,0);one(Joint.PELVIS,12);break;
            case STEP_UP:legs(38,42,12);break;
            case ANKLE_CONTROL:legs(0,0,19);break;
            case KNEE_FLEXION:legs(7,48,5);break;
            case KNEE_EXTENSION:legs(22,32,0);break;
            case HIP_ABDUCTION:pair(Joint.LEFT_HIP_SIDE,Joint.RIGHT_HIP_SIDE,30);break;
            case FOOT_CONTROL:legs(0,0,8);break;
            case LOWER_HOLD:legs(16,19,4);break;
            case JUMP:legs(26,41,16);arms(17,0,0);break;
            case SEAT_TRANSITION:legs(42,54,9);one(Joint.SPINE_BEND,18);break;
            case FOOTWORK:legs(21,30,12);one(Joint.PELVIS,5);break;
            case PUSH:arms(38,44,6);break;
            case PULL:arms(24,61,5);pair(Joint.LEFT_SCAPULA,Joint.RIGHT_SCAPULA,8);break;
            case ELBOW_FLEXION:arms(0,74,5);break;
            case ELBOW_EXTENSION:arms(13,52,0);break;
            case SHOULDER_RAISE:arms(77,6,0);break;
            case SHOULDER_ABDUCTION:pair(Joint.LEFT_SHOULDER_SIDE,Joint.RIGHT_SHOULDER_SIDE,74);break;
            case SHOULDER_ROTATION:arms(17,44,11);pair(Joint.LEFT_SCAPULA,Joint.RIGHT_SCAPULA,5);break;
            case SCAPULAR_CONTROL:pair(Joint.LEFT_SCAPULA,Joint.RIGHT_SCAPULA,12);arms(13,3,0);break;
            case WRIST_HAND:pair(Joint.LEFT_WRIST,Joint.RIGHT_WRIST,26);break;
            case SPORT_UPPER:arms(45,30,12);one(Joint.SPINE_TURN,9);break;
            case TRUNK_FLEXION:one(Joint.SPINE_BEND,36);legs(13,10,0);break;
            case TRUNK_EXTENSION:one(Joint.SPINE_BEND,-21);legs(11,7,0);break;
            case TRUNK_ROTATION:one(Joint.SPINE_TURN,31);arms(20,9,0);break;
            case CORE_BRACE:one(Joint.PELVIS,8);one(Joint.SPINE_BEND,4);break;
            case QUADRUPED:arms(26,14,2);legs(31,36,0);break;
            case TRUNK_REACH:one(Joint.SPINE_BEND,15);arms(48,7,0);break;
            case BREATHING:one(Joint.SPINE_BEND,2);break;
            case LATERAL_BRACE:one(Joint.SPINE_TURN,10);break;
            case BALANCE:one(Joint.LEFT_HIP,27);one(Joint.LEFT_KNEE,30);break;
            case COORDINATION:one(Joint.LEFT_HIP,25);one(Joint.RIGHT_SHOULDER,34);break;
            case HIP_MOBILITY:legs(24,25,4);break;
            case SPINE_MOBILITY:one(Joint.SPINE_BEND,18);one(Joint.SPINE_TURN,14);break;
            case SHOULDER_MOBILITY:arms(52,7,0);break;
            case ANKLE_MOBILITY:legs(0,4,19);break;
            case NECK_MOBILITY:one(Joint.NECK_TURN,18);one(Joint.NECK_PITCH,8);break;
            case STRETCH:legs(18,9,3);one(Joint.SPINE_BEND,16);break;
            case WALK:legs(30,37,10);arms(22,3,0);break;
            case RUN:legs(48,62,17);arms(36,38,3);break;
            case BIKE:legs(54,68,18);arms(34,19,0);break;
            case ROW:legs(36,46,7);arms(53,47,0);one(Joint.SPINE_BEND,14);break;
            case ELLIPTICAL:legs(33,44,12);arms(34,7,0);break;
            case AEROBIC_STEP:legs(31,42,11);arms(24,0,0);break;
            case DANCE:legs(28,33,12);arms(51,15,3);break;
            case SWIM:legs(24,11,12);arms(72,16,5);break;
            case CARDIO_SPORT:legs(41,49,12);arms(39,32,7);break;
            case LATERAL_STEP:
                one(Joint.RIGHT_HIP_SIDE,25);one(Joint.RIGHT_KNEE,20);
                one(Joint.RIGHT_ANKLE,8);break;
            case NECK_SIDE_TILT:one(Joint.NECK_SIDE,12);break;
            case SEATED_TOE_RAISE:
                pair(Joint.LEFT_ANKLE,Joint.RIGHT_ANKLE,22);break;
            case SEATED_FORWARD_REACH:
                one(Joint.SPINE_BEND,21);arms(64,0,0);break;
            case GLUTE_BRIDGE_PAUSE:
                pair(Joint.LEFT_HIP,Joint.RIGHT_HIP,-20);break;
            case SEATED_KNEE_EXTENSION_SINGLE:
            case SEATED_KNEE_EXTENSION_PAUSE:
                one(Joint.LEFT_KNEE,65);break;
            case SEATED_KNEE_EXTENSION_ALTERNATING:
                pair(Joint.LEFT_KNEE,Joint.RIGHT_KNEE,65);break;
            default:throw new IllegalStateException("Comando sin trayectoria");
        }
    }
    public Command command(){return command;}
    public Map<Joint,Float> peakOffsets(){return Collections.unmodifiableMap(targets);}
    /**
     * C2 ease-in/out at both transitions and the loop seam.
     * 0-.08 preparation, .08-.42 action, .42-.58 hold,
     * .58-.94 return, .94-1 neutral. Values are screen-space
     * deltas from a neutral illustration, not clinical ROM.
     */
    public float weight(float phase){
        if(!Float.isFinite(phase)||phase<0||phase>1)
            throw new IllegalArgumentException("Fase 0..1 requerida");
        if(command==Command.GLUTE_BRIDGE_PAUSE||
                command==Command.SEATED_KNEE_EXTENSION_PAUSE)
            return heldWeight(phase);
        if(phase<=.08f||phase>=.94f)return 0;
        if(phase<.42f)return smooth((phase-.08f)/.34f);
        if(phase<=.58f)return 1;
        return 1-smooth((phase-.58f)/.36f);
    }
    /** Long hold for explicitly paused scenes, shared with the body engine. */
    public static float heldWeight(float phase){
        if(!Float.isFinite(phase)||phase<0f||phase>1f)
            throw new IllegalArgumentException("Fase 0..1 requerida");
        if(phase<=.08f||phase>=.94f)return 0f;
        if(phase<.27f)return smooth((phase-.08f)/.19f);
        if(phase<=.73f)return 1f;
        return 1f-smooth((phase-.73f)/.21f);
    }
    private static float smooth(float x){
        x=Math.max(0,Math.min(1,x));
        float result=x*x*x*(x*(x*6f-15f)+10f);
        return Math.max(0f,Math.min(1f,result));
    }
    public float offset(Joint joint,float phase){
        if(joint==null)throw new IllegalArgumentException("Articulación requerida");
        if(command==Command.SEATED_KNEE_EXTENSION_ALTERNATING){
            if(!Float.isFinite(phase)||phase<0f||phase>1f)
                throw new IllegalArgumentException("Fase 0..1 requerida");
            // Two distinct knee actions in the same loop: no simultaneous
            // near/far extension; both are neutral at the half-cycle seam.
            if(joint==Joint.LEFT_KNEE)
                return 65f*(phase<=.5f
                        ?AvatarKinematics.depthAtPhase(2f*phase):0f);
            if(joint==Joint.RIGHT_KNEE)
                return 65f*(phase>=.5f
                        ?AvatarKinematics.depthAtPhase(2f*(phase-.5f)):0f);
            return 0f;
        }
        if(command==Command.SEATED_KNEE_EXTENSION_SINGLE)
            return targets.getOrDefault(joint,0f)*AvatarKinematics.depthAtPhase(phase);
        return targets.getOrDefault(joint,0f)*weight(phase);
    }
    public String describe(float phase){
        StringBuilder b=new StringBuilder();
        for(Joint joint:Joint.values()){
            if(b.length()>0)b.append("\n");
            b.append(joint.label).append(": ").append(Math.round(offset(joint,phase)))
             .append("° de referencia 2D");
        }
        return b.toString();
    }
}
