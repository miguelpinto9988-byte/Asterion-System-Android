package pe.asterionsystem.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.View;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Animated anatomical COMMAND MAP for SPEC_ONLY catalog variants.
 * This is NOT an exercise demonstration or an inferred biomechanical pose.
 * A highlighted pivot indicates a declared 2D joint command; it does not
 * assert planted contacts, equipment, posture or a valid exercise technique.
 */
public final class AvatarJointCommandView extends View {
    private static final float WIDTH=400f,HEIGHT=480f,CYCLE_MS=4200f;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arc=new RectF();
    private final List<AvatarJointTimeline.Joint> affected=new ArrayList<>();
    private AvatarJointTimeline timeline;
    private String variant="";
    private boolean dark=false,purple=false,playing=false;
    private float phase=0f,speed=1f;
    private long lastFrame=0,lastNotify=0;
    private FrameListener listener;
    public interface FrameListener { void onPhase(float value); }
    public AvatarJointCommandView(Context context){
        super(context);
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        setContentDescription("Mapa de comandos articulares, no demostración del ejercicio");
    }
    public void setEntry(AvatarExerciseCommandCatalog.Entry entry){
        if(entry==null)throw new IllegalArgumentException("Ficha requerida");
        timeline=entry.timeline();
        variant=entry.variantId;
        affected.clear();
        for(Map.Entry<AvatarJointTimeline.Joint,Float> item:
                timeline.peakOffsets().entrySet())
            if(Math.abs(item.getValue())>.0001f)affected.add(item.getKey());
        affected.sort(Comparator.comparingDouble(
                (AvatarJointTimeline.Joint j)->
                        -Math.abs(timeline.peakOffsets().get(j))));
        phase=0;lastFrame=SystemClock.uptimeMillis();
        setContentDescription("Ejercicio "+variant+". Diagrama de "+
                affected.size()+" comandos articulares en movimiento, no "+
                "representa la ejecución completa ni el equipamiento.");
        invalidate();
    }
    public void setDark(boolean value){dark=value;invalidate();}
    public void setFemale(boolean value){purple=value;invalidate();}
    public void setFrameListener(FrameListener value){listener=value;}
    public void setPlaying(boolean value){
        playing=value;lastFrame=SystemClock.uptimeMillis();invalidate();
    }
    public boolean isPlaying(){return playing;}
    public void reset(){setPhase(0f);}
    public void setPhase(float value){
        if(!Float.isFinite(value))throw new IllegalArgumentException("Fase finita");
        phase=Math.max(0,Math.min(1,value));
        lastFrame=SystemClock.uptimeMillis();invalidate();
    }
    public float getPhase(){return phase;}
    public void setSpeed(float value){
        if(!Float.isFinite(value))return;
        speed=Math.max(.5f,Math.min(1.5f,value));
    }
    public float getSpeed(){return speed;}
    @Override protected void onAttachedToWindow(){
        super.onAttachedToWindow();lastFrame=SystemClock.uptimeMillis();
        if(playing)postInvalidateOnAnimation();
    }
    @Override protected void onDetachedFromWindow(){
        lastFrame=0;super.onDetachedFromWindow();
    }
    private void style(int color,float width,boolean stroke){
        paint.setColor(color);paint.setStrokeWidth(width);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(stroke?Paint.Style.STROKE:Paint.Style.FILL);
    }
    private void text(Canvas c,String s,float x,float y,int size,int color){
        style(color,1,false);paint.setTextSize(size);
        paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        c.drawText(s,x,y,paint);
    }
    private void line(Canvas c,float ax,float ay,float bx,float by,int color,float w){
        style(color,w,true);c.drawLine(ax,ay,bx,by,paint);
    }
    private void dot(Canvas c,float x,float y,float radius,int color){
        style(color,1,false);c.drawCircle(x,y,radius,paint);
    }
    private float[] pivot(AvatarJointTimeline.Joint joint){
        switch(joint){
            case NECK_PITCH:case NECK_TURN:case NECK_SIDE:return new float[]{200,91};
            case LEFT_SHOULDER:case LEFT_SHOULDER_SIDE:return new float[]{151,135};
            case RIGHT_SHOULDER:case RIGHT_SHOULDER_SIDE:return new float[]{249,135};
            case LEFT_SCAPULA:return new float[]{168,157};
            case RIGHT_SCAPULA:return new float[]{232,157};
            case LEFT_ELBOW:return new float[]{125,196};
            case RIGHT_ELBOW:return new float[]{275,196};
            case LEFT_WRIST:return new float[]{118,235};
            case RIGHT_WRIST:return new float[]{282,235};
            case SPINE_BEND:case SPINE_TURN:return new float[]{200,190};
            case PELVIS:return new float[]{200,252};
            case LEFT_HIP:case LEFT_HIP_SIDE:return new float[]{177,262};
            case RIGHT_HIP:case RIGHT_HIP_SIDE:return new float[]{223,262};
            case LEFT_KNEE:return new float[]{176,323};
            case RIGHT_KNEE:return new float[]{224,323};
            case LEFT_ANKLE:return new float[]{174,375};
            case RIGHT_ANKLE:return new float[]{226,375};
            default:throw new IllegalStateException("Articulación sin mapa");
        }
    }
    private void figure(Canvas c,AvatarCommandFigurePose p,int skeleton,int accent){
        // This rigid-line figure moves according to axis commands, not
        // recorded human kinematics. It intentionally has no ground plane.
        line(c,(p.leftHip.x+p.rightHip.x)/2f,(p.leftHip.y+p.rightHip.y)/2f,
                (p.leftShoulder.x+p.rightShoulder.x)/2f,
                (p.leftShoulder.y+p.rightShoulder.y)/2f,skeleton,7);
        line(c,p.leftShoulder.x,p.leftShoulder.y,
                p.rightShoulder.x,p.rightShoulder.y,skeleton,6);
        line(c,p.leftHip.x,p.leftHip.y,p.rightHip.x,p.rightHip.y,skeleton,8);
        line(c,p.leftShoulder.x,p.leftShoulder.y,
                p.leftElbow.x,p.leftElbow.y,accent,7);
        line(c,p.leftElbow.x,p.leftElbow.y,p.leftWrist.x,p.leftWrist.y,accent,5);
        line(c,p.leftWrist.x,p.leftWrist.y,p.leftHand.x,p.leftHand.y,accent,3);
        line(c,p.rightShoulder.x,p.rightShoulder.y,
                p.rightElbow.x,p.rightElbow.y,accent,7);
        line(c,p.rightElbow.x,p.rightElbow.y,p.rightWrist.x,p.rightWrist.y,accent,5);
        line(c,p.rightWrist.x,p.rightWrist.y,p.rightHand.x,p.rightHand.y,accent,3);
        line(c,p.leftHip.x,p.leftHip.y,p.leftKnee.x,p.leftKnee.y,accent,9);
        line(c,p.leftKnee.x,p.leftKnee.y,p.leftAnkle.x,p.leftAnkle.y,accent,6);
        line(c,p.leftAnkle.x,p.leftAnkle.y,p.leftToe.x,p.leftToe.y,accent,3);
        line(c,p.rightHip.x,p.rightHip.y,p.rightKnee.x,p.rightKnee.y,accent,9);
        line(c,p.rightKnee.x,p.rightKnee.y,p.rightAnkle.x,p.rightAnkle.y,accent,6);
        line(c,p.rightAnkle.x,p.rightAnkle.y,p.rightToe.x,p.rightToe.y,accent,3);
        line(c,(p.leftShoulder.x+p.rightShoulder.x)/2f,
                (p.leftShoulder.y+p.rightShoulder.y)/2f,p.neck.x,p.neck.y,
                skeleton,5);
        line(c,p.neck.x,p.neck.y,p.head.x,p.head.y,accent,5);
        style(accent,3,true);c.drawCircle(p.head.x,p.head.y,17f,paint);
        for(AvatarKinematics.Point joint:new AvatarKinematics.Point[]{
                p.leftShoulder,p.rightShoulder,p.leftElbow,p.rightElbow,
                p.leftWrist,p.rightWrist,p.leftHip,p.rightHip,
                p.leftKnee,p.rightKnee,p.leftAnkle,p.rightAnkle})
            dot(c,joint.x,joint.y,3.5f,skeleton);
    }
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        if(getWidth()<=0||getHeight()<=0)return;
        long now=SystemClock.uptimeMillis();
        if(playing&&timeline!=null&&isShown()){
            if(lastFrame>0){
                float dt=Math.max(0f,Math.min(80f,now-lastFrame));
                phase=(phase+dt*speed/CYCLE_MS)%1f;
            }
            if(listener!=null&&now-lastNotify>=85){
                listener.onPhase(phase);lastNotify=now;
            }
        }
        lastFrame=now;
        float scale=Math.min(getWidth()/WIDTH,getHeight()/HEIGHT);
        canvas.save();
        canvas.translate((getWidth()-WIDTH*scale)/2f,
                (getHeight()-HEIGHT*scale)/2f);
        canvas.scale(scale,scale);
        int bg=dark?0xFF171722:0xFFF7F8FC;
        int skeleton=dark?0xFF626476:0xFF9AA4B8;
        int fg=dark?0xFFF1F2FA:0xFF26293A;
        int muted=dark?0xFFB3B7CA:0xFF646C84;
        int accent=purple?0xFFB69BEA:0xFF4B9AE1;
        style(bg,1,false);canvas.drawRoundRect(8,6,392,473,20,20,paint);
        text(canvas,"MAPA ARTICULAR ANIMADO",30,32,18,fg);
        text(canvas,"No demuestra la ejecución del ejercicio",30,52,12,muted);
        if(timeline==null){
            text(canvas,"Sin comandos cargados",44,236,16,fg);
            canvas.restore();return;
        }
        AvatarCommandFigurePose moving=AvatarCommandFigurePose.from(timeline,phase);
        figure(canvas,moving,skeleton,accent);
        // Angular needles illustrate the selected joint offset, never a
        // complete full-body pose or a medically precise joint angle.
        for(AvatarJointTimeline.Joint joint:affected){
            float[] pos=pivot(joint);
            float value=timeline.offset(joint,phase);
            float radius=joint==AvatarJointTimeline.Joint.NECK_SIDE
                    ||joint==AvatarJointTimeline.Joint.NECK_TURN?18f:14f;
            style(accent,2.4f,true);
            arc.set(pos[0]-radius,pos[1]-radius,pos[0]+radius,pos[1]+radius);
            canvas.drawArc(arc,-90f,value,false,paint);
            float a=(float)Math.toRadians(-90f+value);
            line(canvas,pos[0],pos[1],
                    pos[0]+radius*(float)Math.cos(a),
                    pos[1]+radius*(float)Math.sin(a),accent,2.5f);
            dot(canvas,pos[0],pos[1],4f,accent);
        }
        int visible=Math.min(affected.size(),6);
        for(int i=0;i<visible;i++){
            AvatarJointTimeline.Joint j=affected.get(i);
            float val=timeline.offset(j,phase);
            int col=i%2,row=i/2;
            float x=30+col*193,y=404+row*21;
            String label=j.name().replace("LEFT_","I ").replace("RIGHT_","D ")
                    .replace("SHOULDER","HOMBRO").replace("KNEE","RODILLA")
                    .replace("ELBOW","CODO").replace("HIP","CADERA")
                    .replace("ANKLE","TOBILLO").replace("NECK","CUELLO")
                    .replace("SPINE","TRONCO").replace("_"," ");
            if(label.length()>19)label=label.substring(0,18);
            text(canvas,label,x,y,10,fg);
            text(canvas,Math.round(val)+"°",x+146,y,10,accent);
        }
        if(affected.size()>6)
            text(canvas,"+"+(affected.size()-6)+" ejes activos en la ficha",29,470,10,muted);
        text(canvas,"Fase "+Math.round(phase*100f)+" %",301,55,13,accent);
        style(accent,5f,false);
        canvas.drawRoundRect(29,384,29+342*phase,388,2,2,paint);
        canvas.restore();
        if(playing&&isAttachedToWindow()&&isShown())
            postInvalidateOnAnimation();
    }
}
