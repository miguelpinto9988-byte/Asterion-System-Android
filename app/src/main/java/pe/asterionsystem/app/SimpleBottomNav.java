package pe.asterionsystem.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import org.json.JSONObject;

/**
 * Only three global destinations: Inicio, Rutinas and Calendario.
 * Daily and Monarca live inside the Home cards / Missions screen,
 * while profile editing is reached from the header avatar.
 */
public final class SimpleBottomNav {
    public static final String HOME="home";
    public static final String ROUTINES="routines";
    public static final String CALENDAR="calendar";

    private SimpleBottomNav() {}

    public static void attach(Activity activity,FrameLayout host,String current) {
        host.removeAllViews();
        host.addView(create(activity,current),
                new FrameLayout.LayoutParams(-1,-1));
    }

    public static LinearLayout create(Activity activity,String current) {
        JSONObject settings=ProfileStore.getRegistration(activity);
        boolean light="light".equals(settings.optString("theme","light"));
        float density=activity.getResources().getDisplayMetrics().density;
        int foreground=light?0xFF161616:Color.WHITE;
        int background=light?Color.WHITE:0xFF080808;
        LinearLayout bar=new LinearLayout(activity);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(density,8),dp(density,4),dp(density,8),dp(density,4));
        bar.setBackgroundColor(background);
        add(activity,bar,"Inicio",HOME,current,foreground,background,light,density);
        add(activity,bar,"Rutinas",ROUTINES,current,foreground,background,light,density);
        add(activity,bar,"Calendario",CALENDAR,current,foreground,background,light,density);
        return bar;
    }

    private static void add(Activity activity,LinearLayout bar,String label,String route,
                            String current,int foreground,int background,
                            boolean light,float density) {
        final boolean selected=route.equals(current);
        Button button=new Button(activity);
        button.setAllCaps(false);
        button.setText(label);
        button.setTextSize(13);
        button.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        button.setTextColor(foreground);
        button.setContentDescription(label+(selected?", sección actual":""));
        button.setMinHeight(dp(density,48));
        GradientDrawable shape=new GradientDrawable();
        shape.setColor(selected?(light?0xFFF0EEF5:0xFF292432):background);
        shape.setStroke(dp(density,1),selected?0xFF9B83BD
                :(light?0xFFDDDDDD:0xFF5C5C5C));
        shape.setCornerRadius(dp(density,9));
        button.setBackground(shape);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(
                0,dp(density,50),1f);
        lp.setMargins(dp(density,3),0,dp(density,3),0);
        bar.addView(button,lp);
        button.setOnClickListener(v->navigate(activity,current,route));
    }

    private static void navigate(Activity activity,String current,String route) {
        if(current.equals(route))return;
        Class<?> target=HOME.equals(route)?MainActivity.class:
                ROUTINES.equals(route)?RoutinesActivity.class:CalendarActivity.class;
        Intent intent=new Intent(activity,target);
        if(HOME.equals(route)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        activity.startActivity(intent);
        // Avoid stacking a long chain of independent tab destinations.
        if(!(activity instanceof MainActivity))activity.finish();
    }
    private static int dp(float density,int size) {
        return Math.max(1,Math.round(size*density));
    }
}
