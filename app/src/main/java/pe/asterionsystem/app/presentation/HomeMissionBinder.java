package pe.asterionsystem.app.presentation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONObject;
import pe.asterionsystem.app.MissionsActivity;
import pe.asterionsystem.app.ProfileStore;
import pe.asterionsystem.app.R;

/**
 * Binds exactly five approved homepage cards to existing and new features.
 * MainActivity still owns the existing progress/plan navigation.
 */
final class HomeMissionBinder {
    private HomeMissionBinder() {}

    static void bind(Activity activity, View screen) {
        JSONObject profile=ProfileStore.getRegistration(activity);
        boolean light="light".equals(profile.optString("theme","light"));
        TextView name=screen.findViewById(R.id.home_profile_name);
        String known=ProfileStore.getName(activity);
        name.setText(known.isEmpty()?"Cazador":known);

        screen.<Button>findViewById(R.id.open_monarch)
                .setOnClickListener(v->open(activity,"monarch"));
        screen.<Button>findViewById(R.id.open_daily)
                .setOnClickListener(v->open(activity,"daily"));
        screen.<Button>findViewById(R.id.open_library)
                .setOnClickListener(v->open(activity,"library"));
        screen.<Button>findViewById(R.id.open_profile)
                .setOnClickListener(v->new AlertDialog.Builder(activity)
                        .setTitle("Perfil del cazador")
                        .setMessage("Nombre: "+name.getText()
                                +"\nObjetivo: "+profile.optString("goal","Sin configurar")
                                +"\nModalidad: "+profile.optString("mode","Sin configurar")
                                +"\nTema: "+(light?"Blanco":"Negro"))
                        .setPositiveButton("Cerrar",null).show());

        apply(screen, light, activity.getResources().getDisplayMetrics().density);
    }

    private static void open(Activity activity, String section) {
        activity.startActivity(new Intent(activity,MissionsActivity.class)
                .putExtra("section",section));
    }

    private static void apply(View root, boolean light, float density) {
        int fg=light?Color.BLACK:Color.WHITE;
        int bg=light?Color.WHITE:0xFF080808;
        if(root instanceof TextView) {
            TextView text=(TextView)root;
            text.setTextColor(fg);
            text.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            if(root instanceof Button) {
                GradientDrawable drawable=new GradientDrawable();
                drawable.setColor(bg);
                drawable.setStroke(Math.max(1,Math.round(density)),
                        light?0xFFB0B0B0:0xFF777777);
                drawable.setCornerRadius(9*density);
                root.setBackground(drawable);
                root.setMinimumHeight(Math.round(48*density));
            }
        }
        if(root instanceof LinearLayout) {
            LinearLayout layout=(LinearLayout)root;
            if(root.getId()==R.id.home_root || root.getBackground()!=null) {
                layout.setBackgroundColor(bg);
            }
        }
        if(root.getId()==R.id.home_scroll)root.setBackgroundColor(bg);
        if(root instanceof ViewGroup) {
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++)apply(group.getChildAt(i),light,density);
        }
    }
}
