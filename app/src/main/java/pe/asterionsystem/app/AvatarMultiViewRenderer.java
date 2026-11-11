package pe.asterionsystem.app;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;

/**
 * Exercise-specific camera drawings. Not a rotated side sprite or a 3D avatar.
 * Asterion V6 reuses the existing colors, body specification and 2-bone IK.
 * The overhead camera deliberately shows the back of a floor-facing avatar.
 */
public final class AvatarMultiViewRenderer {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();
    private final LinearGradient blue=new LinearGradient(
            140f,70f,255f,300f,0xFF4289C7,0xFF163A71,Shader.TileMode.CLAMP);
    private final LinearGradient purple=new LinearGradient(
            140f,70f,255f,300f,0xFFB394E0,0xFF493170,Shader.TileMode.CLAMP);
    private AvatarPalette palette=AvatarPalette.BLUE;
    private boolean dark;

    public void draw(Canvas c,AvatarMultiViewKinematics.Pose p,
                     AvatarMultiViewKinematics.ViewMode camera,
                     AvatarPalette palette,boolean dark) {
        if(c==null||p==null||camera==null||palette==null)
            throw new IllegalArgumentException("Vista y pose necesarias");
        this.palette=palette;this.dark=dark;
        int bg=dark?0xFF131722:0xFFF8FAFE;
        int grid=dark?0xFF252D40:0xFFE8EDF5;
        rr(c,10,8,390,468,18,bg);
        color(grid);
        paint.setStrokeWidth(1f);
        for(int y=65;y<420;y+=55)c.drawLine(35,y,365,y,paint);
        for(int x=55;x<360;x+=55)c.drawLine(x,45,x,413,paint);
        if(camera==AvatarMultiViewKinematics.ViewMode.FRONT) {
            drawFront(c,p);
            text(c,"FRONTAL · ELEVACIÓN DE BRAZOS",34,447,12,
                    dark?0xFFC0CCE0:0xFF536987);
        } else if(camera==AvatarMultiViewKinematics.ViewMode.TOP) {
            drawDiamondTop(c,p);
            text(c,"SUPERIOR · FLEXIÓN EN DIAMANTE",28,447,11,
                    dark?0xFFC0CCE0:0xFF536987);
        }else throw new IllegalArgumentException("El lateral usa AvatarRenderer");
    }

    private void color(int value) {
        paint.setShader(null);
        paint.setColor(value);
        paint.setStyle(Paint.Style.FILL);
    }
    private void rr(Canvas c,float l,float t,float r,float b,float radius,int fill){
        color(fill);c.drawRoundRect(l,t,r,b,radius,radius,paint);
    }
    private void text(Canvas c,String value,float x,float y,float size,int fill){
        color(fill);paint.setTextSize(size);paint.setFakeBoldText(true);
        c.drawText(value,x,y,paint);paint.setFakeBoldText(false);
    }
    private void line(Canvas c,float x,float y,float ex,float ey,int fill,float width){
        color(fill);paint.setStrokeWidth(width);paint.setStrokeCap(Paint.Cap.ROUND);
        c.drawLine(x,y,ex,ey,paint);
    }
    private void limb(Canvas c,AvatarKinematics.Point a,
                      AvatarKinematics.Point b,float radius,int fill){
        color(fill);
        paint.setStrokeWidth(radius*2);
        paint.setStrokeCap(Paint.Cap.ROUND);
        c.drawLine(a.x,a.y,b.x,b.y,paint);
    }
    private void circle(Canvas c,float x,float y,float r,int fill){
        color(fill);c.drawCircle(x,y,r,paint);
    }

    /** Frontal view of the same adult avatar, with independent left/right arms. */
    private void drawFront(Canvas c,AvatarMultiViewKinematics.Pose p){
        c.save();
        c.scale(.89f,.89f,200f,210f);
        rr(c,95,426,305,430,2,dark?0xFF56627A:0xFFD2DBE8);
        color(dark?0xFF283249:0xFFDFE5EF);
        c.drawOval(110,425,290,438,paint);
        // Back arms, then torso, with sleeve junctions covering their roots.
        limb(c,p.leftShoulder,p.leftElbow,13,palette.top);
        limb(c,p.leftElbow,p.leftWrist,11,palette.skin);
        limb(c,p.rightShoulder,p.rightElbow,13,palette.top);
        limb(c,p.rightElbow,p.rightWrist,11,palette.skin);
        circle(c,p.leftElbow.x,p.leftElbow.y,10,palette.skin);
        circle(c,p.rightElbow.x,p.rightElbow.y,10,palette.skin);
        handsSimple(c,p.leftWrist,p.rightWrist);
        // Fixed legs: hip->knee->ankle, colored as denim exercise trousers.
        limb(c,p.leftHip,p.leftKnee,17,palette.legColor);
        limb(c,p.leftKnee,p.leftAnkle,13,palette.legColor);
        limb(c,p.rightHip,p.rightKnee,17,palette.legColor);
        limb(c,p.rightKnee,p.rightAnkle,13,palette.legColor);
        circle(c,p.leftKnee.x,p.leftKnee.y,12,palette.legColor);
        circle(c,p.rightKnee.x,p.rightKnee.y,12,palette.legColor);
        line(c,178,290,174,405,0xFF879CBB,1.25f);
        line(c,222,290,226,405,0xFF879CBB,1.25f);
        drawShoeFront(c,p.leftAnkle.x,p.leftAnkle.y);
        drawShoeFront(c,p.rightAnkle.x,p.rightAnkle.y);
        // Waist and tailored torso, avoiding a pill-shaped silhouette.
        path.rewind();
        path.moveTo(174,235);path.cubicTo(180,249,219,249,226,235);
        path.lineTo(226,269);path.quadTo(200,279,174,269);path.close();
        color(palette.shorts);c.drawPath(path,paint);
        path.rewind();path.moveTo(166,148);
        path.cubicTo(158,161,166,194,170,208);
        path.quadTo(178,233,174,240);
        path.quadTo(201,250,226,240);
        path.quadTo(224,224,231,206);
        path.cubicTo(236,180,240,165,234,148);
        path.quadTo(201,138,166,148);path.close();
        color(palette.top);
        paint.setShader(palette.female?purple:blue);c.drawPath(path,paint);
        paint.setShader(null);
        line(c,172,164,179,229,palette.accent,2.3f);
        line(c,229,164,222,229,palette.accent,2.3f);
        line(c,180,143,168,159,0xFF8BC8F4,2f);
        line(c,220,143,232,159,0xFF8BC8F4,2f);
        // Two sleeves share the shoulder height; no protruding shoulder.
        circle(c,159,153,12,palette.top);
        circle(c,241,153,12,palette.top);
        // Small neckline is visibly separate from head.
        rr(c,192,118,208,143,6,palette.skin);
        circle(c,200,145,12,palette.top);
        drawFaceFront(c,200,97);
        // Small geometric clothing mark, not official brand asset.
        text(c,"A",212,188,11,0xFFCAE8F9);
        c.restore();
    }

    private void drawFaceFront(Canvas c,float x,float y){
        c.save();c.translate(x,y);
        color(palette.skin);c.drawOval(-23,-28,23,26,paint);
        color(palette.skinShade);c.drawOval(-26,-7,-20,8,paint);
        c.drawOval(20,-7,26,8,paint);
        path.rewind();path.moveTo(-23,-9);
        path.cubicTo(-29,-37,-10,-40,0,-37);
        path.cubicTo(16,-42,29,-30,23,-9);
        path.lineTo(17,-17);
        path.quadTo(10,-20,3,-19);
        path.quadTo(-9,-15,-18,-18);path.close();
        color(palette.hair);c.drawPath(path,paint);
        line(c,-14,-6,-5,-8,0xFF624A3E,1.8f);
        line(c,5,-8,14,-6,0xFF624A3E,1.8f);
        color(0xFF293642);c.drawOval(-11,-2,-6,3,paint);
        c.drawOval(6,-2,11,3,paint);
        circle(c,-8,-1,0.9f,0xFFF6FFFF);circle(c,8,-1,.9f,0xFFF6FFFF);
        line(c,0,3,-1,10,0xFFB87861,1f);
        line(c,-6,16,0,18,0xFF995E60,1.4f);
        line(c,0,18,6,16,0xFF995E60,1.4f);
        c.restore();
    }
    private void drawShoeFront(Canvas c,float x,float y){
        rr(c,x-13,y-8,x+13,y+8,5,palette.shoe);
        rr(c,x-15,y+5,x+15,y+11,4,palette.shoeDark);
        line(c,x-7,y-4,x+6,y-4,0xFF3D74AA,2f);
        line(c,x-6,y,x+7,y,0xFF7FAFDA,1.3f);
    }
    private void handsSimple(Canvas c,AvatarKinematics.Point l,
                             AvatarKinematics.Point r){
        circle(c,l.x,l.y,8,palette.skin);
        circle(c,r.x,r.y,8,palette.skin);
    }

    /**
     * Not a standing figure turned sideways: feet are at the far end, crown
     * of head and upper back face the overhead camera, hands touch the ground.
     */
    private void drawDiamondTop(Canvas c,AvatarMultiViewKinematics.Pose p) {
        rr(c,42,58,244,419,17,dark?0xFF192436:0xFFF1F5FA);
        // The ground-contact shadow changes slightly as the torso lowers.
        int shadow=dark?0x442D4A71:0x2D6A849E;
        color(shadow);c.drawOval(101,132,216,310,paint);
        // Shoes and trousers point away from the head in top-down space.
        for(int dir:new int[]{-1,1}) {
            AvatarKinematics.Point hip=dir<0?p.leftHip:p.rightHip;
            AvatarKinematics.Point knee=dir<0?p.leftKnee:p.rightKnee;
            AvatarKinematics.Point ankle=dir<0?p.leftAnkle:p.rightAnkle;
            limb(c,hip,knee,14,palette.legColor);
            limb(c,knee,ankle,12,palette.legColor);
            line(c,hip.x+dir*6,hip.y+16,ankle.x+dir*3,ankle.y-13,
                    0xFF879AB7,1f);
            rr(c,ankle.x-11,ankle.y-11,ankle.x+11,ankle.y+9,6,palette.shoe);
            line(c,ankle.x-8,ankle.y-2,ankle.x+8,ankle.y-2,
                    0xFF386CA0,2.5f);
        }
        // Body seen from above: back of the blue shirt and seam detailing.
        path.rewind();path.moveTo(p.leftShoulder.x,p.leftShoulder.y-10);
        path.quadTo(158,p.head.y+56,p.rightShoulder.x,p.rightShoulder.y-10);
        path.lineTo(p.rightHip.x+6,p.rightHip.y+2);
        path.quadTo(158,280,p.leftHip.x-6,p.leftHip.y+2);
        path.close();color(palette.top);
        paint.setShader(palette.female?purple:blue);
        c.drawPath(path,paint);paint.setShader(null);
        line(c,p.leftShoulder.x+9,p.leftShoulder.y+8,139,248,palette.accent,2f);
        line(c,p.rightShoulder.x-9,p.rightShoulder.y+8,178,248,palette.accent,2f);
        line(c,144,255,170,255,0xFF9DC9E9,1.1f);
        // Two arm chains swing outside the torso, converging at paired hands.
        drawTopArm(c,p.leftShoulder,p.leftElbow,p.leftWrist);
        drawTopArm(c,p.rightShoulder,p.rightElbow,p.rightWrist);
        // Back of head: dark hair crown; face correctly invisible from above.
        rr(c,143,p.neck.y-10,173,p.neck.y+8,9,palette.skin);
        color(palette.hair);c.drawOval(p.head.x-26,p.head.y-26,
                p.head.x+26,p.head.y+24,paint);
        color(palette.hairHighlight);
        path.rewind();
        path.moveTo(p.head.x-13,p.head.y-8);
        path.quadTo(p.head.x+1,p.head.y-25,p.head.x+15,p.head.y-7);
        path.quadTo(p.head.x+3,p.head.y-10,p.head.x-13,p.head.y-8);
        path.close();c.drawPath(path,paint);
        // Enlarged hand view is always separate because fingers cannot be
        // read accurately on a full-body mobile-size sprite.
        drawHandsInset(c);
        drawPushupSideInset(c,p.depth);
    }

    private void drawTopArm(Canvas c,AvatarKinematics.Point shoulder,
                            AvatarKinematics.Point elbow,
                            AvatarKinematics.Point wrist){
        limb(c,shoulder,elbow,12,palette.top);
        limb(c,elbow,wrist,9,palette.skin);
        circle(c,elbow.x,elbow.y,9,palette.skin);
        circle(c,wrist.x,wrist.y,9,palette.skin);
        line(c,shoulder.x,shoulder.y,elbow.x,elbow.y,
                palette.accent,1.4f);
    }

    /** High contrast, symbolic finger detail with visible diamond negative space. */
    private void drawHandsInset(Canvas c){
        int panel=dark?0xFF222F45:0xFFFFFFFF;
        rr(c,248,171,382,335,13,panel);
        int ink=dark?0xFFC4D7F2:0xFF4D637F;
        text(c,"MANOS · DETALLE",259,191,10,ink);
        c.save();c.translate(258,199);
        // Hand shapes mirror each other, thumbs point inward below
        // index fingers. The central quadrilateral remains open.
        path.rewind();path.moveTo(7,51);path.lineTo(28,21);
        path.quadTo(37,10,48,12);
        path.lineTo(54,19);path.lineTo(39,29);
        path.lineTo(56,43);path.lineTo(50,49);
        path.lineTo(31,38);path.lineTo(27,69);
        path.lineTo(8,78);path.close();
        color(palette.skin);c.drawPath(path,paint);
        path.rewind();path.moveTo(117,51);path.lineTo(96,21);
        path.quadTo(87,10,76,12);
        path.lineTo(70,19);path.lineTo(85,29);
        path.lineTo(68,43);path.lineTo(74,49);
        path.lineTo(93,38);path.lineTo(97,69);
        path.lineTo(116,78);path.close();
        color(palette.skin);c.drawPath(path,paint);
        // Finger creases and nail hints, not a prescriptive grip.
        line(c,25,56,38,58,0xFFB47B62,1f);
        line(c,99,56,86,58,0xFFB47B62,1f);
        line(c,36,23,46,20,0xFFEED5C0,1.5f);
        line(c,88,23,78,20,0xFFEED5C0,1.5f);
        color(0xFF52A8D1);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.6f);
        path.rewind();path.moveTo(62,22);path.lineTo(80,42);
        path.lineTo(62,65);path.lineTo(44,42);path.close();
        c.drawPath(path,paint);paint.setStyle(Paint.Style.FILL);
        c.restore();
        text(c,"ESQUEMA ILUSTRATIVO",258,314,8,ink);
        text(c,"Sin medición de postura",258,325,7.5f,ink);
    }

    /** Second camera solves the occlusion problem of an overhead push-up. */
    private void drawPushupSideInset(Canvas c,float depth) {
        int panel=dark?0xFF222F45:0xFFFFFFFF;
        rr(c,251,347,381,424,11,panel);
        int ink=dark?0xFFC4D7F2:0xFF4D637F;
        text(c,"ALTURA · LATERAL",259,362,9,ink);
        line(c,259,406,373,406,0xFF93A2BA,1.4f);
        float offset=depth*17f;
        line(c,284,380+offset,341,381+offset,palette.top,14f);
        circle(c,274,382+offset,10,palette.hair);
        line(c,296,384+offset,296,403,palette.skin,5f);
        line(c,341,381+offset,362,403,palette.legColor,5f);
        circle(c,296,403,3,palette.skin);
        text(c,"Vista auxiliar",259,418,7.5f,ink);
    }
}
