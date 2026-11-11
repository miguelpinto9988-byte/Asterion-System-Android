package pe.asterionsystem.app.presentation;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONObject;
import pe.asterionsystem.app.MissionsActivity;
import pe.asterionsystem.app.ProfileDetailsActivity;
import pe.asterionsystem.app.ProfileStore;
import pe.asterionsystem.app.R;
import pe.asterionsystem.app.SimpleBottomNav;

/** Exactly five vertical cards; profile entry is the top-right avatar only. */
final class HomeMissionBinder {
    private HomeMissionBinder() {}

    static void bind(Activity activity,View screen) {
        JSONObject profile=ProfileStore.getRegistration(activity);
        boolean light="light".equals(profile.optString("theme","light"));
        String name=ProfileStore.getName(activity);
        screen.<TextView>findViewById(R.id.home_profile_name)
                .setText(name.isEmpty()?"Cazador":name);
        screen.<TextView>findViewById(R.id.home_profile_summary).setText(
                "Nivel configurado: "+profile.optString("experience","Sin configurar")
                +" · Objetivo: "+profile.optString("goal","Sin configurar")
                +". Para editar tu perfil, toca el avatar de arriba.");

        screen.<Button>findViewById(R.id.open_monarch)
                .setOnClickListener(v->open(activity,"monarch"));
        screen.<Button>findViewById(R.id.open_daily)
                .setOnClickListener(v->open(activity,"daily"));
        screen.<Button>findViewById(R.id.open_library)
                .setOnClickListener(v->open(activity,"library"));
        screen.<Button>findViewById(R.id.open_progress)
                .setOnClickListener(v->open(activity,"progress"));

        ImageButton avatar=screen.findViewById(R.id.home_avatar);
        String savedUri=profile.optString("avatar_uri","");
        if(!savedUri.isEmpty()) {
            try {
                avatar.setImageURI(Uri.parse(savedUri));
                if(avatar.getDrawable()==null)
                    avatar.setImageResource(R.drawable.asterion_reg_profile);
            } catch(RuntimeException badUri) {
                avatar.setImageResource(R.drawable.asterion_reg_profile);
            }
        }
        avatar.setOnClickListener(v->activity.startActivity(
                new Intent(activity,ProfileDetailsActivity.class)));

        apply(screen,light,activity.getResources().getDisplayMetrics().density);
        SimpleBottomNav.attach(activity,
                (FrameLayout)screen.findViewById(R.id.home_bottom_bar),
                SimpleBottomNav.HOME);
    }

    private static void open(Activity activity,String section) {
        activity.startActivity(new Intent(activity,MissionsActivity.class)
                .putExtra("section",section));
    }

    private static boolean isCard(View view) {
        int id=view.getId();
        return id==R.id.home_card_monarch || id==R.id.home_card_profile
                || id==R.id.home_card_daily || id==R.id.home_card_progress
                || id==R.id.home_card_library;
    }

    private static void apply(View root,boolean light,float density) {
        int fg=light?0xFF151515:Color.WHITE;
        int bg=light?Color.WHITE:0xFF080808;
        if(root instanceof TextView) {
            TextView text=(TextView)root;
            text.setTextColor(fg);
            text.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            if(root instanceof Button) {
                GradientDrawable shape=new GradientDrawable();
                shape.setColor(light?0xFFF9F9FB:0xFF16151C);
                shape.setStroke(Math.max(1,Math.round(density)),
                        light?0xFFB9B9C2:0xFF77717F);
                shape.setCornerRadius(9*density);
                root.setBackground(shape);
                root.setMinimumHeight(Math.round(48*density));
            }
        }
        if(isCard(root)) {
            GradientDrawable rectangle=new GradientDrawable();
            rectangle.setColor(light?0xFFFFFFFF:0xFF14131A);
            rectangle.setStroke(Math.max(1,Math.round(density)),
                    light?0xFFD4D0D9:0xFF635A70);
            rectangle.setCornerRadius(14*density);
            root.setBackground(rectangle);
        }else if(root.getId()==R.id.home_shell ||
                root.getId()==R.id.home_root ||
                root.getId()==R.id.home_scroll) {
            root.setBackgroundColor(bg);
        }
        if(root instanceof ViewGroup) {
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++)
                apply(group.getChildAt(i),light,density);
        }
    }
}
