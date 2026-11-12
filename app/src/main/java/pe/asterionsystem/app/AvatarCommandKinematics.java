package pe.asterionsystem.app;

/**
 * Additional schematic motion commands for the existing native 2D body rig.
 *
 * The original six AvatarMotionEngine presets are intentionally unchanged.
 * New commands use the same fixed-length, two-bone inverse kinematics.
 * The render projection is educational only, not a biomechanical assessment.
 */
public final class AvatarCommandKinematics {
    private AvatarCommandKinematics() {}

    public static AvatarKinematics.Pose pose(AvatarCommandCatalog.Entry entry,
                                             float phase,AvatarBodySpec body) {
        if(entry==null||body==null)
            throw new IllegalArgumentException("Comando y cuerpo obligatorios");
        if(!entry.canAnimate())
            throw new IllegalArgumentException("No representar un comando de solo texto");
        return pose(entry.command,phase,body,entry.amplitude);
    }

    public static AvatarKinematics.Pose pose(AvatarCommandCatalog.Command command,
                                             float phase,AvatarBodySpec body,float amplitude) {
        if(command==null||body==null||!Float.isFinite(phase)
                ||!Float.isFinite(amplitude)||amplitude<=0f||amplitude>1f)
            throw new IllegalArgumentException("Parámetros geométricos inválidos");

        // Backward compatibility: keep the six authored motions intact.
        AvatarMotionEngine.Motion old=legacy(command);
        if(old!=null)return AvatarMotionEngine.pose(old,phase,body);
        if(command.name().endsWith("_REFERENCE"))
            throw new IllegalArgumentException("Referencia sin animación aprobada");

        float s=AvatarKinematics.depthAtPhase(phase)*amplitude;
        float hx=184f,hy=174.4f,tilt=.10f;
        float ax=182f,ay=364f,fx=170f,fy=363f;
        float nearAngle=.055f,farAngle=.015f;
        float nearReach=body.upperArm+body.forearm-.025f;
        float farReach=nearReach;
        float neckOffset=0f,headOffset=0f;

        switch(command) {
            case STEP_UP:
                ax+=35f*s;ay-=54f*s;hx+=4f*s;hy-=2f*s;
                nearAngle+=.35f*s;farAngle+=.16f*s;break;
            case CALF_RAISE:
                hy-=8f*s;ay-=8f*s;fy-=8f*s;
                nearAngle+=.05f*s;farAngle+=.05f*s;break;
            case KNEE_EXTENSION:
                ax+=35f*s;ay-=42f*s;break;
            case KNEE_FLEXION:
                ax-=21f*s;ay-=53f*s;break;
            case HIP_EXTENSION:
                ax-=34f*s;ay-=21f*s;hx-=3f*s;break;
            case HIP_ABDUCTION:
                // Lateral movement cannot be verified in this projection.
                throw new IllegalArgumentException("Abducción requiere otra cámara");
            case ARM_PRESS:
                nearAngle+=1.75f*s;farAngle+=1.58f*s;
                nearReach-=15f*s;farReach-=15f*s;break;
            case ARM_PULL:
                nearAngle+=.58f*s;farAngle+=.51f*s;
                nearReach-=43f*s;farReach-=43f*s;break;
            case ARM_CURL:
                nearAngle+=.24f*s;farAngle+=.24f*s;
                nearReach-=62f*s;farReach-=62f*s;break;
            case ELBOW_EXTENSION:
                nearAngle+=.42f*s;farAngle+=.37f*s;
                nearReach-=58f*(1f-s);farReach-=58f*(1f-s);break;
            case SHOULDER_ROTATION:
                nearAngle+=1.12f*s;farAngle+=1.03f*s;break;
            case ANKLE_MOBILITY:
                ax+=11f*s;ay-=9f*s;break;
            case BALANCE:
                ax+=18f*s;ay-=37f*s;nearAngle+=.21f*s;break;
            case TRUNK_FLEXION:
                tilt+=.46f*s;hx-=8f*s;hy+=6f*s;
                nearAngle+=.16f*s;farAngle+=.15f*s;break;
            case TRUNK_EXTENSION:
                tilt-=.24f*s;hx+=5f*s;hy+=4f*s;break;
            case SIDE_BEND:
                // Side bending requires a verified frontal projection.
                throw new IllegalArgumentException("Inclinación lateral no resuelta");
            case NECK_MOBILITY:
                neckOffset=2f*s;headOffset=10f*s;break;
            case BREATHING:
                nearAngle+=.035f*s;farAngle+=.035f*s;break;
            case PLANK_HOLD:
                throw new IllegalArgumentException("Plancha necesita rig en el suelo");
            case STRETCH:
                tilt+=.12f*s;nearAngle+=.81f*s;farAngle+=.78f*s;break;
            case CYCLING:
                ax+=20f*s;ay-=33f*s;fx-=13f*s;fy-=24f*s;
                tilt+=.10f*s;break;
            case JUMPING_JACKS:
            case DIAMOND_PUSHUP:
                throw new IllegalArgumentException("Comando exclusivo de otra cámara");
            default:
                throw new IllegalArgumentException("Movimiento no implementado: "+command);
        }

        AvatarKinematics.Point hip=new AvatarKinematics.Point(hx,hy);
        AvatarKinematics.Point farHip=new AvatarKinematics.Point(hx-10f,hy-1f);
        AvatarKinematics.Point ankle=new AvatarKinematics.Point(ax,ay);
        AvatarKinematics.Point farAnkle=new AvatarKinematics.Point(fx,fy);
        AvatarRigConstraints.TwoBone nearLeg=AvatarRigConstraints.twoBone(
                hip,ankle,body.upperLeg,body.lowerLeg,-1,0f,170f);
        AvatarRigConstraints.TwoBone farLeg=AvatarRigConstraints.twoBone(
                farHip,farAnkle,body.upperLeg,body.lowerLeg,-1,0f,170f);
        if(nearLeg.targetLimited||farLeg.targetLimited)
            throw new IllegalArgumentException("Comando excede las longitudes de pierna");

        AvatarKinematics.Point shoulder=new AvatarKinematics.Point(
                hx+(float)Math.sin(tilt)*body.torso,
                hy-(float)Math.cos(tilt)*body.torso);
        AvatarKinematics.Point farShoulder=new AvatarKinematics.Point(
                shoulder.x-6f,shoulder.y+10f);
        AvatarKinematics.Point neck=new AvatarKinematics.Point(
                shoulder.x+4f+neckOffset,shoulder.y-6f);
        AvatarKinematics.Point head=new AvatarKinematics.Point(
                neck.x+4f+headOffset,neck.y-27f);

        AvatarKinematics.Point nearTarget=AvatarKinematics.wristOnArc(
                shoulder,nearReach,nearAngle);
        AvatarKinematics.Point farTarget=AvatarKinematics.wristOnArc(
                farShoulder,farReach,farAngle);
        AvatarRigConstraints.TwoBone nearArm=AvatarRigConstraints.twoBone(
                shoulder,nearTarget,body.upperArm,body.forearm,1,0f,170f);
        AvatarRigConstraints.TwoBone farArm=AvatarRigConstraints.twoBone(
                farShoulder,farTarget,body.upperArm,body.forearm,1,0f,170f);
        if(nearArm.targetLimited||farArm.targetLimited)
            throw new IllegalArgumentException("Comando excede las longitudes de brazo");
        return new AvatarKinematics.Pose(
                ankle,farAnkle,nearLeg.joint,farLeg.joint,hip,farHip,
                shoulder,farShoulder,neck,head,
                nearArm.joint,nearArm.end,farArm.joint,farArm.end);
    }

    /**
     * Projected coordinates are computed from the SAME rig drawn by Canvas.
     * They are expressed in virtual 400x480 units, not anatomical degrees,
     * user measurements or a claim that an exercise is technically correct.
     */
    public static String projectedJointTrace(AvatarCommandCatalog.Entry entry,
                                              AvatarBodySpec body) {
        if(entry==null||body==null||!entry.canAnimate()
                ||entry.camera!=AvatarMultiViewKinematics.ViewMode.SIDE)
            return "\nSin coordenadas 2D verificadas para esta proyección.";
        AvatarKinematics.Pose start=pose(entry,0f,body);
        AvatarKinematics.Pose active=pose(entry,.49f,body);
        AvatarKinematics.Pose end=pose(entry,1f,body);
        StringBuilder out=new StringBuilder(
                "\n\nCoordenadas exactas del dibujo, inicio → acción → final "
                +"(unidades virtuales de Canvas):");
        trace(out,"Cabeza",start.head,active.head,end.head);
        trace(out,"Cuello",start.neck,active.neck,end.neck);
        trace(out,"Hombro",start.shoulder,active.shoulder,end.shoulder);
        trace(out,"Codo",start.elbow,active.elbow,end.elbow);
        trace(out,"Muñeca",start.wrist,active.wrist,end.wrist);
        trace(out,"Cadera",start.hip,active.hip,end.hip);
        trace(out,"Rodilla",start.knee,active.knee,end.knee);
        trace(out,"Tobillo",start.ankle,active.ankle,end.ankle);
        trace(out,"Hombro opuesto",start.farShoulder,
                active.farShoulder,end.farShoulder);
        trace(out,"Codo opuesto",start.farElbow,active.farElbow,end.farElbow);
        trace(out,"Muñeca opuesta",start.farWrist,active.farWrist,end.farWrist);
        trace(out,"Cadera opuesta",start.farHip,active.farHip,end.farHip);
        trace(out,"Rodilla opuesta",start.farKnee,active.farKnee,end.farKnee);
        trace(out,"Tobillo opuesto",start.farAnkle,active.farAnkle,end.farAnkle);
        return out.toString();
    }

    private static void trace(StringBuilder out,String name,
                              AvatarKinematics.Point a,AvatarKinematics.Point b,
                              AvatarKinematics.Point c) {
        out.append("\n").append(name).append(": ");
        coord(out,a);out.append(" → ");coord(out,b);
        out.append(" → ");coord(out,c);
    }
    private static void coord(StringBuilder out,AvatarKinematics.Point point) {
        out.append("(").append(Math.round(point.x)).append(",")
                .append(Math.round(point.y)).append(")");
    }

    public static AvatarMotionEngine.Motion legacy(AvatarCommandCatalog.Command command) {
        switch(command) {
            case SQUAT:return AvatarMotionEngine.Motion.SQUAT;
            case HIP_HINGE:return AvatarMotionEngine.Motion.HIP_HINGE;
            case ARM_RAISE:return AvatarMotionEngine.Motion.ARM_RAISE;
            case MARCH:return AvatarMotionEngine.Motion.MARCH;
            case LUNGE:return AvatarMotionEngine.Motion.LUNGE;
            case BURPEE:return AvatarMotionEngine.Motion.BURPEE;
            default:return null;
        }
    }
}
