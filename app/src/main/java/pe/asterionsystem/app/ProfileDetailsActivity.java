package pe.asterionsystem.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import org.json.JSONObject;

/**
 * Profile access via the upper-right home avatar. Existing registration can
 * be edited without silently resetting the active profile or mission data.
 * Former secondary tools stay reachable in a collapsed options dialog.
 */
public final class ProfileDetailsActivity extends ComponentActivity {
    private LinearLayout content;
    private Button backButton;
    private float density;
    private boolean light;

    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        density=getResources().getDisplayMetrics().density;
        light="light".equals(ProfileStore.getRegistration(this).optString("theme","light"));
        LinearLayout shell=new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setPadding(dp(18),dp(16),dp(18),dp(14));
        ScrollView scroller=new ScrollView(this);
        content=new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        scroller.addView(content);
        shell.addView(scroller,new LinearLayout.LayoutParams(-1,0,1));
        backButton=button("Volver");
        backButton.setOnClickListener(v->finish());
        shell.addView(backButton,new LinearLayout.LayoutParams(-1,dp(52)));
        setContentView(shell);
        render();
    }

    @Override protected void onResume() {
        super.onResume();
        if(content!=null)render();
    }

    private void render() {
        JSONObject data=ProfileStore.getRegistration(this);
        light="light".equals(data.optString("theme","light"));
        if(backButton!=null)styleButton(backButton);
        content.removeAllViews();
        content.getRootView().setBackgroundColor(light?Color.WHITE:0xFF080808);
        content.addView(title("MI PERFIL",23));
        TextView hint=label("Puedes consultar tu configuración y modificarla. "
                +"Los cambios se guardan solo después de confirmarlos.",13);
        content.addView(hint);
        ImageView avatar=new ImageView(this);
        avatar.setImageResource(R.drawable.asterion_reg_profile);
        avatar.setContentDescription("Fotografía o avatar del perfil");
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        String uri=data.optString("avatar_uri","");
        if(!uri.isEmpty()) {
            try {
                avatar.setImageURI(Uri.parse(uri));
                if(avatar.getDrawable()==null)
                    avatar.setImageResource(R.drawable.asterion_reg_profile);
            }catch(RuntimeException badUri) {
                avatar.setImageResource(R.drawable.asterion_reg_profile);
            }
        }
        avatar.setBackgroundResource(R.drawable.asterion_avatar_frame);
        avatar.setClipToOutline(true);
        LinearLayout.LayoutParams photo=new LinearLayout.LayoutParams(dp(80),dp(80));
        photo.topMargin=dp(16);photo.bottomMargin=dp(12);
        content.addView(avatar,photo);
        content.addView(title(ProfileStore.getName(this),20));
        content.addView(label("Objetivo: "+data.optString("goal","Sin configurar"),14));
        content.addView(label("Nivel configurado: "
                +data.optString("experience","Sin configurar"),14));
        content.addView(label("Modalidad: "
                +data.optString("mode","Sin configurar"),14));
        content.addView(title("DURACIÓN DE RUTINAS",16));
        int preferredMinutes=RoutineDurationPreference.parse(
                data.optString("minutes","30"));
        content.addView(label("Tiempo disponible por rutina: "
                +preferredMinutes+" minutos (orientativo)",14));
        Button changeDuration=button("Cambiar tiempo de rutina");
        changeDuration.setContentDescription(
                "Cambiar tiempo disponible por rutina. Actual: "
                +preferredMinutes+" minutos");
        LinearLayout.LayoutParams timeLayout=new LinearLayout.LayoutParams(-1,dp(54));
        timeLayout.topMargin=dp(8);
        timeLayout.bottomMargin=dp(8);
        content.addView(changeDuration,timeLayout);
        changeDuration.setOnClickListener(v->chooseRoutineDuration());
        content.addView(label("Esta preferencia permanece visible incluso "
                +"si utilizas Misiones; las rutinas no son obligatorias.",12));
        content.addView(label("Preferencia de intensidad: "
                +data.optString("intensity","Sin configurar"),14));
        content.addView(label("Tema: "+(light?"Claro":"Oscuro"),14));
        Button edit=button("Editar mi perfil y avatar");
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(54));
        lp.topMargin=dp(22);
        content.addView(edit,lp);
        edit.setOnClickListener(v->startActivity(
                new Intent(this,ProfileCreateActivity.class)
                        .putExtra("edit_profile",true)));

        Button tools=button("Herramientas y configuración");
        LinearLayout.LayoutParams other=new LinearLayout.LayoutParams(-1,dp(54));
        other.topMargin=dp(12);
        content.addView(tools,other);
        tools.setOnClickListener(v->showOtherTools());
        content.addView(label("Tu perfil permanece guardado localmente. "
                +"Modificar un ajuste de entrenamiento puede requerir una "
                +"nueva revisión de Monarca, sin sustituir sesiones aceptadas.",12));
    }

    private void chooseRoutineDuration() {
        final String[] labels=RoutineDurationPreference.displayOptions();
        int selected=RoutineDurationPreference.indexOf(
                RoutineDurationPreference.parse(ProfileStore.getRegistration(this)
                        .optString("minutes","30")));
        new AlertDialog.Builder(this)
                .setTitle("Tiempo disponible por rutina")
                .setSingleChoiceItems(labels,selected,(dialog,index)->{
                    int value=RoutineDurationPreference.options()[index];
                    JSONObject updated=ProfileStore.getRegistration(this);
                    try {
                        updated.put("minutes",String.valueOf(value));
                    } catch(org.json.JSONException invalid) {
                        Toast.makeText(this,"No se pudo actualizar la duración.",
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        return;
                    }
                    if(ProfileStore.saveRegistration(this,updated)) {
                        dialog.dismiss();
                        render();
                    } else {
                        Toast.makeText(this,"No se pudo guardar la preferencia. "
                                +"Revisa los datos de tu perfil.",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancelar",null).show();
    }

    private void showOtherTools() {
        String[] options={"Planificación","Configuración","Estado local",
                "Seguimiento histórico","Biblioteca clásica","Más información"};
        String[] sections={"plan","settings","local_status","progress",
                "catalog","secondary"};
        new AlertDialog.Builder(this)
                .setTitle("Otras herramientas")
                .setItems(options,(dialog,which)->{
                    startActivity(new Intent(this,MainActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    |Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            .putExtra("asterion_open_section",sections[which]));
                })
                .setNegativeButton("Cerrar",null).show();
    }

    private TextView title(String text,int size) {
        TextView v=label(text,size);
        v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return v;
    }
    private TextView label(String text,int size) {
        TextView view=new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(light?0xFF111111:Color.WHITE);
        view.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        view.setPadding(0,dp(10),0,dp(5));
        return view;
    }
    private Button button(String text) {
        Button button=new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        button.setMinHeight(dp(48));
        styleButton(button);
        return button;
    }
    private void styleButton(Button button) {
        button.setTextColor(light?Color.BLACK:Color.WHITE);
        GradientDrawable background=new GradientDrawable();
        background.setColor(light?Color.WHITE:0xFF14131A);
        background.setStroke(dp(1),light?0xFFBBBBBB:0xFF777777);
        background.setCornerRadius(dp(10));
        button.setBackground(background);
    }
    private int dp(int size){return Math.round(size*density);}
}
