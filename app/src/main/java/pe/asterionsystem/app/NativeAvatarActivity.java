package pe.asterionsystem.app;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
    private Button play;
    private SeekBar timeline;
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
        blue.setOnClickListener(v->{avatar.setFemale(false);status.setText("Avatar azul · Sentadilla");});
        purple.setOnClickListener(v->{avatar.setFemale(true);status.setText("Avatar morado · Sentadilla");});
        root.addView(genders);

        status=text("Avatar azul · Sentadilla",13,true);
        root.addView(status);
        timeline=new SeekBar(this);
        timeline.setMax(1000);
        timeline.setContentDescription("Posición en el ciclo de sentadilla");
        root.addView(timeline,new LinearLayout.LayoutParams(-1,dp(52)));
        timeline.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override public void onProgressChanged(SeekBar seek,int progress,
                                                    boolean fromUser) {
                if(fromUser)avatar.setPhase(progress/1000f);
            }
            @Override public void onStartTrackingTouch(SeekBar seek) {
                scrubbing=true;
                avatar.setPlaying(false);
                updatePlay();
            }
            @Override public void onStopTrackingTouch(SeekBar seek) {
                scrubbing=false;
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
            avatar.setPlaying(!avatar.isPlaying());
            updatePlay();
        });
        reset.setOnClickListener(v->{avatar.reset();timeline.setProgress(0);});
        speed.setOnClickListener(v->{
            float next=avatar.getSpeed()>=1f?.5f:1f;
            avatar.setSpeed(next);
            speed.setText(next==1f?"Velocidad 1×":"Velocidad 0,5×");
        });
        root.addView(text("Movimiento esquemático; no comprueba tu postura "
                +"ni sustituye la orientación de un profesional. "
                +"La profundidad y la inclinación son orientativas.",12,false));
        Button back=button("Volver a Rutinas");
        root.addView(back,new LinearLayout.LayoutParams(-1,dp(52)));
        back.setOnClickListener(v->finish());

        if(state!=null) {
            avatar.setFemale(state.getBoolean("female",false));
            avatar.setSpeed(state.getFloat("speed",1f));
            avatar.setPhase(state.getFloat("phase",0f));
            avatar.setPlaying(state.getBoolean("playing",true));
            status.setText(avatar.isFemale()?"Avatar morado · Sentadilla"
                    :"Avatar azul · Sentadilla");
            speed.setText(avatar.getSpeed()==1f?"Velocidad 1×":"Velocidad 0,5×");
        }
        updatePlay();
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        state.putBoolean("female",avatar.isFemale());
        state.putBoolean("playing",avatar.isPlaying());
        state.putFloat("phase",avatar.getPhase());
        state.putFloat("speed",avatar.getSpeed());
        super.onSaveInstanceState(state);
    }
    @Override protected void onPause(){
        // Stop rendering invisible frames to avoid unnecessary battery usage.
        if(avatar!=null)avatar.setPlaying(false);
        super.onPause();
    }
    private void updatePlay(){
        if(play!=null)play.setText(avatar.isPlaying()?"Pausar":"Reproducir");
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
