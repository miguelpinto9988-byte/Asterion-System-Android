package pe.asterionsystem.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Versioned, explicit exercise-to-animation command contract.
 *
 * Numerical angles are 2D design channels, not human joint measurements.
 * SCHEMATIC permits an illustrative projection. TEXT_ONLY deliberately blocks
 * a misleading animation where the available 2D rig cannot represent it.
 */
public final class AvatarCommandCatalog {
    public enum Command {
        SQUAT, HIP_HINGE, LUNGE, STEP_UP, CALF_RAISE, KNEE_EXTENSION,
        KNEE_FLEXION, HIP_EXTENSION, HIP_ABDUCTION, ARM_RAISE, ARM_PRESS,
        ARM_PULL, ARM_CURL, ELBOW_EXTENSION, SHOULDER_ROTATION,
        ANKLE_MOBILITY, BALANCE, MARCH, TRUNK_FLEXION, TRUNK_EXTENSION,
        SIDE_BEND, NECK_MOBILITY, BREATHING, PLANK_HOLD, STRETCH,
        JUMPING_JACKS, DIAMOND_PUSHUP, BURPEE, CYCLING,
        SPORT_REFERENCE, EQUIPMENT_REFERENCE, WATER_REFERENCE
    }
    public enum VisualStatus { SCHEMATIC, TEXT_ONLY }

    public static final class JointCurve {
        public final int start, active, end;
        private JointCurve(int start,int active,int end) {
            this.start=start; this.active=active; this.end=end;
        }
        public static JointCurve read(String text) {
            String[] p=text.split(">",-1);
            if(p.length!=3)throw new IllegalArgumentException("Curva articular sin 3 fases");
            try {
                int s=Integer.parseInt(p[0]),a=Integer.parseInt(p[1]),
                        e=Integer.parseInt(p[2]);
                if(s!=e || Math.abs(s)>180 || Math.abs(a)>180 ||
                        Math.abs(e)>180)
                    throw new IllegalArgumentException("Curva no cierra o fuera de rango");
                return new JointCurve(s,a,e);
            }catch(NumberFormatException bad) {
                throw new IllegalArgumentException("Ángulo inválido",bad);
            }
        }
        public String describe() {
            return start+"° → "+active+"° → "+end+"°";
        }
    }

    public static final class Entry {
        public final String variantId, name, typeId;
        public final Command command;
        public final AvatarMultiViewKinematics.ViewMode camera;
        public final VisualStatus status;
        public final int tempoMs;
        public final float amplitude;
        public final JointCurve neck,shoulder,elbow,wrist,trunk,hip,knee,ankle;
        public final String startPose,activePose,returnPose,notice;
        private Entry(String[] p) {
            variantId=required(p[0]); name=required(p[1]);typeId=required(p[2]);
            command=Command.valueOf(p[3]);
            camera=AvatarMultiViewKinematics.ViewMode.valueOf(p[4]);
            status=VisualStatus.valueOf(p[5]);
            tempoMs=Integer.parseInt(p[6]);
            amplitude=Float.parseFloat(p[7]);
            if(tempoMs<2000||tempoMs>12000||!Float.isFinite(amplitude)
                    ||amplitude<=0f||amplitude>1f)
                throw new IllegalArgumentException("Tiempo/amplitud inválidos: "+variantId);
            neck=JointCurve.read(p[8]);shoulder=JointCurve.read(p[9]);
            elbow=JointCurve.read(p[10]);wrist=JointCurve.read(p[11]);
            trunk=JointCurve.read(p[12]);hip=JointCurve.read(p[13]);
            knee=JointCurve.read(p[14]);ankle=JointCurve.read(p[15]);
            startPose=required(p[16]);activePose=required(p[17]);
            returnPose=required(p[18]);notice=required(p[19]);
            if(status==VisualStatus.TEXT_ONLY
                    && !command.name().endsWith("_REFERENCE")
                    && command!=Command.HIP_ABDUCTION)
                throw new IllegalArgumentException("Referencia no representable: "+variantId);
            if(command.name().endsWith("_REFERENCE")
                    && status!=VisualStatus.TEXT_ONLY)
                throw new IllegalArgumentException("Referencia sin control visual: "+variantId);
        }
        public boolean canAnimate() {return status==VisualStatus.SCHEMATIC;}
        public String technicalDescription() {
            return "Ejercicio: "+name+"\nMovimiento: "+command.name()
                    +" · Vista: "+camera.title
                    +"\n\nINICIO · "+startPose
                    +"\n\nDURANTE · "+activePose
                    +"\n\nFINAL · "+returnPose
                    +"\n\nCanales angulares de diseño: inicio → momento activo → retorno"
                    +"\nCuello: "+neck.describe()
                    +"\nHombros: "+shoulder.describe()
                    +"\nCodos: "+elbow.describe()
                    +"\nMuñecas: "+wrist.describe()
                    +"\nTronco: "+trunk.describe()
                    +"\nCaderas: "+hip.describe()
                    +"\nRodillas: "+knee.describe()
                    +"\nTobillos: "+ankle.describe()
                    +"\n\nCiclo: "+tempoMs+" ms · Escala de la ilustración: "
                    +amplitude+"\n\n"+notice;
        }
    }

    private final Map<String,Entry> entries;
    private AvatarCommandCatalog(Map<String,Entry> entries) {
        this.entries=Collections.unmodifiableMap(entries);
    }
    public int size() {return entries.size();}
    public Entry find(String id) {return entries.get(id);}
    public Entry require(String id) {
        Entry e=entries.get(Objects.requireNonNull(id,"variantId"));
        if(e==null)throw new IllegalArgumentException("Sin comando visual: "+id);
        return e;
    }
    public static AvatarCommandCatalog load(Reader input) throws IOException {
        Objects.requireNonNull(input,"input");
        Map<String,Entry> entries=new LinkedHashMap<>();
        BufferedReader br=new BufferedReader(input);
        String header=br.readLine();
        if(!("variant_id\tname\ttype_id\tcommand\tcamera\tvisual_status"
                +"\ttempo_ms\tamplitude\tneck_deg\tshoulder_deg\telbow_deg"
                +"\twrist_deg\ttrunk_deg\thip_deg\tknee_deg\tankle_deg"
                +"\tstart_pose\tactive_pose\treturn_pose\tnotice").equals(header))
            throw new IllegalArgumentException("Versión incompatible de comandos");
        String line;
        int number=1;
        while((line=br.readLine())!=null) {
            number++;
            if(line.isBlank())continue;
            String[] columns=line.split("\t",-1);
            if(columns.length!=20)
                throw new IllegalArgumentException("20 campos requeridos en fila "+number);
            final Entry entry;
            try {entry=new Entry(columns);}
            catch(IllegalArgumentException error) {
                throw new IllegalArgumentException("Comando incorrecto en fila "
                        +number+": "+error.getMessage(),error);
            }
            if(entries.putIfAbsent(entry.variantId,entry)!=null)
                throw new IllegalArgumentException("Variante duplicada: "+entry.variantId);
        }
        if(entries.size()!=1257)
            throw new IllegalArgumentException("Comandos incompletos: "+entries.size()+"/1257");
        return new AvatarCommandCatalog(entries);
    }
    private static String required(String value) {
        if(value==null||value.isBlank())throw new IllegalArgumentException("Campo vacío");
        return value;
    }
}
