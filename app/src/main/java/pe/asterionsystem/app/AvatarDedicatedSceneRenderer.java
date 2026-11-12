package pe.asterionsystem.app;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Authored orthographic side illustrations for lying and seated exercise.
 * Unlike standing scenes, includes a mat or chair behind the avatar, and
 * deliberately depicts support contacts. Not clinical biomechanics.
 */
public final class AvatarDedicatedSceneRenderer {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();
    private final AvatarArtDetails details=new AvatarArtDetails();
    private void fill(int color){
        paint.setColor(color);paint.setStyle(Paint.Style.FILL);
        paint.setShader(null);
    }
    private void shape(Canvas c,float x1,float y1,float x2,float y2,
                       float radius,int color){
        fill(color);c.drawRoundRect(x1,y1,x2,y2,radius,radius,paint);
    }
    private void line(Canvas c,AvatarKinematics.Point a,AvatarKinematics.Point b,
                      float width,int color){
        fill(color);paint.setStrokeWidth(width);paint.setStrokeCap(Paint.Cap.ROUND);
        c.drawLine(a.x,a.y,b.x,b.y,paint);
    }
    private void label(Canvas c,String text,float x,float y,int color,float size){
        fill(color);paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        paint.setTextSize(size);c.drawText(text,x,y,paint);
    }
    private void limb(Canvas c,AvatarKinematics.Point a,AvatarKinematics.Point b,
                      float width,int color){
        line(c,a,b,width,color);
        fill(color);c.drawCircle(b.x,b.y,width*.48f,paint);
    }
    private void torso(Canvas c,AvatarKinematics.Point hip,
                       AvatarKinematics.Point shoulder,float width,int topColor){
        line(c,hip,shoulder,width,topColor);
        // A rounded fabric transition hides the pelvis/torso pivot.
        fill(topColor);c.drawCircle(shoulder.x,shoulder.y,width*.46f,paint);
    }
    private void chair(Canvas c,boolean dark){
        int wood=dark?0xFF6B7089:0xFF909CAF;
        int seat=dark?0xFF8D94AD:0xFFB2BDCD;
        // Chair is visibly behind the hips and legs, not floating below them.
        shape(c,120,149,135,241,5,wood);
        shape(c,118,226,211,244,7,seat);
        shape(c,122,238,134,406,4,wood);
        shape(c,195,238,207,406,4,wood);
        shape(c,117,235,214,239,2,dark?0xFFBAC8D8:0xFF62718C);
    }
    private void mat(Canvas c,boolean dark){
        shape(c,26,397,365,416,8,dark?0xFF324C68:0xFFBDD0E0);
        shape(c,36,398,355,408,4,dark?0xFF5C7694:0xFFE3EDF6);
        label(c,"COLCHONETA",40,433,dark?0xFFAFC5DD:0xFF587493,10);
    }
    private void drawBody(Canvas c,AvatarPose p,AvatarBodySpec b,
                          AvatarPalette palette,boolean supine,
                          float toeRaiseDegrees){
        // Render far leg then far arm, trunk, near leg and near arm.
        limb(c,p.farHip,p.farKnee,b.thighWidth*.64f,palette.farLegColor);
        limb(c,p.farKnee,p.farAnkle,b.calfWidth*.60f,palette.farLegColor);
        // Rotate the forefoot about the fixed HEEL contact, rather than
        // moving the whole leg or allowing the heel to float.
        c.save();
        c.rotate(-toeRaiseDegrees,p.farAnkle.x-12f,p.farAnkle.y+10f);
        details.drawSneaker(c,p.farAnkle,palette,true);
        c.restore();
        limb(c,p.farShoulder,p.farElbow,b.upperArmWidth*.70f,palette.skinShade);
        limb(c,p.farElbow,p.farWrist,b.forearmWidth*.67f,palette.skinShade);

        torso(c,p.hip,p.shoulder,b.chestHalfWidth*1.3f,palette.top);
        limb(c,p.hip,p.knee,b.thighWidth*.81f,palette.legColor);
        limb(c,p.knee,p.ankle,b.calfWidth*.73f,palette.legColor);
        c.save();
        c.rotate(-toeRaiseDegrees,p.ankle.x-12f,p.ankle.y+10f);
        details.drawSneaker(c,p.ankle,palette,false);
        c.restore();
        fill(palette.shorts);c.drawCircle(p.hip.x,p.hip.y,b.waistHalfWidth*.83f,paint);
        limb(c,p.shoulder,p.elbow,b.upperArmWidth*.82f,palette.skin);
        limb(c,p.elbow,p.wrist,b.forearmWidth*.78f,palette.skin);
        details.drawHand(c,p.wrist,palette.skin);
        line(c,p.shoulder,p.neck,12f,palette.skin);
        // When supine, the head and face rotate upwards towards the ceiling,
        // rather than turning downward as in a prone burpee.
        c.save();
        if(supine)c.rotate(-90f,p.head.x,p.head.y);
        details.drawHead(c,p.head,b,palette);
        c.restore();
        fill(palette.accent);
        c.drawCircle(p.shoulder.x,p.shoulder.y,3f,paint);
        c.drawCircle(p.hip.x,p.hip.y,3f,paint);
    }
    public void draw(Canvas c,AvatarDedicatedScene scene,
                     AvatarBodySpec b,AvatarPalette palette,boolean dark){
        if(c==null||scene==null||b==null||palette==null)
            throw new IllegalArgumentException("Render completo requerido");
        AvatarMotionEngine.assertSkeleton(scene.pose,b,.15f);
        AvatarPoseGuard.requireRig(AvatarSkeletonFrame.fromSide(scene.pose,b));
        shape(c,10,8,390,468,19,dark?0xFF151923:0xFFF8F9FD);
        int grid=dark?0xFF293044:0xFFE8EDF3;
        for(int y=71;y<=397;y+=54){
            fill(grid);paint.setStrokeWidth(1f);
            c.drawLine(30,y,372,y,paint);
        }
        // Fixed virtual environment: stable chair seat/mat throughout motion.
        if(scene.chairSeatContact)chair(c,dark);
        else mat(c,dark);
        if(scene.chairSeatContact)
            shape(c,20,407,376,411,3,dark?0xFF667086:0xFFD5DCE6);
        drawBody(c,scene.pose,b,palette,scene.floorBackContact,
                scene.toeRaiseDegrees);
        String name=scene.kind==AvatarDedicatedScene.Kind.GLUTE_BRIDGE_SHORT
                ?"PERFIL · PUENTE CORTO":
                scene.kind==AvatarDedicatedScene.Kind.GLUTE_BRIDGE_BASIC
                ?"PERFIL · PUENTE DE GLÚTEOS":
                scene.kind==AvatarDedicatedScene.Kind.GLUTE_BRIDGE_PAUSE
                ?"PERFIL · PUENTE CON PAUSA":
                scene.kind==AvatarDedicatedScene.Kind.SEATED_TOE_RAISE
                ?"PERFIL · ELEVACIÓN DE PUNTAS SENTADO":
                scene.kind==AvatarDedicatedScene.Kind.SEATED_FORWARD_REACH
                ?"PERFIL · ALCANCE FRONTAL SENTADO":
                scene.kind==AvatarDedicatedScene.Kind.SEATED_KNEE_EXTENSION_PAUSE
                ?"PERFIL · EXTENSIÓN DE RODILLA CON PAUSA":
                scene.kind==AvatarDedicatedScene.Kind.SEATED_KNEE_EXTENSION_ALTERNATING
                ?"PERFIL · EXTENSIÓN ALTERNADA DE RODILLAS":
                "PERFIL · EXTENSIÓN DE RODILLA SENTADO";
        label(c,name,27,453,dark?0xFFCBD9E7:0xFF4F6B89,11);
    }
}
