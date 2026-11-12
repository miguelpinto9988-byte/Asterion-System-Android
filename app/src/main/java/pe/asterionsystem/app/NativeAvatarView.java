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
    private final AvatarDedicatedSceneRenderer dedicatedRenderer=new AvatarDedicatedSceneRenderer();
    private AvatarMultiViewKinematics.ViewMode viewMode=AvatarMultiViewKinematics.ViewMode.SIDE;
    private AvatarPalette palette=AvatarPalette.BLUE;
    private AvatarBodySpec bodySpec=AvatarBodySpec.DEFAULT;
    private AvatarMotionEngine.Motion motion=AvatarMotionEngine.Motion.SQUAT;
    private AvatarDedicatedScene.Kind dedicatedScene;
    private AvatarVariantScene.Recipe variantScene;
    private final AvatarVariantSceneRenderer variantRenderer=new AvatarVariantSceneRenderer();
    public void setVariantScene(AvatarVariantScene.Recipe recipe){
        if(recipe==null)throw new IllegalArgumentException("Recipe required");
        variantScene=recipe;dedicatedScene=null;
        viewMode=AvatarMultiViewKinematics.ViewMode.SIDE;
        phase=0f;lastFrameMs=SystemClock.uptimeMillis();
        updateAccessibilityDescription();invalidate();
    }
    public AvatarVariantScene.Recipe getVariantScene(){return variantScene;}


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
    public AvatarDedicatedScene.Kind getDedicatedScene(){return dedicatedScene;}
    public void setDedicatedScene(AvatarDedicatedScene.Kind scene){
        if(scene==null)throw new IllegalArgumentException("Escena de apoyo requerida");
        variantScene=null;
        dedicatedScene=scene;
        viewMode=AvatarMultiViewKinematics.ViewMode.SIDE;
        phase=0f;
        lastFrameMs=SystemClock.uptimeMillis();
        updateAccessibilityDescription();invalidate();
    }
    public AvatarMultiViewKinematics.ViewMode getViewMode(){return viewMode;}
    public void setViewMode(AvatarMultiViewKinematics.ViewMode value){
        if(value==null)throw new IllegalArgumentException("Vista requerida");
        variantScene=null;
        viewMode=value;
        if(value!=AvatarMultiViewKinematics.ViewMode.SIDE)dedicatedScene=null;
        phase=0f;
        lastFrameMs=SystemClock.uptimeMillis();
        updateAccessibilityDescription();
        invalidate();
    }
    public void setMotion(AvatarMotionEngine.Motion value){
        if(value==null)throw new IllegalArgumentException("Movimiento obligatorio");
        variantScene=null;
        motion=value;
        dedicatedScene=null;
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
        if(variantScene!=null){
            setContentDescription((female?"Avatar morado":"Avatar azul")+" · "+variantScene.label+
                    " · "+variantScene.equipment+" · ilustración corporal 2D. "+
                    (playing?"En reproducción.":"Pausada."));return;
        }
        String activity;
        switch(viewMode) {
            case SIDE:
                activity=dedicatedScene==null?motion.label:
                        dedicatedScene==AvatarDedicatedScene.Kind.GLUTE_BRIDGE_SHORT
                        ?"puente corto de glúteos sobre colchoneta":
                        dedicatedScene==AvatarDedicatedScene.Kind.GLUTE_BRIDGE_BASIC
                        ?"puente de glúteos sobre colchoneta":
                        dedicatedScene==AvatarDedicatedScene.Kind.SEATED_TOE_RAISE
                        ?"elevación bilateral de puntas sentado sobre silla":
                        dedicatedScene==AvatarDedicatedScene.Kind.SEATED_FORWARD_REACH
                        ?"alcance frontal sentado sobre silla":
                        dedicatedScene==AvatarDedicatedScene.Kind.GLUTE_BRIDGE_PAUSE
                        ?"puente de glúteos sobre colchoneta con pausa":
                        dedicatedScene==AvatarDedicatedScene.Kind.GLUTE_BRIDGE_BAND
                        ?"puente de glúteos sobre colchoneta con banda visible":
                        dedicatedScene==AvatarDedicatedScene.Kind.SEATED_TOE_RAISE_SINGLE
                        ?"dorsiflexión de un pie sentado sobre silla":
                        dedicatedScene==AvatarDedicatedScene.Kind.SEATED_HEEL_RAISE
                        ?"elevación de talones sentado sobre silla":
                        dedicatedScene==AvatarDedicatedScene.Kind.SEATED_HEEL_RAISE_GENTLE
                        ?"elevación suave de talones sentado sobre silla":
                        dedicatedScene==AvatarDedicatedScene.Kind.SEATED_HEEL_TOE_ALTERNATION
                        ?"alternancia de talones y puntas sentado sobre silla":
                        dedicatedScene==AvatarDedicatedScene.Kind.STANDING_HEEL_RAISE
                        ?"elevación bilateral de talones de pie sin material":
                        dedicatedScene==AvatarDedicatedScene.Kind.STANDING_TOE_RAISE
                        ?"elevación bilateral de puntas de pie sin material":
                        dedicatedScene==AvatarDedicatedScene.Kind.SEATED_KNEE_EXTENSION_PAUSE
                        ?"extensión de rodilla sentado con pausa":
                        dedicatedScene==AvatarDedicatedScene.Kind.SEATED_KNEE_EXTENSION_ALTERNATING
                        ?"extensión alternada de rodillas sentado":
                        "extensión de rodilla sentado sobre silla";
                break;
            case FRONT: activity="elevación frontal de brazos";break;
            case FRONT_SQUAT: activity="sentadilla abierta en cámara frontal";break;
            case FRONT_JACKS: activity="saltos de tijera en cámara frontal";break;
            case FRONT_ELBOW: activity="flexión de codos en cámara frontal";break;
            case FRONT_LATERAL: activity="separación lateral de brazos en cámara frontal";break;
            case FRONT_BALANCE: activity="equilibrio sobre una pierna en cámara frontal";break;
            case FRONT_SIDE_STEP: activity="paso lateral sin banda en cámara frontal";break;
            case FRONT_NECK_TILT: activity="inclinación cervical lateral en cámara frontal";break;
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

        if(variantScene!=null){
            variantRenderer.draw(canvas,AvatarVariantScene.at(variantScene,phase,bodySpec),
                    bodySpec,palette,dark);
        }else if(viewMode==AvatarMultiViewKinematics.ViewMode.SIDE
                &&dedicatedScene!=null){
            AvatarDedicatedScene posture=AvatarDedicatedScene.at(
                    dedicatedScene,phase,bodySpec);
            dedicatedRenderer.draw(canvas,posture,bodySpec,palette,dark);
        }else if(viewMode==AvatarMultiViewKinematics.ViewMode.SIDE) {
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
                case FRONT_NECK_TILT:
                    pose=AvatarMultiViewKinematics.frontNeckSideTilt(phase,bodySpec);
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
