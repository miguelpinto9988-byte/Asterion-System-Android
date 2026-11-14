package pe.asterionsystem.app;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

/**
 * Ficha móvil por variante: jamás muestra un GIF de otro ID.
 * Los movimientos sin revisión técnica NO son demostraciones aprobadas.
 */
public final class ExerciseMotionDetailActivity extends Activity {
    public static final String EXTRA_EXERCISE="exercise_v2_json";
    public static final String EXTRA_MOTION_SPEC="motion_spec_v1_json";
    private ImageView animation;
    private OfflineExerciseMp4Player player;
    private boolean dark;
    private int fg;
    private float density;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        density=getResources().getDisplayMetrics().density;
        dark=!"light".equals(ProfileStore.getRegistration(this)
                .optString("theme","light"));
        fg=dark?Color.WHITE:0xFF10131B;
        LinearLayout outer=column();
        outer.setPadding(dp(16),dp(12),dp(16),dp(12));
        outer.setBackgroundColor(dark?0xFF10121B:0xFFF6F7FA);
        TextView title=label("DEMOSTRACIÓN · ASTERION",19,true);
        outer.addView(title);
        ScrollView scroll=new ScrollView(this);
        LinearLayout content=column();
        content.setPadding(0,dp(8),0,dp(18));
        scroll.addView(content);
        outer.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        Button close=button("Volver al catálogo");
        close.setOnClickListener(v->finish());
        outer.addView(close,new LinearLayout.LayoutParams(-1,dp(50)));
        setContentView(outer);

        final JSONObject exercise,spec;
        try {
            exercise=new JSONObject(getIntent().getStringExtra(EXTRA_EXERCISE));
            spec=new JSONObject(getIntent().getStringExtra(EXTRA_MOTION_SPEC));
            String id=exercise.getString("id");
            if(!id.equals(spec.getString("id"))
                    ||!id.matches("[a-z0-9_-]+")) {
                throw new JSONException("Los IDs no coinciden");
            }
        } catch(Exception invalid) {
            content.addView(label("No se pudo verificar la ficha de esta variante. "
                    +"No se mostrará una animación incorrecta.",15,false));
            return;
        }
        String id=exercise.optString("id");
        String name=exercise.optString("nombre_es");
        content.addView(label(name,19,true));
        content.addView(label("ID único · "+id,12,false));
        content.addView(label("Personaje original · anime oscuro · aura azul",12,false));

        // El área gráfica se ajusta al dispositivo y nunca recorta la postura.
        LinearLayout demo=column();
        demo.setPadding(dp(8),dp(8),dp(8),dp(8));
        GradientDrawable gradient=new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFF242939,0xFF10131D});
        gradient.setCornerRadius(dp(16));
        demo.setBackground(gradient);
        int displayWidth=getResources().getDisplayMetrics().widthPixels-dp(48);
        int displayHeight=Math.min(dp(430),Math.round(displayWidth*9f/16f));
        animation=new ImageView(this);
        animation.setScaleType(ImageView.ScaleType.FIT_CENTER);
        animation.setAdjustViewBounds(true);
        animation.setContentDescription("Animación específica de "+name);
        demo.addView(animation,new LinearLayout.LayoutParams(-1,displayHeight));
        TextView mediaStatus=label("",13,false);
        mediaStatus.setGravity(Gravity.CENTER);
        demo.addView(mediaStatus);
        content.addView(demo);

        String mp4Path=spec.optString("mp4_ruta","");
        String path=spec.optString("gif_ruta","");
        boolean mp4Available=mp4Path.equals("exercise_mp4/v1/"+id+".mp4")
                &&fileExists(getAssets(),mp4Path);
        // Formato oficial: MP4 H.264, 16:9. Sin sustitucion por GIF.
        boolean available=false; // Asterion produce y reproduce solo MP4.
        if(mp4Available) {
            try {
                player=new OfflineExerciseMp4Player(this,id,mp4Path);
                animation.setVisibility(View.GONE);
                demo.addView(player,0,new LinearLayout.LayoutParams(-1,displayHeight));
                mediaStatus.setText("MP4 original de este ejercicio · técnica por verificar");
                Button toggle=button("Pausar o reanudar MP4");
                toggle.setOnClickListener(v->{
                    if(player!=null)player.toggle();
                });
                content.addView(toggle);
            } catch(IOException failed) {
                mediaStatus.setText("No se puede abrir el MP4 de esta variante.");
                animation.setVisibility(View.VISIBLE);
                TextView pending=label("Vista previa temporalmente no disponible.",13,false);
                demo.addView(pending);
            }
        } else if(available) {
            Uri local=Uri.parse("file:///android_asset/"+path);
            boolean reducedMotion=Settings.Global.getFloat(getContentResolver(),
                    Settings.Global.ANIMATOR_DURATION_SCALE,1f)==0f;
            if(reducedMotion) {
                mediaStatus.setText("Fotograma estático: animaciones reducidas en Android.");
                Glide.with(this).asBitmap().load(local).into(animation);
            } else {
                mediaStatus.setText("Vista previa de esta variante · técnica por verificar");
                Glide.with(this).asGif().load(local).into(animation);
                Button toggle=button("Pausar o reanudar animación");
                toggle.setOnClickListener(v->{
                    Drawable shown=animation.getDrawable();
                    if(shown instanceof GifDrawable) {
                        GifDrawable gif=(GifDrawable)shown;
                        if(gif.isRunning()) gif.stop();
                        else gif.start();
                    }
                });
                content.addView(toggle);
            }
        } else {
            animation.setBackgroundColor(0xFF191F2E);
            animation.setImageDrawable(null);
            TextView placeholder=label("MP4 DE ESTA VARIANTE PENDIENTE",14,true);
            placeholder.setGravity(Gravity.CENTER);
            demo.addView(placeholder);
            mediaStatus.setText("No se sustituye por un ejercicio parecido.");
        }

        content.addView(label("CUADRO DE ESPECIFICACIONES",18,true));
        field(content,"Variante", join(spec.optJSONArray("variante")));
        field(content,"Categoría",exercise.optString("categoria","Por clasificar"));
        field(content,"Grupo principal",spec.optString("region","Por verificar"));
        field(content,"Material",spec.optString("material","Por verificar"));
        field(content,"Nivel técnico",spec.optString("nivel","Por revisar"));
        field(content,"Posición inicial",spec.optString("posicion","Por revisar"));
        field(content,"Apoyos y agarre",spec.optString("agarre_o_apoyo","Por revisar"));
        field(content,"Movimiento",spec.optString("recorrido_general","Por revisar"));
        field(content,"Diferencia entre variantes",
                spec.optString("criterio_diferenciador","Por revisar"));
        content.addView(label("INFORMACIÓN TÉCNICA",17,true));
        JSONArray steps=exercise.optJSONArray("instrucciones_es");
        if(steps!=null&&steps.length()>0) {
            for(int i=0;i<steps.length();i++) {
                content.addView(label((i+1)+". "+steps.optString(i),13,false));
            }
        } else {
            content.addView(label("Esta variante histórica todavía no tiene "
                    +"instrucciones individuales en el visor.",13,false));
        }
        content.addView(label("Revisión: pendiente de aprobación técnica individual. "
                +"Las descripciones son orientativas; no constituyen "
                +"una demostración verificada ni una rutina obligatoria.",12,false));
    }

    private boolean fileExists(AssetManager assets,String file) {
        if(!file.matches("(exercise_gifs/v1/[a-z0-9_-]+\\.gif)"
                +"|(exercise_mp4/v1/[a-z0-9_-]+\\.mp4)"))return false;
        try(java.io.InputStream stream=assets.open(file)){return stream.read()!=-1;}
        catch(IOException missing){return false;}
    }
    private static String join(JSONArray values) {
        if(values==null||values.length()==0)return "Pendiente";
        StringBuilder out=new StringBuilder();
        for(int i=0;i<values.length();i++) {
            if(i>0)out.append(" · ");
            out.append(values.optString(i).replace('_',' '));
        }
        return out.toString();
    }
    private void field(LinearLayout parent,String name,String value) {
        TextView heading=label(name,12,true);
        heading.setTextColor(dark?0xFF92C8FF:0xFF235A9B);
        parent.addView(heading);
        TextView body=label(value,14,false);
        body.setPadding(dp(10),0,dp(10),dp(7));
        parent.addView(body);
    }
    private LinearLayout column() {
        LinearLayout layout=new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }
    private TextView label(String value,int size,boolean bold) {
        TextView t=new TextView(this);
        t.setText(value);t.setTextSize(size);t.setTextColor(fg);
        t.setPadding(dp(5),dp(7),dp(5),dp(7));
        t.setTypeface(Typeface.DEFAULT,bold?Typeface.BOLD:Typeface.NORMAL);
        return t;
    }
    private Button button(String text) {
        Button button=new Button(this);
        button.setText(text);button.setTextColor(fg);button.setAllCaps(false);
        return button;
    }
    private int dp(int px){return Math.max(1,Math.round(px*density));}
    @Override protected void onPause() {
        if(player!=null)player.pauseForLifecycle();
        super.onPause();
    }
    @Override protected void onResume() {
        super.onResume();
        if(player!=null)player.resumeForLifecycle();
    }
    @Override protected void onDestroy() {
        if(player!=null)player.release();
        if(animation!=null)Glide.with(this).clear(animation);
        super.onDestroy();
    }
}
