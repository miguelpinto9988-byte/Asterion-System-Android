package pe.asterionsystem.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.SystemClock;
import android.view.View;

/**
 * Native Android 2D illustration — no Godot, network, MP4, images or extra SDK.
 *
 * Organic filled Bezier silhouettes follow an articulated Java pose model;
 * the renderer is intentionally independent from the daily/Monarca logic.
 * A stylized teaching illustration, NOT a clinical biomechanics measurement.
 */
public final class NativeAvatarView extends View {
    private static final float DESIGN_WIDTH=400f;
    private static final float DESIGN_HEIGHT=480f;
    private static final float LOOP_MS=4200f;
    private static final int OUTLINE=0xFF26334C;
    private static final int SKIN=0xFFF0B486;
    private static final int SKIN_SHADOW=0xFFDE986B;
    private static final int HAIR=0xFF49352D;
    private static final int HAIR_GLEAM=0xFF79564A;
    private static final int SHOE=0xFFE6EBF5;
    private static final int SHOE_DARK=0xFF657287;
    private static final int MALE_TOP=0xFF24559D;
    private static final int MALE_HIGHLIGHT=0xFF5584C8;
    private static final int FEMALE_TOP=0xFF7850B1;
    private static final int FEMALE_HIGHLIGHT=0xFFB194E2;
    private static final int TROUSERS=0xFF33384E;

    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();
    private boolean female=false;
    private boolean playing=true;
    private boolean dark=false;
    private float phase=0f;
    private float speed=1f;
    private long lastFrameMs=0;
    private long lastNotifyMs=0;
    private FrameListener listener;

    public interface FrameListener { void onPhase(float value); }

    public NativeAvatarView(Context context) {
        super(context);
        updateAccessibilityDescription();
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    public void setFemale(boolean value) {
        female=value;
        updateAccessibilityDescription();
        invalidate();
    }
    public boolean isFemale(){return female;}
    public void setDark(boolean value){dark=value;invalidate();}
    public void setFrameListener(FrameListener value){listener=value;}
    public void setSpeed(float value) {
        if(!Float.isFinite(value))return;
        speed=Math.max(.5f,Math.min(1.5f,value));
        invalidate();
    }
    public float getSpeed(){return speed;}
    public float getPhase(){return phase;}
    public boolean isPlaying(){return playing;}
    public void setPlaying(boolean value){
        playing=value;lastFrameMs=SystemClock.uptimeMillis();
        updateAccessibilityDescription();
        invalidate();
    }
    public void reset(){
        phase=0f;lastFrameMs=SystemClock.uptimeMillis();invalidate();
    }
    public void setPhase(float value){
        phase=Float.isNaN(value)?0f:Math.max(0f,Math.min(1f,value));
        lastFrameMs=SystemClock.uptimeMillis();
        invalidate();
    }

    private void updateAccessibilityDescription() {
        setContentDescription((female?"Avatar morado":"Avatar azul")
                +" realizando una sentadilla ilustrativa. "
                +(playing?"Animación en reproducción.":"Animación pausada."));
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        lastFrameMs=SystemClock.uptimeMillis();
        if(playing)postInvalidateOnAnimation();
    }
    @Override protected void onDetachedFromWindow(){
        lastFrameMs=0;
        super.onDetachedFromWindow();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long now=SystemClock.uptimeMillis();
        if(playing&&isShown()) {
            if(lastFrameMs>0) {
                float dt=Math.max(0,Math.min(80,now-lastFrameMs));
                phase=(phase+dt*speed/LOOP_MS)%1f;
            }
            if(listener!=null&&now-lastNotifyMs>85) {
                lastNotifyMs=now;
                listener.onPhase(phase);
            }
        }
        lastFrameMs=now;
        if(getWidth()<=0||getHeight()<=0)return;
        float scale=Math.min(getWidth()/DESIGN_WIDTH,getHeight()/DESIGN_HEIGHT);
        canvas.save();
        canvas.translate((getWidth()-DESIGN_WIDTH*scale)*0.5f,
                (getHeight()-DESIGN_HEIGHT*scale)*0.5f);
        canvas.scale(scale,scale);

        final int bg=dark?0xFF13131D:0xFFF8F9FD;
        final int grid=dark?0xFF272838:0xFFEAECF4;
        final int top=female?FEMALE_TOP:MALE_TOP;
        final int hi=female?FEMALE_HIGHLIGHT:MALE_HIGHLIGHT;
        drawRoundedRect(canvas,10,8,390,468,20,bg,0);
        color(grid);
        paint.setStrokeWidth(1f);
        for(int x=50;x<=350;x+=50)canvas.drawLine(x,40,x,405,paint);
        for(int y=70;y<=400;y+=55)canvas.drawLine(35,y,365,y,paint);

        drawRoundedRect(canvas,38,389,362,392,2,dark?0xFF666B82:0xFFD7DCE7,0);
        drawOval(canvas,113,373,284,389,dark?0xFF242532:0xFFE0E4EB);
        AvatarKinematics.Pose p=AvatarKinematics.squat(
                AvatarKinematics.depthAtPhase(phase));

        // Far limbs first. Rounded pieces overlap at each hinge; joints do
        // not display a separate dark ring when the skeleton bends.
        drawShoe(canvas,p.farAnkle,true);
        drawLimb(canvas,p.farHip,p.farKnee,29f,23f,
                female?0xFF4D486A:0xFF8F95A8);
        drawLimb(canvas,p.farKnee,p.farAnkle,22f,15f,
                female?0xFF514D70:SKIN_SHADOW);
        drawJoint(canvas,p.farKnee,11.5f,female?0xFF514D70:SKIN_SHADOW);
        drawLimb(canvas,p.farShoulder,p.farElbow,19f,16f,SKIN_SHADOW);
        drawLimb(canvas,p.farElbow,p.farWrist,17f,11f,SKIN_SHADOW);
        drawJoint(canvas,p.farElbow,8f,SKIN_SHADOW);
        drawHand(canvas,p.farWrist,SKIN_SHADOW);

        // Front leg: continuous rounded leg silhouettes under sportswear.
        drawShoe(canvas,p.ankle,false);
        drawLimb(canvas,p.hip,p.knee,33f,24f,female?0xFF45405F:SKIN);
        drawLimb(canvas,p.knee,p.ankle,24f,16f,female?0xFF45405F:SKIN);
        drawJoint(canvas,p.knee,12.5f,female?0xFF45405F:SKIN);
        if(!female) {
            AvatarKinematics.Point hemStart=interp(p.hip,p.knee,.47f);
            AvatarKinematics.Point hemEnd=interp(p.hip,p.knee,.50f);
            drawLimb(canvas,p.hip,hemStart,38f,30f,TROUSERS);
            drawLimb(canvas,hemStart,hemEnd,28f,25f,hi);
        } else {
            drawLimb(canvas,interp(p.hip,p.knee,.21f),
                    interp(p.hip,p.knee,.80f),3f,2.6f,hi);
        }

        // Contoured torso instead of the former straight cylinder.
        drawTorso(canvas,p,top,hi);
        drawLimb(canvas,p.shoulder,p.neck,21f,13f,SKIN);
        drawHead(canvas,p.head);
        if(!female) {
            drawLimb(canvas,p.shoulder,interp(p.shoulder,p.elbow,.29f),
                    26f,21f,top);
            drawLimb(canvas,interp(p.shoulder,p.elbow,.23f),p.elbow,
                    19f,17f,SKIN);
        } else {
            drawLimb(canvas,p.shoulder,p.elbow,19f,16f,SKIN);
            drawJoint(canvas,p.shoulder,12f,top);
        }
        drawLimb(canvas,p.elbow,p.wrist,17f,12f,SKIN);
        drawJoint(canvas,p.elbow,8.8f,SKIN);
        drawHand(canvas,p.wrist,SKIN);

        if(dark) {
            paint.setColor(0xFFADB4CE);
        }else{
            paint.setColor(0xFF63718C);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(12);
        paint.setTypeface(android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD));
        canvas.drawText(female?"AVATAR MORADO":"AVATAR AZUL",38,436,paint);
        canvas.drawText("SENTADILLA · ILUSTRACIÓN 2D",38,453,paint);
        canvas.restore();
        if(playing&&isAttachedToWindow()&&isShown())postInvalidateOnAnimation();
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
    private AvatarKinematics.Point torsoPoint(AvatarKinematics.Pose pose,
                                               float along,float side) {
        float dx=pose.shoulder.x-pose.hip.x;
        float dy=pose.shoulder.y-pose.hip.y;
        float length=(float)Math.hypot(dx,dy);
        float nx=-dy/length,ny=dx/length;
        return new AvatarKinematics.Point(
                pose.hip.x+dx*along+nx*side,
                pose.hip.y+dy*along+ny*side);
    }

    private void drawTorso(Canvas canvas,AvatarKinematics.Pose p,
                           int top,int highlight) {
        AvatarKinematics.Point a=torsoPoint(p,0f,-21f);
        AvatarKinematics.Point b=torsoPoint(p,.42f,-20f);
        AvatarKinematics.Point c=torsoPoint(p,.78f,-31f);
        AvatarKinematics.Point d=torsoPoint(p,1f,-23f);
        AvatarKinematics.Point e=torsoPoint(p,1f,23f);
        AvatarKinematics.Point f=torsoPoint(p,.78f,31f);
        AvatarKinematics.Point g=torsoPoint(p,.42f,20f);
        AvatarKinematics.Point h=torsoPoint(p,0f,21f);
        AvatarKinematics.Point v,v2;

        path.rewind();
        path.moveTo(a.x,a.y);
        v=torsoPoint(p,.15f,-24f);v2=torsoPoint(p,.25f,-21f);
        path.cubicTo(v.x,v.y,v2.x,v2.y,b.x,b.y);
        v=torsoPoint(p,.55f,-20f);v2=torsoPoint(p,.65f,-30f);
        path.cubicTo(v.x,v.y,v2.x,v2.y,c.x,c.y);
        v=torsoPoint(p,.93f,-32f);v2=torsoPoint(p,1f,-29f);
        path.cubicTo(v.x,v.y,v2.x,v2.y,d.x,d.y);
        v=torsoPoint(p,1.09f,0f);
        path.quadTo(v.x,v.y,e.x,e.y);
        v=torsoPoint(p,1f,29f);v2=torsoPoint(p,.93f,32f);
        path.cubicTo(v.x,v.y,v2.x,v2.y,f.x,f.y);
        v=torsoPoint(p,.65f,30f);v2=torsoPoint(p,.55f,20f);
        path.cubicTo(v.x,v.y,v2.x,v2.y,g.x,g.y);
        v=torsoPoint(p,.25f,21f);v2=torsoPoint(p,.15f,24f);
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

        // One compact garment highlight, without high-contrast outlines.
        drawLimb(canvas,torsoPoint(p,.19f,-12f),
                torsoPoint(p,.78f,-19f),4.4f,3.7f,highlight);
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
                far?0xFFADB7D4:SHOE,1.5f);
        drawRoundedRect(c,x-8,y-10,x+30,y+4,8,
                far?0xFF687696:(female?0xFF9C81D0:0xFF3A69B0),1f);
        color(SHOE_DARK);
        paint.setStrokeWidth(2f);
        c.drawLine(x-7,y+8,x+36,y+8,paint);
    }
    private void drawHand(Canvas c,AvatarKinematics.Point wrist,int skin){
        drawOval(c,wrist.x-7,wrist.y-7,wrist.x+10,wrist.y+6,skin);
    }

    private void drawHead(Canvas c,AvatarKinematics.Point center) {
        float x=center.x,y=center.y;
        if(female) {
            drawOval(c,x-25,y-26,x+13,y+27,HAIR);
            // Ponytail follows the head as one piece.
            path.reset();path.moveTo(x-18,y-18);
            path.cubicTo(x-41,y-39,x-47,y-14,x-39,y+4);
            path.cubicTo(x-44,y+19,x-31,y+22,x-25,y+16);
            path.quadTo(x-20,y+1,x-18,y-18);path.close();
            color(HAIR);c.drawPath(path,paint);
        }
        drawOval(c,x-18,y-23,x+20,y+21,SKIN);
        path.reset();
        path.moveTo(x-19,y-7);
        path.cubicTo(x-22,y-26,x-8,y-34,x+11,y-28);
        path.cubicTo(x+25,y-26,x+22,y-10,x+19,y-9);
        path.quadTo(x+7,y-17,x-2,y-13);
        path.quadTo(x-12,y-11,x-19,y-7);path.close();
        color(HAIR);c.drawPath(path,paint);
        color(HAIR_GLEAM);
        c.drawOval(x-13,y-22,x+8,y-19,paint);
        color(OUTLINE);
        c.drawCircle(x+11,y-1,1.7f,paint);
        paint.setStrokeWidth(1.3f);
        c.drawLine(x+17,y+10,x+11,y+12,paint);
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
            paint.setColor(OUTLINE);
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
