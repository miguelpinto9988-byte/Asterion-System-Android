package pe.asterionsystem.app;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Explicit per-ID recipes. No catch-all, fuzzy name matching or fallback pose. */
public final class AvatarVariantScene {
    public enum Movement { CURL,RAISE,REACH,PRESS,ROW,KNEE_EXTENSION,
        SEATED_MARCH,ARM_SWING,HEEL_SLIDE,HINGE,SQUAT,LUNGE,
        HAMMER_CURL,DEADLIFT,TERMINAL_KNEE,STANDING_LEG_CURL,
        WALL_REACH,WALL_SLIDE,WALL_PRESS,MARCH_STABLE,ARM_ALTERNATION,
        MARCH_KNEES,MARCH_ARMS,HEEL_TAP,HEEL_TAP_ARMS,BACK_STEP }
    public enum Posture { STANDING,SEATED }
    public enum Pattern { BOTH,NEAR,ALTERNATING }
    public enum Prop { NONE,DUMBBELLS,BAR,BAND_FEET,BAND_ANCHOR,TOWEL,
        GOBLET,FRONT_DUMBBELLS,BAR_FRONT,BAR_BACK,
        HAMMER_DUMBBELLS,KETTLEBELL,BAND_SHOULDERS,BAND_KNEE,WALL,CHAIR_SUPPORT }
    static final float WALL_X=264f; // Fixed in-scene support plane (virtual Canvas units).
    public static final class Recipe {
        public final String id,label,equipment,execution;
        public final Movement movement;
        public final Posture posture;
        public final Pattern pattern;
        public final Prop prop;
        public final float amplitude;
        public final boolean hold,supervision;
        public final int level;
        private Recipe(String id,String label,String movement,String posture,float amplitude,
                String pattern,String prop,boolean hold,String equipment,String execution,
                int level,boolean supervision){
            this.id=id;this.label=label;this.movement=Movement.valueOf(movement);
            this.posture=Posture.valueOf(posture);this.amplitude=amplitude;
            this.pattern=Pattern.valueOf(pattern);this.prop=Prop.valueOf(prop);
            this.hold=hold;this.equipment=equipment;this.execution=execution;
            this.level=level;this.supervision=supervision;
            if(!Float.isFinite(amplitude)||amplitude<=0f||amplitude>85f)
                throw new IllegalArgumentException("Invalid authored amplitude");
        }
        public AvatarJointTimeline.Command command(){
            switch(movement){
                case CURL:case HAMMER_CURL:return AvatarJointTimeline.Command.ELBOW_FLEXION;
                case RAISE:case ARM_SWING:case WALL_REACH:case WALL_SLIDE:
                    return AvatarJointTimeline.Command.SHOULDER_RAISE;
                case REACH:case MARCH_STABLE:case ARM_ALTERNATION:
                    return AvatarJointTimeline.Command.TRUNK_REACH;
                case PRESS:case WALL_PRESS:return AvatarJointTimeline.Command.PUSH;
                case ROW:return AvatarJointTimeline.Command.PULL;
                case KNEE_EXTENSION:case TERMINAL_KNEE:return AvatarJointTimeline.Command.KNEE_EXTENSION;
                case SEATED_MARCH:return AvatarJointTimeline.Command.HIP_MOBILITY;
                case HEEL_SLIDE:case STANDING_LEG_CURL:return AvatarJointTimeline.Command.KNEE_FLEXION;
                case HINGE:case DEADLIFT:return AvatarJointTimeline.Command.HIP_HINGE;
                case SQUAT:return AvatarJointTimeline.Command.SQUAT;
                case LUNGE:return AvatarJointTimeline.Command.LUNGE;
                case MARCH_KNEES:case MARCH_ARMS:case HEEL_TAP:
                case HEEL_TAP_ARMS:case BACK_STEP:
                    return AvatarJointTimeline.Command.AEROBIC_STEP;
                default:throw new IllegalStateException("Unauthored movement");
            }
        }
        public void requireConsistent(AvatarExerciseCommandCatalog.Entry e){
            if(e.clip!=AvatarExerciseCommandCatalog.Clip.AUTHORED_VARIANT||!e.canIllustrate()
                    ||e.camera!=AvatarMultiViewKinematics.ViewMode.SIDE
                    ||e.command!=command()||!equipment.equals(e.equipment)
                    ||!execution.equals(e.execution)||level!=e.level||supervision!=e.supervision)
                throw new IllegalArgumentException("Recipe/catalog mismatch: "+id);
        }
        /** Offsets measured from this recipe's actual 2D poses, not a generic command. */
        public String describe(float phase){
            AvatarPose rest=at(this,0f,AvatarBodySpec.DEFAULT).pose;
            AvatarPose now=at(this,phase,AvatarBodySpec.DEFAULT).pose;
            StringBuilder result=new StringBuilder();
            for(AvatarJointTimeline.Joint joint:AvatarJointTimeline.Joint.values()){
                if(result.length()>0)result.append("\n");
                result.append(joint.label).append(": ")
                        .append(Math.round(angle(now,joint)-angle(rest,joint)))
                        .append("° de cambio proyectado 2D");
            }
            return result.toString();
        }
        private float angle(AvatarPose p,AvatarJointTimeline.Joint j){
            switch(j){
                case LEFT_ELBOW:return 180f-AvatarPoseGuard.hingeAngle(p.shoulder,p.elbow,p.wrist);
                case RIGHT_ELBOW:return 180f-AvatarPoseGuard.hingeAngle(p.farShoulder,p.farElbow,p.farWrist);
                case LEFT_KNEE:return 180f-AvatarPoseGuard.hingeAngle(p.hip,p.knee,p.ankle);
                case RIGHT_KNEE:return 180f-AvatarPoseGuard.hingeAngle(p.farHip,p.farKnee,p.farAnkle);
                case LEFT_HIP:return 180f-AvatarPoseGuard.hingeAngle(p.shoulder,p.hip,p.knee);
                case RIGHT_HIP:return 180f-AvatarPoseGuard.hingeAngle(p.farShoulder,p.farHip,p.farKnee);
                case LEFT_SHOULDER:return AvatarPoseGuard.hingeAngle(p.hip,p.shoulder,p.elbow);
                case RIGHT_SHOULDER:return AvatarPoseGuard.hingeAngle(p.farHip,p.farShoulder,p.farElbow);
                case SPINE_BEND:return 90f+(float)Math.toDegrees(Math.atan2(p.shoulder.y-p.hip.y,p.shoulder.x-p.hip.x));
                default:return 0f;
            }
        }
        public String phaseLabel(float phase){
            checkPhase(phase);
            if(pattern==Pattern.ALTERNATING){
                float local=phase<=.5f?phase*2f:(phase-.5f)*2f;
                return (phase<=.5f?"Primer lado · ":"Segundo lado · ")+
                        (local<.08f||local>.94f?"preparación":
                         local<.42f?"ejecución":local<.58f?"control":"retorno");
            }
            return phase<.08f||phase>.94f?"Preparación":phase<(hold?.27f:.42f)
                    ?"Ejecución":phase<(hold?.73f:.58f)?"Mantener posición":"Retorno";
        }
    }
    private static final Map<String,Recipe> RECIPES;
    static {
        LinkedHashMap<String,Recipe> rows=new LinkedHashMap<>();
        add(rows,"curl_biceps_v01","Curl de bíceps con banda ligera","CURL","STANDING",75f,"BOTH","BAND_FEET",false,"banda","resistencia",1,false);
        add(rows,"curl_biceps_v02","Curl de bíceps sentado con mancuernas","CURL","SEATED",75f,"BOTH","DUMBBELLS",false,"mancuerna+silla","carga_externa",2,false);
        add(rows,"curl_biceps_v03","Curl de bíceps de pie alterno","CURL","STANDING",75f,"ALTERNATING","DUMBBELLS",false,"mancuerna","alterno",2,false);
        add(rows,"curl_biceps_v04","Curl de bíceps bilateral con mancuernas","CURL","STANDING",75f,"BOTH","DUMBBELLS",false,"mancuerna","bilateral",3,false);
        add(rows,"curl_biceps_v06","Curl de bíceps con barra","CURL","STANDING",75f,"BOTH","BAR",false,"barra","carga_externa",4,true);
        add(rows,"elevacion_frontal_v02","Elevación frontal con banda","RAISE","STANDING",75f,"BOTH","BAND_FEET",false,"banda","resistencia",2,false);
        add(rows,"elevacion_frontal_v03","Elevación frontal alterna con mancuerna","RAISE","STANDING",75f,"ALTERNATING","DUMBBELLS",false,"mancuerna","alterno",2,false);
        add(rows,"elevacion_frontal_v04","Elevación frontal bilateral con mancuernas","RAISE","STANDING",75f,"BOTH","DUMBBELLS",false,"mancuerna","bilateral",3,false);
        add(rows,"elevacion_frontal_sentado_v01","Elevación frontal corta de brazos sentado","RAISE","SEATED",35f,"BOTH","NONE",false,"silla","controlado",1,false);
        add(rows,"elevacion_frontal_sentado_v02","Elevación frontal de brazos sentado hasta rango cómodo","RAISE","SEATED",75f,"BOTH","NONE",false,"silla","controlado",2,false);
        add(rows,"alcance_manos_frente_sentado_v01","Alcance frontal corto con espalda apoyada","REACH","SEATED",30f,"BOTH","NONE",false,"silla","controlado",1,false);
        add(rows,"alcance_manos_frente_sentado_v02","Alcance frontal sentado sin separar demasiado el tronco","REACH","SEATED",55f,"BOTH","NONE",false,"silla","controlado",2,false);
        add(rows,"alcance_brazos_coordinado_v01","Alcance simultáneo de manos al frente sentado","REACH","SEATED",55f,"BOTH","NONE",false,"silla","coordinacion",1,false);
        add(rows,"press_maquina_horizontal_v01","Movimiento de press sentado sin carga","PRESS","SEATED",55f,"BOTH","NONE",false,"silla","resistencia",1,false);
        add(rows,"remo_banda_sentado_v01","Tracción suave de brazos sentado sin material","ROW","SEATED",55f,"BOTH","NONE",false,"silla","resistencia",1,false);
        add(rows,"remo_banda_sentado_v02","Remo bilateral con banda sentado","ROW","SEATED",55f,"BOTH","BAND_ANCHOR",false,"banda+silla","resistencia",2,false);
        add(rows,"remo_banda_sentado_v03","Remo alterno con banda sentado","ROW","SEATED",55f,"ALTERNATING","BAND_ANCHOR",false,"banda+silla","resistencia",3,false);
        add(rows,"remo_polea_baja_v01","Retracción suave de brazos sentado sin resistencia","ROW","SEATED",45f,"BOTH","NONE",false,"silla","resistencia",1,false);
        add(rows,"remo_v01","Remo sentado con banda ligera","ROW","SEATED",55f,"BOTH","BAND_ANCHOR",false,"banda+silla","resistencia",1,false);
        add(rows,"remo_v02","Remo de pie con banda","ROW","STANDING",55f,"BOTH","BAND_ANCHOR",false,"banda","resistencia",1,false);
        add(rows,"extension_rodilla_sentado_corta_v01","Extensión de rodilla sentado con recorrido corto","KNEE_EXTENSION","SEATED",25f,"NEAR","NONE",false,"silla","controlado",1,false);
        add(rows,"extension_rodilla_sentado_corta_v02","Extensión de rodilla sentado hasta rango cómodo","KNEE_EXTENSION","SEATED",55f,"NEAR","NONE",false,"silla","controlado",2,false);
        add(rows,"extension_cuadriceps_v01","Extensión de rodilla sentado sin carga","KNEE_EXTENSION","SEATED",65f,"NEAR","NONE",false,"silla","unilateral",1,false);
        add(rows,"estiramiento_cuadriceps_v01","Movilidad de rodilla sentado","KNEE_EXTENSION","SEATED",30f,"NEAR","NONE",false,"silla","movilidad",1,false);
        add(rows,"elevar_rodilla_sentado_v01","Elevación pequeña de rodilla sentado","SEATED_MARCH","SEATED",12f,"NEAR","NONE",false,"silla","controlado",1,false);
        add(rows,"elevar_rodilla_sentado_v02","Elevación alterna y cómoda de rodillas sentado","SEATED_MARCH","SEATED",22f,"ALTERNATING","NONE",false,"silla","controlado",2,false);
        add(rows,"elevacion_rodillas_v01","Marcha de rodillas sentado","SEATED_MARCH","SEATED",25f,"ALTERNATING","NONE",false,"silla","alterno",1,false);
        add(rows,"flexor_cadera_v02","Flexión de cadera sentado","SEATED_MARCH","SEATED",20f,"NEAR","NONE",false,"silla","movilidad",1,false);
        add(rows,"marcha_sentada_recreativa_v01","Elevación suave alterna de pies sentado","SEATED_MARCH","SEATED",12f,"ALTERNATING","NONE",false,"silla","controlado",1,false);
        add(rows,"marcha_sentada_recreativa_v02","Marcha de pies sentado a ritmo cómodo","SEATED_MARCH","SEATED",22f,"ALTERNATING","NONE",false,"silla","controlado",2,false);
        add(rows,"brazos_cardio_sentado_v01","Balanceo breve de brazos sentado","ARM_SWING","SEATED",20f,"ALTERNATING","NONE",false,"silla","controlado",1,false);
        add(rows,"brazos_cardio_sentado_v02","Alternancia cómoda de brazos sentado","ARM_SWING","SEATED",40f,"ALTERNATING","NONE",false,"silla","controlado",2,false);
        add(rows,"talon_deslizante_sentado_v01","Deslizar un pie hacia atrás sentado en silla","HEEL_SLIDE","SEATED",22f,"NEAR","NONE",false,"silla","controlado",1,false);
        add(rows,"talon_deslizante_sentado_v02","Deslizamiento alternado de pies en suelo sentado","HEEL_SLIDE","SEATED",30f,"ALTERNATING","NONE",false,"silla","controlado",2,false);
        add(rows,"talon_deslizante_sentado_v03","Deslizamiento de talones sentado con toalla","HEEL_SLIDE","SEATED",30f,"BOTH","TOWEL",false,"silla+toalla","controlado",3,false);
        add(rows,"deslizamiento_pie_sentado_v01","Deslizamiento suave del pie debajo de la silla","HEEL_SLIDE","SEATED",22f,"NEAR","NONE",false,"silla","controlado",1,false);
        add(rows,"deslizamiento_pie_sentado_v02","Deslizamiento adelante y atrás del pie sentado","HEEL_SLIDE","SEATED",38f,"NEAR","NONE",false,"silla","controlado",2,false);
        add(rows,"buenos_dias_v01","Buenos días sentado sin carga","HINGE","SEATED",25f,"BOTH","NONE",false,"silla","controlado",1,false);
        add(rows,"buenos_dias_v02","Buenos días de pie sin carga","HINGE","STANDING",35f,"BOTH","NONE",false,"sin_equipo","bilateral",2,false);
        add(rows,"buenos_dias_v04","Buenos días con mancuerna al pecho","HINGE","STANDING",35f,"BOTH","GOBLET",false,"mancuerna","carga_externa",4,true);
        add(rows,"buenos_dias_v05","Buenos días con barra ligera","HINGE","STANDING",35f,"BOTH","BAR_BACK",false,"barra","carga_externa",5,true);
        add(rows,"peso_muerto_rumano_v02","Bisagra con banda","HINGE","STANDING",40f,"BOTH","BAND_FEET",false,"banda","resistencia",2,false);
        add(rows,"peso_muerto_rumano_v03","Peso muerto rumano con mancuernas","HINGE","STANDING",40f,"BOTH","DUMBBELLS",false,"mancuerna","carga_externa",3,false);
        add(rows,"peso_muerto_rumano_v06","Peso muerto rumano con barra","HINGE","STANDING",40f,"BOTH","BAR",false,"barra","carga_externa",5,true);
        add(rows,"sentadilla_bilateral_v05","Sentadilla goblet","SQUAT","STANDING",1f,"BOTH","GOBLET",false,"mancuerna","carga_externa",3,false);
        add(rows,"sentadilla_bilateral_v06","Sentadilla frontal con mancuernas","SQUAT","STANDING",1f,"BOTH","FRONT_DUMBBELLS",false,"mancuerna","carga_externa",4,true);
        add(rows,"sentadilla_bilateral_v07","Sentadilla frontal con barra","SQUAT","STANDING",1f,"BOTH","BAR_FRONT",false,"barra","carga_externa",5,true);
        add(rows,"zancada_estatica_v02","Zancada estática corta","LUNGE","STANDING",0.5f,"BOTH","NONE",false,"sin_equipo","controlado",2,false);
        add(rows,"zancada_estatica_v04","Zancada estática con pausa","LUNGE","STANDING",1f,"BOTH","NONE",true,"sin_equipo","controlado",3,false);
        add(rows,"zancada_estatica_v05","Zancada estática con mancuernas","LUNGE","STANDING",1f,"BOTH","DUMBBELLS",false,"mancuerna","carga_externa",4,true);
        add(rows,"zancada_estatica_v06","Zancada estática con barra","LUNGE","STANDING",1f,"BOTH","BAR_BACK",false,"barra","carga_externa",5,true);
        add(rows,"curl_martillo_v01","Curl neutro con banda","HAMMER_CURL","STANDING",65f,"BOTH","BAND_FEET",false,"banda","resistencia",1,false);
        add(rows,"curl_martillo_v02","Curl martillo sentado alterno","HAMMER_CURL","SEATED",65f,"ALTERNATING","HAMMER_DUMBBELLS",false,"mancuerna+silla","alterno",2,false);
        add(rows,"curl_martillo_v03","Curl martillo de pie","HAMMER_CURL","STANDING",65f,"BOTH","HAMMER_DUMBBELLS",false,"mancuerna","bilateral",2,false);
        add(rows,"buenos_dias_v03","Buenos días con banda","HINGE","STANDING",30f,"BOTH","BAND_SHOULDERS",false,"banda","resistencia",3,false);
        add(rows,"peso_muerto_v03","Peso muerto con mancuerna","DEADLIFT","STANDING",36f,"BOTH","DUMBBELLS",false,"mancuerna","carga_externa",3,false);
        add(rows,"peso_muerto_v04","Peso muerto sumo con kettlebell","DEADLIFT","STANDING",38f,"BOTH","KETTLEBELL",false,"kettlebell","carga_externa",3,false);
        add(rows,"peso_muerto_v06","Peso muerto convencional con barra","DEADLIFT","STANDING",40f,"BOTH","BAR",false,"barra","carga_externa",5,true);
        add(rows,"extension_cuadriceps_v02","Extensión terminal de rodilla con banda","TERMINAL_KNEE","STANDING",12f,"NEAR","BAND_KNEE",false,"banda","resistencia",2,false);
        add(rows,"flexion_rodilla_bipeda_v01","Flexión mínima de una rodilla con apoyo en pared","STANDING_LEG_CURL","STANDING",13f,"NEAR","WALL",false,"pared","controlado",1,false);
        add(rows,"flexion_rodilla_bipeda_v02","Flexión alterna de rodilla de pie cerca de silla","STANDING_LEG_CURL","STANDING",24f,"ALTERNATING","CHAIR_SUPPORT",false,"silla","controlado",2,false);
        add(rows,"alcance_pared_brazo_v01","Alcance bajo de mano en pared","WALL_REACH","STANDING",24f,"NEAR","WALL",false,"pared","controlado",1,false);
        add(rows,"alcance_pared_brazo_v02","Alcance de mano en pared a altura cómoda","WALL_REACH","STANDING",44f,"NEAR","WALL",false,"pared","controlado",2,false);
        add(rows,"deslizamiento_manos_vertical_v01","Deslizamiento corto de manos por pared","WALL_SLIDE","STANDING",22f,"BOTH","WALL",false,"pared","controlado",1,false);
        add(rows,"deslizamiento_manos_vertical_v02","Deslizamiento vertical cómodo de ambas manos","WALL_SLIDE","STANDING",44f,"BOTH","WALL",false,"pared","controlado",2,false);
        add(rows,"apoyo_manos_pared_v01","Apoyo de manos en pared con codos flexibles","WALL_PRESS","STANDING",6f,"BOTH","WALL",false,"pared","controlado",1,false);
        add(rows,"apoyo_manos_pared_v02","Apoyo de manos en pared con traslado leve de peso","WALL_PRESS","STANDING",12f,"BOTH","WALL",false,"pared","controlado",2,false);
        add(rows,"marcha_estable_tronco_v01","Marcha muy suave manteniendo tronco erguido","MARCH_STABLE","STANDING",10f,"ALTERNATING","NONE",false,"sin_equipo","controlado",1,false);
        add(rows,"marcha_estable_tronco_v02","Marcha alterna en el sitio con tronco estable","MARCH_STABLE","STANDING",20f,"ALTERNATING","NONE",false,"sin_equipo","controlado",2,false);
        add(rows,"alternancia_hombros_pie_v01","Alternar brazos cortos con tronco quieto","ARM_ALTERNATION","STANDING",12f,"ALTERNATING","NONE",false,"sin_equipo","controlado",1,false);
        add(rows,"alternancia_hombros_pie_v02","Alternar alcances de hombros con tronco estable","ARM_ALTERNATION","STANDING",25f,"ALTERNATING","NONE",false,"sin_equipo","controlado",2,false);
        add(rows,"elevaciones_rodillas_cardio_v01","Elevación suave alterna de rodillas","MARCH_KNEES","STANDING",15f,"ALTERNATING","NONE",false,"sin_equipo","aerobico",1,false);
        add(rows,"elevaciones_rodillas_cardio_v03","Elevación alterna de rodilla con brazos coordinados","MARCH_ARMS","STANDING",24f,"ALTERNATING","NONE",false,"sin_equipo","coordinacion",2,false);
        add(rows,"talones_alternos_v01","Toques de talón adelante sin salto","HEEL_TAP","STANDING",13f,"ALTERNATING","NONE",false,"sin_equipo","aerobico",1,false);
        add(rows,"talones_alternos_v03","Toques alternos de talón con brazos suaves","HEEL_TAP_ARMS","STANDING",22f,"ALTERNATING","NONE",false,"sin_equipo","coordinacion",2,false);
        add(rows,"brazos_ritmicos_caminando_v01","Movimiento corto de brazos durante marcha lenta","MARCH_ARMS","STANDING",10f,"ALTERNATING","NONE",false,"sin_equipo","controlado",1,false);
        add(rows,"brazos_ritmicos_caminando_v02","Alternancia suave de brazos con marcha cómoda","MARCH_ARMS","STANDING",18f,"ALTERNATING","NONE",false,"sin_equipo","controlado",2,false);
        add(rows,"paso_atras_bajoimpacto_v01","Paso atrás pequeño y retorno estable","BACK_STEP","STANDING",10f,"NEAR","NONE",false,"sin_equipo","controlado",1,false);
        add(rows,"paso_atras_bajoimpacto_v02","Pasos atrás alternos sin salto a ritmo cómodo","BACK_STEP","STANDING",18f,"ALTERNATING","NONE",false,"sin_equipo","controlado",2,false);
        add(rows,"remo_banda_pecho_v01","Retracción de brazos abiertos sin material","ROW","STANDING",40f,"BOTH","NONE",false,"sin_equipo","resistencia",1,false);
        add(rows,"remo_banda_pecho_v02","Remo a pecho con banda elástica","ROW","STANDING",55f,"BOTH","BAND_ANCHOR",false,"banda","resistencia",2,false);
        RECIPES=Collections.unmodifiableMap(rows);
    }
    private static void add(Map<String,Recipe> rows,String id,String label,String movement,
            String posture,float amplitude,String pattern,String prop,boolean hold,
            String equipment,String execution,int level,boolean supervision){
        Recipe r=new Recipe(id,label,movement,posture,amplitude,pattern,prop,hold,
                equipment,execution,level,supervision);
        if(rows.put(id,r)!=null)throw new IllegalStateException("Duplicate recipe: "+id);
    }
    public static Map<String,Recipe> all(){return RECIPES;}
    public static Recipe find(String id){return RECIPES.get(id);}
    private AvatarVariantScene(){}
    public static final class Frame {
        public final Recipe recipe;
        public final AvatarKinematics.Pose pose;
        public final float phase,floorY;
        public final AvatarKinematics.Point nearRestFoot,farRestFoot;
        private Frame(Recipe r,AvatarKinematics.Pose p,float phase,
                AvatarKinematics.Point nearRest,AvatarKinematics.Point farRest){
            recipe=r;pose=p;this.phase=phase;nearRestFoot=nearRest;farRestFoot=farRest;
            floorY=nearRest.y+14f;
        }
    }
    private static void checkPhase(float p){
        if(!Float.isFinite(p)||p<0f||p>1f)throw new IllegalArgumentException("Phase 0..1 required");
    }
    private static float smooth(float t){
        t=Math.max(0f,Math.min(1f,t));return t*t*t*(t*(t*6f-15f)+10f);
    }
    private static float weight(float phase,boolean hold){
        if(hold)return AvatarJointTimeline.heldWeight(phase);
        if(phase<=.08f||phase>=.94f)return 0f;
        if(phase<.42f)return smooth((phase-.08f)/.34f);
        if(phase<=.58f)return 1f;
        return 1f-smooth((phase-.58f)/.36f);
    }
    public static float sideWeight(Recipe r,float phase,boolean far){
        checkPhase(phase);
        if(r.pattern==Pattern.NEAR&&far)return 0f;
        if(r.pattern!=Pattern.ALTERNATING)return weight(phase,r.hold);
        if(far)return phase>=.5f?weight(2f*(phase-.5f),r.hold):0f;
        return phase<=.5f?weight(2f*phase,r.hold):0f;
    }
    private static AvatarKinematics.Point pt(float x,float y){return new AvatarKinematics.Point(x,y);}
    private static AvatarKinematics.Point polar(AvatarKinematics.Point a,float length,float degrees){
        double rad=Math.toRadians(degrees);
        return pt(a.x+length*(float)Math.cos(rad),a.y+length*(float)Math.sin(rad));
    }
    private static AvatarKinematics.Point translate(AvatarKinematics.Point a,float x,float y){
        return pt(a.x+x,a.y+y);
    }
    private static AvatarRigConstraints.TwoBone solve(AvatarKinematics.Point root,
            AvatarKinematics.Point end,float upper,float lower,int bend){
        AvatarRigConstraints.TwoBone result=AvatarRigConstraints.twoBone(root,end,upper,lower,bend,0,179);
        if(result.targetLimited)throw new IllegalStateException("Authored contact out of reach");
        return result;
    }
    /** A frame contains the actual contacts/props; render and tests consume the same data. */
    public static Frame at(Recipe r,float phase,AvatarBodySpec b){
        if(r==null||b==null)throw new IllegalArgumentException("Recipe and rig required");
        checkPhase(phase);
        float w=weight(phase,r.hold),n=sideWeight(r,phase,false),f=sideWeight(r,phase,true);
        AvatarKinematics.Point hip,farHip,shoulder,farShoulder,knee,farKnee,ankle,farAnkle;
        AvatarKinematics.Point rest,farRest;
        if(r.posture==Posture.SEATED){
            hip=pt(142f,226f);farHip=pt(134f,225f);
            knee=polar(hip,b.upperLeg,12f);farKnee=polar(farHip,b.upperLeg,12f);
            ankle=polar(knee,b.lowerLeg,90f);farAnkle=polar(farKnee,b.lowerLeg,90f);
            rest=ankle;farRest=farAnkle;
            if(r.movement==Movement.KNEE_EXTENSION){
                ankle=polar(knee,b.lowerLeg,90f-r.amplitude*n);
                farAnkle=polar(farKnee,b.lowerLeg,90f-r.amplitude*f);
            }else if(r.movement==Movement.SEATED_MARCH){
                knee=polar(hip,b.upperLeg,12f-r.amplitude*n);
                farKnee=polar(farHip,b.upperLeg,12f-r.amplitude*f);
                ankle=polar(knee,b.lowerLeg,90f);
                farAnkle=polar(farKnee,b.lowerLeg,90f);
            }else if(r.movement==Movement.HEEL_SLIDE){
                ankle=translate(rest,-r.amplitude*n,0f);
                farAnkle=translate(farRest,-r.amplitude*f,0f);
                knee=solve(hip,ankle,b.upperLeg,b.lowerLeg,-1).joint;
                farKnee=solve(farHip,farAnkle,b.upperLeg,b.lowerLeg,-1).joint;
            }
            float tilt=r.movement==Movement.HINGE?r.amplitude*w:0f;
            shoulder=polar(hip,b.torso,-90f+tilt);
            farShoulder=polar(farHip,b.torso,-90f+tilt);
        }else{
            AvatarKinematics.Pose neutral=AvatarMotionEngine.pose(AvatarMotionEngine.Motion.ARM_RAISE,0,b);
            hip=neutral.hip;farHip=neutral.farHip;ankle=neutral.ankle;farAnkle=neutral.farAnkle;
            rest=ankle;farRest=farAnkle;knee=neutral.knee;farKnee=neutral.farKnee;
            if(r.movement==Movement.SQUAT){
                AvatarKinematics.Pose squat=AvatarKinematics.squat(w*r.amplitude,b);
                hip=squat.hip;farHip=squat.farHip;knee=squat.knee;farKnee=squat.farKnee;
                ankle=squat.ankle;farAnkle=squat.farAnkle;rest=ankle;farRest=farAnkle;
                shoulder=squat.shoulder;farShoulder=translate(shoulder,farHip.x-hip.x,farHip.y-hip.y);
            }else if(r.movement==Movement.LUNGE){
                // Direct depth mapping avoids conflating a half-depth lunge with timing.
                ankle=pt(217f,364f);farAnkle=pt(143f,363f);rest=ankle;farRest=farAnkle;
                hip=pt(178f-6f*w*r.amplitude,181f+42f*w*r.amplitude);
                farHip=translate(hip,-10f,-1f);
                knee=solve(hip,ankle,b.upperLeg,b.lowerLeg,-1).joint;
                farKnee=solve(farHip,farAnkle,b.upperLeg,b.lowerLeg,-1).joint;
                shoulder=polar(hip,b.torso,-83f+6f*w*r.amplitude);
                farShoulder=polar(farHip,b.torso,-83f+6f*w*r.amplitude);
            }else{
                float tilt=r.movement==Movement.HINGE?r.amplitude*w:0f;
                if(r.movement==Movement.DEADLIFT){
                    float depth=r.amplitude*w/45f;
                    hip=translate(hip,-15f*depth,28f*depth);
                    farHip=translate(farHip,-15f*depth,28f*depth);
                    knee=solve(hip,ankle,b.upperLeg,b.lowerLeg,-1).joint;
                    farKnee=solve(farHip,farAnkle,b.upperLeg,b.lowerLeg,-1).joint;
                    tilt=22f*depth;
                }else if(r.movement==Movement.TERMINAL_KNEE){
                    // Band tension is shown behind the knee; the sole remains planted.
                    hip=translate(hip,-.30f*r.amplitude*w,.65f*r.amplitude*w);
                    farHip=translate(farHip,-.30f*r.amplitude*w,.65f*r.amplitude*w);
                    knee=solve(hip,ankle,b.upperLeg,b.lowerLeg,-1).joint;
                    farKnee=solve(farHip,farAnkle,b.upperLeg,b.lowerLeg,-1).joint;
                }else if(r.movement==Movement.STANDING_LEG_CURL){
                    ankle=translate(rest,-r.amplitude*n,-1.3f*r.amplitude*n);
                    farAnkle=translate(farRest,-r.amplitude*f,-1.3f*r.amplitude*f);
                    knee=solve(hip,ankle,b.upperLeg,b.lowerLeg,-1).joint;
                    farKnee=solve(farHip,farAnkle,b.upperLeg,b.lowerLeg,-1).joint;
                }else if(r.movement==Movement.MARCH_STABLE||
                         r.movement==Movement.MARCH_KNEES||
                         r.movement==Movement.MARCH_ARMS){
                    ankle=translate(rest,.4f*r.amplitude*n,-1.8f*r.amplitude*n);
                    farAnkle=translate(farRest,.4f*r.amplitude*f,-1.8f*r.amplitude*f);
                    knee=solve(hip,ankle,b.upperLeg,b.lowerLeg,-1).joint;
                    farKnee=solve(farHip,farAnkle,b.upperLeg,b.lowerLeg,-1).joint;
                }else if(r.movement==Movement.HEEL_TAP||
                         r.movement==Movement.HEEL_TAP_ARMS){
                    ankle=translate(rest,1.6f*r.amplitude*n,-3f*n);
                    farAnkle=translate(farRest,1.6f*r.amplitude*f,-3f*f);
                    knee=solve(hip,ankle,b.upperLeg,b.lowerLeg,-1).joint;
                    farKnee=solve(farHip,farAnkle,b.upperLeg,b.lowerLeg,-1).joint;
                }else if(r.movement==Movement.BACK_STEP){
                    ankle=translate(rest,-2f*r.amplitude*n,-.35f*r.amplitude*n);
                    farAnkle=translate(farRest,-2f*r.amplitude*f,-.35f*r.amplitude*f);
                    knee=solve(hip,ankle,b.upperLeg,b.lowerLeg,-1).joint;
                    farKnee=solve(farHip,farAnkle,b.upperLeg,b.lowerLeg,-1).joint;
                }else if(r.movement==Movement.WALL_PRESS){
                    tilt=r.amplitude*w;
                }
                if(r.movement==Movement.HINGE){
                    hip=translate(hip,-12f*w,7f*w);farHip=translate(farHip,-12f*w,7f*w);
                    knee=solve(hip,ankle,b.upperLeg,b.lowerLeg,-1).joint;
                    farKnee=solve(farHip,farAnkle,b.upperLeg,b.lowerLeg,-1).joint;
                }
                shoulder=polar(hip,b.torso,-90f+tilt);
                farShoulder=polar(farHip,b.torso,-90f+tilt);
            }
        }
        AvatarKinematics.Point[] arm=arm(r,shoulder,n,b);
        AvatarKinematics.Point[] farArm=arm(r,farShoulder,f,b);
        float axis=(float)Math.toDegrees(Math.atan2(shoulder.y-hip.y,shoulder.x-hip.x));
        AvatarKinematics.Point neck=polar(shoulder,8f,axis),head=polar(neck,27f,axis);
        AvatarKinematics.Pose p=new AvatarKinematics.Pose(ankle,farAnkle,knee,farKnee,
                hip,farHip,shoulder,farShoulder,neck,head,arm[0],arm[1],farArm[0],farArm[1]);
        AvatarMotionEngine.assertSkeleton(p,b,.15f);
        AvatarRigConstraints.requireFixedLength(farHip,farShoulder,b.torso,.15f);
        return new Frame(r,p,phase,rest,farRest);
    }
    private static AvatarKinematics.Point[] arm(Recipe r,AvatarKinematics.Point shoulder,
            float w,AvatarBodySpec b){
        float upper=87f,lower=84f;
        switch(r.movement){
            case CURL:case HAMMER_CURL:lower=84f-r.amplitude*w;break;
            case RAISE:upper=87f-r.amplitude*w;lower=upper-3f;break;
            case ARM_SWING:upper=87f-r.amplitude*w;lower=upper-3f;break;
            case WALL_REACH:case WALL_SLIDE:case WALL_PRESS:{
                float height=r.movement==Movement.WALL_PRESS?112f:
                        (r.movement==Movement.WALL_REACH?125f:135f)-r.amplitude*w;
                AvatarRigConstraints.TwoBone a=solve(shoulder,pt(WALL_X,height),
                        b.upperArm,b.forearm,1);
                return new AvatarKinematics.Point[]{a.joint,a.end};
            }
            case ARM_ALTERNATION:case MARCH_ARMS:case HEEL_TAP_ARMS:
                upper=87f-r.amplitude*w*1.5f;lower=upper-3f;break;
            case ROW:case PRESS:case REACH:{
                float action=r.movement==Movement.ROW?1f-w:w;
                AvatarKinematics.Point hand=translate(shoulder,
                        44f+r.amplitude*action,20f);
                AvatarRigConstraints.TwoBone arm=solve(shoulder,hand,b.upperArm,b.forearm,1);
                return new AvatarKinematics.Point[]{arm.joint,arm.end};
            }
            default:break;
        }
        if(r.prop==Prop.CHAIR_SUPPORT){
            AvatarRigConstraints.TwoBone a=solve(shoulder,pt(246f,128f),
                    b.upperArm,b.forearm,1);
            return new AvatarKinematics.Point[]{a.joint,a.end};
        }
        if(r.prop==Prop.GOBLET||r.prop==Prop.FRONT_DUMBBELLS||r.prop==Prop.BAR_FRONT){
            AvatarKinematics.Point grip=translate(shoulder,35f,18f);
            AvatarRigConstraints.TwoBone arm=solve(shoulder,grip,b.upperArm,b.forearm,1);
            return new AvatarKinematics.Point[]{arm.joint,arm.end};
        }
        if(r.prop==Prop.BAR_BACK){
            AvatarKinematics.Point grip=translate(shoulder,-12f,5f);
            AvatarRigConstraints.TwoBone arm=solve(shoulder,grip,b.upperArm,b.forearm,-1);
            return new AvatarKinematics.Point[]{arm.joint,arm.end};
        }
        AvatarKinematics.Point elbow=polar(shoulder,b.upperArm,upper);
        AvatarKinematics.Point hand=polar(elbow,b.forearm,lower);
        return new AvatarKinematics.Point[]{elbow,hand};
    }
}
