package pe.asterionsystem.app;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Explicit, reviewable 1:1 catalog-to-scene evidence.
 *
 * A declared joint command alone does not authorize a body illustration:
 * posture, execution and equipment must match the actual authored scene.
 * Illustrations are not motion-capture or clinical form certifications.
 */
public final class AvatarDemonstrationManifest {
    private AvatarDemonstrationManifest(){}
    private static final class Signature {
        final AvatarExerciseCommandCatalog.Clip clip;
        final AvatarMultiViewKinematics.ViewMode camera;
        final String equipment,execution;
        final AvatarJointTimeline.Command command;
        Signature(AvatarExerciseCommandCatalog.Clip clip,
                  AvatarMultiViewKinematics.ViewMode camera,
                  String equipment,String execution,
                  AvatarJointTimeline.Command command){
            this.clip=clip;this.camera=camera;
            this.equipment=equipment;this.execution=execution;
            this.command=command;
        }
    }
    private static final Map<String,Signature> REVIEWED;
    static{
        LinkedHashMap<String,Signature> rows=new LinkedHashMap<>();
        register(rows,"sentadilla_bilateral_v03",
                AvatarExerciseCommandCatalog.Clip.SQUAT,"sin_equipo","bilateral",
                AvatarJointTimeline.Command.SQUAT);
        register(rows,"sentadilla_bilateral_v04",
                AvatarExerciseCommandCatalog.Clip.SQUAT,"sin_equipo","controlado",
                AvatarJointTimeline.Command.SQUAT);
        register(rows,"zancada_estatica_v03",
                AvatarExerciseCommandCatalog.Clip.LUNGE,"sin_equipo","bilateral",
                AvatarJointTimeline.Command.LUNGE);
        register(rows,"peso_muerto_rumano_v01",
                AvatarExerciseCommandCatalog.Clip.HIP_HINGE,"sin_equipo","controlado",
                AvatarJointTimeline.Command.HIP_HINGE);
        register(rows,"caminata_banda_v01",
                AvatarExerciseCommandCatalog.Clip.FRONT_SIDE_STEP,"sin_equipo","lateral",
                AvatarJointTimeline.Command.LATERAL_STEP);
        register(rows,"elevacion_lateral_v01",
                AvatarExerciseCommandCatalog.Clip.FRONT_LATERAL,"sin_equipo","controlado",
                AvatarJointTimeline.Command.SHOULDER_ABDUCTION);
        register(rows,"elevacion_frontal_v01",
                AvatarExerciseCommandCatalog.Clip.ARM_RAISE,"sin_equipo","controlado",
                AvatarJointTimeline.Command.SHOULDER_RAISE);
        register(rows,"equilibrio_unipodal_v04",
                AvatarExerciseCommandCatalog.Clip.FRONT_BALANCE,"sin_equipo","equilibrio",
                AvatarJointTimeline.Command.BALANCE);
        register(rows,"extension_cadera_bipeda_v01",
                AvatarExerciseCommandCatalog.Clip.HIP_EXTENSION_STAND,
                "sin_equipo","controlado",
                AvatarJointTimeline.Command.HIP_EXTENSION);
        register(rows,"inclinacion_cervical_suave_v01",
                AvatarExerciseCommandCatalog.Clip.FRONT_NECK_TILT,
                "sin_equipo","movilidad",
                AvatarJointTimeline.Command.NECK_SIDE_TILT);
        register(rows,"flexion_rodilla_bipeda_v03",
                AvatarExerciseCommandCatalog.Clip.KNEE_CURL,
                "sin_equipo","controlado",
                AvatarJointTimeline.Command.KNEE_FLEXION);
        register(rows,"paso_lateral_y_retorno_v01",
                AvatarExerciseCommandCatalog.Clip.FRONT_SIDE_STEP,
                "sin_equipo","controlado",
                AvatarJointTimeline.Command.LATERAL_STEP);
        register(rows,"puente_gluteo_v01",
                AvatarExerciseCommandCatalog.Clip.GLUTE_BRIDGE_SHORT,
                "colchoneta","bilateral",
                AvatarJointTimeline.Command.HIP_EXTENSION);
        register(rows,"puente_gluteo_v02",
                AvatarExerciseCommandCatalog.Clip.GLUTE_BRIDGE_BASIC,
                "colchoneta","bilateral",
                AvatarJointTimeline.Command.HIP_EXTENSION);
        register(rows,"extension_rodilla_sin_carga_v01",
                AvatarExerciseCommandCatalog.Clip.SEATED_KNEE_EXTENSION,
                "silla","controlado",
                AvatarJointTimeline.Command.SEATED_KNEE_EXTENSION_SINGLE);
        register(rows,"dorsiflexion_tibial_v01",
                AvatarExerciseCommandCatalog.Clip.SEATED_TOE_RAISE,
                "silla","bilateral",
                AvatarJointTimeline.Command.SEATED_TOE_RAISE);
        register(rows,"alcances_tronco_sentado_v02",
                AvatarExerciseCommandCatalog.Clip.SEATED_FORWARD_REACH,
                "silla","controlado",
                AvatarJointTimeline.Command.SEATED_FORWARD_REACH);
        register(rows,"puente_gluteo_v03",
                AvatarExerciseCommandCatalog.Clip.GLUTE_BRIDGE_PAUSE,
                "colchoneta","isometrico",
                AvatarJointTimeline.Command.GLUTE_BRIDGE_PAUSE);
        register(rows,"extension_rodilla_sin_carga_v02",
                AvatarExerciseCommandCatalog.Clip.SEATED_KNEE_EXTENSION_ALTERNATING,
                "silla","controlado",
                AvatarJointTimeline.Command.SEATED_KNEE_EXTENSION_ALTERNATING);
        register(rows,"extension_rodilla_sin_carga_v03",
                AvatarExerciseCommandCatalog.Clip.SEATED_KNEE_EXTENSION_PAUSE,
                "silla","controlado",
                AvatarJointTimeline.Command.SEATED_KNEE_EXTENSION_PAUSE);
        register(rows,"puente_gluteo_v04",
                AvatarExerciseCommandCatalog.Clip.GLUTE_BRIDGE_BAND,
                "banda+colchoneta","resistencia",
                AvatarJointTimeline.Command.HIP_EXTENSION);
        register(rows,"dorsiflexion_tobillo_v01",
                AvatarExerciseCommandCatalog.Clip.SEATED_TOE_RAISE_SINGLE,
                "silla","movilidad",
                AvatarJointTimeline.Command.SEATED_TOE_RAISE_SINGLE);
        register(rows,"elevacion_gemelos_v01",
                AvatarExerciseCommandCatalog.Clip.SEATED_HEEL_RAISE,
                "silla","bilateral",
                AvatarJointTimeline.Command.SEATED_HEEL_RAISE);
        register(rows,"balanceo_talon_punta_sentado_v01",
                AvatarExerciseCommandCatalog.Clip.SEATED_HEEL_RAISE_GENTLE,
                "silla","controlado",
                AvatarJointTimeline.Command.SEATED_HEEL_RAISE_GENTLE);
        register(rows,"balanceo_talon_punta_sentado_v02",
                AvatarExerciseCommandCatalog.Clip.SEATED_HEEL_TOE_ALTERNATION,
                "silla","controlado",
                AvatarJointTimeline.Command.SEATED_HEEL_TOE_ALTERNATION);
        register(rows,"elevacion_gemelos_v03",
                AvatarExerciseCommandCatalog.Clip.STANDING_HEEL_RAISE,
                "sin_equipo","bilateral",
                AvatarJointTimeline.Command.STANDING_HEEL_RAISE);
        register(rows,"dorsiflexion_tibial_v03",
                AvatarExerciseCommandCatalog.Clip.STANDING_TOE_RAISE,
                "sin_equipo","bilateral",
                AvatarJointTimeline.Command.STANDING_TOE_RAISE);
        REVIEWED=Collections.unmodifiableMap(rows);
    }
    private static void register(Map<String,Signature> target,String id,
                                  AvatarExerciseCommandCatalog.Clip clip,
                                  String equipment,String execution,
                                  AvatarJointTimeline.Command command){
        AvatarMultiViewKinematics.ViewMode camera=
                clip==AvatarExerciseCommandCatalog.Clip.FRONT_SIDE_STEP
                ?AvatarMultiViewKinematics.ViewMode.FRONT_SIDE_STEP:
                clip==AvatarExerciseCommandCatalog.Clip.FRONT_BALANCE
                ?AvatarMultiViewKinematics.ViewMode.FRONT_BALANCE:
                clip==AvatarExerciseCommandCatalog.Clip.FRONT_LATERAL
                ?AvatarMultiViewKinematics.ViewMode.FRONT_LATERAL:
                clip==AvatarExerciseCommandCatalog.Clip.FRONT_NECK_TILT
                ?AvatarMultiViewKinematics.ViewMode.FRONT_NECK_TILT:
                AvatarMultiViewKinematics.ViewMode.SIDE;
        if(target.put(id,new Signature(clip,camera,equipment,execution,command))!=null)
            throw new IllegalStateException("Duplicated illustrated ID "+id);
    }
    static void requireConsistent(AvatarExerciseCommandCatalog.Entry entry){
        Signature record=REVIEWED.get(entry.variantId);
        if(record==null){
            if(entry.canIllustrate())throw new IllegalArgumentException(
                    "Demostración no revisada: "+entry.variantId);
            return;
        }
        if(!entry.canIllustrate()||entry.clip!=record.clip||
                entry.camera!=record.camera||
                entry.command!=record.command||
                !entry.equipment.equals(record.equipment)||
                !entry.execution.equals(record.execution)||
                entry.supervision)
            throw new IllegalArgumentException(
                    "La escena no coincide con postura, material o ejecución de "+
                    entry.variantId);
    }
    static int reviewedCount(){return REVIEWED.size();}
}
