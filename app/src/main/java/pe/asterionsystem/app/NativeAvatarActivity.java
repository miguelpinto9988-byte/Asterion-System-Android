package pe.asterionsystem.app;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.activity.ComponentActivity;

/**
 * Educational 2D animation viewer, executed entirely in the Android APK.
 * No other app, engine, remote service, generated MP4 or network required.
 */
public final class NativeAvatarActivity extends ComponentActivity {
    private NativeAvatarView avatar;
    private final AvatarPlaybackController playback=new AvatarPlaybackController();
    private Button play;
    private SeekBar timeline;
    private Spinner motionPicker;
    private Spinner scenePicker;
    private TextView status;
    private boolean light;
    private boolean scrubbing;
    private float density;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        density=getResources().getDisplayMetrics().density;
        light="light".equals(ProfileStore.getRegistration(this).optString("theme","light"));
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14),dp(16),dp(14),dp(16));
        root.setBackgroundColor(light?Color.WHITE:0xFF080808);
        setContentView(root);

        root.addView(text("ASTERION · AVATAR 2D NATIVO",19,true));
        root.addView(text("Animación ilustrativa creada en Java y Canvas. "
                +"No requiere Godot, conexión ni archivos MP4.",12,false));
        avatar=new NativeAvatarView(this);
        avatar.setDark(!light);
        root.addView(avatar,new LinearLayout.LayoutParams(-1,0,1));
        avatar.setContentDescription("Demostración ilustrada de sentadilla con avatar articulado");

        LinearLayout genders=row();
        Button blue=button("Chico azul");
        Button purple=button("Chica morada");
        genders.addView(blue,new LinearLayout.LayoutParams(0,dp(52),1));
        genders.addView(purple,new LinearLayout.LayoutParams(0,dp(52),1));
        blue.setOnClickListener(v->{avatar.setFemale(false);updatePlay();});
        purple.setOnClickListener(v->{avatar.setFemale(true);updatePlay();});
        root.addView(genders);

        // The catalog is not mapped automatically: only authored and reviewed
        // 2D examples appear here, each with explicit anchors and targets.
        motionPicker=new Spinner(this);
        AvatarMotionEngine.Motion[] choices=AvatarMotionEngine.Motion.values();
        String[] labels=new String[choices.length];
        for(int i=0;i<choices.length;i++)labels[i]=choices[i].label;
        ArrayAdapter<String> motionAdapter=new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,labels);
        motionAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        motionPicker.setAdapter(motionAdapter);
        motionPicker.setContentDescription("Ejemplo de movimiento ilustrado");
        motionPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(AdapterView<?> parent,
                    View v,int position,long id){
                avatar.setMotion(choices[position]);
                if(timeline!=null)timeline.setProgress(0);
                updatePlay();
            }
            @Override public void onNothingSelected(AdapterView<?> parent){}
        });
        root.addView(motionPicker,new LinearLayout.LayoutParams(-1,dp(50)));

        scenePicker=new Spinner(this);
        AvatarMultiViewKinematics.ViewMode[] views=
                AvatarMultiViewKinematics.ViewMode.values();
        String[] cameraLabels=new String[views.length];
        for(int i=0;i<views.length;i++)cameraLabels[i]=views[i].title;
        ArrayAdapter<String> cameraAdapter=new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,cameraLabels);
        cameraAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        scenePicker.setAdapter(cameraAdapter);
        scenePicker.setContentDescription("Seleccionar cámara del avatar");
        scenePicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(AdapterView<?> parent,View v,
                                                    int position,long id){
                avatar.setViewMode(views[position]);
                // Other scenes own their own exercise-specific rig.
                motionPicker.setEnabled(views[position]==AvatarMultiViewKinematics.ViewMode.SIDE);
                if(timeline!=null)timeline.setProgress(0);
                updatePlay();
            }
            @Override public void onNothingSelected(AdapterView<?> parent){}
        });
        root.addView(scenePicker,new LinearLayout.LayoutParams(-1,dp(50)));

        status=text("Avatar azul · Sentadilla",13,true);
        root.addView(status);
        timeline=new SeekBar(this);
        timeline.setMax(1000);
        timeline.setContentDescription("Posición en el ciclo del ejercicio ilustrado");
        root.addView(timeline,new LinearLayout.LayoutParams(-1,dp(52)));
        timeline.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override public void onProgressChanged(SeekBar seek,int progress,
                                                    boolean fromUser) {
                if(fromUser)avatar.setPhase(progress/1000f);
            }
            @Override public void onStartTrackingTouch(SeekBar seek) {
                scrubbing=true;
                playback.beginScrub();
                applyPlayback();
            }
            @Override public void onStopTrackingTouch(SeekBar seek) {
                scrubbing=false;
                playback.endScrub();
                applyPlayback();
            }
        });
        avatar.setFrameListener(value->{
            if(!scrubbing)timeline.setProgress(Math.round(value*1000f));
        });

        LinearLayout controls=row();
        play=button("Pausar");
        Button reset=button("Reiniciar");
        Button speed=button("Velocidad 1×");
        controls.addView(play,new LinearLayout.LayoutParams(0,dp(52),1));
        controls.addView(reset,new LinearLayout.LayoutParams(0,dp(52),1));
        controls.addView(speed,new LinearLayout.LayoutParams(0,dp(52),1));
        root.addView(controls);
        play.setOnClickListener(v->{
            playback.toggleRequested();
            applyPlayback();
        });
        reset.setOnClickListener(v->{avatar.reset();timeline.setProgress(0);});
        speed.setOnClickListener(v->{
            float next=avatar.getSpeed()>=1f?.5f:1f;
            avatar.setSpeed(next);
            speed.setText(next==1f?"Velocidad 1×":"Velocidad 0,5×");
        });
        root.addView(text("Cámaras ilustrativas independientes: lateral, frontal y "
                +"superior con ampliación de manos. El visor no comprueba la "
                +"postura ni sustituye orientación profesional.",12,false));
        Button back=button("Volver a Rutinas");
        root.addView(back,new LinearLayout.LayoutParams(-1,dp(52)));
        back.setOnClickListener(v->finish());

        if(state!=null) {
            avatar.setFemale(state.getBoolean("female",false));
            avatar.setSpeed(state.getFloat("speed",1f));
            avatar.setPhase(state.getFloat("phase",0f));
            playback.setPlaybackRequested(state.getBoolean("playing",true));
            String restored=state.getString("motion","SQUAT");
            try {
                AvatarMotionEngine.Motion selected=AvatarMotionEngine.Motion.valueOf(restored);
                motionPicker.setSelection(selected.ordinal());
                avatar.setMotion(selected);
                avatar.setPhase(state.getFloat("phase",0f));
            } catch(IllegalArgumentException unknown) {
                motionPicker.setSelection(0);
            }
            String storedCamera=state.getString("camera","SIDE");
            try {
                AvatarMultiViewKinematics.ViewMode restored=
                        AvatarMultiViewKinematics.ViewMode.valueOf(storedCamera);
                scenePicker.setSelection(restored.ordinal());
                avatar.setViewMode(restored);
                motionPicker.setEnabled(restored==AvatarMultiViewKinematics.ViewMode.SIDE);
                avatar.setPhase(state.getFloat("phase",0f));
            } catch(IllegalArgumentException unknown) {
                scenePicker.setSelection(0);
            }
            updatePlay();
            speed.setText(avatar.getSpeed()==1f?"Velocidad 1×":"Velocidad 0,5×");
        }
        applyPlayback();
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        state.putBoolean("female",avatar.isFemale());
        state.putString("motion",avatar.getMotion().name());
        state.putString("camera",avatar.getViewMode().name());
        state.putBoolean("playing",playback.isPlaybackRequested());
        state.putFloat("phase",avatar.getPhase());
        state.putFloat("speed",avatar.getSpeed());
        super.onSaveInstanceState(state);
    }
    @Override protected void onResume(){
        super.onResume();
        playback.onResume();
        applyPlayback();
    }
    @Override protected void onPause(){
        // The Activity may be hidden by Android without a manual pause.
        // Retain the user's preference so playback resumes only when requested.
        playback.onPause();
        applyPlayback();
        super.onPause();
    }
    @Override protected void onDestroy(){
        if(avatar!=null)avatar.setFrameListener(null);
        super.onDestroy();
    }
    private void applyPlayback(){
        if(avatar!=null)avatar.setPlaying(playback.shouldPlay());
        updatePlay();
    }
    private void updatePlay(){
        if(avatar==null)return;
        if(play!=null)play.setText(avatar.isPlaying()?"Pausar":"Reproducir");
        String scene=avatar.getViewMode()==AvatarMultiViewKinematics.ViewMode.SIDE
                ?avatar.getMotion().label:avatar.getViewMode().title;
        if(status!=null)status.setText((avatar.isFemale()?"Avatar morado":"Avatar azul")
                +" · "+scene+" · "
                +(avatar.isPlaying()?"Reproduciendo":"En pausa"));
    }
    private TextView text(String value,int sp,boolean prominent){
        TextView label=new TextView(this);
        label.setText(value);
        label.setTextSize(sp);
        label.setTextColor(light?0xFF1B1B26:Color.WHITE);
        label.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        label.setPadding(dp(4),dp(prominent?8:5),dp(4),dp(5));
        return label;
    }
    private LinearLayout row(){
        LinearLayout result=new LinearLayout(this);
        result.setOrientation(LinearLayout.HORIZONTAL);
        return result;
    }
    private Button button(String title){
        Button b=new Button(this);
        b.setText(title);
        b.setAllCaps(false);
        b.setTextSize(12);
        b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        b.setTextColor(light?Color.BLACK:Color.WHITE);
        b.setMinHeight(dp(48));
        GradientDrawable shape=new GradientDrawable();
        shape.setColor(light?Color.WHITE:0xFF191822);
        shape.setStroke(dp(1),light?0xFFD1D1DC:0xFF747080);
        shape.setCornerRadius(dp(9));
        b.setBackground(shape);
        return b;
    }
    private int dp(int n){return Math.round(n*density);}
}
