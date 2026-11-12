package pe.asterionsystem.app;

import android.content.Context;
import android.graphics.Canvas;
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
    private final AvatarRenderer renderer=new AvatarRenderer();
    private final AvatarMultiViewRenderer multiRenderer=new AvatarMultiViewRenderer();
    private AvatarMultiViewKinematics.ViewMode viewMode=AvatarMultiViewKinematics.ViewMode.SIDE;
    private AvatarPalette palette=AvatarPalette.BLUE;
    private AvatarBodySpec bodySpec=AvatarBodySpec.DEFAULT;
    private AvatarMotionEngine.Motion motion=AvatarMotionEngine.Motion.SQUAT;

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
        palette=value?AvatarPalette.PURPLE:AvatarPalette.BLUE;
        updateAccessibilityDescription();
        invalidate();
    }
    public boolean isFemale(){return female;}
    public AvatarMotionEngine.Motion getMotion(){return motion;}
    public AvatarMultiViewKinematics.ViewMode getViewMode(){return viewMode;}
    public void setViewMode(AvatarMultiViewKinematics.ViewMode value){
        if(value==null)throw new IllegalArgumentException("Vista requerida");
        viewMode=value;
        phase=0f;
        lastFrameMs=SystemClock.uptimeMillis();
        updateAccessibilityDescription();
        invalidate();
    }
    public void setMotion(AvatarMotionEngine.Motion value){
        if(value==null)throw new IllegalArgumentException("Movimiento obligatorio");
        motion=value;
        phase=0f;
        lastFrameMs=SystemClock.uptimeMillis();
        updateAccessibilityDescription();
        invalidate();
    }
    public void setBodySpec(AvatarBodySpec value){
        if(value==null)throw new IllegalArgumentException("BodySpec obligatorio");
        bodySpec=value;invalidate();
    }
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
        String activity;
        switch(viewMode) {
            case SIDE: activity=motion.label;break;
            case FRONT: activity="elevación frontal de brazos";break;
            case FRONT_SQUAT: activity="sentadilla abierta en cámara frontal";break;
            case FRONT_JACKS: activity="saltos de tijera en cámara frontal";break;
            case FRONT_ELBOW: activity="flexión de codos en cámara frontal";break;
            case FRONT_LATERAL: activity="separación lateral de brazos en cámara frontal";break;
            case FRONT_BALANCE: activity="equilibrio sobre una pierna en cámara frontal";break;
            case FRONT_SIDE_STEP: activity="paso lateral sin banda en cámara frontal";break;
            case TOP: activity="flexión en diamante desde arriba, con detalle de manos";break;
            default: activity="movimiento ilustrativo";
        }
        setContentDescription((female?"Avatar morado":"Avatar azul")
                +" mostrando "+activity.toLowerCase(java.util.Locale.ROOT)
                +" como animación ilustrativa. "
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
                // Multi-pose clips need enough time to show contacts, unlike
                // a two-keyframe arm raise.
                float cycleMs=viewMode==AvatarMultiViewKinematics.ViewMode.SIDE
                        &&motion==AvatarMotionEngine.Motion.BURPEE
                        ?LOOP_MS*1.6f:LOOP_MS;
                phase=(phase+dt*speed/cycleMs)%1f;
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

        if(viewMode==AvatarMultiViewKinematics.ViewMode.SIDE) {
            // Contact flags are authored by motion, never guessed from
            // the visible 2D ankle-to-knee segment.
            AvatarContactFrame.forSide(motion,phase);
            renderer.draw(canvas,AvatarMotionEngine.pose(motion,phase,bodySpec),
                    bodySpec,palette,dark,motion.label);
        } else {
            AvatarContactFrame contact=AvatarContactFrame.forCamera(viewMode,phase);
            if(viewMode==AvatarMultiViewKinematics.ViewMode.TOP)
                contact.assertProneUpperCamera();
            AvatarMultiViewKinematics.Pose pose;
            switch(viewMode) {
                case FRONT:
                    pose=AvatarMultiViewKinematics.frontArms(phase,bodySpec);
                    break;
                case FRONT_SQUAT:
                    pose=AvatarMultiViewKinematics.frontWideSquat(phase,bodySpec);
                    break;
                case FRONT_JACKS:
                    pose=AvatarMultiViewKinematics.frontJumpingJack(phase,bodySpec);
                    break;
                case FRONT_ELBOW:
                    pose=AvatarMultiViewKinematics.frontElbowFlex(phase,bodySpec);
                    break;
                case FRONT_LATERAL:
                    pose=AvatarMultiViewKinematics.frontLateralRaise(phase,bodySpec);
                    break;
                case FRONT_BALANCE:
                    pose=AvatarMultiViewKinematics.frontSingleLegBalance(phase,bodySpec);
                    break;
                case FRONT_SIDE_STEP:
                    pose=AvatarMultiViewKinematics.frontSideStep(phase,bodySpec);
                    break;
                case TOP:
                    pose=AvatarMultiViewKinematics.diamondTop(phase,bodySpec);
                    break;
                default:
                    throw new IllegalStateException("Camera not implemented");
            }
            multiRenderer.draw(canvas,pose,viewMode,palette,dark,bodySpec);
        }
        canvas.restore();
        if(playing&&isAttachedToWindow()&&isShown())postInvalidateOnAnimation();
    }

}
