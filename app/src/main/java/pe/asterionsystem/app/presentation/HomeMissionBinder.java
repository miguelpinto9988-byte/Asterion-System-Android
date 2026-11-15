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
                profile.optString("experience","Sin configurar")
                +" · "+profile.optString("goal","Sin configurar"));

        screen.<Button>findViewById(R.id.open_monarch)
                .setOnClickListener(v->open(activity,"monarch"));
        screen.<Button>findViewById(R.id.open_daily)
                .setOnClickListener(v->open(activity,"daily"));
        screen.<Button>findViewById(R.id.open_library)
                .setOnClickListener(v->open(activity,"library"));
        screen.<Button>findViewById(R.id.open_progress)
                .setOnClickListener(v->open(activity,"progress"));

        ImageButton avatar=screen.findViewById(R.id.home_avatar);
        pe.asterionsystem.app.AppHeader.bindAvatar(activity,avatar);
        avatar.setOnClickListener(v->activity.startActivity(new Intent(activity,ProfileDetailsActivity.class)));
        pe.asterionsystem.app.AppTheme.apply(screen,light);
        pe.asterionsystem.app.HomeOverview.bind(activity,screen);
        SimpleBottomNav.attach(activity,
                (FrameLayout)screen.findViewById(R.id.home_bottom_bar),
                SimpleBottomNav.HOME);
    }

    private static void open(Activity activity,String section) {
        activity.startActivity(new Intent(activity,MissionsActivity.class)
                .putExtra("section",section));
    }

}
