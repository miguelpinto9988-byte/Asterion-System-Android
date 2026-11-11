package pe.asterionsystem.app;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;

/**
 * Option D: organic Canvas volumes ordered as modular layered pieces.
 * No external rendering SDK and no new Paint/Path allocation per frame.
 * This is a stylized exercise illustration, not posture analysis.
 */
public final class AvatarRenderer {
    private static final Typeface LABEL_FACE=Typeface.create(
            Typeface.DEFAULT,Typeface.BOLD);
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();
    private AvatarPalette palette=AvatarPalette.BLUE;
    private AvatarBodySpec body=AvatarBodySpec.DEFAULT;

    public void draw(Canvas canvas,AvatarPose pose,
                     AvatarBodySpec body,AvatarPalette palette,boolean dark) {
        if(canvas==null||pose==null||body==null||palette==null)
            throw new IllegalArgumentException("Renderer necesita pose, spec y paleta");
        this.body=body;
        this.palette=palette;
        AvatarPose p=pose;
        final int bg=dark?0xFF13131D:0xFFF8F9FD;
        final int grid=dark?0xFF272838:0xFFEAECF4;
        final int top=palette.top;
        final int hi=palette.accent;
        drawRoundedRect(canvas,10,8,390,468,20,bg,0);
        color(grid);
        paint.setStrokeWidth(1f);
        for(int x=50;x<=350;x+=50)canvas.drawLine(x,40,x,405,paint);
        for(int y=70;y<=400;y+=55)canvas.drawLine(35,y,365,y,paint);

        drawRoundedRect(canvas,38,389,362,392,2,dark?0xFF666B82:0xFFD7DCE7,0);
        drawOval(canvas,113,373,284,389,dark?0xFF242532:0xFFE0E4EB);


        // Far limbs first. Rounded pieces overlap at each hinge; joints do
        // not display a separate dark ring when the skeleton bends.
        drawShoe(canvas,p.farAnkle,true);
        drawLimb(canvas,p.farHip,p.farKnee,body.thighWidth*.88f,body.thighWidth*.72f,
                palette.farLegColor);
        drawLimb(canvas,p.farKnee,p.farAnkle,body.calfWidth*.92f,body.calfWidth*.62f,
                palette.female?0xFF514D70:palette.skinShade);
        drawJoint(canvas,p.farKnee,11.5f,palette.female?0xFF514D70:palette.skinShade);
        drawLimb(canvas,p.farShoulder,p.farElbow,body.upperArmWidth*.74f,body.upperArmWidth*.62f,palette.skinShade);
        drawLimb(canvas,p.farElbow,p.farWrist,body.forearmWidth,body.forearmWidth*.65f,palette.skinShade);
        drawJoint(canvas,p.farElbow,8f,palette.skinShade);
        drawHand(canvas,p.farWrist,palette.skinShade);

        // Front leg: continuous rounded leg silhouettes under sportswear.
        drawShoe(canvas,p.ankle,false);
        drawLimb(canvas,p.hip,p.knee,body.thighWidth,body.thighWidth*.73f,palette.female?palette.legColor:palette.skin);
        drawLimb(canvas,p.knee,p.ankle,body.calfWidth,body.calfWidth*.67f,palette.female?palette.legColor:palette.skin);
        drawJoint(canvas,p.knee,12.5f,palette.female?palette.legColor:palette.skin);
        if(!palette.female) {
            AvatarKinematics.Point hemStart=interp(p.hip,p.knee,.47f);
            AvatarKinematics.Point hemEnd=interp(p.hip,p.knee,.50f);
            drawLimb(canvas,p.hip,hemStart,body.thighWidth*1.16f,body.thighWidth*.91f,palette.shorts);
            drawLimb(canvas,hemStart,hemEnd,28f,25f,hi);
        } else {
            drawLimb(canvas,interp(p.hip,p.knee,.21f),
                    interp(p.hip,p.knee,.80f),3f,2.6f,hi);
        }

        // Contoured torso instead of the former straight cylinder.
        drawTorso(canvas,p,top,hi);
        drawLimb(canvas,p.shoulder,p.neck,21f,13f,palette.skin);
        drawHead(canvas,p.head);
        if(!palette.female) {
            drawLimb(canvas,p.shoulder,interp(p.shoulder,p.elbow,.29f),
                    body.upperArmWidth,body.upperArmWidth*.81f,top);
            drawLimb(canvas,interp(p.shoulder,p.elbow,.23f),p.elbow,
                    19f,17f,palette.skin);
        } else {
            drawLimb(canvas,p.shoulder,p.elbow,19f,16f,palette.skin);
            drawJoint(canvas,p.shoulder,12f,top);
        }
        drawLimb(canvas,p.elbow,p.wrist,body.forearmWidth,body.forearmWidth*.71f,palette.skin);
        drawJoint(canvas,p.elbow,8.8f,palette.skin);
        drawHand(canvas,p.wrist,palette.skin);

        if(dark) {
            paint.setColor(0xFFADB4CE);
        }else{
            paint.setColor(0xFF63718C);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(12);
        paint.setTypeface(LABEL_FACE);
        canvas.drawText(palette.female?"AVATAR MORADO":"AVATAR AZUL",38,436,paint);
        canvas.drawText("SENTADILLA · ILUSTRACIÓN 2D",38,453,paint);

    }

    private AvatarKinematics.Point interp(AvatarKinematics.Point a,
                                           AvatarKinematics.Point b,float t) {
        return new AvatarKinematics.Point(a.x+(b.x-a.x)*t,a.y+(b.y-a.y)*t);
    }

    /**
     * Coordinates in a soft torso silhouette: along the hip-shoulder axis
     * and across its normal. Shoulder width, chest and waist can vary without
     * changing the animated spine length or introducing a mesh dependency.
     */
    private AvatarKinematics.Point torsoPoint(AvatarPose pose,
                                               float along,float side) {
        float dx=pose.shoulder.x-pose.hip.x;
        float dy=pose.shoulder.y-pose.hip.y;
        float length=(float)Math.hypot(dx,dy);
        float nx=-dy/length,ny=dx/length;
        return new AvatarKinematics.Point(
                pose.hip.x+dx*along+nx*side,
                pose.hip.y+dy*along+ny*side);
    }

    private void drawTorso(Canvas canvas,AvatarPose p,
                           int top,int highlight) {
        AvatarKinematics.Point a=torsoPoint(p,0f,-body.waistHalfWidth);
        AvatarKinematics.Point b=torsoPoint(p,.42f,-body.waistHalfWidth*.95f);
        AvatarKinematics.Point c=torsoPoint(p,.78f,-body.chestHalfWidth);
        AvatarKinematics.Point d=torsoPoint(p,1f,-body.shoulderHalfWidth);
        AvatarKinematics.Point e=torsoPoint(p,1f,body.shoulderHalfWidth);
        AvatarKinematics.Point f=torsoPoint(p,.78f,body.chestHalfWidth);
        AvatarKinematics.Point g=torsoPoint(p,.42f,body.waistHalfWidth*.95f);
        AvatarKinematics.Point h=torsoPoint(p,0f,body.waistHalfWidth);
        AvatarKinematics.Point v,v2;

        path.rewind();
        path.moveTo(a.x,a.y);
        v=torsoPoint(p,.15f,-body.waistHalfWidth*1.14f);v2=torsoPoint(p,.25f,-body.waistHalfWidth);
        path.cubicTo(v.x,v.y,v2.x,v2.y,b.x,b.y);
        v=torsoPoint(p,.55f,-body.waistHalfWidth*.95f);v2=torsoPoint(p,.65f,-body.chestHalfWidth*.97f);
        path.cubicTo(v.x,v.y,v2.x,v2.y,c.x,c.y);
        v=torsoPoint(p,.93f,-body.chestHalfWidth*1.03f);v2=torsoPoint(p,1f,-body.shoulderHalfWidth*1.26f);
        path.cubicTo(v.x,v.y,v2.x,v2.y,d.x,d.y);
        v=torsoPoint(p,1.09f,0f);
        path.quadTo(v.x,v.y,e.x,e.y);
        v=torsoPoint(p,1f,body.shoulderHalfWidth*1.26f);v2=torsoPoint(p,.93f,body.chestHalfWidth*1.03f);
        path.cubicTo(v.x,v.y,v2.x,v2.y,f.x,f.y);
        v=torsoPoint(p,.65f,body.chestHalfWidth*.97f);v2=torsoPoint(p,.55f,body.waistHalfWidth*.95f);
        path.cubicTo(v.x,v.y,v2.x,v2.y,g.x,g.y);
        v=torsoPoint(p,.25f,body.waistHalfWidth);v2=torsoPoint(p,.15f,body.waistHalfWidth*1.14f);
        path.cubicTo(v.x,v.y,v2.x,v2.y,h.x,h.y);
        v=torsoPoint(p,-.055f,0f);
        path.quadTo(v.x,v.y,a.x,a.y);
        path.close();
        color(top);
        canvas.drawPath(path,paint);
        paint.setColor(0x3026334C);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(.55f);
        canvas.drawPath(path,paint);
        paint.setStyle(Paint.Style.FILL);

        // Minimal technical paneling: reference D, not a mechanical joint.
        drawLimb(canvas,torsoPoint(p,.20f,-body.waistHalfWidth*.54f),
                torsoPoint(p,.77f,-body.chestHalfWidth*.57f),
                4.4f,3.2f,highlight);
        drawLimb(canvas,torsoPoint(p,.40f,body.waistHalfWidth*.76f),
                torsoPoint(p,.77f,body.chestHalfWidth*.70f),
                2.6f,2.2f,palette.garmentShade);
    }

    /**
     * Tapered filled Bézier volume with curved ends. Overlapping rounded
     * silhouettes eliminate seams when two hinged segments rotate.
     */
    private void drawLimb(Canvas c,AvatarKinematics.Point a,
                          AvatarKinematics.Point b,float wa,float wb,int fill) {
        float dx=b.x-a.x,dy=b.y-a.y;
        float distance=(float)Math.hypot(dx,dy);
        if(distance<.01f)return;
        float ux=dx/distance,uy=dy/distance;
        float nx=-uy,ny=ux;
        float x1=a.x+nx*wa*.5f,y1=a.y+ny*wa*.5f;
        float x2=b.x+nx*wb*.5f,y2=b.y+ny*wb*.5f;
        float x3=b.x-nx*wb*.5f,y3=b.y-ny*wb*.5f;
        float x4=a.x-nx*wa*.5f,y4=a.y-ny*wa*.5f;
        path.rewind();
        path.moveTo(x1,y1);
        path.cubicTo(a.x+dx*.30f+nx*wa*.51f,
                a.y+dy*.30f+ny*wa*.51f,
                a.x+dx*.72f+nx*wb*.51f,
                a.y+dy*.72f+ny*wb*.51f,x2,y2);
        path.cubicTo(b.x+ux*wb*.40f+nx*wb*.44f,
                b.y+uy*wb*.40f+ny*wb*.44f,
                b.x+ux*wb*.40f-nx*wb*.44f,
                b.y+uy*wb*.40f-ny*wb*.44f,x3,y3);
        path.cubicTo(a.x+dx*.72f-nx*wb*.51f,
                a.y+dy*.72f-ny*wb*.51f,
                a.x+dx*.30f-nx*wa*.51f,
                a.y+dy*.30f-ny*wa*.51f,x4,y4);
        path.cubicTo(a.x-ux*wa*.40f-nx*wa*.44f,
                a.y-uy*wa*.40f-ny*wa*.44f,
                a.x-ux*wa*.40f+nx*wa*.44f,
                a.y-uy*wa*.40f+ny*wa*.44f,x1,y1);
        path.close();
        color(fill);
        c.drawPath(path,paint);
        if(wa>=7f && wb>=7f) {
            paint.setColor(0x3026334C);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(.55f);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            c.drawPath(path,paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }

    /** Joint fill is continuous with its segments; no decorative dark ring. */
    private void drawJoint(Canvas c,AvatarKinematics.Point p,float radius,int fill) {
        color(fill);
        c.drawCircle(p.x,p.y,radius,paint);
    }
    private void drawShoe(Canvas c,AvatarKinematics.Point ankle,boolean far) {
        float x=ankle.x,y=ankle.y;
        drawRoundedRect(c,x-12,y-12,x+39,y+11,9,
                far?0xFFADB7D4:palette.shoe,1.5f);
        drawRoundedRect(c,x-8,y-10,x+30,y+4,8,
                far?0xFF687696:(palette.female?0xFF9C81D0:0xFF3A69B0),1f);
        color(palette.shoeDark);
        paint.setStrokeWidth(2f);
        c.drawLine(x-7,y+8,x+36,y+8,paint);
    }
    private void drawHand(Canvas c,AvatarKinematics.Point wrist,int skin){
        drawOval(c,wrist.x-7,wrist.y-7,wrist.x+10,wrist.y+6,skin);
    }

    private void drawHead(Canvas c,AvatarKinematics.Point center) {
        c.save();
        c.translate(center.x,center.y);
        c.scale(body.headScale,body.headScale);
        float x=0f,y=0f;
        if(palette.female) {
            drawOval(c,x-25,y-26,x+13,y+27,palette.hair);
            // Ponytail follows the head as one piece.
            path.reset();path.moveTo(x-18,y-18);
            path.cubicTo(x-41,y-39,x-47,y-14,x-39,y+4);
            path.cubicTo(x-44,y+19,x-31,y+22,x-25,y+16);
            path.quadTo(x-20,y+1,x-18,y-18);path.close();
            color(palette.hair);c.drawPath(path,paint);
        }
        drawOval(c,x-18,y-23,x+20,y+21,palette.skin);
        path.reset();
        path.moveTo(x-19,y-7);
        path.cubicTo(x-22,y-26,x-8,y-34,x+11,y-28);
        path.cubicTo(x+25,y-26,x+22,y-10,x+19,y-9);
        path.quadTo(x+7,y-17,x-2,y-13);
        path.quadTo(x-12,y-11,x-19,y-7);path.close();
        color(palette.hair);c.drawPath(path,paint);
        color(palette.hairHighlight);
        c.drawOval(x-13,y-22,x+8,y-19,paint);
        color(palette.outline);
        c.drawCircle(x+11,y-1,1.7f,paint);
        paint.setStrokeWidth(1.3f);
        c.drawLine(x+17,y+10,x+11,y+12,paint);
        c.restore();
    }
    private void drawOval(Canvas c,float l,float t,float r,float b,int fill){
        color(fill);
        c.drawOval(l,t,r,b,paint);
    }
    private void drawRoundedRect(Canvas c,float l,float t,float r,float b,
                                 float radius,int fill,float border) {
        color(fill);
        c.drawRoundRect(l,t,r,b,radius,radius,paint);
        if(border>0) {
            paint.setColor(palette.outline);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(border);
            c.drawRoundRect(l,t,r,b,radius,radius,paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }
    private void color(int value){
        paint.setShader(null);
        paint.setColor(value);
        paint.setStyle(Paint.Style.FILL);
    }
}
