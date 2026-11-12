package pe.asterionsystem.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseFamily;
import pe.asterionsystem.core.engine.exercises.ExerciseType;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;

/**
 * Strict, read-only command coverage for every real exercise variant.
 * SPEC_ONLY means its anatomical command is classified but a faithful scene
 * has not been drawn or reviewed; such variants MUST NOT show a generic squat.
 * No progression or training prescription is changed by this catalog.
 */
public final class AvatarExerciseCommandCatalog {
    public enum Status { ILLUSTRATIVE,SPEC_ONLY }
    public enum Clip {
        NONE, SQUAT,HIP_HINGE,LUNGE,ARM_RAISE,ARM_CURL,KNEE_CURL,MARCH,
        FRONT,FRONT_SQUAT,FRONT_JACKS,FRONT_ELBOW,TOP,
        FRONT_LATERAL,FRONT_BALANCE,FRONT_SIDE_STEP,FRONT_NECK_TILT,
        HIP_EXTENSION_STAND;
        public AvatarMotionEngine.Motion sideMotion() {
            switch(this) {
                case SQUAT:return AvatarMotionEngine.Motion.SQUAT;
                case HIP_HINGE:return AvatarMotionEngine.Motion.HIP_HINGE;
                case LUNGE:return AvatarMotionEngine.Motion.LUNGE;
                case ARM_RAISE:return AvatarMotionEngine.Motion.ARM_RAISE;
                case ARM_CURL:return AvatarMotionEngine.Motion.ARM_CURL;
                case KNEE_CURL:return AvatarMotionEngine.Motion.KNEE_CURL;
                case HIP_EXTENSION_STAND:return AvatarMotionEngine.Motion.HIP_EXTENSION_STAND;
                case MARCH:return AvatarMotionEngine.Motion.MARCH;
                default:throw new IllegalStateException("No hay escena lateral: "+this);
            }
        }
    }

    public static final class Entry {
        public final String variantId;
        public final AvatarJointTimeline.Command command;
        public final AvatarMultiViewKinematics.ViewMode camera;
        public final Clip clip;
        public final Status status;
        public final String category,execution,equipment;
        public final int level;
        public final boolean supervision;
        private final AvatarJointTimeline timeline;

        private Entry(String id,AvatarJointTimeline.Command command,
                      AvatarMultiViewKinematics.ViewMode camera,Clip clip,
                      Status status,String category,int level,
                      String execution,String equipment,boolean supervision,
                      AvatarJointTimeline timeline) {
            this.variantId=id;this.command=command;this.camera=camera;
            this.clip=clip;this.status=status;this.category=category;
            this.level=level;this.execution=execution;this.equipment=equipment;
            this.supervision=supervision;this.timeline=timeline;
        }
        public boolean canIllustrate(){return status==Status.ILLUSTRATIVE;}
        public AvatarJointTimeline timeline(){return timeline;}
        public String phaseLabel(float progress){
            if(!Float.isFinite(progress)||progress<0||progress>1)
                throw new IllegalArgumentException("Fase inválida");
            if(progress<.08f)return "Preparación y apoyo";
            if(progress<.42f)return "Movimiento controlado";
            if(progress<.58f)return "Posición ilustrativa central";
            if(progress<.94f)return "Retorno controlado";
            return "Fin en posición neutral";
        }
        public String safeDescription(){
            return "Ejercicio "+variantId+" · comando "+command+
                " · cámara preferente "+camera+
                " · nivel técnico "+level+
                " · ejecución "+execution+
                " · material "+equipment+
                ". Articulaciones: cuello, hombros, codos, muñecas, escápulas,"+
                " columna, pelvis, caderas, rodillas y tobillos."+
                " Fases: preparación, movimiento, control, retorno y posición final."+
                (supervision?" Orientación supervisada recomendada.":"")+
                (canIllustrate()?" Animación esquemática disponible.":
                " Animación específica pendiente de revisión; no mostrar otra ejecución.");
        }
    }

    private final Map<String,Entry> entries;
    private AvatarExerciseCommandCatalog(Map<String,Entry> entries){
        this.entries=Collections.unmodifiableMap(new LinkedHashMap<>(entries));
    }
    public int size(){return entries.size();}
    public Entry require(String id){
        Entry result=entries.get(id);
        if(result==null)throw new IllegalArgumentException("Ejercicio sin comandos: "+id);
        return result;
    }
    public Map<String,Entry> all(){return entries;}

    public static AvatarExerciseCommandCatalog load(Reader reader,ExerciseCatalog catalog)
            throws IOException {
        if(reader==null||catalog==null)throw new IllegalArgumentException("Catálogo requerido");
        final String header="variant_id\tcommand\tcamera\tclip\tstatus\tcategory\t"+
                "level\texecution\tequipment\tsupervision";
        Map<String,ExerciseType> types=new LinkedHashMap<>();
        Map<String,ExerciseFamily> families=new LinkedHashMap<>();
        for(ExerciseType t:catalog.types())types.put(t.id(),t);
        for(ExerciseFamily f:catalog.families())families.put(f.id(),f);
        EnumMap<AvatarJointTimeline.Command,AvatarJointTimeline> timelines=
                new EnumMap<>(AvatarJointTimeline.Command.class);
        Map<String,Entry> result=new LinkedHashMap<>();
        BufferedReader input=new BufferedReader(reader);
        if(!header.equals(input.readLine()))throw new IllegalArgumentException(
                "Encabezado de comandos de avatar inválido");
        String line;
        int lineNumber=1;
        while((line=input.readLine())!=null){
            lineNumber++;
            if(line.isEmpty())throw new IllegalArgumentException(
                    "Registro vacío en línea "+lineNumber);
            String[] p=line.split("\\t",-1);
            if(p.length!=10)throw new IllegalArgumentException(
                    "Columnas inválidas en línea "+lineNumber);
            ExerciseVariant v=catalog.variant(p[0]).orElseThrow(
                    ()->new IllegalArgumentException("Variante inexistente: "+p[0]));
            ExerciseType t=types.get(v.typeId());
            ExerciseFamily f=families.get(t.familyId());
            AvatarJointTimeline.Command command;
            AvatarMultiViewKinematics.ViewMode camera;
            Clip clip;
            Status status;
            try{
                command=AvatarJointTimeline.Command.valueOf(p[1]);
                camera=AvatarMultiViewKinematics.ViewMode.valueOf(p[2]);
                clip=Clip.valueOf(p[3]);
                status=Status.valueOf(p[4]);
            }catch(IllegalArgumentException ex){
                throw new IllegalArgumentException("Comando/cámara no definido: "+p[0],ex);
            }
            int level;
            try{level=Integer.parseInt(p[6]);}
            catch(NumberFormatException ex){throw new IllegalArgumentException("Nivel inválido",ex);}
            boolean supervision="1".equals(p[9]);
            if(!("0".equals(p[9])||"1".equals(p[9])) ||
                    !f.category().equals(p[5]) ||
                    v.difficulty()!=level || !v.execution().equals(p[7]) ||
                    !String.join("+",v.equipment()).equals(p[8]) ||
                    v.supervisionRecommended()!=supervision) {
                throw new IllegalArgumentException("Comando discordante con catálogo: "+p[0]);
            }
            if((status==Status.SPEC_ONLY)!=(clip==Clip.NONE))
                throw new IllegalArgumentException("Estado/escena inconsistente: "+p[0]);
            if(status==Status.ILLUSTRATIVE){
                if(level>=4||supervision)throw new IllegalArgumentException(
                        "Prohibida demostración automática técnica avanzada: "+p[0]);
                if((camera==AvatarMultiViewKinematics.ViewMode.SIDE) !=
                        (clip!=Clip.FRONT&&clip!=Clip.FRONT_SQUAT&&
                         clip!=Clip.FRONT_JACKS&&clip!=Clip.FRONT_ELBOW&&clip!=Clip.TOP&&
                         clip!=Clip.FRONT_LATERAL&&clip!=Clip.FRONT_BALANCE&&
                         clip!=Clip.FRONT_SIDE_STEP&&clip!=Clip.FRONT_NECK_TILT))
                    throw new IllegalArgumentException("Proyección equivocada: "+p[0]);
            }
            AvatarJointTimeline timeline=timelines.get(command);
            if(timeline==null){
                timeline=new AvatarJointTimeline(command);timelines.put(command,timeline);
            }
            Entry e=new Entry(p[0],command,camera,clip,status,p[5],level,p[7],
                    p[8],supervision,timeline);
            if(result.putIfAbsent(p[0],e)!=null)
                throw new IllegalArgumentException("Comando duplicado: "+p[0]);
        }
        if(result.size()!=catalog.variants().size())
            throw new IllegalArgumentException("Faltan comandos de avatar: "+
                    result.size()+"/"+catalog.variants().size());
        for(ExerciseVariant variant:catalog.variants())
            if(!result.containsKey(variant.id()))
                throw new IllegalArgumentException("Sin comando: "+variant.id());
        return new AvatarExerciseCommandCatalog(result);
    }
}
