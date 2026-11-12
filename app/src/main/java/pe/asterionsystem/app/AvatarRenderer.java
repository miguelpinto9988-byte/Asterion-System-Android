package pe.asterionsystem.app;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Shader;
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
    private final AvatarArtDetails details=new AvatarArtDetails();
    private final AvatarSoftContour contours=new AvatarSoftContour();
    private final LinearGradient blueFabric=new LinearGradient(130f,0f,270f,0f,
            new int[]{0xFF163C78,0xFF397DBE,0xFF1A427A},
            null,Shader.TileMode.CLAMP);
    private final LinearGradient purpleFabric=new LinearGradient(130f,0f,270f,0f,
            new int[]{0xFF51387C,0xFF9976C4,0xFF51387C},
            null,Shader.TileMode.CLAMP);
    private AvatarPalette palette=AvatarPalette.BLUE;
    private AvatarBodySpec body=AvatarBodySpec.DEFAULT;

    public void draw(Canvas canvas,AvatarPose pose,
                     AvatarBodySpec body,AvatarPalette palette,boolean dark) {
        draw(canvas,pose,body,palette,dark,"Sentadilla");
    }

    public void draw(Canvas canvas,AvatarPose pose,AvatarBodySpec body,
                     AvatarPalette palette,boolean dark,String motionLabel) {
        if(canvas==null||pose==null||body==null||palette==null)
            throw new IllegalArgumentException("Renderer necesita pose, spec y paleta");
        // Same canonical anatomical joint inventory used by all cameras.
        AvatarSkeletonFrame skeleton=AvatarSkeletonFrame.fromSide(pose,body);
        this.body=skeleton.proportions;
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

        // Raise arms fully overhead without clipping beyond the fixed
        // 400x480 viewport. Keep the shoe floor coordinate unchanged.
        canvas.save();
        // The neutral standing head previously clipped at the card's top.
        // Zoom out around the planted shoe line so feet remain grounded
        // in every phase and the hair/short neck remain visible.
        float cameraScale="Elevación de brazos".equals(motionLabel)
                ?.78f:.94f;
        canvas.scale(cameraScale,cameraScale,200f,364f);

        // Far limbs first. Rounded pieces overlap at each hinge; joints do
        // not display a separate dark ring when the skeleton bends.
        details.drawSneaker(canvas,p.farAnkle,palette,true);
        drawLimb(canvas,p.farHip,p.farKnee,body.thighWidth*.88f,body.thighWidth*.72f,
                palette.farLegColor);
        drawLimb(canvas,p.farKnee,p.farAnkle,body.calfWidth*.92f,body.calfWidth*.62f,
                palette.female?0xFF514D70:0xFF293951);
        drawJoint(canvas,p.farKnee,body.thighWidth*.72f,
                body.calfWidth*.92f,palette.female?0xFF514D70:0xFF293951);
        details.drawDenimLeg(canvas,p.farHip,p.farKnee,p.farAnkle,true);
        // Rear upper arm begins inside the back of the jersey; a short
        // sleeve hides the shoulder pivot instead of a protruding cylinder.
        drawLimb(canvas,p.farShoulder,interp(p.farShoulder,p.farElbow,.34f),
                body.upperArmWidth*.75f,body.upperArmWidth*.68f,palette.top);
        drawLimb(canvas,interp(p.farShoulder,p.farElbow,.27f),p.farElbow,
                body.upperArmWidth*.67f,body.upperArmWidth*.58f,palette.skinShade);
        drawLimb(canvas,p.farElbow,p.farWrist,body.forearmWidth,
                body.forearmWidth*.65f,palette.skinShade);
        drawJoint(canvas,p.farElbow,body.upperArmWidth*.58f,
                body.forearmWidth,palette.skinShade);
        details.drawHand(canvas,p.farWrist,palette.skinShade);

        // Front leg: continuous rounded leg silhouettes under sportswear.
        details.drawSneaker(canvas,p.ankle,palette,false);
        drawLimb(canvas,p.hip,p.knee,body.thighWidth,body.thighWidth*.73f,
                palette.legColor);
        drawLimb(canvas,p.knee,p.ankle,body.calfWidth,body.calfWidth*.67f,
                palette.legColor);
        drawJoint(canvas,p.knee,body.thighWidth*.73f,
                body.calfWidth,palette.legColor);
        details.drawDenimLeg(canvas,p.hip,p.knee,p.ankle,false);
        details.drawPelvis(canvas,p.hip,p.shoulder);

        // Contoured torso instead of the former straight cylinder.
        drawTorso(canvas,p,top,hi);
        details.drawJersey(canvas,p.hip,p.shoulder,body,palette);
        details.drawShortNeck(canvas,p.shoulder,p.head,palette);
        // Face follows the spine orientation. During a floor-facing burpee
        // the profile must look at the floor rather than stay upright.
        canvas.save();
        canvas.rotate(AvatarOrientationRules.sideFacingDegrees(
                p.hip,p.shoulder),p.head.x,p.head.y);
        details.drawHead(canvas,p.head,body,palette);
        canvas.restore();
        // All palettes use the same short-sleeve anatomical cutout. No
        // shoulder sphere or oversized skin cylinder is drawn in either.
        drawLimb(canvas,interp(p.shoulder,p.elbow,.095f),
                interp(p.shoulder,p.elbow,.34f),
                body.upperArmWidth*.91f,body.upperArmWidth*.79f,top);
        details.drawSleeve(canvas,p.shoulder,p.elbow,palette);
        drawLimb(canvas,interp(p.shoulder,p.elbow,.30f),p.elbow,
                body.upperArmWidth*.72f,body.upperArmWidth*.60f,palette.skin);
        drawLimb(canvas,p.elbow,p.wrist,body.forearmWidth,
                body.forearmWidth*.71f,palette.skin);
        drawJoint(canvas,p.elbow,body.upperArmWidth*.60f,
                body.forearmWidth,palette.skin);
        details.drawForearm(canvas,p.elbow,p.wrist,palette);
        details.drawHand(canvas,p.wrist,palette.skin);
        canvas.restore();

        if(dark) {
            paint.setColor(0xFFADB4CE);
        }else{
            paint.setColor(0xFF63718C);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(12);
        paint.setTypeface(LABEL_FACE);
        canvas.drawText(palette.female?"AVATAR MORADO":"AVATAR AZUL",38,436,paint);
        String display=motionLabel==null?"MOVIMIENTO":motionLabel.toUpperCase(
                java.util.Locale.ROOT);
        if(display.length()>19)display=display.substring(0,19);
        canvas.drawText(display+" · ILUSTRACIÓN 2D",38,453,paint);

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

    /** Side-profile torso: restrained back contour, ribcage, abdomen,
     * waist, pelvis and garment shoulder. No concentric oval/capsule body. */
    private void drawTorso(Canvas canvas,AvatarPose p,
                           int top,int highlight) {
        AvatarKinematics.Point hipBack=torsoPoint(p,-.02f,-body.waistHalfWidth*.76f);
        AvatarKinematics.Point lowerBack=torsoPoint(p,.28f,-body.waistHalfWidth*.72f);
        AvatarKinematics.Point chestBack=torsoPoint(p,.79f,-body.chestHalfWidth*.72f);
        AvatarKinematics.Point shoulderBack=torsoPoint(p,1f,-body.shoulderHalfWidth*.68f);
        AvatarKinematics.Point shoulderFront=torsoPoint(p,1f,body.shoulderHalfWidth*.76f);
        AvatarKinematics.Point chestFront=torsoPoint(p,.77f,body.chestHalfWidth*.91f);
        AvatarKinematics.Point abdomenFront=torsoPoint(p,.43f,body.waistHalfWidth*.89f);
        AvatarKinematics.Point hipFront=torsoPoint(p,0f,body.waistHalfWidth*.97f);
        AvatarKinematics.Point p1,p2;
        path.rewind();
        path.moveTo(hipBack.x,hipBack.y);
        p1=torsoPoint(p,.12f,-body.waistHalfWidth*.69f);
        p2=torsoPoint(p,.23f,-body.waistHalfWidth*.71f);
        path.cubicTo(p1.x,p1.y,p2.x,p2.y,lowerBack.x,lowerBack.y);
        p1=torsoPoint(p,.49f,-body.waistHalfWidth*.73f);
        p2=torsoPoint(p,.64f,-body.chestHalfWidth*.70f);
        path.cubicTo(p1.x,p1.y,p2.x,p2.y,chestBack.x,chestBack.y);
        p1=torsoPoint(p,.93f,-body.chestHalfWidth*.70f);
        p2=torsoPoint(p,.98f,-body.shoulderHalfWidth*.75f);
        path.cubicTo(p1.x,p1.y,p2.x,p2.y,shoulderBack.x,shoulderBack.y);
        p1=torsoPoint(p,1.06f,0f);
        path.quadTo(p1.x,p1.y,shoulderFront.x,shoulderFront.y);
        p1=torsoPoint(p,.94f,body.shoulderHalfWidth*.96f);
        p2=torsoPoint(p,.86f,body.chestHalfWidth*.95f);
        path.cubicTo(p1.x,p1.y,p2.x,p2.y,chestFront.x,chestFront.y);
        p1=torsoPoint(p,.61f,body.chestHalfWidth*.85f);
        p2=torsoPoint(p,.52f,body.waistHalfWidth*.91f);
        path.cubicTo(p1.x,p1.y,p2.x,p2.y,abdomenFront.x,abdomenFront.y);
        p1=torsoPoint(p,.27f,body.waistHalfWidth*.85f);
        p2=torsoPoint(p,.13f,body.waistHalfWidth*.98f);
        path.cubicTo(p1.x,p1.y,p2.x,p2.y,hipFront.x,hipFront.y);
        p1=torsoPoint(p,-.055f,0f);
        path.quadTo(p1.x,p1.y,hipBack.x,hipBack.y);
        path.close();
        color(top);
        paint.setShader(palette.female?purpleFabric:blueFabric);
        canvas.drawPath(path,paint);
        paint.setShader(null);
        paint.setColor(0x3026334C);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(.55f);
        canvas.drawPath(path,paint);
        paint.setStyle(Paint.Style.FILL);

        // Panel seams are restrained and remain inside the garment.
        drawLimb(canvas,torsoPoint(p,.22f,-body.waistHalfWidth*.43f),
                torsoPoint(p,.76f,-body.chestHalfWidth*.49f),
                3.4f,2.8f,highlight);
        drawLimb(canvas,torsoPoint(p,.38f,body.waistHalfWidth*.57f),
                torsoPoint(p,.74f,body.chestHalfWidth*.60f),
                2f,1.8f,palette.garmentShade);
    }

    /**
     * No second curve implementation: side, front and top share the same
     * bounded Bézier caps. A joint's screen anchor never moves with paint.
     */
    private void drawLimb(Canvas c,AvatarKinematics.Point a,
                          AvatarKinematics.Point b,float wa,float wb,int fill) {
        contours.segment(c,a,b,wa,wb,fill);
    }

    private void drawJoint(Canvas c,AvatarKinematics.Point p,
                           float proximalWidth,float distalWidth,int fill){
        contours.hinge(c,p,proximalWidth,distalWidth,fill);
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
