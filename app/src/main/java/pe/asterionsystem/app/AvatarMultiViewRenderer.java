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
    private final AvatarArtDetails details=new AvatarArtDetails();
    private final AvatarOrganicVolume organic=new AvatarOrganicVolume();
    private AvatarBodySpec bodySpec=AvatarBodySpec.DEFAULT;
    private final LinearGradient blue=new LinearGradient(
            140f,70f,255f,300f,0xFF397DBE,0xFF163C78,Shader.TileMode.CLAMP);
    private final LinearGradient purple=new LinearGradient(
            140f,70f,255f,300f,0xFF9976C4,0xFF51387C,Shader.TileMode.CLAMP);
    private AvatarPalette palette=AvatarPalette.BLUE;
    private boolean dark;

    public void draw(Canvas c,AvatarMultiViewKinematics.Pose p,
                     AvatarMultiViewKinematics.ViewMode camera,
                     AvatarPalette palette,boolean dark) {
        draw(c,p,camera,palette,dark,AvatarBodySpec.DEFAULT);
    }

    public void draw(Canvas c,AvatarMultiViewKinematics.Pose p,
                     AvatarMultiViewKinematics.ViewMode camera,
                     AvatarPalette palette,boolean dark,AvatarBodySpec body) {
        if(c==null||p==null||camera==null||palette==null||body==null)
            throw new IllegalArgumentException("Vista, paleta y spec necesarios");
        // Every camera validates its anatomical joints and shared body spec.
        AvatarSkeletonFrame skeleton=AvatarSkeletonFrame.fromCamera(p,camera,body);
        this.palette=palette;this.dark=dark;this.bodySpec=skeleton.proportions;
        int bg=dark?0xFF131722:0xFFF8FAFE;
        int grid=dark?0xFF252D40:0xFFE8EDF5;
        rr(c,10,8,390,468,18,bg);
        color(grid);
        paint.setStrokeWidth(1f);
        for(int y=65;y<420;y+=55)c.drawLine(35,y,365,y,paint);
        for(int x=55;x<360;x+=55)c.drawLine(x,45,x,413,paint);
        if(camera==AvatarMultiViewKinematics.ViewMode.FRONT
                ||camera==AvatarMultiViewKinematics.ViewMode.FRONT_SQUAT
                ||camera==AvatarMultiViewKinematics.ViewMode.FRONT_JACKS
                ||camera==AvatarMultiViewKinematics.ViewMode.FRONT_ELBOW) {
            boolean squat=camera==AvatarMultiViewKinematics.ViewMode.FRONT_SQUAT;
            boolean jacks=camera==AvatarMultiViewKinematics.ViewMode.FRONT_JACKS;
            drawFront(c,p,squat||jacks);
            String scene=squat?"FRONTAL · SENTADILLA ABIERTA":
                    jacks?"FRONTAL · SALTOS DE TIJERA":
                    camera==AvatarMultiViewKinematics.ViewMode.FRONT_ELBOW
                    ?"FRONTAL · FLEXIÓN DE CODOS":"FRONTAL · ELEVACIÓN DE BRAZOS";
            text(c,scene,34,447,11,
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

    /**
     * Reusable soft cutout contour. The variable-width silhouette matches
     * the Bézier limbs used by AvatarRenderer, avoiding cylindrical front
     * legs and round elbow beads in other camera projections.
     */
    private void tapered(Canvas c,AvatarKinematics.Point a,
                         AvatarKinematics.Point b,float wa,float wb,int fill){
        float dx=b.x-a.x,dy=b.y-a.y;
        float d=(float)Math.hypot(dx,dy);
        if(d<.01f)return;
        float nx=-dy/d,ny=dx/d,ux=dx/d,uy=dy/d;
        path.rewind();
        path.moveTo(a.x+nx*wa*.5f,a.y+ny*wa*.5f);
        path.cubicTo(a.x+dx*.3f+nx*wa*.51f,a.y+dy*.3f+ny*wa*.51f,
                a.x+dx*.72f+nx*wb*.51f,a.y+dy*.72f+ny*wb*.51f,
                b.x+nx*wb*.5f,b.y+ny*wb*.5f);
        path.cubicTo(b.x+ux*wb*.4f+nx*wb*.44f,
                b.y+uy*wb*.4f+ny*wb*.44f,
                b.x+ux*wb*.4f-nx*wb*.44f,
                b.y+uy*wb*.4f-ny*wb*.44f,
                b.x-nx*wb*.5f,b.y-ny*wb*.5f);
        path.cubicTo(a.x+dx*.72f-nx*wb*.51f,
                a.y+dy*.72f-ny*wb*.51f,
                a.x+dx*.3f-nx*wa*.51f,
                a.y+dy*.3f-ny*wa*.51f,
                a.x-nx*wa*.5f,a.y-ny*wa*.5f);
        path.cubicTo(a.x-ux*wa*.4f-nx*wa*.44f,
                a.y-uy*wa*.4f-ny*wa*.44f,
                a.x-ux*wa*.4f+nx*wa*.44f,
                a.y-uy*wa*.4f+ny*wa*.44f,
                a.x+nx*wa*.5f,a.y+ny*wa*.5f);
        path.close();
        color(fill);c.drawPath(path,paint);
        paint.setColor(0x3026334C);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(.55f);
        paint.setStrokeJoin(Paint.Join.ROUND);
        c.drawPath(path,paint);
        paint.setStyle(Paint.Style.FILL);
    }

    /** Frontal anatomy follows the actual hip/shoulder pair, not fixed pixels.
     * A deliberate short neck is visible above the close jersey collar. */
    private void drawFront(Canvas c,AvatarMultiViewKinematics.Pose p,
                           boolean squat) {
        AvatarOrientationRules.requireUprightFrontPose(p);
        c.save();
        // Frontal silhouette has the same on-screen head/leg presence as
        // the side camera; keep footwear within the floor framing.
        c.translate(0f,-18f);
        c.scale(.94f,.94f,200f,222f);
        rr(c,57,431,343,434,2,dark?0xFF65758C:0xFFD4DFEA);
        color(dark?0xFF263247:0xFFDDE6EE);
        c.drawOval(80,430,320,440,paint);

        // Far-to-near: articulated denim legs are painted behind the pelvis.
        drawFrontDenimLeg(c,p.leftHip,p.leftKnee,p.leftAnkle,true);
        drawFrontDenimLeg(c,p.rightHip,p.rightKnee,p.rightAnkle,false);
        drawShoeFront(c,p.leftAnkle.x,p.leftAnkle.y);
        drawShoeFront(c,p.rightAnkle.x,p.rightAnkle.y);

        float waistY=p.leftHip.y+8f;
        path.rewind();
        path.moveTo(p.leftHip.x-12f,p.leftHip.y-11f);
        path.quadTo(200f,p.leftHip.y-17f,p.rightHip.x+12f,p.rightHip.y-11f);
        path.lineTo(p.rightHip.x+9f,p.rightHip.y+28f);
        path.quadTo(200f,p.rightHip.y+34f,p.leftHip.x-9f,p.leftHip.y+28f);
        path.close();
        color(palette.legColor);c.drawPath(path,paint);
        line(c,p.leftHip.x-9f,waistY,200f,waistY+4f,
                0xFF718CA9,1.3f);
        line(c,200f,waistY+4f,p.rightHip.x+9f,waistY,
                0xFF718CA9,1.3f);
        rr(c,196f,waistY-1f,204f,waistY+4f,1.8f,0xFFB6C8D7);

        // Sleeve-to-forearm drawing uses separate IK endpoints. Small shoulder
        // seams visually blend the sleeves into the torso, rather than orbs.
        drawFrontArm(c,p.leftShoulder,p.leftElbow,p.leftWrist,true);
        drawFrontArm(c,p.rightShoulder,p.rightElbow,p.rightWrist,false);

        float sy=p.leftShoulder.y,hy=p.leftHip.y;
        path.rewind();
        path.moveTo(p.leftShoulder.x+11f,sy-5f);
        path.cubicTo(158f,sy-14f,180f,sy-13f,200f,sy-8f);
        path.cubicTo(222f,sy-13f,240f,sy-14f,
                p.rightShoulder.x-11f,sy-5f);
        path.cubicTo(235f,sy+24f,228f,sy+52f,229f,hy-6f);
        path.quadTo(201f,hy+4f,171f,hy-6f);
        path.cubicTo(172f,sy+54f,165f,sy+25f,
                p.leftShoulder.x+11f,sy-5f);
        path.close();
        color(palette.top);
        paint.setShader(palette.female?purple:blue);
        c.drawPath(path,paint);paint.setShader(null);
        // Narrow side panels, collar seam and gentle fabric grain.
        line(c,174f,sy+16f,179f,hy-14f,palette.accent,2f);
        line(c,226f,sy+16f,221f,hy-14f,palette.accent,2f);
        line(c,180f,hy-7f,221f,hy-7f,0xFF8FC0E4,1.1f);
        for(int n=0;n<5;n++) {
            float y=sy+35f+11f*n;
            line(c,194f,y,210f,y+1f,0x334CB4E0,.7f);
        }
        // Skin neck: head bottom ~head.y+24, collar begins at shoulder-11.
        float topNeck=p.head.y+19f;
        rr(c,193f,topNeck,207f,sy-6f,5f,palette.skin);
        rr(c,190f,sy-11f,210f,sy-1f,5f,palette.top);
        line(c,191f,sy-10f,209f,sy-10f,
                palette.female?0xFFCDB7EF:0xFF9FD1F4,1.4f);
        drawFaceFront(c,p.head.x,p.head.y);
        text(c,"A",215f,sy+42f,11f,0xFFCCE7FA);
        c.restore();
    }

    private void drawFrontDenimLeg(Canvas c,AvatarKinematics.Point hip,
                                   AvatarKinematics.Point knee,
                                   AvatarKinematics.Point ankle,
                                   boolean left) {
        organic.drawChain(c,hip,knee,ankle,
                bodySpec.thighWidth,bodySpec.calfWidth,
                bodySpec.calfWidth*.67f,palette.legColor);
        // Flexible diagonal textile seam follows both moving bones.
        float sign=left?-1f:1f;
        line(c,hip.x+sign*5f,hip.y+23f,
                knee.x+sign*3f,knee.y-10f,0x887F9AB7,1.4f);
        line(c,knee.x+sign*3f,knee.y+10f,
                ankle.x+sign*3f,ankle.y-12f,0x887F9AB7,1.1f);
        // One small, tapered fabric crease from the shared art module;
        // never a second rectangular patch protruding above the knee.
        // The same denim seam-and-knee artwork as the lateral avatar.
        details.drawCameraJeans(c,hip,knee,ankle,left);
    }

    private void drawFrontArm(Canvas c,AvatarKinematics.Point shoulder,
                              AvatarKinematics.Point elbow,
                              AvatarKinematics.Point wrist,boolean left) {
        float dx=elbow.x-shoulder.x,dy=elbow.y-shoulder.y;
        AvatarKinematics.Point sleeve=new AvatarKinematics.Point(
                shoulder.x+dx*.32f,shoulder.y+dy*.32f);
        tapered(c,shoulder,sleeve,
                bodySpec.upperArmWidth*.91f,
                bodySpec.upperArmWidth*.79f,palette.top);
        organic.drawChain(c,sleeve,elbow,wrist,
                bodySpec.upperArmWidth*.72f,bodySpec.forearmWidth*.94f,
                bodySpec.forearmWidth*.71f,palette.skin);
        details.drawForearm(c,elbow,wrist,palette);
        details.drawProjectedHand(c,wrist,palette);
        float u=(float)Math.hypot(dx,dy);
        if(u>.001f){
            float nx=-dy/u,ny=dx/u;
            line(c,sleeve.x-9f*nx,sleeve.y-9f*ny,
                    sleeve.x+9f*nx,sleeve.y+9f*ny,
                    palette.accent,1.8f);
        }
        // Small wrist cuff readable during a complete arm raise.
        float vx=wrist.x-elbow.x,vy=wrist.y-elbow.y;
        float l=(float)Math.hypot(vx,vy);
        if(l>.001f) {
            float x=wrist.x-vx/l*9f,y=wrist.y-vy/l*9f;
            line(c,x-vy/l*5f,y+vx/l*5f,
                    x+vy/l*5f,y-vx/l*5f,0xFF4B7EAC,1.8f);
        }
    }

    private void drawFaceFront(Canvas c,float x,float y){
        details.drawHeadFront(c,new AvatarKinematics.Point(x,y),
                bodySpec,palette);
    }

    private void drawShoeFront(Canvas c,float x,float y){
        details.drawProjectedSneaker(c,new AvatarKinematics.Point(x,y),
                palette,false);
    }

    private void handsSimple(Canvas c,AvatarKinematics.Point l,
                             AvatarKinematics.Point r){
        circle(c,l.x,l.y,8,palette.skin);
        circle(c,r.x,r.y,8,palette.skin);
    }

    /** Floor-facing, overhead camera: back + crown are visible. Hands below
     * the chest are partly occluded; enlarged inset documents the silhouettes.
     * A side-height card shows motion that overhead cannot show directly. */
    private void drawDiamondTop(Canvas c,AvatarMultiViewKinematics.Pose p) {
        AvatarOrientationRules.requireProneTopPose(p);
        rr(c,22,47,276,426,17,dark?0xFF1B293C:0xFFF0F5FA);
        color(dark?0xFF22334A:0xFFDCE7F0);
        c.drawOval(117,104,253,406,paint);
        // Fixed toe contacts and gradual denim narrowing towards each shoe.
        for(int direction:new int[]{-1,1}) {
            AvatarKinematics.Point h=direction<0?p.leftHip:p.rightHip;
            AvatarKinematics.Point k=direction<0?p.leftKnee:p.rightKnee;
            AvatarKinematics.Point a=direction<0?p.leftAnkle:p.rightAnkle;
            organic.drawChain(c,h,k,a,
                    bodySpec.thighWidth*.93f,
                    bodySpec.calfWidth*.92f,
                    bodySpec.calfWidth*.64f,palette.legColor);
            details.drawCameraJeans(c,h,k,a,direction<0);
            AvatarFootOrientation.FootProjection foot=
                    AvatarFootOrientation.proneTop(p,
                        direction<0?AvatarFootOrientation.Side.LEFT
                                   :AvatarFootOrientation.Side.RIGHT);
            details.drawProneTopSneaker(c,foot,palette);
        }
        // Hands and forearms are behind the back from this camera.
        drawTopArm(c,p.leftShoulder,p.leftElbow,p.leftWrist);
        drawTopArm(c,p.rightShoulder,p.rightElbow,p.rightWrist);

        // Neck begins beneath the visible hair, joins the jersey collar.
        float nY=p.neck.y;
        rr(c,177f,p.head.y+17f,193f,nY+16f,6f,palette.skin);

        // This tapered contour is the BACK of the shirt, not a front-facing
        // standing torso. It occludes the hands located beneath the chest.
        path.rewind();
        path.moveTo(p.leftShoulder.x+5f,p.leftShoulder.y-7f);
        path.cubicTo(146f,p.leftShoulder.y-21f,
                217f,p.rightShoulder.y-21f,
                p.rightShoulder.x-5f,p.rightShoulder.y-7f);
        path.cubicTo(227f,205f,222f,244f,p.rightHip.x+11f,p.rightHip.y+2f);
        path.quadTo(185f,300f,p.leftHip.x-11f,p.leftHip.y+2f);
        path.cubicTo(148f,245f,142f,211f,
                p.leftShoulder.x+5f,p.leftShoulder.y-7f);
        path.close();
        color(palette.top);
        paint.setShader(palette.female?purple:blue);
        c.drawPath(path,paint);paint.setShader(null);
        line(c,p.leftShoulder.x+15f,p.leftShoulder.y+20f,
                p.leftHip.x-2f,p.leftHip.y-17f,
                palette.accent,1.8f);
        line(c,p.rightShoulder.x-15f,p.rightShoulder.y+20f,
                p.rightHip.x+2f,p.rightHip.y-17f,
                palette.accent,1.8f);
        line(c,164f,p.leftShoulder.y+21f,205f,p.leftShoulder.y+21f,
                0xCC9DC6E9,1f);
        line(c,168f,p.leftHip.y-13f,202f,p.leftHip.y-13f,
                0x9991BFDE,1.1f);

        // Collar is drawn around a moderate-short visible neck.
        rr(c,175f,p.leftShoulder.y-13f,
                195f,p.leftShoulder.y-3f,5f,palette.top);
        line(c,177f,p.leftShoulder.y-11f,
                193f,p.leftShoulder.y-11f,0xFFADD4F0,1.3f);
        // The same palette hair and headScale as the two other cameras.
        details.drawHeadTop(c,p.head,bodySpec,palette);

        // Paired dashed anchors show hidden hand contact, not floating palms.
        color(dark?0xFFC8D9EA:0xFF7393AF);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.3f);
        paint.setPathEffect(null);
        c.drawCircle(185f,p.leftWrist.y,18f,paint);
        paint.setStyle(Paint.Style.FILL);
        line(c,205f,p.leftWrist.y,280f,227f,
                dark?0xFFAED4F3:0xFF74A6CB,1f);
        // Explanation visible at mobile sizes.
        text(c,"ZONA BAJO EL TORSO",45f,55f,8f,
                dark?0xFFC9D8EB:0xFF617E9D);
        drawHandsInset(c);
        drawPushupSideInset(c,p.depth);
    }

    private void drawTopArm(Canvas c,AvatarKinematics.Point shoulder,
                            AvatarKinematics.Point elbow,
                            AvatarKinematics.Point wrist){
        // Only the near-shoulder THIRD is fabric, just as in the side
        // view; painting the whole upper arm blue was a V8 inconsistency.
        AvatarKinematics.Point sleeve=new AvatarKinematics.Point(
                shoulder.x+(elbow.x-shoulder.x)*.32f,
                shoulder.y+(elbow.y-shoulder.y)*.32f);
        tapered(c,shoulder,sleeve,bodySpec.upperArmWidth*.86f,
                bodySpec.upperArmWidth*.75f,palette.top);
        organic.drawChain(c,sleeve,elbow,wrist,
                bodySpec.upperArmWidth*.68f,bodySpec.forearmWidth*.91f,
                bodySpec.forearmWidth*.70f,palette.skin);
        details.drawForearm(c,elbow,wrist,palette);
        details.drawProjectedHand(c,wrist,palette);
        float length=(float)Math.hypot(elbow.x-shoulder.x,elbow.y-shoulder.y);
        if(length>.001f){
            float px=-(elbow.y-shoulder.y)/length;
            float py=(elbow.x-shoulder.x)/length;
            line(c,sleeve.x-9f*px,sleeve.y-9f*py,
                    sleeve.x+9f*px,sleeve.y+9f*py,
                    palette.accent,1.5f);
        }
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
